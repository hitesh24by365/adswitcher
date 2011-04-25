package net.clearfix.adswitcher;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.widget.ViewFlipper;

import java.util.List;

public class AdSwitcher extends ViewFlipper {
    private final static int REFRESH_AD = 101;
    private static final String AD_SWITCHER_ID = "adSwitcherId";
    private static final String TAG = "AD_SWITCHER";

    private final String mAdSwitcherId;
    private Looper mRefreshLooper;
    private Handler mRefreshHandler;
    private List<Ad> mAds;
    private AdConfiguration mConfiguration;

    public AdSwitcher(Context context, String adSwitcherId) {
        super(context);
        mAdSwitcherId = adSwitcherId;
        init();
    }

    public AdSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        setVisibility(View.GONE);// hidden by default
        mAdSwitcherId = attrs.getAttributeValue(null, AD_SWITCHER_ID);
        if (mAdSwitcherId == null) {
            throw new RuntimeException("AdSwitcher view must contain an 'adSwitcherId' attribute");
        }
        init();
    }

    private void init() {
        mConfiguration = AdSettings.getConfiguration(mAdSwitcherId);
        mAds = mConfiguration.getAds();
        for (Ad ad : mAds) {
            ViewParent parent = ad.getView().getParent();
            if (parent != null) {
                ((AdSwitcher) parent).removeView(ad.getView());
            }
            addView(ad.getView());
        }
        Thread refreshThread = new Thread() {
            @Override
            public void run() {
                Log.i(getLogTag(), "Refresh Thread started");
                Looper.prepare();
                mRefreshLooper = Looper.myLooper();
                mRefreshHandler = new Handler(mRefreshLooper) {
                    @Override
                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case REFRESH_AD: {
                                Log.i(getLogTag(), "Refresh Ad message received.");
                                for (Ad ad : mAds) {
                                    ad.refresh();
                                }
                                switchAds();
                                break;
                            }
                        }
                    }
                };
                Looper.loop();
                Log.i(getLogTag(), "Refresh Thread stopped");
            }
        };
        refreshThread.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mRefreshLooper != null) {
            mRefreshLooper.quit();
            mRefreshLooper = null;
            mRefreshHandler = null;
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (mRefreshHandler != null) {
            mRefreshHandler.removeMessages(REFRESH_AD);
            if (visibility == View.VISIBLE) {
                mRefreshHandler.sendEmptyMessage(REFRESH_AD);
            } else {
                for (Ad ad : mAds) {
                    ad.pause();
                }
            }
        }
    }

    private void switchAds() {
        boolean showAds = false;
        for (Ad ad : mAds) {
            if (ad.isAvailable()) {
                Log.i(getLogTag(), "Showing ad " + ad);
                setDisplayedChild(mAds.indexOf(ad));
                setVisibility(View.VISIBLE);
                showAds = true;
                break;
            }
        }
        if (!showAds) {
            Log.i(getLogTag(), "Hiding ads");
            setVisibility(View.GONE);
        }
        if (mRefreshHandler != null) {
            mRefreshHandler.removeMessages(REFRESH_AD);
            mRefreshHandler.sendEmptyMessageDelayed(REFRESH_AD, mConfiguration.getInterval());
        }
    }

    private String getLogTag() {
        return TAG + ":" + mAdSwitcherId;
    }
}