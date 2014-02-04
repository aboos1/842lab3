import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;

public class LogMessage implements Serializable {

	String event;
	TimeStamp eventTS; 
	ArrayList<LogMessage> nextMsgs = new ArrayList<LogMessage>();
	
	public LogMessage(String event, TimeStamp eventTs) 
	{
		this.event = event;
		this.eventTS = eventTs;
	}
	
	public LogMessage(LogMessage logMsg) 
	{
		this.event = logMsg.getEvent();
		this.eventTS = logMsg.getEventTS();
	}
	
	public String getEvent()
	{
		return this.event;
	}
	
	public TimeStamp getEventTS()
	{
		return this.eventTS;
	}
	
	public ArrayList<LogMessage> getNextMsgs()
	{
		return this.nextMsgs;
	}
	
	public String toString()
	{
		return ("Event: " + this.event + "TS: " + this.eventTS);
	}
}