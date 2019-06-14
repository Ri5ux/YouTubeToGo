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
	protected String extension;
	protected int filesize;

	public Stream(Video v, JsonObject o)
	{
		this.v = v;
		this.url = o.get("url").getAsString();
		this.bitrate = o.get("abr").getAsLong();
		this.type = o.get("format").getAsString();
        this.extension = o.get("ext").getAsString();
        this.filesize = o.get("filesize").getAsInt();

		if (YouTubeGo.DEBUG)
		{
			System.out.println(String.format("URL: %s", this.url));
			System.out.println(String.format("Bitrate: %s", this.bitrate));
            System.out.println(String.format("Type: %s", this.type));
            System.out.println(String.format("Extension: %s", this.extension));
            System.out.println(String.format("Filesize: %s", this.extension));
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
		return extension;
	}
	
	public int getFilesize()
    {
        return filesize;
    }

	public File getFile()
	{
		return null;
	}
}