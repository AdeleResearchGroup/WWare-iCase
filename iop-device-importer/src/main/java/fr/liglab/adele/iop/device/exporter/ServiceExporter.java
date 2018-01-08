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
package fr.liglab.adele.iop.device.exporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.osgi.framework.BundleContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;

import org.ow2.chameleon.fuchsia.core.component.AbstractExporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ExporterIntrospection;
import org.ow2.chameleon.fuchsia.core.component.ExporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;

import de.mannheim.wifo2.iop.identifier.IServiceID;
import de.mannheim.wifo2.iop.service.access.ICall;
import de.mannheim.wifo2.iop.service.access.IParameter;
import fr.liglab.adele.iop.device.api.IOPInvocationHandler;
import fr.liglab.adele.iop.device.api.IOPPublisher;


@Component
@Provides(specifications = {ExporterService.class,ExporterIntrospection.class})
public class ServiceExporter extends AbstractExporterComponent implements IOPInvocationHandler  {

	
	private static final Logger LOG = LoggerFactory.getLogger(ServiceExporter.class);

	@ServiceProperty(name = Factory.INSTANCE_NAME_PROPERTY)
	private String name;

	@ServiceProperty(name = "target", value = "(&(scope=generic)(iop.exported.service.id=*)(iop.exported.service.capabilities=*))")
	private String filter;

	@Requires(optional=false,proxy=false)
	IOPPublisher publisher;
	
	private final Map<String,Object> exportedServices = new HashMap<>();
	/**
	 * The set of declarations pending the availability  
	 */

	public ServiceExporter(BundleContext context) {
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
		
		LOG.debug("starting IOP Device Exporter");
	}

	@Override
	protected void useExportDeclaration(ExportDeclaration exportDeclaration) throws BinderException {
		
		ServiceDeclaration declaration = new ServiceDeclaration(exportDeclaration);
		exportedServices.put(declaration.getId(),declaration.getService());
		publisher.publish(declaration.getId(), declaration.getComponentId(), declaration.getFunctionalities(), this);
	}

	@Override
	protected void denyExportDeclaration(ExportDeclaration exportDeclaration) throws BinderException {
		ServiceDeclaration declaration = new ServiceDeclaration(exportDeclaration);
		exportedServices.remove(declaration.getId());
		publisher.unpublish(declaration.getId());
	}


	@Override
	public Object invoke(IServiceID target, ICall call, long timeout) {

		Object result 	= null;
		try {
			Object service				= exportedServices.get(target.getObjectID());

			List<Object> arguments 		= new ArrayList<>();
			List<Class<?>> parameters 	= new ArrayList<>();
			
			for (IParameter parameter : call.getParameters()) {
				arguments.add(parameter.getValue());
				parameters.add(parameter.getClazz());
			}
			
			Method method	= service.getClass().getMethod(call.getSignature(),parameters.toArray(new Class<?>[0]));
			
			result = method.invoke(service, arguments.toArray());
			
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return result;
	}



}
