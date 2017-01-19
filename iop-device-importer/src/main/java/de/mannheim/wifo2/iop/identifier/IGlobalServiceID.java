package de.mannheim.wifo2.iop.identifier;

public interface IGlobalServiceID extends IServiceID {
	public ISystemID getSystemID();
	public IPluginID getPluginID();
}
