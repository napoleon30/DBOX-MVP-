package cn.sharelink.dbox.view.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.accloud.cloudservice.AC;
import com.accloud.service.ACAccountMgr;
import com.accloud.utils.PreferencesUtils;

import cn.sharelink.dbox.R;
import cn.sharelink.dbox.model.customView.country.CountryActivity;
import cn.sharelink.dbox.model.utils.BroadcastAction;
import cn.sharelink.dbox.model.utils.Pop;
import cn.sharelink.dbox.presenter.IPresenter.ILoginPresenter;
import cn.sharelink.dbox.presenter.presenterCompl.LoginPresenter;
import cn.sharelink.dbox.view.interfaceview.ILoginView;

public class LoginActivity extends AppCompatActivity implements ILoginView,View.OnClickListener{
    private static final String TAG = LoginActivity.class.getSimpleName();
    private Button table01,table02,login;
    private TextView countryCode,forgetPassword,signUp;
    ILoginPresenter presenter;
    RelativeLayout userPhone,userEmail;
    private EditText etPhone,etEmail,etPassword;
    private LinearLayout layout_tab;
    private RelativeLayout rlPhone,rlEmail;
    private CheckBox checkBox;

    // 账号管理器
    ACAccountMgr accountMgr;
    String phoneOrEmail;
    String password;
    int num = 0;//0表示为手机登录，1表示为邮箱登录
    String strTel,strEmail;
    String strCountryCode="+86";
    ProgressDialog loginDialog;
    public static final int BINARY = 0;
    public static final int JSON = 1;

