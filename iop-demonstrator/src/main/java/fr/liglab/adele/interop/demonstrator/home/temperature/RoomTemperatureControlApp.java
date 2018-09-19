package fr.liglab.adele.interop.demonstrator.home.temperature;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;

import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;
import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
import fr.liglab.adele.icasa.layering.applications.api.ApplicationLayer;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import fr.liglab.adele.icasa.layering.services.location.ZoneService;
import fr.liglab.adele.icasa.layering.services.location.ZoneServiceFunctionalExtension;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.physical.abstraction.MomentOfTheDay;
import fr.liglab.adele.interop.services.location.LocalZoneAdjacencies;
import fr.liglab.adele.interop.services.temperature.RoomTemperatureService;
import fr.liglab.adele.interop.services.temperature.RoomTemperatureServiceImpl;
import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.service.command.Descriptor;

//import javax.validation.constraints.Null;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@ContextEntity(coreServices = {ApplicationLayer.class})
//@Provides(specifications = {RoomTemperatureControlApp.class})

@FunctionalExtension(id="ZoneService",contextServices = ZoneService.class, implementation = ZoneServiceFunctionalExtension.class)

public class RoomTemperatureControlApp implements ApplicationLayer{



    @Requires(id="roomadjacencyservices",specification = LocalZoneAdjacencies.class,optional = true)
    private LocalZoneAdjacencies adjacencies;
    @Requires(id="roomtempservices", specification=RoomTemperatureService.class, optional = true)
    @ContextRequirement(spec={ZoneService.class})
    private List<RoomTemperatureService> services;



    @ServiceProperty(name="osgi.command.scope", value ="room-temperature")
    String commandScope;

    @ServiceProperty(name="osgi.command.function", value="{}")
    String[] m_function = new String[] {"schedule"};

    @Descriptor("Set temperature of the appartment")
    public void scheduleTemp(@Descriptor("zone") String zone, @Descriptor("period") String PeriodName){
        System.out.println("Enabling schedule for temp...");

        MomentOfTheDay.PartOfTheDay period = PeriodName.equals("NONE") ? null : MomentOfTheDay.PartOfTheDay.valueOf(PeriodName);

        for (RoomTemperatureService service: services){
            if ("ALL".equalsIgnoreCase(zone) || ((ZoneService) service).getZone().equals(zone)){
                service.setSchedule(period);
            }
        }
    }

    private @Creator.Field(ZoneService.RELATION_ATTACHED_TO) Creator.Relation<ZoneService,Zone> attacher;

   // private @Creator.Field Creator.Entity<LocalZoneAdjacencies> serviceCreator;



    //@Bind(id="zones", specification = Zone.class, aggregate = true,optional = true)
    //public void binzone(Zone zone){
     //   String instancename = zone.getZoneName()+".tempCtrl";
        /*System.out.println(zone.getZoneName());
        System.out.println(zone.getXLength());
        System.out.println(zone.getLeftTopAbsolutePosition());
        System.out.println(zone.getRightBottomAbsolutePosition());*/
     /*   for (service:
             ) {
            zone.getXLength();
            zone.getYLength();
            zone.getRightBottomAbsolutePosition();
            zone.getRightBottomAbsolutePosition();
        }

        Map<String,Object> properties = new HashMap<>();
        properties.put(ContextEntity.State.id(ServiceLayer.class,ServiceLayer.NAME),instancename);
        serviceCreator.create(instancename,properties);
        attacher.link(instancename,zone);*/
    //}

   // @Unbind(id="zones")
    //public void unbindzone(Zone zone){
       /* String instancename = zone.getZoneName()+"adjacencies";
        serviceCreator.delete(instancename);
        attacher.unlink(instancename,zone);*/
    //}

}
