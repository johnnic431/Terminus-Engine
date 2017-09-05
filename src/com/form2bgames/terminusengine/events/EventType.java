package com.form2bgames.terminusengine.events;

public class EventType{
	private Object key;
	
	public EventType(Object key){
		this.key=key;
	}
	
	protected Object getObject(){
		return key;
	}
	
	@Override
	public boolean equals(Object anotherEventType){
		return this.key==((EventType)anotherEventType).key;
	}
	
	@Override
	public int hashCode(){
		return key.hashCode();
	}
}
