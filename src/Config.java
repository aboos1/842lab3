import java.util.ArrayList;
import java.util.List;


/* 18-842 Distributed Systems
 * Lab 1
 * Group 41 - Ankur & Di 
 */

public class Config {



	List<SocketInfo> configuration;
	List<Rule> sendRules;
	List<Rule> receiveRules;

	List<Group> groupList;
	boolean isLogical;

	public Config() {
	}
	
	public List<SocketInfo> getConfiguration() {
		return configuration;
	}
	public void setConfiguration(List<SocketInfo> hosts) {
		this.configuration = hosts;
	}
	public List<Rule> getSendRules() {
		return sendRules;
	}
	public void setSendRules(List<Rule> sendRules) {
		this.sendRules = sendRules;
	}
	public List<Rule> getReceiveRules() {
		return receiveRules;
	}
	public void setReceiveRules(List<Rule> receiveRules) {
		this.receiveRules = receiveRules;
	}
	
	public SocketInfo getConfigSockInfo(String name) {
		for(SocketInfo s : configuration) {
			if(s.getName().equals(name)) {
				return s;
			}
		}
		return null;
	}
	
	public List<Group> getGroupList() {
		return groupList;
	}

	public void setGroupList(List<Group> groupList) {
		this.groupList = groupList;
	}

	public ArrayList<String> findGroupMember(String groupName) {
		for(Group e : this.groupList) {
			if(e.getGroupName().equals(groupName))
				return e.getMemberList();
		}
		System.out.println("We cannot find this group");
		return null;
	}

	@Override
	public String toString() {
		return "Config [configuration=" + configuration + ", sendRules="
				+ sendRules + ", receiveRules=" + receiveRules + ", groupList="
				+ groupList + ", isLogical=" + isLogical + "]";
	}	

}
