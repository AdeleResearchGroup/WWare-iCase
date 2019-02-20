package fr.liglab.adele.interop.services.database;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.icasa.clockservice.Clock;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.doorWindow.WindowShutter;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.light.DimmerLight;
import fr.liglab.adele.icasa.device.light.Photometer;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.device.temperature.ThermometerExt;
import fr.liglab.adele.icasa.layering.applications.api.ApplicationLayer;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.physical.abstraction.MomentOfTheDay;
import fr.liglab.adele.interop.demonstrator.home.temperature.RoomTemperatureControl;
import fr.liglab.adele.interop.demonstrator.home.temperature.RoomTemperatureControlApp;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.InfluxDBIOException;
import org.influxdb.dto.Pong;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.lang.String.valueOf;

@ContextEntity(coreServices = {InfluxService.class, ServiceLayer.class})
public class InfluxServiceImpl implements InfluxService, ServiceLayer{

    //SERVICE's STATES
    @ContextEntity.State.Field(service = InfluxService.class, state = SERVICE_STATUS)
    private String status;


    @ContextEntity.State.Field(service = ServiceLayer.class, state = ServiceLayer.NAME)
    public String name;

    @ContextEntity.State.Field(service = ServiceLayer.class, state = ServiceLayer.SERVICE_QOS, value = "0",directAccess = true)
    private int SrvQoS;

    private static final Integer MIN_QoS = 100;

    private final static int INFLX_PORT = 8089;
    static final String DATABASE_NAME = "test";

    private InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:8086","root","root");
    private static final Logger LOG = LoggerFactory.getLogger(RoomTemperatureControlApp.class);

    //REQUIREMENTS
    @Requires(id="MoD", optional = false, proxy = false, specification = MomentOfTheDay.class)
    public MomentOfTheDay MoD;

    @Requires(id = "locCooler", optional = true, proxy = false, specification = Cooler.class)
    public List<Cooler> locCoolers;
    @Requires(id = "locHeater", optional = true,proxy = false, specification = Heater.class)
    public List<Heater> locHeater;
    @Requires(id = "extThermoeter",optional = true,proxy = false,specification = ThermometerExt.class)
    public List<ThermometerExt> extThermo;
    @Requires(id = "locBinaryLight", optional = true, proxy = false, specification = BinaryLight.class)
    public List<BinaryLight> locBinaryLight;
    @Requires(id = "locWindowShutter", optional = true, proxy = false, specification = WindowShutter.class)
    public List<WindowShutter> locWindowShutter;
    @Requires(id = "locDimmerLight", optional = true, proxy = false, specification = DimmerLight.class)
    public List<DimmerLight> locDimmerLight;
    @Requires(id = "locPresenceSensor", optional = true, proxy = false, specification = PresenceSensor.class)
    public List<PresenceSensor> locPresenceSensor;
    @Requires(id = "locThermometer", optional = true, proxy = false, specification = Thermometer.class)
    public List<Thermometer> locThermometer;
    @Requires(id = "locThermometerExt", optional = true, proxy = false, specification = ThermometerExt.class)
    public List<ThermometerExt> locThermometerExt;
    @Requires(id = "locPhotometer", optional = true, proxy = false, specification = Photometer.class)
    public List<Photometer> locPhotometer;

    @Requires(id="services", optional = true, proxy = false, specification = ServiceLayer.class)
    public List<ServiceLayer> services;
    @Requires(id="apps",optional = false,proxy = false,specification = ApplicationLayer.class)
    public List<ApplicationLayer> apps;
    @Requires
    Clock clock;

    @Validate
    public void start(){

    }

    @Override
    public boolean isInfluxRunning() {

        try{
            Pong result = influxDB.ping();
            SrvQoS=100;

            return true;
        }catch(InfluxDBIOException e){
            LOG.warn("couldn't connect to DB");
            SrvQoS=0;
            return false;
        }
    }

    @Override
    public int getMinQos() {
        return MIN_QoS;
    }

    @Override
    public int getServiceQoS() {
        isInfluxRunning();
        return SrvQoS;
    }

    @Override
    public String getServiceName() {
        return name;
    }



    /**
     *
     * @param timeStart String representing the time such as "2019-01-28"
     * @param lowerLimit 370d the query will be made with the #timeStart - lowerLimit
     * @param upperLimit 365d the query will finish with #timeStart - upperLimit
     * @param function DBfunction that states what function will be applied to the query: mean, sum, etc.
     * @param limit although results are calculated with all the time period, the values returned can be limited
     * @return query result from DB
     */
    @Override
    public List<QueryResult.Result>  aiQuery(String timeStart,String lowerLimit, String upperLimit,DBfunction function, int limit){
        if(isInfluxRunning()){
            return generalQuery(SensorType.AI, timeStart,lowerLimit, upperLimit,function, limit);
        }else {
            return null;
        }

    }

