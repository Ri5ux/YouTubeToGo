package com.asx.ytgo;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.logging.Logger;

import com.asx.ytgo.media.Playlist;
import com.asx.ytgo.media.Video;
import com.asx.ytgo.util.Util;
import com.asx.ytgo.util.streams.AudioStream;

public class YouTubeGo
{
	public static final boolean DEBUG = false;
	private static final File LWJGL_NATIVES = new File("lib/natives");
	public static final File RESOURCES = new File("resources");

	private static Logger logger = Logger.getLogger("YTGO");
	private static File workDirectory = new File("content");
	private static File audioDirectory = new File(YouTubeGo.getWorkDirectory(), "audio");
	
	private static UserInterface ui;
	private static boolean appRunning = true;
	
	public static class Properties
	{
		public static final String NAME = "YouTube Go!";
		public static final String VERSION = "1.0";
	}

	public static void main(String[] args) throws Exception
	{
		System.out.println("LWJGL Natives Path: " + LWJGL_NATIVES.getAbsolutePath());
		System.setProperty("org.lwjgl.librarypath", LWJGL_NATIVES.getAbsolutePath());
		
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

		System.out.println(Properties.NAME);
		System.out.println("Version " + Properties.VERSION);
		
		appRunning = true;
		ui = new UserInterface();
		ui.init();


//		URL url = new URL(args[0]);
//		Map<String, String> queryMap = Util.splitQuery(url);
//		System.out.println("URL To Process: " + url);
//		System.out.println("URL Parameters: " + queryMap);
//		downloadPlaylist(queryMap);
	}
	
	protected static void run()
	{
//		System.out.println("run client");
	}
	
	public static void shutdown()
	{
		YouTubeGo.appRunning = false;
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
	
	public static UserInterface gui()
	{
		return ui;
	}
	
	public static boolean isAppRunning()
	{
		return appRunning;
	}
}