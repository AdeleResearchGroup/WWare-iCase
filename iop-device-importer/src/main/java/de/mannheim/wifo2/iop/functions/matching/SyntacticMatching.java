package de.mannheim.wifo2.iop.functions.matching;

import java.util.ArrayList;
import java.util.List;

import de.mannheim.wifo2.iop.functions.matching.IServiceMatching;
import de.mannheim.wifo2.iop.plugin.function.matching.IMatchRequest;
import de.mannheim.wifo2.iop.service.model.IFunctionality;
import de.mannheim.wifo2.iop.service.model.IServiceDescription;
import de.mannheim.wifo2.iop.util.debug.DebugConstants;
import de.mannheim.wifo2.iop.util.debug.Log;

public class SyntacticMatching implements IServiceMatching {

	@Override
	public List<? extends IServiceDescription> lookup(
			List<? extends IServiceDescription> services, IMatchRequest request) {
		if(DebugConstants.MATCHING)
			Log.log(getClass(), request.toString());
		
		List<IServiceDescription> matchingServices = new ArrayList<>();
		String[] requestedFunctionalities = 
				(String[]) request.getProperty(IMatchRequest.FUNCTIONALITY);
		
		if(requestedFunctionalities != null)  {
			for(String rf : requestedFunctionalities)  {
				for(IServiceDescription s : services)  {
					for(IFunctionality c : s.getFunctionalities())  {
						if(rf.equals(c.getName()))  {
							matchingServices.add(s);
							continue;
						}
					}
				}
			}
		}
		else  {
			matchingServices.addAll(services);
		}
		
		return matchingServices;
	}

}
