package fr.liglab.adele.interop.services.location;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import fr.liglab.adele.icasa.layering.services.location.ZoneService;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.interop.services.temperature.BalconyThermometerService;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Modified;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;

import java.util.List;
import java.util.function.Supplier;

@ContextEntity(coreServices = {ZonesService.class,ServiceLayer.class})
public class ZonesServiceImpl implements ZonesService, ServiceLayer {

    //SERVICE's STATES

    @ContextEntity.State.Field(service = ZonesService.class, state = SERVICE_STATUS, value = "init")
    private String srvState;

    @ContextEntity.State.Field(service = ZonesService.class, state = SERVICE_CHANGE, value = "false")
    private Boolean srvChange;

    @ContextEntity.State.Field(service = ServiceLayer.class, state = ServiceLayer.NAME)
    public String name;

    @ContextEntity.State.Field(service = ServiceLayer.class, state = ServiceLayer.SERVICE_QOS, value = "0")
    private int SrvQoS;

    private static final Integer MIN_QOS = 34;

    //REQUIREMENTS
    @Requires(id = "zone", optional = false, specification = Zone.class)
    List<Zone> zones;

    //ACTIONS
    @Bind(id = "zone")
    public void bindZone(Zone zone){
        updateState("bind:"+zone.getZoneName());
        serviceChange();
    }
    @Unbind(id = "zone")
    public void unbindZone(Zone zone){
        updateState("unbind:"+zone.getZoneName());
    }
    @Modified(id = "zone")
    public void modifiedZone(Zone zone){
        updateState("modified:"+zone.getZoneName());
    }

    //STATES CHANGE
    @ContextEntity.State.Push(service = ZonesService.class, state = ZonesService.SERVICE_STATUS)
    public String pushService(String serviceState) {
        return serviceState;
    }

    @ContextEntity.State.Push(service = ZonesService.class, state = ZonesService.SERVICE_CHANGE)
    public Boolean pushChange(Boolean serviceChange) {
        return serviceChange;
    }


    //IMPLEMENTATION's FUNCTIONS
    @Override
    public String getServiceStatus() {
        return srvState;
    }

    @Override
    public boolean getServiceState() {
        return srvChange;
    }

    @Override
    public Zone getZone(String zone) {
        for(Zone zn:zones){
            if(zn.getZoneName().equals(zone)){
                return zn;
            }
        }
        return null;
    }

    @Override
    public List<Zone> getZoneList() {
        return zones;
    }

    @Override
    public int getMinQos() {
        return MIN_QOS;
    }

    @Override
    public int getServiceQoS() {
        return SrvQoS;
    }

    @Override
    public String getServiceName() {
        return name;
    }
    //FUNCTIONS

    private void updateState(String state) {
        pushService(state);
    }
    private void serviceChange(){
        if (getServiceState()) {
            pushChange(false);
        } else {
            pushChange(true);
        }
    }

}
