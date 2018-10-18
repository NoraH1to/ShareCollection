package sterbenj.com.sharecollection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.widget.AppCompatButton;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;

public class LoginActivity extends BaseActivity {

    TextInputLayout accountLayout;
    TextInputEditText account;
    TextInputLayout passwordLayout;
    TextInputEditText password;

    String accountStr;
    String passwordStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //注册按钮
        AppCompatButton buttonToReg = (AppCompatButton)findViewById(R.id.login_jumpTo_reg);
        buttonToReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });

        //返回按钮
        AppCompatButton buttonBack = (AppCompatButton)findViewById(R.id.login_back);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //账号
        accountLayout = (TextInputLayout)findViewById(R.id.login_account_layout);
        account = (TextInputEditText)accountLayout.getEditText();
        //账号监听
        account.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //监听长度
                if (!s.toString().isEmpty()){
                    if (s.toString().length() < 6 || s.toString().length() > 12){
                        accountLayout.setErrorEnabled(true);
                        accountLayout.setError("长度错误");
                    }
                    else{
                        accountLayout.setErrorEnabled(false);
                    }
                }
                else{
                    accountLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //密码
        passwordLayout = (TextInputLayout)findViewById(R.id.login_password_layout);
        password = (TextInputEditText)passwordLayout.getEditText();
        //密码监听
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //监听长度
                if (!s.toString().isEmpty()){
                    if (s.toString().length() < 8 || s.toString().length() > 16){
                        passwordLayout.setErrorEnabled(true);
                        passwordLayout.setError("长度错误");
                    }
                    else{
                        passwordLayout.setErrorEnabled(false);
                    }
                }
                else{
                    passwordLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //登陆按钮
        AppCompatButton buttonLogin = (AppCompatButton)findViewById(R.id.login_loginButton);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInput()){
                    login();
                }
                else{
                    Toast.makeText(getApplicationContext(), "登入失败，检测输入是否合法", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //检测输入合法性
    private Boolean checkInput(){
        accountStr = account.getText().toString();
        passwordStr = password.getText().toString();
        if (accountStr.isEmpty() || passwordStr.isEmpty()
                || accountStr.length() < 6 || accountStr.length() > 12
                || passwordStr.length() < 8 || passwordStr.length() > 16){
            return false;
        }
        else{
            return true;
        }
    }

    //登入
    private Boolean login(){
        BmobUser.loginByAccount(accountStr, passwordStr, new LogInListener<User>() {
            @Override
            public void done(User user, BmobException e) {
                if (user!=null){
                    Toast.makeText(getApplicationContext(), "登入成功", Toast.LENGTH_SHORT).show();

                    //添加到本地数据库
                    BmobUser bmobUser = BmobUser.getCurrentUser();
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                    editor.putString("AccountHasLogin", bmobUser.getUsername());
                    editor.apply();

                    finish();
                }
                else{
                    Toast.makeText(getApplicationContext(), "登入失败，密码或用户名不正确", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return true;
    }
}
