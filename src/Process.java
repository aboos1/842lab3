package lab0;
import java.io.*;

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
			e.printStackTrace();
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
					System.exit(1);
				}
				else if (command.equals("r")) {
					Message aMessage = mpasser.receive();
					if (aMessage != null) {
						System.out.println("Message #" + aMessage.getSeqNum() + " (" + aMessage.getKind() + ")"
										+ " from " + aMessage.getSrc() + ": " + aMessage.getData());
					}
				}
				else if (commandArgs[0].equals("s")) {
					if (commandArgs.length != 4) {
						Usage();
						continue;
					}
					else {
						mpasser.send(new Message(commandArgs[1], commandArgs[2], commandArgs[3])); //dest, kind, data
					}
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