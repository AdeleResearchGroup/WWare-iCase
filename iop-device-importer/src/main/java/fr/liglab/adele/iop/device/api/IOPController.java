package fr.liglab.adele.iop.device.api;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;

public @ContextService interface IOPController extends IOPLookupService, IOPInvocationHandler {

    public static @State  String PROPERTIES	="iop.controller.properties";
	
}
