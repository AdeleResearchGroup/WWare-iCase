package fr.liglab.adele.interop.services.shutter;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.icasa.layering.applications.api.ApplicationLayer;

public @ContextService interface ShutterController extends ApplicationLayer {

	/**
	 * Sets the luminosity threshold that triggers closing the shutters
	 */
	void setThreshold(double threshold);

	/**
	 * The current threshold
	 */
	double getThreshold();

}