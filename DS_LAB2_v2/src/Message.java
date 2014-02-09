import java.io.Serializable;
import java.util.ArrayList;


public class Message implements Serializable, Comparable
{
	private ArrayList<String> dest;
	private String group;
	private String src;
	private int seqNum, start_num, length;
	private boolean duplicate;
	private String kind;
	private Object data;
	private int port;
	private String hostname, originalDest, loggedRule, originalSrc;
	
	/*
	 * Constructor
	 */
	public Message(String grp, String kind, Object data, int start, int lenth)
	{
		group = grp;
		this.kind = kind;
		this.data = data;
		dest = new ArrayList<String>();
		seqNum = 0;
		start_num = start;
		this.length = lenth;
		duplicate = false;
		originalDest = null;
		originalSrc = null;
	}
	
	/*
	 * Getters
	 */
	public ArrayList<String> getDest()
	{
		return dest;
	}
	
	public String getOriginalSrc()
	{
		return originalSrc;
	}
	
	public String getOriginalDest()
	{
		return originalDest;
	}
	
	public String getLoggedRule()
	{
		return loggedRule;
	}
	
	public String getSrc()
	{
		return src;
	}
	
	public String getKind()
	{
		return kind;
	}
	
	public int getSeqNum()
	{
		return seqNum;
		
	}
	
	public boolean getDuplicate()
	{
		return duplicate;
	}
	
	public Object getData()
	{
		return data;
	}
	
	public String getHostName()
	{
		return hostname;
	}
	
	public int getPort()
	{
		return port;
	}
	
	public String getGroup()
	{
		return group;
	}
	
	public int getMessageStart()
	{
		return start_num;
	}
	
	public int getMessageLength()
	{
		return length;
	}
	
	/*
	 * Setters
	 */
	public void setDest(ArrayList<String> dest)
	{
		for(int i=0; i <dest.size(); i++)
			this.dest.add(dest.get(i));
	}
	
	public void setOriginalSrc(String od)
	{
		originalSrc = od;
	}
	
	public void setOriginalDest(String od)
	{
		originalDest = od;
	}
	
	public void setLoggedRule(String rule)
	{
		loggedRule = rule;
	}
	
	public void setSrc(String src)
	{
		this.src = src;
	}
	
	public void setSeqNum(int num)
	{
		seqNum = num;
	}
	
	public void setDuplicate(boolean dupl)
	{
		duplicate = dupl;
	}
	
	public void setKind(String kind)
	{
		this.kind = kind;
	}
	
	public void setData(Object data)
	{
		this.data = data;
	}
	
	public void setHostname(String name)
	{
		hostname = name;
	}
	
	public void setPort(int port)
	{
		this.port = port;
	}
	
	public void setGroup(String group)
	{
		this.group = group;
	}
	
	/*
	 * Check whether a node is included in the destination list
	 */
	public boolean includeDest(String host)
	{
		for(int i=0; i<dest.size(); i++)
			if(host.equalsIgnoreCase(dest.get(i)))
				return true;
		
		return false;
	}
	
	/*
	 * We consider that two messages are equal if they have the same src and seqNum
	 * by definition, this combination uniquely identifies a message...
	 * 
	 * If two messages are equal, ret 0 else, ret -1
	 */
	public int compareTo(Object msg)
	{
		if(src.equalsIgnoreCase(((Message)msg).getSrc()) && seqNum == ((Message)msg).getSeqNum())
			return 0;
		else
			return -1;
	}
}
