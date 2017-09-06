package com.form2bgames.terminusengine.graphics;

import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_SAMPLES;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_INFO_LOG_LENGTH;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_MAX_TEXTURE_IMAGE_UNITS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;

import com.form2bgames.terminusengine.core.IOUtil;
import com.form2bgames.terminusengine.core.KeyboardManager;

public abstract class GLRenderer{
	protected static final Logger logger=LogManager.getLogger();
	protected static long window=0;
	protected static GLCapabilities glcaps=null;
	protected static String appName;
	protected static final int MAX_GRAPHICS_JOBS_PER_FRAME=0x20;
	protected static boolean doneWithInit=false;
	protected static int width,height;
	protected static float aspectRatio;
	protected static KeyboardManager km=null;
	protected long fps=0,lastFrameTime=0;
	protected static boolean loaded=false;
	protected static SlotInfo[] si;
	
	public GLRenderer(){
	}
	
	public static GLRenderer startRenderer(String appName){
		GLRenderer.appName=appName;
		if(!glfwInit())
			throw new RuntimeException("Failed initializing GLFW");
		
		glfwDefaultWindowHints(); // optional, the current window hints are
									// already the default
		glfwWindowHint(GLFW_VISIBLE,GLFW_FALSE); // the window will stay hidden
													// after creation
		glfwWindowHint(GLFW_RESIZABLE,GLFW_FALSE);
		glfwWindowHint(GLFW_SAMPLES,4); // antialiasing (?)
		
		// window = glfwCreateWindow(width, height, windowTitle,
		// glfwGetPrimaryMonitor(), 0);
		
		GLFWVidMode modes=glfwGetVideoMode(glfwGetPrimaryMonitor());
		
		width=modes.width();
		height=modes.height();
		
		width=640;
		height=480;
		
		aspectRatio=(float)width/(float)height;
		
		long monitor=0;// glfwGetPrimaryMonitor()
		
		window=glfwCreateWindow(width,height,appName,monitor,0);
		
		glfwMakeContextCurrent(window);
		glfwSwapInterval(0);
		
		glcaps=GL.createCapabilities();
		
		logger.info("OpenGL Version:      {}",GL11.glGetString(GL11.GL_VERSION));
		logger.info("OpenGL Renderer:     {}",GL11.glGetString(GL11.GL_RENDERER));
		logger.info("OpenGL Vendor:       {}",GL11.glGetString(GL11.GL_VENDOR));
		
		logger.info("OpenGL TexUnits:     {}",GL11.glGetInteger(GL_MAX_TEXTURE_IMAGE_UNITS));
		
		logger.info("OpenGL 4.5:          {}",glcaps.OpenGL45);
		
		if(!glcaps.OpenGL43)
			throw new RuntimeException("OpenGL 4.3 is required");
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glClearColor(.2f,.2f,.2f,0f);
		
		glEnable(GL_DEPTH_TEST);
		
		si=new SlotInfo[glGetInteger(GL_MAX_TEXTURE_IMAGE_UNITS)];
		for(int i=0;i<si.length;i++){
			si[i]=new SlotInfo();
		}
		
		GL11.glViewport(0,0,width,height);
		
		//FIND GL VERSION AND CREATE INSTANCE AS RENDERER
		
		if(glcaps.OpenGL43){
			logger.info("Using GL43 Renderer");
			INSTANCE=new GL43Renderer();
		}else{
			throw new RuntimeException("GL43 is not present");
		}
		INSTANCE.init();
		KeyboardManager.init(window);
		return INSTANCE;
	}
	
	public abstract void init();
	
	public static SlotInfo getTexAllocation(int texture){
		for(SlotInfo i:si){
			if(i.tex==texture){
				return i;
			}
		}
		for(SlotInfo i:si){
			if(i.inUse==false){
				i.tex=texture;
				return i;
			}
		}
		int lowestAlloc=Integer.MAX_VALUE,slot=-1;
		
		for(SlotInfo i:si){
			if(i.allocated<lowestAlloc)
				slot=i.slot;
		}
		si[slot].tex=texture;
		return si[slot];
	}
	
	public static class SlotInfo{
		public boolean inUse=false;
		public int tex=0,slot=0,allocated=0;
	}
	
