package com.asx.yttg.util.streams;

import com.asx.yttg.YouTubeToGo;
import com.asx.yttg.media.Video;
import com.google.gson.JsonObject;

public class VideoStream extends Stream
{
	private int framerate;
	private String quality;
	private String resolution;

	public VideoStream(Video v, JsonObject o)
	{
		super(v, o);

		this.framerate = o.get("fps").getAsInt();
		this.quality = o.get("quality_label").getAsString();
		this.resolution = o.get("size").getAsString();

		if (YouTubeToGo.DEBUG)
		{
			System.out.println(String.format("Framerate: %s", this.framerate));
			System.out.println(String.format("Quality: %s", this.quality));
			System.out.println(String.format("Resolution: %s", this.resolution));
			System.out.println("");
		}
	}

	public int getFramerate()
	{
		return framerate;
	}

	public String getQuality()
	{
		return quality;
	}

	public String getResolution()
	{
		return resolution;
	}
}