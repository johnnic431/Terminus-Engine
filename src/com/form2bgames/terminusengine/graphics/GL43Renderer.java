package com.form2bgames.terminusengine.graphics;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_MAX_TEXTURE_UNITS;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_info_from_memory;
import static org.lwjgl.stb.STBImage.stbi_is_hdr_from_memory;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.CopyOnWriteArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLCapabilities;

import com.form2bgames.terminusengine.core.IOUtil;
import com.form2bgames.terminusengine.core.KeyboardManager;
import com.form2bgames.terminusengine.core.TerminusGame;

public class GL43Renderer extends Thread{
	private static long window=0;
	private GLCapabilities glcaps=null;
	private String appName;
	@SuppressWarnings("unused")
	private com.form2bgames.terminusengine.graphics.GLDebugCallback_43 gldc;
	private Shader shader2D=null;
	private static final String VERTEX_SHADER_2D=""
			+ "#version 430\n"
			+ ""
			+ "layout(location=0) in vec2 vert;\n"
			+ "layout(location=1) in vec2 tex;\n"
			+ "out vec2 vtex;\n"
			+ "out vec4 gl_Position;\n"
			+ "void main(){\n"
			+ "	vec2 pos=vert-vec2(400,300);\n"
			+ "	pos/=vec2(400,300);\n"
			+ "	gl_Position=vec4(pos,0,1);\n"
			+ "	vtex=tex;\n"
			+ "}\n"
			+ "",
	FRAG_SHADER_2D=""
			+ "#version 430\n"
			+ ""
			+ "layout(location=2) uniform sampler2D itex;\n"
			+ ""
			+ "in vec2 vtex;\n"
			+ "out vec4 color;\n"
			+ ""
			+ "void main(){\n"
			+ "	color=texture2D(itex,vtex);\n"
			+ "	if(color.g<.9f||color.b<.9f||color.r<.9f){\n"
			+ "		discard;\n"
			+ "	}\n"
			+ "}\n"
			+ "";
	private static final int MAX_GRAPHICS_JOBS_PER_FRAME=0x20;
	private boolean doneWithInit=false;
	
	public GL43Renderer(String appName){
		this.appName=appName;
	}
	
	public void init(){
		if (!glfwInit())
			throw new RuntimeException("Failed initializing GLFW");

		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
		glfwWindowHint(GLFW_SAMPLES, 4); // antialiasing

		// window = glfwCreateWindow(width, height, windowTitle,
		// glfwGetPrimaryMonitor(), 0);
		
		GLFWVidMode modes = glfwGetVideoMode(glfwGetPrimaryMonitor());
		
		int width=modes.width(),height=modes.height();
		
		width=640;
		height=480;
		
		long monitor=0;//glfwGetPrimaryMonitor()
		
		window = glfwCreateWindow(width,height,appName,monitor, 0);

		glfwMakeContextCurrent(window);
		glfwSwapInterval(0);

		glcaps = GL.createCapabilities();
		
		TerminusGame.logger.info("OpenGL Version:   {}",GL11.glGetString(GL11.GL_VERSION)); 
		TerminusGame.logger.info("OpenGL Renderer:  {}",GL11.glGetString(GL11.GL_RENDERER)); 
		TerminusGame.logger.info("OpenGL Vendor:    {}",GL11.glGetString(GL11.GL_VENDOR));
		
		if(!glcaps.OpenGL43)
			throw new RuntimeException("OpenGL 4.3 is required");

		//GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glClearColor(.2f, .2f, .2f, 0f);

		glEnable(GL_DEPTH_TEST);
		
		GL11.glEnable(GL43.GL_DEBUG_OUTPUT);
		GL11.glEnable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS);
		GL43.glDebugMessageCallback(gldc=new GLDebugCallback_43(),1);
		GL43.glDebugMessageControl(GL11.GL_DONT_CARE, GL11.GL_DONT_CARE, GL11.GL_DONT_CARE, BufferUtils.createIntBuffer(0), true);
		
		shader2D=loadShader(VERTEX_SHADER_2D,FRAG_SHADER_2D);
		
		si = new SlotInfo[glGetInteger(GL_MAX_TEXTURE_UNITS)];
		for (int i = 0; i < si.length; i++) {
			si[i] = new SlotInfo();
		}
		
		GL11.glViewport(0, 0, width, height);
		
