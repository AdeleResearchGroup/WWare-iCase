package fr.liglab.adele.interop.services.simulations;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.temperature.Thermometer;

import fr.liglab.adele.icasa.location.LocatedObject;
import org.apache.felix.ipojo.annotations.*;
import tec.units.ri.quantity.Quantities;
import tec.units.ri.unit.Units;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;
import java.util.List;

//@ContextEntity(coreServices = {ExternalTemperatureSimulator.class})
//@Provides(specifications = ExternalTemperatureSimulator.class)
public class ExternalTemperatureSimulator  {

    //SERVICE's STATES
   /* @ContextEntity.State.Field(service = Thermometer.class,state=Thermometer.THERMOMETER_CURRENT_TEMPERATURE)
    private Quantity<Temperature>  currentTemperature;

    @ContextEntity.State.Field(service = GenericDevice.class,state = GenericDevice.DEVICE_SERIAL_NUMBER)
    private String serialNumber;*/

    //IMPLEMENTATIONS's FUNCTIONS
  /*  @Override
    public Quantity<Temperature> getTemperature() {
        return Quantities.getQuantity(23.0, Units.KELVIN);
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }*/

    //REQUIREMENTS

    @Requires(id="outThermo",specification = Thermometer.class,filter = "(!(locatedobject.object.zone="+LocatedObject.LOCATION_UNKNOWN+"))")
    List<Thermometer> thermometers;

    //CREATORS

    //ACTIONS
    @Modified(id="outThermo")
    public void thermoMod(Thermometer thr){
        System.out.println("(SRV) exttempSim out Thermo modified");
        System.out.println(thr.getTemperature());
//        currentTemperature=Quantities.getQuantity(23, Units.KELVIN);
    }
    @Bind(id="outThermo")
    public void bindThermometer(Thermometer thr){
        System.out.println("Binded Thermometer");
        System.out.println(thr.getClass());         //class com.sun.proxy.$Proxy57
        System.out.println(thr.getSerialNumber());  //TemperatureSensorService-proxy-c72d0722-0754-3dec-bf06-f445c16f2df5
        System.out.println(thr);                    //fr.liglab.adele.iop.device.proxies.IOPServiceProxy@37951315
        System.out.println(thr.getTemperature());   //293.15 K

//        pushTemperature(thr.getTemperature());
       // System.out.println(thr.getTemperature());
    }

    //FUNCTIONS
   /* @ContextEntity.State.Push(service = Thermometer.class, state = Thermometer.THERMOMETER_CURRENT_TEMPERATURE)
    public Quantity<Temperature> pushTemperature(Quantity<Temperature> temperature){
        return temperature;
    }*/


}
