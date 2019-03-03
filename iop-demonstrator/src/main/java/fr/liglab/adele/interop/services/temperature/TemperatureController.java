package fr.liglab.adele.interop.services.temperature;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.icasa.layering.applications.api.ApplicationLayer;


public @ContextService interface TemperatureController extends ApplicationLayer {


	public void setReference(Quantity<Temperature> reference);
	
	public Quantity<Temperature> getReference();
	
}
