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
import fr.liglab.adele.icasa.physical.abstraction.MomentOfTheDay;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Requires;

import java.util.List;
import java.util.function.Supplier;

@ContextEntity(coreServices = {RoomTemperatureService.class, ServiceLayer.class,})

@FunctionalExtension(id="ZoneService",contextServices = ZoneService.class, implementation = ZoneServiceFunctionalExtension.class)
public class RoomTemperatureServiceImpl implements RoomTemperatureService, ServiceLayer{

    @InjectedFunctionalExtension(id="ZoneService")
    ZoneService zone;
    //Service States
    @ContextEntity.State.Field(service = RoomTemperatureService.class,state = SERVICE_STATUS)
    public String status;

    @ContextEntity.State.Field(service = ServiceLayer.class, state = ServiceLayer.NAME)
    public String name;

    @ContextEntity.State.Field(service = ServiceLayer.class,state = ServiceLayer.SERVICE_QOS)
    private int AppQoS;

    private static final Integer MIN_QOS = 50;
    private MomentOfTheDay.PartOfTheDay scheduledPeriod = null;

    //implementation functions


    @Override
    public String getServiceStatus() {
        return status;
    }


    @Override
    public int getTemp() {
        return 0;
    }

    @Override
    public double getPowerLevel() {
        return 0;
    }

    @Override
    public void setPowerLevel(double powerLevel) {

    }

    //requirements
    @Requires(id="heater",optional = false, filter = ZoneService.objectInSameZone,proxy=false, specification = Heater.class)
    @ContextRequirement(spec={LocatedObject.class})
    private List<Heater> heaters;

   /* @Requires(id="thermometer",optional = true,filter = ZoneService.objectInSameZone,proxy=false,specification = Thermometer.class)
    @ContextRequirement(spec={LocatedObject.class})
    private List<Thermometer> thermoteres;*/

    @Requires(id="MoD", optional= false)
    MomentOfTheDay momentOfTheDay;

    //actions
    @Bind(id="heater")
    public void bindHeater(){
        for (Heater ht:heaters) {
            System.out.println(ht);
            ht.setPowerLevel(0.2d);
        }
    }






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
