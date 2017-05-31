package com.form2bgames.terminusengine.core;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.form2bgames.terminusengine.graphics.GL43Renderer;

public abstract class TerminusGame {
	public static final Logger logger=LogManager.getLogger();
	protected GL43Renderer renderer;
	public TerminusGame(AppInfo ai){
		String arch=System.getProperty("os.arch");
		String os=System.getProperty("os.name");
		String os_arch="";
		if(os.toLowerCase().contains("mac"))
			os_arch="mac";
		else if(os.toLowerCase().contains("windows")){
			if(arch.contains("64"))
				os_arch="windows_x64";
			else
				os_arch="windows_x86";
		}else{
			if(!arch.contains("64"))
				throw new RuntimeException("Unix/Linux requires a 64 bit installation to run");
			os_arch="unix";
		}
		logger.info(os_arch);
		File f=new File("res/natives/"+os_arch);
		logger.info(f.getAbsolutePath());
		System.setProperty("org.lwjgl.util.Debug", EngineInfo.bt!=EngineInfo.BuildType.STABLE?"true":"false");
		System.setProperty("org.lwjgl.librarypath", f.getAbsolutePath());
		
		logger.info("Terminus Version: {}",EngineInfo.getVersion());
		
		renderer=new GL43Renderer(ai.appName);
		renderer.setName("Graphics Thread");
		renderer.setGame(this);

		renderer.start();
		
		gameInit();
		
		logger.info("done with gameinit");
		
		Thread gameLoop=new Thread(new Runnable(){

			@Override
			public void run() {
				loop();
			}
		});
		gameLoop.setName("Game Loop Thread");
		gameLoop.start();
		
		renderer.finishedLoading();
	}
	protected abstract void loop();
	protected abstract void gameInit();
	private boolean doneRender=false;
	protected void waitForRenderFinished(){
		try{
			while(!doneRender){
				Thread.sleep(0,10000);
			}
		}catch(Exception e){}
		doneRender=false;
	}
	public final void signalRenderDone() {
		doneRender=true;
	}
}
