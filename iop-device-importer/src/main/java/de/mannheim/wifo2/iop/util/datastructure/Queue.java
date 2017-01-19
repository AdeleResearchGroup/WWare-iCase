package de.mannheim.wifo2.iop.util.datastructure;

import java.util.NoSuchElementException;

public class Queue<T>  {

	@SuppressWarnings("hiding")
	private class QueueElement<T>  {
		private QueueElement<T> next;
		private T data;
		
		private QueueElement(QueueElement<T> next, T data)  {
			this.next = next;
			this.data = data;
		}
	}
	
	private int size;
	private QueueElement<T> first;
	private QueueElement<T> last;
	
	public Queue()  {
		this.size = 0;
		this.first = null;
		this.last = null;
	}
	
	public boolean isEmpty()  {
		return first == null;
	}
	
	public int size()  {
		return size;
	}
	
	public T peek()  {
		if(isEmpty())
			throw new NoSuchElementException("Queue underflow");
		return first.data;
	}
	
	public synchronized void enqueue(T data)  {
		QueueElement<T> oldLast = last;
		last = new QueueElement<T>(null, data);
		
		if(isEmpty())
			first = last;
		else
			oldLast.next = last;
		size++;
	}
	
	public synchronized T dequeue()  {
		if(isEmpty())
			throw new NoSuchElementException("Queue underflow");
		T item = first.data;
		first = first.next;
		size--;
		if(isEmpty())
			last = null;
		return item;
	}
	
	public String toString()  {
		StringBuilder s = new StringBuilder();
		QueueElement<T> element = first;
		if(!isEmpty())  {
			s.append(element.data + " ");
			
			while(element.next != null)
				s.append(element.data + " ");
		}
		return s.toString();
	}
}
