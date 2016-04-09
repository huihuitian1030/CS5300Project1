package cs5300Project1b;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;
/**
 * Servlet implementation class server
 */
@WebServlet("/server")
public class AppServer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private SimpleDBView sdbView;

	private RPCServer rpcServer;
	private RPCClient rpcClient;
	private String IPAddr = Constant.defaultIPAddr;
	private String svrID = "None";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AppServer() {
        super();
//        int delay = 0;
//        int period = 120*Constant.ONE_SECOND_IN_MILLIS;
//        Timer timer = new Timer();
//        timer.scheduleAtFixedRate(new TimerTask(){
//        	public void run(){
//        		Date newDate = new Date();
//        		removeExpSession(newDate);
//        	}
//        },delay,period);
        
        // TODO Auto-generated constructor stub
        
        rpcClient = new RPCClient(this);
        rpcServer = new RPCServer(this);
  
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
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
		}

		if(cookies!=null ){
			for(Cookie cookie : cookies){
				if(cookie.getName().equals(Constant.cookieName)){
					curCookie = cookie;
					break;
				}
			}
		}
		
		if(curCookie==null){
			SessionID sid = new SessionID();
			SessionState ss = new SessionState(sid);
			ArrayList<String> destAddr = new ArrayList<String>();
			for(int i = 0; i<Constant.W;i++){
				
			}
			DatagramPacket wPkt = rpcClient.SessionWriteClient(ss, destAddr);
			
			
		}
		else{
			String[] token = curCookie.getValue().split("__");
			SessionID sid = new SessionID(token[0]);
			int version = Integer.parseInt(token[1]);

			SessionState ms = new SessionState(sid);
			
			ArrayList<String> destAddr = new ArrayList<String>();

			 
			
			
		}
		
		
		Date newDate = new Date();

		


		

		response.addCookie(curCookie);
		request.setAttribute("cookie", curCookie);	
		request.getRequestDispatcher("main.jsp").forward(request, response);
		
	
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//doGet(request, response);
		Cookie curCookie = null;
		Cookie[] cookies = request.getCookies();
		
		
		if(cookies!=null ){
			for(Cookie cookie : cookies){
				if(cookie.getName().equals(Constant.cookieName)){
					curCookie = cookie;
					break;
				}
			}
		}
		
	
		

		
	}


	//create the value for the new cookie using session ID and version number 
	private String createCookieValue(SessionState ms, int primaryID, int secondID){
		return ms.getSessionID().serialize()+ "_" + ms.getVersion() + "_" + primaryID+"_" + secondID;
	}
	//get the session ID given a cookie
	private String getSid(Cookie cookie){
		return  cookie.getValue().split("__")[0];	
	}
	//get version number given a cookie
	private int getV(Cookie cookie){
		return Integer.parseInt(cookie.getValue().split("__")[1]);
	}
	
	public String getAddr(){
		return this.IPAddr;
	}
	
	public String getSvrID(){
		return this.svrID;
	}
}
	
	