package fr.liglab.adele.interop.services.temperature;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;

public @ContextService interface HeatersService extends ServiceLayer {
    @State
    String STATE_CHANGE = "state.change";
    @State
    String ZONE_ATTACHED="zone.attached";

    boolean getCurrentState();
    double getPowerLevel();
    void setPowerLevel(double powerLevel);
}