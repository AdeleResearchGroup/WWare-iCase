package fr.liglab.adele.interop.services.legacy.temperature;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import fr.liglab.adele.interop.demonstrator.applications.legacy.temperature.TemperatureControlApplication;
import fr.liglab.adele.iop.device.api.IOPLookupService;
import fr.liglab.adele.iop.device.api.IOPService;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tec.units.ri.quantity.Quantities;
import tec.units.ri.unit.Units;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@ContextEntity(coreServices = {RemoteThermometerService.class, ServiceLayer.class})
public class RemoteThermometerServiceImpl implements RemoteThermometerService, ServiceLayer {

    private static final Logger LOG = LoggerFactory.getLogger(TemperatureControlApplication.class);


    boolean requestmade = false;


    //SERVICE's STATES
    @ContextEntity.State.Field(service = RemoteThermometerService.class, state = STATE_CHANGE, value = "false")
    private boolean ServiceStatus;

    @ContextEntity.State.Field(service = RemoteThermometerService.class, state = CONNECTION_STATUS, value = "false")
    private boolean connStat;
    @ContextEntity.State.Field(service = RemoteThermometerService.class, state = REQUEST_MADE, value = "false")
    private boolean isRequestmade;

    @ContextEntity.State.Field(service = ServiceLayer.class, state = ServiceLayer.NAME)
    public String name;

    //REQUIREMENTS

    @Requires(optional = false)
    private IOPLookupService lookup;

    @Requires(id = "outThermo", specification = Thermometer.class, optional = true)
    @ContextRequirement(spec = IOPService.class)
    List<Thermometer> thermometers;

    //CREATORS

    //ACTIONS


    @Validate
    public void start() {
        LOG.info("Connection to Xware stablished");
        getConnectionStatus();
    }

    @Bind(id = "outThermo")
    public void bindThermometer(Thermometer thr) {
        updateState();
    }

    @Unbind(id = "outThermo")
    public void unbindThermometer(Thermometer thr) {
        updateState();
    }

    @Modified(id = "outThermo")
    public void thermoMod(Thermometer thr) {
        updateState();
    }

    //STATES CHANGE
    @ContextEntity.State.Push(service = RemoteThermometerService.class, state = RemoteThermometerService.CONNECTION_STATUS)
    public boolean pushconnStat(boolean connStat) {
        return connStat;
    }

    @ContextEntity.State.Push(service = RemoteThermometerService.class, state = RemoteThermometerService.STATE_CHANGE)
    public boolean pushService(boolean ServiceStatus) {
        return ServiceStatus;
    }

    @Override
    public int getQoS() {
        int currentQoS = 0;
        if (thermometers.size() >= 1) {
            currentQoS = 100;
        } else if (thermometers.size() < 1) {
            currentQoS = 90;
        }
        return currentQoS;
    }

    //IMPLEMENTATION's FUNCTIONS
    @Override
    public int isThermometerPresent() {
        if (thermometers != null) {
            return thermometers.size();
        }
        return 0;

    }

    @Override
    public boolean getCurrentState() {
        return ServiceStatus;
    }

    @Override
    public boolean getConnectionStatus() {
        return connStat;
    }


    @Override
    public void setConnection(String[] RequestStr) {
        if (!isRequestmade) {
            LOG.info("Making request to Base", RequestStr);
            connStat = true;
            pushconnStat(true);
            lookup.consider(RequestStr, Collections.emptyMap());
            isRequestmade = true;
        }
    }

    @Override
    public Quantity<Temperature> getCurrentTemperature() {
        Quantity<Temperature> temp = Quantities.getQuantity(0.0, Units.KELVIN);
        for (Thermometer th : thermometers) {
            temp = th.getTemperature();
        }
        return temp;
    }

    @Override
    public String getServiceName() {
        return name;
    }

    //FUNCTIONS
    private void updateState() {
        if (getCurrentState()) {
            pushService(false);
        } else {
            pushService(true);
        }

    }


}
