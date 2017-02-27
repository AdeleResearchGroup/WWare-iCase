package fr.liglab.adele.iop.device.api;

import fr.liglab.adele.cream.annotations.ContextService;

public @ContextService interface IOPLookupService {

	public void lookup(String[] services);
}
