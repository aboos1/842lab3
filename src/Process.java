package lab0;
import java.io.*;

public class Process {
	public static void main(String[] args) {
		Usage();
		MessagePasser mpasser = new MessagePasser(args[0], args[1]);
		BufferedReader bufreader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			try {
				System.out.print("$ ");
				String command = bufreader.readLine();
				String[] commandArgs = command.split(" ");
                
				if (command.equals("q")) {
					System.exit(1);
				}
				else if (commandArgs[0].equals("r")) {
					Message amessage = mpasser.receive();
				}
				else if (commandArgs[0].equals("s")) {
					if (commandArgs.length != 4) {
						Usage();
						continue;
					}
					else {
						mpasser.send(new Message(commandArgs[1], commandArgs[2], commandArgs[3]));
					}
				}
	
			} catch (IOException e) {
				System.err.println();
			}
		}
	}
                                
	public static void Usage() {
		System.out.println("Usage: ");
	}
}