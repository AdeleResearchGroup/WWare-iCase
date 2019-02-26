package fr.liglab.adele.interop.time.series.influx;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;

import org.influxdb.InfluxDBIOException;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;

/**
 * A utility class to access the influx database line protocol
 * 
 *
 */
public class Database {

	private final String databaseName;

	private final InfluxDB driver;

	public Database(String databaseName) {

		this.databaseName = databaseName;

		this.driver = InfluxDBFactory.connect("http://localhost:8086", "root", "root");
		this.driver.setDatabase(databaseName);
	}

	public boolean isRunning() {
		try {
			driver.ping();
		} catch (InfluxDBIOException e) {
			return false;
		}

		return true;
	}

	private boolean verifyRunning = false;
	
	public void setVerifyRunning(boolean verifyRunning) {
		this.verifyRunning = verifyRunning;
	}

	public void drop() {
		query("DROP DATABASE", quoted(databaseName));
	}

	public void create() {
		query("CREATE DATABASE ", quoted(databaseName));
		driver.setDatabase(databaseName);
	}

	public boolean exists() {

		// {"results":[{"series":[{"name":"databases","columns":["name"],"values":[["mydb"]]}]}]}
		// Series [name=databases, columns=[name], values=[[mydb],
		// [unittest_1433605300968]]]

		for (List<Object> database : values(query("SHOW DATABASES"))) {
			Object name = database != null && !database.isEmpty() ? database.get(0) : null;
			if (name != null && name.toString().trim().equals(databaseName)) {
				return true;
			}
		}

		return false;

	}

	/**
	 * Generic query function
	 */

