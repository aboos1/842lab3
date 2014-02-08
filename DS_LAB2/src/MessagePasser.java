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

import org.yaml.snakeyaml.Yaml;


public class MessagePasser 
{
	private LinkedList<Message> out_buffer = new LinkedList<Message>();    
	private LinkedList<Message> in_buffer = new LinkedList<Message>();
	private ArrayList<Connection> connList = new ArrayList<Connection>();
	private ArrayList<Rule> ruleList = new ArrayList<Rule>();
	private HashMap<String, ArrayList<String>> groups = new HashMap<String, ArrayList<String>>();
	private ArrayList<Message> delayedOutMsg = new ArrayList<Message>();
	private ArrayList<Message> delayedInMsg = new ArrayList<Message>();
	private HashMap<Integer, String> processes;
	private TimeStamp systemTimeStamp;
	private String localName;
	private String configFileName;
	public static int seqNum;
	private File configFile;
	private int nbrOfProcesses;
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
	
	private ArrayList<Message> processRules(Message message, String rule_type,
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
		
		//processRules(message, "send", delayedOutMsg, out_buffer, null);
	}
	
	public ArrayList<Message> receive()
	{
		Message message;
		ArrayList<Message>array = new ArrayList<Message>();
		
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
		 
		 checkConfigUpdates();
		
		//processRules(message, "receive", delayedInMsg, null, array);
			
		return array;
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
