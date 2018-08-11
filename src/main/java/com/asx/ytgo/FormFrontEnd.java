package com.asx.ytgo;

import java.awt.Font;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import org.asx.glx.gui.GuiPanel;
import org.asx.glx.gui.elements.GuiButtonSprite;
import org.asx.glx.gui.elements.GuiElement;
import org.asx.glx.gui.elements.GuiElement.IAction;
import org.asx.glx.gui.elements.GuiText;
import org.asx.glx.gui.elements.GuiTextfield;
import org.asx.glx.gui.forms.GuiForm;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;

import com.asx.ytgo.media.Playlist;
import com.asx.ytgo.media.Video;
import com.asx.ytgo.util.Util;

public class FormFrontEnd extends GuiForm
{
	private Playlist p;
	private String statusBarText;
	private boolean downloading;
	
	private int statusBarYSize = 50;

	protected Font font;
	protected Font fontSecondary;

	protected GuiText title;
	protected GuiText titleDesc;
	protected GuiText pageStatus;
	protected GuiText statusText;
	protected GuiTextfield urlTextfield;
	protected GuiButtonSprite buttonDownload;

	private static Color BACKGROUND_COLOR = new Color(0.2F, 0.2F, 0.2F, 0.5F);
	private static Color HIGHLIGHT_COLOR = new Color(0.75F, 0, 0.1F, 1F);
	private static Color NORMAL_COLOR = new Color(1F, 1F, 1F, 0.75F);
	private static Color HOVER_COLOR = new Color(1F, 1F, 1F, 1F);
	private static Color TITLEBAR_COLOR = new Color(0.175F, 0.175F, 0.175F, 0.95F);
	private static Color STATUSBAR_COLOR = new Color(0.15F, 0.15F, 0.15F, 0.9F);
	private static Color SEARCHBAR_COLOR = new Color(0.5F, 0.5F, 0.5F, 0.25F);
	private static Color BUTTON_COLOR = new Color(0.5F, 0.5F, 0.5F, 0.25F);
	private static Color BUTTON_HOVER_COLOR = new Color(0.75F, 0.75F, 0.75F, 0.25F);
	private static Color VIDEO_ELEMENT_COLOR = new Color(0.25F, 0.25F, 0.25F, 0.15F);
	private static Color VIDEO_ELEMENT_HOVER_COLOR = new Color(0.5F, 0.5F, 0.5F, 0.15F);
	private static Color ERROR_HOVER_BACKGROUND_COLOR = new Color(0.75F, 0, 0.1F, 1F);
	private static Color ERROR_BACKGROUND_COLOR = new Color(0.75F, 0, 0.1F, 0.75F);

	public FormFrontEnd(GuiPanel panel, GuiForm parentForm)
	{
		super(panel, parentForm);
		INSTANCE = this;
		font = new Font("Segoe UI", Font.BOLD, 20);
		fontSecondary = new Font("Segoe UI", Font.PLAIN, 14);

		title = new GuiText(this, font, "GO!");
		title.setColor(NORMAL_COLOR, NORMAL_COLOR);

		titleDesc = new GuiText(this, font, "[ANDY EDITION]");
		titleDesc.setColor(HIGHLIGHT_COLOR, HIGHLIGHT_COLOR);

		pageStatus = new GuiText(this, fontSecondary, "");
		pageStatus.setColor(NORMAL_COLOR, NORMAL_COLOR);

		statusText = new GuiText(this, font, "No Playlist Loaded");
		statusText.setColor(NORMAL_COLOR, NORMAL_COLOR);

		buttonDownload = new GuiButtonSprite(this, 0, 0, Sprites.download);
		buttonDownload.setHoveringBackgroundColor(BUTTON_COLOR);
		buttonDownload.setShouldRender(false);
		this.add(buttonDownload);

		urlTextfield = new GuiTextfield(this, 0, 0, 500, 30, new GuiText(this, fontSecondary, ""), false);
		urlTextfield.setPlaceholderText("Playlist URL...");
		urlTextfield.setColor(SEARCHBAR_COLOR, SEARCHBAR_COLOR);
		urlTextfield.setShouldRender(false);
		urlTextfield.setText(YouTubeGo.getRuntimePlaylistURL() != null && !YouTubeGo.getRuntimePlaylistURL().isEmpty() ? YouTubeGo.getRuntimePlaylistURL() : "");
		this.add(urlTextfield);
		
		if (!this.urlTextfield.getText().isEmpty() && !this.urlTextfield.getText().equals(this.urlTextfield.getPlaceholderText()))
		{
		    this.load();
		}
	}

