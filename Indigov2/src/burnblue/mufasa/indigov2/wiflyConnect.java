package burnblue.mufasa.indigov2;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.net.telnet.TelnetClient;



public class wiflyConnect 
{
	// instance variables
	private String server = "169.254.1.1";	
	private int port = 20000;
	private TelnetClient telnet = new TelnetClient();
	private InputStream in;
	private PrintStream out;
	
	private StringBuffer sb, sb1, sb2;
	private String command;
	
	int len = 0;
	int len1 = 0;

	boolean l_save = false;
	
	public void sendCommand( String i_str )
	{
		command = i_str;
		
		try 
		{	
					
			try 
			{
				telnet.setConnectTimeout(1000);
				telnet.connect(server, port);
			}
			catch ( SocketException e )
			{
				e.printStackTrace();
			}
			
			in = telnet.getInputStream();			
			out = new PrintStream(telnet.getOutputStream());
			
			Thread mThread = new Thread(new Runnable() 
			{
				public void run() 
				{
					try 
					{
						sb = new StringBuffer();						
						do 
						{
							do  
							{
								len = in.read();
								String s = Character.toString((char)len);								
								sb.append( s );								
																								
							} while ( !sb.toString().equals("*HELLO*") );
							
							//command = new StringBuffer("get ip a \r");
							
							mycommand();
							
						} while ( false );
						
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
				}
			});
			
			mThread.start();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
    
		
		
	}
	
	
	   

	   
   private void mycommand() throws IOException 
	{
		out.println("$$$");
		out.flush();
		
		out.println("\r");
		out.flush();
		
		try 
		{
			TimeUnit.SECONDS.sleep(1);
		} 
		catch (InterruptedException e1) 
		{			
			e1.printStackTrace();
		}
		
		sb1 = new StringBuffer();
		len = 0;
		do 
		{
			len = in.read();			
			String s = Character.toString((char)len);			
			sb1.append( s );			
			if ( (sb1.length() > 2) && 
				 (sb1.substring(sb1.length()-2)).equals("> ") )
				break;
			
		} while( true );
		
		//System.out.println(command);
		out.println(command);
		out.flush();		

		try 
		{
			TimeUnit.SECONDS.sleep(1);				
		}
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
			
		
		sb2 = new StringBuffer();
		len = 0;
		do 
		{
			len = in.read();			
			String s = Character.toString((char)len);			
			sb2.append( s );			
		
			//System.out.println ( "byte:" + len + " char:" + (char)len + " len:" + sb2.length());
			
			if ( (sb2.length() > 2) && 
				 (sb2.substring(sb2.length()-2)).equals("> ") )
				break;
			
		} while( true );
		
		//System.out.println("Output:\n" + sb2.toString() + "\n");
		
		String newsb2 = sb2.substring(13, 27);
		
		System.out.println("Current IP:\n" + newsb2 + "\n");
		
		if ( !newsb2.contains("169.254.1.1") )
		{
			System.out.println("User NOT in SETUP mode! \nDisabling Save button. \n");
			
			//Button1 = (Button) findViewById(R.id.button1);
	    	
	    	//Button1.setEnabled(false);
			
			
			
			
	    	l_save = true;
		}
		else
		{
			System.out.println("SetUp mode detected \n");
			
		}

		
				
		disconnect();			
	}
	
	public void disconnect() 
	{
		try 
		{
			in.close();
			out.close();
			telnet.disconnect();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
   
   
}