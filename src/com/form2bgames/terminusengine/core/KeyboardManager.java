package com.form2bgames.terminusengine.core;

import java.util.HashMap;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;

public class KeyboardManager {
	private static HashMap<Integer,AKeyHandler> map=new HashMap<Integer,AKeyHandler>();
	private static HashMap<Character,ACharHandler> mmap=new HashMap<Character,ACharHandler>();
	private static AKeyHandler defaultHandler=null;
	private static ACharHandler defaultCharHandler=null;
	public static void setDefaultCharHandler(ACharHandler defaultHandler) {
		KeyboardManager.defaultCharHandler = defaultHandler;
	}
	public static void setDefaultHandler(AKeyHandler defaultHandler) {
		KeyboardManager.defaultHandler = defaultHandler;
	}
	public static void init(long window){
		GLFW.glfwSetKeyCallback(window, new GLFWKeyCallbackImpl(){});
		GLFW.glfwSetCharCallback(window, new GLFWCharCallbackImpl(){});
	}
	public static class GLFWKeyCallbackImpl implements GLFWKeyCallbackI{

		@Override
		public void invoke(long window, int key, int scancode, int action, int mods) {
			AKeyHandler h=map.get(key);
			if(h!=null)
				h.handle(key, scancode, action, mods);
			else{
				if(defaultHandler!=null)
					defaultHandler.handle(key, scancode, action, mods);
			}
		}

		
	}
	public static void addHandler(Integer c,AKeyHandler h){
		map.put(c,h);
	}
	public static void removeHandler(Integer c){
		map.remove(c);
	}
	public static void addCharHandler(char c,ACharHandler h){
		mmap.put(c,h);
	}
	public static void removeCharHandler(char c){
		mmap.remove(c);
	}
	public static class GLFWCharCallbackImpl implements GLFWCharCallbackI{
		@Override
		public void invoke(long window, int ch) {
			char chr=(char)ch;
			ACharHandler h=mmap.get(chr);
			if(h!=null)
				h.handle(chr);
			else{
				if(defaultCharHandler!=null)
					defaultCharHandler.handle(chr);
			}
		}
	}
	public static abstract class AKeyHandler{
		public abstract void handle(int key,int scancode,int action,int mods);
	}
	public static abstract class ACharHandler{
		public abstract void handle(char ch);
	}
}
