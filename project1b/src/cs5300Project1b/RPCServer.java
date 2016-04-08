package cs5300Project1b;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class RPCServer extends Thread {
	
	private SessionServer ssServer = null;
	
	public RPCServer (SessionServer ssServer) {
		this.ssServer = ssServer;
	}
	
	@Override
	public void run() {
		DatagramSocket rpcSocket =  null;
		try {
			rpcSocket = new DatagramSocket(Constant.portProj1bRPC);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		while(true) {
			byte[] inBuf = new byte[Constant.UDP_PACKET_LENGTH];
			DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
			
			try {
				rpcSocket.receive(recvPkt);
				InetAddress returnAddr = recvPkt.getAddress();
				int returnPort = recvPkt.getPort();
				inBuf = recvPkt.getData();
				String inMessage = new String(inBuf);
				String[] messageInfo = inMessage.split("__");
				String callId = messageInfo[0];
				String operationCode = messageInfo[1];
				String argument = messageInfo[2];
				byte[] outBuf = null;
				switch (operationCode) {
				case "operationSESSIONREAD":
					//String session = ssServer.SessionRead(argument);
					//String rmsg = "" + callId + "__" + session;
					//outBuf = rmsg.getBytes();
					break;
					
				case "operationSESSIONWRITE":
					//String sid = ssServer.SessionWrite(argument);
					//String wmsg = "" + callId + "__" + sid;
					//outBuf = wmsg.getBytes();
					break;
				default:
					throw new IllegalArgumentException("Illegal operationCode");    
				}
				
				DatagramPacket rviewPkt = new DatagramPacket(outBuf, outBuf.length, returnAddr, returnPort);
				rpcSocket.send(rviewPkt);
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}

	}
}
