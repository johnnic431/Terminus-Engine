package com.form2bgames.terminusengine.events;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.form2bgames.terminusengine.core.EngineInfo;
import com.form2bgames.terminusengine.core.EngineInfo.BuildType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EventManager{
	private static final Logger l=LogManager.getLogger();
	private static ConcurrentMap<EventType,List<BasicEventHandler>> handlers=new ConcurrentHashMap<>();
	private static ConcurrentLinkedQueue<Event> eventQueue=new ConcurrentLinkedQueue<>();
	private static Thread dispatchThread;
	static{
		dispatchThread=new Thread(EventDispatchThread.getInstance());
		dispatchThread.setName("Event Dispatch Thread");
		dispatchThread.start();
	}
	
	public static void subscribe(EventType event,BasicEventHandler handler){
		l.trace("Adding handler for event {}",event.getObject());
		List<BasicEventHandler> hList=handlers.get(event);
		if(hList==null){
			hList=new ArrayList<BasicEventHandler>();
			handlers.put(event,hList);
		}
		hList.add(handler);
		
	}
	
	public static void unsubscribeAll(EventType event){
		List<BasicEventHandler> items=handlers.get(event);
		Iterator<BasicEventHandler> iterator=items.iterator();
		while(iterator.hasNext()){
			BasicEventHandler handler=iterator.next();
			items.remove(handler);
		}
	}
	
	public static void postEvent(Event event){
		eventQueue.add(event);
		dispatchThread.interrupt();
	}
	
	/**
	 * Tells the dispatch thread whether or not to warn when an event is fired
	 * and has no listeners to handle it. Defaults to off in a stable build, on
	 * in a non-stable build
	 * 
	 * @param warn
	 *            Whether or not to warn
	 */
	public static void setWarnOnNoHandlers(boolean warn){
		warnOnNoHandlers=warn;
	}
	
	private static boolean warnOnNoHandlers=EngineInfo.bt==BuildType.STABLE;
	
	private static final class EventDispatchThread implements Runnable{
		private static final EventDispatchThread INSTANCE=new EventDispatchThread();
		
		private EventDispatchThread(){};
		
		public static EventDispatchThread getInstance(){
			return INSTANCE;
		}
		
		@Override
		public void run(){
			while(true){
				while(true){
					try{
						Thread.sleep(1000);
					}catch(Exception e){
						break;
					}
				}
				Event event=null;
				while((event=eventQueue.poll())!=null){
					List<BasicEventHandler> hList=handlers.get(event.getEventType());
					if(hList==null||hList.isEmpty()){
						if(warnOnNoHandlers)
							l.warn("Event {} was fired, but has no handlers!");
						return;
					}
					Iterator<BasicEventHandler> iterator=hList.iterator();
					while(iterator.hasNext()){
						iterator.next().handle(event);
					}
				}
			}
		}
	}
}
