package de.c3d2.blitz.moleflap2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.util.Log;

class Token {	
    static final int TOKEN_LENGTH = 164;
    
	String data;
	
	public String toString() {
		return data;		
	}
	
	public Token(String tokenString) {
		if (tokenString.length() != TOKEN_LENGTH)
			throw new IllegalArgumentException();
		data = tokenString;
	}
	
	public static Token fromFile(File file) throws IOException {
		Log.d(MoleflapClient.TAG, "Tring to import " + file.toString());
		BufferedReader input =  new BufferedReader(new FileReader(file));
		String line = null;
		int ignored = 0;
		
		while  ((line = input.readLine()) != null) {
			if (line.length() == TOKEN_LENGTH) {
				Log.d(MoleflapClient.TAG, "Succeeded. Ignored " + ignored + " line(s).");
				return new Token(line);
			} else ignored++;			
		}
		
		Log.d(MoleflapClient.TAG, "No valid token found.");
		throw new IOException();
	}
}
