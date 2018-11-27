package fr.liglab.adele.interop.demonstrator.home.temperature;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;

@ContextService
public interface RoomTemperatureControl {
    @State
    String APPLICATION_STATE ="application.state";
}
