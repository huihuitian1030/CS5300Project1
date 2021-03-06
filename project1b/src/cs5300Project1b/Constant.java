package cs5300Project1b;
/**
 * This class recored all the global Constant string and number used in this project.
 */
public class Constant {
	protected static final String DOMAIN = "p1b";
	protected static final String ATTRIBUTE = "IPv4";
	protected static final int N = 5;	
	protected static final String cookieName = "CS5300PROJECT";
	protected static final int portProj1bRPC = 5300;
	protected static final int W = 3;
	protected static final int WQ = 2;
	protected static final int R = 2;
	protected static final int UDP_PACKET_LENGTH = 512;
	protected static int msgMaxLen = 512 - 4 * 8 - 32 - 6;
	protected static final String welcomeMsg ="Hello User!";
	protected static final String WRITE = "sessionWrite";
	protected static final String READ = "sessionRead";
	protected static final String defaultIPAddr = "0.0.0.0";
	protected static final int expTime = 180 * 1000;
	protected static final int delta = 3000;
	protected static final int cleanUpTime = 240 * 1000;
	protected static final String socketTimeOutWQMessage = "Cannot receive WQ reply messages";
	protected static final String logoutMessage = "You have logout";
	protected static final String cookieDomain = "ts679.bigdata.systems";
}
