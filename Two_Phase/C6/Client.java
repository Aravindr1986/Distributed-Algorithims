import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Random;
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
				//System.out.println("here");
				ans=1;
			}
			return ans;
		}
		String show()
		{
			return msg;
		}
}
public class Client extends Thread 	
{
	static int client_id;
	static String servlist[];																					//The list of servers
	static int exit_cnd=0,exit_cnt=0;									
	Socket s;
	static int cnt_reqst,cnt_reply,cnt_relse;									//statistic collectors
	String name;																								//name of the thread
	ObjectInputStream ois;																						//reading and writing message objects from Quoroms
	ObjectOutputStream os;
	BufferedReader sin ;																						//reading and writing from servers 
	PrintWriter sout;	
	private Thread t;
	int Lflag;
	public Client(String name)																					//assigning name for Thread
	{
			exit_cnt=0;
			servlist=new String[3];																				//represents the array of servers
			this.name=name;		
			Lflag=0;
			//serv_list();
			servlist=file_list("server_list.txt",servlist);
			cnt_reqst=0;
			cnt_relse=0;
	}
	public Client(String name,Socket ser)																		//assigning name for Thread and having server socket initialized
	{
			s=ser;
			this.name=name;							
	}
	public String[] file_list(String fname,String str[])//reading the server list
	{		
		File file = new File(fname);
		BufferedReader reader = null;
		int i=0;
		String text="";
		try
		{
		    reader = new BufferedReader(new FileReader(file));
			client_id=Integer.parseInt(reader.readLine());
			System.out.println("Client : "+client_id+" Running");
			while ((text = reader.readLine()) != null)
			{
				str[i]=text;
				System.out.println("S["+i+"]:"+str[i]);
				i=i+1;
			}
			
			reader.close();
		}
		catch(Exception e)
		{
			System.out.println("Error!! Problem with file!!"+e);
		}
		return(str);
	}
	public void connection(int port,String servip)																//Establishing connection
	{
		try
		{
			int servprt=port;
			s = new Socket(servip, servprt);
		}catch(Exception e){System.out.println("Connection Error!!"+e);}
	}
	static int getRandomNumberInRange()																			//generating random wait times 
	{
		Random r = new Random();
		int x = r.nextInt(51) ;
		//System.out.println("Random value : "+x);
		return(x);
	}
	public void run()
	{
		int seq=1;
		Date d3=new Date();
		if(this.name.equals("writer"))
		{
			Message m;																							//defining message to send to server
			try
			{
				String str;
				String hstname=InetAddress.getLocalHost().getHostAddress();
				System.out.println("Client running at "+hstname+" port =9000");
				do
				{		
					if(Lflag==1)
					{
						int servindex=0;
						while(servindex==0)																		//code for randomly choosing the server to connect
						{
							servindex=getRandomNumberInRange() %3;
						}
						System.out.println("Connection to server s["+servindex+"]:"+servlist[servindex]);
						connection(9000,servlist[servindex]);													//connecting to server for writing to file
						os=new ObjectOutputStream(s.getOutputStream());
						//ois.flush();
						str="write file1.txt <"+client_id+","+seq+","+hstname+">";						//appending to destination file
						m=new Message(str,hstname,"request",9000);												//generating message to send requst(write)
						d3=new Date();
						System.out.println(str+" at "+d3.getTime());
						System.out.println("connected!!");
						os.writeObject(m);
						cnt_reqst+=1;
					}	
					
					ServerSocket listener = new ServerSocket(9000);
					s=listener.accept();
					ois=new ObjectInputStream(s.getInputStream());;
					m=(Message)ois.readObject();									//reading reply from the server
					switch(m.msg)
					{
						case "Start":	Lflag=1;
										System.out.println("Starting the write execution");
										break;
						case "Release" : seq+=1;
										System.out.println("Round "+seq+" time taken ="+((new Date().getTime())-d3.getTime()));
										cnt_relse+=1;
										break;
											
					}
					Thread.sleep(getRandomNumberInRange());							
					listener.close();
				}while(seq<41);
				System.out.println("Statistics\n__________\nRequest = "+cnt_reqst+"\nRelease = "+cnt_relse+"\nBye!!");
			}catch(Exception e){System.out.println("Error!!"+e);}	
		}
	}
	public void start()
	{
		if(t==null)
		{
			t=new Thread(this,name);
			t.start();
		}
	}
	public static void main(String[] args) throws IOException
     {

		Client c=new Client("writer");
		c.start();
	 }
}