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

import de.mannheim.wifo2.iop.identifier.IServiceID;

import java.util.List;
import java.util.Map;

/**
 * Created by aygalinc on 20/04/16.
 */
public class IOPServiceDeclaration {

    public final static String INTERFACE_ID = "iop.interface.id";

    public final static String SERVICE_ID = "iop.service.id";

    private final IServiceID zwaveServiceId;

    private final List<String> zwaveInterfaceId;
    
    @SuppressWarnings("unchecked")
	public IOPServiceDeclaration(ImportDeclaration declaration){
        Map<String,Object> metadatas = declaration.getMetadata();

        zwaveServiceId = (IServiceID)metadatas.get(SERVICE_ID);
        zwaveInterfaceId = (List<String>)metadatas.get(INTERFACE_ID);
    }

    public IServiceID getServiceId() {
        return zwaveServiceId;
    }
    
    public List<String> getInterfaceId() {
        return zwaveInterfaceId;
    }


}
