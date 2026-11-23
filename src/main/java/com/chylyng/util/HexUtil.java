package com.chylyng.util;

public class HexUtil {
	public static String hexToAscii(String hex) {
		if (hex.length() == 0 || hex.length() % 2 != 0) {
			return "";
		} else {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < hex.length(); i = i + 2) {
				String s = hex.substring(i, i + 2);
				int n = Integer.valueOf(s, 16);
				builder.append((char) n);
			}
			return builder.toString();
		}
	}
}
