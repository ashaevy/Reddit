package com.ashaevy.reddit.model;

import java.util.List;

/**
 * Created by ashaevy on 02.06.17.
 */

public class RedditPreview {

    private List<RedditImage> images;
    private boolean enabled;

    public List<RedditImage> getImages() {
        return images;
    }

    public void setImages(List<RedditImage> images) {
        this.images = images;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public static class RedditImage {
        private RedditImageSource source;

        public RedditImageSource getSource() {
            return source;
        }

        public void setSource(RedditImageSource source) {
            this.source = source;
        }
    }

    public static class RedditImageSource {
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

}
