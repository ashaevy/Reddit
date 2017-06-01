package com.ashaevy.reddit;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ashaevy.reddit.model.RedditAPI;
import com.ashaevy.reddit.model.RedditListing;
import com.ashaevy.reddit.model.RedditListingData;
import com.ashaevy.reddit.model.RedditOAuth;
import com.ashaevy.reddit.model.TokenResponse;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by ashaevy on 01.06.17.
 */

public class MainFragment extends Fragment implements BaseAdapter.OnItemClickListener, BaseAdapter.OnReloadClickListener {

    public static final int PAGE_SIZE = 10;

    public static final String REDDIT_API_BASE_URL = "https://www.reddit.com/";
    public static final String REDDIT_OAUTH_BASE_URL = "https://oauth.reddit.com/";

    public static final String FLAG_TOP_OF_DAY = "day";

    public static final String ANONYMOUS_AUTHORIZATION = "Basic bV96Q1cxRGl4czlXTEE6";
    public static final String ANONYMOUS_DEVICE = "DO_NOT_TRACK_THIS_DEVICE";
    public static final String GRANT_TYPE = "https://oauth.reddit.com/grants/installed_client";

    @BindView(R.id.rv)
    RecyclerView recyclerView;
    @BindView(R.id.loading_pb)
    ProgressBar loadingProgressBar;
    @BindView(R.id.error_ll)
    LinearLayout errorLinearLayout;
    @BindView(R.id.error_tv)
    TextView errorTextView;
    @BindView(R.id.reload_btn)
    Button reloadButton;

    private LinearLayoutManager layoutManager;
    private RedditItemsAdapter redditItemsAdapter;

    private boolean isLastPage = false;
    private boolean isLoading = false;

    private Unbinder unbinder;

    private String after;



    private RecyclerView.OnScrollListener recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = layoutManager.getChildCount();
            int totalItemCount = layoutManager.getItemCount();
            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

            if (!isLoading && !isLastPage) {
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= PAGE_SIZE) {
                    loadMoreItems();
                }
            }
        }
    };
    private RedditAPI redditAPIService;
    private RedditOAuth retrofitOAuth;

    @OnClick(R.id.reload_btn)
    public void onReloadButtonClicked() {
        errorLinearLayout.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.VISIBLE);
        requestRedditItemsFetch(redditItemsFirstFetchCallback, reportFirstFetchError());
    }

    private Callback<RedditListing> redditItemsFirstFetchCallback = new Callback<RedditListing>() {
        @Override
        public void onResponse(Call<RedditListing> call, Response<RedditListing> response) {
            loadingProgressBar.setVisibility(View.GONE);
            isLoading = false;

            if (!response.isSuccessful()) {
                int responseCode = response.code();
                if(responseCode == 504) { // 504 Unsatisfiable Request (only-if-cached)
                    errorTextView.setText(getString(R.string.can_not_load_data));
                    errorLinearLayout.setVisibility(View.VISIBLE);
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
                        for (RedditListingData.RedditListingChild child: redditListingChildren) {
                            redditItemsAdapter.add(child.getData());
                        }
                    }

                    if (redditListingChildren.size() >= PAGE_SIZE) {
                        redditItemsAdapter.addFooter();
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

    private Runnable reportFirstFetchError() {
        return new Runnable() {
            @Override
            public void run() {
                isLoading = false;
                loadingProgressBar.setVisibility(View.GONE);

                errorTextView.setText(getString(R.string.can_not_load_data));
                errorLinearLayout.setVisibility(View.VISIBLE);
            }
        };
    }

    private Runnable reportNextFetchError() {
        return new Runnable() {
            @Override
            public void run() {
                redditItemsAdapter.updateFooter(RedditItemsAdapter.FooterType.ERROR);
            }
        };
    }


    private Callback<RedditListing> redditItemsNextFetchCallback = new Callback<RedditListing>() {
        @Override
        public void onResponse(Call<RedditListing> call, Response<RedditListing> response) {
            redditItemsAdapter.removeFooter();
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
                        for (RedditListingData.RedditListingChild child: redditListingChildren) {
                            redditItemsAdapter.add(child.getData());
                        }
                    }

                    if (redditListingChildren.size() >= PAGE_SIZE) {
                        redditItemsAdapter.addFooter();
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

    @Override
    public void onItemClick(int position, View view) {

    }

    @Override
    public void onReloadClick() {
        redditItemsAdapter.updateFooter(RedditItemsAdapter.FooterType.LOAD_MORE);
        requestRedditItemsFetch(redditItemsNextFetchCallback, reportNextFetchError());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createRetrofitAPI();
        createRetrofitOAuth();
        setHasOptionsMenu(true);
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        redditItemsAdapter = new RedditItemsAdapter();
        redditItemsAdapter.setOnItemClickListener(this);
        redditItemsAdapter.setOnReloadClickListener(this);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(redditItemsAdapter);

        // Pagination
        recyclerView.addOnScrollListener(recyclerViewOnScrollListener);

        requestRedditItemsFetch(redditItemsFirstFetchCallback, reportFirstFetchError());
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeListeners();
        after = null;
        unbinder.unbind();
    }

    private void loadMoreItems() {
        isLoading = true;
        requestRedditItemsFetch(redditItemsNextFetchCallback, reportNextFetchError());
    }

    private void removeListeners(){
        recyclerView.removeOnScrollListener(recyclerViewOnScrollListener);
    }

}
