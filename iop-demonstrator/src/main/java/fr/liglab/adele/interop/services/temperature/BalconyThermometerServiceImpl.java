package fr.liglab.adele.interop.services.temperature;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.location.Zone;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Modified;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import tec.units.ri.quantity.Quantities;
import tec.units.ri.unit.Units;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;
import java.util.List;
import java.util.function.Supplier;

@ContextEntity(coreServices = {BalconyThermometerService.class,ServiceLayer.class})
public class BalconyThermometerServiceImpl implements BalconyThermometerService, ServiceLayer {
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

    @Requires(id="thermometer",optional = false,filter="(& (locatedobject.object.zone="+LocatedObject.LOCATION_UNKNOWN+") (!(objectClass=fr.liglab.adele.iop.device.api.IOPService)) )",specification = Thermometer.class)
    @ContextRequirement(spec = LocatedObject.class)
    List<Thermometer> thermometers;

    //ACTIONS
    @Unbind(id="thermometer")
    public void unbindThermometer(Thermometer th){
        //set service state to false if no more thermometers
        System.out.println("(SRV) Balc: thermometer UNbinded for a total of: "+thermometers.size());
        AppQoS=(thermometers.size()==0)?0:100;
        updateState();
    }
    @Bind(id="thermometer")
    public void bindThermometer(Thermometer th){
        System.out.println("(SRV) Balc: thermometer binded for a total of: "+thermometers.size());
        AppQoS=(thermometers.size()==0)?0:100;
        updateState();
    }

    //STATES CHANGE
    @Modified(id="thermometer")
    public void modifiedThermo(Thermometer th){
        System.out.println("(SRV) Balc: thermometer MODED for a total of: "+thermometers.size());
        AppQoS=(thermometers.size()==0)?0:100;
        updateState();
    }

    @ContextEntity.State.Push(service = BalconyThermometerService.class,state = BalconyThermometerService.SERVICE_STATUS)
    public boolean pushService(boolean serviceState){return serviceState;}

    @ContextEntity.State.Pull(service = ServiceLayer.class,state = ServiceLayer.SERVICE_QOS)
    private Supplier<Integer> currentQos =()->{
        System.out.println("SRV(balc) lamba PULL of heating service");
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
        Quantity<Temperature> temp = Quantities.getQuantity(213.15,Units.KELVIN);
        for(Thermometer Th :thermometers){
            if (Th.getSerialNumber().equals(thermoRef)){
                return Th.getTemperature();
            }
            System.out.println(((GenericDevice)Th).toString());
            System.out.println(((GenericDevice)Th).getSerialNumber());
            temp.add(Th.getTemperature());
        }
        return null;
    }

    //FUNCTIONS

    /**
     *
     * @param zne zone for which an outside close thermometer must be found
     * @return returns the name of the instanciated thermometer; it must be close to the zone and not be inside other zones,
     * returns none if no close thermometers are found
     */
    @Override
    public String getExternalZoneSensor(String zne) {

        System.out.println("GETTING CLOSEST THERMO...");
        double ShortestDistance = 99999;
        int index=0;
        int ClosestThermometerIndex=0;

        for(Zone zone:zones){
            System.out.println("matching Zone..."+zone.getZoneName()+" is equal to: "+zne);
            if(zone.getZoneName().equals(zne)){
                System.out.println("Match found...");
                for(Thermometer Thr:thermometers){
                    System.out.println("Searching in thermometers..."+Thr);

                    int Left = zone.getLeftTopAbsolutePosition().x-((LocatedObject) Thr).getPosition().x+OBJ_SIZE;
                    int Right = ((LocatedObject) Thr).getPosition().x-zone.getRightBottomAbsolutePosition().x;
                    int Top = zone.getLeftTopAbsolutePosition().y-((LocatedObject) Thr).getPosition().y+OBJ_SIZE;
                    int Bottom = ((LocatedObject) Thr).getPosition().y-zone.getRightBottomAbsolutePosition().y;

                    System.out.println("L:"+Left+"R:"+Right+"T:"+Top+"B:"+Bottom);

                    Left=(Left>0)?Left:0;
                    Right=(Right>0)?Right:0;
                    Top=(Top>0)?Top:0;
                    Bottom=(Bottom>0)?Bottom:0;


                    System.out.println("L:"+Left+"R:"+Right+"T:"+Top+"B:"+Bottom);

                    if (Left==0&&Right==0&&Top==0&&Bottom==0){//Thermometer right next to zone
                        System.out.println("Getting Temp from Thr: "+Thr);
                        return Thr.getSerialNumber();
                    }else{

                        double distance=Math.sqrt(Math.pow((double)Left,2)+Math.pow((double)Right,2)+Math.pow((double)Top,2)+Math.pow((double)Bottom,2));
                        System.out.println("Calculating distance..."+distance);
                        if (ShortestDistance>distance){
                            ShortestDistance=distance;
                            System.out.println();
                            ClosestThermometerIndex=index;
                        }
                    }
                    index+=1;
                }
            }
        }



        if (ShortestDistance < 400){
            System.out.println("Getting Temp from index: "+ClosestThermometerIndex);
            System.out.println(thermometers.get(ClosestThermometerIndex));
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
