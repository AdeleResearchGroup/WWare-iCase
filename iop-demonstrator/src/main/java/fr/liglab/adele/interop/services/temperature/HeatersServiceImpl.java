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
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.interop.demonstrator.home.temperature.RoomTemperatureControlApp;
import org.apache.felix.ipojo.annotations.*;
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
    @ContextEntity.State.Field(service = HeatersService.class, state = ZONE_ATTACHED)
    private String zoneName;
    @ContextEntity.State.Field(service = HeatersService.class, state = HAS_EXT_SENSOR, value = "false")
    private boolean hasExtSensor;

    @ContextEntity.State.Field(service = ServiceLayer.class, state = ServiceLayer.NAME)
    public String name;

    @ContextEntity.State.Field(service = ServiceLayer.class, state = ServiceLayer.SERVICE_QOS, value = "0",directAccess = true)
    private int SrvQoS;

    private static final Integer MIN_QOS = 100;


    //IMPLEMENTATION's FUNCTIONS

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
        return SrvQoS;
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
        SrvQoS = (heaters.size()>=1)?100:0;
        updateState();
    }

    @Unbind(id = "heaters")
    public void unbindHeater() {
        SrvQoS = (heaters.size()>=1)?100:0;
        if (heaters.size() == 0) {
            updateState();
        }
    }

    @Modified(id = "heaters")
    public void modifideheater() {
        SrvQoS = (heaters.size()>=1)?100:0;
        updateState();
    }


    //STATES CHANGE
    @ContextEntity.State.Push(service = HeatersService.class, state = HeatersService.ZONE_ATTACHED)
    public String pushZone(String zoneName) {
        return zoneName;
    }

    @ContextEntity.State.Pull(service = ServiceLayer.class, state = ServiceLayer.SERVICE_QOS)
    private Supplier<Integer> currentQos = () -> {
        int currentQoS = (heaters.size() >=1) ? 0 : 100;
        return currentQoS;
    };

    //@ContextEntity.State.Push(service = ServiceLayer.class, state = ServiceLayer.SERVICE_QOS)
    // public String pushState(String QoS){return QoS;}



    //FUNCTIONS
    private void updateState() {
        int currQoS = 0;
        if (heaters.size() >= 2) {
            currQoS = 100;
        } else if (heaters.size() < 2 && heaters.size() > 0) {
            currQoS = 80;
        }else{
            currQoS = 0;
        }
        //pushState(String.valueOf(currQoS));
    }

}
