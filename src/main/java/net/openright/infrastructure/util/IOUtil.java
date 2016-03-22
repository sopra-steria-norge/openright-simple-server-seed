package net.openright.infrastructure.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class IOUtil {

    public static Path extractResourceFile(String filename) {
        Path path = Paths.get(filename);
        if (Files.exists(path))
            return path;

        try (InputStream input = IOUtil.class.getResourceAsStream("/" + filename)) {
            if (input == null) {
                throw new IllegalArgumentException("Can't find /" + filename + " in classpath");
            }
            copy(input, path);
            return path;
        } catch (IOException e) {
            throw ExceptionUtil.soften(e);
        }
    }

    public static String toString(URL url) {
        try (InputStream content = (InputStream) url.getContent()) {
            return toString(content);
        } catch (IOException e) {
            throw soften(e);
        }
    }

    public static String toString(HttpURLConnection connection) {
        try (InputStream inputStream = connection.getInputStream()) {
            return toString(inputStream);
        } catch (IOException e) {
            throw soften(e);
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
            throw soften(e);
        }
    }

    public static void copy(Path path, OutputStream output) {
        try {
            Files.copy(path, output);
        } catch (IOException e) {
            throw soften(e);
        }
    }

    public static void copy(InputStream input, Path path) {
        try {
            Files.copy(input, path);
        } catch (IOException e) {
            throw soften(e);
        }
    }

    public static Path copy(URL url, Path file) {
        if (Files.isDirectory(file)) {
            file = file.resolve(Paths.get(url.getPath()).getFileName());
        }
        if (Files.exists(file)) {
            return file;
        }
        try (InputStream content = (InputStream) url.getContent()) {
            Files.copy(content, file);
            return file;
        } catch (IOException e) {
            throw soften(e);
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
            throw soften(e);
        }
    }

    public static int copy(String text, URI uri) {
        try {
            HttpURLConnection connection = (HttpURLConnection)uri.toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            copy(text, connection);
            return connection.getResponseCode();
        } catch (IOException e) {
            throw soften(e);
        }
    }

    public static void copy(String text, HttpURLConnection connection) throws IOException {
        try (Writer writer = new OutputStreamWriter(connection.getOutputStream())) {
            writer.write(text);
        }
    }

    private static RuntimeException soften(IOException e) {
        return ExceptionUtil.soften(e);
    }
}
