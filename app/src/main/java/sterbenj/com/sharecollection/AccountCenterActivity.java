package sterbenj.com.sharecollection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import cn.bmob.v3.BmobUser;

public class AccountCenterActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_center);
        //初始化TextView
        TextView account = (TextView)findViewById(R.id.account_center_account);
        account.setText(PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext()).getString("AccountHasLogin", "获取用户名出错"));

        //初始化Toolbar
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_account_center);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //登出按钮
        AppCompatButton logoutButton = (AppCompatButton)findViewById(R.id.account_center_logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //登出
                BmobUser.logOut();
                SharedPreferences.Editor editor;
                editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                editor.putString("AccountHasLogin", null);
                if (editor.commit()){
                    Toast.makeText(getApplicationContext(), "登出成功", Toast.LENGTH_SHORT).show();
                    finish();
                }
                else{
                    Toast.makeText(getApplicationContext(), "登出失败", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //更改密码按钮
        AppCompatButton changePasswordButton = (AppCompatButton)findViewById(R.id.account_center_changepasswordButton);
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //跳转更改密码活动
                Intent intent = new Intent(getApplicationContext(), ChangePasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;
        }
        return false;
    }
}
