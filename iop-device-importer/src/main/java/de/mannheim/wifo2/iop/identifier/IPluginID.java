package de.mannheim.wifo2.iop.identifier;

public interface IPluginID extends IComponentID {
	public String getPluginName();
	public IDeviceID getDeviceID();
}