    /**
     *
     * @param sensorType passed as an enum SensorType for the desired measurement i.e SensorType.BinaryLight
     * @param timeStart variable should be in epoch format refering to the time
     * @param timeDuration refers to the duration of the returned measurement 10s, 1m, 3h, 1d, 1w
     * @param function DBfunction that states what function will be applied to the query: mean, sum, etc.
     * @param limit although results are calculated with all the time period, the values returned can be limited
     * @return
     */
    @Override
    public List<QueryResult.Result>  QueryDB(SensorType sensorType, String timeStart, String timeDuration,DBfunction function, int limit){
        if(isInfluxRunning()){
            return generalQuery(sensorType, timeStart, "NA",timeDuration, function, limit);
        }else {
            return null;
        }

    }

    /**
     *
     * @param Query manual query that should include a complete query such as
     *              SELECT last("value") FROM temperature WHERE value >278.4 and value <278.6
     * @param DataBaseName name of the database to query
     * @return
     */
    @Override
    public List<QueryResult.Result> manualQuery(String Query, String DataBaseName){
        if(isInfluxRunning()){
            try{
                Query queryDb;
                influxDB.setDatabase(DataBaseName);
                queryDb = new Query(Query, DataBaseName);
                QueryResult result=influxDB.query(queryDb);
                return result.getResults();
            }catch (NullPointerException e){
                LOG.info("empty query",e);
                return null;
            }
        }
        return null;
    }
    @Override
    public void eraseDB(){
        if(isInfluxRunning()){
            Query queryDb = new Query("DROP database "+DATABASE_NAME,DATABASE_NAME);
            influxDB.query(queryDb);
        }
    }
    //@Override

    /**
     *
     * @param sensorType  passed as an enum SensorType for the desired measurement i.e SensorType.BinaryLight
     * @param timeStart   variable should be in epoch format refering to the time
     * @param lowerLimit
     * @param upperLimitOrDuration  refers to the duration of the returned measurement 10s, 1m, 3h, 1d, 1w
     * @param function  DBfunction that states what function will be applied to the query: mean, sum, etc.
     * @param limit   although results are calculated with all the time period, the values returned can be limited
     * @return
     */
    public List<QueryResult.Result> generalQuery(SensorType sensorType, String timeStart,String lowerLimit, String upperLimitOrDuration,DBfunction function, int limit){
        if(isInfluxRunning()){
            String meassurement = getMeassurement(sensorType.toString());
            String formatedTimeStart=timeStart+"000000";
            String tDuration = assertDuration(upperLimitOrDuration);
            String a="";

            String firstQueryPart;

            if(function == DBfunction.none){
                firstQueryPart="SELECT * FROM ";
            }else{
                firstQueryPart="SELECT "+function.toString()+"(*) FROM  ";
            }


            Query queryDb;

            if(sensorType == SensorType.AI){
                influxDB.setDatabase("mLearning");

                a=firstQueryPart+meassurement+" WHERE time > '"+timeStart+"' - "+lowerLimit+" AND time < '"+timeStart+"' - + "+upperLimitOrDuration+" LIMIT "+String.valueOf(limit);
                queryDb = new Query(a, "mLearning");

            }else{
                a=firstQueryPart+meassurement+" WHERE \"type\"='"+sensorType.toString()+"' AND time > "+formatedTimeStart+" AND time <"+formatedTimeStart+"+"+tDuration+" LIMIT "+String.valueOf(limit);
                queryDb = new Query(a, DATABASE_NAME);
            }

            try{

                QueryResult result=influxDB.query(queryDb);
                //if there's no result??
                if(result.getResults().get(0).getSeries().equals("null")){
                    //nothing to return
                }else{
                    return result.getResults();
                }

            }catch (NullPointerException e){
                LOG.info("empty query",e);
            }
        }

        return null;
    }

