package fr.liglab.adele.interop.services.temperature;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;

@ContextService
public interface LearnedHeaterBehavior extends ServiceLayer {
    @State
    String SERVICE_STATUS = "service.status";
    @State
    String SERVICE_CHANGE = "service.change";

    String getServiceStatus();

    Double getHeaterPorcentage(Double ReferenceTemperature, String zone);
}
