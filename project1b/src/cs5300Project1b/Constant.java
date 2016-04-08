package cs5300Project1b;

public class Constant {
	
	protected static final String cookieName = "CS5300PROJECT";
	protected static final int portProj1bRPC = 5300;
	protected static final int UDP_PACKET_LENGTH = 512;
	protected static final int ONE_SECOND_IN_MILLIS=1000;
	protected static int msgMaxLen = 512 - 4 * 8 - 32 - 6;
	protected static final String welcomeMsg ="Hello User!";
	protected static final String WRITE = "sessionWrite";
	protected static final String READ = "sessionRead";
	protected static final String defaultIPAddr = "0.0.0.0";

}
