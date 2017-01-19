package de.mannheim.wifo2.iop.eventing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mannheim.wifo2.iop.util.debug.DebugConstants;
import de.mannheim.wifo2.iop.util.debug.Log;

public class EventDispatcher implements IDynamicRouter<IEvent>  {	
	private Map<Integer, List<IChannel<IEvent>>> mHandlers;
	
	public EventDispatcher() {
		mHandlers = new HashMap<>();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void registerChannel(Integer type, 
			IChannel<? extends IEvent> channel) {
		List<IChannel<IEvent>> registeredHandlers = mHandlers.get(type);
		
		if(registeredHandlers == null)  {
			mHandlers.put(type, new ArrayList<>());
			registeredHandlers = mHandlers.get(type);
		}
		
		registeredHandlers.add((IChannel<IEvent>)channel);
	}
	
	public void deregisterChannel(IChannel<? extends IEvent> channel)  {
		for(List<IChannel<IEvent>> registeredHandlers : mHandlers.values())  {		
			if(registeredHandlers == null)  {
				continue;
			}
			
			if(registeredHandlers.contains(channel))  {
				registeredHandlers.remove(channel);
			}
		}
	}

	@Override
	public void dispatch(IEvent event) {
		List<IChannel<IEvent>> registeredHandlers = mHandlers.get(event.getType());

		if(DebugConstants.DISPATCHER)
			Log.log(getClass(), "Dispatching event: " + event.toString());
		
		if(registeredHandlers != null)  {
			for(IChannel<IEvent> h : registeredHandlers)  {
				h.dispatch(event);
			}
		}
	}
}
