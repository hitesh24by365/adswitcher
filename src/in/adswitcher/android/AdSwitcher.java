package in.adswitcher.android;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
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
    private List<Ad> mAds;
    private AdConfiguration mConfiguration;
    private boolean mStarted;
    private boolean mRunning;
    private boolean mVisible;
    private LayoutParams mParams;
    private Handler mHandler;

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
        mParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        mParams.gravity = Gravity.CENTER_HORIZONTAL;
        mConfiguration = AdSettings.getConfiguration(mAdSwitcherId);
        mAds = buildAds();
        for (Ad ad : mAds) {
            ViewParent parent = ad.getView().getParent();
            if (parent != null) {
                ((AdSwitcher) parent).removeView(ad.getView());
            }
            addView(ad.getView(), mParams);
        }
        mHandler = new Handler() {
            public void handleMessage(Message m) {
                if (mRunning) {
                    Log.i(getLogTag(), "Refresh Ad message received.");
                    for (Ad ad : mAds) {
                        ad.refresh();
                    }
                    showAds();
                    sendMessageDelayed(Message.obtain(this, REFRESH_AD), mConfiguration.getInterval());
                }
            }

            @Override
            public String toString() {
                return AdSwitcher.this.toString() + ":" + getContext();
            }
        };
        showAds();
    }

    private List<Ad> buildAds() {
        List<Ad> ads = new ArrayList<Ad>();
        List<Class<? extends Ad>> adsClasses = mConfiguration.getAds();
        for (Class<? extends Ad> adClass : adsClasses) {
            try {
                Ad ad;
                List<Parameter> params = mConfiguration.getParams(adClass);
                if (params == null) {
                    // constructor is simple
                    Constructor<? extends Ad> constructor = adClass.getConstructor(Context.class);
                    ad = constructor.newInstance(getContext());
                } else {
                    List<Class> clazzes = new ArrayList<Class>();
                    clazzes.add(Context.class);
                    for (Parameter param : params) {
                        clazzes.add(param.getKey());
                    }
                    Constructor<? extends Ad> constructor = adClass.getConstructor(clazzes.toArray(new Class[]{}));
                    // set values
                    List<Object> values = new ArrayList<Object>();
                    values.add(getContext());
                    for (Parameter param : params) {
                        values.add(param.getValue());
                    }
                    ad = constructor.newInstance(values.toArray(new Object[]{}));
                }
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
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mStarted = true;
        updateRunning();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mStarted = false;
        updateRunning();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mVisible = visibility == VISIBLE;
        updateRunning();
    }

    private void showAds() {
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
        if (!ad.isAvailable() || getVisibility() == View.VISIBLE) {
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

    private void updateRunning() {
        boolean running = mVisible && mStarted;
        if (running != mRunning) {
            if (running) {
                mHandler.sendMessageDelayed(Message.obtain(mHandler, REFRESH_AD), mConfiguration.getInterval());
            } else {
                mHandler.removeMessages(REFRESH_AD);
            }
            mRunning = running;
        }
    }
}