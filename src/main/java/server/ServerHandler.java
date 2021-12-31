package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import model.Message;
import model.Status;

public class ServerHandler extends Thread {
	private final Socket socket;
	private final ObjectInputStream Input;
	private final ObjectOutputStream Output;
	private volatile boolean running = true;
	private List<String> rejected = new ArrayList<>();
	private String username;
	private String clientInfo;

	public ServerHandler(Socket socket, ObjectInputStream Input, ObjectOutputStream Output) {
		this.socket = socket;
		this.Input = Input;
		this.Output = Output;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getClientInfo() {
		return clientInfo;
	}

	public void setClientInfo(String clientInfo) {
		this.clientInfo = clientInfo;
	}

	@Override
	public void run() {
		Message received;
		try (socket; Input; Output;) {
			while (running) {
				received = (Message) Input.readObject();
				System.out.println(received);
				switch (received.getStatus()) {
				case CHAT:
					if (clientInfo != null && !clientInfo.isEmpty()) {
						Server.listMatched.get(clientInfo).sendMessage(received);
					}
					break;
				case EXIT:
					if (clientInfo == null) {
						Server.listWait.remove(username);
					} else {
						ServerHandler b;
						if (Server.listMatched.get(clientInfo) != null && Server.listMatched.get(clientInfo).getClientInfo() == null) {
							b = Server.listMatched.get(clientInfo);
							Message unmatch = new Message(username, null, Status.EXIT);
							b.sendMessage(unmatch);
							System.out.println("refused to send");
						}
						removeMatched();
						Server.listMatched.remove(username);
						Server.listWait.remove(username);
					}

					running = false;
					break;
				case OK:
					// peerInfo = received.getName();
					break;
				case DISCONNECT:
					ServerHandler bx;
					if (Server.listMatched.get(clientInfo) != null) {
						bx = Server.listMatched.get(clientInfo);
						Message unmatch = new Message(username, null, Status.EXIT);
						bx.sendMessage(unmatch);
						System.out.println("refused to send");
					}
					removeMatched();
					Server.listMatched.remove(username);
					break;

				case REFUSE:
					ServerHandler b;
					rejected.add(clientInfo);
					if (Server.listMatched.get(clientInfo) != null) {
						b = Server.listMatched.get(clientInfo);
					} else {
						b = Server.listWait.get(clientInfo);
					}
					Message unmatch = new Message(username, null, Status.UNMATCH);
					b.sendMessage(unmatch);
					System.out.println("refused to match");
					
					removeMatched();
					
					if (Server.listWait.size() - 1 > rejected.size() && Server.secondClient.equals(username)) {
						matching();
					}
					
					break;
				case MATCH:
					matching();
					break;
				default:
					if (Server.listMatched.get(received.getName()) != null
							|| Server.listWait.get(received.getName()) != null) {
						Message mess = new Message(username, null, Status.EXIST);
						sendMessage(mess);
					} else if (username == null) {
						username = received.getName();
						Server.listWait.put(username, this);
						Message welcome = new Message(username, null, Status.CONNECTED);
						System.out.println(welcome);
						sendMessage(welcome);
						Server.secondClient = username;
						matching();
					}
				}
			}
			System.out.println("ServerHanlder: " + username + " has left!");
		} catch (IOException | ClassNotFoundException | InterruptedException e) {
			e.printStackTrace();
		}

	}

	public void sendMessage(Message mess) throws IOException {
		Output.writeObject(mess);
		Output.flush();
	}

	public void matching() throws IOException, InterruptedException {
		System.out.println("Room making with");
		System.out.println(username);
		synchronized (this) {
			if (Server.listWait.size() > 1) {
				Server.listWait.remove(username);
				ArrayList<ServerHandler> list = new ArrayList<>(Server.listWait.values());
				list.removeIf(x -> rejected.contains(x.getUsername()));
				Collections.shuffle(list);
				ServerHandler b = list.get(0);
				setClientInfo(b.getUsername());
				b.setClientInfo(username);
				Server.listMatched.put(username, this);
				Server.listMatched.put(b.getUsername(), b);
				Message mess = new Message(clientInfo, null, Status.MATCH);
				sendMessage(mess);
				mess.setName(username);
				b.sendMessage(mess);
				Server.listWait.remove(b.getUsername());
				System.out.println("Room chat has been made");
			}
		}
	}

	public void removeMatched() {
		if (Server.listMatched.get(clientInfo) != null) {
			ServerHandler b = Server.listMatched.get(clientInfo);
			Server.listMatched.remove(clientInfo);
			Server.listWait.put(clientInfo, b);
			Server.listMatched.remove(username);
			Server.listWait.put(username, this);
		}
		setClientInfo(null);
	}

}
