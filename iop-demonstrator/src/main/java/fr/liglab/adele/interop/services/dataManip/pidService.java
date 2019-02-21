package fr.liglab.adele.interop.services.dataManip;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;
@ContextService
public interface pidService {
    @State
    String SERVICE_STATUS = "service.status";

    boolean setPIDvars(double P, double I, double D);
    boolean startPID(double objective);
    double getControlVariableValue(double objective, double currentValue);
    String getServiceStatus();

}
