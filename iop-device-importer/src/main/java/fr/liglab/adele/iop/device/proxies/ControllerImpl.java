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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.osgi.framework.BundleContext;

import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;

import org.ow2.chameleon.fuchsia.core.component.AbstractDiscoveryComponent;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryIntrospection;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;

import de.mannheim.wifo2.iop.eventing.IEvent;
import de.mannheim.wifo2.iop.eventing.event.EventID;
import de.mannheim.wifo2.iop.eventing.event.essential.IApplicationEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.IApplicationResponseEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.ILookupEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.ILookupResponseEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.impl.ApplicationEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.impl.ApplicationResponseEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.impl.LookupEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.impl.LookupResponseEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.impl.RegistrationEvent;
import de.mannheim.wifo2.iop.functions.matching.IMatchRequest;
import de.mannheim.wifo2.iop.functions.matching.SimpleMatchRequest;
import de.mannheim.wifo2.iop.identifier.IComponentID;
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
import de.mannheim.wifo2.iop.service.model.ICapability;
import de.mannheim.wifo2.iop.service.model.IServiceDescription;
import de.mannheim.wifo2.iop.system.IEnqueue;
import de.mannheim.wifo2.iop.util.datastructure.Queue;


import fr.liglab.adele.iop.device.api.IOPController;
import fr.liglab.adele.iop.device.api.IOPInvocationHandler;
import fr.liglab.adele.iop.device.api.IOPLookupService;
import fr.liglab.adele.iop.device.api.IOPPublisher;

import fr.liglab.adele.iop.device.importer.ServiceDeclaration;


@ContextEntity(coreServices = {IOPController.class, })
@Provides(specifications = { DiscoveryService.class, DiscoveryIntrospection.class, IOPInvocationHandler.class, IOPLookupService.class, IOPPublisher.class })

public class ControllerImpl extends AbstractDiscoveryComponent implements IOPController, IOPInvocationHandler, IOPLookupService, IOPPublisher, IEnqueue, Runnable   {

	private static final Logger LOG = LoggerFactory.getLogger(ControllerImpl.class);

	@Property(name=fr.liglab.adele.cream.model.ContextEntity.CONTEXT_ENTITY_ID)
	private String contextId;

	@ContextEntity.State.Field(service = IOPController.class, state = IOPController.PROPERTIES)
	private Map<String,Object> properties;

	private APlugin 			rosePlugin;
	private Thread 				mThread;
	private Queue<IEvent> 		mQueue;
	private boolean 			mIsRunning;

	private ILocalServiceID 	myServiceId;

	private Map<Integer,IApplicationEvent> pendingInvocations 	= new ConcurrentHashMap<>();
	private Map<Integer,Object> pendingResponses 				= new ConcurrentHashMap<>();

	private IOPServiceDeclarationManager importManager;
	private Map<IServiceDescription,IOPInvocationHandler> exportedServices = new ConcurrentHashMap<>();

	private IMatchRequest lookupRequest	= new SimpleMatchRequest(new String[0]);
	
	/**
	 * Constructor
	 */
	protected ControllerImpl(BundleContext bundleContext) {
		super(bundleContext);
	}

	@Override
	public String getName() {
		return "IOPDeviceDiscovery";
	}

