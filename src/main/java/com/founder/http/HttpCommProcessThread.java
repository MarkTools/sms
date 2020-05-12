package com.founder.http;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

/**
 * @author Mark
 */
public class HttpCommProcessThread implements Runnable{
	
	byte[] postMsg;
	private int readLen;
	private boolean isStop = false;
	private boolean readOK = false;
	private HttpURLConnection reqConnection = null;
	private Thread readingThread;
	private byte[] buffer = null;
	private Exception exception = null;

	public HttpCommProcessThread(HttpURLConnection reqConnection, byte[] postMsg) {
		this.reqConnection=reqConnection;
		this.postMsg=postMsg;
	}

	public void run() {
		InputStream input = null;
		OutputStream output = null;

		try{
			reqConnection.connect();
			output = reqConnection.getOutputStream();
			if (postMsg != null && postMsg.length >0) {
				output.write(postMsg);
				output.close();
			}

			int responseCode=reqConnection.getResponseCode();
			if(responseCode==500)
				throw new RuntimeException("C30000,Server 500 error：Requested service response exception");
			
			if(responseCode!=200)
				throw new RuntimeException("HttpCommService failed! responseCode = "+responseCode+" msg=" + reqConnection.getResponseMessage());
			
			input = reqConnection.getInputStream();						
			ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
			int len;				
			byte[] buf = new byte[1024];
			readLen = 0;
			while(!isStop){
				len = input.read(buf,0,1024);
				if (len <= 0) {
					this.readOK = true;
					input.close();
					break;
				}else{
					swapStream.write(buf,0,len);
				}
				readLen += len;
			}				
			buffer=new byte[readLen];
			System.arraycopy(swapStream.toByteArray(), 0, buffer, 0, readLen);
		} catch(Exception e) {
			exception = e;
		} finally {
			try{
				reqConnection.disconnect();
				if( input != null )
					input.close();
				if( output != null )
					output.close();

				wakeUp();
			} catch(Exception e) {}
		}
	}

	private synchronized void wakeUp() {
		notifyAll();
	}
	
	public void startUp() {
		this.readingThread = new Thread(this);
		readingThread.setName("HttpCommService reading thread");
		readingThread.start();
	}
	
	public byte[] getMessage() throws Exception {
		
		if( exception != null )
			throw exception;			
		if (!readOK) {
			throw  new RuntimeException("Communication timeout") ;
		}			
		if (readLen <= 0) {
			return new byte[0];
		}
		return buffer;
	}
	
	public synchronized void waitForData(int timeout) {
		try {
			wait(timeout);
		} catch (Exception e) {}
			
		if (!readOK) {
			isStop = true;
			try{
				if( readingThread.isAlive() )
					readingThread.interrupt();
			}catch(Exception e){}
		}
	}

}
