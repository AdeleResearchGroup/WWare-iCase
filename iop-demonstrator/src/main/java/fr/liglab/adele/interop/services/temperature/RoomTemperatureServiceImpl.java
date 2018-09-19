package fr.liglab.adele.interop.services.temperature;


import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import fr.liglab.adele.icasa.layering.services.location.ZoneService;
import fr.liglab.adele.icasa.layering.services.location.ZoneServiceFunctionalExtension;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.physical.abstraction.MomentOfTheDay;
import org.apache.felix.ipojo.annotations.Modified;
import org.apache.felix.ipojo.annotations.Requires;

import java.util.List;
import java.util.function.Supplier;

@ContextEntity(coreServices = {RoomTemperatureService.class, ServiceLayer.class,})

@FunctionalExtension(id="ZoneService",contextServices = ZoneService.class, implementation = ZoneServiceFunctionalExtension.class)
public class RoomTemperatureServiceImpl implements RoomTemperatureService, ServiceLayer{

    private MomentOfTheDay.PartOfTheDay scheduledPeriod = null;

    @Override
    public void setSchedule(MomentOfTheDay.PartOfTheDay period) {
        this.scheduledPeriod = period;
    }

    //reqs
    @Requires(optional = false, filter = ZoneService.objectInSameZone,proxy=false, specification = Heater.class)
    @ContextRequirement(spec={LocatedObject.class})
    private List<Heater> heaters;

    @Requires(id="MoD", optional= false)
    MomentOfTheDay momentOfTheDay;

    //actions
    @Modified(id="MoD")
    protected void momentOfTheDayUpdated(){
        if(momentOfTheDay.getCurrentPartOfTheDay()==scheduledPeriod){
            for(Heater heater:heaters){
                heater.setPowerLevel(70);
            }
        }
        else{
            for(Heater heater:heaters){
                heater.setPowerLevel(0);
            }
        }
    }

    private static final Integer MIN_QOS = 50;

    @ContextEntity.State.Field(service = ServiceLayer.class, state = ServiceLayer.NAME)
    public String name;

    @ContextEntity.State.Field(service = ServiceLayer.class,state = ServiceLayer.SERVICE_QOS)
    private int AppQoS;

    @ContextEntity.State.Pull(service = ServiceLayer.class,state = ServiceLayer.SERVICE_QOS)
    private Supplier<Integer> currentQoS = ()-> {
        int currentQoS = 0;
        if (heaters.size()>0){
            currentQoS=51;
        }
        return currentQoS;
    };



    @Override
    public int getMinQos() {
        return MIN_QOS;
    }

    @Override
    public int getServiceQoS() {
        return AppQoS;
    }

    @Override
    public String getServiceName() {
        return name;
    }
}
