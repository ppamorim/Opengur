package com.kenny.openimgur.api;

import android.support.annotation.NonNull;

import org.json.JSONObject;

/**
 * Class for handling events on the bus
 */
public class ImgurBusEvent {
    public enum EventType {
        GALLERY,
        COMMENTS,
        ITEM_DETAILS,
        ALBUM_DETAILS,
        PROFILE_DETAILS,
        ACCOUNT_GALLERY_FAVORITES,
        ACCOUNT_SUBMISSIONS,
        COMMENT_POSTING,
        COMMENT_VOTE,
        GALLERY_VOTE,
        FAVORITE,
        GALLERY_ITEM_INFO,
        UPLOAD,
        GALLERY_SUBMISSION,
        REDDIT_SEARCH,
        ACCOUNT_COMMENTS,
        ACCOUNT_CONVOS,
        CONVO_MESSAGES,
        MESSAGE_SEND,
        ACCOUNT_UPLOADS,
        COMMENT_POSTED,
        CONVO_DELETE,
        IMAGE_DELETE,
        RANDOM_GALLERY,
        CONVO_REPORT,
        CONVO_BLOCK,
        TOPICS,
        MEME,
        GALLERY_SEARCH,
        TAGS,
        ALBUM_DELETE,
        USER_ALBUMS
    }

    public JSONObject json;

    public EventType eventType;

    public ApiClient.HttpRequest httpRequest;

    @NonNull
    public String id;

    public ImgurBusEvent(JSONObject json, EventType eventType, ApiClient.HttpRequest httpRequest, String id) {
        this.json = json;
        this.eventType = eventType;
        this.httpRequest = httpRequest;
        this.id = id;
    }

    @Override
    public String toString() {
        return "JSON: " + json.toString()
                + "EVENT TYPE: " + eventType.name()
                + "ID: " + id
                + "REQUEST TYPE: " + httpRequest.name();
    }
}