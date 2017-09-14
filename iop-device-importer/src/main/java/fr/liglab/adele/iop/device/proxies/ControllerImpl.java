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
import java.util.concurrent.TimeoutException;

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

import de.mannheim.wifo2.iop.event.IEvent;
import de.mannheim.wifo2.iop.event.EventID;
import de.mannheim.wifo2.iop.event.i.IApplicationEvent;
import de.mannheim.wifo2.iop.event.i.IApplicationResponseEvent;
import de.mannheim.wifo2.iop.event.i.IEventingEvent;
import de.mannheim.wifo2.iop.event.i.ILookupEvent;
import de.mannheim.wifo2.iop.event.i.ILookupResponseEvent;
import de.mannheim.wifo2.iop.event.impl.ApplicationEvent;
import de.mannheim.wifo2.iop.event.impl.ApplicationResponseEvent;
import de.mannheim.wifo2.iop.event.impl.EventingEvent;
import de.mannheim.wifo2.iop.event.impl.LookupEvent;
import de.mannheim.wifo2.iop.event.impl.LookupResponseEvent;
import de.mannheim.wifo2.iop.eventing.EEventingType;
import de.mannheim.wifo2.iop.plugin.function.matching.IMatchRequest;
import de.mannheim.wifo2.iop.plugin.function.matching.impl.SimpleMatchRequest;

import de.mannheim.wifo2.iop.identifier.IComponentID;
import de.mannheim.wifo2.iop.identifier.IEndpointID;
import de.mannheim.wifo2.iop.identifier.ILocalServiceID;
import de.mannheim.wifo2.iop.identifier.IServiceID;
import de.mannheim.wifo2.iop.identifier.impl.LocalServiceID;
import de.mannheim.wifo2.iop.identifier.impl.PluginID;
import de.mannheim.wifo2.iop.plugin.APlugin;

import de.mannheim.wifo2.iop.service.LocalService;
import de.mannheim.wifo2.iop.service.functionality.ICall;
import de.mannheim.wifo2.iop.service.functionality.IParameter;
import de.mannheim.wifo2.iop.service.functionality.impl.Call;
import de.mannheim.wifo2.iop.service.functionality.impl.Parameter;
import de.mannheim.wifo2.iop.service.model.IFunctionality;
import de.mannheim.wifo2.iop.service.model.IServiceDescription;

import de.mannheim.wifo2.iop.util.i.IEnqueue;
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

	/**
	 * A value used to represent a result of null. This is required as the {@link #pendingResponses} table
	 * do not accept null values or keys
	 */
	private static final Object NULL_RESULT = new Object();
	
	/**
	 * Send an invocation request to the specified service
	 */
	@Override
	public Object invoke(IServiceID target, ICall call, long timeout) throws TimeoutException {
		
		int eventId = EventID.getInstance().getNextID();
		
		ApplicationEvent invocation = new ApplicationEvent(new PluginID("InvocationGenerator", rosePlugin.getID().getDeviceID()), 
											eventId, myServiceId, target, call);

		/*
		 * If no response expected, just return to the caller
		 */
		if (!call.expectsResult()) {
			mQueue.enqueue(invocation);
			return null;
		}
		
		/*
		 * Send the request and wait for response (that will arrive concurrently in another thread)
		 * 
		 */
		pendingInvocations.put(eventId,invocation);
		mQueue.enqueue(invocation);

		if (pendingInvocations.containsKey(eventId)) {
			synchronized(invocation) {
				try {
					invocation.wait(timeout);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		/*
		 * Get the response if available
		 */
		boolean timeoutExpired = ! pendingResponses.containsKey(eventId);
		
		if (timeoutExpired) {
			pendingInvocations.remove(eventId);
			throw new TimeoutException();
		}
			
		Object result = pendingResponses.remove(eventId);
		if (result == NULL_RESULT) {
			result = null;
		}
			
		return result;
			 
	}
	
	/**
	 * Handle responses to ongoing invocations
	 */
	private void invocationResponse(IApplicationResponseEvent response) {

		Object result = null;
		for (IParameter parameter : response.getResponse().getParameters()) {
			if (parameter.getKey().equalsIgnoreCase("result")) {
				result = parameter.getValue();
			}
		}
		
		boolean invocationIsWaiting = pendingInvocations.containsKey(response.getID());
		
		if (invocationIsWaiting) {
			
			IApplicationEvent invocation = pendingInvocations.remove(response.getID());
			if (invocation != null) {
				pendingResponses.put(response.getID(), result != null ? result : NULL_RESULT);
				synchronized(invocation) {
					invocation.notifyAll();
				}
				
			}
			
		}

	}
	
	@Override
	public void publish(String id, String componentName, List<IFunctionality> functionalities, IOPInvocationHandler handler) {

		LocalServiceID serviceId 	= new LocalServiceID(rosePlugin.getID().getDeviceID(), id);
		IServiceDescription service = new LocalService(serviceId, componentName, functionalities, Collections.emptyList());
		
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
		
		IEventingEvent registrationEvent = 	new EventingEvent(rosePlugin.getID(), EEventingType.SERVICE_REGISTRATION);
		registrationEvent.addProperty(IEventingEvent.SERVICE, new LocalService(myServiceId, this.getName(), Collections.emptyList(), Collections.emptyList()));
		
		rosePlugin.enqueue(registrationEvent);
		
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

						Object result = null;
						
						if (handler != null) {
							try {

								result = handler.invoke((IServiceID)invocation.getTargetID(),
												invocation.getCall(), 
												IOPInvocationHandler.TIMEOUT);
								
							} catch (TimeoutException e) {
								result = null;
							}
						}
							
						if (result != null) {
							PluginID componentId = new PluginID("ApplicationResponseEventGenerator", rosePlugin.getID().getDeviceID());
							ICall call = new Call(null, Collections.singletonList(new Parameter("result", result)), null);
							IApplicationResponseEvent responseEvent = new ApplicationResponseEvent(
																			componentId,
																			invocation.getID(),
																			(IServiceID)invocation.getTargetID(),
																			(IServiceID)invocation.getSourceID(), 
																			call);
							rosePlugin.enqueue(responseEvent);								

						}
						
					}
					//outgoing
					else {
						rosePlugin.getConnectionManager().send(invocation);
					}
					break;
				}
				case IEvent.EVENT_APPLICATIONRESPONSE : {
					invocationResponse((IApplicationResponseEvent)event);
					break;
				}
				case IEvent.EVENT_EVENTING : {
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
