package net.openright.infrastructure.rest;

import org.jsonbuddy.JsonObject;

@SuppressWarnings("unused")
public interface ResourceApi {

    default String createResource(JsonObject jsonObject) {
        throw new UnsupportedOperationException();
    }

    default JsonObject getResource(String id) {
        throw new UnsupportedOperationException();
    }

    default void updateResource(String id, JsonObject jsonObject) {
        throw new UnsupportedOperationException();
    }

    default JsonObject listResources() {
        throw new UnsupportedOperationException();
    }
}
