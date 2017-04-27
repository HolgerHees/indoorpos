package com.holgerhees.util;

public class CharSetConverter
{
	public static final String CHARSET_UTF8 = "UTF-8";

	public static String convertToCharset(String orig, String charSet)
	{
		if (orig == null || charSet == null)
		{ return orig; }
		try
		{
			return new String(orig.getBytes(), charSet);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return orig;
	}

	public static String convertToUTF8(String orig)
	{
		return convertToCharset(orig, CHARSET_UTF8);
	}
}