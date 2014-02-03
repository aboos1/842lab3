import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;



public class Logger 
{

	public static final String LOGSTORE = "log.txt";
	private static String config = null;
	private static String procname = null;
	static MessagePasser MsgObject =null;

	public static void main(String []args)
	{
		int port = 45637;
		ArrayList<Message> MsgStore = new ArrayList<Message>();
		LinkedList<TimeStampedMessage> sortedMsgStore = new LinkedList<TimeStampedMessage>();
		config = args[0];
		procname = args[1];
		MsgObject = new MessagePasser(config, procname);
		ClockService cs;
		LogJobHandler jobHandler = new LogJobHandler(port);
		jobHandler.start();
		ArrayList<TimeStampedMessage> ConcurrentMsgs = new ArrayList<TimeStampedMessage>();
		FileWriter fWrite = null;
		boolean sorted = false;
		try
		{
			//assuming receive in Messageparser returns the whole arraylist of
			//of messages
			while(true)
			{
				Scanner scanner = new Scanner(System.in);
				System.out.println("Would you like to log (Yes/No)");
				String logCommand = scanner.nextLine();
				if(logCommand!= null &&logCommand == "No")
					System.exit(-1);
				else if(logCommand !=null && logCommand == "Yes")
				{
					//Need to return a list of messages in messageparser
					MsgStore = MsgObject.receive();
					if(MsgStore != null)
						//assuming the receive() return the timestamped messages list
					{
						if(((TimeStampedMessage) MsgStore.get(0)).getTimeStamp().getClockServiceType().
								equalsIgnoreCase("vector"))
						{
							for(Message msg : MsgStore)
							{
								if(sortedMsgStore.size() == 0)
									sortedMsgStore.add((TimeStampedMessage)msg);
								else
								{
									cs = ((TimeStampedMessage)msg).getTimeStamp().getClockService();
									for(TimeStampedMessage tst_msg: sortedMsgStore)
									{
										sorted = false;
										if(((VectorClock) cs).compareTo(
												(VectorClock)(tst_msg.getTimeStamp().getClockService())) == 0)
										{
											sorted = true;
											sortedMsgStore.add(sortedMsgStore.indexOf(tst_msg)+1, 
													(TimeStampedMessage)msg);
											break;
										}
										else if(((VectorClock) cs).compareTo(
												(VectorClock)(tst_msg.getTimeStamp().getClockService())) == -1)
										{
											sortedMsgStore.add(sortedMsgStore.indexOf(tst_msg), 
													(TimeStampedMessage)msg);
											sorted = true;
											break;
										}
									}
									if(!sorted)
										sortedMsgStore.add((TimeStampedMessage) msg);
								}
							}
						}
						else //for logical clock service
							for(Message msg : MsgStore)
							{
								if(sortedMsgStore.size() == 0)
									sortedMsgStore.add((TimeStampedMessage)msg);
								else
								{
									for(TimeStampedMessage tst_msg: sortedMsgStore)
									{
										sorted = false;
										if(((TimeStampedMessage) msg).getTimeStamp().getTimeStamp().get(0) ==
												tst_msg.getTimeStamp().getTimeStamp().get(0))
										{
											sorted = true;
											sortedMsgStore.add(sortedMsgStore.indexOf(tst_msg)+1, 
												(TimeStampedMessage)msg);
											break;
										}
										else if(((TimeStampedMessage) msg).getTimeStamp().getTimeStamp().get(0) <=
												tst_msg.getTimeStamp().getTimeStamp().get(0))
										{
											sortedMsgStore.add(sortedMsgStore.indexOf(tst_msg), 
													(TimeStampedMessage)msg);
											sorted = true;
											break;
										}
									}
									if(!sorted)
										sortedMsgStore.add((TimeStampedMessage) msg);
								}
							}
					}
						
					//Collections.sort(MsgStore);
					/*for(int i=0; i< MsgStore.size(); i++)
					{
						if(i+1 < MsgStore.size())
						{
							//Concurrent Messages condition.. NOT SURE 
							//need to do this or comparision?
							if( ( MsgStore.get(i).compare((MsgStore.get(i+1))) ==  0)){
								ConcurrentMsgs.add(i, MsgStore.get(i));
							}
						}
						fWrite.write(MsgStore.get(i).toString() + "\n");			
					}*/
					
					for(TimeStampedMessage tst_msg: sortedMsgStore)
						fWrite.write(tst_msg.toString() + "\n");
					fWrite.close();	
				}				
			}
		}
		catch(IOException e){
			e.printStackTrace();
		} 
	}
}


	class LogJobHandler extends Thread
	{
		//need to get the port
		int port;
		public LogJobHandler(int port)
		{
			this.port = port;
		}
		public void run()
		{

			ServerSocket sServer = null ;
			try{
				sServer = new ServerSocket(port) ;  
				//start accepting messages
				while(true)
				{
					Socket socket = sServer.accept();
					fillInReceiveQueue(socket);	 
				}
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				try 
				{
					/* close the service */
					sServer.close();
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}

	}
	


	private void fillInReceiveQueue(Socket socket) throws InterruptedException
	{
		MsgLogReceive revService = new MsgLogReceive(socket);
		/* start the thread */
		revService.start();
	}


	public class MsgLogReceive extends Thread
	{   Socket socket;

	public MsgLogReceive(Socket socket)
	{
		this.socket = socket;	
	}
	public void run()
	{
		ObjectInputStream revInputStream = null;
		TimeStampedMessage tMsg = null;
		Queue <TimeStampedMessage>msgQueue = null; 
		try
		{
			revInputStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
			tMsg = (TimeStampedMessage)revInputStream.readObject();
			//doubt: add to the message queue of message passer or maintain a separate queue?
			msgQueue.add(tMsg);
		}
		catch(EOFException e)
		{
			System.out.println("EOFException");
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(revInputStream != null)
					revInputStream.close();
			} 
			catch(Exception e){e.printStackTrace();}
			
			try
			{
				socket.close();
			}
			catch(Exception e){e.printStackTrace();}
		}

	  }

	}



}















