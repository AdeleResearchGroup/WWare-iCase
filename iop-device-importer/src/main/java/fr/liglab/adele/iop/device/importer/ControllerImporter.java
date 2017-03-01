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
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ImporterIntrospection;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.iop.device.api.IOPController;



@Component
@Provides(specifications = {ImporterService.class,ImporterIntrospection.class})
public class ControllerImporter extends AbstractImporterComponent  {

	private static final Logger LOG = LoggerFactory.getLogger(ControllerImporter.class);

	@ServiceProperty(name = Factory.INSTANCE_NAME_PROPERTY)
	private String name;

	@ServiceProperty(name = "target", value = "(&(scope=generic)(protocol=iop)(SELF_ID=*)(SELF_LOCATION=*))")
	private String filter;


	@Creator.Field  Creator.Entity<fr.liglab.adele.iop.device.proxies.ControllerImpl> iopControllerCreator;

	public ControllerImporter() {
	}
	
	@Validate
	protected void start() {
		super.start();
	}

	@Invalidate
	protected void stop() {
		super.stop();
	}

	@Override
	protected void useImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

		ControllerDeclaration declaration = ControllerDeclaration.from(importDeclaration);

		LOG.info("Importing declaration for IOP device '{}' ",declaration.getId());

		Map<String,Object> properties= new HashMap<>();
		
		properties.put(ContextEntity.State.id(IOPController.class,IOPController.PROPERTIES),declaration.getProperties());
		
		iopControllerCreator.create(getInstanceId(declaration),properties);
		handleImportDeclaration(importDeclaration);

	}

	@Override
	protected void denyImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

		ControllerDeclaration declaration = ControllerDeclaration.from(importDeclaration);

		LOG.info("Removing declaration for IOP device '{}'",declaration.getId());

		iopControllerCreator.delete(getInstanceId(declaration));
		unhandleImportDeclaration(importDeclaration);
	}

	private static String getInstanceId(ControllerDeclaration declaration) {
		return "Controller"+"-"+declaration.getId();
	}


	public String getName() {
		return name;
	}

}
