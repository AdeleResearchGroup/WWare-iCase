package fr.liglab.adele.interop.services.temperature;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
import fr.liglab.adele.icasa.device.temperature.ThermometerExt;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.interop.demonstrator.home.temperature.RoomTemperatureControlApp;
import fr.liglab.adele.interop.services.location.ZonesService;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@ContextEntity(coreServices = {BalconyThermometerService.class, ServiceLayer.class})
public class BalconyThermometerServiceImpl implements BalconyThermometerService, ServiceLayer {

    private static final Logger LOG = LoggerFactory.getLogger(RoomTemperatureControlApp.class);

    private Map<String, String> outsideThermos = new HashMap<String, String>();


    private final int maxDistanceToZone = 400;


    //SERVICE's STATES

    @ContextEntity.State.Field(service = BalconyThermometerService.class, state = SERVICE_STATUS, value = "init")
    private String srvState;

    @ContextEntity.State.Field(service = BalconyThermometerService.class, state = SERVICE_CHANGE, value = "false")
    private Boolean srvChange;

    @ContextEntity.State.Field(service = ServiceLayer.class, state = ServiceLayer.NAME)
    public String name;

    @ContextEntity.State.Field(service = ServiceLayer.class, state = ServiceLayer.SERVICE_QOS, value = "0")
    private int SrvQoS;

    private static final Integer MIN_QOS = 34;
    private static final Integer OBJ_SIZE = 32;

    //REQUIREMENTS

    @Requires(id = "zone", optional = false, specification = ZonesService.class)
    ZonesService zones;

    @Requires(id = "thermometer", optional = false, filter = "(& (locatedobject.object.zone=" + LocatedObject.LOCATION_UNKNOWN + ") (!(objectClass=fr.liglab.adele.iop.device.api.IOPService)) )", specification = ThermometerExt.class)
    @ContextRequirement(spec = LocatedObject.class)
    List<ThermometerExt> thermometers;

    //ACTIONS

    @Bind(id = "thermometer")
    public void bindThermometer(ThermometerExt th) {
        LOG.info("(SRV) Balc: thermometer binded for a total of: " + thermometers.size());
        SrvQoS = (thermometers.size() == 0) ? 0 : 100;
        updateState();
        serviceChange();
    }

    @Unbind(id = "thermometer")
    public void unbindThermometer(ThermometerExt th) {
        //set service state to false if no more thermometers
        SrvQoS = (thermometers.size() == 0) ? 0 : 100;
        String valueToRemove="";
        List<String> toRemove=new ArrayList<>();;

        for (Map.Entry<String, String> entry : outsideThermos.entrySet()) {
            if (entry.getValue().equals(th.getSerialNumber())) {
                toRemove.add(entry.getKey());
                LOG.info("Thermometer removed from" + entry.getKey() + ":>" + entry.getValue());

            }
        }

        for(String a:toRemove){
            outsideThermos.remove(a);
            updateState();
        }

        updateState();
        serviceChange();
    }
    @Modified(id = "thermometer")
    public void modifiedThermo(ThermometerExt th) {
        LOG.debug("(SRV) Balc: thermometer moded for a total of: " + thermometers.size());
        SrvQoS = (thermometers.size() == 0) ? 0 : 100;
        updateState();
        serviceChange();
    }


    //STATES CHANGE


    @ContextEntity.State.Push(service = BalconyThermometerService.class, state = BalconyThermometerService.SERVICE_STATUS)
    public String pushService(String serviceState) {
        return serviceState;
    }

    @ContextEntity.State.Push(service = BalconyThermometerService.class, state = BalconyThermometerService.SERVICE_CHANGE)
    public Boolean pushChange(Boolean serviceChange) {
        return serviceChange;
    }

    @ContextEntity.State.Pull(service = ServiceLayer.class, state = ServiceLayer.SERVICE_QOS)
    private Supplier<Integer> currentQos = () -> {
        int currentQoS = (thermometers.size() == 0) ? 0 : 100;
        return currentQoS;
    };

    //IMPLEMENTATION's FUNCTIONS

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


    @Override
    public Boolean getServiceStatus() {
        return srvChange;
    }

