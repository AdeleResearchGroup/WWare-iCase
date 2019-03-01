package fr.liglab.adele.interop.demonstrator;

import org.apache.felix.ipojo.annotations.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

import org.apache.felix.ipojo.util.DependencyModel;
import org.apache.felix.ipojo.dependency.interceptors.DependencyInterceptor;
import org.apache.felix.ipojo.dependency.interceptors.ServiceRankingInterceptor;

import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;

import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;

import fr.liglab.adele.interop.demonstrator.applications.lightning.HomeLightningApplication;
import fr.liglab.adele.interop.demonstrator.applications.shutter.ShutterControllerApplication;
import fr.liglab.adele.interop.services.shutter.ShutterController;
import fr.liglab.adele.icasa.layering.services.location.ZoneService;
import fr.liglab.adele.icasa.location.Zone;

import fr.liglab.adele.icasa.device.doorWindow.WindowShutter;
import fr.liglab.adele.icasa.device.light.Photometer;
import fr.liglab.adele.icasa.location.LocatedObject;


import fr.liglab.adele.iop.device.api.IOPLookupService;
import fr.liglab.adele.iop.device.api.IOPService;

import org.apache.felix.service.command.Descriptor;


@Component(immediate=true)

@Provides(specifications={ApplicationManager.class, PeriodicRunnable.class, ServiceRankingInterceptor.class})

public class ApplicationManager implements PeriodicRunnable, ServiceRankingInterceptor {

	
    /**
     * Automatically start home lightning
     */
    private @Creator.Field Creator.Entity<HomeLightningApplication> lightningApplicationCreator;

    @Validate
    private void start() {
    	lightningApplicationCreator.create("HomeLightning");
    }

    @Invalidate
    private void stop() {
    	lightningApplicationCreator.delete("HomeLightning");
    };

    /**
     * Commands to set the shutter controller parameters
     */

    @Requires(specification=ShutterController.class, optional=true, proxy=false)
    @ContextRequirement(spec=ZoneService.class)
    private ShutterController[] validControllers;

    @ServiceProperty(name = "osgi.command.scope", value = "smart-shutter")
    String commandScope;

    @ServiceProperty(name = "osgi.command.function", value = "{}")
    String[] m_function = new String[] {"setThreshold"};

