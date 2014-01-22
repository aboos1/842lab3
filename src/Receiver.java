package lab0;

import java.net.*;
import java.io.*;

public class Receiver extends Thread {
	
	private int portNumber;
	
	public Receiver(int aNumber) {
		this.portNumber = aNumber;
	}
	
	public void run() {
		try {
			ServerSocket sSocket = new ServerSocket(portNumber);
			Socket clientSocket = sSocket.accept();
			ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
			Message m;
			while(true) {
				m = (Message) in.readObject();
				// push m to in_buffer
			}
		}
		catch () {
		}
	}
}