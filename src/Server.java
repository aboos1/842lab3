package lab0;

import java.net.*;
import java.io.*;

public class Server extends Thread {
	
	private MessagePasser mpasser;
	
	public Server(MessagePasser aPasser) {
		mpasser = aPasser;
	}
	
	public void run() {
		try {
			for(Connection conn: mpasser.getConnList()) {
				if(mpasser.getLocalName().equals(conn.getName())) {
					System.out.println("Listening on port: " + conn.getPort());
					ServerSocket serverSocket = new ServerSocket(conn.getPort());
					while (true) {
						Socket clientSocket = serverSocket.accept();
						Receiver receiver = new Receiver(clientSocket, mpasser.getInBuffer());
						receiver.start();
					}
				}
			}
			System.out.println("No matching local_name");
			System.exit(1);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}