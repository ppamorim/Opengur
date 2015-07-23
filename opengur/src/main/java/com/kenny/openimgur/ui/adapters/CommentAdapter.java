package com.kenny.openimgur.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.kenny.openimgur.R;
import com.kenny.openimgur.classes.CustomLinkMovement;
import com.kenny.openimgur.classes.ImgurComment;
import com.kenny.openimgur.classes.ImgurListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import butterknife.InjectView;

/**
 * Created by kcampagna on 6/10/15.
 */
public class CommentAdapter extends BaseRecyclerAdapter<ImgurComment> {
    private static final float EXPANDED = 135.0f;

    private static final float COLLASPED = 0.0f;

    private static final Pattern sUserPattern = Pattern.compile("@\\w+");

    private ImgurListener mListener;

    private int mSelectedIndex = -1;

    private String mOP;

    private final Set<ImgurComment> mExpandedComments = new HashSet<>();

    private final LongSparseArray<Integer> mIndicatorMultiples = new LongSparseArray<>();

    private int mGreenTextColor;

    private int mRedTextColor;

    private int mCommentIndent;

    public CommentAdapter(Context context, List<ImgurComment> comments, ImgurListener listener) {
        super(context, comments);
        mListener = listener;
        Resources res = context.getResources();
        mGreenTextColor = res.getColor(R.color.notoriety_positive);
        mRedTextColor = res.getColor(R.color.notoriety_negative);
        mCommentIndent = res.getDimensionPixelSize(R.dimen.comment_padding);
    }

