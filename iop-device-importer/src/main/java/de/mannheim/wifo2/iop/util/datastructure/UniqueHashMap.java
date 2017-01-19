package de.mannheim.wifo2.iop.util.datastructure;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public class UniqueHashMap<T, U> {
	private HashMap<T, U> mFirst;
	private HashMap<U, T> mSecond;
	
	public UniqueHashMap() {
		mFirst = new HashMap<>();
		mSecond = new HashMap<>();
	}
	
	public void put(T key, U value)  {
		synchronized (this) {
			mFirst.put(key, value);
			mSecond.put(value, key);
		}
	}
	
	public U getValue(T key)  {
		return mFirst.get(key);
	}
	
	public T getKey(U value)  {
		return mSecond.get(value);
	}
	
	public boolean containsKey(T key)  {
		return mFirst.containsKey(key);
	}
	
	public boolean containsValue(U value)  {
		return mSecond.containsKey(value);
	}
	
	public void removeByKey(T key)  {
		synchronized (this) {
			U value = mFirst.remove(key);
			mSecond.remove(value);
		}
	}
	
	public void removeByValue(U value)  {
		synchronized (this) {
			T key = mSecond.remove(value);
			mFirst.remove(key);
		}
	}
	
	public Set<T> keySet()  {
		return mFirst.keySet(); 
	}
	
	public Collection<U> values()  {
		return mFirst.values();
	}
	
	@Override
	public String toString()  {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for(Entry<T, U> e : mFirst.entrySet())  {
			sb.append("(" + e.getKey() + ":" + e.getValue() + "), ");
		}
		sb.append("]");
		
		return sb.toString();
	}
}
