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

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Filter;

import org.ow2.chameleon.fuchsia.core.FuchsiaUtils;

import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.core.exceptions.InvalidFilterException;


public class ControllerDeclaration {

    private final static Filter MATCHING_DECLARATION_FILTER = buildMatchFilter();

    private String id;
    private Map<String,Object> properties;


    private static Filter buildMatchFilter() {
    	
        try {
        	return FuchsiaUtils.getFilter("(&(scope=generic)(protocol=iop)(SELF_ID=*)(SELF_LOCATION=*))");
        } catch (InvalidFilterException e) {
            throw new IllegalStateException(e);
        }
    }

    public static ControllerDeclaration from(ImportDeclaration importDeclaration) throws BinderException {
        
    	Map<String, Object> metadata = importDeclaration.getMetadata();

        if (!MATCHING_DECLARATION_FILTER.matches(metadata)) {
            throw new BinderException("Not enough information in the metadata to be used by the IOP Xware importer");
        }
        
        ControllerDeclaration declaration = new ControllerDeclaration();

        declaration.id			= (String) metadata.get("id");
        declaration.properties	= loadProperties(metadata);
        
        return declaration;
    }

    /**
     * Loads the controller configuration from the metadata.
     * 
     * Controller configuration properties are identified because the value has the format
     * string literal=class name
     */
    public static Map<String,Object> loadProperties(Map<String,Object> metadata) {

    	
    	Map<String, Object> result = new HashMap<>();
		for (String key : metadata.keySet()) {
			Object value = metadata.get(key);
			
			if (!(value instanceof String)) {
				continue;
			}
			
			String[] parts = ((String)value).split("=");
			
			if (parts.length != 2) {
				continue;
			}
			
			
			try {
				Class<?> c = Class.forName(parts[1]);
				result.put(key, c.getConstructor(new Class[] {String.class})
						.newInstance(parts[0]));
			} catch (ClassNotFoundException | InstantiationException |
						IllegalAccessException | IllegalArgumentException | InvocationTargetException |
						NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}

		}
		
		return result;

    }
    
    private ControllerDeclaration() {
    }
    
    
    public Map<String,Object> getProperties() {
        return properties;
    }
    
    
    public String getId() {
    	return id;
    }
    
}
