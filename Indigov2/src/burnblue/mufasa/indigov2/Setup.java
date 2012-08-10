package burnblue.mufasa.indigov2;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import org.apache.commons.net.telnet.TelnetClient;

public class Setup extends Activity 
{
	private Button Button1, Button2;
	
	String server = "192.168.2.175";	
	private int port = 20000;
	private TelnetClient telnet = new TelnetClient();
	private InputStream in;
	private PrintStream out;
	StringBuffer sb, sb1, sb2, command;
	Handler mHandler = new Handler();
	
	SharedPreferences ourData;
	String SavedIp;
	int SavedPort;
	
	int len = 0;
	int len1 = 0;

	boolean l_save = false;
	
	//wiflyConnect l_tmp = new wiflyConnect();
	
	public void onCreate(Bundle savedInstanceState) 
    {
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.setup);

        
        ourData = getSharedPreferences(Options.fileName, 0);
    	SavedIp = ourData.getString(Options.moduleip, "169.254.1.1");
        SavedPort = ourData.getInt(Options.moduleport, 2000 );
    	
        
    	try 
		{
			telnet.connect(server, port);
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
							
							command = new StringBuffer("get ip a \r");
							
							mycommand();
							
							Setup.this.mHandler.post(new Runnable()
							{
								public void run() 
								{
									
									if ( l_save )
									{
										Button1.setEnabled(false);
										Setup.this.Button1.setEnabled(false);
									}
									
									System.out.println("Disable: " + l_save + "\n" );
							
								}
							});
							
							
							
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
    	
  	
    	saveButton();
    	cancelButton();
    	
    	if ( l_save )
    		Button1.setEnabled(false);
    	
    	System.out.println("Disable: " + l_save + "\n" );
    	
    	
    }	
   

	public void saveButton() 
	{
    	Button1 = (Button) findViewById(R.id.button1);
    	
    	Button1.setOnClickListener(new OnClickListener() 
    	{
      		public void onClick(View v) 
    		{
    			startActivity(new Intent( v.getContext(), MainActivity.class));
    		}
     	});
   	
	} 

   
   public void cancelButton() 
   {
      	Button2 = (Button) findViewById(R.id.button2);

    	Button2.setOnClickListener(new OnClickListener() 
    	{
     		public void onClick(View v) 
    		{
    			startActivity(new Intent( v.getContext(), MainActivity.class));
    		}
     	});
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
		
		StringBuffer sb1 = new StringBuffer();
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
			
		
		StringBuffer sb2 = new StringBuffer();
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