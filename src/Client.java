package lab0;

import java.net.*;
import java.io.*;
import java.util.HashMap;

public class Client extends Thread {

	private MessagePasser mpasser;
	private HashMap<String, Boolean> ssetup;
	private HashMap<String, Socket> connections;
	
	public Client(MessagePasser aPasser) {
		mpasser = aPasser;
		ssetup = new HashMap<String, Boolean>();
		connections = new HashMap<String, Socket>();
	}
	
	public void run() {
		// check if anything in out_buffer
		Message message;
		while(true) {
			if((message = mpasser.getOutBuffer().removeFirst())) {
				if(ssetup.get(message.getDest())) { // already set up
					Sender sender = new Sender(connections.get(message.getDest()), message);
					sender.start();
				}
				else {
					Socket socket = new Socket(message.getHostName(), message.getPort());
					ssetup.put(message.getDest(), true);
					connections.put(message.getDest(), socket);
					Sender sender = new Sender(socket, message);
					sender.start();
				}
			}
		}
	}
}