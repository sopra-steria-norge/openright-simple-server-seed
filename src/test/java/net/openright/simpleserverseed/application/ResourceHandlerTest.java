package net.openright.simpleserverseed.application;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ResourceHandlerTest {

    @Test
    public void shouldParseLocalPath() throws Exception {
        ResourceHandler handler = new ResourceHandler(null);
        assertThat(handler.parseLocalPath("/foo", "/foo")).isEmpty();
        assertThat(handler.parseLocalPath("/foo", "/foo/")).isEmpty();
        assertThat(handler.parseLocalPath("/foo", "/foo/bar")).containsExactly("bar");
        assertThat(handler.parseLocalPath("/foo", "/foo/bar/baz")).containsExactly("bar", "baz");
        assertThat(handler.parseLocalPath("/foo/", "/foo/")).isEmpty();
        assertThat(handler.parseLocalPath("/foo/", "/foo/bar")).containsExactly("bar");
        assertThat(handler.parseLocalPath("/foo/", "/foo/bar/baz")).containsExactly("bar", "baz");
    }

}
