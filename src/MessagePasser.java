//package lab0;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.yaml.snakeyaml.Yaml;


public class MessagePasser 
{
	private LinkedList<Message> out_buffer = new LinkedList<Message>();    //buffered writer?
	private LinkedList<Message> in_buffer = new LinkedList<Message>();
	private ArrayList<Connection> connList = new ArrayList<Connection>();
	private ArrayList<Rule> ruleList = new ArrayList<Rule>();
	private ArrayList<Message>delayedOutMsg = new ArrayList<Message>();
	private ArrayList<Message>delayedInMsg = new ArrayList<Message>();
	private String localName;
	private String configFileName;
	public static int seqNum;
	private File configFile;
	private long fileModTime;
	
	public MessagePasser(String configuration_filename, String local_name)
	{
		configFileName = configuration_filename;
		localName = local_name;
		seqNum = 0;
	}
	
	public void send(Message message)
	{
		Rule rule;
		Message copy;
		
		// set the message
		boolean flag = false;
		message.setSrc(localName);
		message.setSeqNum(++seqNum);
		String dest, src, kind;
		int seqNum;
		Boolean duplicate;
		
		for(Connection conn: connList) 
		{
			if(message.getDest().equals(conn.getName()))
			{
				message.setHostname(conn.getIP());
				//System.out.println(conn.getIP());
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
		
		for(int i=0; i < ruleList.size(); i++)
		{
			rule = ruleList.get(i);
			if(rule.getType().equalsIgnoreCase("send"))
			{
				dest = rule.getDest();
				src = rule.getSrc();
				kind = rule.getKind();
				seqNum = rule.getSeqNum();
				duplicate = rule.getDuplicate();
				
				if(rule.getAction().equalsIgnoreCase("drop"))   /// revisit
					{
						if((dest == null || dest.equalsIgnoreCase(message.getDest())) && (src == null || src.equalsIgnoreCase(message.getSrc()))
								&& (kind == null || kind.equalsIgnoreCase(message.getKind())) && (seqNum == -1 || seqNum == message.getSeqNum())
								&& (duplicate ==  false || duplicate.equals(message.getDuplicate())))
							return;				
					}
				else if(rule.getAction().equalsIgnoreCase("delay"))
				{
					if((dest == null || dest.equalsIgnoreCase(message.getDest())) && (src == null || src.equalsIgnoreCase(message.getSrc()))
							&& (kind == null || kind.equalsIgnoreCase(message.getKind())) && (seqNum == -1 || seqNum == message.getSeqNum())
							&& (duplicate ==  false || duplicate.equals(message.getDuplicate())))
					{
						delayedOutMsg.add(message);
						return;
					}
								
				}	
				else if(rule.getAction().equalsIgnoreCase("duplicate"))
				{
					if((dest == null || dest.equalsIgnoreCase(message.getDest())) && (src == null || src.equalsIgnoreCase(message.getSrc()))
							&& (kind == null || kind.equalsIgnoreCase(message.getKind())) && (seqNum == -1 || seqNum == message.getSeqNum())
							&& (duplicate ==  false || duplicate.equals(message.getDuplicate())))
					{
						copy = new Message(null, null, null);
						copy.setSrc(message.getSrc());
						copy.setDest(message.getDest());
						copy.setKind(message.getKind());
						copy.setData(message.getData());
						copy.setSeqNum(message.getSeqNum());
						copy.setDuplicate(true);
						
						out_buffer.add(message);
						out_buffer.add(copy);
						
						// add potentially delayed messages to out buffer
						for(int j = 0; j < delayedOutMsg.size(); j++)
							out_buffer.add(delayedOutMsg.get(j));
						delayedOutMsg.clear();
						
						return;
					}
				}
					
			}
		}
			
		out_buffer.add(message);
		for(int i = 0; i < delayedOutMsg.size(); i++)
			out_buffer.add(delayedOutMsg.get(i));
		delayedOutMsg.clear();
	}
	
	public ArrayList<Message> receive()
	{
		Message message, copy;
		ArrayList<Message>array = new ArrayList<Message>();
		Rule rule;
		String dest, src, kind;
		int seqNum;
		Boolean duplicate;
		
		if (in_buffer.isEmpty()) 
		{
			System.out.println("No more messages!");
			return null;
		}
		
		 message = in_buffer.removeFirst();
		 
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
		
		for(int i=0; i < ruleList.size(); i++)
		{
			rule = ruleList.get(i);
			
			if(rule.getType().equalsIgnoreCase("receive"))
			{
				dest = rule.getDest();
				src = rule.getSrc();
				kind = rule.getKind();
				seqNum = rule.getSeqNum();
				duplicate = rule.getDuplicate();
				
				if(rule.getAction().equalsIgnoreCase("drop"))   /// revisit
				{
					//if(duplicate.equals(message.getDuplicate()))
						//System.out.println("dup flag is " + message.getDuplicate());
					if((dest == null || dest.equalsIgnoreCase(message.getDest())) && (src == null || src.equalsIgnoreCase(message.getSrc()))
							&& (kind == null || kind.equalsIgnoreCase(message.getKind())) && (seqNum == -1 || seqNum == message.getSeqNum())
							&& (duplicate == false || duplicate.equals(message.getDuplicate())))
						return null;
				}
				else if(rule.getAction().equalsIgnoreCase("delay"))
				{
					if((dest == null || dest.equalsIgnoreCase(message.getDest())) && (src == null || src.equalsIgnoreCase(message.getSrc()))
							&& (kind == null || kind.equalsIgnoreCase(message.getKind())) && (seqNum == -1 || seqNum == message.getSeqNum())
							&& (duplicate ==  false || duplicate.equals(message.getDuplicate())))
					{
						delayedInMsg.add(message);
						return null;
					}
				}
				else if(rule.getAction().equalsIgnoreCase("duplicate"))
				{
					//System.out.println("in dups dest: "+dest+" src " + src+ " kind "+kind+ " seqnum " + seqNum);
					//System.out.println("message dest: "+message.getDest()+" src " + message.getSrc()+ " kind "+message.getKind()+ 
					//		" seqnum " + message.getSeqNum());
					if((dest == null || dest.equalsIgnoreCase(message.getDest())) && (src == null || src.equalsIgnoreCase(message.getSrc()))
							&& (kind == null || kind.equalsIgnoreCase(message.getKind())) && (seqNum == -1 || seqNum == message.getSeqNum())
							&& (duplicate ==  false || duplicate.equals(message.getDuplicate())))
					{
						copy = new Message(null, null, null);
						copy.setSrc(message.getSrc());
						copy.setDest(message.getDest());
						copy.setKind(message.getKind());
						copy.setData(message.getData());
						copy.setSeqNum(message.getSeqNum());
						copy.setDuplicate(true);
							
						array.add(message);
						array.add(copy);
							
						// add potentially delayed messages to out buffer
						for(int j = 0; j < delayedInMsg.size(); j++)
							array.add(delayedInMsg.get(j));
						delayedInMsg.clear();
						
						array.trimToSize();
						return array;
					}
				}
					
			}
		}		
		array.add(message);
		for(int i = 0; i < delayedInMsg.size(); i++)
			array.add(delayedInMsg.get(i));
		delayedInMsg.clear();
		
		array.trimToSize();
		return array;
	}
	
	/*
	 * Getters
	 */
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
	
	public String getLocalName()
	{
		return localName;
	}
	
	@SuppressWarnings("unchecked")
	public void parseConfig() throws FileNotFoundException 
	{
		configFile = new File("/afs/andrew/usr1/ykandiss/public/" + configFileName); 
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
			if(temp.equalsIgnoreCase("configuration"))
			{
				list = (ArrayList) map.get(temp);
				//System.out.println(((LinkedHashMap<String, String>) list.get(0)).get("name"));
				for(int i = 0; i < list.size(); i++)
				{
					conn = new Connection("", "", 0);
					conn.setIP(((LinkedHashMap<String, String>)list.get(i)).get("ip"));
					conn.setPort((int)((LinkedHashMap) list.get(i)).get("port")); 
					conn.setName(((LinkedHashMap<String, String>) list.get(i)).get("name"));
					connList.add(conn);
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



