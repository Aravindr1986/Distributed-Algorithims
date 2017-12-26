import java.io.RandomAccessFile;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.io.PrintWriter;
import java.util.Date;
import java.io.Serializable;
class Message implements Serializable
{
		public String ip;
		public String msg;
		public Date ts;
		public String type;
		public int port;
		public Message(String str,String ipstr,String mtype,int mport)
		{
			ip=ipstr;
			msg=str;
			type=mtype;
			ts = new Date();
			port=mport;
		}
		int compare(Message m)
		{
			int ans=0;
			//System.out.println("this.ts = "+this.ts.getTime()+"  m.ts = "+m.ts.getTime());
			if(ts.compareTo(m.ts)<0)	//msg_tm.compareTo(time)<0
			{
				ans=1;
			}
			return ans;
		}
		String show()
		{
			return msg;
		}
}
public class Server extends Thread
{
	Socket socket;
	public Process p;
	ObjectInputStream ins;																						//reading and writing message objects from Quoroms
	ObjectOutputStream os;
	public BufferedReader insoc;
	public BufferedReader brpin;
	public PrintWriter out;
	int pos,lim;							//limit of words to read
	String last_fname,fname;
	String ser[]=new String[3];
	String clnt_list[]=new String[7];
	static int m_cnt=0;
	static int cnt_clnt_req,cnt_serv_req,cnt_agreed,cnt_cmt_serv_req,cnt_cmt_reply,cnt_ack,cnt_release;
	static Message ms=new Message("","","write",9000);
	static int exit_cnt=0;					//for finding the count of writes requested
	int filewrite(String cmd)
	{
		int res=0;
		try
		{
			String l="";
			//exit_cnt+=1;
			RandomAccessFile reader=new RandomAccessFile("file.txt","rw");	//opening in write mode. 0 denotes parsing line
			reader.seek(reader.length());
			l="\n";
			//System.out.println("Message = "+cmd);
			reader.writeBytes(cmd);
			//reader.writeBytes(l);
			res=0;
			reader.close();
		}
		catch(Exception e){System.out.println("Error!!"+e);
				res=1;}
		return(res);		
	}
	
