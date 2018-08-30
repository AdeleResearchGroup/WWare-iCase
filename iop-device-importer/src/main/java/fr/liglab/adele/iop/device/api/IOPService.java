package fr.liglab.adele.iop.device.api;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;

public  @ContextService interface IOPService {

    public static  @State  String SERVICE_ID	= "iop.service.id";

	public static final String isIOPService = "(objectClass=fr.liglab.adele.iop.device.api.IOPService)";

}