	@Override
	public void render()
	{
		GuiElement.renderColoredRect(0, 0, Display.getWidth(), Display.getHeight(), BACKGROUND_COLOR);
		super.render();
		GuiElement.renderColoredRect(0, 0, Display.getWidth(), statusBarYSize, TITLEBAR_COLOR);
		title.render(100, 10);
		titleDesc.render(title.getX() + title.getWidth() + 10, 10);
		Sprites.logoWide.draw(15, 8, 0.08F);

		GuiElement.renderColoredRect(0, Display.getHeight() - 50, Display.getWidth(), 50, STATUSBAR_COLOR);

		pageStatus.setText(this.p == null ? "No playlists are currently loaded, paste the URL of one in the bar above to get started!" : "");
		pageStatus.render(Display.getWidth() / 2 - pageStatus.getWidth() / 2, Display.getHeight() / 2);

		statusText.setText(this.getStatusBarText());
		statusText.render(20, Display.getHeight() - 50 + 10);

		buttonDownload.setSize(50, 50);
		buttonDownload.setPosition(Display.getWidth() - buttonDownload.getWidth(), 0);

		if (buttonDownload.shouldRender())
			buttonDownload.render();

		urlTextfield.setPosition(titleDesc.getX() + titleDesc.getWidth() + 10, 10);
		urlTextfield.setWidth(Display.getWidth() - (10 * 2) - titleDesc.getX() - titleDesc.getWidth() - buttonDownload.getWidth());
		urlTextfield.render();
		
		if (YouTubeGo.gui().getTicks() % 20 == 0)
		{
			if (p == null)
			{
				this.setStatusBarText("No Playlist Loaded");
			} else
			{
				if (this.downloading && this.p != null && this.p.videos() != null)
				{
					int count = 0;

					for (Video v : this.p.videos())
					{
						if (v.isDownloaded())
						{
							count++;
						}
					}

					this.setStatusBarText(String.format("%s / %s Videos Downloaded", count, this.p.videos().size()));
				}
			}
		}
	}
	
	private static FormFrontEnd INSTANCE;

	public static FormFrontEnd instance()
	{
		return INSTANCE;
	}
	
	public Playlist getPlaylist()
	{
		return p;
	}

	public void setStatusBarText(String statusBarText)
	{
		this.statusBarText = statusBarText;
	}

	private String getStatusBarText()
	{
		return statusBarText;
	}

	@Override
	public void onScroll(int dwheel)
	{
        GuiVideoElement lastVideoElement = this.getVideoElementFor(this.getPlaylist().videos().get(this.getPlaylist().videos().size() - 1));
        
        int height = Display.getHeight();
        
        if (lastVideoElement.getY() + lastVideoElement.getHeight() < height - this.statusBarYSize && dwheel < 0)
        {
            return;
        }
        
		super.onScroll(dwheel);
	}

	@Override
	public void onElementClick(GuiElement element)
	{
		super.onElementClick(element);
	}

	@Override
	public void onKey(int key, char character)
	{
		super.onKey(key, character);

		if (key == Keyboard.KEY_RETURN)
		{
			if (GuiTextfield.activeTextfield == this.urlTextfield)
			{
				this.load();
			}

			if (this.onScreen)
			{
				this.onScreen = false;
			}
		} else if (key == Keyboard.KEY_TAB)
		{
			GuiTextfield.activeTextfield = this.urlTextfield;
		}
	}

	private void deleteExistingVideoElements()
	{
		if (p != null)
		{
			for (GuiElement e : this.getElements())
			{
				if (e instanceof GuiVideoElement)
				{
				    GuiVideoElement ve = (GuiVideoElement) e;
				    ve.prepareForRemoval();
					this.remove(e);
				}
			}
		}
	}

