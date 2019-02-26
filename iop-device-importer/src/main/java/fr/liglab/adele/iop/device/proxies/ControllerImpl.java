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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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

import de.mannheim.wifo2.iop.event.EDiscoveryType;
import de.mannheim.wifo2.iop.event.IDynamicRouter;
import de.mannheim.wifo2.iop.event.IEvent;
import de.mannheim.wifo2.iop.event.i.IAnnouncementEvent;
import de.mannheim.wifo2.iop.event.i.IApplicationEvent;
import de.mannheim.wifo2.iop.event.i.IApplicationResponseEvent;
import de.mannheim.wifo2.iop.event.i.IDiscoveryEvent;
import de.mannheim.wifo2.iop.event.i.ILookupEvent;
import de.mannheim.wifo2.iop.event.i.ILookupResponseEvent;
import de.mannheim.wifo2.iop.event.identifier.EventID;
import de.mannheim.wifo2.iop.event.impl.ApplicationEvent;
import de.mannheim.wifo2.iop.event.impl.ApplicationResponseEvent;
import de.mannheim.wifo2.iop.event.impl.DiscoveryEvent;
import de.mannheim.wifo2.iop.event.impl.LookupEvent;
import de.mannheim.wifo2.iop.event.impl.LookupResponseEvent;
import de.mannheim.wifo2.iop.event.semantics.i.IInteraction;
import de.mannheim.wifo2.iop.event.semantics.impl.Interaction;
import de.mannheim.wifo2.iop.plugin.function.matching.IMatchRequest;
import de.mannheim.wifo2.iop.identifier.IComponentID;
import de.mannheim.wifo2.iop.identifier.IEndpointID;
import de.mannheim.wifo2.iop.identifier.ILocalServiceID;
import de.mannheim.wifo2.iop.identifier.IMediatorID;
import de.mannheim.wifo2.iop.identifier.IPluginID;
import de.mannheim.wifo2.iop.identifier.IServiceID;
import de.mannheim.wifo2.iop.identifier.impl.LocalServiceID;
import de.mannheim.wifo2.iop.identifier.impl.PluginID;
import de.mannheim.wifo2.iop.mediator.IMediator;
import de.mannheim.wifo2.iop.mediator.filter.i.IFilter;
import de.mannheim.wifo2.iop.plugin.IPlugin;
import de.mannheim.wifo2.iop.service.IGlobalService;
import de.mannheim.wifo2.iop.service.LocalService;
import de.mannheim.wifo2.iop.service.access.ICall;
import de.mannheim.wifo2.iop.service.access.IParameter;
import de.mannheim.wifo2.iop.service.access.impl.Call;
import de.mannheim.wifo2.iop.service.access.impl.Parameter;
import de.mannheim.wifo2.iop.service.matching.SimpleMatchRequest;
import de.mannheim.wifo2.iop.service.model.IFunctionality;
import de.mannheim.wifo2.iop.service.model.IProperty;
import de.mannheim.wifo2.iop.service.model.IServiceDescription;
import de.mannheim.wifo2.iop.service.model.impl.Operator;
import de.mannheim.wifo2.iop.util.i.IEnqueue;
import de.mannheim.wifo2.xware.plugin.RosePlugin;
import de.mannheim.wifo2.iop.util.PluginConstants;
import de.mannheim.wifo2.iop.util.datastructure.Queue;

import fr.liglab.adele.iop.device.api.IOPController;
import fr.liglab.adele.iop.device.api.IOPInvocationHandler;
import fr.liglab.adele.iop.device.api.IOPLookupService;
import fr.liglab.adele.iop.device.api.IOPPublisher;

import fr.liglab.adele.iop.device.importer.ServiceDeclaration;

@ContextEntity(coreServices = { IOPController.class, })
@Provides(specifications = { DiscoveryService.class, DiscoveryIntrospection.class, IOPInvocationHandler.class,
		IOPLookupService.class, IOPPublisher.class })

