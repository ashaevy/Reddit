package com.ashaevy.reddit.model;

/**
 * Created by ashaevy on 01.06.17.
 */

public class RedditListing {
    private String kind;
    private RedditListingData data;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public RedditListingData getData() {
        return data;
    }

    public void setData(RedditListingData data) {
        this.data = data;
    }
}
