package com.form2bgames.terminusengine.graphics;

import java.io.File;
import java.io.IOException;

public class FontRenderer {
	private Texture font;
	public FontRenderer(String imageLoc){
		try {
			font=GL43Renderer.loadImage(new File(imageLoc));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * @param str String to draw
	 * @param sx Coordinate from 0-799 to put x on
	 * @param sy Coordinate from 0-599 to put y on
	 * @param tSize Character size in pixels for 800x600 screen
	 * @return
	 */
	public Renderable2D getString(String str,int sx,int sy,int tSize){
		VAO2D toReturn=new VAO2D();
		toReturn.tex=font;
		
		float os=(float)1/(float)16;
		float[] verts=new float[str.length()*6*2],tex=new float[str.length()*6*2];//string length times six verts per char times 2 values per vert
		int cpos=0;
		for(char c:str.toCharArray()){
			float px=((float)((int)c)%16)/16;
			float py=((float)(((int)c)/16))/16;
			verts[(cpos*12)+0]=sx;
			verts[(cpos*12)+1]=sy;
			verts[(cpos*12)+2]=sx;
			verts[(cpos*12)+3]=sy+tSize;
			verts[(cpos*12)+4]=sx+tSize;
			verts[(cpos*12)+5]=sy;
			verts[(cpos*12)+6]=sx;
			verts[(cpos*12)+7]=sy+tSize;
			verts[(cpos*12)+8]=sx+tSize;
			verts[(cpos*12)+9]=sy;
			verts[(cpos*12)+10]=sx+tSize;
			verts[(cpos*12)+11]=sy+tSize;
			tex[(cpos*12)+0]=px;
			tex[(cpos*12)+1]=py+os;
			tex[(cpos*12)+2]=px;
			tex[(cpos*12)+3]=py;
			tex[(cpos*12)+4]=px+os;
			tex[(cpos*12)+5]=py+os;
			tex[(cpos*12)+6]=px;
			tex[(cpos*12)+7]=py;
			tex[(cpos*12)+8]=px+os;
			tex[(cpos*12)+9]=py+os;
			tex[(cpos*12)+10]=px+os;
			tex[(cpos*12)+11]=py;
			++cpos;
			sx+=tSize;
		}
		GraphicsThread d=new GraphicsThread(){
			@Override
			public void function() {
				toReturn.vao=GL43Renderer.genVAO();
				toReturn.vertices=verts.length/2;
				
				int pvbo=GL43Renderer.createVBO(verts);
				int tvbo=GL43Renderer.createVBO(tex);
				
				toReturn.vbos=new int[]{pvbo,tvbo};
				
				GL43Renderer.addVBO(toReturn.vao, pvbo, 2, 0);
				GL43Renderer.addVBO(toReturn.vao, tvbo, 2, 1);	
			}
		};
		
		GraphicsProvider.addNeedsGraphicsThread(d);
		
		d.waitForCompletion();
		
		Renderable2D r2d=new Renderable2D(new VAO2D[] {toReturn});
		
		return r2d;
	}
}
