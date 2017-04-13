package de.mannheim.wifo2.iop.functions.matching;

import java.util.List;

import de.mannheim.wifo2.iop.plugin.function.matching.IMatchRequest;
import de.mannheim.wifo2.iop.service.model.IServiceDescription;

public interface IServiceMatching {
	public List<? extends IServiceDescription> lookup(
			List<? extends IServiceDescription> services, IMatchRequest request);
}
