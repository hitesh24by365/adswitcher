package in.adswitcher.android.template;

import android.content.Context;
import android.view.View;
import in.adswitcher.android.AdHolder;

import java.lang.reflect.*;

/**
 * This is a template for <a href='http://www.jumptap.com/'>JumpTap</a>. This template has three constructor which allows you
 * to configure the publisher ID, the spot ID and the site ID. The only mandatory argument is the publisher ID. Example:
<pre>List&lt;AdParameter&gt; jumpTapParams = new ArrayList&lt;AdParameter&gt;();
jumpTapParams.add(new AdParameter(String.class, "the publisher ID"));
jumpTapParams.add(new AdParameter(String.class, "the spot ID"));
jumpTapParams.add(new AdParameter(String.class, "the site ID"));
AdConfiguration config = new AdConfiguration.Builder()
        .addAd(JumpTap.class, jumpTapParams)
        .build();</pre>
 */
public class JumpTap extends AdHolder{
    private Object mJtView;
    private Method mRefreshAd;

    public JumpTap(Context context, String publisherId) {
        this(context, publisherId, null, null);
    }

    public JumpTap(Context context, String publisherId, String spotId) {
        this(context, publisherId, spotId, null);
    }

    public JumpTap(Context context, String publisherId, String spotId, String siteId) {
        super(context);
        if (publisherId == null) {
            throw new RuntimeException("Publisher ID cannot be null");
        }
        try {
            // 1. get the needed classes and methods
            Class<?> jtAdViewClass = Class.forName("com.jumptap.adtag.JtAdView");
            Class<?> jtAdViewListenerClass = Class.forName("com.jumptap.adtag.JtAdViewListener");
            Class<?> jtAdWidgetSettingsClass = Class.forName("com.jumptap.adtag.JtAdWidgetSettings");
            Class<?> jtAdWidgetSettingsFactoryClass = Class.forName("com.jumptap.adtag.JtAdWidgetSettingsFactory");
            mRefreshAd = jtAdViewClass.getMethod("refreshAd");

            // 2. Create instance of the JumpTap settings and configure it
            Method createWidgetSettings = jtAdWidgetSettingsFactoryClass.getMethod("createWidgetSettings");
            Object widgetSettings = createWidgetSettings.invoke(null);
            Method setPublisherId = jtAdWidgetSettingsClass.getMethod("setPublisherId", String.class);
            setPublisherId.invoke(widgetSettings, publisherId);
            if (spotId != null) {
                Method setSpotId = jtAdWidgetSettingsClass.getMethod("setSpotId", String.class);
                setSpotId.invoke(widgetSettings, spotId);
            }
            if (siteId != null) {
                Method setSiteId = jtAdWidgetSettingsClass.getMethod("setSiteId", String.class);
                setSiteId.invoke(widgetSettings, siteId);
            }

            // 3. Create the Jump Tap view
            Constructor<?> constructor = jtAdViewClass.getConstructor(Context.class, jtAdWidgetSettingsClass);
            mJtView = constructor.newInstance(context, widgetSettings);

            // 4. Create and set a Jump Tap view listener
            Object jtAdViewListener = Proxy.newProxyInstance(jtAdViewListenerClass.getClassLoader(),
                    new Class[]{jtAdViewListenerClass}, new AdViewListener());
            Method setAdViewListener = jtAdViewClass.getMethod("setAdViewListener", jtAdViewListenerClass);
            setAdViewListener.invoke(mJtView, jtAdViewListener);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View getView() {
        return (View) mJtView;
    }

    @Override
    public void refresh() {
        try {
            mRefreshAd.invoke(mJtView);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private class AdViewListener implements InvocationHandler {
        private static final String ON_NEW_AD = "onNewAd";
        private static final String ON_NO_AD_FOUND = "onNoAdFound";
        @Override
        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
            if (method.getName().equals(ON_NEW_AD)) {
                setAvailable(true);
            } else if (method.getName().equals(ON_NO_AD_FOUND)) {
                setAvailable(false);
            }
            return null;
        }
    }
}