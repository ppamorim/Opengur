package com.kenny.openimgur.fragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.kenny.openimgur.R;
import com.kenny.openimgur.adapters.TopicsAdapter;
import com.kenny.openimgur.api.ApiClient;
import com.kenny.openimgur.api.Endpoints;
import com.kenny.openimgur.api.ImgurBusEvent;
import com.kenny.openimgur.classes.ImgurFilters;
import com.kenny.openimgur.classes.ImgurHandler;
import com.kenny.openimgur.classes.ImgurTopic;
import com.kenny.openimgur.ui.MultiStateView;
import com.kenny.openimgur.util.LogUtil;
import com.kenny.openimgur.util.ViewUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.util.ThrowableFailureEvent;

/**
 * Created by kcampagna on 2/21/15.
 */
public class TopicsFilterFragment extends BaseFragment implements SeekBar.OnSeekBarChangeListener {
    private static final String KEY_TOPIC = "topic";

    private static final String KEY_SORT = "topic_sort";

    private static final String KEY_TIME_SORT = "topic_time_sort";

    @InjectView(R.id.multiView)
    MultiStateView mMultiStateView;

    @InjectView(R.id.day)
    TextView mDay;

    @InjectView(R.id.week)
    TextView mWeek;

    @InjectView(R.id.month)
    TextView mMonth;

    @InjectView(R.id.year)
    TextView mYear;

    @InjectView(R.id.all)
    TextView mAll;

    @InjectView(R.id.time)
    TextView mTime;

    @InjectView(R.id.optionalSort)
    TextView mHighestScoring;

    @InjectView(R.id.viral)
    TextView mViral;

    @InjectView(R.id.topicSpinner)
    Spinner mSpinner;

    @InjectView(R.id.sortSeekBar)
    SeekBar mSortSeekBar;

    @InjectView(R.id.dateSeekBar)
    SeekBar mDateSeekBar;

    @InjectView(R.id.dateRangeContainer)
    View mDateContainer;

    private FilterListener mListener;

