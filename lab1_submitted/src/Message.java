import java.io.Serializable;


public class Message implements Serializable
{
	private String dest;
	private String src;
	private int seqNum;
	private boolean duplicate;
	private String kind;
	private Object data;
	private int port;
	private String hostname, originalDest, loggedRule;
	
	/*
	 * Constructor
	 */
	public Message(String dest, String kind, Object data)
	{
		this.dest = dest;
		this.kind = kind;
		this.data = data;
		seqNum = 0;
		duplicate = false;
	}
	
	/*
	 * Getters
	 */
	public String getDest()
	{
		return dest;
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
	
	/*
	 * Setters
	 */
	public void setDest(String dest)
	{
		this.dest = dest;
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
}
