package de.mannheim.wifo2.iop.service.model;

import java.io.Serializable;
import java.util.Vector;

public interface ICapability extends Serializable  {
	public String getName();
	public Vector<IInput> getInputs();
	public Vector<IOutput> getOutputs();
	public ICategory getCategory();
	public IConversation getConversation();
}
