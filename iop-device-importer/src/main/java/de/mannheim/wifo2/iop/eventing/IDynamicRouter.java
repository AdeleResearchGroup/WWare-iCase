package de.mannheim.wifo2.iop.eventing;

public interface IDynamicRouter<T extends IEvent> {
	public void registerChannel(Integer type,
			IChannel<? extends T> channel);
	public void deregisterChannel(IChannel<? extends T> channel);
	public abstract void dispatch(T content);
}
