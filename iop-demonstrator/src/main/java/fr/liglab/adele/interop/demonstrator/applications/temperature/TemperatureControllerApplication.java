package fr.liglab.adele.interop.demonstrator.applications.temperature;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;

import java.util.concurrent.TimeUnit;
import tec.units.ri.quantity.Quantities;
import tec.units.ri.unit.Units;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.BindingPolicy;
import org.apache.felix.ipojo.annotations.Invalidate;
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
import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;

import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.device.temperature.Thermometer;

@ContextEntity(coreServices = {ApplicationLayer.class, TemperatureController.class, PeriodicRunnable.class})

@FunctionalExtension(id="ZoneService",contextServices = ZoneService.class, implementation = ZoneServiceFunctionalExtension.class)

public class TemperatureControllerApplication implements ApplicationLayer, TemperatureController, PeriodicRunnable {

	
	@Requires(id="thermometer", filter=ZoneService.OBJECTS_IN_ZONE, optional=false, proxy=false, policy= BindingPolicy.DYNAMIC_PRIORITY)
    @ContextRequirement(spec = {LocatedObject.class})
	private Thermometer thermometer;
 
    @Requires(id="heater", filter=ZoneService.OBJECTS_IN_ZONE, optional=false,  proxy=false)
    @ContextRequirement(spec = {LocatedObject.class})
    private Heater[] heaters;

    @Unbind(id="heater")
    private void unboundHeater(Heater heater) {
    	heater.setPowerLevel(0);
    }
    
    @Invalidate
    private void stop() {
    	if (heaters != null) {
    		for (Heater heater : heaters) {
    			heater.setPowerLevel(0);
			}
    	}
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
   
    /**
     * The PID controller to keep track of the feedback loop
     */
    private MiniPID pid = new MiniPID(0.1, 0.1, 0.6);

    private void reset() {
    	
    	pid.setOutputLimits(0,1);
    	pid.setSetpointRange(10);
    	pid.setSetpoint(reference.getValue().doubleValue());
    	
    	pid.reset();

    }


    /**
     * The control loop function
     */
    
    private void control() {
    	
    	double reference	= this.reference.getValue().doubleValue();
        double actual 		= thermometer.getTemperature().to(Units.KELVIN).getValue().doubleValue();
        double output 		= pid.getOutput(actual);
        
        System.err.printf("Target\tActual\tOutput\tError\n");
        System.err.printf("%3.2f\t%3.2f\t%3.2f\t%3.2f %s\n", reference, actual, output, (reference-actual), output > 0 &&  Math.signum(reference-actual) < 0 ? "****" : "");
        
        double level		= output / heaters.length;
		for (Heater	heater : heaters) {
			heater.setPowerLevel(level);
		}

    }

    /**
     * Periodic run execution
     */
    
    @Override
	public void run() {
		control();
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
	
    @Bind(id="thermometer")
    private void bind() {
    	reset();
    }

}
