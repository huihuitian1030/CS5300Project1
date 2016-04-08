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
	private int sessNum = 1;
	private RPCServer rpcServer;
	private RPCClient rpcClient;
	private String IPAddr = Constant.defaultIPAddr;
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
		String action = request.getParameter("function");
		
		Date newDate = new Date();
		removeExpSession(newDate);
		
		
		boolean replace = false;
		
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
		
		//update a existng session
//		if(curCookie!=null && sessionTable.containsKey(getSid(curCookie))){
//			String sid = getSid(curCookie);
//			int version = getV(curCookie);
//			version++;
//			
//			curCookie.setValue(createCookieValue(sid,version));
//			curCookie.setMaxAge(30*ONE_SECOND_IN_MILLIS);
//			mySession session = sessionTable.get(sid);
//			if(replace){
//				String msg = request.getParameter("newStr");
//				session.setMessage(msg);
//			}
//			session.addVersion();
//			session.setExpireTime(new Date(newDate.getTime()+30*ONE_SECOND_IN_MILLIS));
//			sessionTable.put(sid, session);
//			request.setAttribute("mySession", session);
//		}
			
		//Create a new session - first time or current session is timed-out
//		else{
//			curCookie = new Cookie(cookieName, createCookieValue(sessionID,0));
//			curCookie.setMaxAge(30*ONE_SECOND_IN_MILLIS);
//			mySession session = new mySession(sessionID,0,new Date(newDate.getTime()+30*ONE_SECOND_IN_MILLIS));	
//			session.setMessage(welcomeMsg);
//			sessionTable.put(sessionID, session);
//			sessionID = UUID.randomUUID().toString();
//			request.setAttribute("mySession", session);		
//		}

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
		
		//remove the session after log out (if that session stil exists)
//		if(curCookie!=null && sessionTable.containsKey(getSid(curCookie))){
//			String sid = getSid(curCookie);
//			int version = getV(curCookie);
//			version++;
//	
//			sessionTable.remove(sid);
//			curCookie.setValue(createCookieValue(sid,version));
//			curCookie.setMaxAge(30*ONE_SECOND_IN_MILLIS);
//			response.addCookie(curCookie);
//				
//		}
		
		request.getRequestDispatcher("logout.jsp").forward(request, response);

		
	}

	//remove the expired sessions from my session table
	private static void removeExpSession(Date newDate){

		Iterator<Entry<String, mySession>> it = sessionTable.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, mySession> entry = (Map.Entry<String, mySession>) it.next();
			if(newDate.getTime() > entry.getValue().getExpireTime().getTime()){
				it.remove();
			}
		}
	}

	//create the value for the new cookie using session ID and version number 
	private String createCookieValue(String sid, int version){
		return sid+"__"+version+"__0_0";
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
	
	
	
}

