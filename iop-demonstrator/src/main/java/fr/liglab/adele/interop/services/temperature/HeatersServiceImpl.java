package fr.liglab.adele.interop.services.temperature;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;
import fr.liglab.adele.cream.annotations.functional.extension.InjectedFunctionalExtension;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import fr.liglab.adele.icasa.layering.services.location.ZoneService;
import fr.liglab.adele.icasa.layering.services.location.ZoneServiceFunctionalExtension;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.interop.demonstrator.home.temperature.RoomTemperatureControlApp;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Modified;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Supplier;

@ContextEntity(coreServices = {HeatersService.class, ServiceLayer.class})
@FunctionalExtension(id = "ZoneService", contextServices = ZoneService.class, implementation = ZoneServiceFunctionalExtension.class)

public class HeatersServiceImpl implements HeatersService, ServiceLayer {

    private static final Logger LOG = LoggerFactory.getLogger(RoomTemperatureControlApp.class);

    @InjectedFunctionalExtension(id = "ZoneService")
    ZoneService zone;

    //SERVICE's STATES
    @ContextEntity.State.Field(service = HeatersService.class, state = STATE_CHANGE, value = "false")
    private boolean ServiceStatus;
    @ContextEntity.State.Field(service = HeatersService.class, state = ZONE_ATTACHED)
    private String zoneName;
    @ContextEntity.State.Field(service = HeatersService.class, state = HAS_EXT_SENSOR, value = "false")
    private boolean hasExtSensor;

    @ContextEntity.State.Field(service = ServiceLayer.class, state = ServiceLayer.NAME)
    public String name;

    @ContextEntity.State.Field(service = ServiceLayer.class, state = ServiceLayer.SERVICE_QOS)
    private int AppQoS;

    private static final Integer MIN_QOS = 100;


    //IMPLEMENTATION's FUNCTIONS
    @Override
    public boolean getCurrentState() {
        return ServiceStatus;
    }

    @Override
    public boolean getExtSensorStatus() {
        return hasExtSensor;
    }

    @Override
    public double getPowerLevel() {
        double combinedPower = 0d;
        for (Heater Htr : heaters) {
            combinedPower += Htr.getPowerLevel();
        }
        return combinedPower;
    }

    @Override
    public void setPowerLevel(double powerLevel) {

        for (Heater Htr : heaters) {
            if ((powerLevel / heaters.size()) <= 1d) {
                Htr.setPowerLevel(powerLevel / heaters.size());
            } else {
                LOG.warn("SRV(heat) Maximum Power surpassed, setting at 100%");
                Htr.setPowerLevel(1d);
            }

        }
    }

    @Override
    public int getMinQos() {
        return MIN_QOS;
    }

    @Override
    public int getServiceQoS() {
        return (heaters.size() >= 1) ? 100 : 0;
    }

    @Override
    public String getServiceName() {
        return name;
    }

    //REQUIREMENTS
    @Requires(id = "heaters", optional = false, filter = ZoneService.objectInSameZone, proxy = false, specification = Heater.class)
    @ContextRequirement(spec = {LocatedObject.class})
    public List<Heater> heaters;


    //CREATORS

    //ACTIONS
    @Bind(id = "heaters")
    public void bindHeater() {
        updateState();
    }

    @Unbind(id = "heaters")
    public void unbindHeater() {
        if (heaters.size() == 0) {
            updateState();

        }
    }

    @Modified(id = "heaters")
    public void modifideheater() {
        updateState();
    }


    //STATES CHANGE
    @ContextEntity.State.Push(service = HeatersService.class, state = HeatersService.ZONE_ATTACHED)
    public String pushZone(String zoneName) {
        return zoneName;
    }

    @ContextEntity.State.Push(service = HeatersService.class, state = HeatersService.STATE_CHANGE)
    public boolean pushService(boolean ServiceStatus) {
        return ServiceStatus;
    }

    @ContextEntity.State.Pull(service = ServiceLayer.class, state = ServiceLayer.SERVICE_QOS)
    private Supplier<Integer> currentQos = () -> {
        int currentQoS = 0;
        if (heaters.size() >= 2) {
            currentQoS = 100;
        } else if (heaters.size() < 2) {
            currentQoS = 80;
        }
        return currentQoS;
    };


    //FUNCTIONS
    private void updateState() {
        if (getCurrentState()) {
            pushService(false);
        } else {
            pushService(true);
        }

    }

}
