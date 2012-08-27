package burnblue.mufasa.indigov2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
//import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.Button;
import java.io.IOException;
import android.os.Handler;

public class MainActivity extends Activity 
{
  	// ad hoc module ip
	public static String iv_adhoc = "169.254.1.1";
	
	// ad hoc default port
	public static String iv_adhocPort = "2000";
	
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
	
	// used for live debug ... yes its like a live printf
	//private TextView textview5;
  	
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
    	SavedIp = ourData.getString(Options.moduleip, iv_adhoc );
        SavedPort = Integer.parseInt(ourData.getString(Options.moduleport, iv_adhocPort ));
    	
        //textview5 = (TextView) findViewById(R.id.textView5);
        
        updateDoorInfo();
        
    	garageDoor1Button();
    	light1Button();
    	garageDoor2Button();
    	light2Button();

    	setupButton();
    	optionsButton();
    }
    
    public void updateDoorInfo()
    {
    	// garage door 1
    	toggleButton1 = (ToggleButton) findViewById(R.id.toggleButton1);
    	image1 = (ImageView) findViewById(R.id.imageView1);
    	
    	// garage door 2 
    	toggleButton3 = (ToggleButton) findViewById(R.id.toggleButton3);		
      	image3 = (ImageView) findViewById(R.id.imageView3);
    	 
      	final wiflyConnect l_wifly = new wiflyConnect( SavedIp, SavedPort );
      	
      	// at startup need to set connection timeout a little
      	// higher since it keeps timing out
      	l_wifly.setConnectionTimeOut( 750 );

      	try
      	{
      		l_wifly.connect();
      	}
      	catch ( java.net.SocketTimeoutException e)
      	{
      		System.out.println( iv_android + "connection failure: socket timeout" );

      		Toast.makeText( MainActivity.this , "Connection failure, please check your option settings", Toast.LENGTH_SHORT).show();
			
      		e.printStackTrace();
      	}
      	catch ( java.net.UnknownHostException e)
      	{
      		System.out.println( iv_android + "connection failure: unknown host" );

      		Toast.makeText( MainActivity.this , "Connection failure, please check your option settings", Toast.LENGTH_SHORT).show();
      	}
      	catch ( Exception e)
      	{
      		System.out.println( iv_android + "unexpected exception hit" );    					
      	}

      	if (l_wifly.isConnected())
      	{		
      		Thread mThread = new Thread(new Runnable() 
      		{
      			public void run() 
      			{
      				try 
      				{
      			    	// handshake with wifly module
      			    	l_wifly.handshake();

      			    	// get prompt from wifly module
      			    	l_wifly.getPrompt();

      			    	// fix door if needed
      			    	final int l_sensor = l_wifly.readSensor1();
      			    	final int l_sensor2 = l_wifly.readSensor2();
      			    	
      			    	l_wifly.disconnect();

      					MainActivity.this.mHandler.post(new Runnable()
      					{
      						public void run() 
      						{
   							
								if ( l_sensor > 100 )
								{
									//textview5.setText("garage door 1 is closed");
									if (toggleButton1.getText().equals("open"))
									{
										image1.setImageResource(R.drawable.garage_closed);
										toggleButton1.setChecked(false);
									}
										
								}
								else
								{
									//textview5.setText("garage door 1 is open");
									if (toggleButton1.getText().equals("close"))
									{
										image1.setImageResource(R.drawable.garage_opened);
										toggleButton1.setChecked(true);
									}
								}
      							
								if ( l_sensor2 > 100 )
								{
									//textview5.setText("garage door 1 is closed");
									if (toggleButton3.getText().equals("open"))
									{
										image3.setImageResource(R.drawable.garage_closed);
										toggleButton3.setChecked(false);
									}
										
								}
								else
								{
									//textview5.setText("garage door 1 is open");
									if (toggleButton3.getText().equals("close"))
									{
										image3.setImageResource(R.drawable.garage_opened);
										toggleButton3.setChecked(true);
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
    		wiflyConnect l_wifly = new wiflyConnect( SavedIp, SavedPort);
    		boolean l_worked = false;
    		
      		public void onClick(View v) 
    		{
      			try
      			{
      				l_wifly.connect();
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

      			if (l_wifly.isConnected())
      			{		

      				Thread mThread = new Thread(new Runnable() 
      				{
      					public void run() 
      					{
      						try 
      						{
      							l_wifly.SendAll(7);

      						    // if we have gotten to this point everything should have worked!!
      						    l_worked = true;

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
    		wiflyConnect l_wifly = new wiflyConnect( SavedIp, SavedPort );
    		boolean l_worked = false;
    		
    		public void onClick(View v) 
    		{
    			try
    			{
    				l_wifly.connect();
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
    			if (l_wifly.isConnected())
    			{		
    				Thread mThread = new Thread(new Runnable() 
    				{
    					public void run() 
    					{
    						try 
    						{
    							l_wifly.SendAll(4);
    							
    							// if we have gotten to this point everything should have worked!!
    							l_worked = true;


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
    									if ( l_wifly.Sensor1() > 100 )
    									{
    										//textview5.setText("garage door 1 is closed");
    										if (toggleButton1.getText().equals("open"))
    										{
    											image1.setImageResource(R.drawable.garage_closed);
    											toggleButton1.setChecked(false);
    										}
    											
    									}
    									else
    									{
    										//textview5.setText("garage door 1 is open");
    										if (toggleButton1.getText().equals("close"))
    										{
    											image1.setImageResource(R.drawable.garage_opened);
    											toggleButton1.setChecked(true);
    										}
    									}
    									
    									/*
    									if ( l_sensor2 > 100 )
    									{
    										textview5.setText( textview5.getText().toString() + "garage door 2 is closed");
    										if (toggleButton3.getText().equals("open"))
    										{
    											image3.setImageResource(R.drawable.garage_closed);
    											toggleButton3.setChecked(false);
    										}
    											
    									}
    									else
    									{
    										textview5.setText( textview5.getText().toString() + "garage door 2 is open");
    										
    										if (toggleButton3.getText().equals("close"))
    										{
    											image3.setImageResource(R.drawable.garage_opened);
    											toggleButton3.setChecked(true);
    										}
    									}
    									*/
    									
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
    		wiflyConnect l_wifly = new wiflyConnect( SavedIp, SavedPort );
 		    boolean l_worked = false;
 		     
      		public void onClick(View v) 
    		{
      		     try
      		     {
      		    	 l_wifly.connect();
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

      		     if (l_wifly.isConnected())
      		     {		
      		    	 Thread mThread = new Thread(new Runnable() 
      		    	 {
      		    		 public void run() 
      		    		 {
      		    			 try 
      		    			 {
      		    				 l_wifly.SendAll(11);
      		    				 
      		    			     // if we have gotten to this point everything should have worked!!
      		    			     l_worked = true;

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
    		wiflyConnect l_wifly = new wiflyConnect( SavedIp, SavedPort );
    	    boolean l_worked = false;
    		
    		public void onClick(View v) 
    		{
    				
    				try
    				{
    					l_wifly.connect();    					
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
    				
    				
    				if (l_wifly.isConnected())
    				{		
    					Thread mThread = new Thread(new Runnable() 
    					{
    						public void run() 
    						{
    							try 
    							{
    								l_wifly.SendAll(9);
    								
    							    // if we have gotten to this point everything should have worked!!
    							    l_worked = true;

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
		//final TelnetClient l_telnet = new TelnetClient();
		final wiflyConnect l_wifly = new wiflyConnect( iv_adhoc, SavedPort);

	   	// button1 on the main activity is the options button
       	Button1 = (Button) findViewById(R.id.button1);
     	
       	try
       	{
       		// try to connect to adhoc ip
       		l_wifly.connect();
       	}
       	catch ( java.net.SocketTimeoutException e)
       	{
       		// we timed out - big indication that we are NOT in adhoc mode
       		System.out.println( iv_android + "socket timed out on adhoc mode ip, "+ iv_adhoc +", hiding setup menu button ");
       	}
       	catch ( Exception e)
       	{
       		e.printStackTrace();
       	}


       	if (l_wifly.isConnected())
       	{
       		// we are in adhoc mode so we will NOT disable the setup menu
       		System.out.println( iv_android + "We are in adhoc mode!!!?" );

       		Thread mThread = new Thread(new Runnable() 
       		{
       			public void run() 
       			{
       				try 
       				{
       					// handshake with wifly module
       					l_wifly.handshake( );

       					// get prompt from wifly module
       					l_wifly.getPrompt( );								

       					// lets disconnect to allow any new connections to work
       					l_wifly.disconnect();
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