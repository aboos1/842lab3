import java.util.ArrayList;


public class VectorClock implements ClockService, Comparable<VectorClock>
{
	private ArrayList<Integer> timeStamps;
	
	
	public VectorClock(int size)
	{
		timeStamps = new ArrayList<Integer>(size);
		
		for(int i=0; i<timeStamps.size(); i++)
			timeStamps.add(0);
	}
	
	public ArrayList<Integer> getTimeStamp() 
	{
		return timeStamps;
	}

	public void updateTimeStamp(ArrayList<Integer> timeStamps2) 
	{
		for(int i=0; i<timeStamps.size(); i++)
			timeStamps.set(i, Math.max(timeStamps.get(i), timeStamps2.get(i)));
	}
	
	public int compareTo(VectorClock vector) 
	{
		int equal = 0, less = 0, greater = 0;
		ArrayList<Integer> list = vector.getTimeStamp();
		int size = timeStamps.size();
		
		for(int i=0; i<size; i++)
		{
			if(timeStamps.get(i) == list.get(i))
				equal++;
			else if(timeStamps.get(i) < list.get(i))
				less++;
			else if(timeStamps.get(i) > list.get(i))
				greater++;
		}
		
		if(equal == size)
			return 0;
		else if(less == size)
			return -1;
		else if(greater == size)
			return 1;
		else if((equal + less) == size)
			return -2;
		else if((equal + greater) == size)
			return 2;
		else
			return 3;     // concurrent
	}
	
	public String toString()
	{
		String timeStamp = "(";
		int size = timeStamps.size();
		
		for(int i = 0; i < size; i++)
			if (i == size)
				timeStamp += timeStamps.get(i) + ")";
			else
				timeStamp += timeStamps.get(i) + ", ";
		
		return timeStamp;
	}
	
}
