package com.asx.ytgo;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
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

	protected Font font;
	protected Font fontSecondary;

	protected GuiText title;
	protected GuiText titleDesc;
	protected GuiText pageStatus;
	protected GuiText statusText;
	protected GuiTextfield urlTextfield;
	protected GuiButtonSprite buttonDownload;

	private Color backgroundColor = new Color(0.2F, 0.2F, 0.2F, 0.5F);
	private Color highlightColor = new Color(0.75F, 0, 0.1F, 1F);
	private Color normalColor = new Color(1F, 1F, 1F, 0.75F);
	private Color hoverColor = new Color(1F, 1F, 1F, 1F);
	private Color titlebarColor = new Color(0.175F, 0.175F, 0.175F, 0.95F);
	private Color statusBarColor = new Color(0.15F, 0.15F, 0.15F, 0.9F);
	private Color searchBarColor = new Color(0.5F, 0.5F, 0.5F, 0.25F);
	private Color buttonColor = new Color(0.5F, 0.5F, 0.5F, 0.25F);
	private Color buttonHoverColor = new Color(0.75F, 0.75F, 0.75F, 0.25F);
	private Color videoElementColor = new Color(0.25F, 0.25F, 0.25F, 0.15F);
	private Color videoElementHoverColor = new Color(0.5F, 0.5F, 0.5F, 0.15F);
	private static Color erroredHoveringBackgroundColor = new Color(0.75F, 0, 0.1F, 1F);
	private static Color erroredBackgroundColor = new Color(0.75F, 0, 0.1F, 0.75F);

	public FormFrontEnd(GuiPanel panel, GuiForm parentForm)
	{
		super(panel, parentForm);
		font = new Font("Segoe UI", Font.BOLD, 20);
		fontSecondary = new Font("Segoe UI", Font.PLAIN, 14);

		title = new GuiText(this, font, "GO!");
		title.setColor(normalColor, normalColor);

		titleDesc = new GuiText(this, font, "[ALPHA]");
		titleDesc.setColor(highlightColor, highlightColor);

		pageStatus = new GuiText(this, fontSecondary, "");
		pageStatus.setColor(normalColor, normalColor);

		statusText = new GuiText(this, font, "No Playlist Loaded");
		statusText.setColor(normalColor, normalColor);

		buttonDownload = new GuiButtonSprite(this, 0, 0, Sprites.download);
		buttonDownload.setHoveringBackgroundColor(buttonColor);
		buttonDownload.setShouldRender(false);
		this.add(buttonDownload);

		urlTextfield = new GuiTextfield(this, 0, 0, 500, 30, new GuiText(this, fontSecondary, ""), false);
		urlTextfield.setPlaceholderText("Playlist URL...");
		urlTextfield.setColor(searchBarColor, searchBarColor);
		urlTextfield.setShouldRender(false);
		this.add(urlTextfield);
	}

	@Override
	public void render()
	{
		GuiElement.renderColoredRect(0, 0, Display.getWidth(), Display.getHeight(), backgroundColor);
		super.render();
		GuiElement.renderColoredRect(0, 0, Display.getWidth(), 50, titlebarColor);
		title.render(100, 10);
		titleDesc.render(title.getX() + title.getWidth() + 10, 10);
		Sprites.logoWide.draw(15, 8, 0.08F);

		GuiElement.renderColoredRect(0, Display.getHeight() - 50, Display.getWidth(), 50, statusBarColor);

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

//				if (this.p != null && this.p.videos() != null && p.videos().size() > 0)
//				{
//					int count = 0;
//					
//					for (Video v : this.p.videos())
//					{
//						if (v.isPreparationThreadComplete())
//						{
//							count++;
//						}
//					}
//					
//					if (count == p.videos().size())
//					{
//						this.enableDownloadButton();
//					}
//				}
			}
		}
	}

	public static FormFrontEnd instance()
	{
		if (YouTubeGo.gui().getPanel() instanceof GuiMain)
		{
			GuiMain main = (GuiMain) YouTubeGo.gui().getPanel();

			if (main.getForm() instanceof FormFrontEnd)
			{
				FormFrontEnd frontEnd = (FormFrontEnd) main.getForm();

				return frontEnd;
			}
		}

		return null;
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
		String charUnicodeValue = "\\u" + Integer.toHexString(character | 0x10000).substring(1);

		if (charUnicodeValue.equalsIgnoreCase("\\u0016"))
		{
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			try
			{
				Object clipboardData = clipboard.getData(DataFlavor.stringFlavor);

				if (clipboardData instanceof String)
				{
					String clipboardText = (String) clipboardData;

					for (char c : clipboardText.toCharArray())
					{
						urlTextfield.onKey(key, c, true);
					}
				}
			} catch (UnsupportedFlavorException e)
			{
				e.printStackTrace();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		if (key == Keyboard.KEY_RETURN)
		{
			if (GuiTextfield.activeTextfield == this.urlTextfield)
			{
				this.loadPlaylist();
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
					this.remove(e);
				}
			}
		}
	}

	private void loadPlaylist()
	{
		FormFrontEnd.instance().statusText.setText("Loading playlist...");
		this.deleteExistingVideoElements();

		String uri = urlTextfield.getText();

		try
		{
			URL url = new URL(uri);

			if (url != null)
			{
				Map<String, String> queryMap = Util.splitQuery(url);

				String id = queryMap.get("list");

				if (id != null && !id.isEmpty())
				{
					p = new Playlist(queryMap.get("list"));
					p.downloadPlaylistData();
				} else
				{
					YouTubeGo.log().warning("URL is not a YouTube playlist: " + uri);
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

	public static class GuiVideoElement extends GuiText
	{
		private Playlist playlist;
		private Video video;
		private GuiText title;
		private GuiText status;

		public GuiVideoElement(GuiForm form, Font font, Playlist playlist, Video video)
		{
			super(form, font, playlist.videos().indexOf(video) + " - " + video.getTitle());
			this.playlist = playlist;
			this.video = video;
			this.title = new GuiText(form, font, playlist.videos().indexOf(video) + " - " + video.getTitle());
			this.status = new GuiText(form, font, "");
		}

		@Override
		public void render()
		{
			this.setWidth(Display.getWidth());
			this.setHeight(23);
			this.setLeftPadding(10);
			this.setRightPadding(10);

			if (this.video.hasErrored())
			{
				GuiElement.renderColoredRect(this.x, this.getY(), this.width, this.height, this.isMouseHovering() ? erroredHoveringBackgroundColor : erroredBackgroundColor);
			} else
			{
				GuiElement.renderColoredRect(this.x, this.getY(), this.width, this.height, this.isMouseHovering() ? this.hoveringBackgroundColor : this.backgroundColor);
			}
			title.render(this.x + leftPadding, this.getY());
			status.setText(this.getVideo().getStatus());
			status.render(Display.getWidth() - status.getWidth() - rightPadding, this.getY());
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
	}

	public void updatePlaylistData()
	{
		for (Video v : p.videos())
		{
			GuiVideoElement video = new GuiVideoElement(this, fontSecondary, p, v);
			video.setBackgroundColor(videoElementColor);
			video.setPosition(0, 55 + p.videos().indexOf(v) * 24);
			video.setColor(normalColor, hoverColor);
			video.setHoveringBackgroundColor(videoElementHoverColor);
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
