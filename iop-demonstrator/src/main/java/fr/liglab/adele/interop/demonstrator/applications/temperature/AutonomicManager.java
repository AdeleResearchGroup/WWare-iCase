package fr.liglab.adele.interop.demonstrator.applications.temperature;

import org.apache.felix.ipojo.annotations.*;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import tec.units.ri.quantity.Quantities;
import tec.units.ri.unit.Units;

import org.apache.felix.service.command.Descriptor;

import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;

import fr.liglab.adele.interop.services.temperature.TemperatureController;
import fr.liglab.adele.icasa.layering.services.location.ZoneService;
import fr.liglab.adele.icasa.location.Zone;

import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.location.LocatedObject;

@Component(immediate=true)

@Provides(specifications={AutonomicManager.class})

public class AutonomicManager {

	
    /**
     * Commands to set the temperature controller parameters
     */

    @Requires(specification=TemperatureController.class, optional=true, proxy=false)
    @ContextRequirement(spec=ZoneService.class)
    private TemperatureController[] validControllers;

    @ServiceProperty(name = "osgi.command.scope", value = "smart-temperature")
    String commandScope;

    @ServiceProperty(name = "osgi.command.function", value = "{}")
    String[] m_function = new String[] {"setReference"};

    @Descriptor("Set the reference temperature for a zone")
    public void setReference(@Descriptor("zone") String zone, @Descriptor("reference") String reference) {

    	for (TemperatureController controller : validControllers) {
			String controlledZone = ((ZoneService)controller).getZone();
			if (controlledZone.equals(zone.trim())) {
				System.out.println("Setting reference temperature for zone "+zone+" at "+reference+" K");
				controller.setReference(Quantities.getQuantity(Double.valueOf(reference),Units.KELVIN));
				return;
			}
		}
    	
		System.out.println("Invalid zone or invalidated controller "+zone);

    }
    
    /**
     * We keep track of the zones in the home and create a new controller for each zone with at least
     * one shutter
     */

    private @Creator.Field(ZoneService.RELATION_ATTACHED_TO) Creator.Relation<ZoneService,Zone> attacher;
    private @Creator.Field Creator.Entity<TemperatureControllerApplication> controllerCreator;

    private void addController(String zone) {

    	String controllerId = "TemperatureController."+zone;
    	if (controllerCreator.getInstance(controllerId) != null) {
    		return;
    	}
    	
		Map<String,Object> properties = new HashMap<>();
		controllerCreator.create(controllerId,properties);
        attacher.link(controllerId,zone);
    } 

    private void removeController(String zone) {
    	
    	String controllerId = "TemperatureController."+zone;
    	if (controllerCreator.getInstance(controllerId) == null) {
    		return;
    	}

    	controllerCreator.delete(controllerId);
        attacher.unlink(controllerId,zone);
    }

    private final Map<Heater,String> heaterLocation = new ConcurrentHashMap<>();

    private static final boolean unknown(String zone) {
    	return zone == null || LocatedObject.LOCATION_UNKNOWN.equals(zone);
    }

    private static final String zone(Heater heater) {
    	return heater instanceof LocatedObject ? ((LocatedObject)heater).getZone() : null;
    }

    @Bind(id ="heater", aggregate =true,  optional=true, proxy=false)
    private void bindHeater(Heater heater) {
    	
    	String zone = zone(heater);
    	if (unknown(zone)) {
        	return;
    	}

    	heaterLocation.put(heater,zone);
    	addController(zone);
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

   		removeController(zone);
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

 
}
