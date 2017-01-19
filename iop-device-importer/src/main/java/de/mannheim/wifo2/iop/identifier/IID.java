package de.mannheim.wifo2.iop.identifier;

import java.io.Serializable;

public interface IID extends Serializable {
	public final static int ID_SERVICE = 1;
	public final static int ID_DEVICE = 2;
	public final static int ID_PLUGIN = 3;
	public final static int ID_SYSTEM = 4;
	public final static int ID_ADMIN = 5;
	
	public String getID();
	public int getType();
	public IEndpointID getEndpoint();
}
