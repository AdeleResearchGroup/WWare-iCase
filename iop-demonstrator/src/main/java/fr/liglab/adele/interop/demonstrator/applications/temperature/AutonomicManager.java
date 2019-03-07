package fr.liglab.adele.interop.demonstrator.applications.temperature;

import org.apache.felix.ipojo.annotations.*;

import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;

import tec.units.ri.quantity.Quantities;
import tec.units.ri.unit.Units;

import org.apache.felix.service.command.Descriptor;

import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;

import fr.liglab.adele.interop.services.temperature.TemperatureController;

import fr.liglab.adele.iop.device.api.IOPLookupService;
import fr.liglab.adele.icasa.layering.applications.api.ApplicationLayer;
import fr.liglab.adele.icasa.layering.services.location.ZoneService;
import fr.liglab.adele.icasa.location.Zone;

import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.location.LocatedObject;

@Component(immediate=true)

@Provides(specifications={AutonomicManager.class, PeriodicRunnable.class})

public class AutonomicManager implements PeriodicRunnable {

	/**
	 * The current state of the components controlling the zone
	 *
	 */
	private class ZoneState {
		
		private final String zone;

	    private volatile TemperatureController primary;

	    private volatile TemperatureController fallback;

	    public ZoneState(String zone) {
	    	this.zone = zone;
	    } 

	    public void init() {

	    	String controllerId = "TemperatureController."+zone;
	    	if (controllerCreator.getInstance(controllerId) != null) {
	    		return;
	    	}
	    	
			Map<String,Object> properties = new HashMap<>();
			controllerCreator.create(controllerId,properties);
	        attacher.link(controllerId,zone);
	    	
	    }

	    public void dispose() {
	    	this.primary 	= null;
	    	this.fallback 	= null;
	    }

	    public void activate(TemperatureController controller) {
	    	
	    	this.primary = controller;
	    	
	    	if (reference != null) {
	    		this.primary.setReference(reference);
	    	}
	    	
	    	this.fallback = null;
	    	
	    	String controllerId = "TemperatureController.Degraded."+zone;
	    	if (fallbackControllerCreator.getInstance(controllerId) == null) {
	    		return;
	    	}

	    	fallbackControllerCreator.delete(controllerId);
	        attacher.unlink(controllerId,zone);

	    }

	    public void deactivate() {
	    	this.primary 	= null;

	    	String controllerId = "TemperatureController.Degraded."+zone;
	    	if (fallbackControllerCreator.getInstance(controllerId) != null) {
	    		return;
	    	}
	    	
			Map<String,Object> properties = new HashMap<>();
			fallbackControllerCreator.create(controllerId,properties);
	        attacher.link(controllerId,zone);
	    	
	    }

		public void degraded(TemperatureController fallback) {
	    	this.fallback = fallback;
	    	if (reference != null) {
	    		this.fallback.setReference(reference);
	    	}
		}

	    public boolean isActive() {
	    	return primary != null;
	    }

		private Quantity<Temperature> reference;

		public void setReference(Quantity<Temperature> reference) {
			
			this.reference = reference;
			
			if (primary != null) {
				primary.setReference(reference);
			}

			if (fallback != null) {
				fallback.setReference(reference);
			}

		}



	}
	
	/**
	 * The current state of the controllable zones
	 */
	private final Map<String,ZoneState> states = new ConcurrentHashMap<>();
	
    /**
     * Commands to set the temperature controller parameters
     */

    @ServiceProperty(name = "osgi.command.scope", value = "smart-temperature")
    String commandScope;

    @ServiceProperty(name = "osgi.command.function", value = "{}")
    String[] m_function = new String[] {"setReference"};

    @Descriptor("Set the reference temperature for a zone")
    public void setReference(@Descriptor("zone") String zone, @Descriptor("reference") String reference) {

    	ZoneState state = states.get(zone);
    	
    	if (state == null) {
    		System.out.println("Invalid zone or invalidated controller "+zone);
    		return;
    	}
		
    	state.setReference(Quantities.getQuantity(Double.valueOf(reference),Units.KELVIN));
    }
    
    /**
     * We keep track of the zones in the home and create a new controller for each zone with at least
     * one heater
     */

    private @Creator.Field(ZoneService.RELATION_ATTACHED_TO) Creator.Relation<ZoneService,Zone> attacher;
    private @Creator.Field Creator.Entity<TemperatureControllerApplication> controllerCreator;
    private @Creator.Field Creator.Entity<TemperatureOpenControllerApplication> fallbackControllerCreator;

