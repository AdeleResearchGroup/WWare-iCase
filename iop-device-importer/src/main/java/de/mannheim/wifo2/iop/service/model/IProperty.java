package de.mannheim.wifo2.iop.service.model;

import java.io.Serializable;

public interface IProperty extends Serializable  {
	public static final int TYPE_CONTEXT = 0;
	public static final int TYPE_QUALITATIVE = 1;
	public static final int TYPE_QUANTITATIVE = 2;
	
	public int getType();
	public String getName();
	public IAnnotation getAnnotation();
}
