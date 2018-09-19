package fr.liglab.adele.interop.services.location;


import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import fr.liglab.adele.icasa.layering.services.location.ZoneService;
import fr.liglab.adele.icasa.layering.services.location.ZoneServiceFunctionalExtension;
import fr.liglab.adele.icasa.location.Zone;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;

import java.util.List;

@ContextEntity(coreServices = {LocalZoneAdjacencies.class,ServiceLayer.class})
@FunctionalExtension(id="ZoneService",contextServices=ZoneService.class,implementation = ZoneServiceFunctionalExtension.class)

public class LocalZoneAdjacenciesImpl implements LocalZoneAdjacencies,ServiceLayer{
     @Requires(id="zones",optional = false, proxy=false,specification = Zone.class)
     private List<Zone> zones;

    @Bind(id="zones")
    public void bindZone(Zone zone){
        System.out.println(zone.getZoneName());
        System.out.println(zone.getXLength());
        System.out.println(zone.getLeftTopAbsolutePosition());
        System.out.println(zone.getRightBottomAbsolutePosition());
    }
    @Unbind(id="zones")
    public void unbindZone(Zone zone){
        System.out.println("zone dismissed...");
    }

    @Override
    public int getMinQos() {
        return 0;
    }

    @Override
    public int getServiceQoS() {
        return 0;
    }

    @Override
    public String getServiceName() {
        return ServiceLayer.NAME;
    }
}