    public static TopicsFilterFragment createInstance(@Nullable ImgurTopic topic, @NonNull ImgurFilters.GallerySort sort, @NonNull ImgurFilters.TimeSort timeSort) {
        TopicsFilterFragment fragment = new TopicsFilterFragment();
        Bundle args = new Bundle(3);
        args.putParcelable(KEY_TOPIC, topic);
        args.putString(KEY_SORT, sort.getSort());
        args.putString(KEY_TIME_SORT, timeSort.getSort());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter_topics, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args == null || args.isEmpty()) throw new IllegalStateException("No arguments passed to filter");
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this);

        configToolBar((Toolbar) view.findViewById(R.id.toolBar));
        ImgurFilters.GallerySort sort = ImgurFilters.GallerySort.getSortFromString(args.getString(KEY_SORT, null));
        ImgurFilters.TimeSort timeSort = ImgurFilters.TimeSort.getSortFromString(args.getString(KEY_TIME_SORT, null));
        ImgurTopic topic = args.getParcelable(KEY_TOPIC);

        switch (sort) {
            case HIGHEST_SCORING:
                mHighestScoring.setTextColor(getResources().getColor(theme.accentColor));
                mSortSeekBar.setProgress(50);
                mHighestScoring.setTypeface(null, Typeface.BOLD);
                break;

            case TIME:
                mDay.setTextColor(getResources().getColor(theme.accentColor));
                mSortSeekBar.setProgress(0);
                mDay.setTypeface(null, Typeface.BOLD);
                mDateContainer.setAlpha(0);
                break;

            default:
            case VIRAL:
                mSortSeekBar.setProgress(100);
                mViral.setTextColor(getResources().getColor(theme.accentColor));
                mViral.setTypeface(null, Typeface.BOLD);
                mDateContainer.setAlpha(0);
                break;
        }

        switch (timeSort) {
            default:
            case DAY:
                mDateSeekBar.setProgress(0);
                break;

            case WEEK:
                mDateSeekBar.setProgress(20);
                break;

            case MONTH:
                mDateSeekBar.setProgress(40);
                break;

            case YEAR:
                mDateSeekBar.setProgress(60);
                break;

            case ALL:
                mDateSeekBar.setProgress(80);
                break;
        }

        updateTextView(timeSort);
        setupSpinner(topic);
        mSortSeekBar.setOnSeekBarChangeListener(this);
        mDateSeekBar.setOnSeekBarChangeListener(this);

        // I've never found fragment transaction animations to work properly, so we will animate the view
        // when it is added to the fragment manager
        view.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.filter_appear));
    }

    private void configToolBar(Toolbar tb) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tb.getLayoutParams();
        lp.setMargins(0, ViewUtils.getStatusBarHeight(getActivity()), 0, 0);
        tb.setLayoutParams(lp);
        tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss(null, null, null);
            }
        });

        tb.inflateMenu(R.menu.topics);
        tb.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.refresh:
                        fetchTopics();
                        mMultiStateView.setViewState(MultiStateView.ViewState.LOADING);
                        return true;
                }

                return false;
            }
        });
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    private void setupSpinner(ImgurTopic topic) {
        List<ImgurTopic> topics = app.getSql().getTopics();
        int spinnerPosition = 0;

        if (topics != null && !topics.isEmpty()) {
            LogUtil.v(TAG, "Topics loaded from Database");

            // Get the position of the topic for the spinner
            if (topic != null) {
                for (int i = 0; i < topics.size(); i++) {
                    if (topics.get(i).equals(topic)) {
                        spinnerPosition = i;
                        break;
                    }
                }
            }

            mSpinner.setAdapter(new TopicsAdapter(getActivity(), topics));
            mSpinner.setSelection(spinnerPosition);
            mMultiStateView.setViewState(MultiStateView.ViewState.CONTENT);
        } else {
            LogUtil.v(TAG, "No topics found, requesting from API");
            fetchTopics();
        }
    }

    /**
     * Updates the Range TextViews appropriately
     *
     * @param topSort
     */
    private void updateTextView(ImgurFilters.TimeSort topSort) {
        int selected = getResources().getColor(theme.accentColor);
        int defaultColor = theme.isDarkTheme ? Color.WHITE : Color.BLACK;
        int tfNormal = Typeface.NORMAL;
        int tfBold = Typeface.BOLD;

        mDay.setTextColor(topSort == ImgurFilters.TimeSort.DAY ? selected : defaultColor);
        mDay.setTypeface(null, topSort == ImgurFilters.TimeSort.DAY ? tfBold : tfNormal);
        mWeek.setTextColor(topSort == ImgurFilters.TimeSort.WEEK ? selected : defaultColor);
        mWeek.setTypeface(null, topSort == ImgurFilters.TimeSort.WEEK ? tfBold : tfNormal);
        mMonth.setTextColor(topSort == ImgurFilters.TimeSort.MONTH ? selected : defaultColor);
        mMonth.setTypeface(null, topSort == ImgurFilters.TimeSort.MONTH ? tfBold : tfNormal);
        mYear.setTextColor(topSort == ImgurFilters.TimeSort.YEAR ? selected : defaultColor);
        mYear.setTypeface(null, topSort == ImgurFilters.TimeSort.YEAR ? tfBold : tfNormal);
        mAll.setTextColor(topSort == ImgurFilters.TimeSort.ALL ? selected : defaultColor);
        mAll.setTypeface(null, topSort == ImgurFilters.TimeSort.ALL ? tfBold : tfNormal);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // NOOP
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // NOOP
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int position = seekBar.getProgress();
        int defaultColor = theme.isDarkTheme ? Color.WHITE : Color.BLACK;

        if (seekBar == mSortSeekBar) {
            if (position <= 33) {
                mSortSeekBar.setProgress(0);
                mTime.setTextColor(getResources().getColor(theme.accentColor));
                mViral.setTextColor(defaultColor);
                mHighestScoring.setTextColor(defaultColor);
                mTime.setTypeface(null, Typeface.BOLD);
                mViral.setTypeface(null, Typeface.NORMAL);
                mHighestScoring.setTypeface(null, Typeface.NORMAL);
                mDateContainer.animate().alpha(0.0f).setDuration(250L);
            } else if (position <= 66) {
                mSortSeekBar.setProgress(50);
                mTime.setTextColor(defaultColor);
                mViral.setTextColor(defaultColor);
                mHighestScoring.setTextColor(getResources().getColor(theme.accentColor));
                mTime.setTypeface(null, Typeface.NORMAL);
                mViral.setTypeface(null, Typeface.NORMAL);
                mHighestScoring.setTypeface(null, Typeface.BOLD);
                mDateContainer.animate().alpha(1.0f).setDuration(250L);
            } else {
                mSortSeekBar.setProgress(100);
                mTime.setTextColor(defaultColor);
                mViral.setTextColor(getResources().getColor(theme.accentColor));
                mHighestScoring.setTextColor(defaultColor);
                mTime.setTypeface(null, Typeface.NORMAL);
                mViral.setTypeface(null, Typeface.BOLD);
                mHighestScoring.setTypeface(null, Typeface.NORMAL);
                mDateContainer.animate().alpha(0.0f).setDuration(250L);
            }
        } else {
            if (position <= 10) {
                seekBar.setProgress(0);
                updateTextView(ImgurFilters.TimeSort.DAY);
            } else if (position <= 30) {
                seekBar.setProgress(20);
                updateTextView(ImgurFilters.TimeSort.WEEK);
            } else if (position <= 50) {
                seekBar.setProgress(40);
                updateTextView(ImgurFilters.TimeSort.MONTH);
            } else if (position <= 70) {
                seekBar.setProgress(60);
                updateTextView(ImgurFilters.TimeSort.YEAR);
            } else {
                seekBar.setProgress(80);
                updateTextView(ImgurFilters.TimeSort.ALL);
            }
        }
    }

    public void setListener(FilterListener listner) {
        mListener = listner;
    }

    @OnClick(R.id.errorButton)
    public void fetchTopics() {
        mMultiStateView.setViewState(MultiStateView.ViewState.LOADING);
        new ApiClient(Endpoints.TOPICS_DEFAULTS.getUrl(), ApiClient.HttpRequest.GET)
                .doWork(ImgurBusEvent.EventType.TOPICS, "topics", null);
    }

    @OnClick({R.id.negative, R.id.positive})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.negative:
                dismiss(null, null, null);
                break;

            case R.id.positive:
                ImgurTopic topic = null;
                if (mSpinner.getAdapter() != null) topic = (ImgurTopic) mSpinner.getAdapter().getItem(mSpinner.getSelectedItemPosition());

                ImgurFilters.GallerySort sort;
                ImgurFilters.TimeSort timeSort;
                int position = mSortSeekBar.getProgress();
                int timePosition = mDateSeekBar.getProgress();

                if (position <= 33) {
                    sort = ImgurFilters.GallerySort.TIME;
                } else if (position <= 66) {
                    sort = ImgurFilters.GallerySort.HIGHEST_SCORING;
                } else {
                    sort = ImgurFilters.GallerySort.VIRAL;
                }

                if (timePosition <= 10) {
                    timeSort = ImgurFilters.TimeSort.DAY;
                } else if (timePosition <= 30) {
                    timeSort = ImgurFilters.TimeSort.WEEK;
                } else if (timePosition <= 50) {
                    timeSort = ImgurFilters.TimeSort.MONTH;
                } else if (timePosition <= 70) {
                    timeSort = ImgurFilters.TimeSort.YEAR;
                } else {
                    timeSort = ImgurFilters.TimeSort.ALL;
                }

                dismiss(topic, sort, timeSort);
                break;
        }
    }

    public void dismiss(final ImgurTopic topic, final ImgurFilters.GallerySort sort, final ImgurFilters.TimeSort timeSort) {
        Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.filter_disappear);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (mListener != null)
                    mListener.onFilterChange(topic, sort, timeSort);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        getView().startAnimation(anim);
    }

    /**
     * Event Method that receives events from the Bus
     *
     * @param event
     */
    public void onEventAsync(@NonNull ImgurBusEvent event) {
        if (event.eventType == ImgurBusEvent.EventType.TOPICS) {
            try {
                int statusCode = event.json.getInt(ApiClient.KEY_STATUS);

                if (statusCode == ApiClient.STATUS_OK) {
                    List<ImgurTopic> topics = new ArrayList<>();
                    JSONArray array = event.json.getJSONArray(ApiClient.KEY_DATA);

                    for (int i = 0; i < array.length(); i++) {
                        topics.add(new ImgurTopic(array.getJSONObject(i)));
                    }

                    app.getSql().addTopics(topics);
                    mHandler.sendMessage(ImgurHandler.MESSAGE_ACTION_COMPLETE, topics);
                } else {
                    mHandler.sendMessage(ImgurHandler.MESSAGE_ACTION_FAILED, ApiClient.getErrorCodeStringResource(statusCode));
                }

            } catch (JSONException e) {
                LogUtil.e(TAG, "Error parsing JSON", e);
                mHandler.sendMessage(ImgurHandler.MESSAGE_ACTION_FAILED, ApiClient.getErrorCodeStringResource(ApiClient.STATUS_JSON_EXCEPTION));
            }
        }
    }

    /**
     * Event Method that is fired if EventBus throws an error
     *
     * @param event
     */
    public void onEventMainThread(ThrowableFailureEvent event) {
        Throwable e = event.getThrowable();

        if (e instanceof IOException) {
            mHandler.sendMessage(ImgurHandler.MESSAGE_ACTION_FAILED, ApiClient.getErrorCodeStringResource(ApiClient.STATUS_IO_EXCEPTION));
        } else if (e instanceof JSONException) {
            mHandler.sendMessage(ImgurHandler.MESSAGE_ACTION_FAILED, ApiClient.getErrorCodeStringResource(ApiClient.STATUS_JSON_EXCEPTION));
        } else {
            mHandler.sendMessage(ImgurHandler.MESSAGE_ACTION_FAILED, ApiClient.getErrorCodeStringResource(ApiClient.STATUS_INTERNAL_ERROR));
        }

        LogUtil.e(TAG, "Error received from Event Bus", e);
    }

    private ImgurHandler mHandler = new ImgurHandler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_ACTION_FAILED:
                    mMultiStateView.setErrorText(R.id.errorMessage, (Integer) msg.obj);
                    mMultiStateView.setViewState(MultiStateView.ViewState.ERROR);
                    break;

                case MESSAGE_ACTION_COMPLETE:
                    if (msg.obj instanceof List) {
                        List<ImgurTopic> topics = (List<ImgurTopic>) msg.obj;

                        // Shouldn't happen
                        if (topics.isEmpty()) {
                            LogUtil.w(TAG, "Did not receive any topics");
                            sendMessage(MESSAGE_ACTION_FAILED, R.string.error_generic);
                            return;
                        }

                        mSpinner.setAdapter(new TopicsAdapter(getActivity(), topics));
                        mMultiStateView.setViewState(MultiStateView.ViewState.CONTENT);
                    }
                    break;
            }
        }
    };

    public interface FilterListener {
        void onFilterChange(ImgurTopic topic, ImgurFilters.GallerySort sort, ImgurFilters.TimeSort timeSort);
    }
}
