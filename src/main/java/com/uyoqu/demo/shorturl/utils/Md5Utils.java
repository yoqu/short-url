package com.uyoqu.demo.shorturl.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Utils {
	public static String bytes2HexString(byte[] b) {
		String ret = "";
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			ret += hex.toUpperCase();
		}
		return ret;
	}

	private static String md5CryptoCore(byte[] data) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(data);
			byte b[] = md.digest();
			int i;
			StringBuffer buf = new StringBuffer();
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}
			return buf.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String to32LowerCase(byte[] data) {
		return md5CryptoCore(data).toLowerCase();
	}

	public static String to32LowerCase(String sourceStr) {
		return to32LowerCase(sourceStr.getBytes());
	}

	public static String to32UpperCase(byte[] data) {
		return md5CryptoCore(data).toUpperCase();
	}

	public static String to32UpperCase(String sourceStr) {
		return to32UpperCase(sourceStr.getBytes());
	}

	public static String to16UpperCase(String sourceStr) {
		return to32UpperCase(sourceStr).substring(8, 24);
	}
}
