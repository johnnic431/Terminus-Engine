package com.form2bgames.terminusengine.model;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMaterialProperty;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AITexture;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.Assimp;

import static org.lwjgl.opengl.GL30.*;

import com.form2bgames.terminusengine.graphics.GL43Renderer;
import com.form2bgames.terminusengine.graphics.GraphicsProvider;
import com.form2bgames.terminusengine.graphics.GraphicsThread;
import com.form2bgames.terminusengine.graphics.Renderable3D;
import com.form2bgames.terminusengine.graphics.VAO3D;

public class AssimpModelWrapper extends Renderable3D{
	private static final Logger l=LogManager.getLogger();
	
	public AssimpModelWrapper(File assimpFile){
		AIScene modelScene=Assimp.aiImportFile(assimpFile.getAbsolutePath(),
				Assimp.aiProcessPreset_TargetRealtime_MaxQuality);
		if(modelScene==null)
			throw new NullPointerException("The loaded file did not contain a transformable mesh");
		int numMeshes=modelScene.mNumMeshes();
		PointerBuffer aiMeshes=modelScene.mMeshes();
		AIMesh[] meshes=new AIMesh[numMeshes];
		for(int i=0;i<numMeshes;i++){
			meshes[i]=AIMesh.create(aiMeshes.get(i));
		}
		ArrayList<VAO3D> meshList=new ArrayList<>();
		int numMaterials=modelScene.mNumMaterials();
		PointerBuffer mats=modelScene.mMaterials();
		AIMaterial[] materials=new AIMaterial[numMaterials];
		for(int i=0;i<numMaterials;++i){
			materials[i]=AIMaterial.create(mats.get(i));
		}
		ArrayList<AITexture> texList=new ArrayList<>();
		PointerBuffer textures=modelScene.mTextures();
		if(textures!=null){
			l.trace("Texture {} has {} textures",assimpFile.getAbsoluteFile(),textures.capacity());
			for(int i=0;i<textures.capacity();i++){
				texList.add(AITexture.create(textures.get(i)));
			}
		}else{
			l.warn("Model {} has no textures!",assimpFile.getAbsolutePath());
		}
		l.info("model has {} meshes, {} meshes found",modelScene.mNumMeshes(),meshes.length);
		for(int i=0;i<meshes.length;i++){
			l.info("mesh {}",i);
			meshList.add(initMesh(meshes[i],materials,texList));
		}
		vaos=meshList.toArray(new VAO3D[]{});
		
	}
	
	private VAO3D initMesh(AIMesh mesh,AIMaterial[] materials,ArrayList<AITexture> texList){
		
		// ------ VERTICES ------
		ByteBuffer vertexArrayBufferData=BufferUtils.createByteBuffer(mesh.mNumVertices()*4*Float.BYTES);
		AIVector3D.Buffer vertices=mesh.mVertices();
		
		ByteBuffer normalArrayBufferData=BufferUtils.createByteBuffer(mesh.mNumVertices()*3*Float.BYTES);
		AIVector3D.Buffer normals=mesh.mNormals();
		
		ByteBuffer texArrayBufferData=BufferUtils.createByteBuffer(mesh.mNumVertices()*2*Float.BYTES);
		
		l.trace("Mesh {} has {} vertices",new String(mesh.mName().dataString()),mesh.mNumVertices());
		for(int i=0;i<mesh.mNumVertices();++i){
			AIVector3D vert=vertices.get(i);
			vertexArrayBufferData.putFloat(vert.x());
			vertexArrayBufferData.putFloat(vert.y());
			vertexArrayBufferData.putFloat(vert.z());
			vertexArrayBufferData.putFloat(1f);
			
			AIVector3D norm=normals.get(i);
			normalArrayBufferData.putFloat(norm.x());
			normalArrayBufferData.putFloat(norm.y());
			normalArrayBufferData.putFloat(norm.z());
			
			if(mesh.mNumUVComponents().get(0)!=0){
				AIVector3D texture=mesh.mTextureCoords(0).get(i);
				texArrayBufferData.putFloat(texture.x()).putFloat(texture.y());
			}else{
				texArrayBufferData.putFloat(0).putFloat(0);
			}
		}
		
		vertexArrayBufferData.flip();
		normalArrayBufferData.flip();
		texArrayBufferData.flip();
		
		int faceCount=mesh.mNumFaces();
		int elementCount=faceCount*3;
		IntBuffer elementArrayBufferData=BufferUtils.createIntBuffer(elementCount);
		AIFace.Buffer facesBuffer=mesh.mFaces();
		for(int i=0;i<faceCount;++i){
			AIFace face=facesBuffer.get(i);
			if(face.mNumIndices()!=3){
				throw new IllegalStateException("AIFace.mNumIndices() != 3");
			}
			elementArrayBufferData.put(face.mIndices());
		}
		elementArrayBufferData.flip();
		
		AIMaterial mat=materials[mesh.mMaterialIndex()];
		int numProps=mat.mNumProperties();
		AIMaterialProperty[] mprops=new AIMaterialProperty[numProps];
		for(int i=0;i<numProps;++i){
			mprops[i]=AIMaterialProperty.create(mat.mProperties().get(i));
		}
		
		// ------ ELEMENTS ------*/
		GraphicsThread d=new GraphicsThread(){
			@Override
			public void function(){
				VAO3D vao=new VAO3D();
				vao.vao=GL43Renderer.genVAO();
				glBindVertexArray(vao.vao);
				vao.vbos=new int[4];
				
				int vab=GL43Renderer.createVBO(vertexArrayBufferData.asFloatBuffer());
				GL43Renderer.addVBO(vao.vao,vab,4,0);
				vao.vbos[0]=vab;
				vao.vertices=vertexArrayBufferData.capacity()/4;
				
				int normalArrayBuffer=GL43Renderer.createVBO(normalArrayBufferData.asFloatBuffer());
				GL43Renderer.addVBO(vao.vao,normalArrayBuffer,1,3);
				vao.vbos[1]=normalArrayBuffer;
				
				int texArrayBuffer=GL43Renderer.createVBO(texArrayBufferData.asFloatBuffer());
				GL43Renderer.addVBO(vao.vao,texArrayBuffer,2,2);
				vao.vbos[2]=texArrayBuffer;
				
				int elementArrayBuffer=GL43Renderer.createEBO(elementArrayBufferData);
				AssimpModelWrapper.this.ibo=elementArrayBuffer;
				vao.vbos[3]=elementArrayBuffer;
				
				setReturn(vao);
			}
		};
		GraphicsProvider.addNeedsGraphicsThread(d);
		return (VAO3D)d.waitForCompletion();
	}
}
