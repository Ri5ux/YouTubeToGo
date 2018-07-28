package com.asx.yttg;

public class VideoData
{
	private String id;
	private boolean downloaded;
	
	public VideoData(String id)
	{
		this.id = id;
	}
	
	public String getId()
	{
		return id;
	}
	
	public boolean isDownloaded()
	{
		return downloaded;
	}
}
