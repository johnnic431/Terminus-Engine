package com.form2bgames.terminusengine.graphics;

import com.form2bgames.terminusengine.events.Event;
import com.form2bgames.terminusengine.events.EventType;

public final class RenderDoneEvent extends Event{
	private long lastFrameTime;
	public static final EventType RENDER_DONE_EVENT=new EventType("OnRenderFinished");
	
	public RenderDoneEvent(long lastFrameTime){
		this.lastFrameTime=lastFrameTime;
	}
	
	@Override
	public EventType getEventType(){
		return RenderDoneEvent.RENDER_DONE_EVENT;
	}
	
	public long getLastFrameTime(){
		return lastFrameTime;
	}
}
