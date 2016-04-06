package cs5300Project1b;
import java.util.Date;
public class SessionInfo {
	private static int expTime  = 30 * 1000;
	private Integer version;
	private String message;
	private Date expiration_timestamp = new Date();
	
	public SessionInfo (int version) {
		this.version = version;
		message = "Hello User";
		setExpirationTime();
	}
	
	public void setExpirationTime() {
		expiration_timestamp.setTime(new Date().getTime() + expTime);
	}
	
	public Date getExpriationTime() {
		return expiration_timestamp;
	}
	
	public void setMessage(String m) {
		message = m;
	}
	
	public String getMessage(){
		return message;
	}
	
	public void increaseVersion() {
		version++;
	}
	
	public int getVersion() {
		return version;
	}
}

