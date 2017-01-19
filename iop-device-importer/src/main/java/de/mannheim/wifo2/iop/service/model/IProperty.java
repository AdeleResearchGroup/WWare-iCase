package de.mannheim.wifo2.iop.service.model;

public interface IProperty {
	public static final int TYPE_CONTEXT = 0;
	public static final int TYPE_QUALITATIVE = 1;
	public static final int TYPE_QUANTITATIVE = 2;
	
	public int getType();
	public String getName();
	public IAnnotation getAnnotation();
}
