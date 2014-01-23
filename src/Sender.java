package lab0;

import java.net.*;
import java.io.*;

public class Sender extends Thread {
	
	private Socket socket;
	private Message message;
	
	public Sender(Socket aSocket, Message aMessage) {
		socket = aSocket;
		message = aMessage;
	}
	
	public void run() {
		
		try {
			// 
            //Socket aSocket = new Socket(hostName, portNumber);
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			// take message from the out_buffer
			out.writeObject(message);
            out.flush();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}