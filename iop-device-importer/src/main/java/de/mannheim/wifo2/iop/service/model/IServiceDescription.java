package de.mannheim.wifo2.iop.service.model;

import java.util.List;

import de.mannheim.wifo2.iop.identifier.IServiceID;

public interface IServiceDescription {
	public IServiceID getId();
	public String getName();
//	public List<String> getFunctionality();
	public List<IFunctionality> getFunctionalities();
	public List<IProperty> getProperties();
}