	public static float getAspectRatio(){
		// TODO Auto-generated method stub
		return aspectRatio;
	}
	
	public long getFps(){
		return fps;
	}
	
	public long getLastFrameTime(){
		return lastFrameTime;
	}
	
	public static void setWindowShouldClose(boolean b){
		glfwSetWindowShouldClose(window,b);
	}
	
	public static Shader loadShader(String vertexShader,String fragShader){
		int vs=glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vs,vertexShader);
		glCompileShader(vs);
		
		if(glGetShaderi(vs,GL_COMPILE_STATUS)==GL_FALSE){
			int length=glGetShaderi(vs,GL_INFO_LOG_LENGTH);
			System.err.println(glGetShaderInfoLog(vs,length));
			glDeleteShader(vs);
			throw new AssertionError("Vertex shader could not be created.");
		}
		
		int fs=glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fs,fragShader);
		glCompileShader(fs);
		
		if(glGetShaderi(fs,GL_COMPILE_STATUS)==GL_FALSE){
			int length=glGetShaderi(fs,GL_INFO_LOG_LENGTH);
			System.err.println(glGetShaderInfoLog(fs,length));
			glDeleteShader(fs);
			glDeleteShader(vs);
			throw new AssertionError("Fragment shader could not be created.");
		}
		
		int program=glCreateProgram();
		
		glAttachShader(program,vs);
		glAttachShader(program,fs);
		
		glLinkProgram(program);
		
		if(glGetProgrami(program,GL_LINK_STATUS)==GL_FALSE){
			int length=glGetProgrami(program,GL_INFO_LOG_LENGTH);
			System.err.println(glGetProgramInfoLog(program,length));
			glDeleteProgram(program);
			glDeleteShader(fs);
			glDeleteShader(vs);
			throw new AssertionError("Shader could not be created.");
		}
		return new Shader(program);
	}
	
	public static Texture loadTexture(String path){
		ByteBuffer imageBuffer;
		try{
			imageBuffer=IOUtil.ioResourceToByteBuffer(path,8*1024);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		
		return INSTANCE.ngrloadImageFromBuffer(imageBuffer);
	}
	
	public abstract Texture ngrloadImageFromBuffer(ByteBuffer imageBuffer);

	public void finishedLoading(){
		loaded=true;
		while(!doneWithInit){
			try{
				Thread.sleep(0,25000);
			}catch(Exception e){}
		}
		glfwShowWindow(window);
	}
	/**
	 * BEGIN NEEDED GLRENDERER DEFINITIONS (ngrFunc)
	 * */
	public abstract void render();
	public abstract void ngrdoTexture(int uniform,SlotInfo si);
	public abstract int ngrcreateEBO(IntBuffer data);
	public abstract void ngraddVBO(int vao,int vbo,int size,int location);
	public abstract int ngrcreateVBO(FloatBuffer data);
	public abstract int ngrgenVAO();
	
	/**
	 * END NEEDED DEFINITIONS, BEGIN IMPLEMENTATIONS FROM INSTANCE
	 * */
	
	/**
	 * Creates the EBO/IBO that will correctly wind the vertexes
	 * 
	 * @param data
	 *            The Indexes that will correctly wind the faces on the
	 *            currently bound VAO
	 * @return The created EBO/IBO
	 */
	public static int createEBO(IntBuffer data){
		return INSTANCE.ngrcreateEBO(data);
	}
	public static void doTexture(int uniform,SlotInfo si){
		INSTANCE.ngrdoTexture(uniform,si);
	}
	/**
	 * @param vao
	 *            The VAO to add the VBO to
	 * @param vbo
	 *            The VBO that contains data
	 * @param size
	 *            The amount of data in each unit (e.g. 4 vertexes per face)
	 * @param location
	 *            The location in the shader the data will be sent to
	 */
	public static void addVBO(int vao,int vbo,int size,int location){
		INSTANCE.ngraddVBO(vao,vbo,size,location);
	}
	public static int createVBO(FloatBuffer data){
		return INSTANCE.ngrcreateVBO(data);
	}
	public static int genVAO(){
		return INSTANCE.ngrgenVAO();
	}
	
	private static GLRenderer INSTANCE;
}
