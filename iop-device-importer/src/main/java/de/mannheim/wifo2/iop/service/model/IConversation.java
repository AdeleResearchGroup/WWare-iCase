package de.mannheim.wifo2.iop.service.model;

import java.io.Serializable;
import java.util.Vector;

public interface IConversation extends Serializable  {
	public Vector<IState> getStates();
	public Vector<ICapability> getSymbols();
	public ITransitionFunction getTransitionFunction();
	public IState getInitialState();
	public Vector<IState> getFinalState();
}
