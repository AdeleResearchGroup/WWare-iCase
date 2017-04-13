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


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.framework.BundleContext;

import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.IPojoFactory;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.felix.ipojo.extender.ExtensionDeclaration;
import org.apache.felix.ipojo.extender.builder.FactoryBuilderException;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.internal.FunctionalExtensionReference;
import fr.liglab.adele.cream.annotations.internal.HandlerReference;
import fr.liglab.adele.cream.annotations.provider.Creator;

import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ImporterIntrospection;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;

import de.mannheim.wifo2.iop.service.model.IFunctionality;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.iop.device.api.IOPService;


@Component
@Provides(specifications = {ImporterService.class,ImporterIntrospection.class})
public class ServiceImporter extends AbstractImporterComponent  {

	
	private static final Logger LOG = LoggerFactory.getLogger(ServiceImporter.class);

	@ServiceProperty(name = Factory.INSTANCE_NAME_PROPERTY)
	private String name;

	@ServiceProperty(name = "target", value = "(&(scope=generic)(iop.service.description=*))")
	private String filter;

	/**
	 * The set of declarations pending the availability  
	 */

	public ServiceImporter(BundleContext context) {
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

		ServiceDeclaration declaration	= new ServiceDeclaration(importDeclaration);
		Creator.Entity<?> creator 		= getCreator(declaration);
		
		if (creator != null) {
			String instanceId = getInstanceId(declaration);
			
			Map<String,Object> properties = new HashMap<>();
			properties.put(ContextEntity.State.id(GenericDevice.class,GenericDevice.DEVICE_SERIAL_NUMBER),instanceId);
			properties.put(ContextEntity.State.id(IOPService.class,IOPService.SERVICE_ID),declaration.getService().getId());

			creator.create(instanceId,properties);
		}
	}

	@Override
	protected void denyImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

		ServiceDeclaration declaration 	= new ServiceDeclaration(importDeclaration);
		Creator.Entity<?> creator 		= getCreator(declaration);

