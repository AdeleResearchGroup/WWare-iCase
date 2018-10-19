package fr.liglab.adele.interop.services.temperature;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;

@ContextService
public interface ExternalThermometerService extends ServiceLayer {
    @State
    String STATE_CHANGE = "state.change";
    @State
    String CONNECTION_STATUS = "connection.status";
    @State
    String REQUEST_MADE = "request.made";

    boolean getCurrentState();
    boolean getConnectionStatus();
    void setConnection(String[] Request);
    Quantity<Temperature> getCurrentTemperature();
    int isThermometerPresent();
}
