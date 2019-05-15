package fr.liglab.adele.interop.demonstrator.applications.temperature;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;

import java.util.concurrent.TimeUnit;

import tec.units.ri.quantity.Quantities;
import tec.units.ri.unit.Units;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;

import fr.liglab.adele.icasa.layering.applications.api.ApplicationLayer;

import fr.liglab.adele.icasa.layering.services.location.ZoneService;
import fr.liglab.adele.icasa.layering.services.location.ZoneServiceFunctionalExtension;
import fr.liglab.adele.icasa.location.LocatedObject;

import fr.liglab.adele.interop.services.temperature.TemperatureController;

import fr.liglab.adele.interop.time.series.MeasurementStorage;
import static fr.liglab.adele.interop.time.series.MeasurementStorage.Measurement.*;

import fr.liglab.adele.time.series.SeriesDatabase;
import static fr.liglab.adele.time.series.SeriesDatabase.*;
import static fr.liglab.adele.time.series.SeriesDatabase.Function.*;

import org.influxdb.dto.QueryResult;
import org.joda.time.DateTime;


import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;
import fr.liglab.adele.icasa.clockservice.Clock;

import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.device.temperature.ThermometerExt;

import fr.liglab.adele.iop.device.api.IOPService;

@ContextEntity(coreServices = {ApplicationLayer.class, TemperatureController.class, PeriodicRunnable.class})

@FunctionalExtension(id="ZoneService",contextServices = ZoneService.class, implementation = ZoneServiceFunctionalExtension.class)

public class TemperatureOpenControllerApplication implements ApplicationLayer, TemperatureController, PeriodicRunnable {


	@Requires(id="outside", optional=true, proxy=false, nullable=false)
	private ThermometerExt outside;

	@Requires(id="building", optional=true, proxy=false, nullable=false)
    @ContextRequirement(spec = {IOPService.class})
	private Thermometer building;

    @Requires(id="heater", filter=ZoneService.OBJECTS_IN_ZONE, optional=false,  proxy=false)
    @ContextRequirement(spec = {LocatedObject.class})
    private Heater[] heaters;

    @Unbind(id="heater")
    private void undoundHeater(Heater heater) {
    	heater.setPowerLevel(0);
    }
    
    /**
     * The reference temperature of the controller
     */
    private Quantity<Temperature> reference = Quantities.getQuantity(10.0,Units.CELSIUS).to(Units.KELVIN);
    
    @Override
    public Quantity<Temperature> getReference() {
    	return reference;
    }

    @Override
    public void setReference(Quantity<Temperature> reference) {
    	this.reference = reference.to(Units.KELVIN);
    	reset();
    }

    @Validate
    private void start() {
    	reset();
    }
   

    private void reset() {
    	currentDay = null;
    }


    /**
     * The control loop function
     * @param time 
     */
    
    private void control(DateTime time) {
    	
    	double reference = this.reference.getValue().doubleValue();
    	
    	/*
    	 * use alternative sources in order of preference
    	 */
    	double external = reference;
    	
    	if (outside != null) {
//            System.err.printf("Target\tOutside\tOutput\tError\n");
    		external = outside.getTemperature().getValue().doubleValue();
    	} else if (building != null) {
//            System.err.printf("Target\tBuilding\tOutput\tError\n");
    		external = building.getTemperature().getValue().doubleValue();
    	}
    	else {
//            System.err.printf("Target\tHistorical\tOutput\tError\n");
            external = getRecordedTemperature(time);
            return;
    	}
    	
    	
        double output = estimate(external, reference, time);
        
//      System.err.printf("%3.2f\t%3.2f\t%3.2f\t%3.2f %s \n", reference, external, output, (reference-external), output > 0 &&  Math.signum(reference-external) < 0 ? "****" : "");
        
        double level = output / heaters.length;
		for (Heater	heater : heaters) {
			heater.setPowerLevel(level);
		}

    }
    
    private DateTime currentDay;
    private int activeSlots;
    
