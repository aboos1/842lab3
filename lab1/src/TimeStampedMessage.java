
public class TimeStampedMessage extends Message
{
	private TimeStamp timeStamp;
	
	public TimeStampedMessage(String dest, String kind, Object data, String clock_service, int size)
	{
		super(dest, kind, data);
		
		timeStamp = new TimeStamp(clock_service, size);		
	}
	
	public TimeStamp getTimeStamp()
	{
		return timeStamp;
	}
}
