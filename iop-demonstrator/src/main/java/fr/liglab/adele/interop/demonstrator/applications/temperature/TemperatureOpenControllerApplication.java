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
import org.joda.time.DateTime;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;

import fr.liglab.adele.icasa.layering.applications.api.ApplicationLayer;

import fr.liglab.adele.icasa.layering.services.location.ZoneService;
import fr.liglab.adele.icasa.layering.services.location.ZoneServiceFunctionalExtension;
import fr.liglab.adele.icasa.location.LocatedObject;

import fr.liglab.adele.interop.services.temperature.TemperatureController;
import fr.liglab.adele.iop.device.api.IOPService;
import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;
import fr.liglab.adele.icasa.clockservice.Clock;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.device.temperature.ThermometerExt;

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
            System.err.printf("Target\tOutside\tOutput\tError\n");
    		external = outside.getTemperature().getValue().doubleValue();
    	} else if (building != null) {
            System.err.printf("Target\tBuilding\tOutput\tError\n");
    		external = building.getTemperature().getValue().doubleValue();
    	}
    	else {
            System.err.printf("Target\tHistorical\tOutput\tError\n");
            return;
    	}
    	
    	
        double output = estimate(external, reference, time);
        
        System.err.printf("%3.2f\t%3.2f\t%3.2f\t%3.2f %s \n", reference, external, output, (reference-external), output > 0 &&  Math.signum(reference-external) < 0 ? "****" : "");
        
        double level = output / heaters.length;
		for (Heater	heater : heaters) {
			heater.setPowerLevel(level);
		}

    }
    
    private DateTime currentDay;
    private int activeSlots;
    
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
     * The slot number corresponding to a time of day. We renumber the slots so that idle time is spread
     * over the whole day. 
     * 
     * Notice we make all the calculation with a precision of minutes, as we suppose that the estimation
     * period is between a minute and a day. 
     */
    private final int slot(DateTime time) {
    	
    	int total	= slots();
    	int slot 	= (int)  (time.getMinuteOfDay() / TimeUnit.MINUTES.convert(getPeriod(),getUnit()));
    	
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
		control(new DateTime(clock.currentTimeMillis()));
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


}
