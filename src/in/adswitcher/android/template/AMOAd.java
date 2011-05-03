package in.adswitcher.android.template;

public class AMOAd {
    /**
     * <!-- Addictive Mobilility -->
        <meta-data android:name="AMOA.APP_KEY" android:value="3f7e5e49e045d4a267356c7f25ca54cb" />
        <activity android:name="com.atc.adnetwork.microApp" android:screenOrientation="portrait"/>
     */
}
/**
 public class AMOAd extends AdHolder{
     private AMOAdBanner mBanner;

     public AMOAd(Context context) {
         super(context);
         mBanner = new AMOAdBanner(context);
         refresh();
     }

     @Override
     public View getView() {
         return mBanner;
     }

     @Override
     public void refresh() {
         try {
             mBanner.showAdd();
             setAvailable(true);
         } catch (Exception e) {
             setAvailable(false);
         }
     }
 }
*/