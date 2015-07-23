package com.kenny.openimgur.api;

/**
 * Endpoints enum for the different endpoint URL
 * https://api.imgur.com/endpoints
 */
public enum Endpoints {
    // section/sort/page/show viral
    GALLERY("https://api.imgur.com/3/gallery/%s/%s/%d?showViral=%s"),

    // section/sort/time sort/page
    GALLERY_TOP("https://api.imgur.com/3/gallery/%s/%s/%s/%d"),

    // item id
    IMAGE_DETAILS("https://api.imgur.com/3/image/%s"),

    // albumid
    ALBUM("https://api.imgur.com/3/gallery/%s/images"),

    // Album or Image id
    GALLERY_ITEM_DETAILS("https://api.imgur.com/3/gallery/%s"),

    //cover id
    ALBUM_COVER("https://i.imgur.com/%s.jpg"),

    // albumid or imageid, sort
    COMMENTS("https://api.imgur.com/3/gallery/%s/comments/%s"),

    LOGIN("https://api.imgur.com/oauth2/authorize?client_id=" + ApiClient.CLIENT_ID + "&response_type=token"),

    REFRESH_TOKEN("https://api.imgur.com/oauth2/token"),

    // Username
    PROFILE("https://api.imgur.com/3/account/%s"),

    // Username/page
    ACCOUNT_FAVORITES("https://api.imgur.com/3/account/%s/favorites"),

    ACCOUNT_GALLERY_FAVORITES("https://api.imgur.com/3/account/%s/gallery_favorites/%d/newest"),

    // Username/Page
    ACCOUNT_SUBMISSIONS("https://api.imgur.com/3/account/%s/submissions/%d"),

    // Username/Sort/Page
    ACCOUNT_COMMENTS("https://api.imgur.com/3/account/%s/comments/%s/%s"),

    ACCOUNT_CONVOS("https://api.imgur.com/3/conversations"),

    // Username/Page
    ACCOUNT_IMAGES("https://api.imgur.com/3/account/%s/images/%d"),

    // comment id/vote
    COMMENT_VOTE("https://api.imgur.com/3/comment/%s/vote/%s"),

    // id,vote
    GALLERY_VOTE("https://api.imgur.com/3/gallery/%s/vote/%s"),

    // Image id
    FAVORITE_IMAGE("https://api.imgur.com/3/image/%s/favorite"),

    // Album id
    FAVORITE_ALBUM("https://api.imgur.com/3/album/%s/favorite"),

    // Album/Image id
    COMMENT("https://api.imgur.com/3/gallery/%s/comment"),

    // albumid or imageid / comment parent
    COMMENT_REPLY("https://api.imgur.com/3/gallery/%s/comment/%s"),

    // Subreddit,sort,sort date,page
    SUBREDDIT("https://api.imgur.com/3/gallery/r/%s/%s/%s/%d"),

    UPLOAD("https://api.imgur.com/3/upload"),

    // Image/Album id
    GALLERY_UPLOAD("https://api.imgur.com/3/gallery/%s"),

    // ConvoId, page
    MESSAGES("https://api.imgur.com/3/conversations/%s/%d/0"),

    // recipient
    SEND_MESSAGE("https://api.imgur.com/3/conversations/%s"),

    // Convo Id
    DELETE_CONVO("https://api.imgur.com/3/conversations/%s"),

    // Delete hash or image id if owned by account
    IMAGE_DELETE("https://api.imgur.com/3/image/%s"),

    // Page Number
    RANDOM("https://api.imgur.com/3/gallery/random/%d"),

    // Username
    CONVO_REPORT("https://api.imgur.com/3/conversations/report/%s"),

    CONVO_BLOCK("https://api.imgur.com/3/conversations/block/%s"),

    TOPICS_DEFAULTS("https://api.imgur.com/3/topics/defaults"),

    // topic id/sort/page
    TOPICS("https://api.imgur.com/3/topics/%d/%s/%d"),

    // topic id/sort/time sort/page
    TOPICS_TOP("https://api.imgur.com/3/topics/%d/%s/%s/%d"),

    MEME("https://api.imgur.com/3/memegen/defaults"),

    // sort, page, query
    GALLERY_SEARCH("https://api.imgur.com/3/gallery/search/%s/%d?q=%s"),

    // sort, window, page, query
    GALLERY_SEARCH_TOP("https://api.imgur.com/3/gallery/search/%s/%s/%d?q=%s"),

    // Tags
    TAGS("https://api.imgur.com/3/gallery/%s/tags"),

    ALBUM_CREATION("https://api.imgur.com/3/album"),

    // Username/Page
    USER_ALBUMS("https://api.imgur.com/3/account/%s/albums/%d"),

    // Albumid/delete hash
    ALBUM_DELETE("https://api.imgur.com/3/album/%s");

    private final String mUrl;

    Endpoints(String endpoint) {
        mUrl = endpoint;
    }

    public String getUrl() {
        return mUrl;
    }
}
