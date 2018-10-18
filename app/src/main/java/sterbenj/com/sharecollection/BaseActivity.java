package sterbenj.com.sharecollection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import cn.bmob.v3.Bmob;

/**
 * XJB Created by 野良人 on 2018/5/6.
 */
public class BaseActivity extends AppCompatActivity {

    public static int sTheme;
    public static boolean pasteListenerIsRun;

    private IntentFilter intentFilter;
    private NetWorkChangeReceiver netWorkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState){

        //初始化Bmob服务
        Bmob.initialize(this, "b8635d533cc781d63127bf5533d0a692");


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        sTheme = sharedPreferences.getInt("themeID", R.style.white_transStat);
        pasteListenerIsRun = sharedPreferences.getBoolean("PasteSwitchIsChecked", false);

        this.setTheme(sTheme);

        if (BaseActivity.pasteListenerIsRun){
            final Intent serviceStart = new Intent(getApplication(), PasteListenerService.class);
            startService(serviceStart);
        }

        if (pasteListenerIsRun){
            //TODO 233
        }

        //注册监听网络的广播
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        netWorkChangeReceiver = new NetWorkChangeReceiver();
        registerReceiver(netWorkChangeReceiver, intentFilter);

        super.onCreate(savedInstanceState);
        ActivityControl.addActivity(this);
    }

    class NetWorkChangeReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable()){
                tools.NetWork = true;
                Log.d("broad", "onReceive: " + tools.NetWork);
            }
            else{
                tools.NetWork = false;
                Log.d("broad", "onReceive: " + tools.NetWork);
            }
        }
    }
    @Override
    protected  void onDestroy(){
        super.onDestroy();
        ActivityControl.removeActivity(this);

        unregisterReceiver(netWorkChangeReceiver);
    }
}
