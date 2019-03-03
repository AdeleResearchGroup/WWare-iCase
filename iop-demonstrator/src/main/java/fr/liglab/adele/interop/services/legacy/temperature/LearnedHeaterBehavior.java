package fr.liglab.adele.interop.services.legacy.temperature;

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

    double getHeaterPorcentage(double reference, double target, String zone, int iterationsPerDay);
}