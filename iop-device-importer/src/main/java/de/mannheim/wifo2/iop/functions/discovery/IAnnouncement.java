package de.mannheim.wifo2.iop.functions.discovery;

import de.mannheim.wifo2.iop.event.IChannel;
import de.mannheim.wifo2.iop.event.IEvent;
import de.mannheim.wifo2.iop.util.i.IStart;
import de.mannheim.wifo2.iop.util.i.IStop;

public interface IAnnouncement extends IChannel<IEvent>, IStart, IStop {
	public void announce();
	public void processAnnouncement(IEvent event);
}
