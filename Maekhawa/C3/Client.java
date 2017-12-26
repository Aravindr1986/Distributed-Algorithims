import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
	static int myqlength,Qurm_len;																				//for lenght of my quorom and the quoroms that i am involved in
	static String client_list[];																					//defining Quorum
	static int exit_cnd=0,exit_cnt=0;									
	static Message lst_msg;																						//defines the last message for which the token has been granted
	static ArrayList<Message> wait_queue;																		//defining the wait queue
	Socket s;
	static int cnt_reqst,cnt_reply,cnt_relse,cnt_inquire,cnt_failed,cnt_yeild;									//statistic collectors
	String name;																								//name of the thread
	ObjectInputStream ois;																						//reading and writing message objects from Quoroms
	ObjectOutputStream os;
	BufferedReader sin ;																						//reading and writing from servers 
	PrintWriter sout;	
	private Thread t;
	static int my_token=0;
	static String qu[];
	int Lflag;
	public Client(String name)																					//assigning name for Thread
	{
			exit_cnt=0;
			servlist=new String[3];																				//represents the array of servers
			client_list=new String[4];
			qu=new String[4];																					//represents the array of clients
			this.name=name;		
			Lflag=0;
			//serv_list();
			client_list=file_list("Q_list.txt",client_list);														//parsing all the server ip list
			System.out.println("Quorom size:"+myqlength);
			servlist=file_list("server_list.txt",servlist);
			cnt_reqst=0;
			cnt_reply=0;
			cnt_relse=0;
			cnt_inquire=0;
			cnt_failed=0;
			cnt_yeild=0;
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
			if(fname.equals("Q_list.txt"))																		//Reading in the count of quorom this client is a part of
			{
				client_id=Integer.parseInt(reader.readLine());
				Qurm_len=Integer.parseInt(reader.readLine());
				
			}
			
			while ((text = reader.readLine()) != null)
			{
				System.out.println("S["+i+"]:"+str[i]);
				str[i]=text;
				i=i+1;
			}
			if(fname.equals("Q_list.txt"))																		//Reading in the count of quorom this client is a part of
			myqlength=i;
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
		int seq=1,i=0;
		if(this.name.equals("Quorum"))
		{
			try
			{
				os=new ObjectOutputStream(s.getOutputStream());
				//ois.flush();
				ois=new ObjectInputStream(s.getInputStream());
				Message cm=(Message)ois.readObject();
				Message newMsg;
				//System.out.println("Current Message:"+cm.msg);
				switch(cm.msg)
				{
					case "Request":if(my_token==0)																//sending the reply for quoram is zero
									{
										my_token=1;
										lst_msg=cm;
										newMsg=new Message("Reply",client_list[0]);
										os.writeObject(newMsg);
										cnt_reply+=1;																										//token set to 1
									}
									else
									{
										if(lst_msg.compare(cm)==0)		//code for Queue goes here								//if the last sucessful request is having a greater timestamp than the current request
										{
											Socket s2=new Socket(lst_msg.ip,9000);
											ObjectOutputStream os_1=new ObjectOutputStream(s2.getOutputStream());
											ObjectInputStream ois_1=new ObjectInputStream(s2.getInputStream());
											Message inq_msg=new Message("Inquire",client_list[0]);
											cnt_inquire+=1;
											os.writeObject(inq_msg);
											Message rp_msg=(Message)ois_1.readObject();
											if(rp_msg.equals("Yield"))
											{
												lst_msg=cm;
												newMsg=new Message("Reply",client_list[0]);
												os.writeObject(newMsg);
												my_token=1;
											}
											else																				//failing if yeild fails
											{				
												newMsg=new Message("Failed",client_list[0]);
												os.writeObject(newMsg);
												cnt_failed+=1;
											}
										}
										else																					//failing if token is already passed to correct messages
										{	
											newMsg=new Message("Failed",client_list[0]);
											os.writeObject(newMsg);
											cnt_failed+=1;
										}
									}
									break;
					case "Inquire":if(Lflag==1)
									{
										cnt_yeild+=1;
									}
									newMsg=new Message("Failed",client_list[0]);
								   cnt_failed+=1;
								   os.writeObject(newMsg);
								   break;
					case "Release":newMsg=new Message("Released",client_list[0]);
								   os.writeObject(newMsg);
								   my_token=0;	
								   break;
					case "Done":newMsg=new Message("OK",client_list[0]);
								os.writeObject(newMsg);
								exit_cnt+=1;
								System.out.println("Done from : "+cm.ip+ " Exit_cnt="+exit_cnt+" exit_cnd: "+exit_cnd);
								System.out.println("Qurm_len:"+Qurm_len);
								if(exit_cnt==Qurm_len)
								{
									System.out.println("Sequenceing complete!!\n Statics\n-------\n");
									System.out.println("Requets :"+cnt_reqst+"\nReplys : "+cnt_reply+"\nReleases :"+cnt_relse);
									System.out.println("Inquire :"+cnt_inquire+"\nFailed : "+cnt_failed+"\nYeild :"+cnt_yeild);
									exit_cnd+=1;
									System.out.println("exit_cnd1:"+exit_cnd);
									if(exit_cnd==2)																				//Exit Condition
									{
										System.out.println("bye!!");
										System.exit(0);
									}
								}
								break;
				}
				
			}catch(Exception e){System.out.println("exception : "+e);}	
		}
		if(this.name.equals("writer"))
		{
			try
			{
				String str;
				String hstname=InetAddress.getLocalHost().getHostAddress();
				do
				{
					//String qu[]=new String[4];	
					try
					{
					i=0;
					do
					{
						connection(9000,client_list[i]);								//Connecting to connecting node for requesting token
						os=new ObjectOutputStream(s.getOutputStream());
						ois=new ObjectInputStream(s.getInputStream());
						Message msg=new Message("Request",client_list[0]);
						cnt_reqst+=1;
						qu[i]=Quorom(msg);
						if(qu[i].equals("Failed")||qu[i].equals(""))
						{
							Lflag=1;
								break;
						}
						i++;
						s.close();
					}while(i<myqlength);
					if(i==myqlength)														//if atleast 1 node replied as negative then i will be less than 3
					{
						connection(9000,servlist[0]);									//connecting to server for writing to file
						sin =new BufferedReader(new InputStreamReader(s.getInputStream()));
						sout = new PrintWriter(s.getOutputStream(), true);
						str="write file1.txt <"+client_id+","+seq+","+hstname+" : "+client_list[0]+"> 1";				//appending to destination file
						//System.out.println(str);
						Date d3=new Date();
						System.out.println(str+" at "+d3.getTime());
						System.out.println("connected!!");
						sout.println(str);
						String answer = " ";									//input.readLine()
						while(!(answer=sin.readLine()).isEmpty())				//checking for error in the server
						{	
							System.out.println(answer);
							seq+=1;
						}
						Lflag=0;
						Thread.sleep(getRandomNumberInRange());							
					}
					//System.out.println("Relase Cycle!!\n------ -----");
					i=0;
					do
					{
						connection(9000,client_list[i]);								//Connecting to connecting node for Releasing token
						os=new ObjectOutputStream(s.getOutputStream());
						ois=new ObjectInputStream(s.getInputStream());
						Message msg=new Message("Release",""+client_list[0]);
						qu[i]=Quorom(msg);
						if(Lflag==0)
							cnt_relse+=1;
						i++;
					}
					while(i<myqlength);
					}catch(Exception e){}
				}while(seq<41);
				try
				{
					i=0;
					do
					{
						connection(9000,client_list[i]);								//Connecting to connecting node for Releasing token
						os=new ObjectOutputStream(s.getOutputStream());
						ois=new ObjectInputStream(s.getInputStream());
						Message msg=new Message("Done",""+client_list[0]);
						System.out.println("Reply for Done "+client_list[i]+" :"+Quorom(msg));
						
						i++;
					}while(i<myqlength);
					//Thread.sleep(10000);
					exit_cnd+=1;
					System.out.println("exit_cnd22:"+exit_cnd);
					if(exit_cnd==2)											//Exit Condition
					{
						System.out.println("bye!!");
						System.exit(0);
					}
				}catch(Exception e){System.out.println("How:"+e);}
			
			}catch(Exception e){System.out.println("Error!!"+e);}	
		}
	}
	String Quorom(Message m)
	{
		String answer="";
		try
		{
			os.writeObject(m);
			Message rp_msg=(Message)ois.readObject();
			answer=rp_msg.msg;
		}catch(Exception e){System.out.println("E"+e);}
		return answer;
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
		 int flip=0;
		Client c=new Client("writer");
		String sevme=InetAddress.getLocalHost().getHostAddress();
		System.out.println("Client @ " + sevme+" : "+c.client_list[0]+"\n----------------");
		ServerSocket listener = new ServerSocket(9000);
		while(true)																	//starting the thread for client listener.
		{
			new Client("Quorum",listener.accept()).start();
			if(flip==0)																//for starting the client
			{
				c.start();
				flip=1;
			}
		}
	 }
}