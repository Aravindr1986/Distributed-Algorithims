import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.io.PrintWriter;
public class Start_code  	
{
	static String servlist[];
	static int qlength;
	static String client_list[];
	Socket s;
	String name;
	BufferedReader Qinput ;												//reading from server
	PrintWriter Qsout;													//writing to server
	BufferedReader input ;												//reading from server
	PrintWriter sout;													//writing to server
	private Thread t;
	static int my_token=0;
	public void connection(int port,String servip)						//Establishing connection
	{
		try
		{
			int servprt=port;
			System.out.println("Serv_ip:"+servip);
			s = new Socket(servip, servprt);
			input =new BufferedReader(new InputStreamReader(s.getInputStream()));
			sout = new PrintWriter(s.getOutputStream(), true);
		}catch(Exception e){System.out.println("Connection Error!!"+e);}
	}
	public static void main(String[] args) throws IOException
     {
		File file = new File("clients_list.txt");
		System.out.println("File:"+file);
		BufferedReader reader = null;
		Start_code st = new Start_code();
		int i=0,sevport=9000;
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
				st.sout.println("hi!!");
				i++;
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