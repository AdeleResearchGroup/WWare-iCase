package fr.liglab.adele.interop.services.legacy.temperature;

import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.device.temperature.ThermometerExt;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;

import fr.liglab.adele.interop.time.series.MeasurementStorage;
import fr.liglab.adele.interop.time.series.influx.Database;

import static fr.liglab.adele.interop.time.series.MeasurementStorage.*;
import static fr.liglab.adele.interop.time.series.MeasurementStorage.Measurement.*;
import static fr.liglab.adele.interop.time.series.influx.Database.*;
import static fr.liglab.adele.interop.time.series.influx.Database.Function.*;

import org.apache.felix.ipojo.annotations.Requires;

import java.util.List;

@ContextEntity(coreServices = {LearnedHeaterBehavior.class,ServiceLayer.class})
public class LearnedHeaterBehaviorImpl implements LearnedHeaterBehavior, ServiceLayer{

    //SERVICE's STATES

    @ContextEntity.State.Field(service = LearnedHeaterBehavior.class, state = SERVICE_STATUS, value="0")
    private String srvState;

    @ContextEntity.State.Field(service = LearnedHeaterBehavior.class, state = SERVICE_CHANGE, value="0.0")
    private Double srvChange;

    @ContextEntity.State.Field(service = ServiceLayer.class, state = ServiceLayer.NAME)
    public String name;

    //STATES CHANGE
    @ContextEntity.State.Push(service = LearnedHeaterBehavior.class, state = RoomThermometerService.SERVICE_STATUS)
    public Double pushService(Double serviceState){
        return serviceState;
    }

    @ContextEntity.State.Push(service = LearnedHeaterBehavior.class, state = RoomThermometerService.SERVICE_CHANGE)
    public Boolean pushChange(Boolean serviceChange){return serviceChange;}


    @Override
    public String getServiceStatus() {
        return srvState;
    }

    //REQUIREMENTS
    @Requires(optional = false, proxy=false)
    private MeasurementStorage storage;

    /**
     * A temperature point measured by the external thermometer (null if no measure satisfying the filters is found)
     * 
     */
    private List<Object> externalTemperature(Database.Function function, String ... filters) {
    	
    	/*
    	 * The result of the query has the following value structure
    	 * 
    	 * [[time, temperature]]  i.e: [[2019-02-15T14:21:35.447Z, 284.97499999999997]]
    	 */
    	return first(values(storage.select(TEMPERATURE, function, conjunction( ofType(ThermometerExt.class), conjunction(filters) )))); 
    }
    
    /**
     * The heater power level at an specified zone
     * 
     */
    private double heaterLevel(String zone, String ...filters) {

    	/*
    	 * The result of the query has the following value structure
    	 * 
    	 * [[time, powerLvl]]  i.e: [[2019-02-15T14:21:35.447Z, 0.5]]
    	 */
    	List<Object> result = first(values(storage.select(POWER_LEVEL, conjunction( ofType(Heater.class), inZone(zone), conjunction(filters) ))));
    	return result != null ? Double.class.cast(result.get(1)) : 0.0d;
    }

    int iteration = 1;
    double iddleTimePercentage=1;

    /**
     *
     * @param currentTemp
     * @param targetTemp
     * @param zone  a String with the name of the zone the temperature will be set
     * @param iterationsPerDay
     * @return
     */
    @Override
    public double getHeaterPorcentage(double currentTemp, double targetTemp, String zone, int iterationsPerDay) {
        
    	srvState="2.0";

        double internalTempReference = 1.0065*targetTemp;
    	double TempPercentage = currentTemp/internalTempReference;


    	iddleTimePercentage = (iteration==1)?(-3149.312 + 9767.348*TempPercentage - 10100.98*Math.pow(TempPercentage,2) + 3483.876*Math.pow(TempPercentage,3)):iddleTimePercentage;


    	double maxPowerOutput = 1344.338 - 4158.196*TempPercentage + 4293.299*Math.pow(TempPercentage,2) - 1479.396*Math.pow(TempPercentage,3);

    	maxPowerOutput = maxPowerOutput<0?0:maxPowerOutput;
    	maxPowerOutput = maxPowerOutput>1?1:maxPowerOutput;

    	int middleOfIteractions = iterationsPerDay/2;
    	int evenIddleSlots = (int)(iterationsPerDay*iddleTimePercentage);
    	int oddIddleSlots = iterationsPerDay-evenIddleSlots;

    	iteration = (iteration <=iterationsPerDay)?iteration:1;
        System.out.println("temp:"+currentTemp+", iter:"+iteration+"maxPow"+maxPowerOutput);

            //is the iteration an even number?
            if(iteration%2==0){
                //is this n even number less than the number of evenIddleSlots?
                iteration +=1;
                return ((iteration/2)<=evenIddleSlots)? 0:maxPowerOutput;
            }else{
                iteration +=1;
                return ((iteration/2)<=oddIddleSlots)? 0:maxPowerOutput;
            }



        /*
    	
    	List<Object> maxResult = externalTemperature(MAX); 
    	List<Object> minResult = externalTemperature(MIN);

    	if (minResult == null || maxResult == null) {
            System.err.printf("MAX or MIN not found");
    		return 0.0;
    	}





    	double maxTemperature = Double.class.cast(maxResult.get(1));
    	double minTemperature = Double.class.cast(minResult.get(1));
    	
        System.err.printf("Reference\tMin\tMax\n");
        System.err.printf("%3.2f\t%3.2f\t%3.2f\n", currentTemp, minTemperature, maxTemperature);

        
        //ToDO make verification that at the time given from max and min Temp, the Heater did exist
        //checking if ReferenceTemperature is in the range of the saved temperatures...
        
        if( currentTemp > maxTemperature) {
            //return heater % of max temp recorded
            System.out.println("ref to high "+maxResult);
            return heaterLevel(zone, atTime(timestamp(maxResult)));

        }

        if( currentTemp < minTemperature) {
            //return heater% of min temp recorded
            System.out.println("ref to low "+minResult);
            return heaterLevel(zone, atTime(timestamp(minResult)));

        }

        /*
         * Try to find the registered temperature closest to the currentTemp
         */
        /*
        for (double delta = 0.0d; delta < MAX_DELTA; delta += DELTA_INCREMENT) {

        	System.out.println("searching in interval "+delta+".");
        	List<Object> lasInInterval = externalTemperature(LAST, valueIn(currentTemp - delta, currentTemp + delta));
        	
        	if (lasInInterval != null) {
                System.out.println("ref to last value "+lasInInterval);
        		return heaterLevel(zone, atTime(timestamp(lasInInterval)));
        	}
		}

         return 0.0d;*/
    }

    private static final double 	DELTA_INCREMENT = 0.1;
    private static final double 	MAX_DELTA 		= 0.5;
    
    @Override
    public int getQoS() {
        return 100;
    }

    @Override
    public String getServiceName() {
        return name;
    }

}