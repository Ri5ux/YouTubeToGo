package com.asx.ytgo.util.streams;

import java.io.File;

import com.asx.ytgo.YouTubeGo;
import com.asx.ytgo.media.Video;
import com.asx.ytgo.util.Util;
import com.google.gson.JsonObject;

public class Stream
{
	protected Video v;
	protected String url;
	protected long bitrate;
	protected String type;

	public Stream(Video v, JsonObject o)
	{
		this.v = v;
		this.url = o.get("url").getAsString();
		this.bitrate = o.get("bitrate").getAsLong();
		this.type = o.get("type").getAsString();

		if (YouTubeGo.DEBUG)
		{
			System.out.println(String.format("URL: %s", this.url));
			System.out.println(String.format("Bitrate: %s", this.bitrate));
			System.out.println(String.format("Type: %s", this.type));
		}
	}

	public boolean checkFile()
	{
		return checkFile(this.getFile());
	}

	public static boolean checkFile(File mediaFile)
	{
		boolean overwrite = false;

		if (mediaFile.exists() && Util.isFileEmpty(mediaFile))
		{
			overwrite = true;
		}

		return (!mediaFile.exists() || overwrite);
	}

	/**
	 * @return true if this stream was downloaded. False if there was an error or if
	 *         the file already exists.
	 */
	public boolean download()
	{
		return false;
	}

	public Video getVideo()
	{
		return v;
	}

	public String getUrl()
	{
		return url;
	}

	public long getBitrate()
	{
		return bitrate;
	}

	public String getType()
	{
		return type;
	}

	public String getFileExtension()
	{
		return this.getType().split("/")[1];
	}

	public File getFile()
	{
		return null;
	}
}