package fr.liglab.adele.interop.services.lightning;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import fr.liglab.adele.icasa.layering.services.location.ZoneService;
import fr.liglab.adele.icasa.layering.services.location.ZoneServiceFunctionalExtension;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.physical.abstraction.MomentOfTheDay;
import fr.liglab.adele.icasa.physical.abstraction.MomentOfTheDay.PartOfTheDay;
import org.apache.felix.ipojo.annotations.Modified;
import org.apache.felix.ipojo.annotations.Requires;

import java.util.List;

@ContextEntity(coreServices = {LightningService.class, ServiceLayer.class,})

@FunctionalExtension(id="ZoneService",contextServices = ZoneService.class, implementation = ZoneServiceFunctionalExtension.class)

public class LightningServiceImpl implements LightningService, ServiceLayer {


	@Requires(specification=BinaryLight.class, filter=ZoneService.OBJECTS_IN_ZONE, optional=false, proxy=false)
    @ContextRequirement(spec = {LocatedObject.class})
    private List<BinaryLight> binaryLights;

	private PartOfTheDay scheduledPeriod = null;
	
	@Override
	public void setSchedule(PartOfTheDay period) {
		this.scheduledPeriod = period;
	}


    @Requires(id = "MoD", optional=false)
    MomentOfTheDay momentOfTheDay;

    @Modified(id = "MoD")
    protected void momentOfDayUpdated() {

    	System.out.println("verifying lightning schedule at "+momentOfTheDay.getCurrentPartOfTheDay()+" scheduled("+scheduledPeriod+")");
		for (BinaryLight binaryLight : binaryLights) {
    		binaryLight.setPowerStatus(momentOfTheDay.getCurrentPartOfTheDay() == scheduledPeriod);
		}

    }


    @ContextEntity.State.Field(service=ServiceLayer.class, state=ServiceLayer.NAME)
    private String name;

    @Override
    public String getServiceName() {
        return name;
    }

    @ContextEntity.State.Field(service=ServiceLayer.class, state=ServiceLayer.SERVICE_QOS, value="100")
    private int qos;

    @Override
    public int getQoS() {
        return qos;
    }

}
