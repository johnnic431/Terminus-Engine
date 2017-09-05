package com.form2bgames.terminusengine.events;

public final class EngineShutdownEvent extends Event{
	public static final EventType ENGINE_SHUTDOWN_EVENT=new EventType("EngineShutdown");
	
	public EngineShutdownEvent(){}
	
	@Override
	public EventType getEventType(){
		return ENGINE_SHUTDOWN_EVENT;
	}
}