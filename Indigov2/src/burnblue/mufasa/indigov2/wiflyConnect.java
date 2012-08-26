package burnblue.mufasa.indigov2;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
import org.apache.commons.net.telnet.TelnetClient;

public class wiflyConnect 
{
	// instance variables
	private String iv_ip = MainActivity.iv_adhoc;	
	private int iv_port = 20000;
	private TelnetClient iv_telnet = new TelnetClient();
	private InputStream iv_in;
	private PrintStream iv_out;
	private int iv_sensor1 = 0;
	//private int iv_sensor2 = 0;
	private int iv_connectionTimeOut = 250;
   
	private String [][] iv_togglePin = 
	{
		{"",""},																// [0] ->
		{"",""},																// [1] ->
		{"",""},																// [2] ->
		{"",""},																// [3] ->
		{"set sys output 0x0100 0x0100 \r", "set sys output 0x0000 0x0100 \r"}, // [4] -> module pin 4 (garage light 1)
		{"",""},																// [5] ->
		{"",""},																// [6] ->
		{"set sys output 0x0080 0x0080 \r", "set sys output 0x0000 0x0080 \r"}, // [7] -> module pin 7 (garage door 1)
		{"",""},																// [8] -> 
		{"set sys output 0x0002 0x0002 \r", "set sys output 0x0000 0x0002 \r"}, // [9] -> module pin 9 (garage light 2)
		{"",""},																// [10]->
		{"set sys output 0x4000 0x4000 \r", "set sys output 0x4000 0x4000 \r"}  // [11]-> module pin 11(garage door 2)
	};
	
	private String [] iv_readPins = 
	{
		"",				// [0] ->
		"",				// [1] ->
		"show q 2 \r",	// [2] -> module pin 2 ( garage door 1 sensor)
		"",				// [3] ->
		"",				// [4] ->
		"show q 5 \r" 	// [5] -> module pin 5 ( garage door 2 sensor) 
	};
	
	public wiflyConnect( String i_ip, int i_port )
	{
		iv_ip = i_ip; 
		iv_port = i_port;
	}
	
	public void connect( ) throws Exception
	{
		// set a small connection timeout so user does
		// not wait a long time 
		iv_telnet.setConnectTimeout( iv_connectionTimeOut );
		iv_telnet.connect( iv_ip, iv_port );
	}
	
	public void setConnectionTimeOut( int i_connectionTimeOut )
	{
		iv_connectionTimeOut = i_connectionTimeOut;
		iv_telnet.setConnectTimeout(iv_connectionTimeOut);
	}
	
	public boolean isConnected()
	{
		return iv_telnet.isConnected();		
	}
	
    // telnet interface disconnect method
    public void disconnect() throws IOException 
	{
		iv_in.close();
		iv_out.close();
		iv_telnet.disconnect();
		
		System.out.println( MainActivity.iv_android + "communication done: ip: " + iv_ip );
	}

    // get wifly prompt (use only after handshake, also needed after sending a command)
    public boolean getPrompt( ) throws IOException
    {
    	boolean l_rc = false;
    	StringBuffer l_sb = new StringBuffer();
    	int l_len = 0;
    	    	
    	do 
    	{
    		l_len = iv_in.read();			
    		String s = Character.toString((char)l_len);			
    		l_sb.append( s );			

    		if ( (l_sb.length() > 2) && 
    				(l_sb.substring(l_sb.length()-2)).equals("> ") )
    		{
    			l_rc = true;
    			break;
    		}

    	} while( true );
    	
    	System.out.println(MainActivity.iv_wifly + l_sb);
    	System.out.println(MainActivity.iv_android + "Prompt acquired, wifly is ready to receive");
    	
    	return l_rc;
    }
    
    // handshake with wifly (must be done prior to any real commands being sent)
    public boolean handshake( ) throws IOException
    {
		iv_in = iv_telnet.getInputStream();			
		iv_out = new PrintStream(iv_telnet.getOutputStream());
    	
    	boolean l_rc = false;
    	
    	StringBuffer l_sb = new StringBuffer();
    	int l_len = 0;
    	
    	System.out.println( MainActivity.iv_android + "Starting handshake: ip: " + iv_ip );						

    	do  
    	{
    		l_len = iv_in.read();
    		String s = Character.toString((char)l_len);								
    		l_sb.append( s );								

    	} while ( !l_sb.toString().equals("*HELLO*") );

    	
    	System.out.println( MainActivity.iv_android + "Received: " + l_sb );

    	iv_out.println("$$$");
    	iv_out.flush();

    	iv_out.println("\r");
    	iv_out.flush();

    	if ( (l_sb.toString().equals("*HELLO*")) && !iv_out.checkError() )
    		l_rc = true;
    	
    	System.out.println(MainActivity.iv_android + "HandShake Done");
    	
    	return l_rc;
    }

