package de.mannheim.wifo2.iop.announcement;

import de.mannheim.wifo2.iop.event.IEvent;
import de.mannheim.wifo2.iop.identifier.IPluginID;
import de.mannheim.wifo2.iop.mediator.IMediator;
import de.mannheim.wifo2.iop.plugin.function.discovery.impl.DeviceAnnouncement;
import de.mannheim.wifo2.iop.util.i.IEnqueue;

public class RoseAnnouncement extends DeviceAnnouncement {

	public RoseAnnouncement(IEnqueue queue, IPluginID pluginID, IMediator mediator, int advertisementPeriod) {
		super(queue, pluginID, mediator, advertisementPeriod);
	}

	@Override
	public void dispatch(IEvent event) {
		int type = event.getType();

		if (type == IEvent.EVENT_ANNOUNCEMENT) {

			mMediator.enqueue(event);

		}
	}

}
