package in.adswitcher.android.template;

import android.content.Context;
import android.view.View;
import in.adswitcher.android.AdHolder;

import java.lang.reflect.*;

/**
 * This is a template for the <a href='http://www.greystripe.com/'>Greystripe</a> banners. Example of use:
<pre>
List<AdParameter> greystripeParams = new ArrayList<AdParameter>();
greystripeParams.add(new AdParameter(String.class, "PUT HERE THE GREYSTRIPE ID"));
AdConfiguration config = new AdConfiguration.Builder()
    .addAd(in.adswitcher.android.template.Greystripe.class, greystripeParams)
    .build();
AdSettings.addConfiguration("main", config);
</pre>
 */
public class Greystripe extends AdHolder{
    private Object mBannerView;
    private Method mRefresh;

    public Greystripe(Context context, String apiKey) {
        super(context);
        try {
            // 1. Load necessary classes
            Class<?> bannerListenerClass = Class.forName("com.greystripe.android.sdk.BannerListener");
            Class<?> bannerViewClass = Class.forName("com.greystripe.android.sdk.BannerView");
            Class<?> gsSdkClass = Class.forName("com.greystripe.android.sdk.GSSDK");

            // 2. Instantiate the banner view
            Method initialize = gsSdkClass.getMethod("initialize", Context.class, String.class);
            initialize.invoke(null, context, apiKey);
            
            Constructor<?> bannerViewConstructor = bannerViewClass.getConstructor(Context.class);
            mBannerView = bannerViewConstructor.newInstance(context);

            // 3. Instantiate banner view listener
            Object bannerListener = Proxy.newProxyInstance(bannerListenerClass.getClassLoader(),
                    new Class[]{bannerListenerClass}, new BannerListenerProxy());

            // 4. Set the banner listener
            Method addListenerMethod = bannerViewClass.getMethod("addListener", bannerListenerClass);
            addListenerMethod.invoke(mBannerView, bannerListener);

            // 5. Get extra stuff
            mRefresh = bannerViewClass.getMethod("refresh");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
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

    private class BannerListenerProxy implements InvocationHandler{
        private static final String ON_RECEIVED_AD = "onReceivedAd";
        private static final String ON_FAILED_TO_RECEIVE_AD = "onFailedToReceiveAd";
        @Override
        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
            if (ON_FAILED_TO_RECEIVE_AD.equals(method.getName())) {
                setAvailable(false);
            } else if (ON_RECEIVED_AD.equals(method.getName())) {
                setAvailable(true);
            }
            return null;
        }
    }

    @Override
    public View getView() {
        return (View) mBannerView;
    }

    @Override
    public void refresh() {
        try {
            mRefresh.invoke(mBannerView);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}