package de.mannheim.wifo2.iop.service.model.impl;

import de.mannheim.wifo2.iop.service.model.ICategory;

public class Category implements ICategory  {
	private static final long serialVersionUID = 1875290884715551530L;
	
	private String mName;

	public Category(String name)  {
		mName = name;
	}
	
	@Override
	public String getName() {
		return mName;
	}
}
