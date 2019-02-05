package fr.liglab.adele.interop.demonstrator.home.temperature;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.layering.applications.api.ApplicationLayer;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import fr.liglab.adele.icasa.layering.services.location.ZoneService;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.interop.services.database.DBfunction;
import fr.liglab.adele.interop.services.database.SensorType;
import fr.liglab.adele.interop.services.database.influxService;
import fr.liglab.adele.interop.services.database.influxServiceImpl;
import fr.liglab.adele.interop.services.location.ZonesService;
import fr.liglab.adele.interop.services.location.ZonesServiceImpl;
import fr.liglab.adele.interop.services.temperature.*;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;


@ContextEntity(coreServices = {ApplicationLayer.class,RoomTemperatureControl.class})
@Provides(specifications = RoomTemperatureControlApp.class)

public class RoomTemperatureControlApp implements ApplicationLayer, RoomTemperatureControl {

    private static final Logger LOG = LoggerFactory.getLogger(RoomTemperatureControlApp.class);

    //APPLICATION's STATES
    @ContextEntity.State.Field(service = RoomTemperatureControl.class, state = APPLICATION_STATE, value="init")
    private String appState;
    @ContextEntity.State.Field(service = RoomTemperatureControl.class,state = RoomTemperatureControl.APPLICATION_QOS, value="0",directAccess = true)
    private int appQoS;


    //IMPLEMENTATION's FUNCTIONS
    public RoomTemperatureControlApp() {

    }

    //REQUIREMENTS
    @Requires(id = "heaterService", specification = HeatersService.class, optional = true)
    @ContextRequirement(spec = {ZoneService.class})
    private List<HeatersService> heaterSrvices;



    @Requires(id = "remoteThermometer", specification = RemoteThermometerService.class, optional = true)
    private RemoteThermometerService remoteThermometerService;

    @Requires(id = "balconyThermometerService", specification = BalconyThermometerService.class, optional = true)
    private BalconyThermometerService balconyThermometerService;

    @Requires(id="zonesService",specification = ZonesService.class,optional = true)
    private ZonesService zonesService;

   // @Requires(id="zones",specification = ZonesService.class,optional = true)
   // private ZonesService zonesService;


    //CREATORS
    private @Creator.Field(ZoneService.RELATION_ATTACHED_TO)
    Creator.Relation<ZoneService, Zone> attacher;


    private @Creator.Field
    Creator.Entity<RemoteThermometerServiceImpl> externalThermometerServiceCreator;
    private @Creator.Field
    Creator.Entity<HeatersServiceImpl> roomHeaterServices;
    private @Creator.Field
    Creator.Entity<BalconyThermometerServiceImpl> balconyThermometers;
    private @Creator.Field
    Creator.Entity<ZonesServiceImpl> zonesServices;
    private @Creator.Field
    Creator.Entity<AIRoomTemperatureServiceImpl> aiTemperatureService;


   /* private String dbName = "aTimeSeries";*/



    //ACTIONS
    @Validate
    public void start() {
        int UDP_PORT = 8089;
        String UDP_DATABASE = "udp";


        Map<String,Object> SrvIOPParam = new HashMap<>();
        SrvIOPParam.put(ContextEntity.State.id(ServiceLayer.class,ServiceLayer.NAME),"DB");
        Map<String,Object> SrvbalTheParam = new HashMap<>();
        SrvbalTheParam.put(ContextEntity.State.id(ServiceLayer.class,ServiceLayer.NAME),"DB");
        Map<String,Object> SrvZonesParam = new HashMap<>();
        SrvZonesParam.put(ContextEntity.State.id(ServiceLayer.class,ServiceLayer.NAME),"DB");
        Map<String,Object> SrvAItemp = new HashMap<>();
        SrvAItemp.put(ContextEntity.State.id(ServiceLayer.class,ServiceLayer.NAME),"DB");


        LOG.info("Temperature control App Started");
        externalThermometerServiceCreator.create("extThermIOPServ",SrvIOPParam);
        balconyThermometers.create("balconyThermometers",SrvbalTheParam);
        zonesServices.create("zonesService",SrvZonesParam);
        aiTemperatureService.create("AItemp",SrvAItemp);


    }

    @Modified(id="zonesService")
    public void modifiedZone(){
        String action = zonesService.getServiceStatus().split(":")[0];
        String zoneName = zonesService.getServiceStatus().split(":")[1];
        Zone zone=zonesService.getZone(zoneName);
        String instance = zoneName + ".heaters";

        if(action.equals("bind")){
            Map<String, Object> properties = new HashMap<>();
            properties.put(ContextEntity.State.id(ServiceLayer.class, ServiceLayer.NAME), instance);
            if(roomHeaterServices.getInstance(instance)==null){
                roomHeaterServices.create(instance, properties);
                attacher.link(instance,zone);
                setState();
            }
        }else if(action.equals("unbind")){
            roomHeaterServices.delete(instance);
            attacher.unlink(instance,zone);
            setState();
        }
    }