    /**
     * A regression based estimation of the output based on the input variables (external temperature and time)
     */
    private double estimate(double external, double reference, DateTime time) {

    	int totalSlots = slots();
    	
    	/*
    	 * The difference between the external and reference temperature, this is the basic
    	 * indicator to estimate the required power level
    	 */
        double percentage =  external / (1.0065* reference);

        /* At the beginning of the day calculate the idle time when no control is performed, as
         * we do not have any feedback we can not know the instant reaction
         */
        if (currentDay == null || currentDay.getDayOfYear() != time.getDayOfYear()) {
        	
        	double idleTimePercentage = -3149.312 + 9767.348*percentage - 10100.98*Math.pow(percentage,2) + 3483.876*Math.pow(percentage,3);
        	
            
            this.activeSlots 	= totalSlots - (int) (totalSlots * idleTimePercentage);
            this.currentDay 	= time;
            
            System.out.println("starting day "+currentDay.getDayOfYear()+" active slots "+activeSlots);
    	}

        /*
         * Use a regression to calculate the estimated value
         */
        double output = 1344.338 - 4158.196*percentage + 4293.299*Math.pow(percentage,2) - 1479.396*Math.pow(percentage,3);

        output = output < 0 ? 0 : output > 1 ? 1 : output;


        /* 
         * check for idle slots
         */
        int slot 			= slot(time);
        boolean isActive 	= slot < activeSlots;

        System.out.println("time "+time+" => slot "+slot +" active "+isActive);
    	
        return  isActive ? output : 0d;

    }

    /**
     * The total number of period slots in a day
     */
    private final int slots() {
    	return (int) (getUnit().convert(1,TimeUnit.DAYS) / getPeriod());
    }
    
    /**
     * The slot number corresponding to a time of day.
     * 
     * Notice we make all the calculation with a precision of minutes, as we suppose that the estimation
     * period is between a minute and a day. 
     */
    private final int slot(DateTime time) {
    	
    	int total	= slots();
    	int slot 	= (int)  (time.getMinuteOfDay() / TimeUnit.MINUTES.convert(getPeriod(),getUnit()));
    	
    	 /* 
    	  * We renumber the slots (first even then odd) so that active time is spread over the whole day  
    	  */
    	if (slot %2 == 0) {
    		slot = slot / 2;
    	}
    	else {
    		slot = ((total + 1) / 2) + ((slot - 1) / 2 );
    	}
    	
    	return  slot;
    }
    
    /**
     * Periodic run execution
     */
    
    @Requires(optional=false, proxy=false)
    private Clock clock;
    
    @Override
	public void run() {
    	try {
    		control(new DateTime(clock.currentTimeMillis()));
		} catch (Exception unexpected) {
			unexpected.printStackTrace();
		}
	}
	
	@Override
	public long getPeriod() {
		return 1;
	}

	@Override
	public TimeUnit getUnit() {
		return TimeUnit.HOURS;
	}


    /**
     * Controller reconfiguration
     */

    @Bind(id="outside")
    private void bindOuside() {
    	reset();
    }

    @Bind(id="building")
    private void bindBuilding() {
    	reset();
    }

    /**
     * Learning algorithm
     */
    @Requires(optional=false, proxy=false)
    private MeasurementStorage storage;
    
	public static final String DATABASE_NAME = "mLearning";

	@Requires(optional=false, proxy=false, filter="(name="+DATABASE_NAME+")")
    private SeriesDatabase historicData;

    private QueryResult referenceYear = null;
    private double lowerMultiplier = 1;
    private double higherAdder = 0;
     
