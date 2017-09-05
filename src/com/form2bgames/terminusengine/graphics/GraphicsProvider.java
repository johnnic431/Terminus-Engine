package com.form2bgames.terminusengine.graphics;

import java.util.concurrent.CopyOnWriteArrayList;

public class GraphicsProvider{
	private static CopyOnWriteArrayList<GraphicsThread> gThreads=new CopyOnWriteArrayList<GraphicsThread>();
	private static CopyOnWriteArrayList<Renderable2D> r2d=new CopyOnWriteArrayList<Renderable2D>();
	private static CopyOnWriteArrayList<Renderable3D> r3d=new CopyOnWriteArrayList<Renderable3D>();
	
	public static void addNeedsGraphicsThread(GraphicsThread d){
		gThreads.add(d);
	}
	
	public static CopyOnWriteArrayList<GraphicsThread> getGraphicsThreads(){
		return gThreads;
	}
	
	public static void removeGraphicsThread(GraphicsThread graphicsThread){
		gThreads.remove(graphicsThread);
	}
	
	public static CopyOnWriteArrayList<Renderable2D> getRenderable2Ds(){
		return r2d;
	}
	
	public static void addRenderable2D(Renderable2D add){
		r2d.add(add);
	}
	
	public static void removeRenderable2D(Renderable2D add){
		r2d.remove(add);
	}
	
	public static void addRenderable3D(Renderable3D add){
		r3d.add(add);
	}
	
	public static void removeRenderable3D(Renderable3D add){
		r3d.remove(add);
	}
	
	public static CopyOnWriteArrayList<Renderable3D> getRenderable3Ds(){
		return r3d;
	}
	
}
