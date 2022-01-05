package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;


public class Server {
    static Map<String, ServerHandler> listWait = new LinkedHashMap<>();
    static Map<String, ServerHandler> listMatched = new LinkedHashMap<>();
    static String secondClient;
    
    public static void main(String args[]){
        try(ServerSocket serverSocket = new ServerSocket(1234);){
            Socket clienSocket ;
            while(true){
                clienSocket = serverSocket.accept();
                System.out.println("SERVER: Server is waiting for client");
                System.out.println("SERVER: " + clienSocket);
                ObjectOutputStream out = new ObjectOutputStream(clienSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clienSocket.getInputStream());
                ServerHandler newClient = new ServerHandler(clienSocket,in,out);
                Thread thread = new Thread(newClient);
                thread.start();
            }
        }
        catch (IOException e) {
        	e.printStackTrace();
        }

    }
}
