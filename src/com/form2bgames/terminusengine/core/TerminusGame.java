package com.form2bgames.terminusengine.core;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.form2bgames.terminusengine.events.BasicEventHandler;
import com.form2bgames.terminusengine.events.EngineShutdownEvent;
import com.form2bgames.terminusengine.events.EventManager;
import com.form2bgames.terminusengine.graphics.GLRenderer;

public abstract class TerminusGame{
	protected static final Logger logger=LogManager.getLogger();
	protected GLRenderer renderer;
	protected static TerminusGame INSTANCE;
	
	public static TerminusGame getInstance(){
		return INSTANCE;
	}
	
	public TerminusGame(AppInfo ai){
		TerminusGame.INSTANCE=this;
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
		File f=new File("res/natives/"+os_arch);
		System.setProperty("org.lwjgl.util.Debug",EngineInfo.bt!=EngineInfo.BuildType.STABLE?"true":"false");
		System.setProperty("org.lwjgl.librarypath",f.getAbsolutePath());
		
		logger.info("Terminus Version: {} on arch {}",EngineInfo.getVersion(),os_arch);
		
		Thread rTh=new Thread(new Runnable(){

			@Override
			public void run(){
				renderer=GLRenderer.startRenderer("");
				renderer.render();
			}
		});
		rTh.setName("GT Daemon");
		rTh.start();
		
		gameInit();
		
		logger.info("done with gameinit");
		
		Thread gameLoop=new Thread(new Runnable(){
			@Override
			public void run(){
				loop();
			}
		});
		gameLoop.setName("Game Loop Thread");
		gameLoop.start();
		
		BasicEventHandler beh=new BasicEventHandler();
		EventManager.subscribe(EngineShutdownEvent.ENGINE_SHUTDOWN_EVENT,beh);
		
		renderer.finishedLoading();
		
		beh.waitForEvent();// stop things from gameinit being erroneously GC'ed
		// also eventually save preferences
		System.exit(0);
	}
	
	protected abstract void loop();
	
	protected abstract void gameInit();
}
