package de.mannheim.wifo2.iop.rosecontroller;

import de.mannheim.wifo2.iop.eventing.IEvent;
import de.mannheim.wifo2.iop.plugin.APlugin;
import de.mannheim.wifo2.iop.plugin.IPlugin;
import de.mannheim.wifo2.iop.system.IEnqueue;
import de.mannheim.wifo2.iop.system.IStop;
import de.mannheim.wifo2.iop.util.datastructure.Queue;

public class RoseController implements IEnqueue, Runnable, IStop  {

	private Thread mThread;
	private Queue<IEvent> mQueue;
	private volatile boolean mIsRunning;
	
	public RoseController() {
		mIsRunning = false;
		mQueue = new Queue<IEvent>();
		
		mThread = new Thread(this);
		mThread.start();
		mIsRunning = true;
	}
	
	@Override
	public void enqueue(IEvent message) {
		mQueue.enqueue(message);
	}

	@Override
	public void run() {
		while(mIsRunning)  {
			if(!mQueue.isEmpty())  {				
				IEvent event = mQueue.dequeue();
				
				System.out.println("ROSECONTROLLER: " + event);
			}
			else  {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void main(String[] args)  {
		IEnqueue mController = new RoseController();
		IPlugin rosePlugin = new APlugin("iCASA", mController, null);
		rosePlugin.start();
		rosePlugin.stop();
	}

	@Override
	public void stop() {
		mIsRunning = false;
	}
}
