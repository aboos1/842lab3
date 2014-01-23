package lab0;

import java.net.*;
import java.io.*;

public class Sender extends Thread {
	
	//private Socket socket;
	private Message message;
	private ObjectOutputStream oout;
	
	public Sender(Message aMessage, ObjectOutputStream aOout) {
		//socket = aSocket;
		message = aMessage;
		oout = aOout;
	}
	
	public void run() {
		
		try {

			System.out.println("Sending message #" + message.getSeqNum() + " (" + message.getKind() + ")"
										+ " from " + message.getSrc() + ": " + message.getData());
										
			oout.writeObject(message);
            oout.flush();

		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}