    private MyBroadcastReceiver myRecevier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        myRecevier = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastAction.loginSuccess);
        registerReceiver(myRecevier,filter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferencesUtils.putString(LoginActivity.this, "domain", "wuhanxl");
        PreferencesUtils
                .putString(LoginActivity.this, "subDomain", "xinlian01");
        PreferencesUtils.putLong(LoginActivity.this, "domainId", 5287);
        PreferencesUtils.putInt(LoginActivity.this, "formatType", BINARY);
        PreferencesUtils.putInt(LoginActivity.this, "deviceType",
                AC.DEVICE_AI6060H);
        strTel = PreferencesUtils.getString(LoginActivity.this, "phone");
        strCountryCode = PreferencesUtils.getString(LoginActivity.this,
                "areacode", "+86");
        strEmail = PreferencesUtils.getString(LoginActivity.this, "email");
        num = PreferencesUtils.getInt(LoginActivity.this, "num", 0);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG,"onResume");
        if (accountMgr.isLogin()){
            startActivity(new Intent(LoginActivity.this,MainActivity.class));
            LoginActivity.this.finish();
        }

        if (!TextUtils.isEmpty(strTel)){
            etPassword.requestFocus();// 获取光标
            return;
        }
        if (!TextUtils.isEmpty(strEmail)){
            etPassword.requestFocus();
            return;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myRecevier);
    }

    @Override
    public void initView() {
        // 通过AC获取账号管理器
        accountMgr = AC.accountMgr();
        loginDialog = new ProgressDialog(LoginActivity.this);
        checkBox = findViewById(R.id.cb_pass);
        table01 = findViewById(R.id.login_btnTab001);
        table02 = findViewById(R.id.login_btnTab002);
        login = findViewById(R.id.login);
        countryCode = findViewById(R.id.area_code);
        countryCode.setText(strCountryCode);
        forgetPassword = findViewById(R.id.forget_password);
        signUp = findViewById(R.id.register);
        userPhone = findViewById(R.id.user_phone);
        userEmail = findViewById(R.id.user_email);
        etPhone = findViewById(R.id.login_edit_tel);
        if (!TextUtils.isEmpty(strTel)){
            etPhone.setText(strTel);
        }
        etEmail = findViewById(R.id.login_edit_email);
        if (!TextUtils.isEmpty(strEmail)) {
            etEmail.setText(strEmail);
        }
        etPassword = findViewById(R.id.login_edit_pwd);
        layout_tab = findViewById(R.id.layout_tab);
        rlPhone = findViewById(R.id.user_phone);
        rlEmail = findViewById(R.id.user_email);
        presenter = new LoginPresenter(LoginActivity.this,loginDialog);
        table01.setOnClickListener(this);
        table02.setOnClickListener(this);
        login.setOnClickListener(this);
        countryCode.setOnClickListener(this);
        forgetPassword.setOnClickListener(this);
        signUp.setOnClickListener(this);

        if (num == 0) {
            setTabSelected(layout_tab,table01);
            layout_tab.setBackgroundResource(R.mipmap.choice_left);
            table01.setTextColor(Color.WHITE);
            table02.setTextColor(Color.BLUE);
            rlPhone.setVisibility(View.VISIBLE);
            rlEmail.setVisibility(View.GONE);
        } else {
            setTabSelected(layout_tab,table02);
            layout_tab.setBackgroundResource(R.mipmap.choice_right);
            table01.setTextColor(Color.BLUE);
            table02.setTextColor(Color.WHITE);
            rlPhone.setVisibility(View.GONE);
            rlEmail.setVisibility(View.VISIBLE);
        }

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    etPassword.setSelection(etPassword.getText().length());
                } else {
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    etPassword.setSelection(etPassword.getText().length());
                }
            }
        });
    }

    @Override
    public void login() {
        getData();
        if(password.length()==0 || phoneOrEmail.length()==0){
            Pop.popToast(LoginActivity.this,
                    getString(R.string.login_aty_username_or_pwd_cannot_be_empty));
        }else {
            PreferencesUtils.putString(LoginActivity.this, "phone",
                    strTel);
            PreferencesUtils.putString(LoginActivity.this, "email",
                    strEmail);
            presenter.login(LoginActivity.this, accountMgr, phoneOrEmail, password);
        }
    }

    @Override
    public void getData() {
        password = etPassword.getText().toString();
        if(num==0){ //手机号
            strTel = etPhone.getText().toString();
            if(strCountryCode.equals("+86")){
                phoneOrEmail = strTel;
            }else {
                phoneOrEmail = strCountryCode.replace("+", "00") + strTel;
            }
        }else{ //邮箱
            strEmail = etEmail.getText().toString();
            phoneOrEmail = strEmail;
        }

    }

    @Override
    public void setTabSelected(LinearLayout layout_tab,Button btnSelected) {
        presenter.setTabSelected(layout_tab,btnSelected);
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()){
            case R.id.login_btnTab001:
                setTabSelected(layout_tab,table01);
                layout_tab.setBackgroundResource(R.mipmap.choice_left);
                table01.setTextColor(Color.WHITE);
                table02.setTextColor(Color.BLUE);
                rlPhone.setVisibility(View.VISIBLE);
                rlEmail.setVisibility(View.GONE);
                num=0;
                PreferencesUtils.putInt(LoginActivity.this, "num", num);
                break;
            case R.id.login_btnTab002:
                setTabSelected(layout_tab,table02);
                layout_tab.setBackgroundResource(R.mipmap.choice_right);
                table01.setTextColor(Color.BLUE);
                table02.setTextColor(Color.WHITE);
                rlPhone.setVisibility(View.GONE);
                rlEmail.setVisibility(View.VISIBLE);
                num=1;
                PreferencesUtils.putInt(LoginActivity.this, "num", num);
                break;
            case R.id.login:
                login();
                break;
            case R.id.area_code:
                intent = new Intent(LoginActivity.this, CountryActivity.class);
                startActivityForResult(intent, 12);
                break;
            case R.id.forget_password:
                intent = new Intent(LoginActivity.this,ResetPasswordActivity.class);
                startActivity(intent);
                break;
            case R.id.register:
                intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 12:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String countryName = bundle.getString("countryName");
                    strCountryCode = bundle.getString("countryNumber");
                    PreferencesUtils.putString(LoginActivity.this, "areacode",
                            strCountryCode);// 保存区号

                    countryCode.setText(strCountryCode);

                }
                break;

            default:
                break;
        }
    }

    private class MyBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastAction.loginSuccess)){
                startActivity(new Intent(LoginActivity.this,MainActivity.class));
                LoginActivity.this.finish();
            }
        }
    }
}

