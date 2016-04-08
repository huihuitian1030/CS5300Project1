package cs5300Project1b;

import java.util.Date;

public class mySession {
	
	private SessionID sessionID;
	private int version;
	private String message;
	private long expirationTime; 
	
	public mySession(SessionID sid){
		sessionID = sid;
		version = 1;
		message = Constant.welcomeMsg;
		setExpireTime();
	}
	
	public mySession(String session) {
		String[] info = session.split("__");
		assert(info.length == 6); 
		SessionID sessionID = new SessionID(Integer.parseInt(info[0]), Integer.parseInt(info[2]));
		sessionID.setRebootNum(Integer.parseInt(info[1]));
		version = Integer.parseInt(info[3]);
		message = info[4];
		expirationTime = Long.parseLong(info[5]);
	}
	
	public String serialize(){
		return sessionID.serialize() + "__" + version + "__" + message + "__" + String.valueOf(expirationTime);
	}

	public SessionID getSessionID(){
		return sessionID;
	}
	
	public int getVersion(){
		return version;
	}
	
	public void addVersion(){
		version = version +1;
	}
	
	public String getMessage(){
		return message;
	}
	
	public void setMessage(String str){
		if(str.length() > Constant.msgMaxLen){
			str = str.substring(0,Constant.msgMaxLen);
		}
		message = str;
		
	}
	
	public void setExpireTime(){
		expirationTime = new Date().getTime() + Constant.expTime;
	}
	
	public long getExpireTime(){
		return expirationTime;
	}
}
