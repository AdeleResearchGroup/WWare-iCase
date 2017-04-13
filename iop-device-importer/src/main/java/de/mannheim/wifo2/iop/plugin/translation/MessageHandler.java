package de.mannheim.wifo2.iop.plugin.translation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import de.mannheim.wifo2.iop.event.IEvent;
import de.mannheim.wifo2.iop.plugin.translation.IMessageHandler;

public class MessageHandler implements IMessageHandler {

	public MessageHandler() {
	
	}
	
	@Override
	public IEvent processData(Object data, int type) {
		IEvent event = null;
		if(type == IMessageHandler.TYPE_ANNOUNCEMENT)  {
			try {
				ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream((byte[]) data));
				event = (IEvent) ois.readObject();
				ois.close();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		else  {
			event = (IEvent) data;
		}
		return event;
	}

	@Override
	public Object processEvent(IEvent event) {
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		try {
//			ObjectOutputStream oos = new ObjectOutputStream(baos);
//			oos.writeObject(event);
//			oos.flush();
//			oos.close();
//			baos.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} 
//		
//		return baos.toByteArray();
		return event;
	}

}
