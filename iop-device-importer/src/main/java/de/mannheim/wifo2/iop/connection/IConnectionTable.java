package de.mannheim.wifo2.iop.connection;

import java.util.List;

import de.mannheim.wifo2.iop.identifier.IEndpointID;

public interface IConnectionTable {
	public void addEntry(String id, String location, IConnection connection);
	public void deleteEntry(String id);
	public void deleteEntry(String id, boolean close);
	public List<IConnection> getConnection(IEndpointID id);
	public List<IConnection> getConnections();
	public void reset(boolean close);
}
