package de.mannheim.wifo2.iop.service.model.impl;

import java.util.Vector;

import de.mannheim.wifo2.iop.service.model.ICapability;
import de.mannheim.wifo2.iop.service.model.ICategory;
import de.mannheim.wifo2.iop.service.model.IConversation;
import de.mannheim.wifo2.iop.service.model.IInput;
import de.mannheim.wifo2.iop.service.model.IOutput;

public class Capability implements ICapability {
	private static final long serialVersionUID = -6622390359543174496L;
	
	private String mName;
	private Vector<IInput> mInputs;
	private Vector<IOutput> mOutputs;
	private ICategory mCategory;
	private IConversation mConversation;
	
	public Capability(String name)  {
		this(name, null, null, null, null);
	}
	
	public Capability(String name, Vector<IInput> inputs,
			Vector<IOutput> outputs, ICategory category,
			IConversation conversation) {
		mName = name;
		mInputs = inputs;
		mOutputs = outputs;
		mCategory = category;
		mConversation = conversation;
	}
	
	@Override
	public String getName() {
		return mName;
	}

	@Override
	public Vector<IInput> getInputs() {
		return mInputs;
	}

	@Override
	public Vector<IOutput> getOutputs() {
		return mOutputs;
	}

	@Override
	public ICategory getCategory() {
		return mCategory;
	}

	@Override
	public IConversation getConversation() {
		return mConversation;
	}

	@Override
	public String toString()  {
		StringBuffer sb = new StringBuffer();
		sb.append(mName);
		if(null != mInputs)  {
			sb.append(" - Inputs: ");
			for(IInput i : mInputs)  {
				sb.append(i + ", ");
			}
		}
		if(null != mOutputs)  {
			sb.append(" - Outputs: ");
			for(IOutput o : mOutputs)  {
				sb.append(o + ", ");
			}
		}
		
		return sb.toString();
	}
}
