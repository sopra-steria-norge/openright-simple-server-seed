package net.openright.infrastructure;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.spi.FileTypeDetector;

public class WebFileTypeDetectors extends FileTypeDetector {

    @Override
    public String probeContentType(Path path) throws IOException {
        String filename = path.getFileName().toString();
        return probeExtension(filename.substring(filename.lastIndexOf(".")+1));
    }

    private String probeExtension(String extension) {
        switch (extension) {
        case "woff2": return "application/font-woff2";
        }
        return null;
    }

}
