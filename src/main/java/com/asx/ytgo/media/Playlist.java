package com.asx.ytgo.media;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import com.asx.ytgo.util.JSON;
import com.asx.ytgo.util.Util;
import com.asx.ytgo.util.streams.AudioStream;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Playlist
{
	private ArrayList<Video> videos = new ArrayList<Video>();
	private String playlistId;
	private JsonElement json;

	public Playlist(String playlistId)
	{
		this.playlistId = playlistId;
	}

	public void prepare()
	{
		String query = this.buildPlaylistRequest();
		String result = Util.query(query, false, true);
		result = Util.cleanupResult(result);

		if (result != null)
		{
			this.json = JSON.parseJsonFromString(String.format("[%s]", result));
			
			if (json != null)
			{
				for (JsonElement e : json.getAsJsonArray())
				{
					if (e instanceof JsonObject)
					{
						JsonObject streamJson = (JsonObject) e;
						JsonObject snippet = streamJson.get("snippet").getAsJsonObject();
						JsonObject resourceId = snippet.get("resourceId").getAsJsonObject();
						String videoId = resourceId.get("videoId").getAsString();
						String title = snippet.get("title").getAsString();

						Video v = new Video(videoId);
						v.setTitle(title);
						
						if (v.verifyApplicableMediaFiles())
						{
							v.setDownloaded();
						}
						
						this.videos.add(v);
					}
				}
			} else
			{
				System.out.println("Video stream request failed: NULL JSON");
				return;
			}
		} else
		{
			System.out.println("Video stream request failed: NULL RESULT");
			return;
		}

		System.out.println("Information acquired successfully for playlist with id " + this.playlistId);
	}

	public void process()
	{
		this.process(false);
	}

	public void process(boolean download)
	{
		if (download)
		{
			System.out.println("Downloading all videos, this may take a while...");
		} else
		{
			System.out.println("Processing all videos, this may take a bit...");
		}

		for (Video v : this.videos)
		{
			System.out.println(String.format("Processing %s - %s/%s", v.getId(), this.videos.indexOf(v), this.videos.size()));
			
			if (v.getTitle() == null || v.getTitle().isEmpty())
			{
				v.process();
			}

			if (download)
			{
				File[] applicableFiles = v.getApplicableMediaFiles();

				if (applicableFiles != null && applicableFiles.length > 0 && v.verifyApplicableMediaFiles())
				{
					System.out.println("Found applicable media files, skipping download: " + Arrays.asList(applicableFiles));
					v.setDownloaded();
				} else
				{
					this.doVideoDownload(v);
				}
			}

			System.out.println("");
		}
	}
	
	private void doVideoDownload(Video v)
	{
		this.doVideoDownload(v, 0);
	}
	
	private void doVideoDownload(Video v, int pass)
	{
		if (pass > 0)
		{
			System.out.println(String.format("Retrying download, pass %s...", pass));
		}
		
		if (pass >= 3)
		{
			System.out.println("Failed to download. Please try again later.");
			return;
		}
		
		v.acquireStreamData();
		
		for (int i = 0; i < v.getAudioStreams().size(); i++)
		{
			AudioStream audio = v.getAudioStreams().get(0);

			if (audio.download())
			{
				System.out.println("Downloaded successfully!");
				v.setDownloaded();
				break;
			}

			if (audio.getProposedFile().exists() && !Util.isFileEmpty(audio.getProposedFile()))
			{
				break;
			}
			System.out.println(String.format("Stream with index %s is broken, trying next stream.", i));
			
			if ((i + 1) >= v.getAudioStreams().size())
			{
				this.doVideoDownload(v, pass++);
			}
		}
	}

	public String buildPlaylistRequest()
	{
		return String.format("http://www.arisux.com/upload/youtube-downloader/fetchPlaylistData.php?id=%s", this.playlistId);
	}

	public String getPlaylistId()
	{
		return playlistId;
	}

	public ArrayList<Video> videos()
	{
		return this.videos;
	}
	
	public JsonElement getJson()
	{
		return json;
	}
}
