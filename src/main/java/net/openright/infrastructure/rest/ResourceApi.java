package net.openright.infrastructure.rest;

import org.json.JSONObject;

public interface ResourceApi {
    default String createResource(JSONObject jsonObject) {
        throw new UnsupportedOperationException();
    }

    default JSONObject getResource(String id) {
        throw new UnsupportedOperationException();
    }

    default void updateResource(String id, JSONObject jsonObject) {
        throw new UnsupportedOperationException();
    }

    default JSONObject listResources() {
        throw new UnsupportedOperationException();
    }
}
