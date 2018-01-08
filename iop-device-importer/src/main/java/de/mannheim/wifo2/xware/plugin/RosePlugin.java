package de.mannheim.wifo2.xware.plugin;

import java.util.Map;

import de.mannheim.wifo2.iop.mediator.IMediator;
import de.mannheim.wifo2.iop.plugin.APlugin;
import de.mannheim.wifo2.iop.plugin.connectionmanager.IConnectionManager;

public class RosePlugin extends APlugin  {

	public RosePlugin(String name, IMediator mediator, Map<String, Object> properties) {
		super(name, mediator, properties);
	}

	public IConnectionManager getConnectionManager()  {
		return this.mConnectionManager;
	}
}
