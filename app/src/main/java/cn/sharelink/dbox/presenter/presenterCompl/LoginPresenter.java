package cn.sharelink.dbox.presenter.presenterCompl;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.accloud.cloudservice.PayloadCallback;
import com.accloud.service.ACAccountMgr;
import com.accloud.service.ACException;
import com.accloud.service.ACUserInfo;

import cn.sharelink.dbox.R;
import cn.sharelink.dbox.model.utils.BroadcastAction;
import cn.sharelink.dbox.model.utils.DBOXException;
import cn.sharelink.dbox.model.utils.DensityUtils;
import cn.sharelink.dbox.model.utils.ResourceReader;
import cn.sharelink.dbox.presenter.IPresenter.ILoginPresenter;

/**
 * Created by WangLei on 2018/2/1.
 */

public class LoginPresenter implements ILoginPresenter{
    public Context context;
    public ProgressDialog dialog;
    public LoginPresenter(Context context, ProgressDialog dialog){
        this.context = context;
        this.dialog = dialog;
    }
    @Override
    public void login(final Context context, final ACAccountMgr accountMgr, final String phoneOrEmail, final String password) {
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage(context.getString(R.string.logining));
        dialog.show();
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 0: //登录成功
                        dialog.dismiss();
                        Log.e("ABLECLOUD","登录成功");
                        Toast.makeText(context,context.getString(R.string.login_success),Toast.LENGTH_LONG).show();
                        Intent intent = new Intent();
                        intent.setAction(BroadcastAction.loginSuccess);
                        context.sendBroadcast(intent);

                        break;
                    case 1: //登录失败
                        dialog.dismiss();
                        int data = (Integer) msg.obj;
                        Log.e("ABLECLOUD","登录失败"+"/"+data);
                        DBOXException.errorCode(context, data);
                        break;
                }
            }
        };
        new Thread(new Runnable() { //AbleCloud登录
            @Override
            public void run() {
                accountMgr.login(phoneOrEmail, password, new PayloadCallback<ACUserInfo>() {
                    @Override
                    public void success(ACUserInfo acUserInfo) {
                        Message message = new Message();
                        message.what=0;
                        handler.sendMessage(message);
                    }

                    @Override
                    public void error(ACException e) {
                        Message message = new Message();
                        message.obj = e.getErrorCode();
                        message.what=1;
                        handler.sendMessage(message);
                    }
                });
            }
        }).start();

    }

    @Override
    public void setTabSelected(LinearLayout ll, Button btn) {
        Drawable selectedDrawable = ResourceReader.readDrawable(context,
                R.drawable.shape_nav_indicator);
        int screenWidth = DensityUtils.getScreenSize(context)[0];
        int right = screenWidth / 2;
        selectedDrawable.setBounds(0, 0, right, DensityUtils.dipTopx(context, 3));
        btn.setSelected(true);
        btn.setCompoundDrawables(null, null, null, null);//bottom为selectedDrawable可显示下划线，为null将下划线隐藏
        int size = ll.getChildCount();
        for (int i = 0; i < size; i++) {
            if (btn.getId() != ll.getChildAt(i).getId()) {
                ll.getChildAt(i).setSelected(false);
                ((Button) ll.getChildAt(i)).setCompoundDrawables(null,
                        null, null, null);
            }
        }
    }

}
