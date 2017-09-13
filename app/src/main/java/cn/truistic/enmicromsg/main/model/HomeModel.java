package cn.truistic.enmicromsg.main.model;

import android.content.Context;

import cn.truistic.enmicromsg.common.util.SharedPerfUtil;
import cn.truistic.enmicromsg.main.MainMVP;

/**
 * HomeModel
 */
public class HomeModel implements MainMVP.IHomeModel {

    private Context context;
    private MainMVP.IHomePresenter homePresenter;

    public HomeModel(MainMVP.IHomePresenter homePresenter, Context context) {
        this.homePresenter = homePresenter;
        this.context = context;
    }

    @Override
    public MainMVP.IHomeView.State getState(MainMVP.IHomeView.Progress progress) {
        switch (SharedPerfUtil.getState(context, progress)) {
            case 0:
                return MainMVP.IHomeView.State.UNDETECTED;
            case 1:
                return MainMVP.IHomeView.State.DETECTING;
            case 2:
                return MainMVP.IHomeView.State.TRUE;
            case 3:
                return MainMVP.IHomeView.State.FALSE;
        }
        return MainMVP.IHomeView.State.UNDETECTED;
    }

    @Override
    public void saveState(MainMVP.IHomeView.Progress progress, MainMVP.IHomeView.State state) {
        int stateInt = 0;
        switch (state) {
            case UNDETECTED:
                stateInt = 0;
                break;
            case DETECTING:
                stateInt = 1;
                break;
            case TRUE:
                stateInt = 2;
                break;
            case FALSE:
                stateInt = 3;
                break;
        }
        SharedPerfUtil.saveProgressState(context, progress, stateInt);
    }

    @Override
    public int getDbNum() {
        return SharedPerfUtil.getDbNum(context);
    }

    @Override
    public void saveDbNum(int num) {
        SharedPerfUtil.saveDbNum(context, num);
    }

    @Override
    public String getDbPwd() {
        return SharedPerfUtil.getDbPwd(context);
    }

    @Override
    public void saveDbPwd(String pwd) {
        SharedPerfUtil.savedbPwd(context, pwd);
    }
}
