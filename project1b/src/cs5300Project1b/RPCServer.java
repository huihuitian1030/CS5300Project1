package cs5300Project1b;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class RPCServer extends Thread {
	
	public static final int portProj1bRPC = 5300;
	public static final int UDP_PACKET_LENGTH = 512;
	
	@Override
	public void run() {
		DatagramSocket rpcSocket =  null;
		try {
			rpcSocket = new DatagramSocket(portProj1bRPC);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		while(true) {
			byte[] inBuf = new byte[UDP_PACKET_LENGTH];
			DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
			
			try {
				rpcSocket.receive(recvPkt);
				InetAddress returnAddr = recvPkt.getAddress();
				int returnPort = recvPkt.getPort();
				inBuf = recvPkt.getData();
				String inMessage = new String(inBuf);
				
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}

	}
}
