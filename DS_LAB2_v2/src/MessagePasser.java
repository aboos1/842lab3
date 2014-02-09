import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import org.yaml.snakeyaml.Yaml;


public class MessagePasser 
{
	//Buffer that holds messages on client side 
	private LinkedList<Message> out_buffer = new LinkedList<Message>();    
	
	//Buffer that holds messages on receiver thread
	private LinkedList<Message> in_buffer = new LinkedList<Message>();
	
	//All connections with other process
	private ArrayList<Connection> connList = new ArrayList<Connection>();
	private ArrayList<Rule> ruleList = new ArrayList<Rule>();
	
	//keep track of all groups, key:group name, value:all host
	private HashMap<String, ArrayList<String>> groups = new HashMap<String, ArrayList<String>>();
	
	//Hold all delayed message need to send out
	private ArrayList<Message> delayedOutMsg = new ArrayList<Message>();
	
	//Hold all delayed message on receiver side
	private ArrayList<Message> delayedInMsg = new ArrayList<Message>();
	
	//Hold all message has not been confirmed by the group other nodes
	private ArrayList<Message> holdbackQueue = new ArrayList<Message>();
	
	//Finally message delivered to application
	private LinkedList<Message> deliveryQueue = new LinkedList<Message>();
	
	//Holds multicast message
	private LinkedList<Message> multicastSendQueue = new LinkedList<Message>();
	private HashMap<Integer, String> processes;
	
	private HashMap<String, ArrayList<Integer>> ACKMap = new HashMap<String, ArrayList<Integer>>();
	
	private TimeStamp systemTimeStamp, groupTimeStamp;
	private String localName;
	private String configFileName;
	public static int seqNum;
	private File configFile;
	private int nbrOfProcesses;	// number of process in the config file
	private long fileModTime;
	private int pid;
	
	public MessagePasser(String configuration_filename, String local_name)
	{
		configFileName = configuration_filename;
		localName = local_name;
		seqNum = 0;
	}
	
	public void initSystemTimeStamp(String service, int size)
	{
		systemTimeStamp = new TimeStamp(service, size);
	}
	
	private void updateSytemTimeStamp(TimeStampedMessage message, int src_pid, int dest_pid)
	{
		systemTimeStamp.updateTimeStamp(message.getTimeStamp().getTimeStamp(), src_pid, dest_pid);
	}
	
	private void incrementSystemTime()
	{
		try
		{
			if(systemTimeStamp.getClockServiceType().equalsIgnoreCase("vector"))
				systemTimeStamp.getTimeStamp().set(pid, systemTimeStamp.getTimeStamp().get(pid)+1);
			else   //logical
				systemTimeStamp.getTimeStamp().set(0, systemTimeStamp.getTimeStamp().get(0)+1);
		}
		catch(IndexOutOfBoundsException e)
		{
			System.out.println(pid);
			e.printStackTrace();
		}
	}
	