		doneWithInit=true;
		
	}

	KeyboardManager km=null;
	
	@Override
	public void run() {
		init();
		KeyboardManager.init(window);
		render();
	}
	
	private void render() {
		
		TerminusGame.logger.info("Entering render");
		
		while(!loaded){
			for(GraphicsThread d:GraphicsProvider.getGraphicsThreads()){
				d.function();
				d.finished();
			}
			try{
				Thread.sleep(10);
			}catch(Exception e){}
		}
		for(GraphicsThread d:GraphicsProvider.getGraphicsThreads()){
			d.function();
			d.finished();
		}
		
		long lastFrameStart=0,oneS=System.nanoTime(),cFrameStart=0,fpts=0;

		TerminusGame.logger.info("Entering render loop");
		
		while(!glfwWindowShouldClose(window)){
			cFrameStart=System.nanoTime();
			lastFrameTime=cFrameStart-lastFrameStart;
			lastFrameStart=cFrameStart;
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
			glfwPollEvents();
			
			int processed=0;
			for(GraphicsThread d:GraphicsProvider.getGraphicsThreads()){
				if(processed==MAX_GRAPHICS_JOBS_PER_FRAME)
					break;
				d.function();
				d.finished();
				++processed;
			}
			
			//RENDER BEGINS HERE
			
			
			CopyOnWriteArrayList<Renderable2D> twoDList=GraphicsProvider.getRenderable2Ds();
			
			glUseProgram(shader2D.program);
			
			for(Renderable2D r:twoDList){
				VAO2D[] toRender=r.getVaos();
				for(VAO2D v:toRender){
					doTexture(2,getTexAllocation(v.tex.textureID));
					glBindVertexArray(v.vao);
					glDrawArrays(GL_TRIANGLES,0,v.vertices);
					glBindVertexArray(0);
				}
			}
			
			
			//RENDER ENDS HERE
			
			glfwSwapBuffers(window);
			
			fpts++;
			if(oneS+1000000000<lastFrameStart){
				fps=fpts;
				fpts=0;
				oneS=lastFrameStart;
			}
			terminusGame.signalRenderDone();
		}
		System.exit(0);
	}
	
	public long getFps() {
		return fps;
	}

	public long getLastFrameTime() {
		return lastFrameTime;
	}

	private long fps=0,lastFrameTime=0;
	private boolean loaded=false;
	private TerminusGame terminusGame;

	public static void setWindowShouldClose(boolean b) {
		glfwSetWindowShouldClose(window,b);
	}
	
	public static Shader loadShader(String vertexShader,String fragShader){
		int vs = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vs, vertexShader);
		glCompileShader(vs);
		
		if(glGetShaderi(vs, GL_COMPILE_STATUS) == GL_FALSE) {
            int length = glGetShaderi(vs, GL_INFO_LOG_LENGTH);
            System.err.println(glGetShaderInfoLog(vs, length));
            glDeleteShader(vs);
            throw new AssertionError("Vertex shader could not be created.");
        }
		
		int fs = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fs, fragShader);
		glCompileShader(fs);
		
		if(glGetShaderi(fs, GL_COMPILE_STATUS) == GL_FALSE) {
            int length = glGetShaderi(fs, GL_INFO_LOG_LENGTH);
            System.err.println(glGetShaderInfoLog(fs, length));
            glDeleteShader(fs);
            glDeleteShader(vs);
            throw new AssertionError("Fragment shader could not be created.");
        }
		
		int program=glCreateProgram();
		
		glAttachShader(program,vs);
		glAttachShader(program,fs);
		
		glLinkProgram(program);

        if(glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            int length = glGetProgrami(program, GL_INFO_LOG_LENGTH);
            System.err.println(glGetProgramInfoLog(program, length));
            glDeleteProgram(program);
            glDeleteShader(fs);
            glDeleteShader(vs);
            throw new AssertionError("Shader could not be created.");
        }
        return new Shader(program);
	}
	public static void texParameteri(int tex,int pname,int param){
		glBindTexture(GL_TEXTURE_2D, tex);
		glTexParameteri(GL_TEXTURE_2D, pname, param);
	}
	public static void storeTex(int tex,int level,int internalFormat,int width,int height,int border,int format,int type,ByteBuffer pixels){
		glBindTexture(GL_TEXTURE_2D, tex);
		glTexImage2D(GL_TEXTURE_2D, level,internalFormat,width,height,border,format,type, pixels);
	}
	public static int genTexture(){
		return glGenTextures();
	}
	public static void bindTex(int tex){
		glBindTexture(GL_TEXTURE_2D,tex);
	}
	public static int genVAO(){
		return glGenVertexArrays();
	}
	public static int createVBO(float[] data) {
		int vbo = glGenBuffers();
		ByteBuffer ftex = BufferUtils.createByteBuffer(data.length * 4);
		ftex.asFloatBuffer().put(data);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
		return vbo;
	}
	public static void addVBO(int vao, int vbo, int size, int location) {
		glBindVertexArray(vao);
		glEnableVertexAttribArray(location);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glVertexAttribPointer(location, size, GL_FLOAT, true, 0, 0);
	}
	public void finishedLoading() {
		loaded=true;
		while(!doneWithInit){
			try{
				Thread.sleep(0,25000);
			}catch(Exception e){}
		}
		glfwShowWindow(window);
	}
	/**
	 * Secures graphics thread internally
	 * 
	 * @param f File to load
	 * @return
	 * @throws IOException
	 */
	public static Texture loadImage(File f) throws IOException{
		if(!f.exists()||f.isDirectory())
			throw new IOException("File is a directory or does not exist.");
		Texture tex=new Texture();
		ByteBuffer imageBuffer,pixelBuffer;
		try {
			imageBuffer = IOUtil.ioResourceToByteBuffer(f.getAbsolutePath(), 8 * 1024);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		IntBuffer w = BufferUtils.createIntBuffer(1);
		IntBuffer h = BufferUtils.createIntBuffer(1);
		IntBuffer comp = BufferUtils.createIntBuffer(1);

		// Use info to read image metadata without decoding the entire image.
		// We don't need this for this demo, just testing the API.
		if ( !stbi_info_from_memory(imageBuffer, w, h, comp) )
			throw new RuntimeException("Failed to read image information: " + stbi_failure_reason());

		// Decode the image
		pixelBuffer = stbi_load_from_memory(imageBuffer, w, h, comp, 4);
		if ( pixelBuffer == null )
			throw new RuntimeException("Failed to load image: " + stbi_failure_reason());
		
		TerminusGame.logger.info("Image width: {}" , w.get(0));
        TerminusGame.logger.info("Image height: {}" , h.get(0));
        TerminusGame.logger.info("Image components: {}" , comp.get(0));
        TerminusGame.logger.info("Image HDR: {}" , stbi_is_hdr_from_memory(imageBuffer));

		tex.width = w.get(0);
		tex.height = h.get(0);
		
		GraphicsThread d=new GraphicsThread(){

			@Override
			public void function() {
				tex.textureID=glGenTextures();
				glBindTexture(GL_TEXTURE_2D, tex.textureID);
	            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w.get(0), h.get(0), 0, GL_RGBA, GL_UNSIGNED_BYTE, pixelBuffer);

				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			}

			
		};
		GraphicsProvider.addNeedsGraphicsThread(d);
		
		TerminusGame.logger.info("Added GraphicsThread!");

		d.waitForCompletion();
		
		TerminusGame.logger.info("Image loaded successfully!");
		
		return tex;
	}
	private static SlotInfo[] si;

	public static SlotInfo getTexAllocation(int texture) {
		for (SlotInfo i : si) {
			if (i.tex == texture) {
				return i;
			}
		}
		for (SlotInfo i : si) {
			if (i.inUse == false) {
				i.tex = texture;
				return i;
			}
		}
		int lowestAlloc = Integer.MAX_VALUE, slot = -1;
		
		for (SlotInfo i : si) {
			if (i.allocated < lowestAlloc)
				slot = i.slot;
		}
		si[slot].tex = texture;
		return si[slot];
	}

	public static void doTexture(int uniform, SlotInfo si) {
		glActiveTexture(GL_TEXTURE0 + si.slot);
		glBindTexture(GL_TEXTURE_2D, si.tex);
		glUniform1i(uniform, si.slot);
	}
	
	public static class SlotInfo {
		public boolean inUse = false;
		public int tex = 0, slot = 0, allocated = 0;
	}

	public void setGame(TerminusGame terminusGame) {
		this.terminusGame=terminusGame;
	}
}
