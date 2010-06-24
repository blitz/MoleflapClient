package de.c3d2.blitz.moleflap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

    static final int TOKEN_LENGTH = 164;

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

    private void checkTokenFile() {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED) ||
            state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            File path = Environment.getExternalStorageDirectory();
            File tokenfile = new File(path, "token.txt");
            if (tokenfile.exists() && tokenfile.canRead()) {
                try {
                    FileReader fr = new FileReader(tokenfile);
                    BufferedReader in = new BufferedReader(fr);
                    String str = in.readLine().trim();
                    in.close();
                    fr.close();

                    if (str.length() == TOKEN_LENGTH) {
                        SharedPreferences.Editor e = p.edit();
                        e.putString("token", str);
                        e.commit();
                        Log.i(TAG, "Token imported!");
                        Toast.makeText(getBaseContext(), "Token successfully imported.",
                                       Toast.LENGTH_LONG).show();
                        return;
                    }
                    // Fall through to dialog.
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
            Intent intent = new Intent();
            intent.setClassName("de.c3d2.blitz.moleflap", "de.c3d2.blitz.moleflap.Preferences");
            startActivity(intent);
            return true;
        case R.id.import_token:
            checkTokenFile();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private String openDoor(String baseurl, String token) throws IOException {
        URL url = new URL( baseurl + URLEncoder.encode(token) );

        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String str = in.readLine();

        if (str.length() != TOKEN_LENGTH) {
            Log.e(TAG, "Got '" + str + "' from server. Does not seem to be a token.");
            throw new IOException();
        }

        return str;
    }

    private void openDoor() {

        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String url   = p.getString("url", null);
        String token = p.getString("token", "");

        if (token.length() != TOKEN_LENGTH) {

            showDialog(DIALOG_NO_TOKEN);
            return;
        }

        try {
            SharedPreferences.Editor e = p.edit();
            e.putString("token", openDoor(url, token));
            e.commit();
            Toast.makeText(getBaseContext(), "Request sent!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Network error: " + e);
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
