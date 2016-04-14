package cs5300Project1b;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

/**
 * Servlet implementation class server
 * This class is the implementation of the application server. 
 * It is responsible for handle the come-in HTTP request from browser and initialize the RPC client at the current server.
 * Then it send the message to RPC client.
 * Finally it receive the reply message from RPC client and pass the attribute to .jsp to build the web page. 
 */
@WebServlet("/server")
public class AppServer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	

	private RPCClient rpcClient;
	private String IPAddr = Constant.defaultIPAddr;
	private String svrID = "None";
	private int rebootNum = 0;
	private HashMap<String, String> idIPmap;
	private static Integer sessNum = 1;

	
    /**
     * @throws IOException 
     * @see HttpServlet#HttpServlet()
     * 
     * It is the constructor of the applicatoin server.
     */
    public AppServer() throws IOException {
        super();
        idIPmap = new HashMap<String,String>();
        BufferedReader br = new BufferedReader(new FileReader("/usr/share/tomcat8/webapps/local-ipv4"));
        this.IPAddr = br.readLine().trim();
        br.close();
        br = new BufferedReader(new FileReader("/usr/share/tomcat8/webapps/ami-launch-index"));
        this.svrID = br.readLine().trim();
        br.close();
        br = new BufferedReader(new FileReader("/usr/share/tomcat8/webapps/reboot-num"));
        this.rebootNum = Integer.parseInt(br.readLine().trim());
        br.close();
        br = new BufferedReader(new FileReader("/usr/share/tomcat8/webapps/db-data"));
        String map = br.readLine().trim();
        
        String[] nodes = map.split("\\;");
        for(int r = 0 ; r < nodes.length;r++){
        	String node = nodes[r].trim();
        	String id = node.split("\\s+")[1].trim();
        	String ip = node.split("\\s+")[0].trim();
        	idIPmap.put(id, ip);
        }
        br.close();
        
        rpcClient = new RPCClient();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 * 
	 * The doGet() handles the function "Replace" or "Refresh".
	 * If the current cookie is null, the application server should ask the rpc client to create a session on itself
	 * and send write requests to W RPC servers.
	 * 
	 * If the current cookie is not null. Based on the function of HTTP request, the RPC client operate differently.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		Cookie curCookie = null;
		Cookie[] cookies = request.getCookies();
		String action = request.getParameter("function");
		boolean replace = false;
		String msg = null;
		
		// check whether current action is replace 
		if(action!=null && action.equals("Replace")){	
			replace =true;	
			msg = request.getParameter("newStr");
		}
		
		if(cookies!=null ){
			for(Cookie cookie : cookies){
				if(cookie.getName().equals(Constant.cookieName)){
					curCookie = cookie;
					break;
				}
			}
		}
		Cookie newCookie = null;
		
		// cookie is null
		if(curCookie==null || curCookie.getValue().equals(Constant.logoutMessage) || curCookie.getValue().equals(Constant.socketTimeOutWQMessage)){
			SessionID sid = new SessionID(this.svrID,this.rebootNum,getSessionNum());
			newCookie = AppServerWrite(sid,0,Constant.welcomeMsg,request);	
		}
		
		//cookie is not null
		else{
						
			String[] token = URLDecoder.decode(curCookie.getValue().trim(),"UTF-8").split("_");

			SessionID sid = new SessionID(token[0],Integer.parseInt(token[1]),Integer.parseInt(token[2]));
			int version = Integer.parseInt(token[3]);
			ArrayList<String> destAddr = new ArrayList<String>();
			for (int i = 4; i<token.length; i++) {
				destAddr.add(idIPmap.get(token[i]));
			}

			// write
			// first we should do read request to make sure that the current session state is not expired.
			// if it is timed out, it should create a new session ID for the write request.
			if(replace){
				String rStr = rpcClient.SessionReadClient(sid, version, destAddr).trim();
				
				if(rStr.equals("Failure")){
					SessionID sid2 = new SessionID(this.svrID,this.rebootNum,getSessionNum());
					newCookie = AppServerWrite(sid2,0,msg,request);	
				}else{
					String[] rtoken = rStr.split("__");
					request.setAttribute("preSvrID", rtoken[1]);
					newCookie = AppServerWrite(sid, version, msg, request);
				}
			}
			
			//read
			else{
				newCookie = AppServerRead(sid,version,destAddr,request);
			}	
		}
		
		// if the socket is timed out, it redirect to the error page
		if (newCookie.getValue().equals(Constant.socketTimeOutWQMessage)) {
			newCookie.setMaxAge(0);
			newCookie.setDomain(Constant.cookieDomain);
			response.addCookie(newCookie);
			request.getRequestDispatcher("errorPage.jsp").forward(request, response);
		} else {
			newCookie.setMaxAge(Constant.expTime);
			newCookie.setDomain(Constant.cookieDomain);
			response.addCookie(newCookie);
			request.setAttribute("curSvrID", this.svrID);
			request.setAttribute("cookie", newCookie);	
			request.getRequestDispatcher("main.jsp").forward(request, response);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 * The doPost() handles the HTTP request with the logout function.
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		
		Cookie newCookie = new Cookie(Constant.cookieName,Constant.logoutMessage);
	    newCookie.setMaxAge(0);
		newCookie.setDomain(Constant.cookieDomain);
		response.addCookie(newCookie);
		request.getRequestDispatcher("logout.jsp").forward(request, response);
	}
	
	/**
	 * 
	 * @param sid, session ID.
	 * @param version, current version for the session ID.
	 * @param msg, the message for the session state.
	 * @param request
	 * @return, the new cookie created for this HTTP request.
	 * @throws UnsupportedEncodingException
	 * 
	 * This function asks the RPC client to send write request and builds the new cookie based on receiving the reply message from RPC client.
	 */
	public Cookie AppServerWrite(SessionID sid,int version,String msg,HttpServletRequest request) throws UnsupportedEncodingException{
		SessionState ss1 = new SessionState(sid,version,msg);
		ArrayList<String> destAddr = genWQRandomAddrs();

		String wStr = rpcClient.SessionWriteClient(ss1,destAddr);
		String[] reply_data = wStr.trim().split("\\__");
		if (reply_data[0].equals(Constant.socketTimeOutWQMessage)) {
			Cookie errorCookie = new Cookie(Constant.cookieName,Constant.socketTimeOutWQMessage);
			return errorCookie;
		}
		SessionID sid2 = new SessionID(reply_data[reply_data.length -1]);
		ArrayList<String> svrIDs = new ArrayList<String> ();
		for (int i = 0; i< reply_data.length -1; i++) {
			svrIDs.add(reply_data[i]);
		}
		version++;
		SessionState ss2 = new SessionState(sid2,version,msg);
		request.setAttribute("SessionState", ss2);
		Cookie newCookie = new Cookie(Constant.cookieName,URLEncoder.encode(createCookieValue(ss2,svrIDs),"UTF-8"));
		return newCookie;
	}
	
	/**
	 * 
	 * @param sid, session ID.
	 * @param version, current version.
	 * @param destAddr, destination address for read request.
	 * @param request
	 * @return the new cookie created for this HTTP request.
	 * @throws UnsupportedEncodingException
	 * 
	 * This function asks the RPC client to send read request and builds the new cookie based receiving the reply message from RPC client.
	 */
	public Cookie AppServerRead(SessionID sid, int version, ArrayList<String> destAddr,HttpServletRequest request) throws UnsupportedEncodingException{
		String rStr = rpcClient.SessionReadClient(sid, version, destAddr).trim();
		if(rStr.equals("Failure")){
			SessionID sid2 = new SessionID(this.svrID,this.rebootNum,getSessionNum());
			return AppServerWrite(sid2,0,Constant.welcomeMsg,request);
		}
		else{
			String[] token = rStr.split("__");
			SessionState ss2 = new SessionState(token[0]);
			request.setAttribute("preSvrID", token[1]);
			return AppServerWrite(sid,version,ss2.getMessage(),request);
		}
	}

	//create the value for the new cookie using session ID and version number 
	/**
	 * 
	 * @param ss, the session state for the cookie.
	 * @param svrIDs, the svrIDs that the session state is stored
	 * @return the cookie value with the specific format
	 */
	private String createCookieValue(SessionState ss, ArrayList<String> svrIDs){
		StringBuilder sb = new StringBuilder();
		sb.append(ss.getSessionID().serializeForCookie());
		sb.append("_");
		sb.append(ss.getVersion());
		for (int i = 0; i < svrIDs.size();i++) {
			sb.append("_");
			sb.append(svrIDs.get(i));
		}
		return sb.toString();
	}
	
	public int getRebootNum(){
		return this.rebootNum;
	}
	
	public String getAddr(){
		return this.IPAddr;
	}
	
	public String getSvrID(){
		return this.svrID;
	}
	
	/**
	 * @return the distinct W random chosen addresses for write request.
	 */
	public ArrayList<String> genWQRandomAddrs(){
		int[] randomSvrID = new Random().ints(0,Constant.N).distinct().limit(Constant.W).toArray(); 

		ArrayList<String> destAddr = new ArrayList<String>();
		for(int i = 0;i<Constant.W;i++){
			destAddr.add(idIPmap.get(String.valueOf(randomSvrID[i])));
		}
		return destAddr;
	}
	
	/**
	 * We only call this function when we build a new sessionID on the current server,
	 * so we need automatically increase the session number on the current server.
	 * @return the current session number
	 */
	public int getSessionNum(){
		int res = sessNum;
		synchronized(sessNum) {
			sessNum++;
		}
		return res;
	}
}
	
	