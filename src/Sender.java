package lab0;

import java.net.*;
import java.io.*;

public class Sender extends Thread {
	
	private String hostName;
	private int portNumber;
	
	public Sender(String aName, int aNumber) {
		this.hostName = aName;
		this.portNumber = aNumber;
	}
	
	public void run() {
		try {
			// 
            Socket aSocket = new Socket(hostName, portNumber);
			ObjectOutputStream out = new ObjectOutputStream(aSocket.getOutputStream());
			// take message from the out_buffer
			Message m;
			out.writeObject(m);
            out.flush();
		}
		catch () {
		}
	}
	
}