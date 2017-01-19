package de.mannheim.wifo2.iop.connection;

import de.mannheim.wifo2.iop.translation.IMessageHandler;

public abstract class AConnection implements IConnection  {
	private Thread mProcess;
	private IConnectionManager mConnManager;
	private int mBufferSize;
	private volatile boolean isClosed;
	private IMessageHandler mMessageHandler;
	
	public AConnection(IConnectionManager connManager)  {
		mConnManager = connManager;
		mMessageHandler = connManager.getMessageHandler();
		mBufferSize = 4092;
		isClosed = false;
		mProcess = null;
	}
	
	public IMessageHandler getMessageHandler()  {
		return mMessageHandler;
	}
	
	protected int getBufferSize()  {
		return mBufferSize;
	}
	
	protected IConnectionManager getConnectionManager()  {
		return mConnManager;
	}
	
	public void start()  {
		if(mProcess == null)  {
			mProcess = new Thread(this);
			mProcess.setDaemon(true);
			mProcess.start();
		}
	}
	
	protected boolean isClosed()  {
		return isClosed;
	}
	
	protected void setClosed(boolean closed)  {
		this.isClosed = closed;
	}
}
