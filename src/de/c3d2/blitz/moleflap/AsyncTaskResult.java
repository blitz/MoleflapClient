package de.c3d2.blitz.moleflap;

class AsyncTaskResult<T> {
	public final T         result;
	public final Exception error;
	
	public AsyncTaskResult(T result) {
		this.result = result;
		this.error  = null;
	}
	
	public AsyncTaskResult(Exception error) {
		this.result = null;
		this.error  = error;
	}
}
