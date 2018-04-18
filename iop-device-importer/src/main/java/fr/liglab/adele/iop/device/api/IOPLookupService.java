package fr.liglab.adele.iop.device.api;

import java.util.List;
import java.util.Map;

public interface IOPLookupService {

	public void consider(String[] services, Map<String,String> query);

	public void discard(String[] services);

	public List<String> considered();

	public void all();

	public void none();

}
