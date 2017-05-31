package com.form2bgames.terminusengine.graphics;

public abstract class GraphicsThread {
	private Object toReturn=null;
	private boolean finished;
	public abstract void function();
	protected final void setReturn(Object toReturn){
		this.toReturn=toReturn;
	}
	public void finished(){
		GraphicsProvider.removeGraphicsThread(this);
		this.finished=true;
	}
	public Object waitForCompletion(){
		while(!finished){
			try{
				Thread.sleep(0,50000);
			}catch(Exception e){}
		}
		return toReturn;
	}
}
