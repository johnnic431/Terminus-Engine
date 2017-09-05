package com.form2bgames.terminusengine.graphics;

import com.form2bgames.terminusengine.events.Event;
import com.form2bgames.terminusengine.events.EventType;

public final class RenderBeginEvent extends Event{
	public static final EventType RENDER_BEGIN_EVENT=new EventType("OnRenderBegin");
	
	public RenderBeginEvent(){}
	
	@Override
	public EventType getEventType(){
		// TODO Auto-generated method stub
		return RENDER_BEGIN_EVENT;
	}
	
}
