package de.mannheim.wifo2.iop.repository.support.exchange;

import java.io.Serializable;
import java.util.Vector;

public interface IMethod extends Serializable {
	public String getSignature();
	public void addParameter(IParam param);
	public Vector<IParam> getParameters();
	public String getReturnType();
	public String getMapping();
}
