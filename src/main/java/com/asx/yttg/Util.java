package com.asx.yttg;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class Util
{
	public static Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException
	{
		Map<String, String> query_pairs = new LinkedHashMap<String, String>();
		String query = url.getQuery();
		String[] pairs = query.split("&");

		for (String pair : pairs)
		{
			int idx = pair.indexOf("=");
			query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
		}

		return query_pairs;
	}

	/**
	 * Retrieves the contents of a page with the specified URL. NOTE: Networking
	 * must be enabled for this method to function.
	 * 
	 * @param url - The URL to retrieve page contents from.
	 * @return The contents of the remote page.
	 */
	public static String query(String url)
	{
		return query(url, false, true);
	}

	/**
	 * Retrieves the contents of a page with the specified URL. NOTE: Networking
	 * must be enabled for this method to function.
	 * 
	 * @param url            - The URL to retrieve page contents from.
	 * @param insertNewLines - If set true, the method automatically inserts line
	 *                       breaks.
	 * @return The contents of the remote page.
	 */
	public static String query(String url, boolean insertNewLines, boolean quiet)
	{
		HttpURLConnection connection = null;

		try
		{
			connection = ((HttpURLConnection) (new URL(url)).openConnection());
			connection.setDoInput(true);
			connection.setDoOutput(false);
			connection.connect();

			if (connection.getResponseCode() / 100 != 2)
			{
				return null;
			} else
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				StringBuilder builder = new StringBuilder();
				String inputLine;

				while ((inputLine = reader.readLine()) != null)
				{
					if (insertNewLines)
						builder.append(inputLine + "\n");
					else
						builder.append(inputLine);
				}

				reader.close();
				return builder.toString();
			}
		} catch (Exception e)
		{
			if (!quiet)
			{
				Main.log().warning(e.toString() + ": " + url);
			}

			if (connection != null)
			{
				connection.disconnect();
			}
			return null;
		} finally
		{
			if (connection != null)
			{
				connection.disconnect();
			}
		}
	}

	/**
	 * Downloads a file from the specified URL and saves it to the specified
	 * location. NOTE: Networking must be enabled for this method to function.
	 * 
	 * @param fileUrl      - The URL to download a file from.
	 * @param saveLocation - The location where the downloaded file will be saved.
	 * @throws IOException
	 */
	public static void downloadFile(String fileUrl, String saveLocation) throws IOException
	{
		Main.log().info("Downloading file from '" + fileUrl + "' and saving it to '" + saveLocation + "'");
		InputStream is = (new URL(fileUrl)).openStream();
		FileOutputStream os = new FileOutputStream(saveLocation);
		byte[] b = new byte[2048];
		int length;

		while ((length = is.read(b)) != -1)
		{
			os.write(b, 0, length);
		}

		is.close();
		os.close();
	}
}
