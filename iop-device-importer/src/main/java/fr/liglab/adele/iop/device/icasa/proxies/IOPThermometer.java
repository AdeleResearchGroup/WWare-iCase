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
package fr.liglab.adele.iop.device.icasa.proxies;


import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;

import tec.units.ri.quantity.Quantities;
import tec.units.ri.unit.Units;

import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;

import de.mannheim.wifo2.iop.identifier.IServiceID;
import de.mannheim.wifo2.iop.service.access.ICall;
import de.mannheim.wifo2.iop.service.access.impl.Call;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtender;

import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.iop.device.api.IOPInvocationHandler;
import fr.liglab.adele.iop.device.api.IOPService;


/**
 * An proxy to an IOP XWare-based service implementing a thermometer
 *
 */
@FunctionalExtender(contextServices = {GenericDevice.class,Thermometer.class,IOPService.class})
public class IOPThermometer implements GenericDevice, Thermometer, IOPService {

	/**
	 * State
	 */
	@ContextEntity.State.Field(service = GenericDevice.class,state = GenericDevice.DEVICE_SERIAL_NUMBER)
	private String serialNumber;

	@ContextEntity.State.Field(service=IOPService.class, state = IOPService.SERVICE_ID)
	private IServiceID remoteServiceId;

	@ContextEntity.State.Field(service = Thermometer.class,state = Thermometer.THERMOMETER_CURRENT_TEMPERATURE,directAccess=true)
	private Quantity<Temperature> status;


	@Requires(optional=false, proxy=false)
	private IOPInvocationHandler iopInvocationHandler;


	@Override
	public String getSerialNumber() {
		return serialNumber;
	}

	private volatile QueryProcessor processor;
	
	@Validate
	private void start() {
		processor = new QueryProcessor(serialNumber, iopInvocationHandler);
		processor.start();
	}
	
	@Invalidate
	private void stop() {
		QueryProcessor disposed = this.processor;
		this.processor = null;
		
		disposed.dispose();
	}
	
	@Override
	public Quantity<Temperature> getTemperature() {
		
		if (processor != null) {
			processor.schedule(remoteServiceId,	
						new Call("getTemperature", Collections.emptyList(), Double.class), IOPInvocationHandler.TIMEOUT,
						this::updateTemperature,
						QueryProcessor.QoS.ALLOW_STALE);
		}
		
		return status;
	}

	private void updateTemperature(IServiceID target, ICall call, Double result) {
		if (result != null) {
			status = Quantities.getQuantity(result, Units.KELVIN);
		}
	}

	/**
	 * A thread in charge of performing asynchronous invocations to serialize access to the device and avoid performing the same
	 * query repeatedly
	 *
	 * TODO This class SHOULD be generalized and the behavior incorporated into the IOPInvocationHandler, query processors MUST 
	 * be polled and shared between different IOPServices (probably using an executor)
	 */
	private static class QueryProcessor extends Thread {

        /**
         * Callback invoked when the query succeeds.
         *
         */

		@FunctionalInterface
	    public static interface Success<R> {

			public void onSuccess(IServiceID target, ICall call, R result);
	    
		}

	    public enum QoS {
			FRESH, ALLOW_STALE;
		}

		private final IOPInvocationHandler controller;
		
		public QueryProcessor(String serialNumber, IOPInvocationHandler controller) {
			super("IOPThermomter-"+serialNumber);
			setDaemon(true);
			
			this.controller = controller;
		}

		public <R> void schedule(IServiceID target, ICall call, long timeout, Success<R> callback, QoS qos) {

			switch (qos) {

			case FRESH:

				/*
				 * For best quality of service perform the query synchronously
				 */

				try {
					
					@SuppressWarnings("unchecked") R result = (R) controller.invoke(target, call,timeout);
					callback.onSuccess(target, call, result);
				} catch (TimeoutException e) {
					callback.onSuccess(target, call, null);
				}

				break;

			case ALLOW_STALE:

				/*
				 * If we allow stale values, we perform the request asynchronously and can ignore repeated requests
				 */
				Query<?> query	= new Query<>(target, call, timeout, callback);
				Query<?> head	= queries.peek();
				
				if (head == null || ! head.equals(query)) {
					queries.offer(query);
				}

				break;
				
			default:
				break;
			}
		}

		private static class Query<R> {
			
			public final IServiceID target;
			public final ICall call;
			public final long timeout;
			public final Success<R> callback;
			
			public Query(IServiceID target, ICall call, long timeout, Success<R> callback) {
				
				this.target 	= target;
				this.call		= call;
				this.timeout	= timeout;
				this.callback	= callback;
			}
			
			public boolean equals(Object object) {

				if (this == object) {
					return true;
				}

				if (object == null) {
					return false;
				}

				if ( !(object instanceof Query)) {
					return false;
				}
				
				Query<?> that = (Query<?>) object;
				return this.target.equals(that.target) && this.call.getSignature().equals(that.call.getSignature());
			}
		}
		
		private volatile BlockingQueue<Query<?>> queries = new LinkedBlockingQueue<>();

		@Override
		public void run() {
			
			while (!isDisposed()) {
				try {
					invoke(queries.take());
				} catch (InterruptedException unexpected) {
					unexpected.printStackTrace();
				}
			}
		}

		@SuppressWarnings("unchecked")
		private <R> void invoke(Query<R> query) {
			try {
				R result = (R) controller.invoke(query.target, query.call, query.timeout);
				query.callback.onSuccess(query.target, query.call, result);
			} 
			catch (Throwable e) {
				query.callback.onSuccess(query.target, query.call, null);
			}
		}
		
 		public final void dispose() {
			BlockingQueue<Query<?>>  disposed = queries;
			queries = null;
			
			disposed.clear();
		}

		public final boolean isDisposed() {
			return queries == null;
		}


	}

}
