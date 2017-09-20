package com.form2bgames.terminusengine.graphics;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL45.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.CopyOnWriteArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL45;

import com.form2bgames.terminusengine.events.EngineShutdownEvent;
import com.form2bgames.terminusengine.events.EventManager;
import com.form2bgames.terminusengine.graphics.debugcallbacks.GLDebugCallback_43;

public class GL45Renderer extends GLRenderer{
	@SuppressWarnings("unused")
	private com.form2bgames.terminusengine.graphics.debugcallbacks.GLDebugCallback_43 gldc;
	private static final String VERTEX_SHADER_2D=""+"#version 430\n"+""+"layout(location=0) in vec2 vert;\n"
			+"layout(location=1) in vec2 tex;\n"+"out vec2 vtex;\n"+"out vec4 gl_Position;\n"+"void main(){\n"
			+"	vec2 pos=vert-vec2(400,300);\n"+"	pos/=vec2(400,300);\n"+"	gl_Position=vec4(pos,0,1);\n"
			+"	vtex=tex;\n"+"}\n"+"",
			FRAG_SHADER_2D=""+"#version 430\n"+""+"layout(location=2) uniform sampler2D itex;\n"+""+"in vec2 vtex;\n"
					+"out vec4 color;\n"+""+"void main(){\n"+"	color=texture2D(itex,vtex);\n"
					+"	if(color.g<.9f||color.b<.9f||color.r<.9f){\n"+"		discard;\n"+"	}\n"
					+"	color=vec4(.5f,.5f,.5f,1f);\n"+"}\n"+"",
			VERTEX_SHADER_3D=""+"#version 430\n"+""+"layout(location=0) in vec4 position;\n"
					+"layout(location=1) in vec3 normal;\n"+"layout(location=2) in vec2 texCoords;\n"+""
					+"layout(location=3) uniform mat4 VPMatrix;\n"+"layout(location=4) uniform mat4 modelMatrix;\n"+""
					+"out vec2 fTexCoords;\n"+"out vec3 fNormal;\n"+""+"void main(){\n"+"	fTexCoords=texCoords;\n"
					+"	fNormal=normal;\n"+"	gl_Position = VPMatrix * modelMatrix * position;\n"+"}\n"+"",
			FRAG_SHADER_3D=""+"#version 430\n"+""+"in vec2 fTexCoords;\n"+"in vec3 fNormal;\n"+""
					+"layout(location=128) uniform sampler2D texture;\n"
					+"layout(location=129) uniform vec3 kSpecular;\n"+"layout(location=130) uniform vec3 kAmbient;\n"
					+"layout(location=131) uniform vec3 kDiffuse;\n"+"layout(location=132) uniform float shiny;\n"+""
					+"out vec4 color;\n"+""+"void main(){\n"+"	color=texture2D(texture,fTexCoords);\n"+"}\n"+"",
			VERTEX_SHADER_POSTPROCESS=""+"#version 430\n"+""+"layout(location=0) in vec2 position;\n"
					+"layout(location=1) in vec2 texCoords;\n"+"out vec2 textureCoords;\n"+""+"void main(){\n"
					+"	gl_Position=vec4(position,0,1);\n"+"	textureCoords=texCoords;\n"+"}\n"+"",
			FRAG_SHADER_POSTPROCESS=""+"#version 430\n"+""+"in vec2 textureCoords;\n"+""
					+"layout(location=128) uniform sampler2D texture;\n"+""+"out vec4 color;"+""+"void main(){\n"
					+"	color=texture2D(texture,textureCoords);\n"+"}\n";
	public GL45Renderer(){}
	
	@Override
	public void init(){
		GL11.glEnable(GL43.GL_DEBUG_OUTPUT);
		GL11.glEnable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS);
		
		GL43.glDebugMessageCallback(gldc=new GLDebugCallback_43(),1);
		GL43.glDebugMessageControl(GL11.GL_DONT_CARE,GL11.GL_DONT_CARE,GL11.GL_DONT_CARE,BufferUtils.createIntBuffer(0),
				true);
		
		long sLoad=System.nanoTime();
		
		shader2D=loadShader(VERTEX_SHADER_2D,FRAG_SHADER_2D);
		shader3D=loadShader(VERTEX_SHADER_3D,FRAG_SHADER_3D);
		shaderPostprocess=loadShader(VERTEX_SHADER_POSTPROCESS,FRAG_SHADER_POSTPROCESS);
		
		logger.info("Load time of shaders was {} nanos ({} sec)",System.nanoTime()-sLoad,
				((float)(System.nanoTime()-sLoad))/1e9f);
		
