package fr.liglab.adele.interop.services.temperature;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.device.temperature.ThermometerExt;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.interop.demonstrator.home.temperature.RoomTemperatureControlApp;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Modified;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tec.units.ri.quantity.Quantities;
import tec.units.ri.unit.Units;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;
import java.util.List;
import java.util.function.Supplier;

@ContextEntity(coreServices = {BalconyThermometerService.class,ServiceLayer.class})
public class BalconyThermometerServiceImpl implements BalconyThermometerService, ServiceLayer {

    private static final Logger LOG = LoggerFactory.getLogger(RoomTemperatureControlApp.class);

    //SERVICE's STATES

    @ContextEntity.State.Field(service = BalconyThermometerService.class,state = SERVICE_STATUS,value = "false")
    private boolean srvState;

    @ContextEntity.State.Field(service = ServiceLayer.class, state = ServiceLayer.NAME)
    public String name;

    @ContextEntity.State.Field(service = ServiceLayer.class,state = ServiceLayer.SERVICE_QOS,value="0")
    private int AppQoS;

    private static final Integer MIN_QOS = 34;
    private static final Integer OBJ_SIZE = 32;

    //REQUIREMENTS

    @Requires(id="zone",optional = false,specification = Zone.class)
    List<Zone> zones;

    @Requires(id="thermometer",optional = false,filter="(& (locatedobject.object.zone="+LocatedObject.LOCATION_UNKNOWN+") (!(objectClass=fr.liglab.adele.iop.device.api.IOPService)) )",specification = ThermometerExt.class)
    @ContextRequirement(spec = LocatedObject.class)
    List<ThermometerExt> thermometers;

    //ACTIONS

    @Unbind(id="thermometer")
    public void unbindThermometer(ThermometerExt th){
        //set service state to false if no more thermometers
        AppQoS=(thermometers.size()==0)?0:100;
        updateState();
    }
    @Bind(id="thermometer")
    public void bindThermometer(ThermometerExt th){
        LOG.debug("(SRV) Balc: thermometer binded for a total of: "+thermometers.size());
        AppQoS=(thermometers.size()==0)?0:100;
        updateState();
    }

    //STATES CHANGE
    @Modified(id="thermometer")
    public void modifiedThermo(ThermometerExt th){
        LOG.debug("(SRV) Balc: thermometer binded for a total of: "+thermometers.size());
        AppQoS=(thermometers.size()==0)?0:100;
        updateState();
    }

    @ContextEntity.State.Push(service = BalconyThermometerService.class,state = BalconyThermometerService.SERVICE_STATUS)
    public boolean pushService(boolean serviceState){return serviceState;}

    @ContextEntity.State.Pull(service = ServiceLayer.class,state = ServiceLayer.SERVICE_QOS)
    private Supplier<Integer> currentQos =()->{
        int currentQoS=(thermometers.size()==0)?0:100;
        return currentQoS;
    };

    //IMPLEMENTATION's FUNCTIONS

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


    @Override
    public boolean getServiceStatus() {
        return srvState;
    }

    @Override
    public Quantity<Temperature> getCurrentTemperature(String thermoRef) {
        Quantity<Temperature> temp = Quantities.getQuantity(-2.0,Units.KELVIN);
        for(ThermometerExt Th :thermometers){
            if ((Th.getSerialNumber().equals(thermoRef))&&(Th.getTemperature()!=null)){
                    return Th.getTemperature();
            }
        }
        return temp;
    }

    //FUNCTIONS

    /**
     *
     * @param zne zone for which an outside close thermometer must be found, if there're none, then "none" is returned
     * @return returns the name of the instanciated thermometer; it must be close to the zone and not be inside other zones,
     * returns none if no close thermometers are found
     */
    @Override
    public String getExternalZoneSensor(String zne) {

        LOG.info("finding closest thermometer");
        double ShortestDistance = 99999;
        int index=0;
        int ClosestThermometerIndex=0;
        for(Zone zone:zones){
            if(zone.getZoneName().equals(zne)){
                for(ThermometerExt Thr:thermometers){
                    int Left = zone.getLeftTopAbsolutePosition().x-((LocatedObject) Thr).getPosition().x+OBJ_SIZE;
                    int Right = ((LocatedObject) Thr).getPosition().x-zone.getRightBottomAbsolutePosition().x;
                    int Top = zone.getLeftTopAbsolutePosition().y-((LocatedObject) Thr).getPosition().y+OBJ_SIZE;
                    int Bottom = ((LocatedObject) Thr).getPosition().y-zone.getRightBottomAbsolutePosition().y;
                    Left=(Left>0)?Left:0;
                    Right=(Right>0)?Right:0;
                    Top=(Top>0)?Top:0;
                    Bottom=(Bottom>0)?Bottom:0;
                    if (Left==0&&Right==0&&Top==0&&Bottom==0){//Thermometer right next to zone
                        return Thr.getSerialNumber();
                    }else{
                        double distance=Math.sqrt(Math.pow((double)Left,2)+Math.pow((double)Right,2)+Math.pow((double)Top,2)+Math.pow((double)Bottom,2));
                        //calculating distance...
                        if (ShortestDistance>distance){
                            ShortestDistance=distance;
                            ClosestThermometerIndex=index;
                        }
                    }
                    index+=1;
                }
            }
        }
        if (ShortestDistance < 400){
            return thermometers.get(ClosestThermometerIndex).getSerialNumber();
        }else{
            return "none";
        }
    }

    //FUNCTIONS
    private void updateState(){
        if(getServiceStatus()){
            pushService(false);
        }else{
            pushService(true);
        }
    }

}
