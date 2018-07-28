package com.asx.yttg;

import java.net.URL;
import java.util.Map;
import java.util.logging.Logger;

public class Main
{
	private static Logger logger = Logger.getLogger("YTTG");	
	
	public static void main(String[] args) throws Exception
	{
		URL url = new URL(args[0]);
		Map<String, String> queryMap = Util.splitQuery(url);
		
		System.out.println("YouTube To Go");
		System.out.println("Version 1.0 - Copyright (C) 2018 Dustin Christensen");
		System.out.println("URL To Process: " + url);
		System.out.println("URL Parameters: " + queryMap);
		
		VideoData vd = new VideoData(queryMap.get("v"));
		
		if (vd != null && !vd.getId().isEmpty() && !vd.isDownloaded())
		{
			System.out.println("Retrieving remote video data...");
			String videoDataQuery = String.format("http://arisux.com/upload/youtube-downloader/fetchStreamData.php?id=%s&info=true", vd.getId());
			String result = Util.query(videoDataQuery, true, true);
			System.out.println(String.format("Video Data Request URL: %s", videoDataQuery));
			System.out.println(result);
		}
	}
	
	public static Logger log()
	{
		return logger;
	}
}