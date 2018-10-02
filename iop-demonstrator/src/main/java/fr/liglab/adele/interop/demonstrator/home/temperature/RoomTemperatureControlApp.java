package fr.liglab.adele.interop.demonstrator.home.temperature;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;

import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;
import fr.liglab.adele.cream.annotations.functional.extension.InjectedFunctionalExtension;
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

    @InjectedFunctionalExtension(id="ZoneService")
    ZoneService zone;


    @Requires(id="roomadjacencyservices",specification = LocalZoneAdjacencies.class,optional = true)
    @ContextRequirement(spec={ZoneService.class})
    private LocalZoneAdjacencies adjacencies;

    @Requires(id="zones", specification = Zone.class, optional = true)
    private List<Zone> zones;


  /* @ServiceProperty(name="osgi.command.scope", value ="room-temperature")
    String commandScope;

    @ServiceProperty(name="osgi.command.function", value="{}")
    String[] m_function = new String[] {"schedule"};*/

    //private @Creator.Field(ZoneService.RELATION_ATTACHED_TO) Creator.Relation<ZoneService,Zone> attacher;

    //private @Creator.Field Creator.Entity<RoomTemperatureServiceImpl> serviceCreator;

   /* @Bind(id="zones", aggregate = true)
    public void  bindZone(Zone zone){
        zone.getZoneName();
        if (zones.size()==0){
            serviceCreator.create("adjacencySrv");
        }
    }*/

   // private @Creator.Field(ZoneService.RELATION_ATTACHED_TO) Creator.Relation<ZoneService,Zone> attacher;

    public String getLocation() {
        return zone.getZone();
    }

}
