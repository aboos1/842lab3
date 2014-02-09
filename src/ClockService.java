/* 18-842 Distributed Systems
 * Lab 2
 * Group 30 - aboos & dil1
 */

public abstract class ClockService {

	private TimeStamp ts;
	public ClockService(TimeStamp newTS) {
		this.ts = newTS;
	}

	public TimeStamp getTs() {
		return ts;
	}
	public void setTs(TimeStamp ts) {
		this.ts = ts;
	}
	public void cleanUp() {
		this.ts = new TimeStamp();
	}
	public abstract void updateTS(TimeStamp ts);
	public abstract void addTS(String srcName);
}
