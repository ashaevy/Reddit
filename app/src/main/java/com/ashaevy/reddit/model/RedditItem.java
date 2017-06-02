package com.ashaevy.reddit.model;

import com.google.gson.annotations.SerializedName;

public class RedditItem {

    private String title;
    private String author;
    private String thumbnail;
    @SerializedName("created_utc")
    private long createdUTC;
    @SerializedName("num_comments")
    private long numComments;
    private RedditPreview preview;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public long getCreatedUTC() {
        return createdUTC;
    }

    public void setCreatedUTC(long createdUTC) {
        this.createdUTC = createdUTC;
    }

    public long getNumComments() {
        return numComments;
    }

    public void setNumComments(long numComments) {
        this.numComments = numComments;
    }

    public RedditPreview getPreview() {
        return preview;
    }

    public void setPreview(RedditPreview preview) {
        this.preview = preview;
    }
}