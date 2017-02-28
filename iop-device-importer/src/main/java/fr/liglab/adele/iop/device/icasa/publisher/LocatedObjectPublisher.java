package fr.liglab.adele.iop.device.icasa.publisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;

import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;

import org.ow2.chameleon.fuchsia.core.component.AbstractExportManagerComponent;
import org.ow2.chameleon.fuchsia.core.component.ExportManagerIntrospection;
import org.ow2.chameleon.fuchsia.core.component.ExportManagerService;

import de.mannheim.wifo2.iop.service.model.Capability;
import de.mannheim.wifo2.iop.service.model.ICapability;

import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.iop.device.api.IOPService;
import fr.liglab.adele.iop.device.exporter.ServiceDeclaration;

@Component
@Provides(specifications = {ExportManagerService.class, ExportManagerIntrospection.class })
public class LocatedObjectPublisher extends AbstractExportManagerComponent {

	@ServiceProperty(name = Factory.INSTANCE_NAME_PROPERTY)
	private String name;

	protected LocatedObjectPublisher(BundleContext bundleContext) {
		super(bundleContext);
	}
	
	
	@Bind(id="device", proxy = false, optional = true, aggregate = true)
	public void serviceBound(LocatedObject service, Map<String,?> properties) {
		
		if (service instanceof IOPService)
			return;
		
		String id						= String.valueOf((Long) properties.get(org.osgi.framework.Constants.SERVICE_ID));
		List<ICapability> capabilities 	= new ArrayList<>();
		
		for (String provided : (String[])properties.get(org.osgi.framework.Constants.OBJECTCLASS)) {
			capabilities.add(new Capability(provided));
		}
		
		registerExportDeclaration(ServiceDeclaration.from(service,id,capabilities));
	}
	
	@Unbind(id="device")
	public void serviceUnbound(LocatedObject service, Map<String,?> properties) {
		String id						= String.valueOf((Long) properties.get(org.osgi.framework.Constants.SERVICE_ID));
		List<ICapability> capabilities 	= new ArrayList<>();
		
		unregisterExportDeclaration(ServiceDeclaration.from(service,id,capabilities));
	}

	@Override
	public String getName() {
		return name;
	}

	@Validate
	protected void start() {
		super.start();
	}

	@Invalidate
	protected void stop() {
		super.stop();
	}

}
