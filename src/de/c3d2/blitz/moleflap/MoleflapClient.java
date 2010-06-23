package de.c3d2.blitz.moleflap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

public class MoleflapClient extends Activity implements OnClickListener {
	static final int DIALOG_NET_FAIL = 0;
	static final int DIALOG_IMPORTED = 1;
	
	private Dialog createAlertDialog(String msg, String btnmsg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(msg);;
		builder.setNeutralButton("Yes, Sir!", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int arg1) {
				dialog.dismiss();
			}			
		});
		return builder.create();
	}
	
	
    /* (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		switch (id) {
		case DIALOG_NET_FAIL:
			return createAlertDialog("Network error. Check your settings.", "Yes, Sir!");
		case DIALOG_IMPORTED:
			return createAlertDialog("Imported token from card.", "Okay");
		default:
			return null;
		}		
	}

		
	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		checkTokenFile();		
	}


	private void checkTokenFile() {
		// TODO Auto-generated method stub
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		String token = p.getString("token", null);
		if (token == null) {
			// TODO Find token file on card and import token.
		
		}
	}



	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.main);
        
        
        
        
        findViewById(R.id.open_btn).setOnClickListener(this);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.options_menu, menu);
		return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.quit:
			finish();
			return true;
		case R.id.settings:			
			Intent intent = new Intent();
			intent.setClassName("de.c3d2.blitz.moleflap", "de.c3d2.blitz.moleflap.Preferences");			
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);	
		}	
	}

	private String openDoor(String baseurl, String token) throws IOException {
        URL url = new URL( baseurl + URLEncoder.encode(token) );
        
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String str = in.readLine();
        
        if (str.length() != 64)
        	throw new IOException();
        
        return str;
	}
	
	private void openDoor() {
		
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		String url   = p.getString("url", null);
		String token = p.getString("token", null);
		
		try {
			p.edit().putString("token", openDoor(url, token));
		} catch (IOException e) {
			showDialog(DIALOG_NET_FAIL);
		}
	}
	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.open_btn:
			openDoor();
			break;
		default:
			// Do nothing.
		}		
	}
}