	private void processRules(Message message, String rule_type,
			                 ArrayList<Message> delay_list, LinkedList<Message> buffer, ArrayList<Message> array)
	{
		Rule rule; 
		Message copy;
		String dest, src, kind, group_name;
		String group;
		int seqNum;
		Boolean duplicate;
		
		
		for(int i=0; i < ruleList.size(); i++)
		{
			rule = ruleList.get(i);
			if(rule.getType().equalsIgnoreCase(rule_type))
			{
				dest = rule.getDest();
				src = rule.getSrc();
				kind = rule.getKind();
				group = rule.getGroupName();
				seqNum = rule.getSeqNum();
				duplicate = rule.getDuplicate();
				
				if(rule.getAction().equalsIgnoreCase("drop"))   
					{
						if((group == null || group.equalsIgnoreCase(message.getGroup())) &&
								(dest == null || message.includeDest(dest)) && (src == null || src.equalsIgnoreCase(message.getSrc()))
								&& (kind == null || kind.equalsIgnoreCase(message.getKind())) && (seqNum == -1 || seqNum == message.getSeqNum())
								&& (duplicate ==  false || duplicate.equals(message.getDuplicate())))
						{
							return null;		
						}
					}
				else if(rule.getAction().equalsIgnoreCase("delay"))
				{
					if((group == null || group.equalsIgnoreCase(message.getGroup())) &&
							(dest == null || message.includeDest(dest)) && (src == null || src.equalsIgnoreCase(message.getSrc()))
							&& (kind == null || kind.equalsIgnoreCase(message.getKind())) && (seqNum == -1 || seqNum == message.getSeqNum())
							&& (duplicate ==  false || duplicate.equals(message.getDuplicate())))
					{
						delay_list.add(message);
						
						return null;
					}
								
				}	
				else if(rule.getAction().equalsIgnoreCase("duplicate"))
				{
					if((group == null || group.equalsIgnoreCase(message.getGroup())) &&
							(dest == null || message.includeDest(dest)) && (src == null || src.equalsIgnoreCase(message.getSrc()))
							&& (kind == null || kind.equalsIgnoreCase(message.getKind())) && (seqNum == -1 || seqNum == message.getSeqNum())
							&& (duplicate ==  false || duplicate.equals(message.getDuplicate())))
					{
						copy = new Message(null, null, null, 0, 0);
						copy.setSrc(message.getSrc());
						copy.setDest(message.getDest());
						copy.setKind(message.getKind());
						copy.setData(message.getData());
						copy.setSeqNum(message.getSeqNum());
						copy.setDuplicate(true);
						
						if (rule_type.equalsIgnoreCase("send"))
						{
							buffer.add(message);
							buffer.add(copy);
							
							// add potentially delayed messages to out buffer
							for(int j = 0; j < delay_list.size(); j++)
								buffer.add(delay_list.get(j));
							delay_list.clear();
							
							return null;
						}
						else  // receive
						{
							array.add(message);
							array.add(copy);
								
							// add potentially delayed messages to out buffer
							for(int j = 0; j < delay_list.size(); j++)
								array.add(delay_list.get(j));
							delay_list.clear();
							
							array.trimToSize();
							return array;
						}
					}
				}
					
			}
		}
		
		/*
		 * do the following when no rule applies
		 */
		if(rule_type.equalsIgnoreCase("send"))
		{
			buffer.add(message);
			for(int i = 0; i < delay_list.size(); i++)
				buffer.add(delay_list.get(i));        // moves potential delayed msg to buffer
			delay_list.clear();
			return null;
		}
		else   // receive
		{
			array.add(message);
			for(int j = 0; j < delay_list.size(); j++)
				array.add(delay_list.get(j));
			delay_list.clear();
			
			array.trimToSize();
			return array;
		}
	}
	
	private void checkConfigUpdates()
	{
		if(configFile.lastModified() > fileModTime)
		 {
			 try
			 {
				 parseConfig();
			 }
			 catch(FileNotFoundException e)
			 {
				 e.printStackTrace();
			 }
		 }
	}
	
	public void send(Message message)
	{
		boolean flag = false;
		message.setSrc(localName);
		message.setSeqNum(++seqNum);
		
		if(message.getGroup() != null || !(message.getGroup().equals("")))
		{
			message.getDest().add(message.getSrc());    // add source to broadcast group
			sendMulticast(message, flag);
		}
		else
		{
			for(Connection conn: connList) 
			{
				if(message.getDest().equals(conn.getName()))
				{
					message.setHostname(conn.getIP());
					message.setPort(conn.getPort());
					flag = true;
					break;
				}
			}
			
			if (!flag) 
			{
				System.out.println("No matching destination!");
				return;
			}
			
			if(message instanceof TimeStampedMessage)
			{
				incrementSystemTime();
			}
			
			checkConfigUpdates();
			
			processRules(message, "send", delayedOutMsg, out_buffer, null);
		}
	}
	
	public void sendMulticast(Message message, boolean flag)
	{
		/*boolean flag;
		message.setSrc(localName);
		message.setSeqNum(++seqNum);*/    // this is already done in send()
		
		for(String dest_name : message.getDest())
		{	
			// create entry in ACKMap for message
			if(!dest_name.equalsIgnoreCase(localName))     // if this is not the source
			{
				flag = false;
				for(Connection conn: connList) 
				{
					if(dest_name.equalsIgnoreCase(conn.getName()))
					{
						message.setHostname(conn.getIP());
						message.setPort(conn.getPort());
						flag = true;
						break;
					}
				}
				
				if (!flag) 
				{
					System.out.println("No matching destination for : " + dest_name);
				}
				else
				{
					if(message instanceof TimeStampedMessage)
					{
						incrementSystemTime();
					}
					
					multicastSendQueue.add(message);
					
					checkConfigUpdates();
					
					//processRules(message, "send", delayedOutMsg, out_buffer, null);
				}
			}
		}
	}
	
