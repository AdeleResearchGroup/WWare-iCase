package fr.liglab.adele.interop.services.location;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import fr.liglab.adele.icasa.location.Zone;

import java.util.List;

@ContextService
public interface ZonesService extends ServiceLayer {
    @State
    String SERVICE_STATUS = "service.status";

    @State
    String SERVICE_CHANGE = "service.change";

    String getServiceStatus();

    boolean getServiceState();

    Zone getZone(String zone);

    List<Zone> getZoneList();

}
