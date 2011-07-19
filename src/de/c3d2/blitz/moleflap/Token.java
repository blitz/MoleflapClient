package de.c3d2.blitz.moleflap;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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
        FileReader fr = new FileReader(file);
        char b[] = new char[TOKEN_LENGTH];
        int len = 0;
        try {
        	len = fr.read(b, 0, TOKEN_LENGTH);
        	if (len != TOKEN_LENGTH)
        		throw new IOException();
        } finally {        	
        	fr.close();
        }
		return new Token(new String(b));
	}
}
