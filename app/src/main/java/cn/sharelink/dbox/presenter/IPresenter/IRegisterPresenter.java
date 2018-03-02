package cn.sharelink.dbox.presenter.IPresenter;

import android.widget.Button;
import android.widget.LinearLayout;

import com.accloud.service.ACAccountMgr;

/**
 * Created by WangLei on 2018/2/1.
 */

public interface IRegisterPresenter {
    void setTabSelected(LinearLayout ll,Button btn);
    void register(ACAccountMgr accountMgr,String email,String phone,String pwd,String name,String vcode);
    void getVCode(ACAccountMgr accountMgr,Button btn,String account, int num);
}
