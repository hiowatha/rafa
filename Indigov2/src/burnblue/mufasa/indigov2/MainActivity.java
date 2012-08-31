package burnblue.mufasa.indigov2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
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
	
	private ToggleButton door1Toggle, door2Toggle;
	private ImageView image1, image3;
	private Button SetupButton, OptionsButton, lightButton1, lightButton2;
	
	// used for live debug ... yes its like a live printf
	//private TextView textview5;
  	
	public Handler mHandler = new Handler();
	public Handler mHandler2 = new Handler();
	public Handler activateLightsBecauseOfDoors = new Handler();
	public Handler disableButtonWhileInProgress = new Handler();
	public Handler enableButtonAfterProgress = new Handler();
	public Handler updateDoor2 = new Handler();
	public Handler updateDoor1 = new Handler();
		
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
    	door1Toggle = (ToggleButton) findViewById(R.id.door1Toggle);
    	image1 = (ImageView) findViewById(R.id.imageView1);
    	
    	// garage door 2 
    	door2Toggle = (ToggleButton) findViewById(R.id.door2Toggle);		
      	image3 = (ImageView) findViewById(R.id.imageView3);
    	 
      	final wiflyConnect l_wifly = new wiflyConnect( SavedIp, SavedPort );
      	
      	// at startup need to set connection timeout a little
      	// higher since it keeps timing out
      	l_wifly.setConnectionTimeOut( 999 );

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
									// door 1 is closed
									if (door1Toggle.getText().equals("open"))
									{
										// fix door 1 toggle & image
										image1.setImageResource(R.drawable.garage_closed);
										door1Toggle.setChecked(false);
									}
										
								}
								else
								{
									// door 1 is open
									if (door1Toggle.getText().equals("close"))
									{
										// fix door 1 toggle & image
										image1.setImageResource(R.drawable.garage_opened);
										door1Toggle.setChecked(true);
									}
								}
      							
								if ( l_sensor2 > 100 )
								{
									// door 2 is closed
									if (door2Toggle.getText().equals("open"))
									{
										// fix door 2 toggle & image
										image3.setImageResource(R.drawable.garage_closed);
										door2Toggle.setChecked(false);
									}
										
								}
								else
								{
									// door 2 is open
									if (door2Toggle.getText().equals("close"))
									{
										// fix door 2 toggle & image
										image3.setImageResource(R.drawable.garage_opened);
										door2Toggle.setChecked(true);
									}
								}
								
								if ((l_sensor < 100 ) && (l_sensor2 < 100))
								{
						      		Toast.makeText( MainActivity.this , "Note: Both doors are currently open.", Toast.LENGTH_SHORT).show();
								}
								else if ((l_sensor > 100 ) && (l_sensor2 > 100))
								{
						      		Toast.makeText( MainActivity.this , "Note: Both doors are currently closed.", Toast.LENGTH_SHORT).show();
								}
								else if ( l_sensor < 100 )
								{
						      		Toast.makeText( MainActivity.this , "Note: Garage door 1 is currently open.", Toast.LENGTH_SHORT).show();
								}
								else if ( l_sensor2 < 100 )
								{
						      		Toast.makeText( MainActivity.this , "Note: Garage door 2 is currently open.", Toast.LENGTH_SHORT).show();
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
    	door1Toggle = (ToggleButton) findViewById(R.id.door1Toggle);
    	image1 = (ImageView) findViewById(R.id.imageView1);

      	// garage door 1 clicked
    	door1Toggle.setOnClickListener(new OnClickListener() 
    	{
    		wiflyConnect l_wifly = new wiflyConnect( SavedIp, SavedPort);
    		
      		public void onClick(View v) 
    		{
      			try
      			{
      				l_wifly.connect();
      			}
      			catch ( java.net.SocketTimeoutException e)
      			{
      				// garage door 1 clicked but failed!!!
      				System.out.println( iv_android + "connection failure: socket timeout" );

      				// button says open, but since we failed, 
      				// lets set everything for door 1 to closed
      				if (door1Toggle.getText().equals("open"))
      				{
      					door1Toggle.setChecked(false);
      	  				image1.setImageResource(R.drawable.garage_closed);
      				}
      				else
      				{
      					// button says closed, but since we failed,
      					// lets set everything for door 1 to open
      					door1Toggle.setChecked(true);
      					image1.setImageResource(R.drawable.garage_opened);
      				}

    				Toast.makeText( MainActivity.this , "Connection failure, please check your option settings", Toast.LENGTH_SHORT).show();
      			}
      			catch ( java.net.UnknownHostException e)
      			{
      				System.out.println( iv_android + "connection failure: unknown host" );

      				if (door1Toggle.getText().equals("open"))
      				{
      					door1Toggle.setChecked(false);
      	  				image1.setImageResource(R.drawable.garage_closed);
      					
      				}
      				else
      				{
      					// button says closed, but since we failed,
      					// lets set everything for door 1 to open
      					door1Toggle.setChecked(true);
      					image1.setImageResource(R.drawable.garage_opened);
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

      							// update door 2 if needed
      						    /*MainActivity.this.updateDoor2.post(new Runnable()
      						    {
      						    	public void run() 
      						    	{
      						    		// check on door 2 and update if needed!
    									if ( l_wifly.Sensor2() > 100 )
    									{
    										// sensor says we are closed!!
    										
    										if (door2Toggle.getText().equals("open"))
    										{
    											// fix button & image
    											image3.setImageResource(R.drawable.garage_closed);
    											door2Toggle.setChecked(false);
    										}
    											
    									}
    									else
    									{
    										// sensor says we are open!!!
    										if (door2Toggle.getText().equals("close"))
    										{
    											// fix button & image
    											image3.setImageResource(R.drawable.garage_opened);
    											door2Toggle.setChecked(true);
    										}
    									}
      						    	}
      						    });*/
      							
      						    // disable door 1 button temporarily
      							MainActivity.this.disableButtonWhileInProgress.post(new Runnable()
      							{
      								public void run()
      								{
      									// user has pressed button 1 (garage door 1)
      									// lets disable it and eventually enable it 
      									// when door finally finishes moving
  						    			// tell user we are opening door 1
      									if (door1Toggle.getText().equals("open"))
      										Toast.makeText( MainActivity.this , "Opening garage door 1 ... ", Toast.LENGTH_SHORT).show();
      									else
      										Toast.makeText( MainActivity.this , "Closing garage door 1 ...", Toast.LENGTH_SHORT).show();
  						    			      						    
  						    			// disable garage door 1 button while door moving
  						    			door1Toggle.setEnabled(false);

      								}
      							});

      							// enable door 1 button now that door is moving
      							MainActivity.this.enableButtonAfterProgress.postDelayed(new Runnable()
      							{
      								public void run()
      								{
      									if (door1Toggle.getText().equals("open"))
      						    		{
      										// wifly command worked so update door image
      										image1.setImageResource(R.drawable.garage_opened);
      						    		}
      									else
      									{
      						    			// wifly command worked / update both image
      						    			image1.setImageResource(R.drawable.garage_closed);
      									}

      									// user has pressed button 1 (garage door 1)
  										// lets enable it now that door is done moving 
  										door1Toggle.setEnabled(true);
      									
      								}
      							}, 1000);
      						     
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
		lightButton1 = (Button) findViewById(R.id.lightButton1);
		lightButton2 = (Button) findViewById(R.id.lightButton2);

    	lightButton1.setOnClickListener(new OnClickListener() 
    	{
    		wiflyConnect l_wifly = new wiflyConnect( SavedIp, SavedPort );
    		
    		public void onClick(View v) 
    		{
    			Toast.makeText( MainActivity.this , "Click Listener triggered!", Toast.LENGTH_SHORT).show();
    			
    			try
    			{
    				l_wifly.connect();
    			}
    			catch ( java.net.SocketTimeoutException e)
    			{
    				System.out.println( iv_android + "connection failure: socket timeout" );
    				
    				Toast.makeText( MainActivity.this , "Connection failure, please check your option settings", Toast.LENGTH_SHORT).show();
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

    			// did we connect?
    			if (l_wifly.isConnected())
    			{		
    				Thread mThread = new Thread(new Runnable() 
    				{
    					public void run() 
    					{
    						try 
    						{
    							l_wifly.SendAll(19);
    							
    							// if we have gotten to this point everything should have worked!!

    							// update door info if needed
    							MainActivity.this.mHandler.post(new Runnable()
    							{
    								public void run() 
    								{
    									// check on door 1
    									if ( l_wifly.Sensor1() > 100 )
    									{
    										// sensor says we are closed
    										if (door1Toggle.getText().equals("open"))
    										{
    											image1.setImageResource(R.drawable.garage_closed);
    											door1Toggle.setChecked(false);
    										}
    											
    									}
    									else
    									{
    										// sensor says we are open
    										if (door1Toggle.getText().equals("close"))
    										{
    											image1.setImageResource(R.drawable.garage_opened);
    											door1Toggle.setChecked(true);
    										}
    									}
    									
    									// check on door 2
    									if ( l_wifly.Sensor2() > 100 )
    									{
    										// sensor says we are closed
    										if (door2Toggle.getText().equals("open"))
    										{
    											image3.setImageResource(R.drawable.garage_closed);
    											door2Toggle.setChecked(false);
    										}
    											
    									}
    									else
    									{
    										// sensor says we are open
    										if (door2Toggle.getText().equals("close"))
    										{
    											image3.setImageResource(R.drawable.garage_opened);
    											door2Toggle.setChecked(true);
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
    
    	
    	lightButton1.setOnLongClickListener(new OnLongClickListener()
    	{
    		public boolean onLongClick(View v)    		
    		{
    			Toast.makeText( MainActivity.this , "Long Click Listener triggered!", Toast.LENGTH_SHORT).show();
    			//lightButton1.performClick();
    			
    			lightButton2.performClick();
    			
    			
    			return true;
    		}
    	});
   } 
 
	// garage door 2 button on click handler
	public void garageDoor2Button() 
   {
		// garage door 2
    	door2Toggle = (ToggleButton) findViewById(R.id.door2Toggle);
    	image3 = (ImageView) findViewById(R.id.imageView3);
    	
    	door2Toggle.setOnClickListener(new OnClickListener() 
    	{
    		wiflyConnect l_wifly = new wiflyConnect( SavedIp, SavedPort );
 		     
      		public void onClick(View v) 
    		{
      		     try
      		     {
      		    	 l_wifly.connect();
      		     }
      		     catch ( java.net.SocketTimeoutException e)
      		     {
      		    	 System.out.println( iv_android + "connection failure: socket timeout" );

      		    	 // button says open, but since we failed,
      		    	 // lets set everything for door 2 closed
      		    	 if (door2Toggle.getText().equals("open"))
      		    	 {
      		    		 door2Toggle.setChecked(false);
      		    		 image3.setImageResource(R.drawable.garage_closed);
      		    	 }
      		    	 else
      		    	 {
      		    		 // button says closed, but since we failed,
      		    		 // lets set everything for door 2 to open
      		    		 door2Toggle.setChecked(true);
      		    		 image3.setImageResource(R.drawable.garage_opened);
      		    	 }

      		    	Toast.makeText( MainActivity.this , "Connection failure, please check your option settings", Toast.LENGTH_SHORT).show();
      		     }
      		     catch ( java.net.UnknownHostException e)
      		     {
      		    	 System.out.println( iv_android + "connection failure: unknown host" );

      		    	 // button says open, but since we failed,
      		    	 // lets set everything for door 2 closed
      		    	 if (door2Toggle.getText().equals("open"))
      		    	 {
      		    		 door2Toggle.setChecked(false);
      		    		 image3.setImageResource(R.drawable.garage_closed);
      		    	 }
      		    	 else
      		    	 {
      		    		 // button says closed, but since we failed,
      		    		 // lets set everything for door 2 to open
      		    		 door2Toggle.setChecked(true);
      		    		 image3.setImageResource(R.drawable.garage_opened);
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
      		    				 
       							// update door 1 if needed
       						    /*MainActivity.this.updateDoor1.post(new Runnable()
       						    {
       						    	public void run() 
       						    	{
       						    		// check on door 2 and update if needed!
     									if ( l_wifly.Sensor1() > 100 )
     									{
     										// sensor says we are closed!!
     										
     										if (door1Toggle.getText().equals("open"))
     										{
     											// fix button & image
     											image1.setImageResource(R.drawable.garage_closed);
     											door1Toggle.setChecked(false);
     										}
     											
     									}
     									else
     									{
     										// sensor says we are open!!!
     										if (door1Toggle.getText().equals("close"))
     										{
     											// fix button & image
     											image1.setImageResource(R.drawable.garage_opened);
     											door1Toggle.setChecked(true);
     										}
     									}
       						    	}
       						    });*/
       							
       						    // disable door 2 button temporarily
       							MainActivity.this.disableButtonWhileInProgress.post(new Runnable()
       							{
       								public void run()
       								{
       									// user has pressed button 1 (garage door 1)
       									// lets disable it and eventually enable it 
       									// when door finally finishes moving
   						    			// tell user we are opening door 1
       									if (door2Toggle.getText().equals("open"))
       										Toast.makeText( MainActivity.this , "Opening garage door 2 ... ", Toast.LENGTH_SHORT).show();
       									else
       										Toast.makeText( MainActivity.this , "Closing garage door 2 ...", Toast.LENGTH_SHORT).show();
   						    			      						    
   						    			// disable garage door 1 button while door moving
   						    			door2Toggle.setEnabled(false);
       								}
       							});

       							// enable door 2 button now that door is moving
       							MainActivity.this.enableButtonAfterProgress.postDelayed(new Runnable()
       							{
       								public void run()
       								{
       									if (door2Toggle.getText().equals("open"))
       						    		{
       										// wifly command worked so update door image
       										image3.setImageResource(R.drawable.garage_opened);
       						    		}
       									else
       									{
       						    			// wifly command worked / update both image
       						    			image3.setImageResource(R.drawable.garage_closed);
       									}

       									// user has pressed button 1 (garage door 1)
   										// lets enable it now that door is done moving 
   										door2Toggle.setEnabled(true);
       									
       								}
       							}, 1000);
		    				 
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
		lightButton2 = (Button) findViewById(R.id.lightButton2);

    	lightButton2.setOnClickListener(new OnClickListener() 
    	{
    		wiflyConnect l_wifly = new wiflyConnect( SavedIp, SavedPort );
    		
    		public void onClick(View v) 
    		{
    				
    				try
    				{
    					l_wifly.connect();    					
    				}
    				catch ( java.net.SocketTimeoutException e)
    				{
    					System.out.println( iv_android + "connection failure: socket timeout" );
    			
        				Toast.makeText( MainActivity.this , "Connection failure, please check your option settings", Toast.LENGTH_SHORT).show();
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
    								l_wifly.SendAll(9);
    								
    							    // if we have gotten to this point everything should have worked!!

    							    MainActivity.this.mHandler.post(new Runnable()
    							    {
    							    	public void run() 
    							    	{
    							    		// check on door 1
    							    		if ( l_wifly.Sensor1() > 100 )
    							    		{
    							    			// sensor says we are closed
    							    			if (door1Toggle.getText().equals("open"))
    							    			{
    							    				image1.setImageResource(R.drawable.garage_closed);
    							    				door1Toggle.setChecked(false);
    							    			}

    							    		}
    							    		else
    							    		{
    							    			// sensor says we are open
    							    			if (door1Toggle.getText().equals("close"))
    							    			{
    							    				image1.setImageResource(R.drawable.garage_opened);
    							    				door1Toggle.setChecked(true);
    							    			}
    							    		}

    							    		// check on door 2
    							    		if ( l_wifly.Sensor2() > 100 )
    							    		{
    							    			// sensor says we are closed
    							    			if (door2Toggle.getText().equals("open"))
    							    			{
    							    				image3.setImageResource(R.drawable.garage_closed);
    							    				door2Toggle.setChecked(false);
    							    			}

    							    		}
    							    		else
    							    		{
    							    			// sensor says we are open
    							    			if (door2Toggle.getText().equals("close"))
    							    			{
    							    				image3.setImageResource(R.drawable.garage_opened);
    							    				door2Toggle.setChecked(true);
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
       	SetupButton = (Button) findViewById(R.id.SetupButton);
     	
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
       		SetupButton.setVisibility(View.GONE);
       	}
       	
    	SetupButton.setOnClickListener(new OnClickListener() 
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
       	OptionsButton = (Button) findViewById(R.id.OptionsButton);       	
      	
    	OptionsButton.setOnClickListener(new OnClickListener() 
    	{
      		public void onClick(View v) 
    		{
      			startActivity(new Intent( v.getContext(), Options.class));
    		}
    	});
   } 


   
}
