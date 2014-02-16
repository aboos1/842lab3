/**
 * @file CmdTool.java
 * @brief Interactive application with user
 * @author  aboos
 * 			dil1
 * @date 02/09/2014
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ResourceRequestor {
	/*
	 * Command Type:
	 * quit: quit the whole process
	 * ps: print the information of current MessagePasser
	 * send command: dest <kind> <data>
	 * send log command : log <dest> <kind> <data>
	 * request resource command : request resource
	 * receive command: receive
	 * receive log command : receive log
	 * 
	 */
	
	private MessagePasser msgPasser;
	boolean WANTED;
	boolean VOTED;
	boolean IN_CS;
	int sent_count;
	int recv_count;
	Map<String,Integer> okay_recv = new HashMap<String,Integer>();
	List<TimeStampedMessage> requestQueue = new LinkedList<TimeStampedMessage>();
	
	public class receiveRequest extends Thread{
		MessagePasser msgPasser;
		public receiveRequest(MessagePasser msgPasser){
			this.msgPasser = msgPasser;
		}
		
		public void run(){
			while(true){
				TimeStampedMessage ts;
				if((ts = (TimeStampedMessage) msgPasser.receive()) != null){
					if(ts.getKind().equalsIgnoreCase("request")){
						if(IN_CS == true || VOTED == true){
							queueRequest(ts);
							System.out.println("Received and queued request from " + ts.getSrc());
						} else {
							sendReply(ts.getSrc());
							VOTED = true;
							System.out.println("Accepted request from " + ts.getSrc());
						}
					} else if(ts.getKind().equalsIgnoreCase("release")){
						System.out.println("Received release from " + ts.getSrc());
						if(!requestQueue.isEmpty()){
							TimeStampedMessage req = requestQueue.get(0);
							requestQueue.remove(0);
							sendReply(req.getSrc());
							VOTED = true;
							System.out.println("Processed queued request from " + req.getSrc()
									+ " with timestamp " + req.getMsgTS());
						} else {
							VOTED = false;
						}
					} else if(ts.getKind().equalsIgnoreCase("okay"))
					{
						System.out.println("received OK from " + ts.getSrc());
						if(WANTED == true)
						{
							okay_recv.put(ts.getSrc(), 1);							
							if(receivedAllOKs())
							{
								IN_CS = true;
								System.out.println("in cs now...");
							}
						}
					} 
					else
					{
						System.out.println("Received non-standard message");
					}
					recv_count++;
				}
			}
		}
		
		private void queueRequest(TimeStampedMessage msg){
			int i = 0, size = 0;
			synchronized (requestQueue) {
				size = requestQueue.size();
				for (; i < size; i++) {
					TimeStampedMessage tmp = (TimeStampedMessage) requestQueue.get(i);
					if (((TimeStampedMessage) msg).getMsgTS().compare(
							tmp.getMsgTS()) != TimeStampRelation.greaterEqual) {
						break;
					}
				}
				requestQueue.add(i, msg);
			}
		}
		
		private void sendReply(String dest){
			TimeStampedMessage reply = new TimeStampedMessage(dest, "okay", null, null, this.msgPasser.getLocalName());
			msgPasser.send(reply);
		}
	}
	
	
	/*
	public class CSHandler extends Thread
	{
		public void run()
		{
			while(true)
			{
				while(!IN_CS)  //sleep while waiting to enter cs
				{
					try {
						sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				System.out.println("using resource for 30s");
				try {
					sleep(30000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					
				releaseResource();
				System.out.println("done releasing resource. IN_CS is now " + IN_CS);
			}
		}
		
	}*/
		
	private synchronized void releaseResource()
		{
			System.out.println(msgPasser.getLocalName() + " is releasing resource now");
			// clear okay_rcv
			for(String member : msgPasser.getGroup())
				okay_recv.put(member, 0);
						
			WANTED = false;
			IN_CS = false;
			sendForResource("release");
		}
	
	public ResourceRequestor(MessagePasser msgPasser) {
		this.msgPasser = msgPasser;
		new receiveRequest(msgPasser).start();
		//new CSHandler().start();
	}
	
	public void sendForResource(String message_king)
	{
		WANTED = true;
		TimeStampedMessage msg = new TimeStampedMessage(msgPasser.getLocalName() + "_group", 
				message_king, null, msgPasser.getClockSer().getTs(), msgPasser.getLocalName());
		sent_count++;
		msgPasser.send(msg);
	}
	
	public boolean receivedAllOKs()
	{
		for(String key : okay_recv.keySet())
			if(okay_recv.get(key) == 0)
				return false;
		
		return true;
	}
	
	public void executing() {
		String cmdInput = new String();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        Message msg = null;
        LogMessage logMsg = null;
        ArrayList<String> groupMembers = msgPasser.getGroup();
        
        // Initialize the okay received map
        for(String member : groupMembers){
        	okay_recv.put(member, 0);
        }
        
        while (!cmdInput.equals("quit")) {
        	
            System.out.print("CommandLine% ");
            
            try {
                cmdInput = in.readLine();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            if(cmdInput.equals("quit")) {
            	
            	try {
					this.msgPasser.closeAllSockets();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	System.exit(0);
            	
            } else if(cmdInput.equals("ps")) {
            	
            	System.out.println(this.msgPasser.toString());
            	
            } 
            else if(cmdInput.equals("cleanup")) {
            	System.out.println("we clean up the messagePasser and ClockService");
            	this.msgPasser.cleanUp();
            }
            else if(cmdInput.equals("printAllMsg")) {
            	System.out.println("The Map of allMsg is");
            	for(NackItem n : this.msgPasser.getAllMsg().keySet()) {
            		System.out.println(n.toString() + " " + this.msgPasser.getAllMsg().get(n).toString());
            	}
            }
            else if(cmdInput.equals("printHoldBackMap")) {
            	System.out.println("The Map of HoldBack Queue is");
            	for(SrcGroup s : this.msgPasser.getHoldBackMap().keySet()) {
            		System.out.println(s.toString() + " " + this.msgPasser.getHoldBackMap().get(s).toString());
            	}
            }
            else if(cmdInput.equals("printSeqNums")) {
            	System.out.println("The Map of SeqNums");
            	for(SrcGroup s : this.msgPasser.getSeqNums().keySet()) {
            		System.out.println(s.toString() + " " + this.msgPasser.getSeqNums().get(s).toString());
            	}
            }
            else if (!cmdInput.equals(null) && !cmdInput.equals("\n")) {
            	
            	String[] array = cmdInput.split(" ");
            	if(array.length == 3)
            		this.msgPasser.send(new TimeStampedMessage(array[0], array[1], array[2], null, this.msgPasser.getLocalName()));
            	
            	else if(cmdInput.equals("receive")) {
            		msg = this.msgPasser.receive();		
            		if(msg == null) {
            			System.out.println("Nothing to pass to Aplication!");
            		} else {
            			System.out.println("We receive");
((TimeStampedMessage)msg).dumpMsg();
            		}
            	} else if(cmdInput.equals("release")){
            		releaseResource();
            	}
            	else if(array.length == 2) 
            	{
            		if(array[0].equals("receive") && array[1].equals("log")) 
            		{
            			msg = this.msgPasser.receive();
            			if(msg == null) {
            				System.out.println("Nothing to pass to Aplication!");
            			} else {
            				logMsg = new LogMessage(((TimeStampedMessage)msg).getMsg(), this.msgPasser.getClockSer().getTs().makeCopy());
            				TimeStampedMessage newLogMsg = new TimeStampedMessage("logger", "log", logMsg, null, this.msgPasser.getLocalName());
    						this.msgPasser.send(newLogMsg);
          System.out.println("We receive");
((TimeStampedMessage)msg).dumpMsg();
               				//this.msgPasser.logEvent(((TimeStampedMessage)msg).getMsg(), this.msgPasser.getClockSer().getTs().makeCopy());
            			}
            				
            		}
            		else if(array[0].equals("request") && array[1].equals("resource")) 
            		{
            			//msg = this.msgPasser.receive();
            			
            			sendForResource("request");
            		}
            		else if(array[0].equals("event")) 
            		{
System.out.println("Lamport time " + this.msgPasser.getClockSer().getTs().getLamportClock());
            			this.msgPasser.getClockSer().addTS(this.msgPasser.getLocalName());
System.out.println("Lamport time " + this.msgPasser.getClockSer().getTs().getLamportClock());
						logMsg = new LogMessage(array[1], this.msgPasser.getClockSer().getTs().makeCopy());
						TimeStampedMessage newLogMsg = new TimeStampedMessage("logger", "log", logMsg, null, this.msgPasser.getLocalName());
						this.msgPasser.send(newLogMsg);
            			//this.msgPasser.logEvent(array[1], this.msgPasser.getClockSer().getTs().makeCopy());
            		} else 
            		{
            			System.out.println("Invalid Command!");
            		}
            	}
            	else if(array.length == 4) {
            		if(array[0].equals("log")) {
            			TimeStampedMessage newMsg = new TimeStampedMessage(array[1], array[2], array[3], null, this.msgPasser.getLocalName());
            			this.msgPasser.send(newMsg);
System.out.println("send TS:" + this.msgPasser.getClockSer().getTs());
						
						logMsg = new LogMessage(newMsg.getMsg(), newMsg.getMsgTS().makeCopy());
						TimeStampedMessage newLogMsg = new TimeStampedMessage("logger", "log", logMsg, null, this.msgPasser.getLocalName());
						this.msgPasser.send(newLogMsg);
            			//this.msgPasser.logEvent(newMsg.getMsg(), this.msgPasser.getClockSer().getTs().makeCopy());
            		}
            		else {
            			System.out.println("Invalid Command!");
            		}
            	}
            	else {
            		System.out.println("Invalid Command!");
            	}
            	
            } else {
            	System.out.println("Invalid Command!");
            }
        }
	}
	

	public static void main(String[] args) {
		if(args.length != 2) {
			System.out.println("Arguments mismtach.\n" +
					"Required arguments - <Yaml config> <name of host>");
			System.exit(0);
		}
		MessagePasser msgPasser = new MessagePasser(args[0], args[1]);
		ResourceRequestor tool = new ResourceRequestor(msgPasser);
		tool.executing();
	}
}
