package fr.liglab.adele.interop.services.legacy.temperature;

import java.util.concurrent.TimeUnit;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import org.apache.felix.ipojo.annotations.Validate;

import fr.liglab.adele.interop.time.series.influx.Database;
import static fr.liglab.adele.interop.time.series.influx.Database.*;
import static fr.liglab.adele.interop.time.series.influx.Database.Function.*;

import org.influxdb.dto.QueryResult;




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

	private static final String DATABASE_NAME = "mLearning";
	
	private final Database database = new Database(DATABASE_NAME);

	private static final String TIMESTAMP_OF_START_DATE = "2019-01-28";
	
	private static final int DAYS_PER_YEAR	= 365;
		
    @Validate
    public void start() {
    	database.setVerifyRunning(true);
        transitionalFunction(TIMESTAMP_OF_START_DATE);
    }
    
    
    public void transitionalFunction(String start) {
    	QueryResult ref2018 = averageValues(start, (1 * DAYS_PER_YEAR) + 5, 		(1 * DAYS_PER_YEAR));
    	QueryResult ref2017 = averageValues(start, (2 * DAYS_PER_YEAR) + 5,			(2 * DAYS_PER_YEAR));
    	QueryResult ref2016 = averageValues(start, (3 * DAYS_PER_YEAR) + 5 + 1, 	(3 * DAYS_PER_YEAR) + 1);
    	QueryResult ref2015 = averageValues(start, (4 * DAYS_PER_YEAR) + 5 + 1, 	(4 * DAYS_PER_YEAR) + 1);
    }
    
    public QueryResult averageValues(String start, int lowerLimit, int upperLimit) {
    	
    	String since = since(expression(quoted(start,true), "-", time(lowerLimit, TimeUnit.DAYS)));
    	String until = until(expression(quoted(start,true), "+", time(upperLimit, TimeUnit.DAYS)));
    	
    	return database.select(MEAN.of("*"),"Temp", 5, since, until);
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
    public int getQoS() {
        return 100;
    }

    @Override
    public String getServiceName() {
        return name;
    }
}
