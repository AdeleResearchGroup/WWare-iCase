package de.mannheim.wifo2.iop.event;

public interface IChannel<T extends IEvent> {
	public void dispatch(T event);
}
