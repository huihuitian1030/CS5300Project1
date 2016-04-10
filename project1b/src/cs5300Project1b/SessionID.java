package cs5300Project1b;

public class SessionID {
	
	private String svrID;
	private int rebootNum;
	private int sessNum;
	
	public SessionID(String svrID,int rebootNum, int sessNum){
		this.svrID = svrID;
		this.rebootNum = rebootNum;
		this.sessNum = sessNum;
	}
	
	public SessionID(){
		this.svrID = "None";
		this.rebootNum  = 0;
		this.sessNum  = 0;
	}
	
	public String serialize(){
		return ""+svrID+"|"+rebootNum+"|"+sessNum;
	}
	
	public SessionID(String s){
		String[] token = s.split("\\|");
		svrID = token[0];
		rebootNum = Integer.parseInt(token[1]);
		sessNum = Integer.parseInt(token[2]);

	}
	
	public String getSvrID(){
		return svrID;
	}
	
	public int getRebootNum(){
		return rebootNum;
	}
	
	public void setRebootNum( int rebootNum ) {
		this.rebootNum = rebootNum;
	}
	
	public int getSessNum(){
		return sessNum;
	}
	
	public void increamentRootNum(){
		this.rebootNum++;
	}

}
