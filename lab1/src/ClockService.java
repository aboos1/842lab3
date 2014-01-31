import java.util.ArrayList;


public interface ClockService
{
	public ArrayList<Integer> getTimeStamp();
	public void updateTimeStamp(ArrayList<Integer> list);
	public String toString();
}
