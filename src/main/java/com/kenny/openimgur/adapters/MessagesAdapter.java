package com.kenny.openimgur.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.kenny.openimgur.R;
import com.kenny.openimgur.classes.ImgurMessage;
import com.kenny.openimgur.classes.OpengurApp;

import java.util.List;

import butterknife.InjectView;

/**
 * Created by kcampagna on 6/18/15.
 */
public class MessagesAdapter extends BaseRecyclerAdapter<ImgurMessage> {
    private int mUserId;

    private int mUserColor;

    public MessagesAdapter(RecyclerView view, Context context, List<ImgurMessage> messages) {
        super(context, messages);
        OpengurApp app = OpengurApp.getInstance(context);
        Resources res = context.getResources();
        mUserColor = res.getColor(app.getImgurTheme().accentColor);
        mUserId = app.getUser().getId();
        int messageMargin = (int) (res.getDisplayMetrics().widthPixels * .25);
        int messageSpacing = res.getDimensionPixelSize(R.dimen.message_spacing);
        view.setLayoutManager(new LinearLayoutManager(context));
        view.addItemDecoration(new MessageItemDecoration(mUserId, messageMargin, messageSpacing));
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MessagesViewHolder(mInflater.inflate(R.layout.convo_message, parent, false));
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        MessagesViewHolder messageHolder = (MessagesViewHolder) holder;
        ImgurMessage message = getItem(position);

        messageHolder.message.setText(message.getBody());

        if (message.isSending()) {
            messageHolder.timeStamp.setText(R.string.convo_message_sending);
        } else if (message.getDate() > 0) {
            messageHolder.timeStamp.setText(getDateFormattedTime(message.getDate() * DateUtils.SECOND_IN_MILLIS, messageHolder.itemView.getContext()));
        } else {
            messageHolder.timeStamp.setText(R.string.convo_message_failed);
        }

        boolean isFromUser = message.getSenderId() == mUserId;
        int bgColor = isFromUser ? mUserColor : ColorGenerator.MATERIAL.getColor(message.getFrom());
        messageHolder.container.setGravity(isFromUser ? Gravity.RIGHT : Gravity.LEFT);
        messageHolder.itemView.setBackgroundColor(bgColor);
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

    /**
     * Updates the status of a sent message
     *
     * @param successful If the message was successfully sent
     * @param id         The id of the message
     */
    public void onMessageSendComplete(boolean successful, String id) {
        // The message will most likely be the last item in the list, or near the end
        for (int i = getItemCount() - 1; i >= 0; i--) {
            ImgurMessage message = getItem(i);

            if (message.getId().equals(id)) {
                message.setIsSending(false);
                if (!successful) message.setDate(-1L);
                notifyDataSetChanged();
                break;
            }
        }
    }

    static class MessagesViewHolder extends BaseViewHolder {
        @InjectView(R.id.messageContainer)
        LinearLayout container;

        @InjectView(R.id.message)
        TextView message;

        @InjectView(R.id.timeStamp)
        TextView timeStamp;

        public MessagesViewHolder(View view) {
            super(view);
        }

        public void configView(ImgurMessage imgurMessage, int margin, int userId) {
            boolean isUser = userId == imgurMessage.getSenderId();
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) container.getLayoutParams();

            if (isUser) {
                container.setBackgroundColor(Color.WHITE);
                lp.setMargins(margin, 0, 0, 0);
                lp.gravity = Gravity.RIGHT;
                container.setGravity(Gravity.RIGHT);
                container.setLayoutParams(lp);
                message.setTextColor(Color.BLACK);
                timeStamp.setTextColor(Color.BLACK);
            } else {
                container.setBackgroundColor(ColorGenerator.MATERIAL.getColor(imgurMessage.getFrom()));
                lp.setMargins(0, 0, margin, 0);
                lp.gravity = Gravity.LEFT;
                container.setGravity(Gravity.LEFT);
                container.setLayoutParams(lp);
                message.setTextColor(Color.WHITE);
                timeStamp.setTextColor(Color.WHITE);
            }
        }
    }

    static class MessageItemDecoration extends RecyclerView.ItemDecoration {

        private int mUserId;

        private int mMargin;

        private int mSpacing;

        public MessageItemDecoration(int id, int margin, int spacing) {
            mUserId = id;
            mMargin = margin;
            mSpacing = spacing;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getLayoutManager().getPosition(view);
            MessagesAdapter adapter = (MessagesAdapter) parent.getAdapter();
            ImgurMessage message = adapter.getItem(position);

            if (mUserId == message.getSenderId()) {
                outRect.set(mMargin, mSpacing, 0, mSpacing);
            } else {
                outRect.set(0, mSpacing, mMargin, mSpacing);
            }
        }

    }
}
