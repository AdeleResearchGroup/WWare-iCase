package fr.liglab.adele.interop.services.dataManip;

public interface pidService {
    double getControlVariableValue(double P, double I, double D, double objective, double currentValue);

}
