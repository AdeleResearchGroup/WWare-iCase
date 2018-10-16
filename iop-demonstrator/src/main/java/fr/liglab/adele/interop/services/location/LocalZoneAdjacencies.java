package fr.liglab.adele.interop.services.location;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import fr.liglab.adele.icasa.location.Zone;

import java.util.List;


public @ContextService interface LocalZoneAdjacencies extends ServiceLayer {
List<Zone> getAdjacencies();
}