	private void load()
	{
		this.deleteExistingVideoElements();

		String uri = urlTextfield.getText();

		try
		{
			URL url = new URL(uri);

			if (url != null)
			{
				Map<String, String> queryMap = Util.splitQuery(url);

				String playlistId = queryMap.get("list");

				if (playlistId != null && !playlistId.isEmpty())
				{
				    this.loadPlaylist(queryMap);
				} else
				{
	                String videoId = queryMap.get("v");
	                
				    if (videoId != null && !videoId.isEmpty())
				    {
				        this.loadVideo(queryMap);
				    }
				    else
				    {
				        YouTubeGo.log().warning("URL is not a YouTube playlist or video: " + uri);
				    }
				}
			}
		} catch (MalformedURLException e)
		{
			YouTubeGo.log().warning("Invalid URL: " + uri);
			e.printStackTrace();
		} catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}

	}
    
    private void loadPlaylist(Map<String, String> queryMap)
    {
        statusText.setText("Loading playlist...");
        p = new Playlist(queryMap.get("list"));
        p.downloadPlaylistData();
    }
    
    private void loadVideo(Map<String, String> queryMap)
    {
        statusText.setText("Loading video...");
        p = new Playlist(null);
        p.downloadVideoData(queryMap.get("v"));
    }
    
    public ArrayList<GuiVideoElement> getVideoElements()
    {
        ArrayList<GuiVideoElement> videoElements = new ArrayList<GuiVideoElement>();
        
        for (GuiElement element : this.getElements())
        {
            if (element instanceof GuiVideoElement)
            {
                videoElements.add((GuiVideoElement) element);
            }
        }
        
        return videoElements;
    }
    
    public GuiVideoElement getVideoElementFor(Video v)
    {
        ArrayList<GuiVideoElement> videoElements = this.getVideoElements();
        
        for (GuiVideoElement ve : videoElements)
        {
            if (ve.getVideo().getId().equalsIgnoreCase(v.getId()))
            {
                return ve;
            }
        }
        
        return null;
    }

	public static class GuiVideoElement extends GuiText
	{
		private Playlist playlist;
		private Video video;
		private GuiText title;
		private GuiText status;
		private GuiButtonSprite downloadButton;

		public GuiVideoElement(GuiForm form, Font font, Playlist playlist, Video video)
		{
			super(form, font, playlist.videos().indexOf(video) + " - " + video.getTitle());
			this.playlist = playlist;
			this.video = video;
			this.title = new GuiText(form, font, playlist.videos().indexOf(video) + " - " + video.getTitle());
			this.status = new GuiText(form, font, "");
			this.downloadButton = new GuiButtonSprite(form, 0, 0, Sprites.downloadSmall);
            this.downloadButton.setWidth(23);
            this.downloadButton.setHeight(23);
			this.downloadButton.setHoveringBackgroundColor(VIDEO_ELEMENT_HOVER_COLOR);
			this.downloadButton.setClickAction(new IAction<GuiElement>() {
                @Override
                public void run(GuiElement o)
                {
                    System.out.println("Individually downloading " + video.getTitle());
                    video.setErrored(false);
                    playlist.runDownloadThread(video, true, true);
                }
            });
			this.form.add(this.downloadButton);
		}

		public void prepareForRemoval()
        {
		    this.form.remove(this.downloadButton);
        }

        @Override
		public void render()
		{
            boolean large = Display.getHeight() >= 768;
            int dpi = large ? 4 : 1;
            this.setY(55 + playlist.videos().indexOf(video) * (24 * dpi));
            this.setWidth(Display.getWidth());
            this.height = 23 * dpi;

			this.setLeftPadding(10);
			this.setRightPadding(10);
            
            if (this.video.getThumbnailImage() != null)
            {
                int h = this.height;
                this.video.getThumbnailImage().draw(this.x, this.getY(), h * 16 / 9, h);
            }

			if (this.video.hasErrored())
			{
				GuiElement.renderColoredRect(this.x, this.getY(), this.width, this.height, this.isMouseHovering() ? ERROR_HOVER_BACKGROUND_COLOR : ERROR_BACKGROUND_COLOR);
			} else
			{
				GuiElement.renderColoredRect(this.x, this.getY(), this.width, this.height, this.isMouseHovering() ? this.hoveringBackgroundColor : this.backgroundColor);
			}
			
			title.setColor(NORMAL_COLOR, NORMAL_COLOR);
			title.render(this.x + leftPadding, this.getY());
			status.setText(this.getVideo().getStatus());
			status.render(Display.getWidth() - status.getWidth() - rightPadding - this.downloadButton.getWidth(), this.getY());
			
            this.downloadButton.setX(Display.getWidth() - this.downloadButton.getWidth());
			this.downloadButton.setY(this.getY());
			this.downloadButton.render();
		}

		@Override
		public int getY()
		{
			return super.getY() + this.form.getScrollOffset();
		}

		public Playlist getPlaylist()
		{
			return playlist;
		}

		public Video getVideo()
		{
			return video;
		}

		public GuiText getStatus()
		{
			return status;
		}
		
		public GuiButtonSprite getDownloadButton()
        {
            return downloadButton;
        }
	}

	public void updatePlaylistData()
	{
		for (Video v : p.videos())
		{
			GuiVideoElement video = new GuiVideoElement(this, fontSecondary, p, v);
			
			video.setBackgroundColor(VIDEO_ELEMENT_COLOR);
			video.setPosition(0, 55 + p.videos().indexOf(v) * 24);
			video.setColor(NORMAL_COLOR, HOVER_COLOR);
			video.setHoveringBackgroundColor(VIDEO_ELEMENT_HOVER_COLOR);
			video.setShouldRender(true);
			this.add(video);
			this.setStatusBarText(String.format("Playlist loaded. Found %s video(s)", p.videos().size()));
		}

		buttonDownload.setClickAction(new IAction<GuiElement>()
		{
			@Override
			public void run(GuiElement o)
			{
				Thread th = new Thread()
				{
					public void run()
					{
						try
						{
							buttonDownload.setShouldRender(false);
							p.process(true);
							System.out.println(String.format("Downloaded %s video(s) in playlist %s", p.videos().size(), p.getPlaylistId()));

						} catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				};
				th.start();
			}
		});
		this.enableDownloadButton();
	}

	public void setDownloading(boolean downloading)
	{
		this.downloading = downloading;
	}

	public void enableDownloadButton()
	{
		this.buttonDownload.setShouldRender(true);
	}
}