	public int Serv_clint(Message m,String serv)//to contact other servers and get the result
	{
		ObjectInputStream ins1;																						//reading and writing message objects from Quoroms
		ObjectOutputStream os1;
		try
		{
			Socket s = new Socket(serv, 9000);	
	        os1=new ObjectOutputStream(s.getOutputStream());
            ins1=new ObjectInputStream(s.getInputStream());
            os1.writeObject(m);  													//sending the request for commit to other two servers
			m=(Message)ins1.readObject();											//receving the reply			
			//System.out.println("cmd: "+m.type);
			if(m.type.equals("commit")||m.type.equals("done"))
			{	
				s.close();
				return 1;			
			}
			s.close();
		}catch(Exception e){System.out.println("EXCEPTIONS1:"+e);}
		
		return 0;
	}
	Server(Socket s)
	{
		socket = s;
		serv_list();
		clint_list();
	}
	public void clint_list()//reading the server list
	{
		File file = new File("client_list.txt");
		BufferedReader reader = null;
		int i=0;
		String text="";
		try
		{
			reader = new BufferedReader(new FileReader(file));
			while ((text = reader.readLine()) != null)
			 {
				clnt_list[i]=text;
				i=i+1;
			}
		}
		catch(Exception e)
		{
			System.out.println("Error!! Problem with client file!!"+e);
			System.exit(0);
		}
	}
	public void serv_list()//reading the server list
	{
		File file = new File("server_list.txt");
		BufferedReader reader = null;
		int i=0;
		String text="";
		try
		{
			reader = new BufferedReader(new FileReader(file));
			while ((text = reader.readLine()) != null)
			 {
					ser[i]=text;
					i=i+1;
			 }
		}
		catch(Exception e)
		{
			System.out.println("Error!! Problem with Server file!!"+e);
			System.exit(0);
		}
	}
	void release()
	{
		try
		{
			Message m_rel=new Message("Release","","Release",0);
			for(int i=0;i<7;i++)
			{
				Socket s_relse=new Socket(clnt_list[i],9000);
				ObjectOutputStream os1=new ObjectOutputStream(s_relse.getOutputStream());
				os1.writeObject(m_rel);
				cnt_release+=1;
				//s_relse.close();				
			}
		}
		catch(Exception e){}	
		
	}
	public void run()
	{
		String cmd="";
		int s1=0,sme=9000,i;
		String sevme="";
		try
		{
				sevme=InetAddress.getLocalHost().getHostAddress();
				System.out.println(socket.getRemoteSocketAddress().toString());
		}
		
		catch(Exception e){/*System.out.println(e);*/}
		System.out.println("Connected to client!!");
			try
			{
						os=new ObjectOutputStream(socket.getOutputStream());
				 		ins=new ObjectInputStream(socket.getInputStream());
				 		Message m2=(Message)ins.readObject();
						switch(m2.type)
						{
							case "request" :	m2.type="Serv:"+m2.type;
												cnt_clnt_req+=1;														//receiving the request from the client
												s1=0;
												for(i=0;i<3;i++)														//asking opinion of the other clients
												{
												 if(!ser[i].equals(sevme))
												 {
													 cnt_serv_req+=1;
													 s1+=Serv_clint(m2,ser[i]);
													 cnt_agreed+=1;
												 }
												}
												if(s1==2)
												{
													Socket s=new Socket(ser[0],sme);								//sending the commit to central server s[0]	
													os=new ObjectOutputStream(s.getOutputStream());
													m2.type="commit";
													os.writeObject(m2);
													cnt_cmt_serv_req+=1;
													//System.out.println(m2.msg);
													//s.close();
												}
												break;
							case "Serv:request":m2.type="commit";													//replying the request
												os.writeObject(m2);
												break;
							case "commit":	System.out.println("here3 "+m_cnt+" "+ms.type);
												m_cnt+=1;
												ms.msg+="\n"+m2.msg;
												if(m_cnt==7)																//after receving commit for all 7 message
												{
													for(i=0;i<3;i++)														//asking opinion of the other clients
													{
														s1+=Serv_clint(ms,ser[i]);
													}	
												}
												if(s1==3)
												{
													ms.msg="";
													ms.type="write";
													m_cnt=0;
													release();																			//release for all the clients
												}
										  		break;
							case	"write" : filewrite(m2.msg);
											  m2.msg="completed";
											  m2.type="done";
											  os.writeObject(m2);
											  cnt_ack+=7;
											  cnt_cmt_reply+=7;
											  exit_cnt+=7;
											  break;
							default: System.out.println("Real trouble!!");
							
						}
			} 
			catch(Exception e)
			{
				System.out.println("yo:"+e);
			}
			if(exit_cnt==280)		//exit condition
							{
								try
								{
									Thread.sleep(10000);
									System.out.println("\nStatistics\n__________");
									System.out.println("\nClient requests received : "+cnt_clnt_req);
									System.out.println("\nServer requests send : "+cnt_serv_req);
									System.out.println("\nServer Agreeds received : "+cnt_agreed);
									System.out.println("\nCommit request send to main server : "+cnt_cmt_serv_req);
									System.out.println("\nCommit Reply received from main server : "+cnt_cmt_reply);
									System.out.println("\nCommit Ack send to main server : "+cnt_ack);
									System.out.println("\nRelease send to client : "+cnt_release);
									System.out.println("\nTotal Message : "+(cnt_release+cnt_clnt_req+cnt_agreed+cnt_cmt_serv_req+cnt_cmt_reply));
									System.out.println("\nBye!!");
									System.exit(0);
								}catch(Exception e){System.out.println("Stat_excption:"+e);}
							}
		}
    public static void main(String[] args) throws IOException
	 {
		
		ServerSocket listener = new ServerSocket(9000);
		String sevme=InetAddress.getLocalHost().getHostAddress();
		try 
		{
            while (true)
		 	{
				System.out.println("Server is running @ "+ sevme+ " !!");
				try 
				{
					if(Server.exit_cnt!=280)
					{
					 Server s=new Server(listener.accept());//creating new socket to client. initializing the tread.
					 s.start();
					}
				} 
				catch(Exception e)
				{
					System.out.println("Error is here!!");
					System.out.println(e);
				}
			}
        }
       	finally 
       	{
	           	listener.close();
        }
     }
}
