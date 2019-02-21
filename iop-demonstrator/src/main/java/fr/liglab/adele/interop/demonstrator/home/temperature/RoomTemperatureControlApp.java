package fr.liglab.adele.interop.demonstrator.home.temperature;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
import fr.liglab.adele.icasa.clockservice.Clock;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.layering.applications.api.ApplicationLayer;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import fr.liglab.adele.icasa.layering.services.location.ZoneService;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.interop.services.dataManip.pidService;
import fr.liglab.adele.interop.services.dataManip.pidServiceImpl;
import fr.liglab.adele.interop.services.database.InfluxService;
import fr.liglab.adele.interop.services.location.ZonesService;
import fr.liglab.adele.interop.services.location.ZonesServiceImpl;
import fr.liglab.adele.interop.services.temperature.*;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@ContextEntity(coreServices = {ApplicationLayer.class, RoomTemperatureControl.class})
@Provides(specifications = RoomTemperatureControlApp.class)

public class RoomTemperatureControlApp implements ApplicationLayer, RoomTemperatureControl {

    private static final Logger LOG = LoggerFactory.getLogger(RoomTemperatureControlApp.class);

    //APPLICATION's STATES
    @ContextEntity.State.Field(service = RoomTemperatureControl.class, state = APPLICATION_STATE, value = "init")
    private String appState;
    @ContextEntity.State.Field(service = RoomTemperatureControl.class, state = RoomTemperatureControl.APPLICATION_QOS, value = "0", directAccess = true)
    private int appQoS;


    //IMPLEMENTATION's FUNCTIONS
    public RoomTemperatureControlApp() {

    }

    //REQUIREMENTS
    @Requires
    Clock clock;

    @Requires(id = "heaterService", specification = HeatersService.class, optional = true)
    @ContextRequirement(spec = {ZoneService.class})
    private List<HeatersService> heaterSrvices;

    @Requires(id="localThermometerService",specification = RoomThermometerService.class, optional = true)
    @ContextRequirement(spec = {ZoneService.class})
    private List<RoomThermometerService> roomThermo;

    @Requires(id = "pidController", specification = pidService.class, optional = true)
    private pidService pidSrv;

    @Requires(id="mlController",specification = LearnedHeaterBehavior.class,optional = true)
    private LearnedHeaterBehavior mlHeater;


    @Requires(id = "remoteThermometer", specification = RemoteThermometerService.class, optional = true)
    private RemoteThermometerService remoteThermometerService;

    @Requires(id = "balconyThermometerService", specification = BalconyThermometerService.class, optional = true)
    private BalconyThermometerService balconyThermometerService;

    @Requires(id = "zonesService", specification = ZonesService.class, optional = true)
    private ZonesService zonesService;

    @Requires(id = "dbService", specification = InfluxService.class, optional = true)
    private InfluxService DBService;

    // @Requires(id="zones",specification = ZonesService.class,optional = true)
    // private ZonesService zonesService;

    private long currGobalTime=0;


    //CREATORS
    private @Creator.Field(ZoneService.RELATION_ATTACHED_TO)
    Creator.Relation<ZoneService, Zone> attacher;


    private @Creator.Field
    Creator.Entity<RoomThermometerServiceImpl> internalThermometerServiceCreator;
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
    private @Creator.Field
    Creator.Entity<pidServiceImpl> pidService;
    private @Creator.Field
    Creator.Entity<LearnedHeaterBehaviorImpl> mlService;

    /* private String dbName = "aTimeSeries";*/


    //ACTIONS
    @Validate
    public void start() {
        int UDP_PORT = 8089;
        String UDP_DATABASE = "udp";


        Map<String, Object> SrvIOPParam = new HashMap<>();
        SrvIOPParam.put(ContextEntity.State.id(ServiceLayer.class, ServiceLayer.NAME), "extThermIOPServ");
        Map<String, Object> SrvbalTheParam = new HashMap<>();
        SrvbalTheParam.put(ContextEntity.State.id(ServiceLayer.class, ServiceLayer.NAME), "balconyThermometers");
        Map<String, Object> SrvZonesParam = new HashMap<>();
        SrvZonesParam.put(ContextEntity.State.id(ServiceLayer.class, ServiceLayer.NAME), "zonesService");
        Map<String, Object> SrvAItemp = new HashMap<>();
        SrvAItemp.put(ContextEntity.State.id(ServiceLayer.class, ServiceLayer.NAME), "AItemp");
        Map<String, Object> SrvPID = new HashMap<>();
        SrvPID.put(ContextEntity.State.id(ServiceLayer.class, ServiceLayer.NAME), "pidCtrl");
        Map<String, Object> SrvML = new HashMap<>();
        SrvML.put(ContextEntity.State.id(ServiceLayer.class,ServiceLayer.NAME),"MLCtrl");


        LOG.info("Temperature control App Started");
        externalThermometerServiceCreator.create("extThermIOPServ", SrvIOPParam);
        balconyThermometers.create("balconyThermometers", SrvbalTheParam);
        zonesServices.create("zonesService", SrvZonesParam);
        aiTemperatureService.create("AItemp", SrvAItemp);
        pidService.create("pidCtrl",SrvPID);
        mlService.create("MLCtrl",SrvML);


    }

