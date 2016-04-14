package cs5300Project1b;

import java.util.Date;
/**
 * 
 * SessionState object contains all the session information stored in the Session table including 
 * sessionID, version number, display message and session expiration time. Also this class provide get, set functions 
 * for each field of object and serialize, deserialize method.
 *
 */

public class SessionState {
	
	private SessionID sessionID;
	private int version;
	private String message;
	private long expirationTime; 
	
	public SessionState(SessionID sid){
		sessionID = sid;
		version = 0;
		message = Constant.welcomeMsg;
		setExpireTime();
	}
	
	public SessionState(SessionID sid, int version){
		sessionID = sid;
		this.version = version;
		setExpireTime();
	}
	public SessionState(SessionID sid, int version, String message) {
		sessionID = sid;
		this.version = version;
		this.message = message;
		setExpireTime();
	}
	public SessionState(String session) {
		String[] info = session.split("\\|");
		assert(info.length == 6); 
		sessionID = new SessionID(info[0], Integer.parseInt(info[1]), Integer.parseInt(info[2]));
		sessionID.setRebootNum(Integer.parseInt(info[1]));
		version = Integer.parseInt(info[3]);
		message = info[4];
		//System.out.println(info[5]);
		expirationTime = Long.parseLong(info[5].trim());
	}
	
	public String serialize(){
		return sessionID.serialize() + "|" + version + "|" + message + "|" + String.valueOf(expirationTime);
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
		expirationTime = new Date().getTime() + Constant.expTime + Constant.delta;
	}
	
	public long getExpireTime(){
		return expirationTime;
	}
}