	public ArrayList<Message> receive()
	{
		Message message;
		ArrayList<Message> array = new ArrayList<Message>();
		
		if (in_buffer.isEmpty()) 
		{
			System.out.println("No more messages!");
			return null;
		}
		
		 message = in_buffer.removeFirst();
		 //System.out.println("message is: " + message);
		 
		if(message instanceof TimeStampedMessage)
		 {
			 updateSytemTimeStamp((TimeStampedMessage) message, getSrcPID(message), pid);

			 System.out.println("message received at: " + getSystemTimeStamp().
			 getTimeStamp());
			 incrementSystemTime();
		 }
		 
		if(message.getGroup() != null || !(message.getGroup().equals("")))
			receiveMulticast(message);
		else
		{
			checkConfigUpdates();
		
		 	processRules(message, "receive", delayedInMsg, null, array);
		}
			
		return array;
	}
	
	/*
	 *  This method receives the multicast messages.
	 *  When a message comes in, the passer checks the hold back queue
	 *  to see whether that message was previously received. If, yes
	 *  and this is the last packet, then the passer proceeds to forwarding the 
	 *  message sequence to a sorting queue, and eventually to the delivery queue.
	 *  Else, the message is re-broadcast to the group and added to the hold back queue 
	 */
	public ArrayList<Message> receiveMulticast(Message message)
	{
		TimeStampedMessage ack, ts_msg;
		//ArrayList<Message>array = new ArrayList<Message>();
		
		if(message.getKind().equals("ACK"))
			 processACK(message);
		 else
		 {
			 if(!message.getOriginalSrc().equals(localName))       //  if this is not the message source
			 {
				 if(!isInHoldbackQueue(message))
				 {
					 holdbackQueue.add(message);
					 
					 // send ack
					 ts_msg = (TimeStampedMessage) message;
					 // note that original msg seqNum is stored in ACK's data field
					 ack = new TimeStampedMessage(ts_msg.getGroup(), "ACK", ts_msg.getSeqNum(), 
							 ts_msg.getMessageStart(), ts_msg.getMessageLength(), ts_msg.getTimeStamp().getClockServiceType(), 
							 nbrOfProcesses, systemTimeStamp);
					 ack.setSeqNum(ts_msg.getSeqNum() + 1);
					 ack.setSrc(localName);
					 ack.setOriginalSrc(ts_msg.getSrc());
					 ack.setDest(ts_msg.getDest());
					 
					 //update ACKMap
					 updateACKMap(ack);
					 
					 sendMulticast(ack, false);
				 }
			 }
		 }
		 
		 
		 
		 updateSytemTimeStamp((TimeStampedMessage) message, getSrcPID(message), pid);

		 System.out.println("message received at: " + getSystemTimeStamp().
		 getTimeStamp());
		 incrementSystemTime();
		 
		 checkConfigUpdates();
		
		processRules(message, "receive", delayedInMsg, null, holdbackQueue);
			
		return holdbackQueue;
	}
	
	/*
	 * Checks whether a message is already in the hold back queue
	 */
	private boolean isInHoldbackQueue(Message msg)
	{
		for(Message m : holdbackQueue)
			if(m.compareTo(msg) == 0)
				return true;
		
		return false;
	}
	
	
	/*
	 *  Add a list of messages to the delivery queue in a sorted sequence
	 */
	private void addToDeliveryQueue(ArrayList<Message> msg_list)
	{
		TimeStampedMessage ts_msg; 
		Message m;
		boolean message_sorted;
		
		for(int i =0; i < msg_list.size(); i++)
		{
			ts_msg = (TimeStampedMessage) msg_list.get(i);
			
			if(deliveryQueue.size() == 0)
				deliveryQueue.add(ts_msg);
			else
			{
				if((ts_msg.getTimeStamp().getClockServiceType().
						equalsIgnoreCase("vector")))   // change to outer check
				{
					message_sorted = false;
					for(int j=0; j < deliveryQueue.size(); j++)
					{
						m = deliveryQueue.get(j);
						if((((VectorClock) (((TimeStampedMessage) m).getTimeStamp().getClockService())).
						         compareTo((VectorClock)(ts_msg.getTimeStamp().getClockService())) == 0))
						{
							deliveryQueue.add(ts_msg);
							message_sorted = true;
							break;
						}
						else if((((VectorClock) (((TimeStampedMessage) m).getTimeStamp().getClockService())).
						         compareTo((VectorClock)(ts_msg.getTimeStamp().getClockService())) == -1)
						         ||
						         (((VectorClock) (((TimeStampedMessage) m).getTimeStamp().getClockService())).
						         compareTo((VectorClock)(ts_msg.getTimeStamp().getClockService())) == -2))
						{
							deliveryQueue.add(j, ts_msg);
							message_sorted = true;
							break;
						}
					}
					if(!message_sorted)  // if not equal or less then append end of queue
						deliveryQueue.add(ts_msg);					
				}
			}
		}
		holdbackQueue.clear();   // clear the hold back queue
	}
	
