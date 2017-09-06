package com.form2bgames.terminusengine.graphics;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

public class FontRenderer{
	private Texture font;
	
	public FontRenderer(String imageLoc){
		GraphicsThread gt=new GraphicsThread(){
			@Override
			public void function(){
				try{
					font=GL43Renderer.loadImage(new File(imageLoc));
				}catch(IOException e){
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		};
		GraphicsProvider.addNeedsGraphicsThread(gt);
		gt.waitForCompletion();
	}
	
	/**
	 * @param str
	 *            String to draw
	 * @param sx
	 *            Coordinate from 0-799 to put x on
	 * @param sy
	 *            Coordinate from 0-599 to put y on
	 * @param tSize
	 *            Character size in pixels for 800x600 screen
	 * @return
	 */
	public Renderable2D getString(String str,int sx,int sy,int tSize){
		VAO2D toReturn=new VAO2D();
		toReturn.tex=font;
		
		float os=(float)1/(float)16;
		
		FloatBuffer vt=BufferUtils.createFloatBuffer(str.length()*6*2),
				tx=BufferUtils.createFloatBuffer(str.length()*6*2);
		for(char c:str.toCharArray()){
			float px=((float)((int)c)%16)/16;
			float py=((float)(((int)c)/16))/16;
			vt.put(sx);
			vt.put(sy);
			vt.put(sx);
			vt.put(sy+tSize);
			vt.put(sx+tSize);
			vt.put(sy);
			vt.put(sx);
			vt.put(sy+tSize);
			vt.put(sx+tSize);
			vt.put(sy);
			vt.put(sx+tSize);
			vt.put(sy+tSize);
			tx.put(px);
			tx.put(py+os);
			tx.put(px);
			tx.put(py);
			tx.put(px+os);
			tx.put(py+os);
			tx.put(px);
			tx.put(py);
			tx.put(px+os);
			tx.put(py+os);
			tx.put(px+os);
			tx.put(py);
			sx+=tSize;
		}
		
		vt.flip();
		tx.flip();
		
		GraphicsThread d=new GraphicsThread(){
			@Override
			public void function(){
				toReturn.vao=GLRenderer.genVAO();
				toReturn.vertices=vt.capacity()/2;
				
				int pvbo=GLRenderer.createVBO(vt);
				int tvbo=GLRenderer.createVBO(tx);
				
				toReturn.vbos=new int[]{pvbo,tvbo};
				
				GLRenderer.addVBO(toReturn.vao,pvbo,2,0);
				GLRenderer.addVBO(toReturn.vao,tvbo,2,1);
			}
		};
		
		GraphicsProvider.addNeedsGraphicsThread(d);
		
		d.waitForCompletion();
		
		Renderable2D r2d=new Renderable2D(new VAO2D[]{toReturn});
		
		return r2d;
	}
}
