package fr.liglab.adele.interop.services.temperature;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.iop.device.api.IOPLookupService;
import fr.liglab.adele.iop.device.api.IOPService;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Modified;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@ContextEntity(coreServices = {ExternalThermometerService.class, ServiceLayer.class })
public class ExternalThermometerServiceImpl implements ExternalThermometerService, ServiceLayer {
    boolean requestmade =false;

    //SERVICE's STATES
    @ContextEntity.State.Field(service = ExternalThermometerService.class,state = CONNECTION_STATUS,value="false")
    private boolean connStat;
    @ContextEntity.State.Field(service = ExternalThermometerService.class,state = REQUEST_MADE, value="false")
    private boolean isRequestmade;

    @ContextEntity.State.Field(service = ServiceLayer.class, state = ServiceLayer.NAME)
    public String name;

    @ContextEntity.State.Field(service = ServiceLayer.class,state = ServiceLayer.SERVICE_QOS)
    private int AppQoS;

    private static final Integer MIN_QOS = 34;

    //REQUIREMENTS

    @Requires(optional = false)
    private IOPLookupService lookup;

   /* @Requires(id="outThermo",specification = Thermometer.class,filter = "(!(locatedobject.object.zone="+LocatedObject.LOCATION_UNKNOWN+"))")
    List<Thermometer> thermometers;*/

    @Requires(id="outThermo",specification = Thermometer.class,optional = true)
    @ContextRequirement(spec = IOPService.class)
    List<Thermometer> thermometers;

    //factory.name = TemperatureSensorService
    //CREATORS

    //ACTIONS

    @Validate
    public void start(){
        System.out.println("IOP TERMOMETER SRV STARTED");
        //getConnectionStatus();
    }
    @ContextEntity.State.Push(service = ExternalThermometerService.class,state = ExternalThermometerService.CONNECTION_STATUS)
    public boolean pushService(boolean serviceState){return serviceState;}

    @Modified(id="outThermo")
    public void thermoMod(Thermometer thr){
        System.out.println("(SRV) exttempSim out Thermo modified,# of THRS: "+thermometers.size());
        System.out.println(thr.getTemperature());
//        currentTemperature=Quantities.getQuantity(23, Units.KELVIN);
    }
    @Bind(id="outThermo")
    public void bindThermometer(Thermometer thr){
        System.out.println("(SRV) extTh: Binded Thermometer for a total of: "+thermometers.size());
        System.out.println(thr.getClass());         //class com.sun.proxy.$Proxy57
        System.out.println(thr.getSerialNumber());  //TemperatureSensorService-proxy-c72d0722-0754-3dec-bf06-f445c16f2df5
        System.out.println(thr);                    //fr.liglab.adele.iop.device.proxies.IOPServiceProxy@37951315
        System.out.println(thr.getTemperature());   //293.15 K

//        pushTemperature(thr.getTemperature());
        // System.out.println(thr.getTemperature());
    }

   /* @ContextEntity.State.Pull(service = ExternalThermometerService.class,state = ExternalThermometerService.CONNECTION_STATUS)
    private Supplier<String> pullCurrentState =()->{
        System.out.println("SRV(heat) lamba PULL of extThermo service");
        return "true";
    };*/




    //IMPLEMENTATION's FUNCTIONS
    @Override
    public String getConnectionStatus() {return String.valueOf(connStat);}

    @Override
    public void setConnection(String[] RequestStr) {
        if(!isRequestmade){
            System.out.println("IOP lookup: "+RequestStr);
            connStat=true;
            pushService(true);
            lookup.consider(RequestStr,Collections.emptyMap());
            isRequestmade=true;
        }
    }

    @Override
    public Quantity<Temperature> getCurrentTemperature() {


        return null;
    }

    @Override
    public int getMinQos() {
        return MIN_QOS;
    }

    @Override
    public int getServiceQoS() {
        return AppQoS;
    }

    @Override
    public String getServiceName() {
        return name;
    }

    //FUNCTIONS



}
