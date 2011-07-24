/**
 * 
 */
package de.c3d2.blitz.moleflap2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

import android.os.AsyncTask;
import android.util.Log;

/**
 * @author julian
 *
 */
public class OpenDoorTask extends AsyncTask<OpenDoorRequest, Integer, AsyncTaskResult<Token>> {
	
	 protected Token openDoor(URL baseurl, Token token) throws IOException {
		 URL url = new URL( baseurl.toString() + "open?" + URLEncoder.encode(token.toString()) );
		 Log.d(MoleflapClient.TAG, "Post token " + token + " to " + url);
		 BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		 
		 String str = in.readLine();
		 Log.d(MoleflapClient.TAG, "Got back " + str + " (" + str.length() + ")");
		 try {
			 Token newToken = new Token(str);
			 return newToken;
		 } catch (IllegalArgumentException e) {
			 Log.e(MoleflapClient.TAG, "Got '" + str + "' from server. Does not seem to be a token.");
			 throw new IOException();
		 }
	 }
	 
	@Override
	protected AsyncTaskResult<Token> doInBackground(OpenDoorRequest... params) {
		try {
			return new AsyncTaskResult<Token>(openDoor(params[0].postUrl, params[0].token));
		} catch (IOException e) {
			return new AsyncTaskResult<Token>(e);
		}
	}	
}
