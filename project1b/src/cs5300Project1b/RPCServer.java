package cs5300Project1b;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class RPCServer extends Thread {

	private String svrID;
	private int rebootNum;
	private ConcurrentHashMap<String, SessionState> sessionTable;
	private Thread clean;
	
	private HashMap<String, String> idIPmap;

	public RPCServer () throws IOException {
	     
	    BufferedReader br = new BufferedReader(new FileReader("/usr/share/tomcat8/webapps/ami-launch-index"));
	    this.svrID = br.readLine().trim();
	    br.close();
	    br = new BufferedReader(new FileReader("/usr/share/tomcat8/webapps/reboot-num"));
	    this.rebootNum = Integer.parseInt(br.readLine().trim());
	    br.close();
		
		
		sessionTable = new ConcurrentHashMap<String, SessionState>();
		clean = new CleanUp(sessionTable);
		clean.setDaemon(true);
		clean.start();
	}
	
	
	@Override
	public void run() {
		System.out.println("rpc server start running");
		DatagramSocket rpcSocket =  null;
		try {
			rpcSocket = new DatagramSocket(Constant.portProj1bRPC);
			while(true) {
				byte[] inBuf = new byte[Constant.UDP_PACKET_LENGTH];
				DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
				
				try {
					rpcSocket.receive(recvPkt);
					InetAddress returnAddr = recvPkt.getAddress();
					int returnPort = recvPkt.getPort();
					inBuf = recvPkt.getData();
					String inMessage = new String(inBuf);
					System.out.println("rpc server receive msg form rpc client: "+ inMessage);
					String[] messageInfo = inMessage.trim().split("\\__");
					assert(messageInfo.length == 3);
					String callId = messageInfo[0];
					String operationCode = messageInfo[1];
					String argument = messageInfo[2];
					byte[] outBuf = null;
					switch (operationCode) {
					case Constant.READ:
						System.out.println("----------RPC Server Session read-----------");
						SessionState session = SessionRead(argument);
						String rmsg = "" + callId + "__" + session.serialize() + "__" + this.svrID;
						System.out.println("rpc server send replyMsg to rpc client for read: "+ rmsg);
						outBuf = rmsg.getBytes();
						break;
						
					case Constant.WRITE:
						System.out.println("----------RPC Server Session write-----------");
						SessionID sid = SessionWrite(argument);
						String wmsg = "" + callId + "__" + sid.serialize()+"__"+this.svrID;
						System.out.println("rpc server send replyMsg to rpc client for write: "+ wmsg);
						outBuf = wmsg.getBytes();
						break;
					default:
						throw new IllegalArgumentException("Illegal operationCode");    
					}
					
					DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, returnAddr, returnPort);
					rpcSocket.send(sendPkt);
					
				} catch (IOException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
				
			}
		} catch (SocketException e) {
			System.out.println("rpc server send pkt to rpc client time out");
			e.printStackTrace();
		} finally{
			rpcSocket.close();
		}
	}
	
	public SessionState SessionRead(String key) {
		Date now = new Date();
		if (sessionTable.containsKey(key) && sessionTable.get(key).getExpireTime() > now.getTime()) {
			SessionState ss = sessionTable.get(key);
			return ss;
		} else {
			return new SessionState(new SessionID("None",this.rebootNum, 0));
		}
	}
	
	public SessionID SessionWrite(String sessionState) {
		SessionState givenSS = new SessionState(sessionState);
		SessionID currentID;
		SessionState currentSS;
		String key;
		//System.out.println(sessionState);
		//System.out.println(givenSS.getSessionID());
		
		currentID = givenSS.getSessionID();
		currentSS = new SessionState(currentID,givenSS.getVersion(), givenSS.getMessage());
		currentSS.addVersion();
		key = currentID.serialize() + "--" + currentSS.getVersion();
		sessionTable.put(key, currentSS);
		System.out.println(key);
		System.out.println("sessionTable size: "+ sessionTable.size());
		return currentID;
	}
	

}