public class ControllerImpl extends AbstractDiscoveryComponent
		implements IOPController, IOPInvocationHandler, IOPLookupService, IOPPublisher, IMediator, Runnable {

	private static final boolean EVALUATION = true;

	private static final Logger LOG = LoggerFactory.getLogger(ControllerImpl.class);

	@Property(name = fr.liglab.adele.cream.model.ContextEntity.CONTEXT_ENTITY_ID)
	private String contextId;

	@ContextEntity.State.Field(service = IOPController.class, state = IOPController.PROPERTIES)
	private Map<String, Object> properties;

	private RosePlugin rosePlugin;
	private Thread mThread;
	private Queue<IEvent> mQueue;
	private boolean mIsRunning;

	private ILocalServiceID myServiceId;

	private Map<Integer, IApplicationEvent> pendingInvocations = new ConcurrentHashMap<>();
	private Map<Integer, Object> pendingResponses = new ConcurrentHashMap<>();

	private IOPServiceDeclarationManager importManager;
	private Map<IServiceDescription, IOPInvocationHandler> exportedServices = new ConcurrentHashMap<>();

	private List<IMatchRequest> lookupRequests = new ArrayList<>();

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
	 * A value used to represent a result of null. This is required as the
	 * {@link #pendingResponses} table do not accept null values or keys
	 */
	private static final Object NULL_RESULT = new Object();

	/**
	 * Send an invocation request to the specified service
	 */
	@Override
	public Object invoke(IServiceID target, ICall call, long timeout) throws TimeoutException {

		int eventId = EventID.getInstance().getNextID();

		IInteraction interaction = (call.expectsResult() ? new Interaction(IInteraction.SEMANTICS_CS_REQUEST_RESPONSE)
				: new Interaction(IInteraction.SEMANTICS_CS_ONE_WAY));
		ApplicationEvent invocation = new ApplicationEvent(
				new PluginID("InvocationGenerator", rosePlugin.getID().getDeviceID(), IInteraction.INTERACTION_CS),
				eventId, myServiceId, target, call, interaction);

		/*
		 * If no response expected, just return to the caller
		 */
		if (!call.expectsResult()) {
			mQueue.enqueue(invocation);
			return null;
		}

		/*
		 * Send the request and wait for response (that will arrive concurrently in
		 * another thread)
		 * 
		 */
		pendingInvocations.put(eventId, invocation);
		mQueue.enqueue(invocation);

		if (pendingInvocations.containsKey(eventId)) {
			synchronized (invocation) {
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
		boolean timeoutExpired = !pendingResponses.containsKey(eventId);

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
		for (IParameter parameter : response.getCall().getParameters()) {
			if (parameter.getKey().equalsIgnoreCase("result")) {
				result = parameter.getValue();
			}
		}

		boolean invocationIsWaiting = pendingInvocations.containsKey(response.getID());

		if (invocationIsWaiting) {

			IApplicationEvent invocation = pendingInvocations.remove(response.getID());
			if (invocation != null) {
				pendingResponses.put(response.getID(), result != null ? result : NULL_RESULT);
				synchronized (invocation) {
					invocation.notifyAll();
				}

			}

		}

	}

	@Override
	public void publish(String id, String componentName, List<IFunctionality> functionalities,
			Map<String, ?> properties, IOPInvocationHandler handler) {

		LocalServiceID serviceId = new LocalServiceID(rosePlugin.getID().getDeviceID(), id);

		List<IProperty> exportedProperties = new ArrayList<>();
		for (String property : properties.keySet()) {
			exportedProperties.add(new de.mannheim.wifo2.iop.service.model.impl.Property(IProperty.TYPE_CONTEXT,
					property, properties.get(property).toString(), Operator.EQUAL));
		}

		IServiceDescription service = new LocalService(serviceId, componentName, functionalities, exportedProperties);

		exportedServices.put(service, handler);
	}

	@Override
	public void unpublish(String id) {
		LocalServiceID serviceId = new LocalServiceID(rosePlugin.getID().getDeviceID(), id);
		IServiceDescription service = new LocalService(serviceId, id, Collections.emptyList(), Collections.emptyList());
		exportedServices.remove(service);
	}

	@Override
	public void consider(String[] considered, Map<String, String> query) {

		List<IProperty> properties = null;
		if (query.size() > 0) {
			properties = new ArrayList<IProperty>();
			for (String property : query.keySet()) {
				properties.add(new de.mannheim.wifo2.iop.service.model.impl.Property(IProperty.TYPE_CONTEXT, property,
						query.get(property), Operator.EQUAL));
			}
		}

		lookupRequests.add(new SimpleMatchRequest(considered, properties));
	}

	@Override
	public void discard(String[] discarded) {

		IMatchRequest found = null;
		for (IMatchRequest lookupRequest : lookupRequests) {
			String requested[] = (String[]) lookupRequest.getProperty(IMatchRequest.FUNCTIONALITY);
			if (Arrays.equals(requested, discarded)) {
				found = lookupRequest;
			}
		}

		if (found != null) {
			lookupRequests.remove(found);
		}
	}

	@Override
	public void all() {
		lookupRequests.clear();
		lookupRequests.add(new SimpleMatchRequest());
	}

	@Override
	public void none() {
		lookupRequests.clear();
	}

	@Override
	public List<String> considered() {

		List<String> requests = new ArrayList<>();
		for (IMatchRequest lookupRequest : lookupRequests) {
			requests.add(lookupRequest.toString());
		}

		return requests;
	}

	/**
	 * LifeCycle
	 */
	@Validate
	public synchronized void start() {
		super.start();

		LOG.debug("Starting IOP Controller");

		importManager = new IOPServiceDeclarationManager((int) properties.get(PluginConstants.LEASE_TIMEOUT));
		importManager.start();

		mIsRunning = false;
		mQueue = new Queue<IEvent>();

		mThread = new Thread(this, "IOPControlller-Runner");
		mThread.setDaemon(true);
		mThread.start();
		mIsRunning = true;

		rosePlugin = new RosePlugin("iCasa over IOP", this, properties);
		rosePlugin.start();

		myServiceId = new LocalServiceID(rosePlugin.getID().getDeviceID(), contextId);

		// (DiscoveryEvent, EDiscoveryType)
		IDiscoveryEvent registrationEvent = new DiscoveryEvent(rosePlugin.getID(), EDiscoveryType.SERVICE_REGISTRATION);
		registrationEvent.addProperty(IDiscoveryEvent.SERVICE,
				new LocalService(myServiceId, this.getName(), Collections.emptyList(), Collections.emptyList()));

		rosePlugin.enqueue(registrationEvent);

	}

	@Invalidate
	public synchronized void stop() {
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
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(System.getProperty("user.home") + File.separator + "iCasa-log"
					+ System.currentTimeMillis() + ".txt", "UTF-8");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		while (mIsRunning) {
			if (!mQueue.isEmpty()) {
				IEvent event = mQueue.dequeue();

				switch (event.getType()) {

				case IEvent.EVENT_LOOKUPRESPONSE: {
					if (importManager != null) {
						importManager.dispatch((ILookupResponseEvent) event);
					}
					break;
				}

				case IEvent.EVENT_ANNOUNCEMENT: {
					IAnnouncementEvent announcementEvent = (IAnnouncementEvent) event;

					/*
					 * Send back my lookup requests
					 */
					IComponentID lookupService = new PluginID("LookupService", rosePlugin.getID().getDeviceID(),
							IInteraction.INTERACTION_CS);

					if (lookupRequests.isEmpty()) {
						ILookupEvent lookupEvent = new LookupEvent(lookupService, EventID.getInstance().getNextID(),
								(IEndpointID) rosePlugin.getID().getDeviceID(),
								(IEndpointID) announcementEvent.getSourceID(), new SimpleMatchRequest(new String[0]));

						lookupEvent.setReadyToSend(true);
						rosePlugin.enqueue(lookupEvent);

					} else {
						for (IMatchRequest lookupRequest : lookupRequests) {

							ILookupEvent lookupEvent = new LookupEvent(lookupService, EventID.getInstance().getNextID(),
									(IEndpointID) rosePlugin.getID().getDeviceID(),
									(IEndpointID) announcementEvent.getSourceID(), lookupRequest);

							lookupEvent.setReadyToSend(true);
							rosePlugin.enqueue(lookupEvent);
						}
					}

					break;
				}

				case IEvent.EVENT_LOOKUP: {
					ILookupEvent lookupEvent = (ILookupEvent) event;

					// incoming
					if (rosePlugin != null && !lookupEvent.getTargetID().equals(rosePlugin.getID())) {
						/*
						 * Send response with exported services
						 */
						List<? extends IServiceDescription> matchedServices = new Vector<>(exportedServices.keySet());

						IComponentID lookupService = new PluginID("LookupService", rosePlugin.getID().getDeviceID(),
								IInteraction.INTERACTION_CS);

						ILookupResponseEvent responseEvent = new LookupResponseEvent(lookupService, lookupEvent.getID(),
								(IEndpointID) lookupEvent.getTargetID(), (IEndpointID) lookupEvent.getSourceID(),
								matchedServices);

						responseEvent.setReadyToSend(true);
						rosePlugin.enqueue(responseEvent);
					}

					break;
				}

				case IEvent.EVENT_APPLICATION: {
					IApplicationEvent invocation = (IApplicationEvent) event;
					// incoming
					if (rosePlugin != null && invocation.getSource().equals(rosePlugin.getID())) {

						IServiceDescription service = new LocalService((ILocalServiceID) invocation.getTargetID(), null,
								Collections.emptyList(), Collections.emptyList());
						IOPInvocationHandler handler = exportedServices.get(service);

						Object result = null;

						if (handler != null) {
							try {

								result = handler.invoke((IServiceID) invocation.getTargetID(), invocation.getCall(),
										IOPInvocationHandler.TIMEOUT);

							} catch (TimeoutException e) {
								result = null;
							}
						}

						if (result != null) {
							PluginID componentId = new PluginID("ApplicationResponseEventGenerator",
									rosePlugin.getID().getDeviceID(), IInteraction.INTERACTION_CS);
							ICall call = new Call(null,
									Collections.singletonList(new Parameter("result", result, null)), null);
							IApplicationResponseEvent responseEvent = new ApplicationResponseEvent(componentId,
									invocation.getID(), (IServiceID) invocation.getTargetID(),
									(IServiceID) invocation.getSourceID(), call,
									new Interaction(IInteraction.SEMANTICS_CS_REQUEST_RESPONSE));
							rosePlugin.enqueue(responseEvent);

						}

					}
					// outgoing
					else {
						invocation.setReadyToSend(true);

						if (EVALUATION) {
							writer.print(System.nanoTime());
						}
						rosePlugin.getConnectionManager().send(invocation);
					}
					break;
				}

				case IEvent.EVENT_APPLICATIONRESPONSE: {
					IApplicationResponseEvent response = (IApplicationResponseEvent) event;
					// incoming
					if (rosePlugin != null && response.getSource().equals(rosePlugin.getID())) {
						if (EVALUATION) {
							writer.println("\t" + System.nanoTime());
							writer.flush();
						}
						invocationResponse(response);
					} else {
						event.setReadyToSend(true);
						rosePlugin.getConnectionManager().send(event);
					}
					break;
				}

				case IEvent.EVENT_EVENTING: {
					break;
				}

				}
			} else {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private class IOPServiceDeclarationManager extends Thread {

		private final int leaseTimeOut;

		public IOPServiceDeclarationManager(int leaseTimeOut) {
			super("IOPServiceDeclarationManager");

			this.leaseTimeOut = leaseTimeOut;
			setDaemon(true);
		}

		public void dispatch(ILookupResponseEvent event) {
			updateDeclarations(event.getServices());
		}

		private Map<IServiceID, ImportDeclaration> declarations = new ConcurrentHashMap<>();
		private Map<IServiceID, Long> lastSeen = new ConcurrentHashMap<>();

		private BlockingQueue<ImportDeclaration> pendingDeclarations = new LinkedBlockingQueue<>();

		@Override
		public void run() {
			while (!isDisposed()) {

				try {
					ImportDeclaration declaration = pendingDeclarations.take();
					registerImportDeclaration(declaration);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}

		private final void updateDeclarations(List<? extends IServiceDescription> services) {

			Set<IServiceDescription> discoveredServices = new HashSet<>();

			synchronized (this) {

				for (IServiceDescription service : services) {
					if (declarations.containsKey(service.getID())) {
						lastSeen.put(service.getID(), System.currentTimeMillis());
					} else {
						discoveredServices.add(service);
					}
				}

			}

			for (IServiceDescription discovered : discoveredServices) {
				createDeclaration(discovered);
			}

			garbageCollect();

		}

		private final void garbageCollect() {
			Set<IServiceID> collected = new HashSet<>();

			long now = System.currentTimeMillis();

			synchronized (this) {

				for (Map.Entry<IServiceID, Long> service : lastSeen.entrySet()) {

					if (now - service.getValue() > leaseTimeOut) {
						collected.add(service.getKey());
					}
				}

			}

			for (IServiceID garbage : collected) {
				removeDeclaration(garbage);
			}
		}

		private final void createDeclaration(IServiceDescription service) {

			try {

				ImportDeclaration declaration = ServiceDeclaration.from(service);
				declarations.put(service.getID(), declaration);
				lastSeen.put(service.getID(), System.currentTimeMillis());
				pendingDeclarations.put(declaration);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		private final void removeDeclaration(IServiceID serviceId) {
			ImportDeclaration declaration = declarations.remove(serviceId);
			if (declaration != null) {
				lastSeen.remove(serviceId);
				unregisterImportDeclaration(declaration);
			}
		}

		public synchronized void dispose() {
			declarations.clear();
			lastSeen.clear();
			declarations = null;
			pendingDeclarations.clear();
		}

		public synchronized boolean isDisposed() {
			return declarations == null;
		}

	}

	// most inherited methods from IMediator, can be empty
	@Override
	public void addPlugin(IPlugin arg0, IEnqueue arg1) {
	}

	@Override
	public void addProcessor(IFilter<IEvent> arg0) {
	}

	@Override
	public IDynamicRouter<IEvent> getDispatcher() {
		return null;
	}

	@Override
	public IMediatorID getMediatorID() {
		return null;
	}

	@Override
	public List<? extends IServiceDescription> getServicesForAdvertisement(IPluginID self) {
		List<IServiceDescription> list = new ArrayList<>();
		for (IServiceDescription s : exportedServices.keySet()) {
			list.add(s);
		}
		return list;
	}

	@Override
	public void addGlobalService(IGlobalService contextManager) {		
	}

	@Override
	public void initializeFilters() {		
	}

}
