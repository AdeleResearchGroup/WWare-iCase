package fr.liglab.adele.interop.services.location;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;

@ContextService
public interface PrecenseService extends ServiceLayer {
    @State
    String STATE_CHANGE = "state.change";
    @State
    String ZONE_ATTACHED="zone.attached";

    boolean getCurrentState();
    String getAttachedZone();
   // public String getchange(String state);
   // String getChanges();
}
