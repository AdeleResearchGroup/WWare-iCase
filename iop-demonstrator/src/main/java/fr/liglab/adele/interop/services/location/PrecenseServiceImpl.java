package fr.liglab.adele.interop.services.location;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import fr.liglab.adele.icasa.layering.services.location.ZoneService;
import fr.liglab.adele.icasa.layering.services.location.ZoneServiceFunctionalExtension;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.location.Zone;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Modified;
import org.apache.felix.ipojo.annotations.Requires;

import java.util.List;
import java.util.function.Supplier;


@ContextEntity(coreServices = {PrecenseService.class, ServiceLayer.class})

@FunctionalExtension(id="ZoneService",contextServices = ZoneService.class, implementation = ZoneServiceFunctionalExtension.class)

public class PrecenseServiceImpl implements PrecenseService, ServiceLayer {

    //SERVICE's STATES
    @ContextEntity.State.Field(service = PrecenseService.class, state = STATE_CHANGE,value="false")
    private boolean ServiceStatus;
    @ContextEntity.State.Field(service = PrecenseService.class,state = ZONE_ATTACHED)
    private String zoneName;

    @ContextEntity.State.Field(service = ServiceLayer.class, state = ServiceLayer.NAME)
    public String name;

    @ContextEntity.State.Field(service = ServiceLayer.class,state = ServiceLayer.SERVICE_QOS)
    private int AppQoS;

    private static final Integer MIN_QOS = 34;


    //IMPLEMENTATION's FUNCTIONS
    @Override
    public String getServiceName() {
        return name;
    }

    @Override
    public int getServiceQoS() {
        return AppQoS;
    }

    @Override
    public int getMinQos() {
        return MIN_QOS;
    }

    @Override
    public boolean getCurrentState() {
        return ServiceStatus;
    }

    @Override
    public String getAttachedZone() {
        return zoneName;
    }


    //REQUIREMENTS
	@Requires(id="sensor", optional=true, specification = PresenceSensor.class)
    @ContextRequirement(spec = {LocatedObject.class})
    private List<PresenceSensor> presenceSensors;

	@Requires(id="blight", optional = true,specification = BinaryLight.class)
    @ContextRequirement(spec = {LocatedObject.class})
    private List<BinaryLight> lights;

    //CREATORS

    //ACTIONS
	@Modified(id="sensor")
    private void sensorTriggered(PresenceSensor presenceSensor){
	    if(presenceSensors.size()>0){
            updateState();
            for (BinaryLight light:lights) {
                if(((LocatedObject)light).getZone().equals(((LocatedObject)presenceSensor).getZone())){
                    if(presenceSensor.getSensedPresence()){
                        light.turnOn();
                    }else{
                        light.turnOff();
                    }

                }
            }

        }
    }

    /**
     * Zone management
     */
    @Bind(id="zones",specification = Zone.class, aggregate = true, optional = true)
    public void zoneMod(Zone zone){
        pushZone(zone.getZoneName());
    }

    //STATES CHANGE
    @ContextEntity.State.Push(service=PrecenseService.class,state = PrecenseService.ZONE_ATTACHED)
    public String pushZone(String zoneName){
        return zoneName;}

    @ContextEntity.State.Push(service = PrecenseService.class,state = PrecenseService.STATE_CHANGE)
    public boolean pushService(boolean ServiceStatus){
        return ServiceStatus;}

    @ContextEntity.State.Pull(service = ServiceLayer.class,state = ServiceLayer.SERVICE_QOS)
    private Supplier<Integer> currentQoS = ()-> {
    	int currentQoS = 0;
    	return currentQoS;
    };

    //FUNCTIONS
    private void updateState() {
        if (getCurrentState()){
            pushService(false);
        } else {
            pushService(true);
        }
    }


    public List<String> getchange2() {
        List<String> activatedSensors = null;
        for(PresenceSensor ps:presenceSensors){
            if(ps.getSensedPresence()){
                activatedSensors.add(((LocatedObject)ps).getZone());
            }
        }
        return activatedSensors;
    }



}
