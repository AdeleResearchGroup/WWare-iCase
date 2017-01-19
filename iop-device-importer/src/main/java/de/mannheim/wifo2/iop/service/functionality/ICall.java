package de.mannheim.wifo2.iop.service.functionality;

import java.util.List;

//import de.mannheim.wifo2.iop.identifier.IServiceID;

public interface ICall {
//	public IServiceID getSource();
//	public IServiceID getTarget();
	public String getSignature();
	public List<IParameter> getParameters();
	public Class<? extends Object> getReturnType();
	public boolean expectsResult();
}
