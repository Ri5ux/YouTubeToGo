package com.asx.yttg.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

import com.asx.yttg.YouTubeToGo;

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
				System.out.println("ERROR1");
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
				YouTubeToGo.log().warning(e.toString() + ": " + url);
			}

			if (connection != null)
			{
				connection.disconnect();
			}
			System.out.println("ERROR2");
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
		YouTubeToGo.log().info("Downloading file from '" + fileUrl + "' and saving it to '" + saveLocation + "'");
//		InputStream is = (new URL(fileUrl)).openStream();
//		FileOutputStream os = new FileOutputStream(saveLocation);
//		byte[] b = new byte[4096 * 1024];
//		int length;
//
//		while ((length = is.read(b)) != -1)
//		{
//			os.write(b, 0, length);
//		}
//
//		is.close();
//		os.close();

		/////////////////////////////////////////////////////

		URL url = new URL(fileUrl);
		URLConnection connection = url.openConnection();
		int filesize = connection.getContentLength();
		float dataRead = 0;
		BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
		FileOutputStream fos = new FileOutputStream(saveLocation);
		BufferedOutputStream bout = new BufferedOutputStream(fos, 2048);
		byte[] data = new byte[2048];
		int i = 0;
		int c = 0;

		System.out.println(String.format("File Size: %s MB", Math.round(((double) filesize / 1024D / 1024D) * 100D) / 100D));

		while ((i = in.read(data, 0, 2048)) >= 0)
		{
			c++;
			dataRead = dataRead + i;
			bout.write(data, 0, i);
			float progress = (dataRead * 100) / filesize;

			if (c % 100 == 0)
			{
				commandLineProgressBar((int) progress);
			}
		}
		
		System.out.println("");

		bout.close();
		in.close();
	}

	public static void commandLineProgressBar(int progress)
	{
		int size = 50;
		int relativeProgress = progress * size / 100;

		System.out.print("\r[");

		for (int i = 0; i < size; i++)
		{
			if (i < relativeProgress)
			{
				System.out.print("=");
			} else
			{
				System.out.print(" ");
			}
		}

		System.out.print("] " + progress + "%");
	}

	public static String cleanupResult(String result)
	{
		if (result != null && !result.isEmpty())
		{
			result = result.replaceAll("[\\s\\p{Z}]+", " ").trim();
			return result.replace("\n", "").replace("\r", "");
		}

		System.out.println("Unable to cleanup.");

		return null;
	}

	public static boolean isFileEmpty(File audioFile)
	{
		BufferedReader br = null;

		try
		{
			br = new BufferedReader(new FileReader(audioFile.getAbsolutePath()));

			if (br.readLine() == null)
			{
				return true;
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		} finally
		{
			try
			{
				if (br != null)
				{
					br.close();
				}
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		return false;
	}

	public static String cleanupFilename(String filename)
	{
		return filename.replaceAll("[\\\\/:*?\"<>|]", "");
	}
}