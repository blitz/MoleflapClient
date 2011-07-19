package de.c3d2.blitz.moleflap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.util.Log;
import android.widget.Toast;

public class MoleflapClient extends Activity implements OnClickListener {
    static final int DIALOG_NO_TOKEN = 1;

    static final String TAG = "Moleflap";

	private Dialog createAlertDialog(String msg, String btnmsg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);;
        builder.setNeutralButton(btnmsg, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int arg1) {
                    dialog.dismiss();
                }
            });
        return builder.create();
    }


    /* (non-Javadoc)
     * @see android.app.Activity#onCreateDialog(int)
     */
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        switch (id) {
        case DIALOG_NO_TOKEN:
            return createAlertDialog("No valid token. Put one on your card as token.txt and hit Import.", "I'll do that.");
        default:
            return null;
        }
    }


    /* (non-Javadoc)
     * @see android.app.Activity#onStart()
     */
    protected void onStart() {
        super.onStart();
        checkDefaults();
    }

    private void checkDefaults() {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor e = p.edit();

        if (p.getString("url", null) == null) {
            String url = "http://moleflap.hq.c3d2.de/open?";
            Log.i(TAG, "Default URL not set. Setting to: " + url );
            e.putString("url",  url);
        }

        e.commit();
    }

    private File tokenFile()
    {
        return new File(Environment.getExternalStorageDirectory(), "token.txt");
    }

    private void exportTokenFile() {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String token = p.getString("token", null);

        if (token == null) {
            Toast.makeText(getBaseContext(), "You have to import a token, before you can export it.",
                           Toast.LENGTH_LONG).show();
            return;
        }

        String state = Environment.getExternalStorageState();
        
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            try {
                FileWriter fw = new FileWriter(tokenFile());
                BufferedWriter out = new BufferedWriter(fw);
                out.write(token);
                out.newLine();
                out.close();
                fw.close();

                Toast.makeText(getBaseContext(), "Token exported as token.txt.",
                               Toast.LENGTH_LONG).show();
                return;
            } catch (IOException e) {
                Log.e(TAG, "IO Exception during token export: " + e );
            }
        }
        
        Toast.makeText(getBaseContext(), "Could not write token file.",
                       Toast.LENGTH_LONG).show();
    }

    private void checkTokenFile() {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        
      	SharedPreferences.Editor ed = p.edit();
      	char[] x = new char[Token.TOKEN_LENGTH];
        Arrays.fill(x, '0');
        ed.putString("token", new String(x));
        ed.commit();
        if (true) return;
        
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED) ||
            state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            File tokenfile = tokenFile();
            if (tokenfile.exists() && tokenfile.canRead()) {
                try {
                	Token t = Token.fromFile(tokenfile);
                    
                	SharedPreferences.Editor e = p.edit();
                    e.putString("token", t.toString());
                    e.commit();
                    Log.i(TAG, "Token imported!");
                    
                    Toast.makeText(getBaseContext(), "Token successfully imported.", Toast.LENGTH_LONG).show();
                    return;                    
                } catch (IOException e) {
                    Log.e(TAG, "IO Exception during token import: " + e );
                }
            }
        }

        showDialog(DIALOG_NO_TOKEN);
    }



    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        findViewById(R.id.open_btn).setOnClickListener(this);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.quit:
            finish();
            return true;
        case R.id.settings:
            Intent intent = new Intent(this, Preferences.class);
            startActivity(intent);
            return true;
        case R.id.import_token:
            checkTokenFile();
            return true;
        case R.id.export_token:
            exportTokenFile();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void openDoor() {

        final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        final URL url;
		try {
			url = new URL(p.getString("url", null));
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Log.e(TAG, "Post URL is malformed: " + p.getString("url", "<empty>"));
			return;
		}

        final Token token;
        
        try {
        	token = new Token(p.getString("token", ""));        	
        } catch (IllegalArgumentException e) {
            showDialog(DIALOG_NO_TOKEN);
            return;
        }
        
        // Asynchronously open the door and update the token.
        OpenDoorTask t = new OpenDoorTask() {
        	protected void onPostExecute(AsyncTaskResult<Token> token) {
        		if (token.error == null) {
        			SharedPreferences.Editor e = p.edit();
        			e.putString("token", token.result.toString());
        			e.commit();
        			Toast.makeText(getBaseContext(), "Request sent!", Toast.LENGTH_LONG).show();
        		} else {
        			Toast.makeText(getBaseContext(), "Network error: " + token.error.getMessage(),
        					Toast.LENGTH_LONG).show();
        			Log.e(TAG, "Network error: " + token.error);
        		}
        	}
        	
        	protected void onCancelled() {
        		Toast.makeText(getBaseContext(), "Cancelled?", Toast.LENGTH_LONG );
        	}
        };
        
        t.execute(new OpenDoorRequest(token, url));
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
