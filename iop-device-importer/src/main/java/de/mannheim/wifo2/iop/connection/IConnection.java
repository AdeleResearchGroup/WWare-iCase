package de.mannheim.wifo2.iop.connection;

import de.mannheim.wifo2.iop.event.IEvent;
import de.mannheim.wifo2.iop.plugin.translation.IMessageHandler;

public interface IConnection extends Runnable {
	public boolean send(IEvent event);
	public IEvent receive();
	public void start();
	public void close();
	public IMessageHandler getMessageHandler();
}
