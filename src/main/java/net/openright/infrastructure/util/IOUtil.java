package net.openright.infrastructure.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;

public class IOUtil {

	public static void extractResourceFile(String filename) {
		File file = new File(filename);
		if (file.exists())
			return;

		try (InputStream input = IOUtil.class.getResourceAsStream("/" + filename)) {
			if (input == null) {
				throw new IllegalArgumentException("Can't find /" + filename + " in classpath");
			}

			copy(input, file);
		} catch (IOException e) {
			throw ExceptionUtil.soften(e);
		}
	}

	public static String toString(URL url) {
		try (InputStream content = (InputStream) url.getContent()) {
			return toString(content);
		} catch (IOException e) {
			throw ExceptionUtil.soften(e);
		}
	}

	public static String toString(InputStream content) {
		return toString(new InputStreamReader(content));
	}

	public static String toString(Reader reader) {
		char[] buffer = new char[1024];
		StringBuilder out = new StringBuilder();

		try {
			for (;;) {
				int rsz = reader.read(buffer, 0, buffer.length);
				if (rsz < 0)
					break;
				out.append(buffer, 0, rsz);
			}
			return out.toString();
		} catch (IOException e) {
			throw ExceptionUtil.soften(e);
		}
	}

	public static void copy(URL url, File file) {
		try (InputStream content = (InputStream) url.getContent()) {
			copy(content, file);
		} catch (IOException e) {
			throw ExceptionUtil.soften(e);
		}

	}

	public static void copy(InputStream content, File file) {
		try (FileOutputStream output = new FileOutputStream(file)) {
			copy(content, output);
		} catch (IOException e) {
			throw ExceptionUtil.soften(e);
		}
	}

	public static void copy(InputStream in, OutputStream out) {
		try {
			byte[] buf = new byte[1024];
			int count = 0;
			while ((count = in.read(buf)) >= 0) {
				out.write(buf, 0, count);
			}
		} catch (IOException e) {
			throw ExceptionUtil.soften(e);
		}
	}
}
