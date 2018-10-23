package fr.liglab.adele.interop.services.temperature;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;

@ContextService
public interface BalconyThermometerService extends ServiceLayer {
    @State
    String SERVICE_STATUS = "service.status";

    boolean getServiceStatus();
    String getExternalZoneSensor(String zone);
    Quantity<Temperature> getCurrentTemperature(String thermoRef);
}