    @Bind(id = "remoteThermometer")
    public void bindservice(RemoteThermometerService srv) {

    }

    @Unbind(id = "remoteThermometer")
    public void unbindservice(RemoteThermometerService srv) {

    }

    @Modified(id = "remoteThermometer")
    public void modifiedservice(RemoteThermometerService srv) {

        if (srv.isThermometerPresent() > 0) {
            for(HeatersService ht:heaterSrvices){
                setTemperature(((ZoneService) ht).getZone());
            }
        }

    }


    @Bind(id = "balconyThermometerService")
    public void bindbalconyTh(BalconyThermometerService srv) {


    }

    @Unbind(id = "balconyThermometerService")
    public void unbindbalconyTh(BalconyThermometerService srv) {
        LOG.warn("No Local thermometers left, asking Base...", srv);
        remoteThermometerService.setConnection(new String[]{Thermometer.class.getCanonicalName()});
    }

    @Modified(id = "balconyThermometerService")
    public void modifiedBalconyTher(BalconyThermometerService srv) {
        for (HeatersService htr : heaterSrvices) {
            String zone = ((ZoneService) htr).getZone();
            setTemperature(zone);
        }
    }


    @Bind(id = "heaterService")
    public void bindedHeaters(HeatersService srv) {

        String zone = ((ZoneService) srv).getZone();
        setTemperature(zone);
    }

    @Unbind(id = "heaterService")
    public void unbindedHeaters(HeatersService srv) {
        String zone = ((ZoneService) srv).getZone();

        balconyThermometerService.removeAsignedThermometer(zone);
    }

    @Modified(id = "heaterService")
    public void modifiedheaters(HeatersService srv) {

        String tmp = (srv.getServiceName().split("\\."))[0];
        setTemperature(tmp);
    }

    @ContextEntity.State.Push(service=RoomTemperatureControl.class, state = RoomTemperatureControl.APPLICATION_STATE)
    public String pushApp(String appState){return appState;}




    //FUNCTIONS

    public void setState(){
        double availabilityHeaters=0;
        double availabilityLocalThermos=0;
        double availabilityRemoteThermos=0;
        int zonesWithHeater=0;
        int zonesWithLocalThermo=0;
        Map<String,String> assignedThermometers=new HashMap<String, String>();

        if(balconyThermometerService.getServiceStatus()!=null){
            assignedThermometers=balconyThermometerService.getAsignedThermometers();
        }

        zonesWithLocalThermo=assignedThermometers.size();
        for(HeatersService htrSrv:heaterSrvices){
            if(htrSrv.getServiceQoS()>0){
                zonesWithHeater+=1;
            }
        }


        availabilityHeaters=(zonesWithHeater>0)?(((double)zonesWithHeater/(double)zonesService.getZoneList().size()))*100:0;
        availabilityLocalThermos=(zonesWithLocalThermo>0)?((double)zonesWithLocalThermo/(double)(zonesService.getZoneList().size()))*100:0;
        availabilityRemoteThermos=(remoteThermometerService.getServiceQoS()==100)?(availabilityHeaters-availabilityLocalThermos):0;

        pushApp("Heaters: "+Math.round(availabilityHeaters)+"%, BalcTher: "+Math.round(availabilityLocalThermos)+"%, RemTher: "+availabilityRemoteThermos+"%");
        //influxDB.singleDBwrite("coverage",Long.toString(Math.round(availabilityHeaters)),"zone=ALL,name=RoomTemperature,type=app,sub=Heaters");
        //influxDB.singleDBwrite("coverage",Long.toString(Math.round(availabilityLocalThermos)),"zone=ALL,name=RoomTemperature,type=app,sub=localThermos");
        //influxDB.singleDBwrite("coverage",Long.toString(Math.round(availabilityRemoteThermos)),"zone=ALL,name=RoomTemperature,type=app,sub=ExternalThermos");
    }

