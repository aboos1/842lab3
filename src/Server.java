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
			// check config
				if(mpasser.getLocalName().equals(conn.getName())) {
					System.out.println("Listening on port: " + conn.getPort());
					ServerSocket serverSocket = new ServerSocket(conn.getPort());
					while (true) {
						
						Thread.sleep(100);
						
						Socket clientSocket = serverSocket.accept();
						System.out.println("new connection from " + clientSocket.getRemoteSocketAddress().toString());
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
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}