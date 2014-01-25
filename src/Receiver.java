//package lab0;

import java.net.*;
import java.io.*;
import java.util.LinkedList;

public class Receiver extends Thread {
	
	private Socket socket;
	private LinkedList<Message> in_buffer;
	private ObjectInputStream in;
	
	public Receiver(Socket aSocket, LinkedList<Message> aBuffer) {
		this.socket = aSocket;
		this.in_buffer = aBuffer;
	}
	
	public void teardown() {
		try {
			in.close();
		}
		catch (IOException e) {
			System.out.println("Closing...");
		}
	}
	
	public void run() {
	
		try {
			in = new ObjectInputStream(socket.getInputStream());
			Message m;
			while(true) {

				Thread.sleep(100);

				m = (Message) in.readObject();
				in_buffer.add(m);
			}
		}
		catch (IOException e) {
			//e.printStackTrace();
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		finally {
			try {
				socket.close();
				in.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}