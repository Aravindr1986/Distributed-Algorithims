import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Random;
import java.util.ArrayList;
import java.io.Serializable;
class Message implements Serializable
{
		public String ip;
		public String msg;
		public Date ts;
		public Message(String str,String ipstr)
		{
			ip=ipstr;
			msg=str;
			ts = new Date();
		}
		int compare(Message m)
		{
			int ans=0;
			if(ts.compareTo(m.ts)<0)	//msg_tm.compareTo(time)<0
				ans=1;
			return ans;
		}
		String show()
		{
			return msg;
		}
}
public class Client extends Thread 	
{
	static String servlist[];											//The list of servers
	static int qlength,Qurm_len;										//for lenght of my quorom and the quoroms that i am involved in
	public static int client_list[];									//defining my Quorum
	public static int exit_cnd=0;
	static Message lst_msg;
	static int last_req;												//defining the last request for which reply has been send
	static String time;													//timestamp of the last reply for setting priority
	Socket s;
	static int cnt_reqst,cnt_reply,cnt_relse,cnt_inquire,cnt_faild,cnt_yeild;		//gathering statistics
	static int exit_cnt;
	String name;
	BufferedReader Qinput ;												//reading from server
	PrintWriter Qsout;													//writing to server
	BufferedReader input ;												//reading from server
	PrintWriter sout;													//writing to server
	private Thread t;
	static int my_token=0;
	public Client(String name)											//assigning name for Thread
	{
			exit_cnt=0;
			servlist=new String[3];										//represents the array of servers
			client_list=new int[4];									//represents the array of clients
			this.name=name;			
			//serv_list();												//parsing all the server ip list
			qlength=client_list();
			cnt_reqst=0;
			cnt_reply=0;
			cnt_relse=0;
			cnt_inquire=0;
			cnt_faild=0;
			cnt_yeild=0;
	}
	public Client(String name,Socket ser)								//assigning name for Thread and having server socket initialized
	{
			s=ser;
			this.name=name;							
	}
	public int client_list()//reading the server list
	{		
		File file = new File("Q_list.txt");
		BufferedReader reader = null;
		int i=0;
		String text="";
		try
		{
		    reader = new BufferedReader(new FileReader(file));
			Qurm_len=Integer.parseInt(reader.readLine());				//Reading in the count of quorom this client is a part of
			System.out.println("Qurm_len : "+Qurm_len);
			while ((text = reader.readLine()) != null)
			 {
				client_list[i]=Integer.parseInt(text);
				System.out.println("Quorum["+i+"] = "+client_list[i]);
				i=i+1;
			}
			reader.close();
		}
		catch(Exception e)
		{
			System.out.println("Error!! Problem with Server file!!"+e);
		}
		return(i);
	}
	public void connection(int port,String servip)						//Establishing connection
	{
		try
		{
			int servprt=port;
			//System.out.println("Serv_ip:"+servip);
			s = new Socket(servip, servprt);
			input =new BufferedReader(new InputStreamReader(s.getInputStream()));
			sout = new PrintWriter(s.getOutputStream(), true);
		}catch(Exception e){System.out.println("Connection Error!!"+e);}
	}
	String Quorom(String str)
	{
		String answer="";
		try
		{
			sout.println(str);
			answer=input.readLine();
		}catch(Exception e){System.out.println("E"+e);}
		return answer;
	}
	public String create_msg(String msg)
	{
		try
		{
				Date d=new Date();
				msg=d.getTime()+":"+msg;
				msg=client_list[0]+":"+msg;
				
		}catch(Exception e){}
		return(msg);
	}
	String parse_Msg(String str)
	{
		int i1=str.indexOf(":");
		int i2=str.indexOf(":",(i1+1));
		int i3=str.indexOf(":",(i2+1));
		last_req=Integer.parseInt(str.substring(0,i1));
		time=str.substring(i1+1,i2);
		return(str.substring(i2+1));
	}
	static int getRandomNumberInRange()													//generating random wait times 
	{
		Random r = new Random();
		int x = r.nextInt(51) ;
		System.out.println("Random value : "+x);
		return(x);
	}
	int check_fail()
	{
		int res=0;
		for(int i=0;i<qlength;i++)
		{
			if(qu[i].equals("Failed"))
				x=1;
		}
		return x;
	}
	public void run()
	{
		int seq=1,i=0;
		if(this.name=="Quoram")															//Checking if the current thread is a Quoram request
		{
			try
			{
				Qinput =new BufferedReader(new InputStreamReader(s.getInputStream()));
				Qsout = new PrintWriter(s.getOutputStream(), true);
				String clint_req=Qinput.readLine();
				
				if(clint_req.indexOf("Request")>0)										//Checking if the input message is a request
				{
					int i1=clint_req.indexOf(":");
					int i2=clint_req.indexOf(":",(i1+1));
					int i3=clint_req.indexOf(":",(i2+1));
					int ip=Integer.parseInt(clint_req.substring(0,i1));					//extracting the message ip
					String msg_tm=clint_req.substring(i1+1,i2);							//extracting the message time
					if(my_token==0)														//sending the reply for quoram is zero
					{
						last_req=ip;
						time=msg_tm;
						Qsout.println("Reply");
						cnt_reply+=1;
						my_token=1;														//token set to 1
					}
					else
					{	
						if(msg_tm.compareTo(time)<0)									//checking for yeild condition if the msg time is less.
						{
							Socket s2=new Socket("127.0.0.1",last_req);					//connecting to the input of the last reply send machine
							BufferedReader Qinput1 =new BufferedReader(new InputStreamReader(s.getInputStream()));
							PrintWriter Qsout1 = new PrintWriter(s.getOutputStream(), true);
							Qsout1.println("Inquire");
							cnt_inquire+=1;
							if((Qinput1.readLine()).equals("Yield"))
							{
								last_req=ip;
								time=msg_tm;
								Qsout.println("Reply");
								my_token=1;
							}
							else														//failing if yeild fails
							{
								Qsout.println("Failed");
								cnt_faild+=1;
							}
						}																//failing if compare fails
						else														
						{
							Qsout.println("Failed");
							cnt_faild+=1;
						}
					}
				}
				if(clint_req.equals("Inquire"))
				{
					if(check_fail())
					{
						Qsout.println("Reply");
					}
					//Qsout.println("Failed");
				}
				if(clint_req.equals("Release"))									//Checking if the input message is a release
				{
					my_token=0;													//token set to 0
					Qsout.println("Released");	
				}
				if(clint_req.indexOf("Done")>0)
				{
					System.out.println("Client_req:"+clint_req);
					Qsout.println("ok");	
					exit_cnt+=1;
					if(exit_cnt==Qurm_len)
					{
						System.out.println("Sequenceing complete!!\n Statics\n-------\n");
						System.out.println("Requets :"+cnt_reqst+"\nReplys : "+cnt_reply+"\nReleases :"+cnt_relse);
						System.out.println("Inquire :"+cnt_inquire+"\nFailed : "+cnt_faild+"\nYeild :"+cnt_yeild);
						exit_cnd+=1;
						if(exit_cnd==2)											//Exit Condition
						{
							System.out.println("bye!!");
							getRandomNumberInRange();
							System.exit(0);
						}
					}
					
				}
			}catch(Exception e){System.out.println("exception : "+e);}			
		}
		if(this.name=="writer")														//For writer thread
		{
			try
			{
				String hstname=InetAddress.getLocalHost().getHostAddress();
				System.out.println("Client running at "+hstname);
				do
				{
					String str="";
					String qu[]=new String[4];
					i=0;
					do
					{
						connection(/*9000*/client_list[i],"127.0.0.1"/*client_list[i]*/);								//Connecting to connecting node for requesting token
						String msg=create_msg("Request");
						cnt_reqst+=1;
						qu[i]=Quorom(msg);
						//System.out.println("Reply for Qorom :  "+client_list[i]+": "+qu[i]);
						if(qu[i].equals("Failed")||qu[i].equals(""))
							break;
						i++;
						s.close();
					}while(i<qlength);
					if(i==qlength)														//if atleast 1 node replied as negative then i will be less than 3
					{
						//connection(9000,servlist[0]);									//connecting to server for writing to file
						str="write file1.txt <1,"+seq+","+hstname+" : "+client_list[0]+"> 1";				//appending to destination file
						seq+=1;
						if(!str.equals("Error!!"))
						{
							Date d3=new Date();
							System.out.println(str+" at "+d3.getTime());
							/*	System.out.println("connected!!");
								sout.println(str);
								String answer = " ";									//input.readLine()
								while(!(answer=input.readLine()).isEmpty())				//checking for error in the server
								{	
									System.out.println(answer);
									seq+=1;
								}*/
						}
						Thread.sleep(getRandomNumberInRange());							
					}
					//System.out.println("Relase Cycle!!\n------ -----");
					i=0;
					do
					{
						connection(/*9000*/client_list[i],"127.0.0.1"/*client_list[i]*/);								//Connecting to connecting node for Releasing token
						qu[i]=Quorom("Release");
						cnt_relse+=1;
						i++;
					}while(i<qlength);
				}while(seq<41);
				try
				{
					i=0;
					do
					{
						connection(/*9000*/client_list[i],"127.0.0.1"/*client_list[i]*/);								//Connecting to connecting node for Releasing token
						System.out.println("Reply for Done "+client_list[i]+" :"+Quorom(client_list[0]+":Done"));
						i++;
					}while(i<qlength);
					exit_cnd+=1;
					if(exit_cnd==2)											//Exit Condition
					{
						System.out.println("bye!!");
						getRandomNumberInRange();
						System.exit(0);
					}
				}catch(Exception e){System.out.println("How:"+e);}
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
	public void serv_list()//reading the server list
	{		
		File file = new File("server_list.txt");
		System.out.println("Parsing file :"+file);
		BufferedReader reader = null;
		int i=0;
		String text="";
		try
		{
		    reader = new BufferedReader(new FileReader(file));
			while ((text = reader.readLine()) != null)
			 {
				servlist[i]=text;
				i=i+1;
			}
			reader.close();
		}
		catch(Exception e)
		{
			System.out.println("Error!! Problem with Server file!!"+e);
		}
	}
	public static void main(String[] args) throws IOException
     {
		 int flip=0;
		Client c=new Client("writer");
		String sevme=InetAddress.getLocalHost().getHostAddress();
		System.out.println("Client @ " + sevme+" : "+c.client_list[0]+"\n----------------");
		
		ServerSocket listener = new ServerSocket(c.client_list[0]);
		while(true)																	//starting the thread for client listener.
		{
			new Client("Quoram",listener.accept()).start();
			if(flip==0)																//for starting the client
			{
				c.start();
				flip=1;
			}
		}
	 }
}