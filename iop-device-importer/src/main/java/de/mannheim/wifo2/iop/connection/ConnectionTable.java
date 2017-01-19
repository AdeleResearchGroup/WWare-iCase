package de.mannheim.wifo2.iop.connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import de.mannheim.wifo2.iop.connection.IConnection;
import de.mannheim.wifo2.iop.connection.IConnectionManager;
import de.mannheim.wifo2.iop.connection.IConnectionTable;
import de.mannheim.wifo2.iop.identifier.IEndpointID;
import de.mannheim.wifo2.iop.identifier.SystemID;
import de.mannheim.wifo2.iop.util.debug.DebugConstants;
import de.mannheim.wifo2.iop.util.debug.Log;

public class ConnectionTable implements IConnectionTable {
	private Vector<IConnection> iconnections;
	private Vector<String> ids;
	private Vector<String> locations;
	
	public ConnectionTable()  {
		iconnections = new Vector<>();
		ids = new Vector<>();
		locations = new Vector<>();
	}
	
	public synchronized void addEntry(String id, String location, IConnection connection)  {
		if(id != null)  {
			for(int i=0 ; i<ids.size() ; i++)  {
				if(null != ids.get(i) && ids.get(i).equals(id))  {
					return;
				}
			}
		}
		if(location != null)  {
			for(int i=0 ; i<locations.size() ; i++)  {
				if(null != locations.get(i) && locations.get(i).equals(location))  {
					ids.set(i, id);
					
					if(DebugConstants.CONNECTION) 
						Log.log(getClass(), "Changed connection id: " + id + "(" + location + ")");
					
					return;
				}
			}
		}
		
		iconnections.add(connection);
		ids.add(id);
		locations.add(location);
		
		if(DebugConstants.CONNECTION)
			Log.log(getClass(), "Added IConnection(" + id + ", " + location + ")");
	}
	
	@Override
	public void reset(boolean close) {
		List<String> entries = new ArrayList<>(ids);
		for (String entry : entries) {
			deleteEntry(entry,close);
		}
	}
	
	public synchronized void deleteEntry(String id)  {
		deleteEntry(id, true);
	}
	
	public synchronized void deleteEntry(String id, boolean close)  {
		int index = ids.indexOf(id);
		if(index != -1)  {				
			ids.remove(index);
			if(close)  {
				iconnections.get(index).close();
			}
			iconnections.remove(index);
			locations.remove(index);
		}
		else  {
			return;
		}
		
		if(DebugConstants.CONNECTION)
			Log.log(getClass(), "Deleted entry(" + id + ")");
	}
	
	public synchronized List<IConnection> getConnection(IEndpointID id)  {
		List<IConnection> connections = new ArrayList<>();
		
		if(id.getID().equals(new SystemID(IConnectionManager.ALL, null).getID()))  {
			for(int i=0 ; i<ids.size() ; i++)  {
				if(!ids.get(i).equals(IConnectionManager.ADVERTISEMENT) && 
						!ids.get(i).equals(IConnectionManager.SERVER))  {
					connections.add(iconnections.get(i));
				}
			}
			
			return connections;
		}
		else  {
			int index = ids.indexOf(id.getID());
			if(index != -1)  {
				connections.add(iconnections.get(index));
				return connections;
			}
	
			index = locations.indexOf(id.getLocation().toString());
			if(index != -1)  {
				ids.set(index, id.getID());
				connections.add(iconnections.get(index));
				return connections;
			}
		}
		
		return connections;
	}
	
	@Override
	public String toString()  {
		StringBuffer sb = new StringBuffer();
		sb.append("ConnectionTable(");
		for(String id : ids)  {
			if(id == null)  {
				sb.append("null, ");
			}
			else  {
				sb.append(id + ", ");
			}
		}
		sb.append(")");
		
		return sb.toString();
	}

	@Override
	public List<IConnection> getConnections() {
		return iconnections;
	}
}
