package fr.liglab.adele.interop.demonstrator.home.temperature;

import javax.measure.Quantity;
import javax.measure.quantity.Dimensionless;

import fr.liglab.adele.cream.annotations.ContextService;
import fr.liglab.adele.cream.annotations.State;
import tec.units.ri.quantity.Quantities;
import tec.units.ri.unit.Units;

@ContextService
public interface RoomTemperatureControl {

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
    
    public static final @State String APPLICATION_QOS="application.qos";
    int getAppQoS();

}
