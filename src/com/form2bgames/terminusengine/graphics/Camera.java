package com.form2bgames.terminusengine.graphics;

import java.nio.FloatBuffer;

import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import com.form2bgames.terminusengine.events.BasicEventHandler;
import com.form2bgames.terminusengine.events.EventManager;

public class Camera{
	public Vector3f pos=new Vector3f(0f, 0f, 0f),rot=new Vector3f(0f, 0f, 0f);
	private Matrix4f projMatrix=new Matrix4f(),viewMatrix=new Matrix4f(),viewProjMatrix=new Matrix4f();
	private FrustumIntersection frustum=new FrustumIntersection();
	private FloatBuffer viewBuffer=BufferUtils.createFloatBuffer(16),projBuffer=BufferUtils.createFloatBuffer(16),
			vpBuffer=BufferUtils.createFloatBuffer(16);
	private Thread cThread;
	private BasicEventHandler handler;
	private static int cameraCount=0;
	private int cameraNumber;
	public static final Camera NO_CAMERA=new Camera(null);
	
	/**
	 * Creates a new camera.
	 * 
	 * @param fov
	 *            The Field of View (in radians) to use
	 */
	public Camera(float fov){
		cThread=new Thread(new Runnable(){
			@Override
			public void run(){
				handler=new BasicEventHandler();
				EventManager.subscribe(RenderDoneEvent.RENDER_DONE_EVENT,handler);
				handler.waitForEvent();
				handler.clearQueue();
				projMatrix.identity().setPerspective(fov,GLRenderer.getAspectRatio(),.1f,100f);
				projMatrix.get(projBuffer);
				viewMatrix.identity().rotateX((float)org.joml.Math.toRadians(-rot.x))
						.rotateY((float)org.joml.Math.toRadians(-rot.y))
						.rotateZ((float)org.joml.Math.toRadians(-rot.z))
						.translate(pos);
				viewProjMatrix.set(projMatrix).mul(viewMatrix);
				frustum.set(viewProjMatrix);
				viewMatrix.get(viewBuffer);
				viewProjMatrix.get(vpBuffer);
				
				while(true){
					handler.waitForEvent();
					handler.clearQueue();
					viewMatrix.identity().rotateX((float)org.joml.Math.toRadians(-rot.x))
							.rotateY((float)org.joml.Math.toRadians(-rot.y))
							.rotateZ((float)org.joml.Math.toRadians(-rot.z))
							.translate(pos);
					viewProjMatrix.set(projMatrix).mul(viewMatrix);
					frustum.set(viewProjMatrix);
					viewMatrix.get(viewBuffer);
					viewProjMatrix.get(vpBuffer);
				}
			}
		});
		this.cameraNumber=cameraCount++;
		cThread.setName("Camera "+cameraNumber+" Thread");
		cThread.start();
	}
	
	public void changeFOV(float radians){
		projMatrix.identity().setPerspective(radians,GLRenderer.getAspectRatio(),.1f,100f);
		projMatrix.get(projBuffer);
	}
	
	public FloatBuffer getViewMatrix(){
		return viewBuffer;
	}
	
	public FloatBuffer getProjMatrix(){
		return projBuffer;
	}
	
	public FloatBuffer getViewProjMatrix(){
		return vpBuffer;
	}
	
	public FrustumIntersection getFrustum(){
		return frustum;
	}
	
	private Camera(Object object){}
}
