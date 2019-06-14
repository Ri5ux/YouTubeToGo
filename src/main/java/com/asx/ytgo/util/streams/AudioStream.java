package com.asx.ytgo.util.streams;

import java.io.File;
import java.io.IOException;

import com.asx.ytgo.YouTubeGo;
import com.asx.ytgo.media.Video;
import com.asx.ytgo.util.Util;
import com.google.gson.JsonObject;

public class AudioStream extends Stream
{
	public AudioStream(Video v, JsonObject o)
	{
		super(v, o);

		if (YouTubeGo.DEBUG)
		{
			System.out.println("");
		}
	}

	@Override
	public boolean download()
	{
		try
		{
			if (this.checkFile())
			{
				System.out.println(String.format("Save filename: %s", this.getFile().getAbsolutePath()));
				this.getFile().createNewFile();
				Util.downloadFile(this.getUrl(), this.getFile().toString());
			} else
			{
				System.out.println("Media file already exists, skipping!");
				return false;
			}
		} catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
	@Override
	public String getFileExtension()
	{
		return this.extension;
	}

	@Override
	public File getFile()
	{
		if (!YouTubeGo.getAudioDirectory().exists())
		{
			YouTubeGo.getAudioDirectory().mkdirs();
		}
		
		return new File(YouTubeGo.getAudioDirectory().getAbsolutePath(), String.format("%s.%s", this.v.getFileSafeName(), this.getFileExtension()));
	}
}