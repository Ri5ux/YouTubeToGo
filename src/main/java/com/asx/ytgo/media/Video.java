package com.asx.ytgo.media;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.asx.glx.opengl.Sprite;

import com.asx.ytgo.YouTubeGo;
import com.asx.ytgo.util.JSON;
import com.asx.ytgo.util.Util;
import com.asx.ytgo.util.streams.AudioStream;
import com.asx.ytgo.util.streams.Stream;
import com.asx.ytgo.util.streams.VideoStream;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Video
{
    private String                 id;
    private String                 title;
    private String                 videoLength;
    private JsonObject             videoArguments;
    private JsonObject             videoDetails;
    private float                  percentDownloaded;
    private String                 statusText;

    private boolean                preparationThreadComplete;
    private boolean                hasErrored;

    private Sprite                 thumbnailImage;

    private ArrayList<VideoStream> videoStreams;
    private ArrayList<AudioStream> audioStreams;
    private AudioStream            audioStream = null;

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
        return percentDownloaded >= 100;
    }

    public void setDownloaded()
    {
        this.percentDownloaded = 100;
    }

    public void setPercentDownloaded(float percentDownloaded)
    {
        this.percentDownloaded = percentDownloaded;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getFileSafeName()
    {
        return Util.cleanupFilename(this.getTitle());
    }

    public File[] getApplicableMediaFiles()
    {
        return YouTubeGo.getAudioDirectory().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name)
            {
                return name.startsWith(getFileSafeName()) && !name.contains("jpg") && !name.contains("png");
            }
        });
    }

    public boolean verifyApplicableMediaFiles()
    {
        boolean OK = true;

        File[] applicable = getApplicableMediaFiles();

        if (applicable == null)
        {
            return false;
        }

        for (File f : applicable)
        {
            if (Stream.checkFile(f))
            {
                OK = false;
            }
        }

        // TODO: TEST
        if (applicable.length <= 0)
        {
            OK = false;
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

    public JsonObject getVideoDetails()
    {
        return videoDetails;
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
        if (this.getId().isEmpty() && !this.isDownloaded())
        {
            this.acquireVideoInfo();
        }
    }

    public File getThumbnailFile()
    {
        if (!YouTubeGo.getAudioDirectory().exists())
        {
            YouTubeGo.getAudioDirectory().mkdirs();
        }

        return new File(YouTubeGo.getAudioDirectory().getAbsolutePath(), String.format("%s.%s", this.getFileSafeName(), this.getThumbnailFileExtension()));
    }

    public String getThumbnailFileExtension()
    {
        if (videoArguments != null)
        {
            String thumbnailUrl = this.videoArguments.get("thumbnail_url").toString();
            return thumbnailUrl.substring(thumbnailUrl.lastIndexOf(".") + 1, thumbnailUrl.length() - 1);
        }

        return "jpg";
    }

    public URL getThumbnailMaxUrl() throws MalformedURLException
    {
        return new URL(String.format("https://i.ytimg.com/vi/%s/maxresdefault.jpg", this.id));
    }

    public URL getThumbnailHQUrl() throws MalformedURLException
    {
        return new URL(String.format("https://i.ytimg.com/vi/%s/hqdefault.jpg", this.id));
    }

    public void downloadThumbnail()
    {
        try
        {
            if (!this.getThumbnailFile().exists())
            {
                System.out.println(String.format("Downloading thumbnail for '%s'", this.getTitle()));
                URL thumbnailUrl = getThumbnailHQUrl();
                System.out.println(thumbnailUrl);
                this.getThumbnailFile().createNewFile();
                Util.downloadFile(thumbnailUrl.toString(), this.getThumbnailFile().getAbsolutePath().toString());
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public Sprite getThumbnailImage()
    {
        if (this.thumbnailImage == null && this.getThumbnailFile() != null && this.getThumbnailFile().exists())
        {
            this.thumbnailImage = Sprite.load(this.getThumbnailFile());
        }

        return thumbnailImage;
    }

    public void acquireVideoInfo()
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
                    String playerResponse = this.videoArguments.get("player_response").getAsString();
                    this.videoDetails = new Gson().fromJson(playerResponse, JsonObject.class).get("videoDetails").getAsJsonObject();
                    this.title = this.videoArguments.get("title").toString().replaceAll("\"", "");
                    this.videoLength = this.videoArguments.get("length_seconds").toString();

                    System.out.println("Video Details: " + this.videoDetails);
                }
            }
            else
            {
                System.out.println("Video info request failed: NULL JSON");
                return;
            }
        }
        else
        {
            System.out.println("Video info request failed: NULL RESULT");
            return;
        }

        System.out.println(String.format("Metadata acquired for '%s'", this.title));
    }

    public void acquireStreamData()
    {
        this.acquireStreamData(0);
    }

    public void acquireStreamData(int pass)
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
                        System.out.println(streamJson);
                        JsonElement format = streamJson.get("format");
                        String mediaType = format.toString();

                        if (mediaType.contains("audio"))
                        {
                            AudioStream audio = new AudioStream(this, streamJson);

                            try
                            {
                                int code = Util.getRemoteCode(new URL(audio.getUrl()));

                                if (code >= 300)
                                {
                                    if (pass >= 3)
                                    {
                                        YouTubeGo.log().warning(String.format("Pass %s, Error %s for '%s'", pass, code, this.getTitle()));
                                        return;
                                    }
                                    else
                                    {
                                        this.acquireStreamData(pass++);
                                    }
                                }
                            }
                            catch (MalformedURLException e1)
                            {
                                e1.printStackTrace();
                            }

                            this.audioStreams.add(audio);
                        }

                        if (mediaType.contains("video"))
                        {
                            VideoStream video = new VideoStream(this, streamJson);
                            this.videoStreams.add(video);
                        }
                    }
                }
            }
            else
            {
                System.out.println("Video stream request failed: NULL JSON");
                return;
            }
        }
        else
        {
            System.out.println("Video stream request failed: NULL RESULT");
            return;
        }

        System.out.println(String.format("%s total streams found. %s audio streams, %s video streams for '%s'", this.audioStreams.size() + this.videoStreams.size(), this.audioStreams.size(), this.videoStreams.size(), this.getTitle()));
    }

    private String buildStreamRequest()
    {
        return String.format("http://arisux.com/upload/ytdl/download.php?link=https://www.youtube.com/watch?v=%s", this.getId());
    }

    private String buildVideoInfoRequest()
    {
        return String.format("http://arisux.com/upload/youtube-downloader/fetchStreamData.php?info=true&id=%s", this.getId());
    }

    public void setAudioStream(AudioStream audio)
    {
        this.audioStream = audio;
    }

    public void setAudioStream(int idx)
    {
        this.audioStream = this.getAudioStreams().get(idx);
    }

    public AudioStream getProposedAudioStream()
    {
        return this.audioStream;
    }

    public void setStatusText(String statusText)
    {
        this.statusText = statusText;
    }

    public String getStatus()
    {
        return this.statusText;
    }

    // public boolean isPreparationThreadComplete()
    // {
    // return preparationThreadComplete;
    // }
    //
    // public void setVideoPreparationThreadComplete()
    // {
    // this.preparationThreadComplete = true;
    // }

    public void setErrored(boolean error)
    {
        this.hasErrored = error;
    }

    public boolean hasErrored()
    {
        return this.hasErrored;
    }
}
