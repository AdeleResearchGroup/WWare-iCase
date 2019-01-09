package fr.liglab.adele.interop.services.database;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.provider.Creator;
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
import fr.liglab.adele.icasa.layering.services.location.ZoneService;
import fr.liglab.adele.icasa.physical.abstraction.MomentOfTheDay;
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

@ContextEntity(coreServices = {influxService.class, ServiceLayer.class})
public class influxServiceImpl implements influxService, ServiceLayer{

    //SERVICE's STATES
    @ContextEntity.State.Field(service = influxService.class, state = SERVICE_STATUS)
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
            //QueryResult influxDB.write(new Query("CREATE DATABASE " ));
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

    /*public void test(){
        //influxDB=InfluxFactory.connect
        try{
            Pong result = influxDB.ping();
            //QueryResult influxDB.write(new Query("CREATE DATABASE " ));
        }catch(InfluxDBIOException e){
            System.out.println("error:"+e);
        }
    }*/

    //STATES CHANGE
   // @ContextEntity.State.Push(service = influxService.class,state = influxService.)


    /*public boolean connectionStatus() {
        return super.equals(obj);
    }*/
    @Override
    public String QueryDB(String sensorType, int timeStart, int timeDuration,DBfunction function, int limit){
        if(isInfluxRunning()){

            Query queryDb = new Query("SELECT * FROM temperature WHERE \"type\"='Thermometer' LIMIT 2", DATABASE_NAME);
            QueryResult result=influxDB.query(queryDb);

            if(result.getResults().get(0).getSeries().equals("null")){

            }

            System.out.println("");
            System.out.println(result.getResults());
            System.out.println(result.getError());
            //[Result [series=[Series [name=temperature, tags=null, columns=[time, sensor, type, value, zone], values=[[2019-01-04T16:18:20.983Z, ThermometerExt-452f2020fc, Thermometer, 291.0, none], [2019-01-04T16:44:14.635Z, ThermometerExt-452f2020fc, Thermometer, 291.0, none]]]], error=null]]
            System.out.println(result.getResults().get(0));
            //Result [series=[Series [name=temperature, tags=null, columns=[time, sensor, type, value, zone], values=[[2019-01-04T16:18:20.983Z, ThermometerExt-452f2020fc, Thermometer, 291.0, none], [2019-01-04T16:44:14.635Z, ThermometerExt-452f2020fc, Thermometer, 291.0, none]]]], error=null]
            System.out.println(result.getResults().get(0).getSeries());
            //[Series [name=temperature, tags=null, columns=[time, sensor, type, value, zone], values=[[2019-01-04T16:18:20.983Z, ThermometerExt-452f2020fc, Thermometer, 291.0, none], [2019-01-04T16:44:14.635Z, ThermometerExt-452f2020fc, Thermometer, 291.0, none]]]]
            System.out.println(result.getResults().get(0).getSeries().get(0).getName());
            //temperature
            System.out.println(result.getResults().get(0).getSeries().get(0).getColumns().get(0));
            //time
            System.out.println(result.getResults().get(0).getSeries().get(0).getValues().get(0));

            //[2019-01-04T16:18:20.983Z, ThermometerExt-452f2020fc, Thermometer, 291.0, none]

        }


        //influxDB.query(queryDb,2,)

       // QueryResult result1 = influxDB.query(new Query("CREATE DATABASE " + DATABASE_NAME, DATABASE_NAME));
        //influxDB.setDatabase(DATABASE_NAME);

        return "void";
    }

    @Override
    /**
     * Makes a single write in the influxDB.
     * To write the <strong>temperature</strong> of a <strong>thermometer</strong>
     * that is located in the <strong>kitchen</strong> then
     * singleDBwrite("temp",30,"host=kitchen,name=ThermometerExt-4c8be2e2a1")
     * @param varName name of the measurement
     * @param varValue measurement value
     * @param Parameters aditional parameters of the measure
     */
    public void singleDBwrite(String varName, String varValue, String Parameters){
        if(isInfluxRunning()){
            String query="";
            QueryResult result;
            String timeFormated = String.valueOf(clock.currentTimeMillis())+"000000";
            QueryResult result1 = influxDB.query(new Query("CREATE DATABASE " + DATABASE_NAME, DATABASE_NAME));
            influxDB.setDatabase(DATABASE_NAME);
            query=varName+","+Parameters+" value="+varValue+" "+timeFormated;
            influxDB.write(query);
        }

    }

    /**
     * takes information from every service and sensor and saves it into influx
     * @param Ttime
     */
    @Override
    public void writeAllSensorsState(int Ttime){

        if(isInfluxRunning()){
            String timeFormatted = String.valueOf(clock.currentTimeMillis())+"000000";
            String time=(Ttime==0)?timeFormatted:String.valueOf(Ttime);

            String query="";
            QueryResult result;
            QueryResult result1 = influxDB.query(new Query("CREATE DATABASE " + DATABASE_NAME, DATABASE_NAME));
            //System.out.println(result1);

            influxDB.setDatabase(DATABASE_NAME);
            String CurZone="";

            for (ServiceLayer srv:services) {
                //System.out.println("qos,host="+srv.getServiceName()+" value="+srv.getServiceQoS()+" "+time);
                query = "qos,zone="+srv.getServiceName()+",type=Service,name="+srv.getServiceName()+" value="+srv.getServiceQoS()+" "+time;
               // System.out.println(query);
                influxDB.write(query);
            }
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
                    query = "temperature,zone="+getZoneName(tm)+",type=Thermometer,name="+tm.getSerialNumber()+" value="+tm.getTemperature().getValue()+" "+time;
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
        }
    }


    String getZoneName(GenericDevice device){
        try{
            return ((ZoneService)device).getZone();
        }catch(ClassCastException e){
            return "none";
        }
    }
}
