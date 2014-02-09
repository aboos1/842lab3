import java.util.ArrayList;


public class multicastGroup {

	String groupname;
	ArrayList<String> members = new ArrayList<String>() ;
	
	public multicastGroup(String  name, ArrayList<String> members) {
		this.groupname = name;
		this.members = members;
	}
	
	public String getGroupName() {
		return this.groupname;
	}
	
	public ArrayList<String> getMembers() {
		return this.members;
	}
	
	public void setGroupName(String name) {
		this.groupname = name;
	}
	
	public void setMembers(ArrayList<String> members) {
		this.members = members;
	}
}
