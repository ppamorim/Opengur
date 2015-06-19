package com.kenny.openimgur.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.kenny.openimgur.R;
import com.kenny.openimgur.classes.ImgurConvo;

import java.util.List;

import butterknife.InjectView;

/**
 * Created by kcampagna on 6/18/15.
 */
public class ConvoAdapter extends BaseRecyclerAdapter<ImgurConvo> {

    private int mCircleSize;

    private View.OnClickListener mOnClickListener;

    private View.OnLongClickListener mOnLongClickListener;

    public ConvoAdapter(Context context, List<ImgurConvo> convos, View.OnClickListener clickListener, View.OnLongClickListener longClickListener) {
        super(context, convos);
        mCircleSize = context.getResources().getDimensionPixelSize(R.dimen.avatar_size);
        mOnClickListener = clickListener;
        mOnLongClickListener = longClickListener;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ConvoViewHolder holder = new ConvoViewHolder(mInflater.inflate(R.layout.profile_comment_item, parent, false));
        holder.itemView.setOnClickListener(mOnClickListener);
        holder.itemView.setOnLongClickListener(mOnLongClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        ImgurConvo convo = getItem(position);
        ConvoViewHolder convoHolder = (ConvoViewHolder) holder;

        String firstLetter = convo.getWithAccount().substring(0, 1);
        int color = ColorGenerator.MATERIAL.getColor(convo.getWithAccount());

        convoHolder.image.setImageDrawable(
                TextDrawable.builder()
                        .beginConfig()
                        .toUpperCase()
                        .width(mCircleSize)
                        .height(mCircleSize)
                        .endConfig()
                        .buildRound(firstLetter, color));

        convoHolder.author.setText(convo.getWithAccount());
        convoHolder.message.setText(convo.getLastMessage());
    }

    public void removeItem(String id) {
        List<ImgurConvo> items = retainItems();

        for (ImgurConvo c : items) {
            if (c.getId().equals(id)) {
                removeItem(c);
                break;
            }
        }
    }

    static class ConvoViewHolder extends BaseRecyclerAdapter.BaseViewHolder {
        @InjectView(R.id.author)
        TextView author;

        @InjectView(R.id.comment)
        TextView message;

        @InjectView(R.id.image)
        ImageView image;

        public ConvoViewHolder(View view) {
            super(view);
        }
    }
}
