import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.io.PrintWriter;
public class Client2 extends Thread 	
{
	static String servlist[];
	static int my_token=0;
	Socket s;
	String name;
	BufferedReader input ;												//reading from server
	PrintWriter sout;													//writing to server
	private Thread t;
	public Client2(String name)											//assigning name for Thread
	{
			servlist=new String[3];										//represents the array of servers
			this.name=name;							
	}
	public Client2(String name,Socket ser)								//assigning name for Thread and having server socket initialized
	{
			s=ser;
			servlist=new String[3];										//represents the array of servers
			this.name=name;							
	}
	public void connection(int port,String servip)						//Establishing connection to the servers for writing file
	{
		try
		{
			int servprt=port;									
			s = new Socket(servip, servprt);
			input =new BufferedReader(new InputStreamReader(s.getInputStream()));
			sout = new PrintWriter(s.getOutputStream(), true);
		}catch(Exception e){System.out.println("Connection Error!!"+e);}
	}
	public void run()
	{
		System.out.println("name:"+this.name);
		if(this.name=="Quoram")															//Checking if the current thread is a Quoram request
		{
			System.out.println("sucess!!1");
			try
			{
				input =new BufferedReader(new InputStreamReader(s.getInputStream()));
				sout = new PrintWriter(s.getOutputStream(), true);
				String clint_req=input.readLine();
				System.out.println("Client Says: "+clint_req);
				if(clint_req.equals("Request"))									//Checking if the input message is a request
				{
					if(my_token==0)														//sending the reply for quoram is zero
					{
						sout.println("Reply");	
						my_token=1;														//token set to 1
					}
				}
				if(clint_req.equals("Release"))									//Checking if the input message is a release
				{
					my_token=0;		//token set to 0
					System.out.println("Token released");
					sout.println("Released");	
				}
			}catch(Exception e){System.out.println("exception : "+e);}			
			
		}
		/*if(this.name=="writer")
		{
			
			try
			{
				String str="";
				connection(9000,serv_list[0]);
				int seq=1;
				String hstname=InetAddress.getLocalHost().getHostAddress();
		    	System.out.println("Client running at"+hstname);
		    		 do
		    		 {
		    			 str="write file1.txt <1,"+seq+","+hstname+"> 1";	//appending to destination file
					 if(!str.equals("Error!!"))
					 {
							sout.println(str);
							String answer = " ";							//input.readLine()
							while(!(answer=input.readLine()).isEmpty())		//checking for error in the server
							{	
								System.out.println(answer);
							}
							if(seq==40)
								sout.println("exit");
					  }
						Thread.sleep(10);
						seq+=1;
		    	 }while(seq<41);
			}catch(Exception e){System.out.println("Error!!"+e);}
		}*/
	}
	public void start()
	{
		System.out.println("Starting Thread: "+name);
		if(t==null)
		{
			t=new Thread(this,name);
			t.start();
		}
	}
	public void serv_list()								//reading the server list
	{		
		File file = new File("server_list.txt");
		System.out.println("File:"+file);
		BufferedReader reader = null;
		int i=0;
		String text="";
		try
		{
		    	reader = new BufferedReader(new FileReader(file));
			while ((text = reader.readLine()) != null)
			 {
				servlist[i]=text;
				System.out.println("servlist["+i+"] = "+servlist[i]);
				i=i+1;
			}
			reader.close();
		}
		catch(Exception e)
		{
			System.out.println("Error!! Problem with Server file!!"+e);
			//System.exit(0);
		}
	}
	public static void main(String[] args) throws IOException
     {
		ServerSocket listener = new ServerSocket(9000);
		String sevme=InetAddress.getLocalHost().getHostAddress();
		System.out.println("Address: " + sevme);
		 while(true)									//starting the thread for client listener.
		 {
			new Client2("Quoram",listener.accept()).start();
		 }		 
       	//s.close();
    	 //System.exit(0);
     }
}