	@Override
	public Object invoke(IServiceID target, ICall call) {
		int eventId = EventID.getInstance().getNextID();
		ApplicationEvent invocation = new ApplicationEvent(new PluginID("InvocationGenerator", rosePlugin.getID().getDeviceID()), eventId, myServiceId, target, call);

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
	
	@Override
	public void publish(String id, String componentName, List<ICapability> capabilities, IOPInvocationHandler handler) {

		LocalServiceID serviceId 	= new LocalServiceID(rosePlugin.getID().getDeviceID(), id);
		IServiceDescription service = new LocalService(serviceId, componentName, capabilities, Collections.emptyList());
		
		exportedServices.put(service, handler);
	}

	@Override
	public void unpublish(String id) {
		LocalServiceID serviceId 	= new LocalServiceID(rosePlugin.getID().getDeviceID(), id);
		IServiceDescription service = new LocalService(serviceId, id, Collections.emptyList(), Collections.emptyList());
		exportedServices.remove(service);
	}


	@Override
	public void consider(String[] considered) {
		String current[] 		= (String[]) lookupRequest.getProperty(IMatchRequest.FUNCTIONALITY);
		
		Set<String> updated 	= current != null ? new HashSet<>(Arrays.asList(current)) : new HashSet<>();
		updated.addAll(Arrays.asList(considered));

		lookupRequest = new SimpleMatchRequest(updated.toArray(new String[updated.size()]));
	}

	@Override
	public void discard(String[] discarded) {
		
		String current[] 		= (String[]) lookupRequest.getProperty(IMatchRequest.FUNCTIONALITY);
		
		Set<String> updated 	= current != null ? new HashSet<>(Arrays.asList(current)) : new HashSet<>();
		updated.removeAll(Arrays.asList(discarded));
		lookupRequest = new SimpleMatchRequest(updated.toArray(new String[updated.size()]));
	}

	@Override
	public void all() {
		lookupRequest = new SimpleMatchRequest();
	}

	@Override
	public void none() {
		lookupRequest = new SimpleMatchRequest(new String[0]);
	}


	/**
	 * LifeCycle
	 */
	@Validate
	protected synchronized void start() {
		super.start();

		LOG.debug("Starting IOP Controller");

		importManager = new IOPServiceDeclarationManager();
		importManager.start();

		mIsRunning = false;
		mQueue = new Queue<IEvent>();
		
		mThread = new Thread(this);
		mThread.setDaemon(true);
		mThread.start();
		mIsRunning = true;
		
		rosePlugin = new APlugin("iCasa over IOP", this, properties);
		rosePlugin.start();

		myServiceId = new LocalServiceID(rosePlugin.getID().getDeviceID(), contextId);
		
		rosePlugin.enqueue(new RegistrationEvent(rosePlugin.getID(), new LocalService(myServiceId, this.getName(), Collections.emptyList(), Collections.emptyList())));
		
	}

	@Invalidate
	protected synchronized void stop() {
		super.stop();

		mIsRunning = false;
		rosePlugin.stop();
		
		importManager.dispose();
		importManager = null;
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
					if (importManager != null) {
						importManager.dispatch((ILookupResponseEvent) event);
					}
					break;
				}
				case IEvent.EVENT_LOOKUP : {
					
					/*
					 * Send response with exported services
					 */
					ILookupEvent lookupEvent = (ILookupEvent) event;
					List<? extends IServiceDescription> matchedServices = new Vector<>(exportedServices.keySet());
					
					IComponentID lookupService = new PluginID("LookupService", rosePlugin.getID().getDeviceID());
					
					ILookupResponseEvent responseEvent = new LookupResponseEvent(lookupService,lookupEvent.getID(), 
							(IEndpointID)lookupEvent.getTargetID(), (IEndpointID)lookupEvent.getSourceID(), 
							matchedServices);

					rosePlugin.enqueue(responseEvent);
					
					/*
					 * An send back my own lookup request
					 */
					ILookupEvent dualLookupEvent = new LookupEvent(lookupService, EventID.getInstance().getNextID(),
							(IEndpointID)lookupEvent.getTargetID(), (IEndpointID)lookupEvent.getSourceID(),
							lookupRequest);
					rosePlugin.enqueue(dualLookupEvent);	
					 
					break;
				}
				case IEvent.EVENT_APPLICATION : {
					IApplicationEvent invocation = (IApplicationEvent) event;
					//incoming
					if (rosePlugin != null && invocation.getSource().equals(rosePlugin.getID())) {
						IServiceDescription service 	= new LocalService((ILocalServiceID)invocation.getTargetID(), null, Collections.emptyList(), Collections.emptyList());
						IOPInvocationHandler handler 	= exportedServices.get(service);

						if (handler != null) {
							Object result = handler.invoke((IServiceID)invocation.getTargetID(), invocation.getCall());
							if (result != null) {
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

	

	private class IOPServiceDeclarationManager extends Thread {
		
		public IOPServiceDeclarationManager() {
			super("IOPServiceDeclarationManager");
			setDaemon(true);
		}
		
		public void dispatch(ILookupResponseEvent event) {
			updateDeclarations(event.getServices());
		}

		private Map<IServiceID,ImportDeclaration> declarations 			= new ConcurrentHashMap<>();

		private BlockingQueue<ImportDeclaration > pendingDeclarations 	= new LinkedBlockingQueue<>();
		
				
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

		private final void updateDeclarations(List<? extends IServiceDescription> services) {

			Set<IServiceID> unrenewedServices 			= new HashSet<>();
			Set<IServiceDescription> discoveredServices	= new HashSet<>();
			
			synchronized (this) {
				unrenewedServices.addAll(declarations.keySet());
				
				for (IServiceDescription service : services) {
					if (declarations.containsKey(service.getId())) {
						unrenewedServices.remove(service.getId());
					}
					else {
						discoveredServices.add(service);
					}
				}
				
			}
			
			for (IServiceDescription discovered : discoveredServices) {
				createDeclaration(discovered);
			}
			
			for (IServiceID unrenewed : unrenewedServices) {
				removeDeclaration(unrenewed);
			}
		}
		
		private final void createDeclaration(IServiceDescription service) {

			try {
			
				ImportDeclaration declaration = ServiceDeclaration.from(service);
				declarations.put(service.getId(),declaration);
				pendingDeclarations.put(declaration);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		private final void removeDeclaration(IServiceID serviceId) {
			ImportDeclaration declaration = declarations.remove(serviceId);
			if (declaration != null) {
				unregisterImportDeclaration(declaration);
			}
		}
		
		public synchronized void dispose() {
			declarations.clear();
			declarations = null;
			pendingDeclarations.clear();
		}

	}


}
