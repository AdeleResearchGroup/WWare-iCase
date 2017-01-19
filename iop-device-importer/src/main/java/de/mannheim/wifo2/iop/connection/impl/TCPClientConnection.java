package de.mannheim.wifo2.iop.connection.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import de.mannheim.wifo2.iop.connection.AConnection;
import de.mannheim.wifo2.iop.connection.IConnectionManager;
import de.mannheim.wifo2.iop.eventing.IEvent;
import de.mannheim.wifo2.iop.translation.IMessageHandler;
import de.mannheim.wifo2.iop.util.debug.DebugConstants;
import de.mannheim.wifo2.iop.util.debug.Log;

/**
 * This client connection uses object streams for transmission
 * (ObjectOutputStream, ObjectInputStream).
 * @author Max
 *
 */
public class TCPClientConnection extends AConnection  {
	private Socket mSocket;
	private ObjectOutputStream mOos;
	private ObjectInputStream mOis;
	
	public TCPClientConnection(IConnectionManager connManager, 
			String address, int port) {
		super(connManager);
		try {
			mSocket = new Socket(address, port);
			mSocket.setTcpNoDelay(true);
			mOos = new ObjectOutputStream(mSocket.getOutputStream());
			mOis = new ObjectInputStream(mSocket.getInputStream());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public TCPClientConnection(IConnectionManager connManager, 
			Socket socket) {
		super(connManager);
		mSocket = socket;
		
		try {
			mSocket.setTcpNoDelay(true);
			mOos = new ObjectOutputStream(mSocket.getOutputStream());
			mOis = new ObjectInputStream(mSocket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		this.setClosed(false);
		
		while(!isClosed())  {
			if(mSocket != null)  {
				IEvent result = receive();
				
				if(result != null)  {	
					this.getConnectionManager().receive(result);
				}
				else  {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public boolean send(IEvent event) {
		if(this.mSocket == null)  {
			if (DebugConstants.CONNECTION) 
				Log.log(getClass(), "Socket is null");
			
			return false;
		}
		
		Object data = this.getMessageHandler().processEvent(event);
		
		try {
			mOos.writeObject(data);
			mOos.flush();

			if(DebugConstants.EVALUATION)  {
				Log.log(getClass(), "message sent: \t" + System.currentTimeMillis());
				Log.log(getClass(), event.getType()+"");
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (DebugConstants.CONNECTION) 
			Log.log(getClass(), "Error while sending message");
		
		return false;
	}

	@Override
	public IEvent receive()  {
		try {
			if(!mSocket.isClosed())  {
				if(mOis != null)  {
					Object o = mOis.readObject();

					if(DebugConstants.EVALUATION)  
						Log.log(getClass(), "message received: \t" + System.currentTimeMillis());
					
					if(DebugConstants.CONNECTION)
						Log.log(getClass(), "Connection: Received data from " 
								+ mSocket.getInetAddress().getHostAddress()
								+ ":" + mSocket.getPort());
					
					IEvent event = this.getMessageHandler().processData(o, IMessageHandler.TYPE_UNKNOWN);
					
					if(event != null) 
						if(DebugConstants.EVALUATION)  
							Log.log(getClass(), event.getType()+"");
						
					return event; 
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			//e.printStackTrace();
		} 
		
		return null;
	}

	@Override
	public void close() {
		try {
			this.setClosed(true);
			this.mSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
