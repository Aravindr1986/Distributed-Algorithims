import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import java.net.Socket;
import java.net.InetAddress;
import java.io.PrintWriter;
import java.util.Scanner;
//Client program running :  sed -i  '1 i Line here!!' trial.txt

public class Client {

	
    
	public void menu()//providing Server menu
	{
		System.out.println("\nCommand Menu\n------- ----\n1)create <Filename>\n2)read <Filename> <no of characters to be read> <append(1/0)>\n3)seek <Filename> <count of locations> \n4)write <Filename> <line to be entered>\n5)delete <Filename>\n6)terminate");
		System.out.println("Enter your command>");	
	}
	public void serv_list(String []s)//reading the server list
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
				s[i]=text;
				i=i+1;
			}
		}
		catch(Exception e)
		{
			System.out.println("Error!! Problem with Server file!!"+e);
			System.exit(0);
		}
		finally {
    				try {
       				 	if (reader != null)
				 	{
         			  		 reader.close();
       					}
   			 	   }
				catch (IOException e) 
				{
    				}
			}
	}
	public String cmd_translate(String str)
	{
		String cmd ="";
		String strbck="";
		if(str.indexOf(' ')==-1)
		{
			cmd=str;
		}
		else
		{
			cmd=str.substring(0,str.indexOf(' '));
		}
		cmd=cmd.toUpperCase();
		switch(cmd)
		{
			case "CREATE":strbck=str.replace("create","touch");
					break;
			case "TERMINATE":strbck=str.replace("terminate","exit");
					break;
			case "DELETE":strbck=str.replace("delete","rm");
					break;	
			case "READ":	strbck =str;
					break;
			case "SEEK":	strbck =str;
					break;
			case "WRITE":	strbck =str;					
					break;	
			
			default : System.out.println("Error in command!! Please reenter!!");
					strbck="Error!!";
 
		}
		return strbck;			
	}
     public static void main(String[] args) throws IOException
     {
	Client c=new Client();
	Scanner inp = new Scanner(System.in);
	String str="";
	String servip="";
        int servprt=9000;
	String  servlist[]=new String[3]; //for server list
	System.out.println(InetAddress.getLocalHost().getHostAddress());
	c.serv_list(servlist);     	
	System.out.println("Server Menu\n------ ----\n1)S1: "+servlist[0]+"\n2)s2: "+servlist[1]+"\n3)S3: "+servlist[2]+"\nEnter your Choice:");
	String ch=inp.nextLine();
	if(ch.equals("1"))
		servip=servlist[0];
	if(ch.equals("2"))
		servip=servlist[1];
	if(ch.equals("3"))
		servip=servlist[2];
	System.out.println("Server selected : "+servip);
	Socket s = new Socket(servip, servprt);	
	do
	{
		c.menu();
		str=inp.nextLine();
		str=c.cmd_translate(str);
		if(!str.equals("Error!!"))
		{
			BufferedReader input =new BufferedReader(new InputStreamReader(s.getInputStream()));//reading from server
			PrintWriter sout = new PrintWriter(s.getOutputStream(), true);//writing to server
			sout.println(str);
			if(str.equals("exit"))
				break;
			String answer = " ";//input.readLine()
			while(!(answer=input.readLine()).isEmpty())
			{	
        			System.out.println(answer);
			}
		}
	}while(!str.equals("exit"));
	System.out.println("Bye!!");
       	//s.close();
	System.exit(0);
    }
}