		if (creator != null) {
			String instanceId = getInstanceId(declaration);
			creator.delete(instanceId);
		}
	}

	/**
	 * Get the identifier of the service instance
	 */
	private static String getInstanceId(ServiceDeclaration serviceDeclaration) {
		return serviceDeclaration.getService().getName()+"-proxy-"+serviceDeclaration.getService().getId().getObjectID();
	}

	/**
	 * Get the creator to be used to create the proxy of a given declaration
	 */
	private Creator.Entity<?> getCreator(ServiceDeclaration declaration) {
		
		IPojoFactory factory = factoriesByName.get(declaration.getService().getName());

		if (factory == null) {
			factory = generateFactory(declaration);
		}
		
		return factory != null ? creators.apply(factory) : null;
	}

	
	/**
	 * The set of dynamic factories generated for representing remote services  
	 */
	private Map<String,IPojoFactory> factoriesByName = new HashMap<>(); 

	/**
	 * The creators associated to each dynamic factory
	 */
	private @Creator.Dynamic Function<Factory,Creator.Entity<?>> creators;

	/**
	 * The factory builder used to create ContextEntity factories
	 */
	@Requires(optional=false,proxy=false,filter="(ipojo.extension.name=context-component)")
	private ExtensionDeclaration contextEntityBuilder;

	/**
	 * The factory of the base component that is used as a template for all the generated factories
	 */
	@Requires(optional=false,proxy=false,filter="(factory.name=fr.liglab.adele.iop.device.proxies.IOPServiceProxy)")
	private Factory iopServiceProxy;
	
	/**
	 * The factories providing the proxy implementation of the functional interfaces of the service
	 */
	private Map<Factory,String[]> functionalExtensions = new HashMap<>();

	
	/**
	 * Generates a new factory that can be used to instantiate a proxy for the specified declaration
	 */
	private IPojoFactory generateFactory(ServiceDeclaration declaration) {
		try {
			
			IPojoFactory factory = contextEntityBuilder.getFactoryBuilder().build(
										iopServiceProxy.getBundleContext(), 
										generateMetadata(declaration));

			factoriesByName.put(factory.getName(),factory);
			factory.start();
			
			return factory;
		} catch (FactoryBuilderException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Generate the metadata describing the factory of the dynamic proxy
	 */
	private Element generateMetadata(ServiceDeclaration declaration) {
		/*
		 * Create the component metadata and clone attributes from the template 
		 */

		Element template	= iopServiceProxy.getComponentMetadata();
		Element metadata 	= new Element(template.getName(),template.getNameSpace());

		for (Attribute attribute : template.getAttributes()) {

			if (attribute.getName().equals("name")) {
				continue;
			}
			
			metadata.addAttribute(new Attribute(attribute.getName(),attribute.getNameSpace(),attribute.getValue()));
		}
		
		metadata.addAttribute(new Attribute("name", declaration.getService().getName()));

		/*
		 * Get the list of functional specifications to provide, and their corresponding extension
		 */
		Set<String> expectedInterfaces 	= new HashSet<>();
		for (IFunctionality functionality : declaration.getService().getFunctionalities()) {
			expectedInterfaces.add(functionality.getName());
		}

		Set<String> actualInterfaces 	= new HashSet<>();
		List<Element> extensions		= new ArrayList<>();
		
		for (Factory extension : functionalExtensions.keySet()) {
			
			Set<String> provided	= new HashSet<>(Arrays.asList(functionalExtensions.get(extension)));
			boolean intersects 		= expectedInterfaces.removeAll(provided);
			
			if (intersects) {
				
		        Element extensionMetadata = new Element(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_INDIVIDUAL_ELEMENT_NAME.toString(), "");
		        extensionMetadata.addAttribute(new Attribute(FunctionalExtensionReference.SPECIFICATION_ATTRIBUTE_NAME.toString(), "{"+String.join(",",provided)+"}"));
		        extensionMetadata.addAttribute(new Attribute(FunctionalExtensionReference.IMPLEMEMENTATION_ATTRIBUTE_NAME.toString(), extension.getName()));
		        extensionMetadata.addAttribute(new Attribute(FunctionalExtensionReference.ID_ATTRIBUTE_NAME.toString(), extension.getName()));
		        extensionMetadata.addAttribute(new Attribute(FunctionalExtensionReference.FUNCTIONAL_EXTENSION_MANDATORY_ATTRIBUTE_NAME.toString(),"false"));

		        extensions.add(extensionMetadata);
		        actualInterfaces.addAll(provided);
			}
			
			if (expectedInterfaces.isEmpty()) {
				break;
			}
		}
		

		/*
		 * Clone all elements of the template, but override the list of provided services and add new functional extensions
		 * to the existing container
		 */
		for (Element element : template.getElements()) {
			
			if (element.getName().equals("provides")) {
				Element provides = clone(element);
				for (Attribute attribute : provides.getAttributes()) {
					if (attribute.getName().equals("specifications")) {
						provides.removeAttribute(attribute);
						actualInterfaces.addAll(Arrays.asList(list(attribute.getValue())));
						provides.addAttribute(new Attribute("specifications", "{"+String.join(",",actualInterfaces)+"}"));
						break;
					}
				}
				metadata.addElement(provides);
			}
			else if (element.getName().equals(HandlerReference.FUNCTIONAL_EXTENSION_TRACKER_HANDLER)) {
				Element container = clone(element);
				for (Element extension : extensions) {
					container.addElement(extension);
				}
				metadata.addElement(container);
			}
			else {
				metadata.addElement(clone(element));
			}
		}
		
		return metadata;
	}
	/**
	 * Clones a metadata definition
	 */
	private static Element clone(Element self) {
		
		Element result  = new Element(self.getName(),self.getNameSpace());
		
		for (Attribute attribute : self.getAttributes()) {
			result.addAttribute(new Attribute(attribute.getName(),attribute.getNameSpace(),attribute.getValue()));
		}
		
		for (Element element : self.getElements()) {
			result.addElement(clone(element));
		}
		
		return result;
	}

	@Bind(id="functionalExtension",optional=true,aggregate=true,proxy=false,filter="(&(functional.extension.factory.property=*)(functional.extension.spec=fr.liglab.adele.iop.device.api.IOPService))")
	private void bindExtension(Factory extension, Map<String, Object> properties) {
		functionalExtensions.put(extension, optional(properties.get("functional.extension.spec"), new String[0]));
	}

	@Unbind(id="functionalExtension")
	private void unbindExtension(Factory extension, Map<String, Object> properties) {
		functionalExtensions.remove(extension);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T optional(Object value, T orElse) {
		return value != null ? (T) value : orElse;
	}
	
	public static String[] list(String value) {
		
		if (value == null) {
			return new String[0];
		}
		
		value = value.trim();
		
		if (value.isEmpty()) {
			return new String[0];
		}
		
		if (value.startsWith("{") && value.endsWith("}")) {
			return value.substring(1, value.length()-1).split(",");
		}
		
		return new String[] {value};
	}
}
