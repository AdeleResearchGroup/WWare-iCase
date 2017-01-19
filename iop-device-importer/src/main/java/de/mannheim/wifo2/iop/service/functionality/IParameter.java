package de.mannheim.wifo2.iop.service.functionality;

public interface IParameter {
	public String getKey();
	public Class<? extends Object> getClazz();
	public Object getValue();
	
	public boolean isMiddlewareSpecific();
	public String getMetaInformation();
}
