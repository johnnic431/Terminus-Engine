package com.form2bgames.terminusengine.graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

public class Renderable3D implements AutoCloseable{
	protected Vector3f position=new Vector3f(0),rotation=new Vector3f(0); // Rotation
																			// is
																			// in
																			// radians
	protected VAO3D vaos[];
	protected Camera camera=Camera.NO_CAMERA;
	protected int ibo;
	
	public VAO3D[] getVaos(){
		return vaos;
	}
	
	public int getIBO(){
		return ibo;
	}
	
	public Camera getCamera(){
		return this.camera;
	}
	
	public void setCamera(Camera c){
		this.camera=c;
	}
	
	public Vector3f getPosition(){
		return position;
	}
	
	public void setPosition(Vector3f position){
		this.position=position;
	}
	
	public Vector3f getRotation(){
		return rotation;
	}
	
	public void setRotation(Vector3f rotation){
		this.rotation=rotation;
	}
	
	public Matrix4f getModelViewMat(){
		return new Matrix4f().identity().rotateX((float)org.joml.Math.toRadians(-rotation.x))
				.rotateY((float)org.joml.Math.toRadians(-rotation.y))
				.rotateZ((float)org.joml.Math.toRadians(-rotation.z)).translate(position);
	}
	
	@Override
	public void close() throws Exception{
		for(VAO3D v3d:vaos){
			GL30.glDeleteVertexArrays(v3d.vao);
			for(int vbo:v3d.vbos){
				GL15.glDeleteBuffers(vbo);
			}
		}
	}
}
