package com.asx.yttg;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.logging.Logger;

import com.asx.yttg.media.Playlist;
import com.asx.yttg.media.Video;
import com.asx.yttg.util.Util;
import com.asx.yttg.util.streams.AudioStream;

public class YouTubeToGo
{
	public static final boolean DEBUG = false;

	private static Logger logger = Logger.getLogger("YTTG");
	private static File workDirectory = new File("content");
	private static File audioDirectory = new File(YouTubeToGo.getWorkDirectory(), "audio");

	public static void main(String[] args) throws Exception
	{
		if (!getWorkDirectory().exists())
		{
			if (getWorkDirectory().mkdirs())
			{
				System.out.println("Working directory created: " + getWorkDirectory().getAbsolutePath());
			}
		}

		if (!getAudioDirectory().exists())
		{
			if (getAudioDirectory().mkdirs())
			{
				System.out.println("Audio directory created: " + getAudioDirectory().getAbsolutePath());
			}
		}

		URL url = new URL(args[0]);
		Map<String, String> queryMap = Util.splitQuery(url);

		System.out.println("YouTube To Go");
		System.out.println("Version 1.0");
		System.out.println("URL To Process: " + url);
		System.out.println("URL Parameters: " + queryMap);

		downloadPlaylist(queryMap);
	}

	public static void downloadPlaylist(Map<String, String> queryMap)
	{
		String playlistId = queryMap.get("list");
		Playlist p = new Playlist(playlistId);
		p.prepare();
		p.process(true);

		System.out.println(String.format("Downloaded %s video(s) in playlist %s", p.videos().size(), p.getPlaylistId()));
	}

	public static void downloadSingleVideo(Map<String, String> queryMap)
	{
		Video v = new Video(queryMap.get("v"));
		v.process();
		v.acquireStreamData();

		AudioStream audio = v.getAudioStreams().get(0);
		System.out.println("");
		System.out.println(String.format("Downloading first audio stream in list for '%s'...", v.getTitle()));
		System.out.println(String.format("Bitrate: %s kbps", +audio.getBitrate() / 1000));

		if (audio.download())
		{
			System.out.println("Complete!");
		}
	}

	public static Logger log()
	{
		return logger;
	}

	public static File getWorkDirectory()
	{
		return workDirectory;
	}
	
	public static File getAudioDirectory()
	{
		return audioDirectory;
	}
}