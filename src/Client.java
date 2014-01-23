package lab0;

import java.net.*;
import java.io.*;
import java.util.HashMap;

public class Client extends Thread {

	private MessagePasser mpasser;
	private HashMap<String, Boolean> ssetup;
	//private HashMap<String, Socket> connections;
	private HashMap<String, ObjectOutputStream> outs;
	
	public Client(MessagePasser aPasser) {
		mpasser = aPasser;
		ssetup = new HashMap<String, Boolean>();
		//connections = new HashMap<String, Socket>();
		outs = new HashMap<String, ObjectOutputStream>();
	}
	
	public void run() {
		
		Message message;
		while(true) {
			
			// take a rest...
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// check if anything in out_buffer
			if(!mpasser.getOutBuffer().isEmpty()) {
				message = mpasser.getOutBuffer().removeFirst();
			
				if(ssetup.get(message.getDest()) != null) { 
				// if connection already set up	
					Sender sender = new Sender(message, outs.get(message.getDest()));
					sender.start();
				}
				else {
					try {
						// set up a new connection
						Socket socket = new Socket(message.getHostName(), message.getPort());
						ObjectOutputStream oout = new ObjectOutputStream(socket.getOutputStream());
						
						// store the connection
						ssetup.put(message.getDest(), true);					
						outs.put(message.getDest(), oout);
						
						Sender sender = new Sender(message, oout);
						sender.start();
					}
					catch (UnknownHostException e) {
						e.printStackTrace();
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}