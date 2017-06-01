package com.ashaevy.reddit.model;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by ashaevy on 01.06.17.
 */

public interface RedditAPI {
    @POST("api/v1/access_token")
    @FormUrlEncoded
    @Headers({
            "Content-Type: application/x-www-form-urlencoded"
    })
    Call<TokenResponse> accessToken(@Field("grant_type") String grantType,
                                    @Field("device_id") String deviceId,
                                    @Header("Authorization") String authorization);
}
