package burnblue.mufasa.indigov2;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.Button;


import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import org.apache.commons.net.telnet.TelnetClient;
import android.os.Handler;

public class MainActivity extends Activity 
{
	// garage light 1
	public static String modulePin4on =  "set sys output 0x0100 0x0100 \r";
  	public static String modulePin4off = "set sys output 0x0000 0x0100 \r";

	// garage door 1
	public static String modulePin7on =  "set sys output 0x0080 0x0080 \r";
  	public static String modulePin7off = "set sys output 0x0000 0x0080 \r";

	// garage light 2
	public static String modulePin9on =  "set sys output 0x0002 0x0002 \r";
  	public static String modulePin9off = "set sys output 0x0000 0x0002 \r";

  	// garage door 2
	public static String modulePin11on =  "set sys output 0x4000 0x4000 \r";
  	public static String modulePin11off = "set sys output 0x0000 0x4000 \r";

  	// ad hoc module ip
	public static String iv_adhoc = "169.254.1.1";
	
	// android app identifier
	public static String iv_android = "Android> ";
	
	// wifly module identifier
	public static String iv_wifly = "Wifly> ";

	
	
	private SharedPreferences ourData;
	private String SavedIp;
	private int SavedPort;
	
	private ToggleButton toggleButton1, toggleButton2, toggleButton3, toggleButton4;
	private ImageView image1, image2, image3, image4;
	private Button Button1, Button2;
	
	private TextView textview5;
  	
	public Handler mHandler = new Handler();
	public Handler mHandler2 = new Handler();
		