    /**
     * Removes all items from list and ImgurListener is removed
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        clear();
        mListener = null;
    }

    public void clearExpansionInfo() {
        mExpandedComments.clear();
        mIndicatorMultiples.clear();
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final CommentViewHolder holder = new CommentViewHolder(mInflater.inflate(R.layout.comment_item, parent, false));
        holder.comment.setMovementMethod(CustomLinkMovement.getInstance(mListener));
        holder.replies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) mListener.onViewRepliesTap(holder.itemView);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This will trigger the callback for list item click
                if (mListener != null) mListener.onLinkTap(holder.itemView, null);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        holder.itemView.setTag(holder);
        CommentViewHolder commentHolder = (CommentViewHolder) holder;
        final ImgurComment comment = getItem(position);

        commentHolder.comment.setText(comment.getComment());
        commentHolder.author.setText(constructSpan(comment, commentHolder.author.getContext()));
        Linkify.addLinks(commentHolder.comment, Linkify.WEB_URLS);
        Linkify.addLinks(commentHolder.comment, sUserPattern, null);
        commentHolder.replies.setVisibility(comment.getReplyCount() > 0 ? View.VISIBLE : View.GONE);
        boolean isExpanded = mExpandedComments.contains(comment);
        commentHolder.replies.setRotation(isExpanded ? EXPANDED : COLLASPED);
        commentHolder.indicator.setVisibility(comment.getParentId() > 0 ? View.VISIBLE : View.GONE);

        Integer multiple = mIndicatorMultiples.get(comment.getParentId());
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) commentHolder.itemView.getLayoutParams();

        if (multiple != null) {
            lp.setMargins(multiple * mCommentIndent, 0, 0, 0);
        } else {
            lp.setMargins(0, 0, 0, 0);
        }

        int bgColor = position == mSelectedIndex ?
                commentHolder.itemView.getResources().getColor(R.color.comment_bg_selected) :
                commentHolder.itemView.getResources().getColor(android.R.color.transparent);
        commentHolder.itemView.setBackgroundColor(bgColor);
    }

    /**
     * Creates the spannable object for the authors name, points, and time
     *
     * @param comment
     * @param context
     * @return
     */
    private Spannable constructSpan(ImgurComment comment, Context context) {
        CharSequence date = getDateFormattedTime(comment.getDate() * DateUtils.SECOND_IN_MILLIS, context);
        String author = comment.getAuthor();
        StringBuilder sb = new StringBuilder(author);
        boolean isOp = isOP(author);
        int spanLength = author.length();
        String points = context.getResources().getQuantityString(R.plurals.comment_points, (int) comment.getPoints(), comment.getPoints());
        int scoreTextLength = points.length();

        if (isOp) {
            // TODO Other languages for OP?
            sb.append(" OP");
            spanLength += 3;
        }

        sb.append(": ").append(points).append(" ").append(date);
        Spannable span = new SpannableString(sb.toString());
        int scoreColor = comment.getPoints() >= 0 ? mGreenTextColor : mRedTextColor;

        if (isOp) {
            span.setSpan(new ForegroundColorSpan(mGreenTextColor), author.length() + 1, spanLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        int scoreStart = author.length() + 2;
        if (isOp) scoreStart += +3;
        span.setSpan(new ForegroundColorSpan(scoreColor), scoreStart, scoreStart + scoreTextLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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

    public void setOP(String op) {
        mOP = op;
    }

    public void expandComments(View view, int position) {
        ImgurComment comment = getItem(position);
        if (comment.getReplyCount() <= 0) return;

        // Should always be the case
        if (view.getTag() instanceof CommentViewHolder) {
            ((CommentViewHolder) view.getTag()).replies.animate().rotation(EXPANDED);
        }

        mExpandedComments.add(comment);
        Integer multiple = mIndicatorMultiples.get(comment.getParentId());

        if (multiple == null) {
            if (comment.getParentId() > 0) mIndicatorMultiples.put(comment.getParentId(), 1);
            mIndicatorMultiples.put(Long.valueOf(comment.getId()), 1);
        } else {
            mIndicatorMultiples.put(Long.valueOf(comment.getId()), multiple + 1);
        }

        addItems(comment.getReplies(), position + 1);
    }

    public void collapseComments(View view, int position) {
        ImgurComment comment = getItem(position);
        if (comment.getReplyCount() <= 0) return;

        // Should always be the case
        if (view.getTag() instanceof CommentViewHolder) {
            ((CommentViewHolder) view.getTag()).replies.animate().rotation(COLLASPED);
        }

        position++;
        mExpandedComments.remove(comment);
        int endPosition = -1;

        for (int i = position; i < getItemCount(); i++) {
            ImgurComment c = getItem(i);

            if (c.getParentId() == comment.getParentId()) {
                endPosition = i;
                break;
            } else if (mExpandedComments.contains(c)) {
                // Remove any expanded comments
                mExpandedComments.remove(c);
            }
        }


        // Didn't find a comment parent to find the ending position, need to traverse the list and find comments to close
        if (endPosition == -1) {
            // Find the best parent to collapse to
            for (int i = position - 2; i >= 0; i--) {
                ImgurComment possibleParent = getItem(i);
                // Now do the same steps as above to get a matching parent
                for (int x = position; x < getItemCount(); x++) {
                    ImgurComment c = getItem(x);
                    if (c.getParentId() == possibleParent.getParentId()) {
                        endPosition = x;
                        break;
                    } else if (mExpandedComments.contains(c)) {
                        // Remove any expanded comments
                        mExpandedComments.remove(c);
                    }
                }

                if (endPosition != -1) break;
            }
        }

        // Still didn't find anything, just collapse the number of replies it has as a fail safe
        if (endPosition == -1) endPosition = position + comment.getReplyCount();
        removeItems(position, endPosition);
    }

    /**
     * Sets the currently selected item. If the item selected is the one that is already selected, it is deselected
     *
     * @param index
     * @return If the selected item was already selected
     */
    public boolean setSelectedIndex(int index) {
        boolean wasSelected = mSelectedIndex == index;
        mSelectedIndex = wasSelected ? -1 : index;
        notifyDataSetChanged();

        return wasSelected;
    }

    /**
     * Returns if the current comment is expanded
     *
     * @param position
     * @return
     */
    public boolean isExpanded(int position) {
        return mExpandedComments.contains(getItem(position));
    }

    private boolean isOP(String user) {
        return !TextUtils.isEmpty(mOP) && mOP.equals(user);
    }

    public static class CommentViewHolder extends BaseViewHolder {
        @InjectView(R.id.author)
        TextView author;

        @InjectView(R.id.comment)
        TextView comment;

        @InjectView(R.id.replies)
        ImageButton replies;

        @InjectView(R.id.indicator)
        View indicator;

        public CommentViewHolder(View view) {
            super(view);
        }
    }
}
