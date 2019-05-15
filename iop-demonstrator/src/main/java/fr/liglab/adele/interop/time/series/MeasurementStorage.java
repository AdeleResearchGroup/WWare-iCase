package fr.liglab.adele.interop.time.series;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;

import fr.liglab.adele.interop.services.shutter.ShutterController;
import fr.liglab.adele.interop.services.temperature.TemperatureController;

import fr.liglab.adele.time.series.SeriesDatabase;
import static fr.liglab.adele.time.series.SeriesDatabase.*;

import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.QueryResult;

import static org.influxdb.dto.BatchPoints.*;
import static org.influxdb.dto.Point.*;

import fr.liglab.adele.icasa.clockservice.Clock;
import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;
import tec.units.ri.unit.Units;

import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.device.doorWindow.WindowShutter;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.light.DimmerLight;
import fr.liglab.adele.icasa.device.light.Photometer;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.device.temperature.ThermometerExt;

import fr.liglab.adele.icasa.layering.applications.api.ApplicationLayer;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;

import fr.liglab.adele.icasa.layering.services.location.ZoneService;

@Component
@Provides(specifications = {MeasurementStorage.class, PeriodicRunnable.class})
public class MeasurementStorage implements PeriodicRunnable {

	

	@Requires(optional=false, proxy=false)
	Clock clock;


	@Requires(optional=false, proxy=false, filter="(name="+DATABASE_NAME+")")
	private SeriesDatabase database;

	public static final String DATABASE_NAME = "test";

	/**
	 * The public series stored in this service
	 * 
	 */
	public enum Measurement {
		
		ANY("*"),
		UNKNOWN("unknown"),

		CLOCK("clock"),

		QOS("qos"),

		COVERAGE("coverage"),

		POWER_LEVEL("powerLvl"),
		POWER_STATUS("powerStatus"),
		SHUTTER_LEVEL("ShutterLvl"),
		PRESENCE("presence"),
		TEMPERATURE("temperature"),
		ILLUMINANCE( "illuminance");
		
		private final String label;
		
		private Measurement(String label) {
			this.label = label;
		}
		
		public Point.Builder at(long timestamp, TimeUnit unit) {
			return measurement(label).time(timestamp, unit);
		}
		
		public static Measurement of(GenericDevice device) {

			return choice(
				when(is(device, Cooler.class), constant(Measurement.POWER_LEVEL)),
				when(is(device, Heater.class), constant(Measurement.POWER_LEVEL)),
				when(is(device, BinaryLight.class), constant(Measurement.POWER_STATUS)),
				when(is(device, WindowShutter.class), constant(Measurement.SHUTTER_LEVEL)),
				when(is(device, DimmerLight.class), constant(Measurement.POWER_LEVEL)),
				when(is(device, PresenceSensor.class), constant(Measurement.PRESENCE)),
				when(is(device, Thermometer.class), constant(Measurement.TEMPERATURE)),
				when(is(device, ThermometerExt.class), constant(Measurement.TEMPERATURE)),
				when(is(device, Photometer.class), constant(Measurement.ILLUMINANCE)), 
				otherwise(Measurement.UNKNOWN)
			);

		}
		
	}
	
	
	private Long previousRun = null;
	
	@Validate
	private void start() {
		database.setVerifyRunning(false);
		if (database.isRunning()) {
			database.drop();
			database.create();
		}
		
		previousRun = null;
	}

	@Override
	public long getPeriod() {
		return 30;
	}

	@Override
	public TimeUnit getUnit() {
		return TimeUnit.MINUTES;
	}

	@Override
	public void run() {
		try {
			if (database.isRunning()) {
				store();
			}
		} catch (Throwable unexpected) {
			unexpected.printStackTrace();
		}
	}

	@Requires(optional = true, proxy = false, specification = GenericDevice.class)
	private List<GenericDevice> devices;

	@Requires(optional = true, proxy = false, specification = ServiceLayer.class)
	private List<ServiceLayer> services;

	@Requires(optional = true, proxy = false, specification = ApplicationLayer.class)
	private List<ApplicationLayer> applications;

	private void store() throws Throwable {

		long timestamp = clock.currentTime(TimeUnit.NANOSECONDS);

		BatchPoints.Builder points = database(DATABASE_NAME);
		
		if ( previousRun != null) {
			points.point(
				Measurement.CLOCK.at(timestamp, TimeUnit.NANOSECONDS).
				addField("real", System.currentTimeMillis()).
				addField("elapsed", timestamp - previousRun).
				build()
			);
		}
		
		previousRun = timestamp;
		
		for (GenericDevice device : devices) {
			
			Measurement deviceMeasurement = Measurement.of(device);
			if (deviceMeasurement == Measurement.UNKNOWN) {
				continue;
			}

			/*
			 * Handle NaN and Infinite values as absent values see https://github.com/influxdata/influxdb/issues/4089
			 */
			double value = valueOf(device).doubleValue();
			if (! Double.isFinite(value)) {
				continue;
			}

			points.point(
				deviceMeasurement.at(timestamp, TimeUnit.NANOSECONDS).
				tag("name", device.getSerialNumber()).
				tag("type", typeOf(device)).
				tag("zone", zoneOf(device)).
				addField("value",value).build()
			);
		}

		for (ServiceLayer service : services) {
			points.point(
				Measurement.QOS.at(timestamp, TimeUnit.NANOSECONDS).
				tag("name", service.getServiceName()).
				tag("type", "service").
				tag("zone", zoneOf(service)).
				addField("value", service.getQoS()).
				build()
			);
		}
		
        for(ApplicationLayer application : applications) {
        	
     		if (application instanceof TemperatureController && application instanceof ZoneService) {
    			
    			double threshold = ((TemperatureController) application).getReference().to(Units.KELVIN).getValue().doubleValue();

    			points.point(
        				Measurement.TEMPERATURE.at(timestamp, TimeUnit.NANOSECONDS).
    					tag("name","TemperatureController.reference").
    					tag("type", "app").
    					tag("zone", ((ZoneService) application).getZone()).
    					addField("value", threshold).
    					build()
        			);                
    		}

    		if (application instanceof ShutterController && application instanceof ZoneService) {
    			
    			double threshold = ((ShutterController) application).getThreshold();

    			points.point(
        				Measurement.ILLUMINANCE.at(timestamp, TimeUnit.NANOSECONDS).
    					tag("name","ShutterController.threshold").
    					tag("type", "app").
    					tag("zone", ((ZoneService) application).getZone()).
    					addField("value", threshold).
    					build()
        			);                
    		}
        }

        database.write(points.build());
	}
	