	/*
	 *  Update the map if it already has an entry for the message,
	 *  else create one before updating it 
	 */
    private void updateACKMap(Message message)
    {
    	ArrayList<Integer> ACKList = ACKMap.get(message.getOriginalSrc()+message.getData());
    	
    	if (ACKList != null)
    		ACKList.add(pid, (Integer)(ACKList.get(pid)) + 1);
    	else
    	{
    		ACKList = new ArrayList<Integer>();
    		ACKList.add(pid, 1);
    		ACKMap.put(message.getOriginalSrc()+message.getData(), ACKList);
    	}
    }
	
	private void processACK(Message msg)
	{
		boolean checkACKs = false;
		updateACKMap(msg);
		ArrayList<String> grp_members = msg.getDest();
		int map_array_index;
		
		// check whether all ACKs are in 
		for(int i=0; i<grp_members.size(); i++)
		{
			map_array_index = getProcessPID((String)grp_members.get(i));
			if(ACKMap.get(msg.getSrc()+msg.getData()).get(grp_members.indexOf(map_array_index)) == msg.getMessageLength())
				checkACKs = true;
			else
			{
				checkACKs = false;
				break;
			}
		}
		
		if(checkACKs)
		{
			if(msg.getOriginalSrc().equalsIgnoreCase(localName))      // if source
			{
				// remove message from multicast send Q
				for(Message message : multicastSendQueue)
					if(message.getSrc().equalsIgnoreCase(msg.getOriginalSrc()) && message.getSeqNum() == (Integer)msg.getData())	
					{
						multicastSendQueue.remove(message);
						break;
					}
			}
			else
				addToDeliveryQueue(holdbackQueue);    // check!!!!! not all messages should be xfered
			
			// remove message from ACKMap
			ACKMap.remove(msg.getSrc()+msg.getData());
			
			//stop timer
						
		}
		
	}
	
	/*
	 * Getters
	 */
	public int getPid()
	{
		return pid;
	}
	
	public LinkedList<Message> getOutBuffer()
	{
		return out_buffer;
	}
	
	public LinkedList<Message> getInBuffer()
	{
		return in_buffer;
	}
	
	public ArrayList<Connection> getConnList()
	{
		return connList;
	}
	
	public HashMap<Integer, String> getProcesses()
	{
		return processes;
	}
	
	public String getLocalName()
	{
		return localName;
	}
	
	public int getNbrOfProcesses()
	{
		return nbrOfProcesses;
	}
	
	public TimeStamp getSystemTimeStamp()
	{
		return systemTimeStamp;
	}
	
	public void setPid()
	{
		/*
		 * Assign pid
		 */
		for(int i = 0; i < nbrOfProcesses; i++)
			if(processes.get(i).equalsIgnoreCase(localName))
			{
				pid = i;
				break;
			}
	}
	
	/*
	 * Return the PID of the process that sent the message
	 * 
	 * @return an integer
	 */
	public int getSrcPID(Message msg)
	{ 
		int src_pid = -1;
		
		for(int i = 0; i < nbrOfProcesses; i++)
			if(processes.get(i).equalsIgnoreCase(msg.getSrc()))
			{
				src_pid = i;
				break;
			}
		
		return src_pid;
	}
	
	/*
	 * Return a member PID
	 * 
	 * @return an integer
	 */
	public int getProcessPID(String process_name)
	{ 
		int pid = -1;
		
		for(int i = 0; i < nbrOfProcesses; i++)
			if(processes.get(i).equalsIgnoreCase(process_name))
			{
				pid = i;
				break;
			}
		
		return pid;
	}
	
