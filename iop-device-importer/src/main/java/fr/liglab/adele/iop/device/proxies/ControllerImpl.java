/**
 *
 *   Copyright 2011-2013 Universite Joseph Fourier, LIG, ADELE Research
 *   Group Licensed under a specific end user license agreement;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://adeleresearchgroup.github.com/iCasa/snapshot/license.html
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.iop.device.proxies;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.fuchsia.core.component.AbstractDiscoveryComponent;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryIntrospection;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mannheim.wifo2.iop.eventing.IEvent;
import de.mannheim.wifo2.iop.eventing.event.EventID;
import de.mannheim.wifo2.iop.eventing.event.essential.IApplicationEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.IApplicationResponseEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.ILookupEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.ILookupResponseEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.IRegistrationEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.impl.ApplicationEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.impl.ApplicationResponseEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.impl.LookupResponseEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.impl.RegistrationEvent;
import de.mannheim.wifo2.iop.identifier.IEndpointID;
import de.mannheim.wifo2.iop.identifier.ILocalServiceID;
import de.mannheim.wifo2.iop.identifier.IServiceID;
import de.mannheim.wifo2.iop.identifier.LocalServiceID;
import de.mannheim.wifo2.iop.identifier.PluginID;
import de.mannheim.wifo2.iop.plugin.APlugin;
import de.mannheim.wifo2.iop.service.LocalService;
import de.mannheim.wifo2.iop.service.functionality.Call;
import de.mannheim.wifo2.iop.service.functionality.ICall;
import de.mannheim.wifo2.iop.service.functionality.IParameter;
import de.mannheim.wifo2.iop.service.functionality.Parameter;
import de.mannheim.wifo2.iop.service.model.Capability;
import de.mannheim.wifo2.iop.service.model.ICapability;
import de.mannheim.wifo2.iop.service.model.IServiceDescription;
import de.mannheim.wifo2.iop.system.IEnqueue;
import de.mannheim.wifo2.iop.util.datastructure.Queue;
import de.mannheim.wifo2.iop.util.datastructure.UniqueHashMap;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.iop.device.api.IOPController;
import fr.liglab.adele.iop.device.api.IOPDevice;
import fr.liglab.adele.iop.device.importer.IOPServiceDeclaration;


@ContextEntity(services = {IOPController.class,IOPController.class})
@Provides(specifications = { DiscoveryService.class, DiscoveryIntrospection.class })

public class ControllerImpl extends AbstractDiscoveryComponent implements IOPController,IEnqueue, Runnable   {

	private static final Logger LOG = LoggerFactory.getLogger(ControllerImpl.class);

	@Property(name=fr.liglab.adele.cream.model.ContextEntity.CONTEXT_ENTITY_ID)
	private String contextId;

	private ILocalServiceID sourceServiceId;
	
	@ContextEntity.State.Field(service = IOPController.class, state = IOPController.BROADCAST_ADDRESS)
	private String broadcast;

	@ContextEntity.State.Field(service = IOPController.class, state = IOPController.BROADCAST_PORT)
	private int port;

	private Thread mThread;
	private Queue<IEvent> mQueue;
	private boolean mIsRunning;

	private UniqueHashMap<IServiceDescription,BinaryLight> exportedServices = new UniqueHashMap<>();
	private Map<Integer,IApplicationEvent> pendingInvocations = new ConcurrentHashMap<>();
	private Map<Integer,Object> pendingResponses = new ConcurrentHashMap<>();

	private FuchsiaThread fuchsiaThread ;
	
	@Bind(id="binaryLights", proxy = false, optional = true, aggregate = true)
	public void lightBound(BinaryLight light, Map<String,?> properties) {
		
		if (light instanceof IOPDevice)
			return;
		
		String contextId			= String.valueOf((Long) properties.get("service.id"));
		LocalServiceID serviceId 	= new LocalServiceID(rosePlugin.getID().getDeviceID(), contextId);
		
		ICapability simpleLight		= new Capability("SimpleLight");
		IServiceDescription service = new LocalService(serviceId,contextId, Collections.singletonList(simpleLight), Collections.emptyList());
		
		exportedServices.put(service, light);
	}
	
	@Unbind(id="binaryLights", proxy = false, optional = true)
	public void lightUnbound(BinaryLight light, Map<String,?> properties) {
		exportedServices.removeByValue(light);
	}

	/**
	 * Constructor
	 */
	protected ControllerImpl(BundleContext bundleContext) {
		super(bundleContext);
	}
	
	@Override
	public void enqueue(IEvent message) {
		mQueue.enqueue(message);
	}

	@Override
	public void run() {
		while(mIsRunning)  {
			if(!mQueue.isEmpty())  {				
				IEvent event = mQueue.dequeue();

				System.out.println("IOP CONTROLLER event arrived "+event);
				
				switch(event.getType()) {
				case IEvent.EVENT_LOOKUPRESPONSE: {
					ILookupResponseEvent response = (ILookupResponseEvent) event;
					for(IServiceDescription service : response.getServices()) {
						if (fuchsiaThread != null) {
							fuchsiaThread.createDeclaration(service.getId(), service.getCapabilities().stream().map(ICapability::getName).collect(Collectors.toList()));
						}
					}
					break;
				}
				case IEvent.EVENT_LOOKUP : {
					ILookupEvent lookupEvent = (ILookupEvent) event;
					List<? extends IServiceDescription> matchedServices = new Vector<>(exportedServices.keySet());
					
					ILookupResponseEvent responseEvent = new LookupResponseEvent(
							new PluginID("LookResponseEventGenerator", rosePlugin.getID().getDeviceID()), lookupEvent.getID(), 
							(IEndpointID)lookupEvent.getTargetID(), (IEndpointID)lookupEvent.getSourceID(), 
							matchedServices);

					rosePlugin.enqueue(responseEvent);
					break;
				}
				case IEvent.EVENT_APPLICATION : {
					IApplicationEvent invocation = (IApplicationEvent) event;
					//incoming
					if (rosePlugin != null && invocation.getSource().equals(rosePlugin.getID())) {
						IServiceDescription serviceId = new LocalService((ILocalServiceID)invocation.getTargetID(),null, null, Collections.emptyList());
						BinaryLight light = exportedServices.getValue(serviceId);
						if (light != null) {
							if (invocation.getCall().getSignature().equalsIgnoreCase("turnOff")) {
								light.turnOff();
							}
							if (invocation.getCall().getSignature().equalsIgnoreCase("turnOn")) {
								light.turnOn();
							}
							if (invocation.getCall().getSignature().equalsIgnoreCase("getPowerStatus")) {
								boolean result = light.getPowerStatus();
								
								
								PluginID componentId = new PluginID("ApplicationResponseEventGenerator", rosePlugin.getID().getDeviceID());
								ICall call = new Call(null, Collections.singletonList(new Parameter("result", result)), null);
								IApplicationResponseEvent responseEvent = new ApplicationResponseEvent(
																			componentId,
																			invocation.getID(),
																			(IServiceID)invocation.getTargetID(), (IServiceID)invocation.getSourceID(), 
																			call );
								rosePlugin.enqueue(responseEvent);								
							}
						}
					}
					//outgoing
					else {
						rosePlugin.getConnectionManager().send(invocation);
					}
					break;
				}
				case IEvent.EVENT_APPLICATIONRESPONSE : {
					IApplicationResponseEvent response = (IApplicationResponseEvent) event;
					System.out.println("application response "+response);
					Object result = null;
					for (IParameter parameter : response.getResponse().getParameters()) {
						if (parameter.getKey().equalsIgnoreCase("result")) {
							result = parameter.getValue();
						}
					}
					
					if (result != null)
						pendingResponses.put(response.getID(), result);
					
					IApplicationEvent invocation = pendingInvocations.remove(response.getID());
					if (invocation != null) {
						synchronized(invocation) {
							invocation.notifyAll();
						}
						
					}
					break;
				}
				case IEvent.EVENT_DEVICE_REGISTRATION : {
					System.out.println("Device regsitered "+event);
					break;
				}
				}
			}
			else  {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		System.out.println("**********************************************");
	}

	@Override
	public Object invoke(IServiceID target, ICall call) {
		int eventId = EventID.getInstance().getNextID();
		ApplicationEvent invocation = new ApplicationEvent(new PluginID("InvocationGenerator", rosePlugin.getID().getDeviceID()), eventId, sourceServiceId, target, call);

		if (!call.expectsResult()) {
			mQueue.enqueue(invocation);
		}
		else {
			pendingInvocations.put(eventId,invocation);
			mQueue.enqueue(invocation);

			if (pendingInvocations.containsKey(eventId)) {
				synchronized(invocation) {
					try {
						invocation.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
			return pendingResponses.remove(eventId);
		}
		
		return null;
	}
	
	
	private APlugin rosePlugin;
	
	/**
	 * LifeCycle
	 */
	@Validate
	protected synchronized void start() {
		super.start();

		LOG.debug("Starting IOP Controller");

		fuchsiaThread = new FuchsiaThread();
		fuchsiaThread.setDaemon(true);
		fuchsiaThread.start();

		mIsRunning = false;
		mQueue = new Queue<IEvent>();
		
		mThread = new Thread(this);
		mThread.setDaemon(true);
		mThread.start();
		mIsRunning = true;
		
		rosePlugin = new APlugin("iCasa over IOP", this, null);
		rosePlugin.start();

		sourceServiceId = new LocalServiceID(rosePlugin.getID().getDeviceID(), contextId);
		
		rosePlugin.enqueue(new RegistrationEvent(rosePlugin.getID(), new LocalService(sourceServiceId, this.getName(), Collections.emptyList(), Collections.emptyList())));
		
	}

	@Invalidate
	protected synchronized void stop() {
		super.stop();

		mIsRunning = false;
		rosePlugin.stop();
		
		fuchsiaThread.dispose();
		fuchsiaThread = null;
	}


	@Override
	public String getName() {
		return "IOPDeviceDiscovery";
	}

	


	private class FuchsiaThread extends Thread {
		public FuchsiaThread() {
			super("FuchsiaThread");
		}
		
		private Map<IServiceID,ImportDeclaration> declarations = new HashMap<>();

		private BlockingQueue<ImportDeclaration > pendingDeclarations = new LinkedBlockingQueue<>();
		
				
		public synchronized void dispose() {
			declarations.clear();
			declarations = null;
			pendingDeclarations.clear();
		}

		@Override
		public void run() {
			while (declarations != null) {
				
				try {
					ImportDeclaration declaration = pendingDeclarations.take();
					registerImportDeclaration(declaration);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}

		public final void createDeclaration(IServiceID serviceId, List<String> interfacesId) {

			if (declarations.containsKey(serviceId)) {
				return;
			}
			
			ImportDeclaration declaration = ImportDeclarationBuilder.empty()
					.key("scope").value("generic")
					.key(IOPServiceDeclaration.SERVICE_ID).value(serviceId)
					.key(IOPServiceDeclaration.INTERFACE_ID).value(interfacesId)
					.build();

			declarations.put(serviceId,declaration);
			try {
				pendingDeclarations.put(declaration);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		@SuppressWarnings("unused")
		private final void removeDeclaration(IServiceID serviceId) {
			ImportDeclaration declaration = declarations.remove(serviceId);
			if (declaration != null) {
				unregisterImportDeclaration(declaration);
			}
		}
		

	}

}
