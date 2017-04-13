package de.mannheim.wifo2.iop.event;

public class Handler implements IChannel<IEvent> {

	@Override
	public void dispatch(IEvent message) {
		System.out.println(message.getType());
	}
}
