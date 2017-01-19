package de.mannheim.wifo2.iop.translation;

import de.mannheim.wifo2.iop.eventing.IEvent;

public interface IMessageHandler {
	public static final int TYPE_ANNOUNCEMENT = 0;
	public static final int TYPE_LOOKUP = 1;
	public static final int TYPE_LOOKUP_RESPONSE = 2;
	public static final int TYPE_APPLICATION = 3;
	public static final int TYPE_APPLICATION_RESPONSE = 4;
	public static final int TYPE_UNKNOWN = ~0;
	
	public IEvent processData(Object data, int type);
	public Object processEvent(IEvent event);
}
