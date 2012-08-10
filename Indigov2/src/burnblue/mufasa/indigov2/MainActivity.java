package burnblue.mufasa.indigov2;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ToggleButton;
import android.widget.Button;


import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
//import java.net.SocketException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.net.telnet.TelnetClient;
import android.os.Handler;
import android.text.Editable;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends Activity 
{
	TextView text;
	EditText edit1, edit4;
	Editable server;	
	private int port = 20000;
	private TelnetClient telnet = new TelnetClient();
	private InputStream in;
	private PrintStream out;
	StringBuffer sb, sb1, sb2, command, command2;
	Handler mHandler = new Handler();
	int len = 0;
	int len1 = 0;
	SharedPreferences ourData;
	String SavedIp;
	int SavedPort;
	boolean l_worked;
	
	private ToggleButton toggleButton1, toggleButton2, toggleButton3, toggleButton4;
	private ImageView image1, image2, image3, image4;
	private Button Button1, Button2;
	
	public static String iv_adhoc = "169.254.1.1";
	public static String iv_android = "Android> ";
	public static String iv_wifly = "Wifly> ";
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.activity_main);
    
        ourData = getSharedPreferences(Options.fileName, 0);
    	SavedIp = ourData.getString(Options.moduleip, "169.254.1.1");
        SavedPort = Integer.parseInt(ourData.getString(Options.moduleport, "2000" ));
    	
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
   
   public void garageDoor1Button() 
   {
    	toggleButton1 = (ToggleButton) findViewById(R.id.toggleButton1);
    	image1 = (ImageView) findViewById(R.id.imageView1);
    	 
    	toggleButton1.setOnClickListener(new OnClickListener() 
    	{
      		public void onClick(View v) 
    		{
    			if (toggleButton1.getText().equals("open"))
    			{
    				image1.setImageResource(R.drawable.garage_opened);    		
    			}
    			else
    			{
    				image1.setImageResource(R.drawable.garage_closed);
    			}
    		}
 
    	});
   } 

   private void mycommand() throws IOException 
   {
	   System.out.println( "mycommand start \n" );
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
			//System.out.println ( "byte:" + len + " char:" + (char)len + " len:" + sb1.length());
			
			//if (sb1.length() > 2)
				//System.out.println("length:"+ sb1.length()+" substring:" + sb1.substring(sb1.length()-2) );
			
			if ( (sb1.length() > 2) && 
				 (sb1.substring(sb1.length()-2)).equals("> ") )
				break;
			
		} while( true );
		
		System.out.println(command);
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
			
		disconnect();			
		System.out.println( "mycommand end \n" );
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

   
   
   
	public void light1Button() 
	{
		
		toggleButton2 = (ToggleButton) findViewById(R.id.toggleButton2);
		text = (TextView)findViewById(R.id.textView5);
      	image2 = (ImageView) findViewById(R.id.imageView2);
		
      	command = new StringBuffer("set sys output 0x0080 0x0080 \r");
      	command2 = new StringBuffer("set sys output 0x0000 0x0080 \r");      	

    	toggleButton2.setOnClickListener(new OnClickListener() 
    	{
  
    		public void onClick(View v) 
    		{
    			
    			try 
    			{	
    				l_worked = false;
    				
    				try
    				{
    					telnet.setConnectTimeout(125);
    					telnet.connect(SavedIp, SavedPort);
    				}
    				catch ( java.net.SocketTimeoutException e)
    				{
    					System.out.println( iv_android + "connection failure: socket timeout" );
    					
    					//text.getText();
						//text.append( iv_android + "connection failure: timeout" + toggleButton2.getText() + " \n" );
    					
    					if (toggleButton2.getText().equals("on"))
        					toggleButton2.setChecked(false);
        				else
        					toggleButton2.setChecked(true);
    					
    				}
    				catch ( java.net.UnknownHostException e)
    				{
    					System.out.println( iv_android + "connection failure: unknown host" );
    					
    					//text.getText();
						//text.append( iv_android + "connection failure" + toggleButton2.getText() + " \n" );
    					
    					if (toggleButton2.getText().equals("on"))
        					toggleButton2.setChecked(false);
        				else
        					toggleButton2.setChecked(true);
    					
    				}
    				
    				
    				if (telnet.isConnected())
    				{		
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
    									System.out.println( iv_android + "Starting handshake: ip: " + SavedIp );						

    									do  
    									{
    										len = in.read();
    										String s = Character.toString((char)len);								
    										sb.append( s );								
    																									
    									} while ( !sb.toString().equals("*HELLO*") );
    								
    								
    									System.out.println( iv_android + "Received: " + sb );
    								
    									out.println("$$$");
    									out.flush();
    									
    									out.println("\r");
    									out.flush();
    								   	
    									try 
    									{
	    									TimeUnit.MILLISECONDS.sleep(125);				
	    								}
	    								catch (InterruptedException e) 
	    								{
	    									e.printStackTrace();
	    								}
    									
	    								StringBuffer sb1 = new StringBuffer();
	    								len = 0;
	    								do 
	    								{
	    									len = in.read();			
	    									String s = Character.toString((char)len);			
	    									sb1.append( s );			
	    									//System.out.println ( "byte:" + len + " char:" + (char)len + " len:" + sb1.length());
	    										
	    									//if (sb1.length() > 2)
	    									//System.out.println("length:"+ sb1.length()+" substring:" + sb1.substring(sb1.length()-2) );
	    										
	    									if ( (sb1.length() > 2) && 
	    										(sb1.substring(sb1.length()-2)).equals("> ") )
	    										break;
	    										
	    								} while( true );
	    									
	    								System.out.println(iv_android + "HandShake Done, Sending:" + command);
	
	    								out.println(command);
	    								out.flush();		
	    									
	    								try 
	    								{
	    									TimeUnit.MILLISECONDS.sleep(125);				
	    								}
	    								catch (InterruptedException e) 
	    								{
	    									e.printStackTrace();
	    								}
	    								
	    								sb1.setLength(0);
	    								len = 0;
	    								do 
	    								{
	    									len = in.read();			
	    									String s = Character.toString((char)len);			
	    									sb1.append( s );			
	    									//System.out.println ( "byte:" + len + " char:" + (char)len + " len:" + sb1.length());
	    										
	    									//if (sb1.length() > 2)
	    									//System.out.println("length:"+ sb1.length()+" substring:" + sb1.substring(sb1.length()-2) );
	    										
	    									if ( (sb1.length() > 2) && 
	    										(sb1.substring(sb1.length()-2)).equals("> ") )
	    										break;
	    										
	    								} while( true );
	    									
	    								System.out.println( iv_wifly + sb1 );
	    								System.out.println( iv_android + "Sending second command: " + command2);
	
	    								out.println(command2);
	    								out.flush();		
	    									
	    								try 
	    								{
	    									TimeUnit.MILLISECONDS.sleep(250);				
	    								}
	    								catch (InterruptedException e) 
	    								{
	    									e.printStackTrace();
	    								}
	    										
	    								sb1.setLength(0);
	    								len = 0;
	    								do 
	    								{
	    									len = in.read();			
	    									String s = Character.toString((char)len);			
	    									sb1.append( s );			
	    									//System.out.println ( "byte:" + len + " char:" + (char)len + " len:" + sb1.length());
	    										
	    									//if (sb1.length() > 2)
	    									//System.out.println("length:"+ sb1.length()+" substring:" + sb1.substring(sb1.length()-2) );
	    										
	    									if ( (sb1.length() > 2) && 
	    										(sb1.substring(sb1.length()-2)).equals("> ") )
	    										break;
	    										
	    								} while( true );
	    									
	    								System.out.println( iv_wifly + sb1 );
	    								
	    								l_worked = true;
	    								
	    								disconnect();			
	    								
	    								System.out.println( iv_android + "communication done: ip: " + SavedIp );
	    								
	    								
	    								MainActivity.this.mHandler.post(new Runnable()
	    								{
	    									public void run() 
	    									{
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
	    		}
	    		catch (Exception e) 
	    		{
	    			e.printStackTrace();
	    		}
    		}
 
    	});
   } 
   
   
   public void garageDoor2Button() 
   {
 
    	toggleButton3 = (ToggleButton) findViewById(R.id.toggleButton3);
    	image3 = (ImageView) findViewById(R.id.imageView3);
			
    	 
    	toggleButton3.setOnClickListener(new OnClickListener() 
    	{
      		public void onClick(View v) 
    		{
    			
    			if (toggleButton3.getText().equals("open"))
    			{
    				image3.setImageResource(R.drawable.garage_opened);    				
    			}
    			else
    			{
    				image3.setImageResource(R.drawable.garage_closed);    				
    			}
    		}
 
    	});
   } 

   
   public void light2Button() 
   {
 
      	toggleButton4 = (ToggleButton) findViewById(R.id.toggleButton4);
      	image4 = (ImageView) findViewById(R.id.imageView4);
		

    	toggleButton4.setOnClickListener(new OnClickListener() 
    	{
  
    		public void onClick(View v) 
    		{
    			if (toggleButton4.getText().equals("on"))
    			{
    				image4.setImageResource(R.drawable.light_on);    				
    			}
    			else
    			{
    				image4.setImageResource(R.drawable.light_off);    				
    			}    			
    		
    			try 
    			{			
    				telnet.connect("hiowatha.no-ip.info", port);
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
    								System.out.println( "start of loop" );						

    								do  
    								{
    									len = in.read();
    									String s = Character.toString((char)len);								
    									sb.append( s );								
    																									
    								} while ( !sb.toString().equals("*HELLO*") );
    								
    								
    								System.out.println( sb );
    								
    								/*MainActivity.this.mHandler.post(new Runnable()
    								{
    									public void run() 
    									{
    										MainActivity.this.text.getText();
    										MainActivity.this.text.append( sb.toString() );
    									}
    								});*/
    								
    								mycommand();
    								
    								System.out.println( "end of loop" );
    								
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
 
    	});
   } 

   public void setupButton() 
   {
	   	// button1 on the mainactivity is the options button
       	Button1 = (Button) findViewById(R.id.button1);
      	
		try 
		{
			//setting connection timeout to something really small
			//so we can check quickly if module is in adhoc mode
			telnet.setConnectTimeout(125);
		
			try
			{
				// try to connect to adhoc ip
				telnet.connect(iv_adhoc, port);
			}
			catch ( java.net.SocketTimeoutException e)
			{
				// we timed out - big indication that we are NOT in adhoc mode
				System.out.println( iv_android + "socket timed out on adhoc mode ip, "+ iv_adhoc +", hiding setup menu button ");
			}
			
			if (telnet.isConnected())
			{
				// we are in adhoc mode so we will NOT disable the setup menu
				System.out.println( iv_android + "We are in adhoc mode!!!?" );
			
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
								System.out.println( iv_android + "starting handshake" );						

								do  
								{
									len = in.read();
									String s = Character.toString((char)len);								
									sb.append( s );								
																								
								} while ( !sb.toString().equals("*HELLO*") );
							
							
		//						System.out.println( sb );
							
							//	mycommand();
								out.println("$$$");
								out.flush();
								
								out.println("\r");
								out.flush();
							   	
								try 
								{
									TimeUnit.MILLISECONDS.sleep(250);				
								}
								catch (InterruptedException e) 
								{
									e.printStackTrace();
								}

								disconnect();			
							
								System.out.println( iv_android + "communication done" );
							
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