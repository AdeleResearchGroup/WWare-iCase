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

import org.osgi.framework.Filter;

import org.ow2.chameleon.fuchsia.core.FuchsiaUtils;

import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.core.exceptions.InvalidFilterException;

import fr.liglab.adele.iop.device.api.IOPController;

import java.util.Map;

public class ControllerDeclaration {

    private final static Filter MATCHING_DECLARATION_FILTER = buildMatchFilter();

    private String id;
    private String broadcast;
    private int port;


    private static Filter buildMatchFilter() {
    	
        try {
        	return FuchsiaUtils.getFilter("(&(scope=generic)(protocol=iop)(broadcast.address=*)(broadcast.port=*))");
        } catch (InvalidFilterException e) {
            throw new IllegalStateException(e);
        }
    }

    public static ControllerDeclaration from(ImportDeclaration importDeclaration) throws BinderException {
        
    	Map<String, Object> metadata = importDeclaration.getMetadata();

        if (!MATCHING_DECLARATION_FILTER.matches(metadata)) {
            throw new BinderException("Not enough information in the metadata to be used by the zwave importer");
        }
        
        ControllerDeclaration declaration = new ControllerDeclaration();

        declaration.id			= (String)  metadata.get("id");
        declaration.broadcast	= (String) metadata.get(IOPController.BROADCAST_ADDRESS);
        declaration.port		= Integer.valueOf( (String)metadata.get(IOPController.BROADCAST_PORT));
        
        return declaration;
    }


    private ControllerDeclaration() {
    }
    
    public String getBroadcast() {
        return broadcast;
    }
    
    public int getPort() {
        return port;
    }
    
    public String getId() {
    	return id;
    }
    
}