        /**
     * takes information from every service and sensor and saves it into influx
     * @param Ttime
     */
    @Override
    public void writeAllSensorsState(long Ttime){
//System.err.printf("%B%n",isInfluxRunning());
        if(isInfluxRunning()){
            String timeFormatted = String.valueOf(clock.currentTimeMillis())+"000000";
            String time=(Ttime==0)?timeFormatted:String.valueOf(Ttime)+"000000";

            String query="";
            QueryResult result1 = influxDB.query(new Query("CREATE DATABASE " + DATABASE_NAME, DATABASE_NAME));

            influxDB.setDatabase(DATABASE_NAME);
            String CurZone="";


            for (Cooler cl:locCoolers) {
                try{
                    query = "powerLvl,zone="+getZoneName(cl)+",type=Cooler,name="+cl.getSerialNumber()+" value="+cl.getPowerLevel()+" "+time;
                    influxDB.write(query);
                }catch (NullPointerException e){

                }

            }
            for (Heater ht:locHeater) {
                try{
                    query = "powerLvl,zone="+getZoneName(ht)+",type=Heater,name="+ht.getSerialNumber()+" value="+ht.getPowerLevel()+" "+time;
                    influxDB.write(query);
                }catch (NullPointerException e){

                }
            }
            for (BinaryLight bl:locBinaryLight) {
                try{
                    int val = bl.getPowerStatus()?1:0;
                    query = "powerStatus,zone="+getZoneName(bl)+",type=BinaryLight,name="+bl.getSerialNumber()+" value="+val+" "+time;
                    influxDB.write(query);
                }catch (NullPointerException e){

                }
            }
            for (WindowShutter ws:locWindowShutter) {
                try{
                    query = "ShutterLvl,zone="+getZoneName(ws)+",type=WindowShutter,name="+ws.getSerialNumber()+" value="+ws.getShutterLevel()+" "+time;
                    influxDB.write(query);
                }catch (NullPointerException e){

                }
            }
            for (DimmerLight dl:locDimmerLight) {
                try{
                    query = "powerLvl,zone="+getZoneName(dl)+",type=DimmerLight,name="+dl.getSerialNumber()+" value="+dl.getPowerLevel()+" "+time;
                    influxDB.write(query);
                }catch (NullPointerException e){

                }

            }
            for (PresenceSensor ps:locPresenceSensor) {
                try{
                    int presence= ps.getSensedPresence()?1:0;
                    query = "presence,zone="+getZoneName(ps)+",type=PresenceSensor,name="+ps.getSerialNumber()+" value="+presence+" "+time;
                    influxDB.write(query);
                }catch (NullPointerException e){

                }
            }
            for (Thermometer tm:locThermometer) {
                try{
                    Double temp= (Double) tm.getTemperature().getValue();

                    query = "temperature,zone="+getZoneName(tm)+",type=Thermometer,name="+tm.getSerialNumber()+" value="+temp+" "+time;
                    influxDB.write(query);
                }catch (NullPointerException e){

                }
            }
            for (ThermometerExt tm:locThermometerExt) {
                try{
                    query = "temperature,zone="+getZoneName(tm)+",type=ThermometerExt,name="+tm.getSerialNumber()+" value="+tm.getTemperature().getValue()+" "+time;
                    influxDB.write(query);
                }catch (NullPointerException e){
                }
            }
            for (Photometer pt:locPhotometer) {
                try{
                    query = "illuminance,zone="+getZoneName(pt)+",type=Photometer,name="+pt.getSerialNumber()+" value="+pt.getIlluminance()+" "+time;
                    influxDB.write(query);
                }catch (NullPointerException e){

                }
            }

            //Apps
            for(ApplicationLayer app:apps){
                String [] appClass = app.toString().split("\\.");
                String appName = appClass[appClass.length-1].split("@")[0];
                if(appName.equals("RoomTemperatureControlApp")){
                    String[] rawAppState = ((RoomTemperatureControl)app).getAppState();
                    double heaters =0;
                    double balconyThermos = 0;
                    double remoteThermos = 0;
                    double aiThermo = 0;
                    if(rawAppState.length>1){
                        heaters =Double.parseDouble(rawAppState[0].split(":")[1].split("%")[0]);
                        balconyThermos = Double.parseDouble(rawAppState[1].split(":")[1].split("%")[0]);
                        remoteThermos = Double.parseDouble(rawAppState[2].split(":")[1].split("%")[0]);
                        query = "coverage,zone=ALL,type=app,sub=Heaters value="+Double.toString(heaters)+" "+time;
                        influxDB.write(query);
                        query = "coverage,zone=ALL,type=app,sub=localThermos value="+Double.toString(balconyThermos)+" "+time;
                        influxDB.write(query);
                        query = "coverage,zone=ALL,type=app,sub=ExternalThermos value="+Double.toString(remoteThermos)+" "+time;
                        influxDB.write(query);

                    }

                }


            }

            //services
            for (ServiceLayer srv:services) {
                query = "qos,zone="+srv.getServiceName()+",type=Service,name="+srv.getServiceName()+" value="+srv.getServiceQoS()+" "+time;
                influxDB.write(query);
            }
        }
    }

    String getMeassurement(String type){
        String toReturn="";
        switch (type){
            case "AI": toReturn="Temp";
            break;
            case "Service": toReturn="qos";
                break;
            case "Cooler":
            case "Heater":
            case "DimmerLight": toReturn="powerLvl";
                break;
            case "BinaryLight": toReturn="powerStatus";
                break;
            case "WindowShutter": toReturn="ShutterLvl";
                break;
            case "PresenceSensor": toReturn="presence";
                break;
            case "Thermometer": toReturn="temperature";
                break;
            case "ThermometerExt": toReturn="temperature";
                break;
            case "Photometer": toReturn="illuminance";
                break;
            case "App": toReturn="/.*/";

        }
        return toReturn;
    }

    String assertDuration(String duration){
        char period =duration.charAt(duration.length()-1);
        if(period=='s'||period=='m'||period=='h'||period=='d'){
            return duration;
        }else{
            return "0";
        }
    }


    String getZoneName(GenericDevice device){
        try{
            return ((LocatedObject)device).getZone();
        }catch(ClassCastException e){
            System.err.println(e);
            return "none";
        }
    }
}
