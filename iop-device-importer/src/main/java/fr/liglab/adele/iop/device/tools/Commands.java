package fr.liglab.adele.iop.device.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;

import org.apache.felix.service.command.Descriptor;

import fr.liglab.adele.iop.device.api.IOPLookupService;

@Component(immediate = true)
@Provides(specifications=Commands.class)
@Instantiate

public class Commands {

    @ServiceProperty(name = "osgi.command.scope", value = "iop")
    private String scope;

    @ServiceProperty(name = "osgi.command.function", value = "{}")
    private String[] function = new String[]{"lookup"};

    @Requires(optional=false,proxy=false)
    IOPLookupService lookup;
    
    @Descriptor("configure service lookup")
    public void lookup(@Descriptor("lookup list|none|all|add services...|remove services...") String... parameters) {

    	if (parameters.length < 1) {
    		System.out.println("command usage : lookup list|none|all|add services...|remove services...");
    	}
    	else if (parameters[0].equalsIgnoreCase("list")) {
    		System.out.println("iop lookup = {");
    		for (String request : lookup.considered()) {
	    		System.out.println("	"+request);
			}
    		System.out.println("}");
    	}
    	else if (parameters[0].equalsIgnoreCase("all")) {
    		lookup.all();
    	}
    	else if (parameters[0].equalsIgnoreCase("none")) {
    		lookup.none();
    	}
    	else if (parameters[0].equalsIgnoreCase("add")) {
    		List<String> interfaces = new ArrayList<>();
    		Map<String,String> query = new HashMap<>();
    		
    		for (int i = 1; i < parameters.length; i++) {
				String parameter = parameters[i];
				if (parameter.startsWith("-")) {
					query.put(parameter.substring(1), parameters[++i]);
				}
				else {
					interfaces.add(parameter);
				}
				
			}
    		
    		lookup.consider(interfaces.toArray(new String[0]),query);
    	}
    	else if (parameters[0].equalsIgnoreCase("remove")) {
    		lookup.discard(Arrays.copyOfRange(parameters, 1, parameters.length));
    	}
    }

}
