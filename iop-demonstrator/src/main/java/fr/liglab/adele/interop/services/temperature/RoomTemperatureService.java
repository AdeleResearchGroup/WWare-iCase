package fr.liglab.adele.interop.services.temperature;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;

public @ContextService interface RoomTemperatureService extends ServiceLayer {
    @State
    String SERVICE_STATUS = "service.status";


    String getServiceStatus();
    int getTemp();
    double getPowerLevel();
    void setPowerLevel(double powerLevel);
}
