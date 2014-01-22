public class Rule
{
	private String type;
	private String action;
	private String src;
	private String dest;
	private String kind;
	private int seqNum;
	
	public Rule()
	{
		type="";
		action = "";
		src = "";
		dest = "";
		kind = "";
		seqNum = -1;
	}
	
	public String getType()
	{
		return type;
	}
	
	public String getAction()
	{
		return action;
	}
	
	public String getSrc()
	{
		return src;
	}
	public String getDest()
	{
		return dest;
	}
	
	public String getKind()
	{
		return kind;
	}
	public int getSeqNum()
	{
		return seqNum;
	}
	
	public void setType(String t)
	{
		type = t;
	}
	
	public void setDest(String t)
	{
		dest = t;
	}
	
	public void setSrc(String t)
	{
		src = t;
	}
	
	public void setKind(String t)
	{
		kind = t;
	}
	
	public void setseqNum(int t)
	{
		seqNum = t;
	}
	
	public void setAction(String action)
	{
		this.action = action;
	}
}