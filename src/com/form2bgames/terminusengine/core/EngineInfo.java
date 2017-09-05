package com.form2bgames.terminusengine.core;

public class EngineInfo{
	public static final Integer MAJOR=0,MINOR=2,REVISION=0;
	public static final BuildType bt=BuildType.ALPHA;
	
	public static final String getVersion(){
		return String.format("Terminus Engine %d.%d.%d %s",MAJOR,MINOR,REVISION,bt.toString());
	}
	
	public enum BuildType{
		ALPHA,BETA,STABLE,RELEASE_CANDIDATE_1,RELEASE_CANDIDATE_2,RELEASE_CANDIDATE_3
	}
}
