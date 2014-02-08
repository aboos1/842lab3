import java.io.Serializable;
import java.util.ArrayList;


public class TimeStampedMessage extends Message implements Serializable
{
	private TimeStamp timeStamp, logsTimeStamp;
	
	public TimeStampedMessage(String group, String kind, Object data, int start, int length,
								String clock_service, int size, TimeStamp ts)
	{
		super(group, kind, data, start, length);
		
		timeStamp = new TimeStamp(clock_service, size);	
		logsTimeStamp = new TimeStamp(clock_service, size);
		setTimeStamp(ts);
	}
	
	public TimeStamp getTimeStamp()
	{
		return timeStamp;
	}
	
	public TimeStamp getLogsTimeStamp()
	{
		return logsTimeStamp;
	}
	
	private void setTimeStamp(TimeStamp ts)
	{
		for(int i = 0; i <timeStamp.getTimeStamp().size(); i++)
			timeStamp.getTimeStamp().set(i, ts.getTimeStamp().get(i));
	}
	
	public void setLogsTimeStamp(TimeStamp ts)
	{
		for(int i = 0; i <logsTimeStamp.getTimeStamp().size(); i++)
			logsTimeStamp.getTimeStamp().set(i, ts.getTimeStamp().get(i));
	}
}
