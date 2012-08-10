package burnblue.mufasa.indigov2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;



public class Options extends Activity 
{
	private Button Button1, Button2;
	public static String fileName = "myData";
	
	public static String provider = "provider";
	public static String propos = "provpos";
	public static String telephone = "telephone";
	public static String textTimer = "textTimer";
	public static String moduleip = "moduleip";
	public static String moduleport = "moduleport";
    SharedPreferences ourData;
	
	
	public void onCreate(Bundle savedInstanceState) 
    {
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.options);
        
        ourData = getSharedPreferences(fileName, 0);
                
        Integer SavedPropos = ourData.getInt(propos,1);
        String SavedTelephone = ourData.getString(telephone, "9563800814");
        String SavedTextTimer = ourData.getString(textTimer, "1200");
        String SavedModuleIp = ourData.getString(moduleip, "192.168.2.2");
        String SavedModulePort = ourData.getString(moduleport, "2000");
        
		Spinner provider = (Spinner) findViewById(R.id.spinner1);
		EditText telephone = (EditText) findViewById(R.id.editText1);
		EditText texttimer = (EditText) findViewById(R.id.editText2);
		EditText moduleip = (EditText) findViewById(R.id.editText3);
		EditText moduleport = (EditText) findViewById(R.id.editText4);
		
        provider.setSelection(SavedPropos);
        telephone.setText(SavedTelephone);
        texttimer.setText(SavedTextTimer);
        moduleip.setText(SavedModuleIp);
        moduleport.setText(SavedModulePort);
        
    	saveButton();
    	cancelButton();
    }
    
   public void saveButton() 
   {
    	Button1 = (Button) findViewById(R.id.button1);
   	
    	Button1.setOnClickListener(new OnClickListener() 
    	{
      		public void onClick(View v) 
    		{
      			Spinner et_provider = (Spinner) findViewById(R.id.spinner1);
      			EditText et_telephone = (EditText) findViewById(R.id.editText1);
      			EditText et_texttimer = (EditText) findViewById(R.id.editText2);
      			EditText et_moduleip = (EditText) findViewById(R.id.editText3);
      			EditText et_moduleport = (EditText) findViewById(R.id.editText4);
      			
      			String  l_provider = et_provider.getSelectedItem().toString();
      			Integer l_propos = et_provider.getSelectedItemPosition();
      			String l_telephone = et_telephone.getText().toString();
      			String l_texttimer = et_texttimer.getText().toString();
      			String l_moduleip = et_moduleip.getText().toString();
      			String l_moduleport = et_moduleport.getText().toString();
      			
      	    	SharedPreferences.Editor editor = ourData.edit();
      	    	
      	    	editor.putString(provider, l_provider);
      	    	editor.putInt(propos, l_propos);      	    	
      	    	editor.putString(telephone, l_telephone);
      	    	editor.putString(textTimer, l_texttimer);
      	    	editor.putString(moduleip, l_moduleip);
      	    	editor.putString(moduleport, l_moduleport);
      	    	
      	    	editor.commit();
      	    	
      	    	System.out.println( MainActivity.iv_android + "saving off provider: " + l_provider );
      	    	System.out.println( MainActivity.iv_android + "saving off provider position: " + l_propos );
      	    	System.out.println( MainActivity.iv_android + "saving off telephone: " + l_telephone );
      	    	System.out.println( MainActivity.iv_android + "saving off text timer: " + l_texttimer );
      	    	System.out.println( MainActivity.iv_android + "saving off module ip: " + l_moduleip );
      	    	System.out.println( MainActivity.iv_android + "saving off module port: " + l_moduleport );
     	    	
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
   
   
   
   
}