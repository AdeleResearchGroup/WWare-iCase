package fr.liglab.adele.interop.demonstrator.home.lightning;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
import fr.liglab.adele.icasa.layering.applications.api.ApplicationLayer;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import fr.liglab.adele.icasa.layering.services.location.ZoneService;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.interop.services.location.PrecenseService;
import fr.liglab.adele.interop.services.location.PrecenseServiceImpl;
import org.apache.felix.ipojo.annotations.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@ContextEntity(coreServices = {ApplicationLayer.class})
@Provides(specifications= {LightFollowApplication.class})

public class LightFollowApplication implements ApplicationLayer {
    //SERVICE's STATES

    //IMPLEMENTATION's FUNCTIONS
    public LightFollowApplication() {
	}


    //REQUIREMENTS
    @Requires(id="presenceservices", specification = PrecenseService.class, optional=true)
    @ContextRequirement(spec = {ZoneService.class})
    private List<PrecenseService> services;
    //CREATORS
    private @Creator.Field(ZoneService.RELATION_ATTACHED_TO) Creator.Relation<ZoneService,Zone> attacher;

    private @Creator.Field Creator.Entity<PrecenseServiceImpl>	serviceCreator;

    //ACTIONS
    @Invalidate
    public void stop(){
        for (PrecenseService service:services) {
            serviceCreator.delete(service.getServiceName());
            attacher.unlink(service.getServiceName(),((LocatedObject)service).getZone());
        }
    }


    @Bind(id="zones",specification = Zone.class, aggregate = true, optional = true)
    public void bindZone(Zone zone) {

        String instance =  zone.getZoneName()+".presences";

        Map<String,Object> properties = new HashMap<>();
        properties.put(ContextEntity.State.id(ServiceLayer.class,ServiceLayer.NAME), instance);

        serviceCreator.create(instance,properties);
        attacher.link(instance,zone);
    }

    @Unbind(id="zones")
    public void unbindZone(Zone zone) {

        String instance = zone.getZoneName()+".presences";

        serviceCreator.delete(instance);
        attacher.unlink(instance,zone);
    }
    @Bind(id="presenceservices")
    public void bindservice1(PrecenseService drv){
        System.out.println(drv.getServiceName());
        System.out.println("APP(light) PRESENCE SRV BINDED**************************************************");
    }

    @Unbind(id="presenceservices")
    public void unbindservice1(){
        System.out.println("APP(light) PRESENCE SRV UN-BINDED**************************************************");
    }

    @Modified(id="presenceservices")
    public void presenceChange(){
        System.out.println("APP(light) PRESENCE CHANGE******************************************");
    }

    //FUNCTIONS

 

}
