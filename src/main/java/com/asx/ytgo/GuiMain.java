package com.asx.ytgo;

import org.asx.glx.gui.GuiPanel;
import org.asx.glx.gui.forms.GuiForm;

public class GuiMain extends GuiPanel
{
	private GuiForm form;
	
	public GuiMain()
	{
		super(new ThemeYouTubeToGo());
		this.form = new FormFrontEnd(this, null);
	}
	
	public GuiForm getForm()
	{
		return form;
	}
}
