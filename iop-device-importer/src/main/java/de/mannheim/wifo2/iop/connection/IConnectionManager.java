package de.mannheim.wifo2.iop.connection;

import java.util.List;

import de.mannheim.wifo2.iop.event.IEvent;
import de.mannheim.wifo2.iop.identifier.IEndpointID;
import de.mannheim.wifo2.iop.plugin.translation.IMessageHandler;

public interface IConnectionManager {
	public final static String ADVERTISEMENT = "advertisement";
	public final static String LOOKUP = "lookup";
	public final static String SERVER = "server";
	public final static String ALL = "all";
	
	public void send(IEvent event);
	public void receive(IEvent data);
	public void addConnection(IEndpointID id, IConnection connection);
	public void deleteConnection(IEndpointID id);
	public List<IConnection> getConnections(IEndpointID id);
	public void stopConnections();
	
	public IMessageHandler getMessageHandler();
}