    // readSensor1 (must be done after handshake)
    public int readSensor1( ) throws IOException
    {
    	int l_idx = 0;
    	
    	// send the command we want
    	sendCommand( iv_readPins[2] );

    	// get prompt
    	StringBuffer l_sb = new StringBuffer();
    	int l_len = 0;
    	    	
    	do 
    	{
    		l_len = iv_in.read();			
    		String s = Character.toString((char)l_len);			
    		l_sb.append( s );			

    		if ( (l_sb.length() > 2) && 
    				(l_sb.substring(l_sb.length()-2)).equals("> ") )
    			break;

    	} while( true );
    	
    	//System.out.println(iv_wifly + l_sb);
    	
    	l_idx = l_sb.indexOf("8");
    	
    	//System.out.println(iv_wifly + "length:" + l_sb.length() + "lastindex:" + l_idx );
    	
    	String l_tmp = l_sb.substring(l_idx+1, l_idx+6);
    	
    	//System.out.println(iv_wifly + "lbuf:" + l_tmp  );  	  	
  	  	
    	return (Integer.parseInt(l_tmp, 16) / 1000 );
    }
    
    // readSensor2 (must be done after handshake)
    public int readSensor2( ) throws IOException
    {
    	int l_idx = 0;
    	
    	// send the command we want
    	sendCommand( iv_readPins[5] );

    	// get prompt
    	StringBuffer l_sb = new StringBuffer();
    	int l_len = 0;
    	    	
    	do 
    	{
    		l_len = iv_in.read();			
    		String s = Character.toString((char)l_len);			
    		l_sb.append( s );			

    		if ( (l_sb.length() > 2) && 
    				(l_sb.substring(l_sb.length()-2)).equals("> ") )
    			break;

    	} while( true );
    	
    	//System.out.println(iv_wifly + l_sb);
    	
    	l_idx = l_sb.indexOf("8");
    	
    	//System.out.println(iv_wifly + "length:" + l_sb.length() + "lastindex:" + l_idx );
    	
    	String l_tmp = l_sb.substring(l_idx+1, l_idx+6);
    	
    	//System.out.println(iv_wifly + "lbuf:" + l_tmp  );  	  	
  	  	
    	return (Integer.parseInt(l_tmp, 16) / 1000 );
    }
    
    
    public int Sensor1()
    {
    	return iv_sensor1;
    }
    
    // send a command to wifly module (use only after handshake, and follow it with a getPrompt)
    // NOTE: all commands should end with \r!
    public boolean sendCommand( String i_cmd )
    {
    	boolean l_rc = false;
		System.out.println(MainActivity.iv_android + "Sending:" + i_cmd);

		iv_out.println(i_cmd);
		iv_out.flush();		
		
		if (!iv_out.checkError())
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
    
   
    public void SendAll( int i_modulePinIndex ) throws IOException
    {
    	// handshake with wifly module
    	//handshake( l_in, l_out);
    	handshake();

    	// get prompt from wifly module
    	//getPrompt( l_in );
    	getPrompt();

    	// send command to pulse pin 4 on
    	//sendCommand( modulePin4on, l_out);
    	sendCommand( iv_togglePin[i_modulePinIndex][0] );

    	// get prompt from wifly module    								
    	//getPrompt( l_in );
    	getPrompt();

    	// Note: wifly requires at least some time to 
    	// saturate output pins to high 
    	//delayBetweenSends(100);
    	delayBetweenSends(100);

    	// send command to pulse pin 4 off
    	//sendCommand( modulePin4off, l_out );
    	sendCommand( iv_togglePin[i_modulePinIndex][1] );

    	// get prompt from wifly module    								
    	//getPrompt( l_in );
    	getPrompt();

    	// fix door if needed
    	iv_sensor1 = readSensor1();
    	//final int l_sensor2 = readSensor2( l_in, l_out );

    	// lets disconnect to allow any new connections to work
    	//disconnect( l_telnet, l_in, l_out);
    	disconnect();
    }
}