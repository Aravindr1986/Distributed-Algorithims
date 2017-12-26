import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.net.Socket;
import java.util.Date;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.io.PrintWriter;
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
public class Start_code  	
{
	static String servlist[];
	static String client_list[];
	Socket s;
	String name;																						//reading and writing message objects from Quoroms
	public ObjectOutputStream os;
	private Thread t;
	public void connection(int port,String servip)						//Establishing connection
	{
		try
		{
			int servprt=port;
			System.out.println("Serv_ip:"+servip);
			s = new Socket(servip, servprt);
			os=new ObjectOutputStream(s.getOutputStream()); 
		}catch(Exception e){System.out.println("Connection Error!!"+e);}
	}
	public static void main(String[] args) throws IOException
     {
		File file = new File("client_list.txt");
		System.out.println("File:"+file);
		BufferedReader reader = null;
		Start_code st = new Start_code();
		int i=0;
		Message m=new Message("Start","","Start",9000);
		String text="";
		String client="";
		try
		{
		    reader = new BufferedReader(new FileReader(file));
			while ((text = reader.readLine()) != null)
			{
				System.out.println("Starting Client "+(i+1)+" : "+text);
				client=text;
				st.connection(9000,client);
				st.os.writeObject(m);
			}
			reader.close();
		}
		catch(Exception e)
		{
			System.out.println("Error!! Problem with Server file!!"+e);
		}	
    	
		// System.out.println("Bye!!");
     }
}