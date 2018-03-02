package cn.sharelink.dbox.presenter.presenterCompl;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.accloud.cloudservice.PayloadCallback;
import com.accloud.cloudservice.VoidCallback;
import com.accloud.service.ACAccountMgr;
import com.accloud.service.ACException;
import com.accloud.service.ACUserInfo;

import cn.sharelink.dbox.R;
import cn.sharelink.dbox.model.utils.BroadcastAction;
import cn.sharelink.dbox.model.utils.CountDownTimerUtils;
import cn.sharelink.dbox.model.utils.DBOXException;
import cn.sharelink.dbox.model.utils.DensityUtils;
import cn.sharelink.dbox.model.utils.Pop;
import cn.sharelink.dbox.model.utils.ResourceReader;
import cn.sharelink.dbox.presenter.IPresenter.IResetPasswordPresenter;

/**
 * Created by WangLei on 2018/2/2.
 */

public class ResetPasswordPresenter implements IResetPasswordPresenter{
    public Context context;
    public ResetPasswordPresenter(Context context){
        this.context=context;
    }

    @Override
    public void setTabSelected(LinearLayout ll, Button btn) {
        Drawable selectedDrawable = ResourceReader.readDrawable(context,
                R.drawable.shape_nav_indicator);
        int screenWidth = DensityUtils
                .getScreenSize(context)[0];
        int right = screenWidth / 2;
        selectedDrawable.setBounds(0, 0, right, DensityUtils.dipTopx(context, 3));
        btn.setSelected(true);
        btn.setCompoundDrawables(null, null, null, null);
        int size = ll.getChildCount();
        for (int i = 0; i < size; i++) {
            if (btn.getId() != ll.getChildAt(i).getId()) {
                ll.getChildAt(i).setSelected(false);
                ((Button) ll.getChildAt(i)).setCompoundDrawables(null,
                        null, null, null);
            }
        }
    }

    @Override
    public void getVcode(ACAccountMgr accountMgr, final Button btn, String account, int num) {
        int code = 1;
        if (num ==0 ) {
            code=1;
        }else if (num==1) {
            code=2;
        }
        accountMgr.sendVerifyCode(account,code, new VoidCallback() {
            @Override
            public void success() {
                Pop.popToast(context,
                        context.getString(R.string.vcode_send_success));
                CountDownTimerUtils mCountDownTimerUtils = new CountDownTimerUtils(
                        btn, 60000, 1000);
                mCountDownTimerUtils.start();
            }

            @Override
            public void error(ACException e) {
                DBOXException.errorCode(context, e.getErrorCode());
            }
        });
    }

    @Override
    public void resetPassword(ACAccountMgr accountMgr, String account, String pwd, String vcode) {
        accountMgr.resetPassword(account, pwd, vcode,
                new PayloadCallback<ACUserInfo>() {

                    @Override
                    public void error(ACException e) {
                        DBOXException.errorCode(context, e.getErrorCode());
                    }

                    @Override
                    public void success(ACUserInfo arg0) {
                        Toast.makeText(context,
                                context.getString(R.string.success_reset_password),
                                Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.setAction(BroadcastAction.resetPasswordSuccess);
                        context.sendBroadcast(intent);
                    }
                });
    }
}
