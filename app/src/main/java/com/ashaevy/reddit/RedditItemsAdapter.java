package com.ashaevy.reddit;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ashaevy.reddit.model.RedditItem;
import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RedditItemsAdapter extends BaseAdapter<RedditItem> {

    private FooterViewHolder footerViewHolder;

    public RedditItemsAdapter() {
        super();
    }

    @Override
    public int getItemViewType(int position) {
        return (isLastPosition(position) && isFooterAdded) ? FOOTER : ITEM;
    }

    @Override
    protected RecyclerView.ViewHolder createHeaderViewHolder(ViewGroup parent) {
        return null;
    }

    @Override
    protected RecyclerView.ViewHolder createItemViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.reddit_item_row, parent, false);

        final RedditItemViewHolder holder = new RedditItemViewHolder(v);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPos = holder.getAdapterPosition();
                if(adapterPos != RecyclerView.NO_POSITION){
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(adapterPos, holder.itemView);
                    }
                }
            }
        });

        return holder;
    }

    @Override
    protected RecyclerView.ViewHolder createFooterViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_footer, parent, false);

        final FooterViewHolder holder = new FooterViewHolder(v);
        holder.reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onReloadClickListener != null){
                    onReloadClickListener.onReloadClick();
                }
            }
        });

        return holder;
    }

    @Override
    protected void bindHeaderViewHolder(RecyclerView.ViewHolder viewHolder) {

    }

    @Override
    protected void bindItemViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final RedditItemViewHolder holder = (RedditItemViewHolder) viewHolder;

        final RedditItem redditItem = getItem(position);
        if (redditItem != null) {
            holder.bind(redditItem);
        }
    }

    @Override
    protected void bindFooterViewHolder(RecyclerView.ViewHolder viewHolder) {
        footerViewHolder = (FooterViewHolder) viewHolder;
    }

    @Override
    protected void displayLoadMoreFooter() {
        if(footerViewHolder!= null){
            footerViewHolder.errorRelativeLayout.setVisibility(View.GONE);
            footerViewHolder.loadingFrameLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void displayErrorFooter() {
        if(footerViewHolder!= null){
            footerViewHolder.loadingFrameLayout.setVisibility(View.GONE);
            footerViewHolder.errorRelativeLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void addFooter() {
        isFooterAdded = true;
        add(new RedditItem());
    }

    public static class RedditItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.thumbnail_iv)
        ImageView thumbnailImageView;
        @BindView(R.id.title_tv)
        TextView titleTextView;
        @BindView(R.id.author_tv)
        TextView authorTextView;
        @BindView(R.id.created_tv)
        TextView createdTextView;
        @BindView(R.id.num_comments_tv)
        TextView numCommentsTextView;

        public RedditItemViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        private void bind(RedditItem redditItem){
            setUpTV(titleTextView, redditItem.getTitle());
            setUpTV(authorTextView, "Author: " + redditItem.getAuthor());
            setUpTV(createdTextView, "Created: " + redditItem.getCreated());
            setUpTV(numCommentsTextView, "Comments: " + redditItem.getNumComments());
            setUpThumbnail(thumbnailImageView, redditItem);
        }

        private void setUpTV(TextView tv, String value) {
           if (!TextUtils.isEmpty(value)) {
                tv.setText(value);
            }
        }

        private void setUpThumbnail(ImageView iv, RedditItem redditItem) {
            String thumbnailUrl = redditItem.getThumbnail();
            if (!TextUtils.isEmpty(thumbnailUrl)) {
                Glide.with(iv.getContext())
                        .load(thumbnailUrl)
                        .into(iv);
            }
        }
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.loading_fl)
        FrameLayout loadingFrameLayout;
        @BindView(R.id.error_rl)
        RelativeLayout errorRelativeLayout;
        @BindView(R.id.reload_btn)
        Button reloadButton;

        public FooterViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}