package com.ashaevy.reddit;

import android.support.v4.app.Fragment;

import com.ashaevy.reddit.model.RedditAPI;
import com.ashaevy.reddit.model.RedditItem;
import com.ashaevy.reddit.model.RedditListing;
import com.ashaevy.reddit.model.RedditListingData;
import com.ashaevy.reddit.model.RedditOAuth;
import com.ashaevy.reddit.model.TokenResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by ashaevy on 02.06.17.
 */

public class ListStateFragment extends Fragment implements Contract.Presenter {

    public static final int PAGE_SIZE = 10;

    public static final String REDDIT_API_BASE_URL = "https://www.reddit.com/";
    public static final String REDDIT_OAUTH_BASE_URL = "https://oauth.reddit.com/";

    public static final String FLAG_TOP_OF_DAY = "day";

    public static final String ANONYMOUS_AUTHORIZATION = "Basic bV96Q1cxRGl4czlXTEE6";
    public static final String ANONYMOUS_DEVICE = "DO_NOT_TRACK_THIS_DEVICE";
    public static final String GRANT_TYPE = "https://oauth.reddit.com/grants/installed_client";

    // if there are no more items
    private boolean isLastPage = false;
    // loading more items in progress
    private boolean isLoading = false;

    private String after;

    private RedditAPI redditAPIService;
    private RedditOAuth retrofitOAuth;

    private List<RedditItem> redditItems = new ArrayList<>();

    private Contract.View listStateListener;
    private ItemsListUIState uiState = new ItemsListUIState();

    private boolean isFirstFetchCalled = false;

    public static class ItemsListUIState {
        public boolean showLoadMoreFooter = false;
        public boolean showLoadMoreItems = false;
        public boolean showFirstLoading = true;
        public boolean showFirstLoadingError = false;
        public boolean showLoadMoreError = false;
    }

    public ListStateFragment() {
        setRetainInstance(true);
        createRetrofitAPI();
        createRetrofitOAuth();
    }

    @Override
    public void setListStateListener(Contract.View listStateListener) {
        this.listStateListener = listStateListener;
    }

    private void createRetrofitAPI() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(REDDIT_API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        redditAPIService = retrofit.create(RedditAPI.class);
    }

    private void createRetrofitOAuth() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(REDDIT_OAUTH_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitOAuth = retrofit.create(RedditOAuth.class);
    }

    @Override
    public void requestRedditItemsFirstFetch() {
        isFirstFetchCalled = true;
        requestRedditItemsFetch(redditItemsFirstFetchCallback, reportFirstFetchError());
    }

    public void requestRedditItemsNextFetch() {
        requestRedditItemsFetch(redditItemsNextFetchCallback, reportNextFetchError());
    }

