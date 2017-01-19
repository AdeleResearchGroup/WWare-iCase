package de.mannheim.wifo2.iop.eventing;

public interface IChannel<T extends IEvent> {
	public void dispatch(T event);
}
