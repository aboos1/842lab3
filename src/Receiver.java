package lab0;

import java.net.*;
import java.io.*;
import java.util.LinkedList;

public class Receiver extends Thread {
	
	private Socket socket;
	private LinkedList<Message> in_buffer;
	
	public Receiver(Socket aSocket, LinkedList<Message> aBuffer) {
		this.socket = aSocket;
		this.in_buffer = aBuffer;
	}
	
	public void run() {
	
		try {
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			Message m;
			while(true) {
				m = (Message) in.readObject();
				in_buffer.add(m);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}