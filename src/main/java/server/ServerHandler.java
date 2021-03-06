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
						if (Server.listMatched.get(clientInfo) != null
								&& Server.listMatched.get(clientInfo).getClientInfo().equals(username)) {
							b = Server.listMatched.get(clientInfo);
							Message exit = new Message(username, null, Status.EXIT);
							b.sendMessage(exit);
						}
						removeMatched();
						Server.listMatched.remove(username);
						Server.listWait.remove(username);
					}
					running = false;
					break;

				case OK:
					break;

				case DISCONNECT:
					ServerHandler bx;
					if (Server.listMatched.get(clientInfo) != null
							&& Server.listMatched.get(clientInfo).getClientInfo().equals(username)) {
						bx = Server.listMatched.get(clientInfo);
						Message exit = new Message(username, null, Status.EXIT);
						bx.sendMessage(exit);
					}
					removeMatched();
					Server.listMatched.remove(username);
					break;

				case REFUSE:
					ServerHandler b;
					// B??? client t??? ch???i v??o rejectedlist
					rejected.add(clientInfo);
					// L???y th??ng tin ng?????i b??? t??? ch???i
					if (Server.listMatched.get(clientInfo) != null) {
						b = Server.listMatched.get(clientInfo);
					} else {
						b = Server.listWait.get(clientInfo);
					}
					Message unmatch = new Message(username, null, Status.UNMATCH);
					b.sendMessage(unmatch);

					removeMatched();
					b.removeMatched();

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
						Message exist = new Message(username, null, Status.EXIST);
						sendMessage(exist);
					} else if (username == null) {
						username = received.getName();
						Server.listWait.put(username, this);
						Message connected = new Message(username, null, Status.CONNECTED);
						sendMessage(connected);
						Server.secondClient = username;
						matching();
					}
				}
			}
			System.out.println("SERVER: " + username + " has left!");
		} catch (IOException | ClassNotFoundException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void sendMessage(Message mess) throws IOException {
		Output.writeObject(mess);
		Output.flush();
	}

	public void matching() throws IOException, InterruptedException {
		synchronized (this) {
			// ki???m tra listwait > 1
			if (Server.listWait.size() > 1) {
				Server.listWait.remove(username);
				// Khai b??o 1 list ????? ch???a
				ArrayList<ServerHandler> list = new ArrayList<>(Server.listWait.values());
				// X??a nh???ng ng?????i ???? t???ng t??? ch???i tr?????c ????
				list.removeIf(x -> rejected.contains(x.getUsername()));
				// Tr???n danh s??ch l??n
				Collections.shuffle(list);
				// L???y th??ng tin ng?????i ?????u ti??n
				ServerHandler b = list.get(0);
				setClientInfo(b.getUsername());
				b.setClientInfo(username);
				// B??? 2 th???ng v??o list matched
				Server.listMatched.put(username, this);
				Server.listMatched.put(b.getUsername(), b);
				Message match = new Message(clientInfo, null, Status.MATCH);
				sendMessage(match);
				// Set t??n cho client th??? 2
				match.setName(username);
				b.sendMessage(match);
				Server.listWait.remove(b.getUsername());
			}
		}
	}

	public void removeMatched() {
		// Ki???m tra ???? c?? trong list match ch??a
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
