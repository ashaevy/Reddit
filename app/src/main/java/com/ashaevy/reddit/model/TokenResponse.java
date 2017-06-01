package com.ashaevy.reddit.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ashaevy on 01.06.17.
 */

public class TokenResponse {
    @SerializedName("access_token")
    private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
