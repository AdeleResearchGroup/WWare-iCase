package de.mannheim.wifo2.iop.repository.support.exchange;

import java.io.Serializable;
import java.util.List;

public interface IServiceDesc extends Serializable {
	public String getMiddleware();
	public String getName();
	public List<IMethod> getMethods();
	public IInterface getInterface();
}
