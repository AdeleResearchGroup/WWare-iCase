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

import java.util.List;
import java.util.Map;

import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclarationBuilder;

import de.mannheim.wifo2.iop.service.model.IFunctionality;

public class ServiceDeclaration {


    public final static String SERVICE 					= "iop.exported.service";

    public final static String COMPONENT_ID				= "iop.exported.component.id";

    public final static String SERVICE_ID 				= "iop.exported.service.id";

    public final static String SERVICE_FUNCTIONALITIES	= "iop.exported.service.functionalities";

    public final static String SERVICE_PROPERTIES 		= "iop.exported.service.properties";


    private final String 				id;
    private final String				componentId;
    private final List<IFunctionality>	functionalities;
    private final Object				service;
    private final Map<String,?> 		properties;
    
	@SuppressWarnings("unchecked")
	public ServiceDeclaration(ExportDeclaration declaration) {
        Map<String,Object> metadata = declaration.getMetadata();
        id 				= (String) metadata.get(SERVICE_ID);
        componentId		= (String) metadata.get(COMPONENT_ID);
        service			= metadata.get(SERVICE);
        functionalities	= (List<IFunctionality>) metadata.get(SERVICE_FUNCTIONALITIES);
        properties		= (Map<String,?>) metadata.get(SERVICE_PROPERTIES);
    }

    public String getId() {
        return id;
    }
    
    public String getComponentId() {
    	return componentId;
    }
    
    public List<IFunctionality> getFunctionalities() {
    	return functionalities;
    }

    public Object getService() {
    	return service;
    }
    
    public Map<String,?> getProperties() {
    	return properties;
    }

    public static ExportDeclaration from(String componentId, Object service, String serviceId, List<IFunctionality> functionalities, Map<String,?> properties) {
    	
    	return ExportDeclarationBuilder.empty()
    			.key("scope").value("generic")
    			.key(COMPONENT_ID).value(componentId)
    			.key(SERVICE).value(service)
    			.key(SERVICE_ID).value(serviceId)
    			.key(SERVICE_FUNCTIONALITIES).value(functionalities)
    			.key(SERVICE_PROPERTIES).value(properties)
    			.build();
    }


}
