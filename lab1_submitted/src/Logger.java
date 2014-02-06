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
	private ArrayList<Message> MsgStore;
	private LinkedList<TimeStampedMessage> sortedMsgStore;
	private LinkedList<TimeStampedMessage> concurrentMsgStore;
	private ClockService cs;
	private FileWriter fWrite = null;
	public static final String LOGSTORE = "log/logs.txt";
	
	public Logger()
	{
		 MsgStore = new ArrayList<Message>();
		 sortedMsgStore = new LinkedList<TimeStampedMessage>();
	}

	public ArrayList<Message> getMsgStore()
	{
		return MsgStore;
	}
	
	public LinkedList<TimeStampedMessage> getSortedMsgStore()
	{
		sortMsgStore();
		return sortedMsgStore;
	}
	
	public ClockService getClockService()
	{
		return cs;
	}
	
	private void sortMsgStore()
	{
		boolean sorted = false;
		
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
			
				/*
				 * moved to getconcurrent method
				 * int msgComp =0;
				for (int i=0; i< sortedMsgStore.size(); i++){
					for(int j=0; j< sortedMsgStore.size(); j++)
					{
						TimeStampedMessage m1 = sortedMsgStore.get(i);
						TimeStampedMessage m2 = sortedMsgStore.get(j);
						msgComp = ((VectorClock)m1.getTimeStamp().getClockService())
								  .compareTo((VectorClock)m2.getTimeStamp().getClockService());
					   if(msgComp == -1)
						   System.out.println(m1 + "is less than" + m2);
					   else if(msgComp == 1)
						   System.out.println(m1 + "is greater than" + m2);
					   else if(msgComp == 0)
						   System.out.println(m1 + "is equal to " + m2);
					   else
						   System.out.println(m1 + "is concurrent to " + m2);
					   
					}
				}*/
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
	}
	
	public void printConcurrentMsg()
	{
		if(((TimeStampedMessage) MsgStore.get(0)).getTimeStamp().getClockServiceType().
				equalsIgnoreCase("logical"))
			System.out.println("The limits of the logical clock service does not allow"
					+ " to discern concurrency");
		else
		{
			int msgComp =0;
			for (int i=0; i< sortedMsgStore.size(); i++)
			{
				for(int j=0; j< sortedMsgStore.size(); j++)
				{
					TimeStampedMessage m1 = sortedMsgStore.get(i);
					TimeStampedMessage m2 = sortedMsgStore.get(j);
					msgComp = ((VectorClock)m1.getTimeStamp().getClockService())
							  .compareTo((VectorClock)m2.getTimeStamp().getClockService());
				   if(msgComp == 3)
					   System.out.println(m1.getSrc() + " to "+ m1.getOriginalDest() + " kind "+ m1.getKind()
								+ " timestamp " + m1.getTimeStamp().getTimeStamp()+ " and " +
								m2.getSrc() + " to "+ m2.getOriginalDest() + " kind "+ m2.getKind()
								+ " timestamp " + m2.getTimeStamp().getTimeStamp());
				}
			}
		}
	}
	
	public void printGreaterThan(TimeStamp ts)
	{
		if(((TimeStampedMessage) MsgStore.get(0)).getTimeStamp().getClockServiceType().
				equalsIgnoreCase("logical"))
		{
			TimeStampedMessage m1;
			for (int i=0; i< sortedMsgStore.size(); i++)
			{
				m1 = sortedMsgStore.get(i);
				if(m1.getTimeStamp().getTimeStamp().get(0) >= ts.getTimeStamp().get(0))
				System.out.println(m1.getSrc() + " to "+ m1.getOriginalDest() + " kind "+ m1.getKind()
								+ " timestamp " + m1.getTimeStamp().getTimeStamp().get(0));
			}
		}
		else
		{
			int msgComp =0;
			for (int i=0; i< sortedMsgStore.size(); i++)
			{
				TimeStampedMessage m1 = sortedMsgStore.get(i);
				msgComp = ((VectorClock)m1.getTimeStamp().getClockService())
							  .compareTo((VectorClock)ts.getClockService());
				   if(msgComp == 1 || msgComp == 2)
					   System.out.println(m1.getSrc() + " to "+ m1.getOriginalDest() + " kind "+ m1.getKind()
								+ " timestamp " + m1.getTimeStamp().getTimeStamp());
			}
		}
	}
	
	public void printLessThan(TimeStamp ts)
	{
		if(((TimeStampedMessage) MsgStore.get(0)).getTimeStamp().getClockServiceType().
				equalsIgnoreCase("logical"))
		{
			TimeStampedMessage m1;
			for (int i=0; i< sortedMsgStore.size(); i++)
			{
				m1 = sortedMsgStore.get(i);
				if(m1.getTimeStamp().getTimeStamp().get(0) <= ts.getTimeStamp().get(0))
				System.out.println(m1.getSrc() + " to "+ m1.getOriginalDest() + " kind "+ m1.getKind()
								+ " timestamp " + m1.getTimeStamp().getTimeStamp());
			}
		}
		else
		{
			int msgComp =0;
			for (int i=0; i< sortedMsgStore.size(); i++)
			{
				TimeStampedMessage m1 = sortedMsgStore.get(i);
				msgComp = ((VectorClock)m1.getTimeStamp().getClockService())
							  .compareTo((VectorClock)ts.getClockService());
				   if(msgComp == -1 || msgComp == -2)
					   System.out.println(m1.getSrc() + " to "+ m1.getOriginalDest() + " kind "+ m1.getKind()
								+ " timestamp " + m1.getTimeStamp().getTimeStamp().get(0));
			}
		}
	}
	
	public void printEqual(TimeStamp ts)
	{
		if(((TimeStampedMessage) MsgStore.get(0)).getTimeStamp().getClockServiceType().
				equalsIgnoreCase("logical"))
		{
			TimeStampedMessage m1;
			for (int i=0; i< sortedMsgStore.size(); i++)
			{
				m1 = sortedMsgStore.get(i);
				if(m1.getTimeStamp().getTimeStamp().get(0) == ts.getTimeStamp().get(0))
				System.out.println(m1.getSrc() + " to "+ m1.getOriginalDest() + " kind "+ m1.getKind()
								+ " timestamp " + m1.getTimeStamp().getTimeStamp());
			}
		}
		else
		{
			int msgComp =0;
			for (int i=0; i< sortedMsgStore.size(); i++)
			{
				TimeStampedMessage m1 = sortedMsgStore.get(i);
				msgComp = ((VectorClock)m1.getTimeStamp().getClockService())
							  .compareTo((VectorClock)ts.getClockService());
				   if(msgComp == 0)
					   System.out.println(m1.getSrc() + " to "+ m1.getOriginalDest() + " kind "+ m1.getKind()
								+ " timestamp " + m1.getTimeStamp().getTimeStamp().get(0));
			}
		}
	}
		
	public void writeLogsToFile() 
	{
		try
		{
			for(TimeStampedMessage tst_msg: sortedMsgStore)
				fWrite.write(tst_msg.toString() + "\n");
			fWrite.close();
		}
		catch (IOException exc)
		{
			exc.printStackTrace();
		}
	}	
}