    @Override
    public int getNumberOfZones() {
        return zones.getZoneList().size();
    }

    /**
     * Get the current temperature of the specified external thermometer
     * @param thermoRef name of the external thermometer
     * @return Quantity with the temperature from the thermometer
     */
    @Override
    public Quantity<Temperature> getCurrentTemperature(String thermoRef) {
        Quantity<Temperature> temp = Quantities.getQuantity(-2.0, Units.KELVIN);
        for (ThermometerExt Th : thermometers) {
            if ((Th.getSerialNumber().equals(thermoRef)) && (Th.getTemperature() != null)) {
                return Th.getTemperature();
            }
        }
        return temp;
    }

    //FUNCTIONS

    /**
     * @param zne zone for which an outside close thermometer must be found, if there're none, then "none" is returned
     * @return returns the name of the instanciated thermometer; it must be close to the zone and not be inside other zones,
     * returns none if no close thermometers are found
     */
    @Override
    public String getClosestExternalThermometerToZone(String zne) {

        LOG.info("finding closest thermometer to: "+zne);
        double ShortestDistance = 99999;
        int index = 0;
        int ClosestThermometerIndex = 0;
        if (thermometers.size() == 0) {
            outsideThermos.remove(zne);
            return "none";
        }
        for (Zone zone : zones.getZoneList()) {
            if (zone.getZoneName().equals(zne)) {
                for (ThermometerExt Thr : thermometers) {
                    int Left = zone.getLeftTopAbsolutePosition().x - ((LocatedObject) Thr).getPosition().x + OBJ_SIZE;
                    int Right = ((LocatedObject) Thr).getPosition().x - zone.getRightBottomAbsolutePosition().x;
                    int Top = zone.getLeftTopAbsolutePosition().y - ((LocatedObject) Thr).getPosition().y + OBJ_SIZE;
                    int Bottom = ((LocatedObject) Thr).getPosition().y - zone.getRightBottomAbsolutePosition().y;
                    Left = (Left > 0) ? Left : 0;
                    Right = (Right > 0) ? Right : 0;
                    Top = (Top > 0) ? Top : 0;
                    Bottom = (Bottom > 0) ? Bottom : 0;
                    if (Left == 0 && Right == 0 && Top == 0 && Bottom == 0) {//Thermometer right next to zone
                        outsideThermos.put(zne, Thr.getSerialNumber());
                        updateState();
                        LOG.info("thermometer found at 0m");
                        return Thr.getSerialNumber();
                    } else {
                        double distance = Math.sqrt(Math.pow((double) Left, 2) + Math.pow((double) Right, 2) + Math.pow((double) Top, 2) + Math.pow((double) Bottom, 2));
                        //calculating distance...
                        if (ShortestDistance > distance) {
                            ShortestDistance = distance;
                            ClosestThermometerIndex = index;
                        }
                    }
                    index += 1;
                }
            }
        }
        if (ShortestDistance < maxDistanceToZone) {
            outsideThermos.put(zne, thermometers.get(ClosestThermometerIndex).getSerialNumber());
            updateState();
            LOG.info("thermometer found at " + ShortestDistance + "m");
            return thermometers.get(ClosestThermometerIndex).getSerialNumber();
        } else {
            outsideThermos.remove(zne);
            return "none";
        }
    }

    /**
     *
     * @param zone String with a zone name to be checked
     * @return Gets the already assigned thermometer to a zone, meaning the closest adjacent thermometer near the zone
     */
    @Override
    public String getAsignedThermometer(String zone) {
        LOG.info("list updated: " + outsideThermos);
        String thermo = (outsideThermos.get(zone) != null) ? outsideThermos.get(zone) : "none";
        return thermo;
    }

    /**
     * when a zone
     * @param zone
     */
    @Override
    public void removeAsignedThermometer(String zone) {
        outsideThermos.remove(zone);
    }

    @Override
    public Map<String, String> getAsignedThermometers() {
        return outsideThermos;
    }

    //FUNCTIONS
    private void updateState() {

        pushService(String.valueOf(outsideThermos));
    }
    private void serviceChange(){
        if (getServiceStatus()) {
            pushChange(false);
        } else {
            pushChange(true);
        }
    }

}
