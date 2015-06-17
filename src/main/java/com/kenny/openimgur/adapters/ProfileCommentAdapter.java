package com.kenny.openimgur.adapters;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kenny.openimgur.R;
import com.kenny.openimgur.api.Endpoints;
import com.kenny.openimgur.classes.ImgurComment;
import com.kenny.openimgur.classes.ImgurPhoto;
import com.kenny.openimgur.util.ImageUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.util.List;

import butterknife.InjectView;

/**
 * Created by kcampagna on 6/17/15.
 */
public class ProfileCommentAdapter extends BaseRecyclerAdapter<ImgurComment> {
    private int mPositiveColor;

    private int mNegativeColor;

    private View.OnClickListener mOnClickListener;

    public ProfileCommentAdapter(Context context, List<ImgurComment> comments, View.OnClickListener onClickListener) {
        super(context, comments, true);
        mPositiveColor = context.getResources().getColor(R.color.notoriety_positive);
        mNegativeColor = context.getResources().getColor(R.color.notoriety_negative);
        mOnClickListener = onClickListener;
    }

    @Override
    protected DisplayImageOptions getDisplayOptions() {
        return ImageUtil.getDisplayOptionsForComments().build();
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CommentViewHolder holder = new CommentViewHolder(mInflater.inflate(R.layout.profile_comment_item, parent, false));
        holder.itemView.setOnClickListener(mOnClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        CommentViewHolder commentHolder = (CommentViewHolder) holder;
        ImgurComment comment = getItem(position);
        String photoUrl;

        commentHolder.author.setText(constructSpan(comment, commentHolder.itemView.getContext()));
        commentHolder.comment.setText(comment.getComment());

        if (comment.isAlbumComment() && !TextUtils.isEmpty(comment.getAlbumCoverId())) {
            photoUrl = String.format(Endpoints.ALBUM_COVER.getUrl(), comment.getAlbumCoverId() + ImgurPhoto.THUMBNAIL_SMALL);
        } else {
            photoUrl = "https://imgur.com/" + comment.getImageId() + ImgurPhoto.THUMBNAIL_SMALL + ".jpeg";
        }

        displayImage(commentHolder.image, photoUrl);
    }

    /**
     * Creates the spannable object for the authors name, points, and time
     *
     * @param comment
     * @param context
     * @return
     */
    private Spannable constructSpan(ImgurComment comment, Context context) {
        CharSequence date = getDateFormattedTime(comment.getDate() * 1000L, context);
        String author = comment.getAuthor();
        StringBuilder sb = new StringBuilder(author);
        int spanLength = author.length();

        sb.append(" ")
                .append(comment.getPoints())
                .append(" ")
                .append(context.getString(R.string.points))
                .append(" : ")
                .append(date);

        Spannable span = new SpannableString(sb.toString());
        int color = comment.getPoints() < 0 ? mNegativeColor : mPositiveColor;
        span.setSpan(new ForegroundColorSpan(color), spanLength, sb.length() - date.length() - 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }

    private CharSequence getDateFormattedTime(long commentDate, Context context) {
        long now = System.currentTimeMillis();
        long difference = System.currentTimeMillis() - commentDate;

        return (difference >= 0 && difference <= DateUtils.MINUTE_IN_MILLIS) ?
                context.getResources().getString(R.string.moments_ago) :
                DateUtils.getRelativeTimeSpanString(
                        commentDate,
                        now,
                        DateUtils.MINUTE_IN_MILLIS,
                        DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_ABBREV_RELATIVE
                                | DateUtils.FORMAT_ABBREV_ALL);
    }

    static class CommentViewHolder extends BaseViewHolder {
        @InjectView(R.id.author)
        TextView author;

        @InjectView(R.id.comment)
        TextView comment;

        @InjectView(R.id.image)
        ImageView image;

        public CommentViewHolder(View view) {
            super(view);
        }
    }
}
