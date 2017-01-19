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
package fr.liglab.adele.iop.device.importer;

import java.util.HashMap;
import java.util.Map;

import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ImporterIntrospection;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.iop.device.api.IOPDevice;


@Component
@Provides(specifications = {ImporterService.class,ImporterIntrospection.class})
public class DeviceImporter extends AbstractImporterComponent  {

	
	private static final Logger LOG = LoggerFactory.getLogger(DeviceImporter.class);

	

	@Creator.Field Creator.Entity<fr.liglab.adele.iop.device.proxies.IOPLight> 			iopLightCreator;


	@ServiceProperty(name = Factory.INSTANCE_NAME_PROPERTY)
	private String name;

	@ServiceProperty(name = "target", value = "(&(scope=generic)(iop.interface.id=*)(iop.service.id=*))")
	private String filter;

	public DeviceImporter(BundleContext context) {
	}

	@Validate
	protected void start() {
		super.start();
	}

	@Invalidate
	protected void stop() {
		super.stop();
		
		LOG.debug("starting IOP Device Importer");
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	protected void useImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

		IOPServiceDeclaration deviceDeclaration = new IOPServiceDeclaration(importDeclaration);

		if (deviceDeclaration.getInterfaceId().contains("SimpleLight")) {
			
			String instanceId = getInstanceId("SimpleLight", deviceDeclaration);
			
			Map<String,Object> properties = new HashMap<>();
			properties.put(ContextEntity.State.id(GenericDevice.class,GenericDevice.DEVICE_SERIAL_NUMBER),instanceId);
			properties.put(ContextEntity.State.id(IOPDevice.class,IOPDevice.SERVICE_ID),deviceDeclaration.getServiceId());

			iopLightCreator.create(instanceId,properties);
		}
	}

	@Override
	protected void denyImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

		IOPServiceDeclaration deviceDeclaration = new IOPServiceDeclaration(importDeclaration);
		if (deviceDeclaration.getInterfaceId().equals("SimpleLight")) {
			String instanceId = getInstanceId("SimpleLight", deviceDeclaration);
			iopLightCreator.delete(instanceId);
		}

	}

	private static String getInstanceId(String interfaceId,IOPServiceDeclaration deviceDeclaration) {
		return interfaceId+"-"+deviceDeclaration.getServiceId().getObjectID();
	}

}
