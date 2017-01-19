package de.mannheim.wifo2.iop.eventing.event;

public class EventID   {
	
	private static EventID mInstance;
	
	private int mID = 0;
	
	private EventID()  {
		
	}
	
	public static EventID getInstance()  {
		if(mInstance == null)  {
			mInstance = new EventID();
		}
		return mInstance;
	}

	public synchronized int getNextID() {
		return mID++;
	}
}
