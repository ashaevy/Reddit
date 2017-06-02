package com.ashaevy.reddit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
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

import com.ashaevy.reddit.model.RedditItem;
import com.ashaevy.reddit.model.RedditPreview;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by ashaevy on 01.06.17.
 */

public class MainFragment extends
        Fragment implements BaseAdapter.OnItemClickListener, BaseAdapter.OnReloadClickListener,
        Contract.View {

    private static final String LIST_STATE_FRAGMENT_TAG = "LIST_STATE_FRAGMENT_TAG";
    private static final String BUNDLE_RECYCLER_LAYOUT = "MainFragment.recycler.layout";

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

    private Unbinder unbinder;

    private LinearLayoutManager layoutManager;
    private RedditItemsAdapter redditItemsAdapter;

    private Contract.Presenter presenter;

    private RecyclerView.OnScrollListener recyclerViewOnScrollListener =
            new RecyclerView.OnScrollListener() {
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

                    if (presenter.canHaveMoreItems()) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                                && firstVisibleItemPosition >= 0
                                && totalItemCount >= ListStateFragment.PAGE_SIZE) {
                            presenter.loadMoreItems();
                        }
                    }
                }
            };

    @OnClick(R.id.reload_btn)
    public void onReloadButtonClicked() {
        presenter.requestFirstFetchReloading();
    }

    @Override
    public void onUIStateChanged(ListStateFragment.ItemsListUIState uiState) {
        if (uiState.showFirstLoading) {
            loadingProgressBar.setVisibility(View.VISIBLE);
            errorLinearLayout.setVisibility(View.GONE);
        } else {
            loadingProgressBar.setVisibility(View.GONE);
        }
        if (uiState.showFirstLoadingError) {
            errorTextView.setText(getString(R.string.can_not_load_data));
            errorLinearLayout.setVisibility(View.VISIBLE);
            loadingProgressBar.setVisibility(View.GONE);
        } else {
            errorLinearLayout.setVisibility(View.GONE);
        }
        if (uiState.showLoadMoreItems) {
            redditItemsAdapter.updateFooter(RedditItemsAdapter.FooterType.LOAD_MORE);
        }
        if (uiState.showLoadMoreError) {
            redditItemsAdapter.updateFooter(RedditItemsAdapter.FooterType.ERROR);
        }
        if (uiState.showLoadMoreFooter) {
            if (!redditItemsAdapter.isFooterAdded()) {
                redditItemsAdapter.addFooter();
            }
        } else {
            if (redditItemsAdapter.isFooterAdded()) {
                redditItemsAdapter.removeFooter();
            }
        }
    }

    @Override
    public void onFirstListItemsLoaded(List<RedditItem> items) {
        redditItemsAdapter.addAll(items);
    }

    @Override
    public void onNextListItemsLoaded(List<RedditItem> items) {
        redditItemsAdapter.addAll(items);
    }

    @Override
    public void onItemClick(int position, View view) {
        RedditItem clickedItem = redditItemsAdapter.getItem(position);
        RedditPreview preview = clickedItem.getPreview();
        if (preview != null && preview.isEnabled()) {
            String imageUrl = preview.getImages().get(0).getSource().getUrl();
            Intent intent = new Intent(getActivity(), ImageActivity.class);
            intent.putExtra(ImageActivity.IMAGE_URL_KEY, imageUrl);
            startActivity(intent);
        }
    }

    @Override
    public void onReloadClick() {
        presenter.requestNextFetchReloading();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ListStateFragment listStateFragment = ((ListStateFragment) getFragmentManager().
                findFragmentByTag(LIST_STATE_FRAGMENT_TAG));
        if (listStateFragment == null) {
            listStateFragment = new ListStateFragment();
            getFragmentManager().beginTransaction().add(listStateFragment,
                    LIST_STATE_FRAGMENT_TAG).commit();
        }
        this.presenter = listStateFragment;

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable
            ViewGroup container, @Nullable Bundle savedInstanceState) {
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

        presenter.setListStateListener(this);
        if (savedInstanceState == null) {
            presenter.requestRedditItemsFirstFetch();
        } else {
            presenter.requestRestoreState();
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            Parcelable savedRecyclerLayoutState = savedInstanceState.
                    getParcelable(BUNDLE_RECYCLER_LAYOUT);
            recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, recyclerView.getLayoutManager().onSaveInstanceState());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.setListStateListener(null);
        removeListeners();
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void removeListeners() {
        recyclerView.removeOnScrollListener(recyclerViewOnScrollListener);
    }

}
