import java.io.Serializable;
import java.util.ArrayList;

public class TimeStamp implements Serializable
{
	ClockService cs;
	
	public TimeStamp(String service, int size)
	{
		cs = ClockServiceFactory.getClockService(service, size);
	}

	public ArrayList<Integer> getTimeStamp()
	{
		return cs.getTimeStamp();
	}
	
	public void updateTimeStamp(ArrayList<Integer> list)
	{
		cs.updateTimeStamp(list);
	}
}
