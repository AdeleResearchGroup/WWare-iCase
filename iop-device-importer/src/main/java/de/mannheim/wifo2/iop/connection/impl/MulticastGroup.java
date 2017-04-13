package de.mannheim.wifo2.iop.connection.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import de.mannheim.wifo2.iop.connection.AConnection;
import de.mannheim.wifo2.iop.connection.IConnectionManager;
import de.mannheim.wifo2.iop.event.IEvent;
import de.mannheim.wifo2.iop.plugin.translation.IMessageHandler;

public class MulticastGroup extends AConnection {
	protected MulticastSocket mSocket;
	private InetAddress group;
	private int port;
	
	public MulticastGroup(IConnectionManager connManager, 
			String address, int port) {
		super(connManager);
		try {
			this.port = port;
			this.group = InetAddress.getByName(address);
			this.mSocket = new MulticastSocket(port);
			this.mSocket.setLoopbackMode(false);  //TODO set to true or filter messages from self
			this.mSocket.joinGroup(group);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean send(IEvent event) {
		Object data = this.getMessageHandler().processEvent(event);
		DatagramPacket packet = null;
		byte[] sendData = null;
		if(data.getClass().equals(byte[].class))  {
			sendData = (byte[]) data;
			
		}
		else  {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos;
			try {
				oos = new ObjectOutputStream(baos);
				oos.writeObject(data);
				oos.flush();
				oos.close();
				baos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			sendData = baos.toByteArray();
		}
		
		packet = new DatagramPacket(sendData, sendData.length, group, port);
		
		try {
			if(packet != null)  {
				this.mSocket.send(packet);
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public IEvent receive() {
		DatagramPacket packet;
		byte[] buf = new byte[this.getBufferSize()];
	    packet = new DatagramPacket(buf, buf.length);
	    
	    try {
			this.mSocket.receive(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    return this.getMessageHandler().processData(packet.getData(), IMessageHandler.TYPE_ANNOUNCEMENT);
	}

	@Override
	public void close()  {
		try {
			this.setClosed(true);
			this.mSocket.leaveGroup(group);
			this.mSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		this.setClosed(false);
		
		while(!isClosed())  {
			if(this.mSocket != null)  {
				IEvent event = (IEvent) receive();
				this.getConnectionManager().receive(event);
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
