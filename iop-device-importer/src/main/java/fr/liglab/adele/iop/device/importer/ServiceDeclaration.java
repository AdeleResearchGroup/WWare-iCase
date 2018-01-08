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

import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;

import de.mannheim.wifo2.iop.service.model.IServiceDescription;

import java.util.Map;

public class ServiceDeclaration {

    public final static String SERVICE_DESCRIPTION = "iop.service.description";


    private final IServiceDescription service;

    
	public ServiceDeclaration(ImportDeclaration declaration){
        Map<String,Object> metadatas = declaration.getMetadata();
        service = (IServiceDescription) metadatas.get(SERVICE_DESCRIPTION);
    }

    public IServiceDescription getService() {
        return service;
    }

    public static ImportDeclaration from(IServiceDescription service) {
    	
    	return ImportDeclarationBuilder.empty()
    			.key("scope").value("generic")
    			.key(SERVICE_DESCRIPTION).value(service)
    			.build();
    }


}
