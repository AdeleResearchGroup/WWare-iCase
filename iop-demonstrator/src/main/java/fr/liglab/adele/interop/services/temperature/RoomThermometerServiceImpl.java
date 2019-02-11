package fr.liglab.adele.interop.services.temperature;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import fr.liglab.adele.icasa.layering.services.location.ZoneService;
import fr.liglab.adele.icasa.layering.services.location.ZoneServiceFunctionalExtension;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.interop.demonstrator.home.temperature.RoomTemperatureControlApp;
import fr.liglab.adele.interop.services.location.ZonesService;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Modified;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;
import java.util.List;

@ContextEntity(coreServices = {RoomThermometerService.class,ServiceLayer.class})
@FunctionalExtension(id = "ZoneService", contextServices = ZoneService.class, implementation = ZoneServiceFunctionalExtension.class)
public class RoomThermometerServiceImpl implements RoomThermometerService, ServiceLayer{

    private static final Logger LOG = LoggerFactory.getLogger(RoomTemperatureControlApp.class);

    //SERVICE's STATES

    @ContextEntity.State.Field(service = RoomThermometerService.class, state = SERVICE_STATUS, value = "293.15")
    private Double srvState;

    @ContextEntity.State.Field(service = RoomThermometerService.class, state = SERVICE_CHANGE, value = "false")
    private Boolean srvChange;

    @ContextEntity.State.Field(service = ServiceLayer.class, state = ServiceLayer.NAME)
    public String name;

    @ContextEntity.State.Field(service = ServiceLayer.class, state = ServiceLayer.SERVICE_QOS, value = "0",directAccess = true)
    private int SrvQoS;

    //REQUIREMENTS

    @Requires(id="thermometer",optional = false, filter = ZoneService.objectInSameZone, proxy = false, specification = Thermometer.class)
    @ContextRequirement(spec = {LocatedObject.class})
    public List<Thermometer> thermometers;


    @Bind(id="thermometer")
    public void bindThermometer(Thermometer th){
        SrvQoS=(thermometers.size()>1)?100:0;
    }
    @Unbind(id="thermometer")
    public void unbindThermometer(Thermometer th){
        SrvQoS=(thermometers.size()==0)?0:100;
    }

    @Modified(id="thermometer")
    public void modifyThermometer(Thermometer th){
        SrvQoS=(thermometers.size()>1)?100:0;
        srvState=th.getTemperature().getValue().doubleValue();
        pushService(th.getTemperature().getValue().doubleValue());
    }

    //STATES CHANGE

    @ContextEntity.State.Push(service = RoomThermometerService.class, state = RoomThermometerService.SERVICE_STATUS)
    public Double pushService(Double serviceState){
        return serviceState;
    }
    @ContextEntity.State.Push(service = RoomThermometerService.class,state = RoomThermometerService.SERVICE_CHANGE)
    public Boolean pushChange(Boolean serviceChange){return serviceChange;}


    @Override
    public String getServiceStatus() {
        return srvState.toString();
    }

    @Override
    public Double getTemperature() {
        return srvState;
    }

    @Override
    public int getMinQos() {
        return 100;
    }

    @Override
    public int getServiceQoS() {
        return SrvQoS;
    }

    @Override
    public String getServiceName() {
        return name;
    }
}
