package de.mannheim.wifo2.iop.location.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;

import de.mannheim.wifo2.iop.location.ILocation;

public class Location implements ILocation {
	private static final long serialVersionUID = 6669227366529914895L;
	
	private String mProtocol;
	private String mAddress;
	private Integer mPort;
	private String mPath;
	
	public Location(String protocol, String address, 
			Integer port, String path)  {
		this.mAddress = address;
		this.mPort = port;
		this.mProtocol = protocol;
		this.mPath = path;
	}
	
	public Location(String location)  {
		mProtocol = null;
		mAddress = null;
		mPort = null;
		mPath = null;
		
		int pos = location.indexOf("://");
		if(pos >= 0)  {
			mProtocol = location.substring(0, pos);
			location = location.substring(pos+3);
		}
		pos = location.indexOf("/");
		if(pos < 0)  {
			mAddress = location;
			mPath = null;
		}
		else  {
			mAddress = location.substring(0, pos);
			mPath = location.substring(pos);
		}
		pos = location.indexOf(":");
		if(pos < 0) {
			mPort = null;
		}
		else  {
			mPort = Integer.valueOf(mAddress.substring(pos+1));
			mAddress = mAddress.substring(0, pos);
		}
		
		if(mAddress.equals("127.0.0.1"))  {
			try {
				mAddress = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
	}
	
	public String getAddress() {
		return mAddress;
	}
	public Integer getPort() {
		return mPort;
	}
	public String getProtocol() {
		return mProtocol;
	}
	public String getPath() {
		return mPath;
	}

	public String toString()  {
		StringBuffer sb = new StringBuffer();
		if(mProtocol != null)  {
			sb.append(mProtocol + "://");
		}
		sb.append(mAddress);
		if(mPort != null)  {
			sb.append(":" + mPort);
		}
		if(mPath != null)  {
			sb.append(mPath);
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mAddress == null) ? 0 : mAddress.hashCode());
		result = prime * result + ((mPath == null) ? 0 : mPath.hashCode());
		result = prime * result + ((mPort == null) ? 0 : mPort.hashCode());
		result = prime * result + ((mProtocol == null) ? 0 : mProtocol.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Location other = (Location) obj;
		if (mAddress == null) {
			if (other.mAddress != null)
				return false;
		} else if (!mAddress.equals(other.mAddress))
			return false;
		if (mPath == null) {
			if (other.mPath != null)
				return false;
		} else if (!mPath.equals(other.mPath))
			return false;
		if (mPort == null) {
			if (other.mPort != null)
				return false;
		} else if (!mPort.equals(other.mPort))
			return false;
		if (mProtocol == null) {
			if (other.mProtocol != null)
				return false;
		} else if (!mProtocol.equals(other.mProtocol))
			return false;
		return true;
	}
}
