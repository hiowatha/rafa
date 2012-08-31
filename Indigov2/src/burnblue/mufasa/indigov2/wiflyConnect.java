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
	private int iv_sensor2 = 0;
	private int iv_connectionTimeOut = 250;
   
	private String iv_handshakeResponse ="CMD\r\n\r\n\r\n<2.32> "; 
	private String iv_cmdResponse="\n\r\nAOK\r\n<2.32> ";	
	
	public enum DoorStatus { OPEN, CLOSED };
	
	private String [][] iv_togglePin = 
	{
		{"",""},																// [0] ->
		{"",""},																// [1] ->
		{"",""},																// [2] ->
		{"",""},																// [3] ->
		{"set sys output 0x0100 0x0100 \r", "set sys output 0x0000 0x0100 \r"}, // [4] -> module pin 4
		{"",""},																// [5] ->
		{"",""},																// [6] ->
		{"set sys output 0x0080 0x0080 \r", "set sys output 0x0000 0x0080 \r"}, // [7] -> module pin 7 (garage door 1)
		{"",""},																// [8] -> 
		{"set sys output 0x0002 0x0002 \r", "set sys output 0x0000 0x0002 \r"}, // [9] -> module pin 9 (garage light 2)
		{"",""},																// [10]->
		{"set sys output 0x4000 0x4000 \r", "set sys output 0x0000 0x4000 \r"}, // [11]-> module pin 11(garage door 2)
		{"",""},																// [12]->
		{"",""},																// [13]->
		{"",""},																// [14]->
		{"",""},																// [15]->
		{"",""},																// [16]->
		{"",""},																// [17]->
		{"",""},																// [18]->
		{"set sys output 0x0008 0x0008 \r", "set sys output 0x0000 0x0008 \r"}	// [19]-> module pin 19
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

    // get wifly prompt (pre: after handshake or after sending a commands)
    public String getPrompt( ) throws IOException
    {
    	String l_rc = new String();
    	StringBuffer l_sb = new StringBuffer();
    	int l_len = 0;
    	
    	do 
    	{
    		l_len = iv_in.read();
    		//System.out.println(MainActivity.iv_wifly + "character: "+ l_len );
    		String s = Character.toString((char)l_len);			
    		l_sb.append( s );			
    		
    		if ( (l_sb.length() > 2) && 
    				(l_sb.substring(l_sb.length()-2)).equals("> ") )
    			break;

    	} while( true );
    	
    	l_rc = l_sb.toString();
    	
    	//System.out.println(MainActivity.iv_wifly + l_sb);
    	System.out.println(MainActivity.iv_android + "Prompt acquired");
    	
    	return l_rc;
    }
    
    // handshake with wifly (must be done prior to any real commands being sent)
    public void handshake( ) throws IOException
    {
		iv_in = iv_telnet.getInputStream();			
		iv_out = new PrintStream(iv_telnet.getOutputStream());
    	
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
    	
    	String l_resp = getPrompt();
    	
    	if ( iv_handshakeResponse.equals(l_resp) )
    		System.out.println(MainActivity.iv_android + "Handshake response acquired!");
    	
    	System.out.println(MainActivity.iv_android + "HandShake Done");
    }

    // readSensor1 (must be done after handshake)
    // input pin will read as such 8xxxxx,
    // ex output: show q 2 \r\n\r\n82262a,\r\n<2.32> 
    public void readSensor1( ) throws IOException
    {
    	int l_idx = 0;
    	int l_idx2 = 0;
    	
    	System.out.println(MainActivity.iv_android + "Sending:" + iv_readPins[2]);
    	
    	// send the command we want
		iv_out.println(iv_readPins[2]);
		iv_out.flush();		

    	// get prompt
    	String l_resp = getPrompt();
    	
    	l_idx = l_resp.indexOf("8");
    	l_idx2= l_resp.indexOf(",");
    	
    	String l_tmp = l_resp.substring(l_idx+1, l_idx2);
  	  	
    	iv_sensor1 = Integer.parseInt(l_tmp, 16) / 1000;    	
    }
    
    // readSensor2 (must be done after handshake)
    public void readSensor2( ) throws IOException
    {
    	int l_idx = 0;
    	int l_idx2 = 0;
    	
    	System.out.println(MainActivity.iv_android + "Sending:" + iv_readPins[5]);
    	
    	// send the command we want
		iv_out.println(iv_readPins[5]);
		iv_out.flush();		

    	// get prompt
		String l_resp = getPrompt();
    	
    	l_idx = l_resp.indexOf("8");
    	l_idx2= l_resp.indexOf(",");
    	
    	String l_tmp = l_resp.substring(l_idx+1, l_idx2);
  	  	
    	iv_sensor2 = Integer.parseInt(l_tmp, 16) / 1000;    	
    }
    
    
    public DoorStatus doorSensor1()
    {
    	DoorStatus l_rc = DoorStatus.OPEN;
    	if ( iv_sensor1 > 100 )
    		l_rc = DoorStatus.CLOSED;
    	return l_rc;
    }
        
    public DoorStatus doorSensor2()
    {
    	DoorStatus l_rc = DoorStatus.OPEN;
    	if ( iv_sensor2 > 100 )
    		l_rc = DoorStatus.CLOSED;
    	return l_rc;
    }
    
    // send a command to wifly module (use only after handshake, and follow it with a getPrompt)
    // NOTE: all commands should end with \r!
    public boolean sendCommand( String i_cmd ) throws IOException
    {
    	boolean l_rc = false;
		System.out.println(MainActivity.iv_android + "Sending:" + i_cmd);

		do
		{
			iv_out.println(i_cmd);
			iv_out.flush();		
		
			if (!iv_out.checkError())
				l_rc = true;
			
			String l_tmp = new String( i_cmd );
			String l_resp = getPrompt();
			
			l_tmp.concat(iv_cmdResponse);
			if ( l_tmp.equals(l_resp) )
	    		System.out.println(MainActivity.iv_android + "Command Response acquired!");
			
			
		} while ( false );
		
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
    	handshake();

    	// send command to pulse pin 4 on
    	sendCommand( iv_togglePin[i_modulePinIndex][0] );

    	// Note: wifly requires at least some time to 
    	// saturate output pins to high 
    	delayBetweenSends(100);

    	// send command to pulse pin 4 off
    	sendCommand( iv_togglePin[i_modulePinIndex][1] );

    	// fix door if needed
    	readSensor1();
    	readSensor2();

    	// lets disconnect to allow any new connections to work
    	disconnect();
    }
}