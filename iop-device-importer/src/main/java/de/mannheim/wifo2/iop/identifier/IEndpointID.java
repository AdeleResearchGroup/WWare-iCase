package de.mannheim.wifo2.iop.identifier;

import de.mannheim.wifo2.iop.location.ILocation;

public interface IEndpointID extends IID {
	public ILocation getLocation();
}
