package com.asx.ytgo.media;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.asx.ytgo.FormFrontEnd;
import com.asx.ytgo.YouTubeGo;
import com.asx.ytgo.util.JSON;
import com.asx.ytgo.util.Util;
import com.asx.ytgo.util.streams.AudioStream;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Playlist
{
    private ArrayList<Video> videos = new ArrayList<Video>();
    private String           playlistId;
    private JsonElement      json;
    private boolean          stopPlaylistDownloadThread;

    private Thread           playlistDownloadThread;

    public Playlist(String playlistId)
    {
        this.playlistId = playlistId;
    }

    public Thread getPlaylistDownloadThread()
    {
        return playlistDownloadThread;
    }

    public void downloadPlaylistData()
    {
        FormFrontEnd.instance().setStatusBarText("Loading playlist...");

        if (playlistDownloadThread != null && playlistDownloadThread.isAlive())
        {
            stopPlaylistDownloadThread = true;
        }

        playlistDownloadThread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                if (playlistId != null)
                {
                    String query = buildPlaylistRequest();
                    String result = Util.query(query, false, true);
                    result = Util.cleanupResult(result);

                    if (result != null)
                    {
                        json = JSON.parseJsonFromString(String.format("[%s]", result));

                        if (json != null)
                        {
                            FormFrontEnd.instance().setStatusBarText("Preparing video streams...");

                            for (JsonElement e : json.getAsJsonArray())
                            {
                                if (e instanceof JsonObject)
                                {
                                    JsonObject streamJson = (JsonObject) e;
                                    JsonObject snippet = streamJson.get("snippet").getAsJsonObject();
                                    JsonObject resourceId = snippet.get("resourceId").getAsJsonObject();
                                    String videoId = resourceId.get("videoId").getAsString();
                                    String title = snippet.get("title").getAsString();

                                    processVideo(videoId, title);
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
                        System.out.println("Playlist stream request failed: NULL RESULT");
                        FormFrontEnd.instance().setStatusBarText("Error loading playlist, try again.");
                        return;
                    }

                    System.out.println("Information acquired successfully for playlist with id " + playlistId);

                    FormFrontEnd.instance().updatePlaylistData();
                }
            }
        });
        playlistDownloadThread.start();
    }

    public void downloadVideoData(String videoId)
    {
        if (playlistDownloadThread != null && playlistDownloadThread.isAlive())
        {
            stopPlaylistDownloadThread = true;
        }

        playlistDownloadThread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                if (videoId != null)
                {
                    processVideo(videoId, null);
                    System.out.println("Information acquired successfully for video with id " + videoId);
                    FormFrontEnd.instance().updatePlaylistData();
                }
            }
        });
        playlistDownloadThread.start();
    }

    private void processVideo(String videoId, String title)
    {
        Video v = new Video(videoId);

        if (title != null)
        {
            v.setTitle(title);
        }
        else
        {
            v.acquireVideoInfo();
        }
        v.process();

        Thread thumbnailDownloadThread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                System.out.println(title);
                System.out.println(String.format("Downloading thumbnail for '%s'", v.getTitle()));
                v.downloadThumbnail();
            }
        });

        thumbnailDownloadThread.start();

        if (FormFrontEnd.instance().getPlaylist().stopPlaylistDownloadThread)
        {
            return;
        }

        if (v.verifyApplicableMediaFiles())
        {
            v.setStatusText("[Local File Found]");
            v.setDownloaded();
        }

        videos.add(v);
    }

    public void runVerification(Video v)
    {
        Thread videoPreparationThread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                v.setStatusText("Finding streams...");
                v.acquireStreamData();

                if (v.getProposedAudioStream() == null)
                {
                    for (AudioStream stream : v.getAudioStreams())
                    {
                        if (stream.getFile().exists())
                        {
                            v.setAudioStream(stream);
                            break;
                        }
                    }

                    if (v.getProposedAudioStream() == null && v.getAudioStreams() != null && v.getAudioStreams().size() > 0)
                    {
                        v.setAudioStream(v.getAudioStreams().get(0));
                    }
                }

                try
                {
                    if (v.getProposedAudioStream() != null)
                    {
                        if (v.getProposedAudioStream().getFile().exists())
                        {
                            long remoteFileSize = Util.getRemoteFilesize(new URL(v.getProposedAudioStream().getUrl()));
                            long localFileSize = v.getProposedAudioStream().getFile().length();

                            if (v.getProposedAudioStream() != null && Util.fileSizeMatches(v.getProposedAudioStream().getFile(), new URL(v.getProposedAudioStream().getUrl())))
                            {
                                v.setStatusText("[Downloaded]");
                                v.setDownloaded();
                            }
                            else if (v.getProposedAudioStream() != null)
                            {
                                YouTubeGo.log().warning(String.format("File size mismatch for '%s'. Remote: %s, Local %s", v.getTitle(), remoteFileSize, localFileSize));
                                System.out.println(v.getProposedAudioStream().getFile().getAbsolutePath());
                                v.setStatusText("Local File Corrupt");
                                System.out.println(String.format("Deleting corrupt file for song '%s'", v.getTitle()));
                                v.getProposedAudioStream().getFile().delete();
                                v.setStatusText("Deleted Corrupt Local File");
                            }
                        }
                        else
                        {
                            v.setStatusText("Local File Missing");
                            YouTubeGo.log().info(String.format("Local file does not exist for '%s'", v.getTitle()));
                        }
                    }
                    else
                    {
                        v.setStatusText("Audio Stream Error");
                        v.setErrored(true);
                        YouTubeGo.log().warning(String.format("Unable to verify local file for '%s'. Proposed audio stream was null.", v.getTitle()));
                    }
                }
                catch (MalformedURLException e1)
                {
                    v.setErrored(true);
                    v.setStatusText("Invalid Stream URL");
                    System.out.println("Unable to verify local filesize against remote filesize.");
                    e1.printStackTrace();
                }
            }
        });

        videoPreparationThread.start();
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
        }
        else
        {
            System.out.println("Processing all videos, this may take a bit...");
        }

        FormFrontEnd.instance().setStatusBarText(String.format("Downloading %s videos...", this.videos.size()));
        FormFrontEnd.instance().setDownloading(true);

        for (Video v : this.videos)
        {
            this.runDownloadThread(v, download);
        }
    }

    public void runDownloadThread(Video v, boolean download)
    {
        this.runDownloadThread(v, download, false);
    }

    public void runDownloadThread(Video v, boolean download, boolean processVideo)
    {
        Thread th = new Thread(new Runnable() {
            @Override
            public void run()
            {
                if (processVideo)
                {
                    v.process();
                }

                runVerification(v);

                if (download)
                {
                    doVideoDownload(v);
                }

                if (YouTubeGo.DEBUG)
                    System.out.println("");
            }
        });

        th.start();
    }

    private void doVideoDownload(Video v)
    {
        this.doVideoDownload(v, 0);
    }

    private void doVideoDownload(Video v, int pass)
    {
        if (pass > 0)
        {
            if (YouTubeGo.DEBUG)
                System.out.println(String.format("Retrying download, pass %s...", pass));
        }

        if (pass >= 3)
        {
            if (YouTubeGo.DEBUG)
                System.out.println(String.format("Failed to download '%s'. Please try again later.", v.getTitle()));
            return;
        }

        v.acquireStreamData();

        for (int i = 0; i < v.getAudioStreams().size(); i++)
        {
            AudioStream audio = v.getAudioStreams().get(i);
            
            if (audio.getFileExtension().equalsIgnoreCase("m4a"))
            {
                downloadStream(v, audio, 0);
            }
        }
    }

    private boolean downloadStream(Video v, AudioStream audio, int pass)
    {
        if (audio.download())
        {
            v.setAudioStream(audio);
            System.out.println(String.format("Download of '%s' completed.", v.getTitle()));
            v.setStatusText("[Downloaded]");
            v.setDownloaded();
            return true;
        }

        boolean fileSizeMatch = false;

        try
        {
            fileSizeMatch = Util.fileSizeMatches(audio.getFile(), new URL(audio.getUrl()));
        }
        catch (MalformedURLException e)
        {
            YouTubeGo.log().warning("Unable to verify filesize against remote source: URL Malformed");
            e.printStackTrace();
        }

        if (audio.getFile().exists() && !Util.isFileEmpty(audio.getFile()) && fileSizeMatch)
        {
            return true;
        }
        
        if (pass > 3)
        {
            System.out.println(String.format("Audio stream is broken, unable to download."));
            return false;
        }

        pass++;
        
        System.out.println(String.format("Audio stream download error. Retrying, Pass %s...", pass));
        
        return downloadStream(v, audio, pass);
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
