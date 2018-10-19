package fr.liglab.adele.interop.services.temperature;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.iop.device.api.IOPLookupService;
import fr.liglab.adele.iop.device.api.IOPService;

import org.apache.felix.ipojo.annotations.*;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@ContextEntity(coreServices = {ExternalThermometerService.class, ServiceLayer.class })
public class ExternalThermometerServiceImpl implements ExternalThermometerService, ServiceLayer {
    boolean requestmade =false;

    //SERVICE's STATES
    @ContextEntity.State.Field(service = ExternalThermometerService.class,state = STATE_CHANGE,value = "false")
    private boolean ServiceStatus;

    @ContextEntity.State.Field(service = ExternalThermometerService.class,state = CONNECTION_STATUS,value="false")
    private boolean connStat;
    @ContextEntity.State.Field(service = ExternalThermometerService.class,state = REQUEST_MADE, value="false")
    private boolean isRequestmade;

    @ContextEntity.State.Field(service = ServiceLayer.class, state = ServiceLayer.NAME)
    public String name;

    @ContextEntity.State.Field(service = ServiceLayer.class,state = ServiceLayer.SERVICE_QOS, value="0")
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
        getConnectionStatus();
    }



    @Modified(id="outThermo")
    public void thermoMod(Thermometer thr){
        System.out.println("(SRV) exttempSim out Thermo modified,# of THRS: "+thermometers.size());
        System.out.println(thr.getTemperature());
        updateState();
//        currentTemperature=Quantities.getQuantity(23, Units.KELVIN);
    }
    @Bind(id="outThermo")
    public void bindThermometer(Thermometer thr){
        System.out.println("(SRV) extTh: Binded Thermometer for a total of: "+thermometers.size());
        updateState();


    }
    @Unbind(id="outThermo")
    public void unbindThermometer(Thermometer thr){
        updateState();
    }



    //STATES CHANGE
    @ContextEntity.State.Push(service = ExternalThermometerService.class,state = ExternalThermometerService.CONNECTION_STATUS)
    public boolean pushconnStat(boolean connStat){return connStat;}

    @ContextEntity.State.Push(service = ExternalThermometerService.class,state = ExternalThermometerService.STATE_CHANGE)
    public boolean pushService(boolean ServiceStatus){
        return ServiceStatus;}

    @ContextEntity.State.Pull(service = ServiceLayer.class,state = ServiceLayer.SERVICE_QOS)
    private Supplier<Integer> currentQos =()->{
        System.out.println("SRV(heat) lamba PULL of heating service"+String.valueOf((thermometers.size()>=1)?true:false));
        int currentQoS=0;

        if  (thermometers.size()>=1){
            currentQoS= 100;
        }else if (thermometers.size()<1){
            currentQoS= 90;
        }
        return currentQoS;
    };


    //IMPLEMENTATION's FUNCTIONS
    @Override
    public int isThermometerPresent(){
        if(thermometers!=null){
            return thermometers.size();
        }
        return 0;

    }
    @Override
    public boolean getCurrentState() {
        return ServiceStatus;
    }
    @Override
    public boolean getConnectionStatus() {return connStat;}

    @Override
    public void setConnection(String[] RequestStr) {
        if(!isRequestmade){
            System.out.println("IOP lookup: "+RequestStr);
            connStat=true;
            pushconnStat(true);
            lookup.consider(RequestStr,Collections.emptyMap());
            isRequestmade=true;
        }
    }

    @Override
    public Quantity<Temperature> getCurrentTemperature() {
        Quantity<Temperature> temp;
        for(Thermometer th:thermometers){
            temp=th.getTemperature();
        }

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
    private void updateState(){
        if(getCurrentState()){
            pushService(false);
        }else{
            pushService(true);
        }

    }



}