	// basically the android main function
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.activity_main);
    
        ourData = getSharedPreferences(Options.fileName, 0);
    	SavedIp = ourData.getString(Options.moduleip, "169.254.1.1");
        SavedPort = Integer.parseInt(ourData.getString(Options.moduleport, "2000" ));
    	
        textview5 = (TextView) findViewById(R.id.textView5);
        
    	garageDoor1Button();
    	light1Button();
    	garageDoor2Button();
    	light2Button();

    	setupButton();
    	optionsButton();
    	
    	
    	
    	//wiflyConnect l_tmp = new wiflyConnect();
    	//l_tmp.sendCommand("get ip a \r");
    	
    	//if ( l_tmp.l_save )
    	//    		((Button)findViewById(R.id.button1)).setEnabled(false);
    	
    	//	System.out.println("l_options filename: " + Options.fileName + "\n");
    }
   
    // telnet interface disconnect method
    public void disconnect(TelnetClient i_client, InputStream i_in, PrintStream i_out) throws IOException 
	{
		i_in.close();
		i_out.close();
		i_client.disconnect();
		
		System.out.println( iv_android + "communication done: ip: " + SavedIp );
	}

    // get wifly prompt (use only after handshake, also needed after sending a command)
    public boolean getPrompt( InputStream i_in ) throws IOException
    {
    	boolean l_rc = false;
    	StringBuffer l_sb = new StringBuffer();
    	int l_len = 0;
    	    	
    	do 
    	{
    		l_len = i_in.read();			
    		String s = Character.toString((char)l_len);			
    		l_sb.append( s );			

    		if ( (l_sb.length() > 2) && 
    				(l_sb.substring(l_sb.length()-2)).equals("> ") )
    		{
    			l_rc = true;
    			break;
    		}

    	} while( true );
    	
    	System.out.println(iv_wifly + l_sb);
    	System.out.println(iv_android + "Prompt acquired, wifly is ready to receive");
    	
    	return l_rc;
    }
    
    // handshake with wifly (must be done prior to any real commands being sent)
    public boolean handshake( InputStream i_in, PrintStream i_out ) throws IOException
    {
    	boolean l_rc = false;
    	
    	StringBuffer l_sb = new StringBuffer();
    	int l_len = 0;
    	
    	System.out.println( iv_android + "Starting handshake: ip: " + SavedIp );						

    	do  
    	{
    		l_len = i_in.read();
    		String s = Character.toString((char)l_len);								
    		l_sb.append( s );								

    	} while ( !l_sb.toString().equals("*HELLO*") );

    	
    	System.out.println( iv_android + "Received: " + l_sb );

    	i_out.println("$$$");
    	i_out.flush();

    	i_out.println("\r");
    	i_out.flush();

    	if ( (l_sb.toString().equals("*HELLO*")) && !i_out.checkError() )
    		l_rc = true;
    	
    	System.out.println(iv_android + "HandShake Done");
    	
    	return l_rc;
    }

    // send command but also read the output (must be done after handshake)
    public int sendCommandAndRead( InputStream i_in, PrintStream i_out ) throws IOException
    {
    	int l_rc = 0;
    	int l_idx = 0;
    	
    	// send the command we want
    	sendCommand( "show q 2 \r", i_out );

    	// get prompt
    	StringBuffer l_sb = new StringBuffer();
    	int l_len = 0;
    	    	
    	do 
    	{
    		l_len = i_in.read();			
    		String s = Character.toString((char)l_len);			
    		l_sb.append( s );			

    		if ( (l_sb.length() > 2) && 
    				(l_sb.substring(l_sb.length()-2)).equals("> ") )
    			break;

    	} while( true );
    	
    	System.out.println(iv_wifly + l_sb);
    	
    	l_idx = l_sb.indexOf("8");
    	
    	System.out.println(iv_wifly + "length:" + l_sb.length() + "lastindex:" + l_idx );
    	
    	String l_tmp = l_sb.substring(l_idx+1, l_idx+6);
    	
    	System.out.println(iv_wifly + "lbuf:" + l_tmp  );
  	  	    	
  	  	l_rc = (Integer.parseInt(l_tmp, 16) / 1000 );
  	  	
    	return l_rc;
    }
    
    
    
    // send a command to wifly module (use only after handshake, and follow it with a getPrompt)
    // NOTE: all commands should end with \r!
    public boolean sendCommand( String i_cmd, PrintStream i_out )
    {
    	boolean l_rc = false;
		System.out.println(iv_android + "Sending:" + i_cmd);

		i_out.println(i_cmd);
		i_out.flush();		
		
		if (!i_out.checkError())
			l_rc = true;
		
		return l_rc;
    }
        
    public void delayBetweenSends( int i_milliSeconds )
    {
    	try 
    	{
    		TimeUnit.MILLISECONDS.sleep( i_milliSeconds );
    	}
    	catch ( Exception e )
    	{
    		e.printStackTrace();
    	}
    }
    
    
    // garage door 1 button on click handler
    public void garageDoor1Button() 
    {
    	// garage door 1
    	toggleButton1 = (ToggleButton) findViewById(R.id.toggleButton1);
    	image1 = (ImageView) findViewById(R.id.imageView1);
    	
    	// light 1
    	toggleButton2 = (ToggleButton) findViewById(R.id.toggleButton2);		
      	image2 = (ImageView) findViewById(R.id.imageView2);
    	 
      	// garage door 1 clicked
    	toggleButton1.setOnClickListener(new OnClickListener() 
    	{
    		TelnetClient l_telnet = new TelnetClient();
    		InputStream l_in;
    		PrintStream l_out;
    		boolean l_worked = false;
    		
      		public void onClick(View v) 
    		{
      			try
      			{
      				l_telnet.setConnectTimeout(250);
      				l_telnet.connect(SavedIp, SavedPort);
      			}
      			catch ( java.net.SocketTimeoutException e)
      			{
      				System.out.println( iv_android + "connection failure: socket timeout" );

      				if (toggleButton1.getText().equals("open"))
      				{
      					toggleButton1.setChecked(false);
      					toggleButton2.setChecked(false);
      				}
      				else
      				{
      					toggleButton1.setChecked(true);
      					toggleButton2.setChecked(true);
      				}

    				Toast.makeText( MainActivity.this , "Connection failure, please check your option settings", Toast.LENGTH_SHORT).show();
      			}
      			catch ( java.net.UnknownHostException e)
      			{
      				System.out.println( iv_android + "connection failure: unknown host" );

      				if (toggleButton1.getText().equals("open"))
      				{
      					toggleButton1.setChecked(false);
      					toggleButton2.setChecked(false);
      				}
      				else
      				{
      					toggleButton1.setChecked(true);
      					toggleButton2.setChecked(true);
      				}

      				Toast.makeText( MainActivity.this , "Connection failure, please check your option settings", Toast.LENGTH_SHORT).show();
      			}
      			catch ( Exception e)
      			{
      				System.out.println( iv_android + "unexpected exception hit" );    					
      			}


      			if (l_telnet.isConnected())
      			{		
      				l_in = l_telnet.getInputStream();			
      				l_out = new PrintStream(l_telnet.getOutputStream());
      				

      				Thread mThread = new Thread(new Runnable() 
      				{
      					public void run() 
      					{
      						try 
      						{
      							
      							// handshake with wifly module
      						    handshake( l_in, l_out);

      						    // get prompt from wifly module
      						    getPrompt( l_in );
      						     
      						    // send command to pulse pin 7 on
      						    sendCommand( modulePin7on, l_out);

      						    // get prompt from wifly module
      						    getPrompt( l_in );

      						    // Note: wifly requires at least some time to 
      						    // saturate output pins to high 
      						    delayBetweenSends(100);

      						    // send command to pulse pin 7 off
      						    sendCommand( modulePin7off, l_out );

      						    // get prompt from wifly module
      						    getPrompt( l_in );

      						    // if we have gotten to this point everything should have worked!!
      						    l_worked = true;

      						    // lets disconnect to allow any new connections to work
      						    disconnect( l_telnet, l_in, l_out);	

      						    MainActivity.this.mHandler.post(new Runnable()
      						    {
      						    	public void run() 
      						    	{
      						    		if ( l_worked )
      						    		{
      						    			if (toggleButton1.getText().equals("open"))
      						    			{
      						    				image1.setImageResource(R.drawable.garage_opened);
      						    				toggleButton2.setChecked(true);
      						    				image2.setImageResource(R.drawable.light_on);
      						    			}
      						    			else
      						    			{
      						    				image1.setImageResource(R.drawable.garage_closed);
      						    				toggleButton2.setChecked(true);
      						    				image2.setImageResource(R.drawable.light_on);
      						    			}
      						    		}
      						    	}
      						    });
      						     
     						/*	MainActivity.this.mHandler2.post(new Runnable()
     							{
     								public void run() 
     								{
     									int l_cnt = 0;
     									
     									do
     									{
     										try 
     										{
     											TimeUnit.SECONDS.sleep(1);
     										}
     										catch ( Exception e)
     										{
     										
     										}
     										
     										l_cnt++;
     									} while (l_cnt < 10);
     									
     									// check if everything worked
     									if ( l_worked )
     									{
     										if (toggleButton2.getText().equals("on"))
     										{
     											toggleButton2.setChecked(false);
     											image2.setImageResource(R.drawable.light_off);
     										}     										    			
     									}
     								}    								
     							});*/

      						     
      						} 
      						catch (IOException e) 
      						{
      							e.printStackTrace();
      						}
      					}
      				});

      				mThread.start();
      			}

    		}
 
    	});
   } 
   
    // garage light 1 button on click handler
	public void light1Button() 
	{
		toggleButton2 = (ToggleButton) findViewById(R.id.toggleButton2);
      	image2 = (ImageView) findViewById(R.id.imageView2);

    	toggleButton2.setOnClickListener(new OnClickListener() 
    	{
    		TelnetClient l_telnet = new TelnetClient();
    		InputStream l_in;
    		PrintStream l_out;
    		boolean l_worked = false;
    		
    		public void onClick(View v) 
    		{
    			try
    			{
    				// set a small connection timeout so user does
    				// not wait a long time 
    				l_telnet.setConnectTimeout(250);
    				l_telnet.connect(SavedIp, SavedPort);
    			}
    			catch ( java.net.SocketTimeoutException e)
    			{
    				System.out.println( iv_android + "connection failure: socket timeout" );

    				if (toggleButton2.getText().equals("on"))
    					toggleButton2.setChecked(false);
    				else
    					toggleButton2.setChecked(true);
    				
    				Toast.makeText( MainActivity.this , "Connection failure, please check your option settings", Toast.LENGTH_SHORT).show();
    			}
    			catch ( java.net.UnknownHostException e)
    			{
    				System.out.println( iv_android + "connection failure: unknown host" );

    				if (toggleButton2.getText().equals("on"))
    					toggleButton2.setChecked(false);
    				else
    					toggleButton2.setChecked(true);
    				
    				Toast.makeText( MainActivity.this , "Connection failure, please check your option settings", Toast.LENGTH_SHORT).show();
    			}
    			catch ( Exception e)
    			{
    				System.out.println( iv_android + "unexpected exception hit" );    					
    			}

    			// did we connect?
    			if (l_telnet.isConnected())
    			{		
    				// setup our input and output streams
    				l_in = l_telnet.getInputStream();			
    				l_out = new PrintStream(l_telnet.getOutputStream());

    				Thread mThread = new Thread(new Runnable() 
    				{
    					public void run() 
    					{
    						try 
    						{
    							// handshake with wifly module
    							handshake( l_in, l_out);

    							// get prompt from wifly module
    							getPrompt( l_in );				

    							// send command to pulse pin 4 on
    							sendCommand( modulePin4on, l_out);

    							// get prompt from wifly module    								
    							getPrompt( l_in );

      						    // Note: wifly requires at least some time to 
      						    // saturate output pins to high 
      						    delayBetweenSends(100);
    							
    							// send command to pulse pin 4 off
    							sendCommand( modulePin4off, l_out );

    							// get prompt from wifly module    								
    							getPrompt( l_in );
    							
    							// attempt to read value from module
    							final int l_read = sendCommandAndRead( l_in, l_out );

    							// if we have gotten to this point everything should have worked!!
    							l_worked = true;

    							// lets disconnect to allow any new connections to work
    							disconnect( l_telnet, l_in, l_out);			

    							MainActivity.this.mHandler.post(new Runnable()
    							{
    								public void run() 
    								{
    									// check if everything worked
    									if ( l_worked )
    									{
    										if (toggleButton2.getText().equals("on"))
    										{
    											image2.setImageResource(R.drawable.light_on);
    										}
    										else
    										{
    											image2.setImageResource(R.drawable.light_off);
    										}    			
    									}
    								}    								
    							});

    							MainActivity.this.mHandler2.post(new Runnable()
    							{
    								public void run() 
    								{
    									if ( l_read > 100 )
    									{
    										textview5.setText("garage door 1 is closed");
    										if (toggleButton1.getText().equals("open"))
    										{
    											image1.setImageResource(R.drawable.garage_closed);
    											toggleButton1.setChecked(false);
    										}
    											
    									}
    									else
    									{
    										textview5.setText("garage door 1 is open");
    										if (toggleButton1.getText().equals("close"))
    										{
    											image1.setImageResource(R.drawable.garage_opened);
    											toggleButton1.setChecked(true);
    										}
    									}
    									
    								}    								
    							});
    							
    						} 
    						catch (IOException e) 
    						{
    							e.printStackTrace();
    						}
    					}
    				});

    				mThread.start();
    			}
	    		
    		}
 
    	});
    	
   } 
 
	// garage door 2 button on click handler
	public void garageDoor2Button() 
   {
    	toggleButton3 = (ToggleButton) findViewById(R.id.toggleButton3);
    	image3 = (ImageView) findViewById(R.id.imageView3);
    	toggleButton4 = (ToggleButton) findViewById(R.id.toggleButton3);		
      	image4 = (ImageView) findViewById(R.id.imageView3);
 	
    	
    	toggleButton3.setOnClickListener(new OnClickListener() 
    	{
    		TelnetClient l_telnet = new TelnetClient();
 		    InputStream l_in;
 		    PrintStream l_out;
 		    boolean l_worked = false;
 		     
      		public void onClick(View v) 
    		{
      		     try
      		     {
      		    	 l_telnet.setConnectTimeout(250);
      		    	 l_telnet.connect(SavedIp, SavedPort);
      		     }
      		     catch ( java.net.SocketTimeoutException e)
      		     {
      		    	 System.out.println( iv_android + "connection failure: socket timeout" );

      		    	 if (toggleButton3.getText().equals("open"))
      		    	 {
      		    		 toggleButton3.setChecked(false);
      		    		 toggleButton4.setChecked(false);
      		    	 }
      		    	 else
      		    	 {
      		    		 toggleButton3.setChecked(true);
      		    		 toggleButton4.setChecked(true);
      		    	 }

      		    	Toast.makeText( MainActivity.this , "Connection failure, please check your option settings", Toast.LENGTH_SHORT).show();
      		     }
      		     catch ( java.net.UnknownHostException e)
      		     {
      		    	 System.out.println( iv_android + "connection failure: unknown host" );

      		    	 if (toggleButton3.getText().equals("open"))
      		    	 {
      		    		 toggleButton3.setChecked(false);
      		    		 toggleButton4.setChecked(false);
      		    	 }
      		    	 else
      		    	 {
      		    		 toggleButton3.setChecked(true);
      		    		 toggleButton4.setChecked(true);
      		    	 }

       		    	Toast.makeText( MainActivity.this , "Connection failure, please check your option settings", Toast.LENGTH_SHORT).show();
      		     }
      		     catch ( Exception e)
      		     {
      		    	 System.out.println( iv_android + "unexpected exception hit" );
      		     }

      		     if (l_telnet.isConnected())
      		     {		
      		    	 l_in = l_telnet.getInputStream();			
      		    	 l_out = new PrintStream(l_telnet.getOutputStream());

      		    	 Thread mThread = new Thread(new Runnable() 
      		    	 {
      		    		 public void run() 
      		    		 {
      		    			 try 
      		    			 {
      		    			     // handshake with wifly module
      		    			     handshake( l_in, l_out);

      		    			     // get prompt from wifly module
      		    			     getPrompt( l_in );	

      		    			     // send command to pulse pin 11 on
      		    			     sendCommand( modulePin11on, l_out);

      		    			     // get prompt from wifly module
      		    			     getPrompt( l_in );

       						    // Note: wifly requires at least some time to 
       						    // saturate output pins to high 
       						    delayBetweenSends(100);
      		    			     
      		    			     // send command to pulse pin 11 off
      		    			     sendCommand( modulePin11off, l_out );

      		    			     // get prompt from wifly module
      		    			     getPrompt( l_in );

      		    			     // if we have gotten to this point everything should have worked!!
      		    			     l_worked = true;

      		    			     // lets disconnect to allow any new connections to work
      		    			     disconnect( l_telnet, l_in, l_out);	

      		    			     MainActivity.this.mHandler.post(new Runnable()
      		    			     {
      		    			    	 public void run() 
      		    			    	 {
      		    			    		 if ( l_worked )
      		    			    		 {
      		    			    			 if (toggleButton3.getText().equals("open"))
      		    			    			 {
      		    			    				 image3.setImageResource(R.drawable.garage_opened);
      		    			    				 toggleButton4.setChecked(true);
      		    			    				 image4.setImageResource(R.drawable.light_on);
      		    			    			 }
      		    			    			 else
      		    			    			 {
      		    			    				 image3.setImageResource(R.drawable.garage_closed);
      		    			    				 toggleButton4.setChecked(false);
      		    			    				 image4.setImageResource(R.drawable.light_off);
      		    			    			 }
      		    			    		 }
      		    			    	 }
      		    			     });
		    				 
      		    			 } 
      		    			 catch (IOException e) 
      		    			 {
      		    				 e.printStackTrace();
      		    			 }
      		    		 }
      		    	 });

      		    	 mThread.start();
      		     }
    		}
 
    	});
   } 
   
	// garage light 2 button on click handler
	public void light2Button() 
	{
 		toggleButton4 = (ToggleButton) findViewById(R.id.toggleButton4);
     	image4 = (ImageView) findViewById(R.id.imageView4);

    	toggleButton4.setOnClickListener(new OnClickListener() 
    	{
    		TelnetClient l_telnet = new TelnetClient();
    	    InputStream l_in;
    	    PrintStream l_out;
    	    boolean l_worked = false;
    		
    		public void onClick(View v) 
    		{
    				
    				try
    				{
    					l_telnet.setConnectTimeout(250);
    					l_telnet.connect(SavedIp, SavedPort);
    				}
    				catch ( java.net.SocketTimeoutException e)
    				{
    					System.out.println( iv_android + "connection failure: socket timeout" );
    					
    					if (toggleButton4.getText().equals("on"))
        					toggleButton4.setChecked(false);
        				else
        					toggleButton4.setChecked(true);
    					
    				}
    				catch ( java.net.UnknownHostException e)
    				{
    					System.out.println( iv_android + "connection failure: unknown host" );
    					
    					if (toggleButton4.getText().equals("on"))
        					toggleButton4.setChecked(false);
        				else
        					toggleButton4.setChecked(true);
    					
    				}
    			     catch ( Exception e)
    			     {
    			    	 System.out.println( iv_android + "unexpected exception hit" );
    			     }
    				
    				
    				if (l_telnet.isConnected())
    				{		
    					l_in = l_telnet.getInputStream();			
    					l_out = new PrintStream(l_telnet.getOutputStream());
    				
    					Thread mThread = new Thread(new Runnable() 
    					{
    						public void run() 
    						{
    							try 
    							{
    								// handshake with wifly module
    							    handshake( l_in, l_out);

    							    // get prompt from wifly module
    							    getPrompt( l_in );	

    							    // send command to pulse pin 9 on
    							    sendCommand( modulePin9on, l_out);

    							    // get prompt from wifly module
    							    getPrompt( l_in );
    							    
           						    // Note: wifly requires at least some time to 
           						    // saturate output pins to high 
           						    delayBetweenSends(100);

    							    // send command to pulse pin 9 off
    							    sendCommand( modulePin9off, l_out );

    							    // get prompt from wifly module
    							    getPrompt( l_in );

    							    // if we have gotten to this point everything should have worked!!
    							    l_worked = true;

    							    // lets disconnect to allow any new connections to work
    							    disconnect( l_telnet, l_in, l_out);	
	    								
    							    MainActivity.this.mHandler.post(new Runnable()
    							    {
    							    	public void run() 
    							    	{
    							    		if ( l_worked )
    							    		{
    							    			if (toggleButton4.getText().equals("on"))
    							    			{
    							    				image4.setImageResource(R.drawable.light_on);
    							    			}
    							    			else
    							    			{
    							    				image4.setImageResource(R.drawable.light_off);
    							    			}    			
    							    		}
    							    	}
    							    });
	    						} 
	    						catch (IOException e) 
	    						{
	    							e.printStackTrace();
	    						}
	    					}
	    				});
	    				
    					mThread.start();
    				}
    		}
 
    	});
   } 

	// setup up button on click handler
	public void setupButton() 
   {
		final TelnetClient l_telnet = new TelnetClient();

	   	// button1 on the main activity is the options button
       	Button1 = (Button) findViewById(R.id.button1);
     	
		try 
		{
			//setting connection timeout to something really small
			//so we can check quickly if module is in adhoc mode
			l_telnet.setConnectTimeout(125);
		
			try
			{
				// try to connect to adhoc ip
				l_telnet.connect(iv_adhoc, SavedPort);
			}
			catch ( java.net.SocketTimeoutException e)
			{
				// we timed out - big indication that we are NOT in adhoc mode
				System.out.println( iv_android + "socket timed out on adhoc mode ip, "+ iv_adhoc +", hiding setup menu button ");
			}
			
			if (l_telnet.isConnected())
			{
				// we are in adhoc mode so we will NOT disable the setup menu
				System.out.println( iv_android + "We are in adhoc mode!!!?" );
			
				Thread mThread = new Thread(new Runnable() 
				{
			        InputStream l_in = l_telnet.getInputStream();			
					PrintStream l_out = new PrintStream(l_telnet.getOutputStream());
					
					public void run() 
					{
						try 
						{
						     // handshake with wifly module
						     handshake( l_in, l_out);

						     // get prompt from wifly module
						     getPrompt( l_in );								
						
						     // lets disconnect to allow any new connections to work
						     disconnect( l_telnet, l_in, l_out);
						} 
						catch (IOException e) 
						{
							e.printStackTrace();
						}
					}
				});
			
				mThread.start();
			}
			else
			{
				Button1.setVisibility(View.GONE);
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
       	
       	
    	Button1.setOnClickListener(new OnClickListener() 
    	{
    		public void onClick(View v) 
    		{
    			startActivity(new Intent( v.getContext(), Setup.class));    			    			
    		}
    	});
   } 
   
	// options button on click handler
	public void optionsButton() 
   {
       	Button2 = (Button) findViewById(R.id.button2);       	
      	
    	Button2.setOnClickListener(new OnClickListener() 
    	{
      		public void onClick(View v) 
    		{
      			startActivity(new Intent( v.getContext(), Options.class));
    		}
    	});
   } 


   
}