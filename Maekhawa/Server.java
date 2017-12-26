import java.io.IOException;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileReader;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread
{
	Socket socket;
	public Process p;
	public BufferedReader insoc;
	public BufferedReader brpin;
	public PrintWriter out;
	int pos,lim;							//limit of words to read
	String last_fname,fname;
	String line;	
	static int exit_cnt=0;					//for finding the count of writes requested
	void fname(String cmd)
	{
		char c=' ';
		int ind1=cmd.indexOf(c)+1;
		fname=cmd.substring(ind1);
	}
	RandomAccessFile parseFilename(String cmd,String m,int k)         //for parsing the commands of Seek and Read files.
	{
		try{
			char c=' ';
			int ind1=cmd.indexOf(c)+1;
			int ind2=cmd.indexOf(c,ind1);
			fname=cmd.substring(ind1,ind2);
			line=cmd.substring(ind2+1);//reading the line
			if(k==1)                                           //taking care of read funtion. In read the line contains limit
				lim=Integer.parseInt(line);
			RandomAccessFile reader = new RandomAccessFile(fname,m);
			return(reader);
		}
		catch(Exception e){return(null);}
	}
	int filewrite(String cmd)
	{
		int res=0;
		try
		{
			String text="",l="";
			exit_cnt+=1;
			RandomAccessFile reader=parseFilename(cmd,"rw",0);	//opening in write mode. 0 denotes parsing line
			if(cmd.charAt(cmd.length()-1)=='1')			//checking for append
			{
				reader.seek(reader.length());
				l="\n";
			}
			line=line.substring(0,line.length()-2)	;		//for removing the append option
			reader.writeBytes(line);
			reader.writeBytes(l);
			res=0;
		}
		catch(Exception e){System.out.println("Error!!"+e);
				res=1;}
		return(res);		
	}
	public int executeCmd(String cmd)//for executing Create and Delete.
	{
		System.out.println("Command:"+cmd);
		int res=1;
		try
		{
			out =new PrintWriter(socket.getOutputStream(), true);	 //writing to client
			switch(cmd.substring(0,4))
			{
				
				case "writ" :res=filewrite(cmd);
					    break;
				default :p = Runtime.getRuntime().exec(cmd);	//executing the command
					brpin = new BufferedReader(new InputStreamReader(p.getInputStream())); //getting output of the execution
					pos=0;	
					last_fname="";
					res=p.waitFor();
			}	
		}
		catch(Exception e){System.out.println("Here is the error:"+e);}				//executing the command
		return(res);
	}
	public int Serv_clint(String cmd,String serv)//to contact other servers and get the result
	{
		BufferedReader input =null;
		PrintWriter sout = null;
		try
		{
			Socket s = new Socket(serv, 9000);	
			input =new BufferedReader(new InputStreamReader(s.getInputStream()));//reading from server
			sout = new PrintWriter(s.getOutputStream(), true);//writing to server
			sout.println(cmd);
			System.out.println("cmd: "+cmd);
			String answer=input.readLine();
			if(answer.equals("Command successfull"))
				return 1;			
		}catch(Exception e){System.out.println("EXCEPTIONS1:"+e);}
		return 0;
	}
	Server(Socket s)
	{
		socket = s;
	}
	public void serv_list(String []s,String sme)//reading the server list
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
				if(!text.equals(sme))
				{
					s[i]=text;
					i=i+1;
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Error!! Problem with Server file!!"+e);
			System.exit(0);
		}
	}
	public void run()
	{
		String cmd="";
		int s1=1,s3=1,cres,sme=9000;
		String sevme="";
		try{
				sevme=InetAddress.getLocalHost().getHostAddress();
		   }
		catch(Exception e){System.out.println(e);}
		String ser[]=new String[2];
		serv_list(ser,sevme);
		System.out.println("Connected to client!!");
		while(!cmd.equals("exit"))
		{
			try
			{
						insoc =new BufferedReader(new InputStreamReader(socket.getInputStream()));
						String s="";
						cmd=insoc.readLine();			//for command
						System.out.println("Client says "+cmd);
						fname(cmd);
						if(!cmd.contains("Serv:"))		//for the current server
						{
							cres=executeCmd(cmd);		//Calling the Execute function for native linux commands	
							if(cres!=0)
							throw new IOException();
							cmd="Serv:"+cmd;
							s1=Serv_clint(cmd,ser[0]);
							s3=Serv_clint(cmd,ser[1]);
							System.out.println("Result Server 1:"+s1);
							System.out.println("Result Server 3:"+s3);
						}
						else					// for other servers
						{
							cmd=cmd.substring(cmd.indexOf("Serv:")+5);
							cres=executeCmd(cmd);		//Calling the Execute function for native linux commands
							if(cres!=0)
							throw new IOException();
							out.println("Command successfull");
							break;
						}
						if(s1==1 && s3==1)
						{
							out.println("Command successfull");
						}
						else
						{
							throw new IOException();
						}
						if(exit_cnt==280/*cmd.equals("exit")*/)		//exit condition
						{
							/*s1=Serv_clint(("Serv:"+cmd),ser[0]);
							s3=Serv_clint(("Serv:"+cmd),ser[1]);*/
							System.out.println("Bye!!");
							System.exit(0);
						}
						/*if(cmd.equals("Serv:exit"))
						{
							System.out.println("Bye!!");
							System.exit(0);
						}*/
						out.println("\n");
			} 
			catch(Exception e)
			{
				System.out.println(e);
				out.println("Command Unsuccessful!!");
				out.println("\n");
			}
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
					 new Server(listener.accept()).start();//creating new socket to client. initializing the tread.
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