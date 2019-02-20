package fr.liglab.adele.interop.services.temperature;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import fr.liglab.adele.icasa.physical.abstraction.MomentOfTheDay;
import fr.liglab.adele.interop.services.database.DBfunction;
import fr.liglab.adele.interop.services.database.InfluxService;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.influxdb.dto.QueryResult;

import java.util.List;

@ContextEntity(coreServices = {RoomTemperatureService.class, ServiceLayer.class})

public class AIRoomTemperatureServiceImpl implements RoomTemperatureService, ServiceLayer {

    //Service States
    @ContextEntity.State.Field(service = RoomTemperatureService.class, state = SERVICE_STATUS)
    public String status;

    @ContextEntity.State.Field(service = ServiceLayer.class, state = ServiceLayer.NAME)
    public String name;

    @ContextEntity.State.Field(service = ServiceLayer.class,state = ServiceLayer.SERVICE_QOS)
    private int SrvQoS;

    private static final Integer MIN_QOS = 50;
    private MomentOfTheDay.PartOfTheDay scheduledPeriod = null;

    //implementation functions

    @Override
    public String getServiceStatus() {
        //return status;
        return "stat";
    }

    @Override
    public int getTemp() {
        return 0;
    }

    @Override
    public double getPowerLevel() {
        return 0;
    }

    @Override
    public void setPowerLevel(double powerLevel) {

    }

    //REQUIREMENTS
    @Requires(id="database", optional = false,specification = InfluxService.class)
    private InfluxService DB;

    @Validate
    public void start(){
        transitionalFunction("2019-01-28");
    }
    public void transitionalFunction(String time){
        if(DB.isInfluxRunning()){
            List<QueryResult.Result> ref2018= DB.aiQuery(time," 370d","365d",DBfunction.mean,5);
            List<QueryResult.Result>  ref2017= DB.aiQuery(time," 735d","730d",DBfunction.mean,5);
            List<QueryResult.Result>  ref2016= DB.aiQuery(time," 1101d","1096d",DBfunction.mean,5);
            List<QueryResult.Result>  ref2015= DB.aiQuery(time," 1466d","1461d",DBfunction.mean,5);
        }

       //String ref2017= DB.QueryDB(SensorType.AI,"\""+time+"\" -735d","5d",DBfunction.mean,5);
        //String ref2016= DB.QueryDB(SensorType.AI,"\""+time+"\" -1101d","5d",DBfunction.mean,5);
        //String ref2015= DB.QueryDB(SensorType.AI,"\""+time+"\" -1466d","5d",DBfunction.mean,5);


       // List<QueryResult.Result> test = DB.QueryDB()





      /*  System.out.println("");
        System.out.println(ref2015);
        System.out.println("");
        System.out.println(ref2016);
        System.out.println("");
        System.out.println(ref2017);
        System.out.println("");
        System.out.println(ref2018);*/

    }

    /**
     * Based on a simple temperature model it returns the estimated temperature in a day,
     * providing the higher and lower temperatures in said day
     * @param minTemp
     * @param maxTemp
     * @param time
     * @return
     */
    public double getTemperature(double minTemp,double maxTemp,String time){
        double TemperatureDelta = maxTemp-minTemp;
        double lowerMultiplier = (TemperatureDelta+0.05557039)/3.368699;
        double higherAdder;
        double formattedTime;

        double hour = Integer.parseInt(time.split(":")[0]);
        double minute = Integer.parseInt(time.split(":")[1]);
        formattedTime = hour+(minute/60);

        higherAdder = maxTemp-tempCurve(lowerMultiplier,0,15);
        return tempCurve(lowerMultiplier,higherAdder,formattedTime);
    }

    /**
     * a simple regretion curve
     * @param lowerMultiplier variable
     * @param higherAdder
     * @param formattedTime time of the day from 0(0:00) to 23.5 (11:30pm)
     * @return
     */
    public double tempCurve(double lowerMultiplier,double higherAdder,double formattedTime){
        return 8.190593 +higherAdder+ 0.2831266*formattedTime*lowerMultiplier - 0.3401877*Math.pow(formattedTime,2)*lowerMultiplier + 0.05460142*Math.pow(formattedTime,3) *lowerMultiplier- 0.003024781*Math.pow(formattedTime,4)*lowerMultiplier + 0.00005486382*Math.pow(formattedTime,5)*lowerMultiplier;
    }

    @Override
    public int getMinQos() {
        return MIN_QOS;
    }

    @Override
    public int getServiceQoS() {
        //return SrvQoS;
        return 0;
    }

    @Override
    public String getServiceName() {
        return name;
    }
}
