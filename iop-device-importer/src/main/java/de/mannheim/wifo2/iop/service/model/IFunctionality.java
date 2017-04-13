package de.mannheim.wifo2.iop.service.model;

import java.io.Serializable;
import java.util.List;

public interface IFunctionality extends Serializable  {
	public String getName();
	public List<ICapability> getCapabilities();
	public List<IProperty> getProperties();
}
