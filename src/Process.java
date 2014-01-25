//package lab0;
import java.io.*;
import java.util.ArrayList;

public class Process {
	public static void main(String[] args) {
		if(args.length != 2) {
			Usage();
			System.exit(1);
		}
		MessagePasser mpasser = new MessagePasser(args[0], args[1]);
		
		try 
		{
			mpasser.parseConfig();
		} 
		catch (FileNotFoundException e) 
		{
			System.out.println("File not found!");
		}
		
		Server server = new Server(mpasser);
		server.start();
		Client client = new Client(mpasser);
		client.start();
		
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		BufferedReader bufreader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			try {
				//System.out.print("$ ");
				String command = bufreader.readLine();
				String[] commandArgs = command.split(" ");
                
				if (command.equals("q")) {
					client.teardown();
					server.teardown();
					System.exit(1);
				}
				else if (command.equals("r")) {
					ArrayList<Message> messages = mpasser.receive();
					if (messages != null) {
						for(Message aMessage: messages) {
						//if (aMessage != null) {
							System.out.println("Message #" + aMessage.getSeqNum() + " (" + aMessage.getKind() + ")"
										+ " from " + aMessage.getSrc() + ": " + aMessage.getData());
						//}
						}
					}
				}
				else if (commandArgs[0].equals("s")) {
					
					if (commandArgs.length < 4) {
						Usage();
						continue;
					}
					//else {
					StringBuffer data = new StringBuffer();
					for(int i = 3; i < commandArgs.length; i++) {
						data.append(commandArgs[i] + " ");
					}
					//System.out.println(data.toString());
					//for(String s)
						mpasser.send(new Message(commandArgs[1], commandArgs[2], data.toString())); //dest, kind, data
					//}
				}
				else {
					Usage();
				}
	
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
                                
	public static void Usage() {
		System.out.println("Usage: <configuration_filename> <local_name>");
		System.out.println("CommandLine: q: quit, r: receive, s <dest> <kind> <data>: send");
	}
}