    /**
     * Based on a simple temperature model it returns the estimated temperature in a day
     */
    public double getRecordedTemperature(DateTime time) {

    	DateTime today = time.withTimeAtStartOfDay();

    	/*
    	 * Calculate the closer (most alike) year in the historic data to this year
    	 */
    	if (referenceYear == null) {
    		
            //get the date from the latest available temperature
            QueryResult lastMeasured 	= storage.select(TEMPERATURE, LAST, since(today.minusYears(5)), until(today));
            DateTime timeOfLastMeasure	= asDate(timestamp(lastMeasured));
            
            if (timeOfLastMeasure != null) {
            	
	            //get that temperature
	            double LastTemperature = measure(lastMeasured, 1, 0.0d);
	
	
	            //With that date, get the max and min temperatures in the day
	            QueryResult lastMin = storage.select(TEMPERATURE, MIN, since(timeOfLastMeasure), until(timeOfLastMeasure.plusDays(1)));
	            QueryResult lastMax = storage.select(TEMPERATURE, MAX, since(timeOfLastMeasure), until(timeOfLastMeasure.plusDays(1)));
	            
	            double lastMaximum = measure(lastMax, 1, 0.0d);
	            double lastMinimum = measure(lastMin, 1, 0.0d);
	
	
	            //compare to the temperatures for last 4 years at the same date
	            
	            double closestResemblance = Double.MAX_VALUE;
	            
	            for (int years = 1; years < 5; years++) {
	            	QueryResult reference = historicData.select(MEAN.of("*"), "Temp", since(today.minusYears(years).minusDays(5)), until(today.minusYears(years)));
					
					double tempReference	= Units.CELSIUS.getConverterTo(Units.KELVIN).convert(measure(reference,1,0.0d)).doubleValue();
					double maxReference 	= Units.CELSIUS.getConverterTo(Units.KELVIN).convert(measure(reference,2,0.0d)).doubleValue();
	            	double minReference		= Units.CELSIUS.getConverterTo(Units.KELVIN).convert(measure(reference,3,0.0d)).doubleValue();
					
	            	double resemblance = Math.abs((tempReference - LastTemperature) + (maxReference - lastMaximum) + (minReference - lastMinimum));
	            	
	            	if (resemblance < closestResemblance) {
	            		closestResemblance = resemblance;
	            		referenceYear = reference;
	            	}
				}
	
	            //take the closest temperature reference to the current
	
	            double TemperatureDelta = lastMaximum - lastMinimum;
	            lowerMultiplier = (TemperatureDelta+0.05557039)/3.368699;
	            higherAdder = lastMaximum - temperatureRegression(lowerMultiplier,0,15.0);

	            // calculate the regression
	            return temperatureRegression(lowerMultiplier, higherAdder, time.getHourOfDay()+ (time.getMinuteOfHour() / 60.0d));
            }
    		
    	}
    	
        /* At the beginning of the day recalculate the factors of the day
         */ 
        if (currentDay == null || currentDay.getDayOfYear() != time.getDayOfYear()) {

			double maxReference = Units.CELSIUS.getConverterTo(Units.KELVIN).convert(measure(referenceYear,2,0.0d)).doubleValue();
        	double minReference = Units.CELSIUS.getConverterTo(Units.KELVIN).convert(measure(referenceYear,3,0.0d)).doubleValue();

            double TemperatureDelta = maxReference - minReference;
            lowerMultiplier = (TemperatureDelta+0.05557039)/3.368699;
            higherAdder = maxReference - temperatureRegression(lowerMultiplier,0,15.0);

        }
        

        // calculate the regression
        return temperatureRegression(lowerMultiplier, higherAdder, time.getHourOfDay()+ (time.getMinuteOfHour() / 60.0d));
        
    }

    /**
     * a simple regression curve for the temperature
     */
    private double temperatureRegression(double lowerMultiplier, double higherAdder, double hourOfDay) {

       // List<Object> maxResult = externalTemperature(MAX, "time=")
        return 8.190593 +higherAdder+ 0.2831266*hourOfDay*lowerMultiplier - 0.3401877*Math.pow(hourOfDay,2)*lowerMultiplier + 0.05460142*Math.pow(hourOfDay,3) *lowerMultiplier- 0.003024781*Math.pow(hourOfDay,4)*lowerMultiplier + 0.00005486382*Math.pow(hourOfDay,5)*lowerMultiplier;
    }

}
