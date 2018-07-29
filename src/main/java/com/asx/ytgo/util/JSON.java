package com.asx.ytgo.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.asx.ytgo.YouTubeGo;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JSON
{
	public static JsonElement parseJsonFromFile(File pathToJson)
	{
		try
		{
			return parseJsonFromStream(new FileInputStream(pathToJson));
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static JsonElement parseJsonFromStream(InputStream stream)
	{
		try
		{
			InputStreamReader reader = new InputStreamReader(stream);
			JsonParser parser = new JsonParser();
			JsonElement rootElement = parser.parse(reader);

			if (rootElement.isJsonArray())
			{
				for (JsonElement json : rootElement.getAsJsonArray())
				{
					return json;
				}
			}
		} catch (Exception e)
		{
			YouTubeGo.log().info("The stream could not be parsed as valid JSON.");
			e.printStackTrace();
		}
		
		return null;
	}

	public static JsonElement parseJsonFromString(String s)
	{
		try
		{
			JsonParser parser = new JsonParser();
			JsonElement rootElement = parser.parse(s);

			if (rootElement.isJsonArray())
			{
				for (JsonElement json : rootElement.getAsJsonArray())
				{
					return json;
				}
			}
		} catch (Exception e)
		{
			YouTubeGo.log().info("The stream could not be parsed as valid JSON.");
			e.printStackTrace();
		}
		
		return null;
	}
}
