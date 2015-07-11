package com.kenny.openimgur.classes;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.kenny.openimgur.api.Endpoints;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kcampagna on 7/11/15.
 */
public class ImgurAlbum2 extends ImgurBaseObject2 {

    @SerializedName("cover")
    private String mCoverId;

    // TODO Set this
    private String mCoverUrl;

    @SerializedName("cover_width")
    private int mCoverWidth;

    @SerializedName("cover_height")
    private int mCoverHeight;

    private List<ImgurPhoto> mAlbumPhotos;

    public ImgurAlbum2(String id, String title, String link) {
        super(id, title, link);
    }

    private ImgurAlbum2(Parcel in) {
        super(in);
        mCoverId = in.readString();
        mCoverUrl = in.readString();
        mCoverWidth = in.readInt();
        mCoverHeight = in.readInt();
        mAlbumPhotos = new ArrayList<>();
        in.readTypedList(mAlbumPhotos, ImgurPhoto.CREATOR);
    }

    public void setCoverId(String id) {
        mCoverId = id;
    }

    public String getCoverId() {
        return mCoverId;
    }

    public int getCoverWidth() {
        return mCoverWidth;
    }

    public int getCoverHeight() {
        return mCoverHeight;
    }

    /**
     * Returns the cover image url of an album
     *
     * @param size Optional parameter of size
     * @return
     */
    public String getCoverUrl(@Nullable String size) {
        if (TextUtils.isEmpty(size)) {
            return String.format(Endpoints.ALBUM_COVER.getUrl(), mCoverId);
        } else {
            return String.format(Endpoints.ALBUM_COVER.getUrl(), mCoverId + size);
        }
    }

    /**
     * Adds a photo to the album
     *
     * @param photo
     */
    public void addPhotoToAlbum(ImgurPhoto photo) {
        if (mAlbumPhotos == null) {
            mAlbumPhotos = new ArrayList<>();
        }

        mAlbumPhotos.add(photo);
    }

    public void addPhotosToAlbum(List<ImgurPhoto> photos) {
        if (mAlbumPhotos == null) {
            mAlbumPhotos = photos;
        } else {
            mAlbumPhotos.addAll(photos);
        }
    }

    public List<ImgurPhoto> getAlbumPhotos() {
        return mAlbumPhotos;
    }

    /**
     * Clears the Album of all its photos
     */
    public void clearAlbum() {
        if (mAlbumPhotos != null) {
            mAlbumPhotos.clear();
        }
    }

    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(mCoverId);
        out.writeString(mCoverUrl);
        out.writeInt(mCoverWidth);
        out.writeInt(mCoverHeight);
        out.writeTypedList(mAlbumPhotos);
    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ImgurAlbum2> CREATOR = new Parcelable.Creator<ImgurAlbum2>() {
        public ImgurAlbum2 createFromParcel(Parcel in) {
            return new ImgurAlbum2(in);
        }

        public ImgurAlbum2[] newArray(int size) {
            return new ImgurAlbum2[size];
        }
    };

}