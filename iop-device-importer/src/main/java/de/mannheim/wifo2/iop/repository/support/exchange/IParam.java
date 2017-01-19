package de.mannheim.wifo2.iop.repository.support.exchange;

import java.io.Serializable;

public interface IParam extends Serializable {
	public String getName();
	public String getType();
	public String getMapping();
	public String getMetaInfo();
}
