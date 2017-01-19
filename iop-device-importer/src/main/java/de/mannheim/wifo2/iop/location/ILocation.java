package de.mannheim.wifo2.iop.location;

import java.io.Serializable;

public interface ILocation extends Serializable {
	public String getProtocol();
	public String getAddress();
	public Integer getPort();
	public String getPath();
}
