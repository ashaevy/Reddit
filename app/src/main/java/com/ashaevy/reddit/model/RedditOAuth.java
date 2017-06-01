package com.ashaevy.reddit.model;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * Created by ashaevy on 01.06.17.
 */

public interface RedditOAuth {
    @GET("/top/.json")
    @Headers({
            "User-Agent: org.quantumbadger.redreader/1.9.8"
    })
    Call<RedditListing> getRedditItems(@Query("t") String t,
                                       @Header("Authorization") String authorization,
                                       @Query("limit") int limit,
                                       @Query("after") String after);
}
