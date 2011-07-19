package de.c3d2.blitz.moleflap;

import java.net.URL;

class OpenDoorRequest {
	public final Token token;
	public final URL   postUrl;

	public OpenDoorRequest(Token token, URL postUrl) {
		this.token   = token;
		this.postUrl = postUrl;
	}
}
