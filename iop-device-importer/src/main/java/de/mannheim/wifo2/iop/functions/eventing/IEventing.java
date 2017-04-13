package de.mannheim.wifo2.iop.functions.eventing;

import de.mannheim.wifo2.iop.event.IChannel;
import de.mannheim.wifo2.iop.event.IEvent;

public interface IEventing extends IChannel<IEvent>{
	public void notify(IEvent event);
	public void processNotification(IEvent event);
}
