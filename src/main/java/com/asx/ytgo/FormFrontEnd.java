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
import org.asx.glx.gui.elements.GuiElement;
import org.asx.glx.gui.elements.GuiText;
import org.asx.glx.gui.elements.GuiTextfield;
import org.asx.glx.gui.forms.GuiForm;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;

import com.asx.ytgo.media.Playlist;
import com.asx.ytgo.media.Video;
import com.asx.ytgo.util.Util;

public class FormFrontEnd extends GuiForm
{
	private Playlist p;
	protected Font font;
	protected Font fontSecondary;
	protected GuiText title;
	protected GuiText titleDesc;
	protected GuiTextfield urlTextfield;

	private Color backgroundColor = new Color(0.2F, 0.2F, 0.2F, 0.5F);
	private Color highlightColor = new Color(0.75F, 0, 0.1F, 1F);
	private Color normalColor = new Color(1F, 1F, 1F, 0.75F);
	private Color hoverColor = new Color(1F, 1F, 1F, 1F);
	private Color titlebarColor = new Color(0.15F, 0.15F, 0.15F, 0.9F);
	private Color searchBarColor = new Color(0.5F, 0.5F, 0.5F, 0.25F);
	private Color videoElementColor = new Color(0.25F, 0.25F, 0.25F, 0.15F);
	private Color videoElementHoverColor = new Color(0.5F, 0.5F, 0.5F, 0.15F);

	public FormFrontEnd(GuiPanel panel, GuiForm parentForm)
	{
		super(panel, parentForm);
		font = new Font("Segoe UI", Font.BOLD, 20);
		fontSecondary = new Font("Segoe UI", Font.PLAIN, 14);

		title = new GuiText(this, font, "GO!");
		title.setColor(normalColor, normalColor);

		titleDesc = new GuiText(this, font, "[ALPHA]");
		titleDesc.setColor(highlightColor, highlightColor);

		urlTextfield = new GuiTextfield(this, 0, 0, 500, 30, new GuiText(this, fontSecondary, ""), false);
		urlTextfield.setPlaceholderText("Playlist URL...");
		urlTextfield.setColor(searchBarColor, searchBarColor);
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

		urlTextfield.setPosition(Display.getWidth() - urlTextfield.getWidth() - 10, 10);
		urlTextfield.render();

		Sprites.logoWide.draw(15, 8, 0.08F);
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
							p.prepare();

							for (Video v : p.videos())
							{
								GuiVideoElement video = new GuiVideoElement(this, fontSecondary, p, v);
								video.setBackgroundColor(videoElementColor);
								video.setPosition(0, 55 + p.videos().indexOf(v) * 24);
								video.setColor(normalColor, hoverColor);
								video.setHoveringBackgroundColor(videoElementHoverColor);
								video.setShouldRender(true);
								this.add(video);
							}
						}
						else
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

			if (this.onScreen)
			{
				this.onScreen = false;
			}
		} else if (key == Keyboard.KEY_TAB)
		{
			GuiTextfield.activeTextfield = this.urlTextfield;
		}
	}

	public static class GuiVideoElement extends GuiText
	{
		private Playlist playlist;
		private Video video;
		private GuiText status;

		public GuiVideoElement(GuiForm form, Font font, Playlist playlist, Video video)
		{
			super(form, font, playlist.videos().indexOf(video) + " - " + video.getTitle());
			this.playlist = playlist;
			this.video = video;
			this.status = new GuiText(form, font, "");
		}

		@Override
		public void render()
		{
			this.setWidth(Display.getWidth());
			this.setHeight(23);
			this.setLeftPadding(10);
			this.setRightPadding(10);

			GL11.glPushMatrix();
			{
				GL11.glTranslated(0, this.form.getScrollOffset(), 0);
				GuiElement.renderColoredRect(this.x, this.y, this.width, this.height, this.isMouseHovering() ? this.hoveringBackgroundColor : this.backgroundColor);
				super.render();
				status.setText(this.getVideo().isDownloaded() ? "Downloaded" : "-");
				status.render(Display.getWidth() - status.getWidth() - rightPadding, this.y);
			}
			GL11.glPopMatrix();
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
}