    @Descriptor("Set the smart shutter threshold for a zone")
    public void setThreshold(@Descriptor("zone") String zone, @Descriptor("threshold") String threshold) {

    	for (ShutterController controller : validControllers) {
			String controlledZone = ((ZoneService)controller).getZone();
			if (controlledZone.equals(zone.trim())) {
				System.out.println("Setting threshlod for zone "+zone+" at "+threshold);
				controller.setThreshold(Double.valueOf(threshold));
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
    private @Creator.Field Creator.Entity<ShutterControllerApplication> controllerCreator;

    private void addController(String zone) {

    	String controllerId = "SmartShutterApp."+zone;
    	if (controllerCreator.getInstance(controllerId) != null) {
    		return;
    	}
    	
		Map<String,Object> properties = new HashMap<>();
		controllerCreator.create(controllerId,properties);
        attacher.link(controllerId,zone);
    } 

    private void removeController(String zone) {
    	
    	String controllerId = "SmartShutterApp."+zone;
    	if (controllerCreator.getInstance(controllerId) == null) {
    		return;
    	}

    	controllerCreator.delete(controllerId);
        attacher.unlink(controllerId,zone);
    }

    private final Map<WindowShutter,String> shutterLocation = new ConcurrentHashMap<>();

    private static final boolean unknown(String zone) {
    	return zone == null || LocatedObject.LOCATION_UNKNOWN.equals(zone);
    }

    private static final String zone(WindowShutter shutter) {
    	return shutter instanceof LocatedObject ? ((LocatedObject)shutter).getZone() : null;
    }

    @Bind(id ="shutter", aggregate =true,  optional=true, proxy=false)
    private void bindShutter(WindowShutter shutter) {
    	
    	String zone = zone(shutter);
    	if (unknown(zone)) {
        	return;
    	}

    	shutterLocation.put(shutter,zone);
    	addController(zone);
    }

    @Unbind(id ="shutter")
    private void unbindShutter(WindowShutter shutter) {
    	
    	String zone = shutterLocation.remove(shutter);
    	
    	/*
    	 * If it is not the last shutter in a zone just ignore the event
    	 */
    	if (unknown(zone) || shutterLocation.containsValue(zone)) {
    		return;
    		
    	}

   		removeController(zone);
    }

    @Modified(id ="shutter")
    private void modifiedShutter(WindowShutter shutter) {

    	String previousZone = shutterLocation.get(shutter);
    	String currentZone 	= zone(shutter);
    	
    	if (unknown(previousZone) && !unknown(currentZone)) {
    		bindShutter(shutter);
    	}
    	else if (unknown(currentZone) && !unknown(previousZone)) {
    		unbindShutter(shutter);
    	}
    	else if (!unknown(currentZone) && !unknown(previousZone) && !currentZone.equals(previousZone)) {
    		unbindShutter(shutter);
    		bindShutter(shutter);
    	}
    	
    	
    }

    /**
     * Automatic adaptation by looking up services via IOP to try to activate controllers that are invalid waiting for
     * a photometer
     */


    @Requires(optional=false, proxy=false)
    private IOPLookupService lookup;

	@Override
	public long getPeriod() {
		return 15;
	}

	@Override
	public TimeUnit getUnit() {
		return TimeUnit.MINUTES;
	}

	private static final String[] LOOKUP_SERVICES = {Photometer.class.getCanonicalName()};
	
	private boolean lookupRequested = false;
	@Override
	public void run() {
		
		Set<String> validIds = new HashSet<>();
		for (ShutterController activeController : validControllers) {
			validIds.add(controllerCreator.id(activeController));
		}
		
		boolean allActive = controllerCreator.identifiers().equals(validIds);
		
		if (!allActive && !lookupRequested) {
			System.out.println("Requersting IOP services");
			lookup.consider(LOOKUP_SERVICES, Collections.emptyMap());
			lookupRequested = true;
		}
		
	}

	/**
	 * Service ranking implementation injected to prefer local devices over IOP discovered services
	 */
	@ServiceProperty(name=DependencyInterceptor.TARGET_PROPERTY, value="(& (factory.name=fr.liglab.adele.interop.demonstrator.applications.shutter.ShutterControllerApplication) (dependency.id=photometer))")
	private String targetDependency;


	@Override
	public void open(DependencyModel dependency) {
	}

	@Override
	public void close(DependencyModel dependency) {
	}

	public static final <S> boolean isRemote(ServiceReference<S> reference) {
		String[] services = (String[]) reference.getProperty(Constants.OBJECTCLASS);
		for (String service : services) {
			if (service.contains(IOPService.class.getCanonicalName())) {
				return true;
			}
		}
		
		return false;
	}

	public static final <S> int getQualityOfService(ServiceReference<S> reference) {
		return isRemote(reference) ? 0 : 100;
	}

	public static final <S> Comparator<ServiceReference<S>> ranking() {
		return Comparator.comparingInt(ApplicationManager::<S>getQualityOfService).reversed().thenComparing(Comparator.naturalOrder());
	} 

	@SuppressWarnings("unchecked")
	private static final <S, R extends ServiceReference<S>> Comparator<R> raw(Comparator<ServiceReference<S>> comparator) {
		return (Comparator<R> ) comparator;
	} 

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Comparator<ServiceReference> RANKING = raw(ranking());
	

	@Override
	@SuppressWarnings("rawtypes")
	public  List<ServiceReference> getServiceReferences(DependencyModel dependency, List<ServiceReference> matching) {
		List<ServiceReference> sorted = new ArrayList<>(matching);
		sorted.sort(RANKING);
		return sorted;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public List<ServiceReference> onServiceArrival(DependencyModel dependency, List<ServiceReference> matching,
			ServiceReference<?> reference) {
		return getServiceReferences(dependency, matching);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public List<ServiceReference> onServiceDeparture(DependencyModel dependency, List<ServiceReference> matching,
			ServiceReference<?> reference) {
		return getServiceReferences(dependency, matching);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public List<ServiceReference> onServiceModified(DependencyModel dependency, List<ServiceReference> matching,
			ServiceReference<?> reference) {
		return getServiceReferences(dependency, matching);
	}



}
