import java.util.ArrayList;


public class LogicalClock implements ClockService
{
	private ArrayList<Integer> timeStamp;
	
	public LogicalClock()
	{
		timeStamp = new ArrayList<Integer>(1);
		timeStamp.add(0);
	}
	
	public ArrayList<Integer> getTimeStamp()
	{
		return timeStamp;
	}
	
	public void updateTimeStamp(ArrayList<Integer> timeStamps2)
	{
		timeStamp.set(0, (Math.max(timeStamp.get(0), timeStamps2.get(0)) +1));
	}
	
	public String toString()
	{
		return Integer.toString(timeStamp.get(0));
	}
}
