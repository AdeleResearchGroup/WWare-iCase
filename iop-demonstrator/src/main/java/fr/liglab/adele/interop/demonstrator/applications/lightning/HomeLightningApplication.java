package fr.liglab.adele.interop.demonstrator.applications.lightning;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
import fr.liglab.adele.icasa.layering.applications.api.ApplicationLayer;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import fr.liglab.adele.icasa.layering.services.location.ZoneService;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.physical.abstraction.MomentOfTheDay.PartOfTheDay;
import fr.liglab.adele.interop.services.lightning.LightningService;
import fr.liglab.adele.interop.services.lightning.LightningServiceImpl;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.service.command.Descriptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@ContextEntity(coreServices = {ApplicationLayer.class})
@Provides(specifications= {HomeLightningApplication.class})

public class HomeLightningApplication implements ApplicationLayer {


    @Requires(id="lightningservices", specification = LightningService.class, optional=true)
    @ContextRequirement(spec = {ZoneService.class})
    private List<LightningService> services;

    private @Creator.Field(ZoneService.RELATION_ATTACHED_TO) Creator.Relation<ZoneService,Zone> attacher;

    private @Creator.Field Creator.Entity<LightningServiceImpl>	serviceCreator;

    @Requires(id="zones", specification=Zone.class, optional=true)
    private List<Zone> zones;

    @Bind(id="zones",specification = Zone.class, aggregate = true, optional = true)
    public void bindZone(Zone zone) {

        String instance =  zone.getZoneName()+".lightning";

        Map<String,Object> properties = new HashMap<>();
        properties.put(ContextEntity.State.id(ServiceLayer.class,ServiceLayer.NAME), instance);

        attacher.link(instance,zone);
        serviceCreator.create(instance,properties);
    }

    @Unbind(id="zones")
    public void unbindZone(Zone zone) {

        String instance = zone.getZoneName()+".lightning";

        attacher.unlink(instance,zone);
        serviceCreator.delete(instance);
    }

    @Invalidate
    private void stop() {
    	for (Zone zone : zones) {
			unbindZone(zone);
		}
    }

    @ServiceProperty(name = "osgi.command.scope", value = "home-lightning")
    String commandScope;

    @ServiceProperty(name = "osgi.command.function", value = "{}")
    String[] m_function = new String[] {"schedule"};

    @Descriptor("Schedule lightning in a given zone and period")
    public void schedule(@Descriptor("zone") String zone, @Descriptor("period") String periodName) {
    	
    	System.out.println("Enabling schedule at : "+periodName+" for zone "+zone);
    	
    	PartOfTheDay period = periodName.equals("NONE") ? null : PartOfTheDay.valueOf(periodName);
    	
        for (LightningService service: services) {
        	if ("ALL".equalsIgnoreCase(zone) || ((ZoneService) service).getZone().equals(zone)) {
            	service.setSchedule(period);
        	}
        }
        
    }


}
