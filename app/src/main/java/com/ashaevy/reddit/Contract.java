package com.ashaevy.reddit;

import com.ashaevy.reddit.model.RedditItem;

import java.util.List;

/**
 * Created by ashaevy on 02.06.17.
 */

public interface Contract {
    interface View {
        void onUIStateChanged(ListStateFragment.ItemsListUIState uiState);
        void onFirstListItemsLoaded(List<RedditItem> items);
        void onNextListItemsLoaded(List<RedditItem> items);
    }
    interface Presenter {
        void setListStateListener(View view);
        void requestRedditItemsFirstFetch();
        void requestFirstFetchReloading();
        void requestNextFetchReloading();
        void loadMoreItems();
        boolean canHaveMoreItems();
        void requestRestoreState();
    }
}
