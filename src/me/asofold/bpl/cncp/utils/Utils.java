package me.asofold.bpl.cncp.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class Utils {

	public static final String toString(final Throwable t) {
		final Writer buf = new StringWriter(500);
		final PrintWriter writer = new PrintWriter(buf);
		t.printStackTrace(writer);
		return buf.toString();
	}

}
