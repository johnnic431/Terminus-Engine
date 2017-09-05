package com.form2bgames.terminusengine.graphics;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

public class Renderable2D implements AutoCloseable{
	private VAO2D[] vaos;
	public Shader s;
	
	public Renderable2D(VAO2D[] vaos){
		this.vaos=vaos;
	}
	
	public VAO2D[] getVaos(){
		return vaos;
	}
	
	@Override
	public void close() throws Exception{
		GraphicsProvider.addNeedsGraphicsThread(new GraphicsThread(){
			@Override
			public void function(){
				for(VAO2D D:getVaos()){
					GL30.glDeleteVertexArrays(D.vao);
					for(int vbo:D.vbos){
						GL15.glDeleteBuffers(vbo);
					}
				}
			}
		});
	}
}
