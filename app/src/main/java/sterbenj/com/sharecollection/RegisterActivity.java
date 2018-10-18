package sterbenj.com.sharecollection;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.widget.AppCompatButton;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;


public class RegisterActivity extends BaseActivity {

    TextInputLayout accountLayout;
    TextInputEditText account;
    TextInputLayout passwordLayout;
    TextInputEditText password;
    TextInputLayout passwordAgainLayout;
    TextInputEditText passwordAgain;

    String accountStr;
    String passwordStr;
    String passwordAgainStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //返回按钮
        AppCompatButton buttonBack = (AppCompatButton)findViewById(R.id.reg_back);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //账号
        accountLayout = (TextInputLayout)findViewById(R.id.reg_account_layout);
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
        passwordLayout = (TextInputLayout)findViewById(R.id.reg_password_layout);
        password = (TextInputEditText) passwordLayout.getEditText();
        //密码监听
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //监听重复输入是否相同
                passwordAgainStr = passwordAgain.getText().toString();
                if (!passwordAgainStr.isEmpty()){
                    if (!s.toString().equals(passwordAgainStr)){
                        passwordAgainLayout.setErrorEnabled(true);
                        passwordAgainLayout.setError("两次输入密码不同");
                    }
                    else{
                        passwordAgainLayout.setErrorEnabled(false);
                    }
                }
                else{
                    passwordAgainLayout.setErrorEnabled(false);
                }

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

        //确认密码
        passwordAgainLayout = (TextInputLayout)findViewById(R.id.reg_password_again_layout);
        passwordAgain = (TextInputEditText) passwordAgainLayout.getEditText();
        //确认密码监听
        passwordAgain.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //监听重复输入是否相同
                passwordStr = password.getText().toString();
                if (!s.toString().isEmpty()){
                    if (!s.toString().equals(passwordStr)){
                        passwordAgainLayout.setErrorEnabled(true);
                        passwordAgainLayout.setError("两次输入密码不同");
                    }
                    else{
                        passwordAgainLayout.setErrorEnabled(false);
                    }
                }
                else{
                    passwordAgainLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //注册按钮
        AppCompatButton buttonReg = (AppCompatButton)findViewById(R.id.reg_regButton);
        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInput()){
                    register();
                }
                else{
                    Toast.makeText(getApplicationContext(), "注册失败，检测输入是否合法", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private Boolean checkInput(){
        accountStr = account.getText().toString();
        passwordStr = password.getText().toString();
        passwordAgainStr = passwordAgain.getText().toString();
        if (accountStr.isEmpty() || passwordStr.isEmpty() || passwordAgainStr.isEmpty()
                || accountStr.length() < 6 || accountStr.length() > 12
                || passwordStr.length() < 8 || passwordStr.length() > 16
                || !passwordAgainStr.equals(passwordStr)){
            return false;
        }
        else{
            return true;
        }
    }

    private Boolean register(){
        BmobUser bmobUser = new BmobUser();
        bmobUser.setUsername(accountStr);
        bmobUser.setPassword(passwordStr);
        bmobUser.signUp(new SaveListener<User>() {
            @Override
            public void done(User s, BmobException e) {
                if(e == null) {
                    Toast.makeText(getApplicationContext(), "注册成功，请登入", Toast.LENGTH_SHORT).show();
                    finish();
                }
                else {
                    Toast.makeText(getApplicationContext(), "注册失败，用户名可能被占用，网络状况可能不好", Toast.LENGTH_LONG).show();
                }
            }
        });
        return false;
    }
}
