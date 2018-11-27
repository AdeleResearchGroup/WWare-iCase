package fr.liglab.adele.interop.services.temperature;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;
import java.util.Map;

@ContextService
public interface BalconyThermometerService extends ServiceLayer {
    @State
    String SERVICE_STATUS = "service.status";
    @State
    String SERVICE_CHANGE = "service.change";

    Boolean getServiceStatus();

    int getNumberOfZones();

    String getExternalZoneSensor(String zone);

    String getAsignedThermometer(String zone);

    void removeAsignedThermometer(String zone);

    Map<String, String> getAsignedThermometers();

    Quantity<Temperature> getCurrentTemperature(String thermometer);
}
