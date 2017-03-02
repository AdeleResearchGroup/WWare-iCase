package fr.liglab.adele.iop.device.tools;

import java.util.Arrays;

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
    public void lookup(@Descriptor("lookup add|remove|none|all services...") String... parameters) {

    	if (parameters.length < 1) {
    		System.out.println("command usage : lookup add|remove|none|all services...");
    	}
    	else if (parameters[0].equalsIgnoreCase("all")) {
    		lookup.all();
    	}
    	else if (parameters[0].equalsIgnoreCase("none")) {
    		lookup.none();
    	}
    	else if (parameters[0].equalsIgnoreCase("add")) {
    		lookup.consider(Arrays.copyOfRange(parameters, 1, parameters.length));
    	}
    	else if (parameters[0].equalsIgnoreCase("remove")) {
    		lookup.discard(Arrays.copyOfRange(parameters, 1, parameters.length));
    	}
    }

}
