package com.form2bgames.terminusengine.events;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BasicEventHandler{
	private ConcurrentLinkedQueue<Event> eventQueue=new ConcurrentLinkedQueue<>();
	
	public void handle(Event e){
		eventQueue.add(e);
	}
	
	public void waitForEvent(){
		while(eventQueue.isEmpty()){
			try{
				Thread.sleep(0,1000);
			}catch(Exception e){}
		}
	}
	
	public Event nextEvent(){
		return eventQueue.poll();
	}
	
	public Iterator<Event> currentEventsIterator(){
		return eventQueue.iterator();
	}
	
	public void clearQueue(){
		while(eventQueue.poll()!=null){}
	}
}