    /**
     * Sets the power level of the heaters in a zone, depending on the zoneAM and the appAm level
     *
     * @param zone Zone for which the temperature will be attempted to be set
     */
    public void setTemperature(String zone) {
        setState();
        //appState= String.valueOf(appAM()+zoneAM(zone));
        LOG.info("AM (global-zone:"+zone+"): (" + appAM() + "-" + zoneAM(zone)+")");
        switch (zoneAM(zone)) {
            case 1:
            case 2:
            case 4:
            case 6:
                if (appAM() == 5) {
                    LOG.warn("Local thermometers not found for zone:" + zone + ", asking Base...");
                    remoteThermometerService.setConnection(new String[]{Thermometer.class.getCanonicalName()});
                } else {
                    LOG.warn("can't set Temperature, insufficient resources", zone);
                }

                break;
            case 3:
                //iop present
                double farTemp = (Double) remoteThermometerService.getCurrentTemperature().getValue();
                setHeatersPower(farTemp, zone);
                break;
            case 5:
                //balcony present
            case 7:
                //balcony+iop present
                String nearestThermo = balconyThermometerService.getExternalZoneSensor(zone);
                LOG.info("geting temp from local thermometer service: " + nearestThermo, balconyThermometerService);
                double nearTemp = (double) balconyThermometerService.getCurrentTemperature(nearestThermo).getValue();
                if (nearTemp != -2.0d) {
                    setHeatersPower(nearTemp, zone);
                }
                break;
        }



       // influxDB.QueryDB();




    }

    /**
     * Sets a power level distributed among all the heaters in a zone
     *
     * @param referenceTemperature outside temperature for which the heaters will be set for.
     * @param zone                 zone for which the power will be set
     */
    private void setHeatersPower(double referenceTemperature, String zone) {
        LOG.info("setting temperatue on "+zone+" from a reference of "+referenceTemperature+"°K");
        for (HeatersService htrSrv : heaterSrvices) {
            String tmp = (htrSrv.getServiceName().split("\\."))[0];
            if (tmp.equals(zone)) {
                if (referenceTemperature > 290d) {//16.85
                    htrSrv.setPowerLevel(0d);
                } else if (referenceTemperature > 288.15d) {//15°C
                    htrSrv.setPowerLevel(0.2d);
                } else if (referenceTemperature > 283.15d) {//10°C
                    htrSrv.setPowerLevel(0.4d);
                } else if (referenceTemperature > 278.15d) {//5°C
                    htrSrv.setPowerLevel(0.6d);
                } else if (referenceTemperature > 273.15d) {//0°C
                    htrSrv.setPowerLevel(0.8d);
                } else if (referenceTemperature < 273.15d) {//256.2=-16.95
                    htrSrv.setPowerLevel(1d);
                }
            }
        }
    }

    /**appAM
     * @return
     */
    private byte appAM() {
        int mainSrv = heaterSrvices.size();
        int balconSrv = balconyThermometerService.getServiceQoS();
        int extThSrv = remoteThermometerService.getServiceQoS();

        byte level = 0b0;
        if (mainSrv < 1) {
            return 0b0;
        } else {
            level += (byte) 0b1;
        }
        if (extThSrv == 100) {
            level += (byte) 0b10;
        }
        if (balconSrv == 100) {
            level += (byte) 0b100;
        }
        return level;
    }

    /**
     * zoneAM checks the available services and returns a number that is the binary sum of
     *   balconyThermometer service active += 100b
     *   remoteThermometer service active += 10b
     *   heater service += 1b
     * @param zone String: name of the zone
     * @return
     */
    private byte zoneAM(String zone) {
        byte heaterSrv = 0b0;
        byte balconyThermometerSrv = 0b0;
        byte externalThermometerSrv = 0b0;
        byte AMstatusLevel = 0b0;

        int mainSrv = heaterSrvices.size();
        int balconSrv = balconyThermometerService.getServiceQoS();

        if((mainSrv < 1)&&(balconSrv == 100)){
            
        }

        int balconyState=(balconyThermometerService.getAsignedThermometer(zone)!=null)?1:0;

        //is a heater service present in the zone?
        for (HeatersService htsr : heaterSrvices) {
            if (((ZoneService) htsr).getZone().equals(zone)) {
                heaterSrv = 0b1;
            }
        }
        //is a balcony thermometer assigned to the zone?
        if(balconyState==1){
            String nearestThermo = balconyThermometerService.getExternalZoneSensor(zone);
            balconyThermometerSrv = balconyThermometerService.getAsignedThermometer(zone).equals("none") ? (byte) 0b0 : (byte) 0b100;
        }else{
            balconyThermometerSrv=0b0;
        }

        //is the external base thermometer available?
        externalThermometerSrv = (remoteThermometerService.getServiceQoS() == 100) ? (byte) 0b10 : (byte) 0b0;

        //calculate the zone AM level...
        AMstatusLevel = (byte) (heaterSrv + balconyThermometerSrv + externalThermometerSrv);
        return AMstatusLevel;
    }


    @Override
    public String[] getAppState() {
        String[] states=appState.split("\\,");
        if(states.length==0){
            return new String[0];
        }else{
            return states;
        }
    }

    @Override
    public int getAppQoS() {
        return appQoS;
    }

}
