package de.mannheim.wifo2.iop.functions.discovery;

import de.mannheim.wifo2.iop.eventing.IChannel;
import de.mannheim.wifo2.iop.eventing.IEvent;
import de.mannheim.wifo2.iop.system.IStart;
import de.mannheim.wifo2.iop.system.IStop;

public interface IAnnouncement extends IChannel<IEvent>, IStart, IStop {
	public void announce();
	public void processAnnouncement(IEvent event);
}
