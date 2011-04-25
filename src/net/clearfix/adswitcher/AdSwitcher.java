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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class AdSwitcher extends ViewFlipper implements OnAdAvailabilityChanged {
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
        mAds = buildAds();
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

    private List<Ad> buildAds() {
        List<Ad> ads = new ArrayList<Ad>();
        List<Class<? extends Ad>> adsClasses = mConfiguration.getAds();
        for (Class<? extends Ad> adClass : adsClasses) {
            try {
                Constructor<? extends Ad> constructor = adClass.getConstructor(Context.class);
                Ad ad = constructor.newInstance(getContext());
                ad.setOnAdAvailabilityChanged(this);
                ads.add(ad);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return ads;
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
                setCurrentAd(ad);
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

    private void setCurrentAd(Ad ad) {
        Log.i(getLogTag(), "Showing ad " + ad);
        setDisplayedChild(mAds.indexOf(ad));
        setVisibility(View.VISIBLE);
    }

    private String getLogTag() {
        return TAG + ":" + mAdSwitcherId;
    }

    public void onAdAvailabilityChanged(Ad ad) {
        if (!ad.isAvailable()) {
            return;
        }
        boolean atLeastOneAvailable = false;
        for (Ad anAd : mAds) {
            if (anAd != ad && anAd.isAvailable()) {
                atLeastOneAvailable = true;
            }
        }
        if (!atLeastOneAvailable) {
            setCurrentAd(ad);
        }
    }
}