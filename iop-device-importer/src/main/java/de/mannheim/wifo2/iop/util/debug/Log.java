package de.mannheim.wifo2.iop.util.debug;

import java.io.PrintStream;

public final class Log {
	public static final boolean LOG = true;

	private static PrintStream out = System.out;
	
	public static void setOutput(PrintStream stream) {
    	if (stream != null) {
			out = stream;	
    	}
    }
	
	public static void log(String location, String message) {
		if (!LOG ) return;
		out.format("LOG|%-35s %s\n", location+":", message);
	}
	
	@SuppressWarnings("rawtypes")
	public static void log(Class location, String message){
		log(((location == null)?null:location.getSimpleName()), message);
	}
}
