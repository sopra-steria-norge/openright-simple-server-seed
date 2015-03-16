package net.openright.simpleserverseed.infrastructure.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtil {

	public static void extractResourceFile(String filename) {
        File file = new File(filename);
        if (file.exists()) return;

        try (InputStream input = IOUtil.class.getResourceAsStream("/" + filename)) {
        	if (input == null) {
        		throw new IllegalArgumentException("Can't find /" + filename + " in classpath");
        	}

        	try (FileOutputStream output = new FileOutputStream(file)) {
                copy(input, output);
            }
        } catch (IOException e) {
        	throw new RuntimeException("Failed to extract " + filename + ": " + e);
        }
	}

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[1024];
        int count = 0;
        while ((count = in.read(buf)) >= 0) {
            out.write(buf, 0, count);
        }
    }
}
