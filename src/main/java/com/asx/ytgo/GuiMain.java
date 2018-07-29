package com.asx.ytgo;

import org.asx.glx.gui.GuiPanel;

public class GuiMain extends GuiPanel
{
	public GuiMain()
	{
		super(new ThemeYouTubeToGo());
		new FormFrontEnd(this, null);
	}
}
