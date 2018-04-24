package fr.liglab.adele.iop.services.api;

import fr.liglab.adele.cream.annotations.ContextService;

public  @ContextService interface LocationService {
	public String[] getNearbyZones(String location);
}