    @Modified(id = "zonesService")
    public void modifiedZone() {
        String action = zonesService.getServiceStatus().split(":")[0];
        String zoneName = zonesService.getServiceStatus().split(":")[1];
        Zone zone = zonesService.getZone(zoneName);
        String instance = zoneName + ".heaters";
        String instanceThermo = zoneName + ".thermometers";

        if (action.equals("bind")) {
            Map<String, Object> properties = new HashMap<>();
            Map<String, Object> thermoProperties = new HashMap<>();
            properties.put(ContextEntity.State.id(ServiceLayer.class, ServiceLayer.NAME), instance);
            thermoProperties.put(ContextEntity.State.id(ServiceLayer.class,ServiceLayer.NAME), instanceThermo);


            if (roomHeaterServices.getInstance(instance) == null) {
                roomHeaterServices.create(instance, properties);
                attacher.link(instance, zone);
                setState();
            }
            if(internalThermometerServiceCreator.getInstance(instanceThermo) == null){
                internalThermometerServiceCreator.create(instanceThermo,thermoProperties);
                attacher.link(instanceThermo,zone);
                setState();
            }
        } else if (action.equals("unbind")) {
            roomHeaterServices.delete(instance);
            attacher.unlink(instance, zone);
            internalThermometerServiceCreator.delete(instanceThermo);
            attacher.unlink(instanceThermo,zone);
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
            for (HeatersService ht : heaterSrvices) {
                setTemperature(((ZoneService) ht).getZone(),1,clock.currentTimeMillis());

            }
        }

    }
    @Bind(id="localThermometerService")
    public void bindThermo(RoomThermometerService rt ){

    }
    @Unbind(id="localThermometerService")
    public void ubindThermo(RoomThermometerService rt ){

    }
    @Modified(id="localThermometerService")
    public void modifyThermo(RoomThermometerService rt ){
        setTemperature(((ZoneService) rt).getZone(),2,clock.currentTimeMillis());

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
            setTemperature(zone,3,clock.currentTimeMillis());
        }
    }


    @Bind(id = "heaterService")
    public void bindedHeaters(HeatersService srv) {

        String zone = ((ZoneService) srv).getZone();
        setTemperature(zone,4,clock.currentTimeMillis());
    }

    @Unbind(id = "heaterService")
    public void unbindedHeaters(HeatersService srv) {
        String zone = ((ZoneService) srv).getZone();

        balconyThermometerService.removeAsignedThermometer(zone);
    }

    @Modified(id = "heaterService")
    public void modifiedheaters(HeatersService srv) {

        String tmp = (srv.getServiceName().split("\\."))[0];
        setTemperature(tmp,5,clock.currentTimeMillis());
    }

    @ContextEntity.State.Push(service = RoomTemperatureControl.class, state = RoomTemperatureControl.APPLICATION_STATE)
    public String pushApp(String appState) {
        return appState;
    }


    //FUNCTIONS

    public void setState() {
        double availabilityHeaters = 0;
        double availabilityLocalThermos = 0;
        double availabilityRemoteThermos = 0;
        int zonesWithHeater = 0;
        int zonesWithLocalThermo = 0;
        Map<String, String> assignedThermometers = new HashMap<String, String>();

        if (balconyThermometerService.getServiceStatus() != null) {
            assignedThermometers = balconyThermometerService.getAsignedThermometers();
        }

        zonesWithLocalThermo = assignedThermometers.size();
        for (HeatersService htrSrv : heaterSrvices) {
            if (htrSrv.getServiceQoS() > 0) {
                zonesWithHeater += 1;
            }
        }


        availabilityHeaters = (zonesWithHeater > 0) ? (((double) zonesWithHeater / (double) zonesService.getZoneList().size())) * 100 : 0;
        availabilityLocalThermos = (zonesWithLocalThermo > 0) ? ((double) zonesWithLocalThermo / (double) (zonesService.getZoneList().size())) * 100 : 0;
        availabilityRemoteThermos = (remoteThermometerService.getServiceQoS() == 100) ? (availabilityHeaters - availabilityLocalThermos) : 0;

        pushApp("Heaters: " + Math.round(availabilityHeaters) + "%, BalcTher: " + Math.round(availabilityLocalThermos) + "%, RemTher: " + availabilityRemoteThermos + "%");
    }

    private int lastOriginer =0;
    private int lastScenario=0;
    private long lastTime=0;
    private int interaction=-1;
    /**
     * Sets the power level of the heaters in a zone, depending on the zoneAM and the appAm level
     *
     * @param zone Zone for which the temperature will be attempted to be set
     */
    public void setTemperature(String zone, int Originer,long time) {
        interaction+=1;
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(time);
        int h=date.get(Calendar.HOUR_OF_DAY);
        int m=date.get(Calendar.MINUTE);

        date.setTimeInMillis(lastTime);
        int hh=date.get(Calendar.HOUR_OF_DAY);
        int mm=date.get(Calendar.MINUTE);

       /* System.out.println("");
        System.out.println("time\tpevTime");
        System.out.println(h+":"+m+"\t"+hh+":"+mm);
        System.out.printf("delta:%B\n",(time-lastTime>2000000));
        System.out.println(time-lastTime);*/

        if(time-lastTime>500000){
            lastTime=time;
            setState();

            //appState= String.valueOf(appAM()+zoneAM(zone));
             LOG.info("AM (global-zone:" + zone + "): (" + appAM() + "-" + zoneAM(zone) + ")");
            //determining the scenario to run...
            int scenario = zoneAM(zone);
            System.out.printf("SCR\tlOrg\ttime\tltime\tSCN\titeration\n");
            System.out.printf("%1d\t%1d\t%13d\t%13d\t%d\t%4d\n",Originer, lastOriginer,time,lastTime,scenario,interaction);

            if(scenario >= 0 && scenario < 17){
                //SCENARIO 5: no enough resources to set temperature
            }else if(scenario == 17){
                //SCENARIO 4:
            }else if(scenario > 17 && scenario < 20){
                //SCENARIO 3: no local
            }else if(scenario >= 20 && scenario < 24){
                //SCENARIO 2: local thermo unavailable but balcony  Thermo available  >>> impossible to PID
                String nearestThermo = balconyThermometerService.getClosestExternalThermometerToZone(zone);
                LOG.info("geting temp from local thermometer service: " + nearestThermo, balconyThermometerService);
                double nearTemp = (double) balconyThermometerService.getCurrentTemperature(nearestThermo).getValue();

                Double powerPercent = mlService.getInstance("MLCtrl").getHeaterPorcentage(nearTemp,zone);

                //get min and max temperature for the zone??
                //need to get the same outside temperature date and then with that date get the heater level


                if (nearTemp != -2.0d) {
                    System.out.printf("%s\t%s",powerPercent, zone);
                    if(powerPercent != null){
                        setHeatersPower(powerPercent, zone);
                    }
                }

            }else if(scenario >= 24 && scenario < 32){
                //SCENARIO 1: local thermometer available >>> PID controller
                double ZoneTemperature = (double)internalThermometerServiceCreator.getInstance(zone + ".thermometers").getTemperature();
                double Tempobjective = 283.15;//aka 10°C

                if(!pidService.getInstance("pidCtrl").getServiceStatus().equals("init")){
                    pidService.getInstance("pidCtrl").setPIDvars(0.1, 0.1, 0.6);
                    pidService.getInstance("pidCtrl").startPID(Tempobjective);
                }


                double output = pidService.getInstance("pidCtrl").getControlVariableValue( Tempobjective, ZoneTemperature);
                System.err.printf("Target\tActual\tOutput\tError\n");
                System.err.printf("%3.2f\t%3.2f\t%3.2f\t%3.2f\n", Tempobjective, ZoneTemperature, output, (Tempobjective-ZoneTemperature));
                setHeatersPower(output,zone);

            }else{
                //impossible scenario
            }

            switch (8) {
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
                    String nearestThermo = balconyThermometerService.getClosestExternalThermometerToZone(zone);
                    LOG.info("geting temp from local thermometer service: " + nearestThermo, balconyThermometerService);
                    double nearTemp = (double) balconyThermometerService.getCurrentTemperature(nearestThermo).getValue();




                    if (nearTemp != -2.0d) {
                        setHeatersPower(nearTemp, zone);
                    }
                    break;
                case 8:
                    //failsafe
                    break;
            }
            lastScenario = scenario;
        }else {
            lastTime=time;
        }



        lastOriginer = Originer;

        // influxDB.QueryDB();


    }

    /**
     * Sets a power level distributed among all the heaters in a zone
     *
     * @param powerLevel power level for which the heaters will be set for.
     * @param zone                 zone for which the power will be set
     */
    private void setHeatersPower(double powerLevel, String zone) {
      //  LOG.info("setting temperatue on " + zone + " from a reference of " + powerLevel + "%");
        roomHeaterServices.getInstance(zone + ".heaters").setPowerLevel(powerLevel);


       /* for (HeatersService htrSrv : heaterSrvices) {
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
        }*/
    }

    /**
     * appAM checks the global available services and returns a number that is the binary sum of
     * heaterService +=1b
     * balconyThermometers +=100b
     * remoteThermometer +=10b
     *
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
     * balconyThermometer service active += 100b
     * remoteThermometer service active += 10b
     * heater service += 1b
     *
     * @param zone String: name of the zone
     * @return
     */
    private byte zoneAM(String zone) {

        //variables to store the availability of the needed services
        byte heaterSrv = 0b0;
        byte internalThermometerSrv =0b0;
        byte balconyThermometerSrv = 0b0;
        byte remoteThermometerSrv = 0b0;
        byte dbService = 0b0;
        //combined service level for the app
        byte AMstatusLevel = 0b0;

        //determining the availability of services in the zone
        heaterSrv = isHeaterServiceInZone(zone);
        internalThermometerSrv = isThermoServiceInZone(zone);
        balconyThermometerSrv = isbalconyThermoServiceInZone(zone);
        remoteThermometerSrv = isRemoteServiceAvailable();
        dbService = isDBServiceAvailable();

        //calculate the zone AM level...
        AMstatusLevel = (byte) (heaterSrv + internalThermometerSrv + balconyThermometerSrv + remoteThermometerSrv + dbService);
        return AMstatusLevel;
    }



    @Override
    public String[] getAppState() {
        String[] states = appState.split("\\,");
        if (states.length == 0) {
            return new String[0];
        } else {
            return states;
        }
    }

    @Override
    public int getAppQoS() {
        return appQoS;
    }

    /**
     * checks availability of heater service
     * @return 0 if unavailable, 10000 if available
     */
    private byte isHeaterServiceInZone(String zone){
        byte ans = 0b0;
        for (HeatersService htsr : heaterSrvices) {
            ans = (((ZoneService) htsr).getZone().equals(zone))? (byte)0b10000:(byte)0b0;
        }
        return ans;
    }
    /**
     * checks availability of local thermometer service
     * @return 0 if unavailable, 1000 if available
     */
    private byte isThermoServiceInZone(String zone){
        byte ans = 0b0;
        for (RoomThermometerService thsrv:roomThermo) {
            ans = (((ZoneService) thsrv).getZone().equals(zone))? (byte)0b1000:(byte)0b0;
        }
        return ans;
    }
    /**
     * checks availability of local balcony thermometer service
     * @return 0 if unavailable, 100 if available
     */
    private byte isbalconyThermoServiceInZone(String zone){
        byte ans = 0b0;
        int balconyState = (balconyThermometerService.getAsignedThermometer(zone) != null) ? 1 : 0;

        //is a balcony thermometer assigned to the zone?
        if (balconyState == 1) {
            String nearestThermo = balconyThermometerService.getClosestExternalThermometerToZone(zone);
            ans = balconyThermometerService.getAsignedThermometer(zone).equals("none") ? (byte) 0b0 : (byte) 0b100;
        } else {
            ans = 0b0;
        }
        return ans;
    }
    /**
     * checks availability of remote Thermometer service
     * @return 0 if unavailable, 10 if available
     */
    private byte isRemoteServiceAvailable(){
        return (remoteThermometerService.getServiceQoS() == 100) ? (byte) 0b10 : (byte) 0b0;
    }

    /**
     * checks availability of DB service
     * @return 0 if unavailable, 1 if available
     */
    private byte isDBServiceAvailable(){
        return (DBService.getServiceQoS() == 100) ? (byte) 0b1 : (byte) 0b0;
    }


}
