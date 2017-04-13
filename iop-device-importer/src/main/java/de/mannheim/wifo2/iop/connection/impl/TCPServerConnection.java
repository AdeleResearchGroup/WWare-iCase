package de.mannheim.wifo2.iop.connection.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;

import de.mannheim.wifo2.iop.connection.AConnection;
import de.mannheim.wifo2.iop.connection.IAcceptConnection;
import de.mannheim.wifo2.iop.connection.IConnection;
import de.mannheim.wifo2.iop.connection.IConnectionManager;
import de.mannheim.wifo2.iop.event.IEvent;
import de.mannheim.wifo2.iop.identifier.IEndpointID;
import de.mannheim.wifo2.iop.identifier.impl.DeviceID;
import de.mannheim.wifo2.iop.location.impl.Location;

public class TCPServerConnection extends AConnection  implements IAcceptConnection {
	protected ServerSocket serverSocket;
	protected Class<? extends IConnection> mClass;
	
	public TCPServerConnection(IConnectionManager connManager, 
			Class<? extends IConnection> clazz, int port) {
		super(connManager);
		mClass = clazz;
		try {
			this.serverSocket = new ServerSocket(port);
			this.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		this.setClosed(false);
		
		while(!isClosed())  {
			try {
				if(null != this.serverSocket)  {
					Socket socket = this.serverSocket.accept();
					IConnection connection = null;
					try {
						connection = mClass.getConstructor(IConnectionManager.class, Socket.class)
								.newInstance(getConnectionManager(), socket);
						connection.start();
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException | NoSuchMethodException | SecurityException e) {
						e.printStackTrace();
					}

					String address = socket.getInetAddress().getHostAddress();
					int port = socket.getPort();
					
					IEndpointID deviceID = new DeviceID(null, new Location(null, address, port, null));
					this.getConnectionManager().addConnection(deviceID, connection);
				}
				else  {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e)  {
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean send(IEvent event) {
		return false;
	}

	@Override
	public IEvent receive() {			
		return null;
	}

	@Override
	public void close() {
		try {
			this.serverSocket.close();
			this.setClosed(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