    private void requestRedditItemsFetch(final Callback<RedditListing> callback,
                                         final Runnable failureCallback) {
        redditAPIService.accessToken(GRANT_TYPE,
                ANONYMOUS_DEVICE, ANONYMOUS_AUTHORIZATION).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                String accessToken = response.body().getAccessToken();
                Call<RedditListing> getRedditItemsCall = retrofitOAuth.
                        getRedditItems(FLAG_TOP_OF_DAY, "bearer " + accessToken, PAGE_SIZE, after);
                getRedditItemsCall.enqueue(callback);
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                failureCallback.run();
            }
        });
    }

    private Callback<RedditListing> redditItemsFirstFetchCallback = new Callback<RedditListing>() {
        @Override
        public void onResponse(Call<RedditListing> call, Response<RedditListing> response) {
            uiState.showFirstLoading = false;
            notifyUIStateChanged();
            isLoading = false;

            if (!response.isSuccessful()) {
                int responseCode = response.code();
                if(responseCode == 504) { // 504 Unsatisfiable Request (only-if-cached)
                    uiState.showFirstLoadingError = true;
                    notifyUIStateChanged();
                }
                return;
            }

            RedditListing redditListing = response.body();
            if (redditListing != null) {

                after = redditListing.getData().getAfter();

                List<RedditListingData.RedditListingChild> redditListingChildren =
                        redditListing.getData().getChildren();
                if (redditListingChildren != null) {
                    if(redditListingChildren.size() > 0) {
                        List<RedditItem> newItems = new ArrayList<>();
                        for (RedditListingData.RedditListingChild child: redditListingChildren) {
                            newItems.add(child.getData());
                        }
                        redditItems.addAll(newItems);
                        notifyFirstItemsLoaded(newItems);
                    }

                    if (redditListingChildren.size() >= PAGE_SIZE) {
                        uiState.showLoadMoreFooter = true;
                        notifyUIStateChanged();
                    } else {
                        isLastPage = true;
                    }
                }
            }
        }

        @Override
        public void onFailure(Call<RedditListing> call, Throwable t) {
            if (!call.isCanceled()) {
                reportFirstFetchError();
            }
        }
    };

    private void notifyFirstItemsLoaded(List<RedditItem> newItems) {
        if (listStateListener != null) {
            listStateListener.onFirstListItemsLoaded(newItems);
        }
    }

    private Runnable reportFirstFetchError() {
        return new Runnable() {
            @Override
            public void run() {
                isLoading = false;
                uiState.showFirstLoading = false;
                uiState.showFirstLoadingError = true;
                notifyUIStateChanged();
            }
        };
    }

    private void notifyUIStateChanged() {
        if (listStateListener != null) {
            listStateListener.onUIStateChanged(uiState);
        }
    }

    private Callback<RedditListing> redditItemsNextFetchCallback = new Callback<RedditListing>() {
        @Override
        public void onResponse(Call<RedditListing> call, Response<RedditListing> response) {
            uiState.showLoadMoreFooter = false;
            notifyUIStateChanged();
            isLoading = false;

            if (!response.isSuccessful()) {
                int responseCode = response.code();
                switch (responseCode){
                    case 504: // 504 Unsatisfiable Request (only-if-cached)
                        break;
                    case 400:
                        isLastPage = true;
                        break;
                }
                return;
            }

            RedditListing redditListing = response.body();
            if (redditListing != null) {

                after = redditListing.getData().getAfter();

                List<RedditListingData.RedditListingChild> redditListingChildren =
                        redditListing.getData().getChildren();
                if (redditListingChildren != null) {
                    if(redditListingChildren.size() > 0) {
                        List<RedditItem> newItems = new ArrayList<>();
                        for (RedditListingData.RedditListingChild child: redditListingChildren) {
                            newItems.add(child.getData());
                        }
                        redditItems.addAll(newItems);
                        notifyNextListItemsLoaded(newItems);
                    }

                    if (redditListingChildren.size() >= PAGE_SIZE) {
                        uiState.showLoadMoreFooter = true;
                        notifyUIStateChanged();
                    } else {
                        isLastPage = true;
                    }
                }
            }
        }

        @Override
        public void onFailure(Call<RedditListing> call, Throwable t) {
            if (!call.isCanceled()){
                reportNextFetchError().run();
            }
        }
    };

    private void notifyNextListItemsLoaded(List<RedditItem> newItems) {
        if (listStateListener != null) {
            listStateListener.onNextListItemsLoaded(newItems);
        }
    }

    private Runnable reportNextFetchError() {
        return new Runnable() {
            @Override
            public void run() {
                uiState.showLoadMoreError = true;
                uiState.showLoadMoreItems = false;
                notifyUIStateChanged();
            }
        };
    }

    @Override
    public void requestFirstFetchReloading() {
        uiState.showFirstLoadingError = false;
        uiState.showFirstLoading = true;
        notifyUIStateChanged();
        requestRedditItemsFetch(redditItemsFirstFetchCallback, reportFirstFetchError());
    }

    @Override
    public void requestNextFetchReloading() {
        uiState.showLoadMoreError = false;
        uiState.showLoadMoreItems = true;
        notifyUIStateChanged();
        requestRedditItemsFetch(redditItemsNextFetchCallback, reportNextFetchError());
    }

    @Override
    public boolean canHaveMoreItems() {
        return !isLoading && !isLastPage;
    }

    @Override
    public void loadMoreItems() {
        isLoading = true;
        requestRedditItemsNextFetch();
    }

    @Override
    public void requestRestoreState() {
        if (!isFirstFetchCalled) {
            requestRedditItemsFirstFetch();
            return;
        }
        notifyFirstItemsLoaded(redditItems);
        notifyUIStateChanged();
    }
}