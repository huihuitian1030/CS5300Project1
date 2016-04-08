package cs5300Project1b;

import java.io.IOException;
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
public class SessionServer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static ConcurrentHashMap<String, mySession> sessionTable = new ConcurrentHashMap<>();
	private SimpleDBView sdbView;
	private Integer sessNum = 1;
	private RPCServer rpcServer;
	private RPCClient rpcClient;
	private String IPAddr = Constant.defaultIPAddr;
	private int svrID = -1;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SessionServer() {
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
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
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
		
		if(curCookie==null){
			SessionID sid  = null;
			synchronized(sessNum){
				sid = new SessionID(svrID, sessNum);
				sessNum++;
			}
			
		}
		else{
			String[] token = curCookie.getValue().split("__");
			int svrID = Integer.parseInt(token[0]);
			String primaryIP = token[2];
			String backupIP = token[3];
			SessionID sid = new SessionID(svrID,sessNum);
					
		}
		
		String action = request.getParameter("function");
		
		Date newDate = new Date();
		removeExpSession(newDate);
		
		
		boolean replace = false;
		
		// check whether current action is replace 
		if(action!=null && action.equals("Replace")){	
			replace =true;	
		}
		
		

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
		
		//check session table to remove the expired sessions
		Date newDate = new Date();
		removeExpSession(newDate);
		
		if(cookies!=null ){
			for(Cookie cookie : cookies){
				if(cookie.getName().equals(Constant.cookieName)){
					curCookie = cookie;
					break;
				}
			}
		}
		
	
		

		
	}

	//remove the expired sessions from my session table
	private static void removeExpSession(Date newDate){

		Iterator<Entry<String, mySession>> it = sessionTable.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, mySession> entry = (Map.Entry<String, mySession>) it.next();
			if(newDate.getTime() > entry.getValue().getExpireTime()){
				it.remove();
			}
		}
	}

	//create the value for the new cookie using session ID and version number 
	private String createCookieValue(mySession session, String primaryIP, String backupIP){
		return session.getSessionID().serialize()+ "__" + session.getVersion() + "__" + primaryIP+"__" + backupIP;
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
	
	public mySession SessionRead(String sessionId) {
		if (!sessionTable.containsKey(sessionId)) {
			return new mySession(new SessionID(-1, 0));
		} else {
			mySession ms = sessionTable.get(sessionId);
			ms.getVersion();
			ms.setExpireTime();
			return ms;
		}
	}
	
	public SessionID SessionWrite(String session) {
		mySession ms = new mySession(session);
		SessionID sid = ms.getSessionID();
		String sessionId = sid.serialize();
		sessionTable.put(sessionId, ms);
		return sid;
	}
}

