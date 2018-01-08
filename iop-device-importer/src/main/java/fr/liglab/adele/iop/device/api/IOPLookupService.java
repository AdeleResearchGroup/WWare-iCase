package fr.liglab.adele.iop.device.api;

public interface IOPLookupService {

	public void consider(String[] services);

	public void discard(String[] services);
	
	public void all();

	public void none();

}
