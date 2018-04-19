package de.mannheim.wifo2.iop.lookup;

import de.mannheim.wifo2.iop.event.IEvent;
import de.mannheim.wifo2.iop.identifier.IPluginID;
import de.mannheim.wifo2.iop.mediator.IMediator;
import de.mannheim.wifo2.iop.plugin.function.discovery.ILookup;
import de.mannheim.wifo2.iop.plugin.function.matching.IServiceMatching;
import de.mannheim.wifo2.iop.util.i.IEnqueue;

public class ICasaLookup implements ILookup  {

	IMediator mMediator;
	
	public ICasaLookup(IEnqueue queue, IPluginID pluginID, IMediator mediator, IServiceMatching matching) {
		mMediator = mediator;
	}
	
	@Override
	public void dispatch(IEvent arg0) {
		mMediator.enqueue(arg0);
	}

	@Override
	public void processLookupRequest(IEvent arg0) { }

	@Override
	public void processLookupResponse(IEvent arg0) { }

}
