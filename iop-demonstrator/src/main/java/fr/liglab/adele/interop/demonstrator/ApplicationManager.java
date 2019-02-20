package fr.liglab.adele.interop.demonstrator;

import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.icasa.device.light.Photometer;
import fr.liglab.adele.icasa.layering.services.location.ZoneService;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;
import fr.liglab.adele.interop.demonstrator.database.ConstantDatabaseWriteApp;
import fr.liglab.adele.interop.demonstrator.home.lightning.HomeLightningApplication;
import fr.liglab.adele.interop.demonstrator.home.lightning.LightFollowApplication;
import fr.liglab.adele.interop.demonstrator.home.temperature.RoomTemperatureControlApp;
import fr.liglab.adele.interop.demonstrator.smart.shutter.ShutterController;
import fr.liglab.adele.iop.device.api.IOPLookupService;
import fr.liglab.adele.iop.device.api.IOPService;
import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.ipojo.dependency.interceptors.DependencyInterceptor;
import org.apache.felix.ipojo.dependency.interceptors.ServiceRankingInterceptor;
import org.apache.felix.ipojo.dependency.interceptors.ServiceTrackingInterceptor;
import org.apache.felix.ipojo.dependency.interceptors.TransformedServiceReference;
import org.apache.felix.ipojo.util.DependencyModel;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Component(immediate = true)
@Provides(specifications={PeriodicRunnable.class, ServiceTrackingInterceptor.class, ServiceRankingInterceptor.class})

@Instantiate

public class ApplicationManager implements PeriodicRunnable, ServiceTrackingInterceptor, ServiceRankingInterceptor {

	
	 @ServiceProperty(name=DependencyInterceptor.TARGET_PROPERTY, value="(& (factory.name=fr.liglab.adele.interop.demonstrator.smart.shutter.ShutterController) (dependency.id=photometer))")
	 private String targetDependency;
	 
    private @Creator.Field(ZoneService.RELATION_ATTACHED_TO) Creator.Relation<ZoneService,Zone> attacher;

    @Creator.Field Creator.Entity<HomeLightningApplication> lightningApplicationCreator;
    @Creator.Field Creator.Entity<LightFollowApplication> followMeApplicationCreator;
    @Creator.Field Creator.Entity<ConstantDatabaseWriteApp> databaseAppCreator;

    private @Creator.Field Creator.Entity<ShutterController> smartShutterCreator;

    private @Creator.Field Creator.Entity<RoomTemperatureControlApp> roomTemperatureApp;

    public ApplicationManager() {
	}

    @Validate
    public void start(){
    	//lightningApplicationCreator.create("LightApp");
		//followMeApplicationCreator.create("FollowApp");
		roomTemperatureApp.create("TemperatureCloningApp");
        databaseAppCreator.create("DBapp");
    }

    @Invalidate
    public void stop(){
    	//lightningApplicationCreator.delete("LightApp");
		//followMeApplicationCreator.delete("FollowApp");
		roomTemperatureApp.delete("TemperatureConingApp");
    };



    @Bind(id="zones",specification = Zone.class, aggregate = true, optional = true)
    public void bindZone(Zone zone) {
    	
		String instance = "SmartShutterApp."+zone.getZoneName();

		Map<String,Object> properties = new HashMap<>();


		/*smartShutterCreator.create(instance,properties);
        attacher.link(instance,zone);*/

    }

    @Unbind(id="zones")
    public void unbindZone(Zone zone) {
    	
		String instance = "SmartShutterApp."+zone.getZoneName();
		String instance2 = "TemperatureApp.:"+zone.getZoneName();

		/*smartShutterCreator.delete(instance);
        attacher.unlink(instance,zone);*/

        roomTemperatureApp.delete(instance2);
		attacher.unlink(instance2,zone);
    }

 
	@Override
	public long getPeriod() {
		return 15;
	}

	@Override
	public TimeUnit getUnit() {
		return TimeUnit.MINUTES;
	}

    private boolean hasRequestedLookup	= false;
    private boolean hasPhotometer 		= false;

    
    @Bind(id="photometer", optional=true, aggregate=true, proxy=false)
    public void addPhotometer(Photometer photometer) {
    	this.hasPhotometer = true;
    }
    
    @Unbind(id="photometer")
    public void unbind() {
    	this.hasPhotometer = false;
    }

    @Requires(optional = false)
    private IOPLookupService lookup;

	@Override
	public void run() {
		if (!hasRequestedLookup && !hasPhotometer ) {
			System.out.println("je vais faire un lookup vers IOP");
			lookup.consider(new String[] {Photometer.class.getCanonicalName()}, Collections.emptyMap());
			hasRequestedLookup = true;
		}
		
	}

	private static final boolean isRemote(ServiceReference<?> reference) {
		String[] services = (String[]) reference.getProperty(Constants.OBJECTCLASS);
		for (String service : services) {
			if (service.contains(IOPService.class.getCanonicalName())) {
				return true;
			}
		}
		
		return false;
	}

    @SuppressWarnings("rawtypes")
	public static class Ranking implements Comparator<ServiceReference> {


    	@Override
		public int compare(ServiceReference first, ServiceReference second) {
			
			boolean firstIsRemote 	= isRemote(first);
			boolean secondIsRemote 	= isRemote(second);
			
			return 	firstIsRemote && !secondIsRemote ? +1 :
		    		!firstIsRemote && secondIsRemote ? -1 :
		    		first.compareTo(second);
			
		}
    	
    }

	@Override
	public void open(DependencyModel dependency) {
	}

	@Override
	public void close(DependencyModel dependency) {
	}

	@Override
	public <S> TransformedServiceReference<S> accept(DependencyModel dependency, BundleContext context, TransformedServiceReference<S> ref) {
		return ref;
	}

	@Override
	@SuppressWarnings({ "rawtypes" })
	public List<ServiceReference> getServiceReferences(DependencyModel dependency, List<ServiceReference> matching) {
		matching.sort(new Ranking());
		return matching;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public List<ServiceReference> onServiceArrival(DependencyModel dependency, List<ServiceReference> matching,
			ServiceReference<?> reference) {
		return getServiceReferences(dependency,matching);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public List<ServiceReference> onServiceDeparture(DependencyModel dependency, List<ServiceReference> matching,
			ServiceReference<?> reference) {
		return getServiceReferences(dependency,matching);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public List<ServiceReference> onServiceModified(DependencyModel dependency, List<ServiceReference> matching,
			ServiceReference<?> reference) {
		return getServiceReferences(dependency,matching);
	}



}
