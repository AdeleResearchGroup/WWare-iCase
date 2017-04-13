package de.mannheim.wifo2.iop.event.impl;

import java.util.HashMap;
import java.util.Map;

import de.mannheim.wifo2.iop.event.Event;
import de.mannheim.wifo2.iop.event.IEvent;
import de.mannheim.wifo2.iop.event.i.IEventingEvent;
import de.mannheim.wifo2.iop.eventing.EEventingType;
import de.mannheim.wifo2.iop.identifier.IComponentID;

public class EventingEvent extends Event 
			implements IEventingEvent  {
	private static final long serialVersionUID = 7485589877556701958L;

	private EEventingType mEventingType;
	private Map<String, Object> mProperties;
	
	public EventingEvent(IComponentID sourceID, EEventingType eventingType) {
		super(IEvent.EVENT_EVENTING, 0, sourceID);

		mEventingType = eventingType;
		mProperties = new HashMap<>();
	}

	@Override
	public EEventingType getEventingType() {
		return mEventingType;
	}

	@Override
	public void addProperty(String key, Object value) {
		mProperties.put(key, value);
	}

	@Override
	public Object getProperty(String key) {
		return mProperties.get(key);
	}
	
	@Override
	public String toString()  {
		StringBuffer sb = new StringBuffer();
		sb.append("Event(");
		sb.append(getType() + ", ");
		sb.append(getEventingType() + ", ");
		sb.append(mProperties);
		sb.append(")");
		
		return sb.toString();
	}
}