	@SuppressWarnings("unchecked")
	public void parseConfig() throws FileNotFoundException 
	{
		configFile = new File ("C:/Users/YAYA/workspace_1/DS_LAB2/src/configuration.yaml"); 
		//configFile = new File ("/afs/andrew/usr1/ykandiss/public/" + configFileName); 
		fileModTime = configFile.lastModified();
	    InputStream input = new FileInputStream(configFile);
	    Yaml yaml = new Yaml();
	    
		Map<String, Map> map = yaml.loadAs(input, Map.class);
		Iterator iterator  = map.keySet().iterator();
		String temp;	
		ArrayList list;
		Connection conn;
		Rule rule;
		
		while(iterator.hasNext())
		{
			//System.out.println(iterator.next().getClass());
			temp = (String) iterator.next();
			if(temp.equalsIgnoreCase("groups"))
			{
				list = (ArrayList) map.get(temp);
				for(int i=0; i<list.size(); i++)
					groups.put(((LinkedHashMap<String, String>)list.get(i)).get("name"), 
							((LinkedHashMap<String, ArrayList<String>>)list.get(i)).get("members"));
				//System.out.println(groups.get("Group1"));
			}
			else if(temp.equalsIgnoreCase("configuration"))
			{
				list = (ArrayList) map.get(temp);
				nbrOfProcesses = list.size();
				processes = new HashMap<Integer, String>(nbrOfProcesses);
				
				//System.out.println(((LinkedHashMap<String, String>) list.get(0)).get("name"));
				for(int i = 0; i < nbrOfProcesses; i++)
				{
					conn = new Connection("", "", 0);
					conn.setIP(((LinkedHashMap<String, String>)list.get(i)).get("ip"));
					conn.setPort((int)((LinkedHashMap) list.get(i)).get("port")); 
					conn.setName(((LinkedHashMap<String, String>) list.get(i)).get("name"));
					connList.add(conn);
					processes.put(i, conn.getName());
				}
				connList.trimToSize();
				//System.out.println(msgList.size());
			}
			else 
			{
				//System.out.println(map.get(temp).getClass());
				
				list = (ArrayList) map.get(temp);
				for(int i = 0; i < list.size(); i++)
				{
					rule = new Rule();
					
					if(temp.equalsIgnoreCase("sendRules"))
						rule.setType("send");
					else
						rule.setType("receive");
					
					rule.setAction(((LinkedHashMap<String, String>)list.get(i)).get("action"));
					
					if(((LinkedHashMap<String, String>)list.get(i)).containsKey("src"))
						rule.setSrc(((LinkedHashMap<String, String>)list.get(i)).get("src"));
					
					if(((LinkedHashMap<String, String>)list.get(i)).containsKey("dest"))
						rule.setDest(((LinkedHashMap<String, String>)list.get(i)).get("dest"));
					
					if(((LinkedHashMap<String, String>)list.get(i)).containsKey("kind"))
						rule.setKind(((LinkedHashMap<String, String>)list.get(i)).get("kind"));
					
					if(((LinkedHashMap)list.get(i)).containsKey("seqNum"))
						rule.setseqNum((int)((LinkedHashMap)list.get(i)).get("seqNum"));
					
					if(((LinkedHashMap<String, Boolean>)list.get(i)).containsKey("duplicate"))
						rule.setDuplicate(((LinkedHashMap<String, Boolean>)list.get(i)).get("duplicate"));
					
					if(((LinkedHashMap<String, String>)list.get(i)).containsKey("groupName"))
						rule.setGroupName(((LinkedHashMap<String, String>)list.get(i)).get("groupName"));
				
					ruleList.add(rule);
				}
				ruleList.trimToSize();
			}			
		}
	}	
}

class Connection
{
    private String name;
    private String ip;
    private int port;

    public Connection(String name, String ip, int port)
    {
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    public String getName() 
    {
        return name;
    }

    public void setName(String name) 
    {
        this.name = name;
    }

    public String getIP() 
    {
        return ip;
    }

    public void setIP(String ip) 
    {
        this.ip = ip;
    }

    public int getPort()
    {
    	return port;
    }
    
    public void setPort(int prt)
    {
    	port = prt;
    }
} 
