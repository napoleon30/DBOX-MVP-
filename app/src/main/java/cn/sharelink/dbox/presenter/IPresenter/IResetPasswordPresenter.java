package cn.sharelink.dbox.presenter.IPresenter;

import android.widget.Button;
import android.widget.LinearLayout;

import com.accloud.service.ACAccountMgr;

/**
 * Created by WangLei on 2018/2/2.
 */

public interface IResetPasswordPresenter {
    void setTabSelected(LinearLayout ll, Button btn);
    void getVcode(ACAccountMgr accountMgr,Button btn,String account,int num);
    void resetPassword(ACAccountMgr accountMgr,String account,String pwd,String vcode);
}