    private final Map<Heater,String> heaterLocation = new ConcurrentHashMap<>();


    @Bind(id ="heater", aggregate =true,  optional=true, proxy=false)
    private void bindHeater(Heater heater) {
    	
    	String zone = zone(heater);
    	if (unknown(zone)) {
        	return;
    	}

    	heaterLocation.put(heater,zone);
    	
    	ZoneState state = states.get(zone);
    	if (state == null) {
    		state =  this.new ZoneState(zone);
    		states.put(zone, state);
    		state.init();
    	}
    }

    @Unbind(id ="heater")
    private void unbindHeater(Heater heater) {
    	
    	String zone = heaterLocation.remove(heater);
    	
    	/*
    	 * If it is not the last heater in a zone just ignore the event
    	 */
    	if (unknown(zone) || heaterLocation.containsValue(zone)) {
    		return;
    		
    	}

    	ZoneState state = states.remove(zone);
    	state.dispose();
    }

    @Modified(id ="heater")
    private void modifiedHeater(Heater heater) {

    	String previousZone = heaterLocation.get(heater);
    	String currentZone 	= zone(heater);
    	
    	if (unknown(previousZone) && !unknown(currentZone)) {
    		bindHeater(heater);
    	}
    	else if (unknown(currentZone) && !unknown(previousZone)) {
    		unbindHeater(heater);
    	}
    	else if (!unknown(currentZone) && !unknown(previousZone) && !currentZone.equals(previousZone)) {
    		unbindHeater(heater);
    		bindHeater(heater);
    	}
    	
    	
    }

    /**
     * Track the controllers (primary and fallback) associated to  each zone
     */

    @Requires(id="controller", specification=TemperatureController.class, filter="(factory.name=fr.liglab.adele.interop.demonstrator.applications.temperature.TemperatureControllerApplication)", optional=true, proxy=false)
    @ContextRequirement(spec=ZoneService.class)
    private TemperatureController[] controllers;

    @Bind(id="controller")
    private void bindController(TemperatureController controller) {

    	String zone = zone(controller);
    	if (unknown(zone)) {
    		return;
    	}
    	
    	ZoneState state = states.get(zone);
    	if (state != null) {
    		state.activate(controller);
    	}
    }

    @Unbind(id="controller")
    private void unbindController(TemperatureController controller) {

    	String zone = zone(controller);
    	if (unknown(zone)) {
    		return;
    	}
    	
    	ZoneState state = states.get(zone);
    	if (state != null) {
    		state.deactivate();
    	}
    }

    @Requires(id="fallback", specification=TemperatureController.class, filter="(factory.name=fr.liglab.adele.interop.demonstrator.applications.temperature.TemperatureOpenControllerApplication)", optional=true, proxy=false)
    @ContextRequirement(spec=ZoneService.class)
    private TemperatureController[] fallbackControllers;

    @Bind(id="fallback")
    private void bindFallbackController(TemperatureController controller) {

    	String zone = zone(controller);
    	if (unknown(zone)) {
    		return;
    	}
    	
    	ZoneState state = states.get(zone);
    	if (state != null) {
    		state.degraded(controller);
    	}
    }


    /**
     * Automatic adaptation by looking up services via IOP to try to activate controllers that are invalid waiting for
     * a thermometer
     */

    @Requires(optional=false, proxy=false)
    private IOPLookupService lookup;

	@Override
	public long getPeriod() {
		return 2;
	}

	@Override
	public TimeUnit getUnit() {
		return TimeUnit.HOURS;
	}

	private static final String[] LOOKUP_SERVICES = {Thermometer.class.getCanonicalName()};
	
	private boolean lookupRequested = false;
	
	@Override
	public void run() {
		
		boolean allActive = true;
		
		for (ZoneState state : states.values()) {
			allActive = allActive && state.isActive();
		}
		
		if (!allActive && !lookupRequested) {
			System.out.println("Requesting IOP services");
			lookup.consider(LOOKUP_SERVICES, Collections.emptyMap());
			lookupRequested = true;
		}
		
	}

    private static final boolean unknown(String zone) {
    	return zone == null || LocatedObject.LOCATION_UNKNOWN.equals(zone);
    }

    private static final String zone(GenericDevice device) {
    	return device instanceof LocatedObject ? ((LocatedObject)device).getZone() : null;
    }

    private static final String zone(ApplicationLayer application) {
    	return application instanceof ZoneService ? ((ZoneService)application).getZone() : null;
    }

}
