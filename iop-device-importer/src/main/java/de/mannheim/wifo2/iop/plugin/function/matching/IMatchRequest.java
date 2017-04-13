package de.mannheim.wifo2.iop.plugin.function.matching;

import java.io.Serializable;

public interface IMatchRequest extends Serializable {
	public static final String FUNCTIONALITY = "functionality";

	public Object getProperty(String key);
}