	private static Number valueOf(GenericDevice device) {

		return choice(
			when(is(device, Cooler.class), Cooler::getPowerLevel),
			when(is(device, Heater.class), Heater::getPowerLevel),
			when(is(device, BinaryLight.class), numeric(BinaryLight::getPowerStatus)),
			when(is(device, WindowShutter.class), WindowShutter::getShutterLevel),
			when(is(device, DimmerLight.class), DimmerLight::getPowerLevel),
			when(is(device, PresenceSensor.class), numeric(PresenceSensor::getSensedPresence)),
			when(is(device, Thermometer.class), numeric(Thermometer::getTemperature, Units.KELVIN)),
			when(is(device, ThermometerExt.class), numeric(ThermometerExt::getTemperature, Units.KELVIN)),
			when(is(device, Photometer.class), numeric(Photometer::getIlluminance, Units.LUX)), 
			otherwise(0)
		);
	}

	private static String typeOf(GenericDevice device) {

		return choice(
			when(is(device, Cooler.class), type(Cooler.class)),
			when(is(device, Heater.class), type(Heater.class)),
			when(is(device, BinaryLight.class), type(BinaryLight.class)),
			when(is(device, WindowShutter.class), type(WindowShutter.class)),
			when(is(device, DimmerLight.class), type(DimmerLight.class)),
			when(is(device, PresenceSensor.class), type(PresenceSensor.class)),
			when(is(device, Thermometer.class), type(Thermometer.class)),
			when(is(device, ThermometerExt.class), type(ThermometerExt.class)),
			when(is(device, Photometer.class), type(Photometer.class)), 
			otherwise("unkown")
		);
	}

	private static <D extends GenericDevice> Function<D, String> type(Class<D> kind) {
		return (D device) -> kind.getSimpleName();
	}

	private static String zoneOf(GenericDevice device) {

		if (device instanceof LocatedObject) {
			return ((LocatedObject) device).getZone();
		}

		return "none";
	}

	private static String zoneOf(ServiceLayer service) {

		if (service instanceof ZoneService) {
			return ((ZoneService) service).getZone();
		}

		return "none";
	}

	/**
	 * General query interface
	 */
	public QueryResult select(Measurement measure, int limit, String ...filters) {
        return database.select("value",measure.label, limit, filters);
	}

	public QueryResult select(Measurement measure, SeriesDatabase.Function function, int limit, String ...filters) {
        return database.select(function.of("value"), measure.label, limit, filters);
	}

	public QueryResult select(Measurement measure,  String ...filters) {
        return database.select("value",measure.label, filters);
	}

	public QueryResult select(Measurement measure, SeriesDatabase.Function function, String ...filters) {
        return database.select(function.of("value"), measure.label, filters);
	}

	public static String ofType(Class<? extends GenericDevice> kind) {
        return where("type", "=", type(kind).apply(null));
	}

	public static String inZone(String zone) {
        return where("zone", "=", zone);
	}

	public static String valueIn(double lower, double upper) {
        return conjunction(where("value", ">=", lower), where("value", "<=", upper));
	}

	/**
	 * Small fluent API to a make a switch case based on the class of the device
	 */

	private static <D extends GenericDevice> Optional<D> is(GenericDevice device, Class<D> kind) {

		if (kind.isInstance(device)) {
			return Optional.of(kind.cast(device));
		}

		return Optional.empty();

	}

	private static <T, D extends GenericDevice> Optional<T> when(Optional<D> device, Function<D, T> extractor) {
		return device.map(extractor);
	}

	private static <D extends GenericDevice, T> Function<D, T> constant(T t) {
		return (D device) -> t;
	}

	private static <D extends GenericDevice> Function<D, Number> numeric(Function<D, Boolean> booleanFunction) {
		return (D device) -> booleanFunction.apply(device) ? 1 : 0;
	}

	private static <D extends GenericDevice, Q extends Quantity<Q>> Function<D, Double> numeric(Function<D, Quantity<Q>> quantityFunction, Unit<Q> unit) {
		return (D device) -> {
			Quantity<Q> value = quantityFunction.apply(device);
			return value != null ? value.to(unit).getValue().doubleValue() : Double.NaN;
		};
	}

	@SafeVarargs
	private static <T> T choice(Optional<? extends T>... choices) {

		for (Optional<? extends T> choice : choices) {
			if (choice.isPresent()) {
				return choice.get();
			}
		}

		return null;
	}

	private static <T> Optional<T> otherwise(T value) {
		return Optional.ofNullable(value);
	}


}
