package com.form2bgames.terminusengine.graphics;

public class Texture{
	private static Texture NO_TEXTURE;
	
	public static Texture getNoTex(){
		return NO_TEXTURE;
	}
	
	protected static void setNoTex(Texture noTex){
		Texture.NO_TEXTURE=noTex;
	}
	
	public int textureID,width,height;
	
	public Texture(int textureID,int width,int height){
		this.textureID=textureID;
		this.width=width;
		this.height=height;
	}
}