	public QueryResult query(String command, String... args) {

		if (verifyRunning && !isRunning()) {
			return null;
		}

		StringBuilder query = new StringBuilder();

		query.append(command);
		for (String arg : args) {
			query.append(" ").append(arg);
		}

		try {
			return driver.query(new Query(query.toString(), databaseName));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String quoted(String value) {
		return quoted(value,false);
	}
	
	public static String quoted(String value, boolean single) {
		StringBuilder quoted 	= new StringBuilder();
		char quote			 	= single ? '\'' : '"'; 
		
		return quoted.append(quote).append(value).append(quote).toString();
	}

	/**
	 * The values of the first series of the first result
	 * 
	 */
	public static List<List<Object>> values(QueryResult result) {

		if (result == null) {
			return Collections.emptyList();
		}

		List<Result> results = maybe(result.getResults());
		if (results.isEmpty()) {
			return Collections.emptyList();
		}

		List<Series> series = maybe(results.get(0).getSeries());
		if (series.isEmpty()) {
			return Collections.emptyList();
		}

		return maybe(series.get(0).getValues());
	}

	public static String timestamp(List<Object> row) {
		return row != null && ! row.isEmpty() ? (String) first(row) : null;
	}

	public static <E> E first(List<E> list) {
		return ! list.isEmpty() ? list.get(0) : null;
	}

	public static <E> E last(List<E> list) {
		return ! list.isEmpty() ? list.get(list.size()-1) : null;
	}

	private static <E> List<E> maybe(List<E> list) {
		return list != null ? list : Collections.emptyList();
	}

	
	/**
	 * Generic write function
	 */

	public void write(BatchPoints points) {
		
		if (verifyRunning && !isRunning()) {
			return;
		}

		try {
			driver.write(points);	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void write(Point point) {
		
		if (verifyRunning && !isRunning()) {
			return;
		}

		try {
			driver.write(point);	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	/**
	 * Utility methods for building select queries
	 * 
	 */
	
	public QueryResult select(String field, String measure, int limit, String ...filters) {
        return query("SELECT", field, "FROM", measure, condition(filters), "LIMIT", Integer.toString(limit));
	}

	public QueryResult select(String field, String measure,  String ...filters) {
        return query("SELECT", field, "FROM", measure, condition(filters));
	}

	public enum Function {
		
		MEAN,
		MEDIAN,
		MIN,
		MAX,
		SPREAD,
		STDDEV,

		SUM,
		COUNT,
		
		FISRT,
		LAST;
		
		public final String of(String field) {
			StringBuilder result = new StringBuilder();
			result.append(name()).
				append('(').
				append(field).
				append(')').toString();
			
			return result.toString();
		}

	}
	

	private static String condition(String... filters) {

		StringBuilder result = new StringBuilder();

		String condition = conjunction(filters);
		if (! condition.isEmpty()) {
			result.append("WHERE").
			append(' ').
			append(condition);
		}
		
		
		return result.toString();

	}

	public static String conjunction(String... operands) {
		return connective("AND",operands);
	}
	
	public static String disjunction(String... operands) {
		return connective("OR",operands);
	}
	
	private static String connective(String connective, String... operands) {

		StringBuilder result = new StringBuilder();

		boolean isFirst = true;
		for (String operand : operands) {
			
			/*
			 * ignore empty operands
			 */
			if (operand.trim().isEmpty()) {
				continue;
			}
			
			/*
			 * concatenate connectivity between operands
			 */
			if (!isFirst) {
				result.append(' ').append(connective).append(' ');
			}
			
			result.append(operand.trim());
			isFirst = false;
		}
		
		return result.toString(); 
	}

	public static String where(String field, String operator, Object value) {
		
		StringBuilder result = new StringBuilder();
		
		result.append(quoted(field)).
			append(' ').append(operator).append(' ').
			append(value(value));
		
		return result.toString();

	}

	public static String value(Object value) {
		if (value instanceof Number)
			return value((Number) value);

		if (value instanceof String)
			return value((String) value);

		if (value instanceof Expression)
			return value((Expression) value);

		return value.toString();
	}

	public static String value(Number value) {
		return Double.toString(((Number) value).doubleValue());
	}
	
	public static String value(String value) {
		return quoted(value,true);
	}

	public static String value(Expression expression) {
		return expression.unquouted;
	}

	public static class Expression {
		
		private final String unquouted;
		
		private Expression(String unquouted) {
			this.unquouted = unquouted;
		}
	}
	
	public static Expression expression(String... terms) {
		
		StringBuilder expression = new StringBuilder();
		for (String term : terms) {
			expression.append(term).append(' ');
		}
		
		return new Expression(expression.toString().trim());
	}
	
	public static String since(long time, TimeUnit unit) {
		return where("time", ">", TimeUnit.NANOSECONDS.convert(time,unit));
	}

	public static String until(long time, TimeUnit unit) {
		return where("time", "<", TimeUnit.NANOSECONDS.convert(time,unit));
	}

	public static String atTime(long time, TimeUnit unit) {
		return where("time", "=", TimeUnit.NANOSECONDS.convert(time,unit));
	}

	public static String since(String timestamp) {
		return where("time", ">", timestamp);
	}

	public static String until(String timestamp) {
		return where("time", "<", timestamp);
	}

	public static String atTime(String timestamp) {
		return where("time", "=", timestamp);
	}
	
	public static String since(Expression timestamp) {
		return where("time", ">", timestamp);
	}

	public static String until(Expression timestamp) {
		return where("time", "<", timestamp);
	}

	public static String atTime(Expression timestamp) {
		return where("time", "=", timestamp);
	}

	public final static String NOW = "now()";

	public static String weeks(long weeks) {
		return weeks+"w";
	}

	public static String duration(long time, TimeUnit unit) {
		return time+suffix(unit);
	}

	private static String suffix(TimeUnit unit) {
		switch (unit) {
		case NANOSECONDS: 	return "ns";
		case MICROSECONDS: 	return "u";
		case MILLISECONDS: 	return "ms";
		case SECONDS: 		return "s";
		case MINUTES: 		return "m";
		case HOURS: 		return "h";
		case DAYS: 			return "d";
		default:  			return "";
		}
	}


}
