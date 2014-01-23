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
	private ArrayList<Message> out_buffer = new ArrayList<Message>();    //buffered writer?
	private LinkedList<Message> in_buffer = new LinkedList<Message>();
	private ArrayList<Connection> connList = new ArrayList<Connection>();
	private ArrayList<Rule> ruleList = new ArrayList<Rule>();
	private ArrayList<Message>delayedMsg = new ArrayList<Message>();
	private String localHostName;
	
	public MessagePasser(String configuration_filename, String local_name)
	{
		localHostName = local_name;
	}
	
	public void send(Message message)
	{
		Rule rule;
		Message copy;
		boolean match = false;
		
		for(int i=0; i < ruleList.size(); i++)
		{
			rule = ruleList.get(i);
			if(rule.getAction().equalsIgnoreCase("drop"))   /// revisit
				{
					if(rule.getDest().equalsIgnoreCase(message.getDest()) && rule.getSrc().equalsIgnoreCase(message.getSrc())
							&& rule.getKind().equalsIgnoreCase(message.getKind()))
						{
							if(rule.getSeqNum() == -1 || (rule.getSeqNum() == message.getSeqNum())
									return;
						}				
				}
			else if(rule.getAction().equalsIgnoreCase("delay"))
			{
				if(rule.getDest().equalsIgnoreCase(message.getDest()) && rule.getSrc().equalsIgnoreCase(message.getSrc())
						&& rule.getKind().equalsIgnoreCase(message.getKind()))
				{
					if(rule.getSeqNum() == -1 || (rule.getSeqNum() == message.getSeqNum()))
					{
						delayedMsg.add(message);
						return;
					}
							
				}	
			}
			else if(rule.getAction().equalsIgnoreCase("duplicate"))
			{
				if(rule.getDest().equalsIgnoreCase(message.getDest()) && rule.getSrc().equalsIgnoreCase(message.getSrc())
						&& rule.getKind().equalsIgnoreCase(message.getKind()))
				{
					if(rule.getSeqNum() == -1 || (rule.getSeqNum() == message.getSeqNum()))
					{
						copy = message;
						copy.setDuplicate(true);
						
						out_buffer.add(message);
						out_buffer.add(copy);
						return;
					}
				}
				
			}
		}
		out_buffer.add(message);	
	}
	
	public Message receive()
	{
		return in_buffer.removeFirst();
	}
	
	/*
	 * Getters
	 */
	public ArrayList<Message> getOutBuffer()
	{
		return out_buffer;
	}
	
	public LinkedList<Message> getInBuffer()
	{
		return in_buffer;
	}
	
	public String getLocalName()
	{
		return localHostName;
	}
	
	@SuppressWarnings("unchecked")
	public void parseConfig() throws FileNotFoundException 
	{
	    InputStream input = new FileInputStream(new File(
	            "C:/Users/YAYA/workspace_1/DS_LAB0/src/config.yaml"));
	    Yaml yaml = new Yaml();
	    
		Map<String, Map> map = yaml.loadAs(input, Map.class);
		Iterator iterator  = map.keySet().iterator();
		String temp;	
		ArrayList list;
		Map<String, String> inner_map;
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
				System.out.println(temp);
				
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



