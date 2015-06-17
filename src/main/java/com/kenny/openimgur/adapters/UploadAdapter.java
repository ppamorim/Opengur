package com.kenny.openimgur.adapters;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kenny.openimgur.R;
import com.kenny.openimgur.classes.ImgurPhoto;
import com.kenny.openimgur.classes.UploadedPhoto;
import com.kenny.openimgur.util.ImageUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.util.List;

import butterknife.InjectView;

/**
 * Created by kcampagna on 6/17/15.
 */
public class UploadAdapter extends BaseRecyclerAdapter<UploadedPhoto> {
    private View.OnClickListener mOnClickListener;

    private View.OnLongClickListener mOnLongClickListener;

    public UploadAdapter(Context context, RecyclerView view, List<UploadedPhoto> photos, View.OnClickListener onClick, View.OnLongClickListener onLongClick) {
        super(context, photos, true);
        int gridSize = context.getResources().getInteger(R.integer.gallery_num_columns);
        view.setLayoutManager(new GridLayoutManager(context, gridSize));
        mOnClickListener = onClick;
        mOnLongClickListener = onLongClick;
    }

    @Override
    protected DisplayImageOptions getDisplayOptions() {
        return ImageUtil.getDisplayOptionsForGallery().build();
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        UploadHolder holder = new UploadHolder(mInflater.inflate(R.layout.gallery_item, parent, false));
        holder.itemView.setOnClickListener(mOnClickListener);
        holder.itemView.setOnLongClickListener(mOnLongClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        UploadHolder uploadHolder = (UploadHolder) holder;
        UploadedPhoto photo = getItem(position);
        displayImage(uploadHolder.image, ImageUtil.getThumbnail(photo.getUrl(), ImgurPhoto.THUMBNAIL_GALLERY));
    }

    static class UploadHolder extends BaseRecyclerAdapter.BaseViewHolder {
        @InjectView(R.id.image)
        ImageView image;

        @InjectView(R.id.score)
        TextView score;

        public UploadHolder(View view) {
            super(view);
            score.setVisibility(View.GONE);
        }
    }
}
