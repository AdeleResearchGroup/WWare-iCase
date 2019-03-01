package fr.liglab.adele.interop.services.temperature;

import javax.measure.Quantity;
import javax.measure.quantity.Dimensionless;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;
import fr.liglab.adele.icasa.layering.applications.api.ApplicationLayer;
import tec.units.ri.quantity.Quantities;
import tec.units.ri.unit.Units;


public @ContextService interface TemperatureControl extends ApplicationLayer {

	public static class Availability {
		
		public final Quantity<Dimensionless> heaters;
		public final Quantity<Dimensionless> locals;
		public final Quantity<Dimensionless> remotes;

		public Availability(float heaters, float locals, float remotes) {
			this.heaters 	= Quantities.getQuantity(heaters, Units.PERCENT);
			this.locals 	= Quantities.getQuantity(locals, Units.PERCENT);
			this.remotes 	= Quantities.getQuantity(remotes, Units.PERCENT);
		}
		
		public final static Availability DEFAULT = new Availability(0,0,0); 
	}

    public static final  @State String APPLICATION_STATE ="application.state";
    Availability getAvailability();
    
}