		framebufferTexture = genTexture();
		glTextureStorage2D(framebufferTexture, 1, GL_RGBA32F, width,height);
		framebuffer = GL45.glCreateFramebuffers();
		framebufferDepth = GL45.glCreateRenderbuffers();
		IntBuffer renderBuffers = BufferUtils.createIntBuffer(1).put(GL30.GL_COLOR_ATTACHMENT0);
		renderBuffers.flip();
		GL45.glNamedFramebufferDrawBuffers(framebuffer, renderBuffers);
		GL45.glNamedRenderbufferStorage(framebufferDepth, GL_DEPTH_COMPONENT, width, height);
		GL45.glNamedFramebufferTexture(framebuffer, GL30.GL_COLOR_ATTACHMENT0, framebufferTexture, 0);
		GL45.glNamedFramebufferRenderbuffer(framebuffer, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, framebufferDepth);
		int fboStatus = GL45.glCheckNamedFramebufferStatus(framebuffer, GL30.GL_FRAMEBUFFER);
		if (fboStatus != GL30.GL_FRAMEBUFFER_COMPLETE) {
			throw new AssertionError("Could not create FBO: " + fboStatus);
		}
	}
	
	@Override
	public void render(){
		logger.info("Entering render");
		
		int svao=genVAO();
		addVBO(svao,createVBO(fb),2,0);
		addVBO(svao,createVBO(tx),2,1);
		
		Texture.setNoTex(loadTexture("com/form2bgames/terminusengine/graphics/4x4.jpg"));
		
		GraphicsThread d=null;
		while(!loaded){
			while((d=GraphicsProvider.getNextGraphicsThread())!=null){
				d.function();
				d.finished();
			}
			try{
				Thread.sleep(10);
			}catch(Exception e){}
		}
		while((d=GraphicsProvider.getNextGraphicsThread())!=null){
			d.function();
			d.finished();
		}
		
		FloatBuffer sixteen=BufferUtils.createFloatBuffer(16);
		
		long lastFrameStart=System.nanoTime(),oneS=System.nanoTime(),cFrameStart=System.nanoTime(),fpts=0;
		
		logger.info("Entering render loop");
		while(!glfwWindowShouldClose(window)){
			
			glBindFramebuffer(GL_DRAW_FRAMEBUFFER,framebuffer);
			EventManager.postEvent(new RenderBeginEvent());
			cFrameStart=System.nanoTime();
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
			glfwPollEvents();
			
			int processed=0;
			GraphicsThread td=null;
			while((td=GraphicsProvider.getNextGraphicsThread())!=null&&!(processed==MAX_GRAPHICS_JOBS_PER_FRAME)){
				td.function();
				td.finished();
				++processed;
			}
			
			// RENDER BEGINS HERE
			
			glEnable(GL_DEPTH_TEST);
			
			CopyOnWriteArrayList<Renderable3D> threeDList=GraphicsProvider.getRenderable3Ds();
			
			glUseProgram(shader3D.program);
			
			for(Renderable3D r:threeDList){
				for(VAO3D vao:r.getVaos()){
					Material m=vao.material;
					if(m==null){
						doTexture(128,getTexAllocation(Texture.getNoTex().textureID));
					}else{
						doTexture(128,getTexAllocation(m.texture.textureID));
						GL20.glUniform1f(132,m.brightness);
						GL20.glUniform3f(129,m.kSpecular.x,m.kSpecular.x,m.kSpecular.x);
						GL20.glUniform3f(130,m.kAmbient.x,m.kAmbient.x,m.kAmbient.x);
						GL20.glUniform3f(131,m.kDiffuse.x,m.kDiffuse.x,m.kDiffuse.x);
					}
					GL20.glUniformMatrix4fv(3,false,r.camera.getViewProjMatrix());
					GL20.glUniformMatrix4fv(4,false,r.getModelViewMat().get(sixteen));
					glBindVertexArray(vao.vao);
					glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,r.getIBO());
					glDrawElements(GL_TRIANGLES,vao.vertices,GL_UNSIGNED_INT,0);
				}
			}
			
			CopyOnWriteArrayList<Renderable2D> twoDList=GraphicsProvider.getRenderable2Ds();
			
			glDisable(GL_DEPTH_TEST);
			
			glUseProgram(shader2D.program);
			
			for(Renderable2D r:twoDList){
				for(VAO2D v:r.getVaos()){
					doTexture(2,getTexAllocation(v.tex.textureID));
					glBindVertexArray(v.vao);
					glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,v.ibo);
					glDrawElements(GL_TRIANGLES,v.vertices,GL_UNSIGNED_INT,0);
				}
			}
			glBindVertexArray(0);
			
			// RENDER ENDS HERE
			
			glFlush();
			
			// Postprocess and draw to screen //eventully i guess
			
			glBindFramebuffer(GL_DRAW_FRAMEBUFFER,0);
			glUseProgram(shaderPostprocess.program);
			doTexture(128,getTexAllocation(framebufferTexture));
			glBindVertexArray(svao);
			glDrawArrays(GL_TRIANGLES,0,6);
			
			glfwSwapBuffers(window);
			
			fpts++;
			if(oneS+1000000000<lastFrameStart){
				fps=fpts;
				fpts=0;
				oneS=lastFrameStart;
			}
			lastFrameTime=cFrameStart-lastFrameStart;
			lastFrameStart=cFrameStart;
			EventManager.postEvent(new RenderDoneEvent(lastFrameTime));
		}
		EventManager.postEvent(new EngineShutdownEvent());
	}
	
	@Override
	public void ngrdoTexture(int uniform,SlotInfo si){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Texture ngrloadImageFromBuffer(ByteBuffer imageBuffer){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int ngrcreateEBO(IntBuffer data){
		int vbo=glCreateBuffers();
		glNamedBufferData(vbo, data.capacity(), GL_STATIC_DRAW);
		return vbo;
	}
	
	@Override
	public void ngraddVBO(int vao,int vbo,int size,int location){
		glVertexArrayAttribBinding(vao, location, location);
		glVertexArrayVertexBuffer(vao, location, vbo, 0, size);
		glEnableVertexArrayAttrib(vao, location);
		glVertexArrayAttribFormat(vao, location, size, GL_FLOAT, false, 0);
	}
	
	@Override
	public int ngrcreateVBO(FloatBuffer data){
		int vbo=glCreateBuffers();
		glNamedBufferData(vbo, data.capacity(), GL_STATIC_DRAW);
		return vbo;
	}
	
	@Override
	public int ngrgenVAO(){
		// TODO Auto-generated method stub
		return glCreateVertexArrays();
	}

	@Override
	public int ngrgenTexture(){
		return GL45.glCreateTextures(GL_TEXTURE_2D);
	}

	@Override
	public void ngrsaveScreenshot(String path) throws IOException{
		// TODO Auto-generated method stub
		
	}
	
}
