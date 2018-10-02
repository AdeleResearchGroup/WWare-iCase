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



    //requirements
	@Requires(id="sensor", optional=true, specification = PresenceSensor.class)
    @ContextRequirement(spec = {LocatedObject.class})
    private List<PresenceSensor> presenceSensors;

	@Requires(id="blight", optional = true,specification = BinaryLight.class)
    @ContextRequirement(spec = {LocatedObject.class})
    private List<BinaryLight> lights;


	@Modified(id="sensor")
    private void sensorTriggered(PresenceSensor presenceSensor){
	    if(presenceSensors.size()>0){
	        //System.out.println(presenceSensor.getSensedPresence());
           // System.out.println(((LocatedObject)presenceSensor).getZone());
            updateService();
            for (BinaryLight light:lights) {
                if(((LocatedObject)light).getZone().equals(((LocatedObject)presenceSensor).getZone())){
                    if(presenceSensor.getSensedPresence()){
                        light.turnOn();
                        //System.out.println("LIGHT OOOOON");
                    }else{
                        light.turnOff();
                       //System.out.println("LIGHT OOOOOFF");
                    }

                }
            }
            getMinQos();
            getchange("ze");
            try
            {
                notifyAll();
            }
        catch(IllegalMonitorStateException imse)
            {
            }


        }
    }
    @Bind(id="zones",specification = Zone.class, aggregate = true, optional = true)
    public void zoneMod(){
        updateService();
    }

    @ContextEntity.State.Field(service = PrecenseService.class, state = CHANGES,directAccess = true)
    private String stateChange;

    private static final Integer MIN_QOS = 34;

    @ContextEntity.State.Field(service = ServiceLayer.class, state = ServiceLayer.NAME)
    public String name;

    @Override
    public String getServiceName() {
        return name;
    }


    @ContextEntity.State.Field(service = ServiceLayer.class,state = ServiceLayer.SERVICE_QOS)
    private int AppQoS;

    @Override
    public int getServiceQoS() {
        return AppQoS;
    }

    @ContextEntity.State.Pull(service = ServiceLayer.class,state = ServiceLayer.SERVICE_QOS)
    private Supplier<Integer> currentQoS = ()-> {

    	int currentQoS = 0;
    	return currentQoS;
    };
    
    @Override
    public int getMinQos() {
        return MIN_QOS;
    }

    public boolean stateChange(){
        stateChange=stateChange+"*";
        return true;
    }
    @ContextEntity.State.Push(service = PrecenseService.class, state = CHANGES)
    public String PushChange(String change){return change;}

    public void updateService(){
        PushChange("srv");
    }


    public List<String> getchange2() {
        List<String> activatedSensors = null;
        String a=CHANGES;
        for(PresenceSensor ps:presenceSensors){
            if(ps.getSensedPresence()){
                activatedSensors.add(((LocatedObject)ps).getZone());
            }
        }
        return activatedSensors;
    }

    @Override
    public String getchange(String state) {
        return null;
    }

    @Override
    public String getChanges() {
        return stateChange;
    }
}
