package com.form2bgames.terminusengine.graphics;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;

public class FontRenderer{
	private Texture font;
	
	public FontRenderer(String imageLoc){
		GraphicsThread gt=new GraphicsThread(){
			@Override
			public void function(){
				font=GLRenderer.loadTexture(imageLoc);
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
		
		FloatBuffer vt=BufferUtils.createFloatBuffer(str.length()*4*2),
				tx=BufferUtils.createFloatBuffer(str.length()*4*2);
		//string length times four verts per char times 2 values per vert
		IntBuffer ibo=BufferUtils.createIntBuffer(str.length()*6);
		//four verts make up 2 triangles with 6 verts
		int place=0;
		for(char c:str.toCharArray()){
			float px=((float)((int)c)%16)/16;
			float py=((float)(((int)c)/16))/16;
			vt.put(sx);//1
			vt.put(sy);
			vt.put(sx);//2
			vt.put(sy+tSize);
			vt.put(sx+tSize);//3
			vt.put(sy);
			vt.put(sx+tSize);//6
			vt.put(sy+tSize);
			tx.put(px);//1
			tx.put(py+os);
			tx.put(px);//2
			tx.put(py);
			tx.put(px+os);//3
			tx.put(py+os);
			tx.put(px+os);//6
			tx.put(py);
			ibo.put((place*4)+0);
			ibo.put((place*4)+1);
			ibo.put((place*4)+2);
			ibo.put((place*4)+1);
			ibo.put((place*4)+2);
			ibo.put((place*4)+3);
			sx+=tSize;
			++place;
		}
		
		vt.flip();
		tx.flip();
		ibo.flip();
		
		GraphicsThread d=new GraphicsThread(){
			@Override
			public void function(){
				toReturn.vao=GLRenderer.genVAO();
				toReturn.vertices=ibo.capacity();
				
				int pvbo=GLRenderer.createVBO(vt);
				int tvbo=GLRenderer.createVBO(tx);
				int ebo=GLRenderer.createEBO(ibo);
				
				toReturn.vbos=new int[]{pvbo,tvbo,ebo};
				toReturn.ibo=ebo;
				
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
