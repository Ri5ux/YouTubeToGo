package com.asx.yttg.media;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import com.asx.yttg.YouTubeToGo;
import com.asx.yttg.util.JSON;
import com.asx.yttg.util.Util;
import com.asx.yttg.util.streams.AudioStream;
import com.asx.yttg.util.streams.Stream;
import com.asx.yttg.util.streams.VideoStream;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Video
{
	

	private String id;
	private boolean downloaded;
	private String title;
	private String videoLength;
	private JsonObject videoArguments;

	private ArrayList<VideoStream> videoStreams;
	private ArrayList<AudioStream> audioStreams;

	public Video(String id)
	{
		this.id = id;
		this.videoStreams = new ArrayList<VideoStream>();
		this.audioStreams = new ArrayList<AudioStream>();
	}

	public String getId()
	{
		return id;
	}

	public boolean isDownloaded()
	{
		return downloaded;
	}

	public String getTitle()
	{
		return title;
	}

	public String getFileSafeName()
	{
		return Util.cleanupFilename(this.getTitle());
	}

	public File[] getApplicableMediaFiles()
	{
		return YouTubeToGo.getAudioDirectory().listFiles(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String name)
			{
				return name.startsWith(getFileSafeName());
			}
		});
	}

	public boolean verifyApplicableMediaFiles()
	{
		boolean OK = true;

		for (File f : getApplicableMediaFiles())
		{
			if (Stream.checkFile(f))
			{
				OK = false;
			}
		}

		return OK;
	}

	public String getVideoLengthSeconds()
	{
		return videoLength;
	}

	public JsonObject getVideoArguments()
	{
		return videoArguments;
	}

	public ArrayList<AudioStream> getAudioStreams()
	{
		return audioStreams;
	}

	public ArrayList<VideoStream> getVideoStreams()
	{
		return videoStreams;
	}

	public void process()
	{
		if (!this.getId().isEmpty() && !this.isDownloaded())
		{
			this.acquireVideoInfo();
		}
	}

	private void acquireVideoInfo()
	{
		String query = this.buildVideoInfoRequest();
		String result = Util.query(query, false, true);
		result = Util.cleanupResult(result);

		if (result != null)
		{
			JsonElement json = JSON.parseJsonFromString(String.format("[%s]", result));

			if (json != null)
			{
				if (json instanceof JsonObject)
				{
					JsonObject streamJson = (JsonObject) json;
					this.videoArguments = streamJson.get("args").getAsJsonObject();
					this.title = this.videoArguments.get("title").toString().replaceAll("\"", "");
					this.videoLength = this.videoArguments.get("length_seconds").toString();
				}
			} else
			{
				System.out.println("Video info request failed: NULL JSON");
				return;
			}
		} else
		{
			System.out.println("Video info request failed: NULL RESULT");
			return;
		}

		System.out.println("Video metadata acquired.");
	}

	public void acquireStreamData()
	{
		if (this.audioStreams != null)
		{
			this.audioStreams.clear();
		}

		if (this.videoStreams != null)
		{
			this.videoStreams.clear();
		}

		System.out.println(String.format("Finding audio and video streams for '%s'", this.getTitle()));
		String query = this.buildStreamRequest();
		String result = Util.cleanupResult(Util.query(query, false, true));

		if (result != null)
		{
			JsonElement json = JSON.parseJsonFromString(String.format("[%s]", result));

			if (json != null)
			{
				for (JsonElement e : json.getAsJsonArray())
				{
					if (e instanceof JsonObject)
					{
						JsonObject streamJson = (JsonObject) e;
						String mediaType = streamJson.get("type").toString();

						if (mediaType.contains("audio"))
						{
							AudioStream audio = new AudioStream(this, streamJson);
							this.audioStreams.add(audio);
						}

						if (mediaType.contains("video"))
						{
							VideoStream video = new VideoStream(this, streamJson);
							this.videoStreams.add(video);
						}
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

		System.out.println(String.format("%s total streams found. %s audio streams, %s video streams", this.audioStreams.size() + this.videoStreams.size(), this.audioStreams.size(), this.videoStreams.size()));
	}

	private String buildStreamRequest()
	{
		return String.format("http://arisux.com/upload/youtube-downloader/fetchStreamData.php?id=%s", this.getId());
	}

	private String buildVideoInfoRequest()
	{
		return String.format("http://arisux.com/upload/youtube-downloader/fetchStreamData.php?info=true&id=%s", this.getId());
	}
}
