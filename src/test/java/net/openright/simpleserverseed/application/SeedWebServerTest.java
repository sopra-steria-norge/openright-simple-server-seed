package net.openright.simpleserverseed.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

public class SeedWebServerTest {

    @Test
    public void shouldProbeFileTypes() throws Exception {
        assertThat(Files.probeContentType(Paths.get("index.html"))).isEqualTo("text/html");
        assertThat(Files.probeContentType(Paths.get("index.foo"))).isEqualTo(null);
        assertThat(Files.probeContentType(Paths.get("index"))).isEqualTo(null);
        assertThat(Files.probeContentType(Paths.get("directory/"))).isEqualTo(null);
        assertThat(Files.probeContentType(Paths.get("some-webfont.woff2"))).isEqualTo("application/font-woff2");
    }

}
