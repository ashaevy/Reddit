package com.ashaevy.reddit.model;

import java.util.List;

/**
 * Created by ashaevy on 01.06.17.
 */

public class RedditListingData {
    private List<RedditListingChild> children;
    private String after;
    private String before;

    public List<RedditListingChild> getChildren() {
        return children;
    }

    public void setChildren(List<RedditListingChild> children) {
        this.children = children;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public String getBefore() {
        return before;
    }

    public void setBefore(String before) {
        this.before = before;
    }

    /**
     * Created by ashaevy on 01.06.17.
     */
    public static class RedditListingChild {
        private String kind;
        private RedditItem data;

        public String getKind() {
            return kind;
        }

        public void setKind(String kind) {
            this.kind = kind;
        }

        public RedditItem getData() {
            return data;
        }

        public void setData(RedditItem data) {
            this.data = data;
        }
    }
}
