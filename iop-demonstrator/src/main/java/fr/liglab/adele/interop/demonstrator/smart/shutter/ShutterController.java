package fr.liglab.adele.interop.demonstrator.smart.shutter;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;

import org.apache.felix.ipojo.annotations.Modified;
import org.apache.felix.ipojo.annotations.Requires;

import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;
import fr.liglab.adele.icasa.location.LocatedObject;


import fr.liglab.adele.icasa.layering.applications.api.ApplicationLayer;
import fr.liglab.adele.icasa.layering.services.location.ZoneService;
import fr.liglab.adele.icasa.layering.services.location.ZoneServiceFunctionalExtension;

import tec.units.ri.unit.Units;
import fr.liglab.adele.icasa.device.doorWindow.WindowShutter;
import fr.liglab.adele.icasa.device.light.Photometer;

@ContextEntity(coreServices = {ApplicationLayer.class})

@FunctionalExtension(id="ZoneService",contextServices = ZoneService.class, implementation = ZoneServiceFunctionalExtension.class)


public class ShutterController implements ApplicationLayer {


	@Requires(id="photometer", optional = false, filter = ZoneService.objectInSameZone,	proxy = false)
    @ContextRequirement(spec = {LocatedObject.class})
	private Photometer photometer;
 
    @Requires(id="shutters", optional = true,	filter = ZoneService.objectInSameZone, proxy = false)
    @ContextRequirement(spec = {LocatedObject.class})
    private WindowShutter[] shutters;

    @Modified(id="photometer")
    public void modified() {
		double shutterLevel = photometer.getIlluminance().to(Units.LUX).getValue().doubleValue() >= 1600 ? 0d : 1d;
		
		for (WindowShutter	shutter : shutters) {
			shutter.setShutterLevel(shutterLevel);
		}
    }

}
