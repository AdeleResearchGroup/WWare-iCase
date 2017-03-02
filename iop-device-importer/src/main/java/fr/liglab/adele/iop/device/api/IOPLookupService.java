package fr.liglab.adele.iop.device.api;

import fr.liglab.adele.cream.annotations.ContextService;

public @ContextService interface IOPLookupService {

	public void consider(String[] services);

	public void discard(String[] services);
	
	public void all();

	public void none();

}
