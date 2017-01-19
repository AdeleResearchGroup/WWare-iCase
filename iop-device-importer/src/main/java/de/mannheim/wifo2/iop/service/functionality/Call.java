package de.mannheim.wifo2.iop.service.functionality;

import java.io.Serializable;
import java.util.List;

//import de.mannheim.wifo2.iop.identifier.IServiceID;
import de.mannheim.wifo2.iop.service.functionality.ICall;
import de.mannheim.wifo2.iop.service.functionality.IParameter;

public class Call implements ICall, Serializable {
	private static final long serialVersionUID = 9027866233747566395L;
	
//	private IServiceID mSource;
//	private IServiceID mTarget;
	private String mSignature;
	private List<IParameter> mParameters;
	private Class<? extends Object> mReturnType;

	public Call(//IServiceID source, IServiceID target, 
			String signature, List<IParameter> parameters, 
			Class<? extends Object> returnType)  {
		mSignature = signature;
		mParameters = parameters;
		mReturnType = returnType;
	}
	
//	@Override
//	public IServiceID getSource() {
//		return mSource;
//	}
//
//	@Override
//	public IServiceID getTarget() {
//		return mTarget;
//	}
	
	@Override
	public String getSignature() {
		return mSignature;
	}

	@Override
	public List<IParameter> getParameters() {
		return mParameters;
	}

	@Override
	public Class<? extends Object> getReturnType() {
		return mReturnType;
	}

	@Override
	public boolean expectsResult() {
		return (null == mReturnType) ? false : true;
	}
	
	@Override
	public String toString()  {
		StringBuffer sb = new StringBuffer();
		sb.append(mSignature + " - ");
		for(IParameter p : mParameters)  {
			sb.append(p + ", ");
		}
		return sb.toString();
	}
}
