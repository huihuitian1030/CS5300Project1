package cs5300Project1b;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class RPCClient {
	private Integer callID = 1;
	SessionServer sessionServer;
	public RPCClient(SessionServer ss){
		this.sessionServer = ss ;
	}
	
	public DatagramPacket SessionReadClient(SessionID sid, ArrayList<String> destAddr){
		DatagramSocket rpcSocket = null;
		
		try{
			rpcSocket = new DatagramSocket();
			rpcSocket.setSoTimeout(10000);
		}catch (SocketException e){
			e.printStackTrace();
		}
		
		int cid = -1;
		synchronized (callID){
			cid = callID;
			callID++;
		}
		
		byte[] outBuf = new byte[Constant.UDP_PACKET_LENGTH];
		String callMsg = ""+cid + "__" + Constant.READ + "__" + sid.serialize();
		outBuf = callMsg.getBytes(); 
		for(String host : destAddr){
			if(host.equals(sessionServer.getAddr())){
				continue;
			}
			InetAddress addr = null;
			try {
				addr = InetAddress.getByName(host);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, addr, Constant.portProj1bRPC);
			try {
				rpcSocket.send(sendPkt);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		byte[] inBuf = new byte[Constant.UDP_PACKET_LENGTH];
		DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
		Integer recvCallID = -1;
		try {
			do {
				recvPkt.setLength(inBuf.length);
		        rpcSocket.receive(recvPkt);
		        String replyMsg = new String(recvPkt.getData());
		        String[] token = replyMsg.split("\\__");
		        recvCallID = new Integer(token[0]);
		        } while(recvCallID != cid);
			} catch(SocketTimeoutException stoe) {
				recvPkt = null;
			} catch(IOException ioe) {
				ioe.printStackTrace();
		    }
		rpcSocket.close();
		return recvPkt;
		
	}
	
	
	public DatagramPacket SessionWriteClient(mySession session, ArrayList<String> destAddr){
		DatagramSocket rpcSocket = null;
		try{
			rpcSocket = new DatagramSocket();
			rpcSocket.setSoTimeout(10000);
		}catch (SocketException e){
			e.printStackTrace();
		}
		
		int cid = -1;
		synchronized (callID) {
			cid = callID;
			callID++;
		}
		byte[] outBuf = new byte[Constant.UDP_PACKET_LENGTH];
		String callMsg = ""+ cid+ "__" + Constant.WRITE + "__" + session.serialize();
		outBuf = callMsg.getBytes();
		for(String host : destAddr){
			if(host.equals(sessionServer.getAddr())){
				continue;
			}
			
			InetAddress addr = null;
			try {
				addr = InetAddress.getByName(host);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			
			DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, addr, Constant.portProj1bRPC);
			try {
				rpcSocket.send(sendPkt);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		byte[] inBuf = new byte[Constant.UDP_PACKET_LENGTH];
		DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
		Integer recvCallID = -1;
		try {
			do {
				recvPkt.setLength(inBuf.length);
		        rpcSocket.receive(recvPkt);
		        String replyMsg = new String(recvPkt.getData());
		        String[] token = replyMsg.split("\\__");
		        recvCallID = new Integer(token[0]);
		        } while(recvCallID != cid);
			} catch(SocketTimeoutException stoe) {
				recvPkt = null;
			} catch(IOException ioe) {
				ioe.printStackTrace();
		    }
		
		rpcSocket.close();
		return recvPkt;
	}

}
