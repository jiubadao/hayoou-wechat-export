package cn.truistic.enmicromsg.main;

import android.content.Context;

/**
 * MainMVP接口
 */
public interface MainMVP {

    interface IMainView {

    }

    interface IHomeView {

        enum Progress {DETECT_WECHAT, DETECT_ROOT, DETECT_PERMISSION, REQUEST_DATA, ANALYSIS_DATA}

        enum State {UNDETECTED, DETECTING, TRUE, FALSE}
//        void onOperateStart();


        void onDetectStop();

//        void showCancelDialog();
//
//        void showSuccessDialog(String msg);
//
//        void showFailureDialog(String msg);

        void setProgressState(Progress progress, State state);
    }

    interface IGroupsView {

    }

    interface IExportView {

    }

    interface IMainPresenter {

    }

    interface IHomePresenter {
        void detect();

//        boolean checkWechat();
//
//        boolean checkRoot();
//
//        boolean checkRootPermission();
//
//        boolean requestData();
//
//        boolean analysisData();

//        void updateProgressState(int progress, int State);
    }

    interface IGroupsPresenter {

    }

    interface IExportPresenter {

    }

    interface IHomeModel {
        IHomeView.State getState(IHomeView.Progress progress);

        void saveState(IHomeView.Progress progress, IHomeView.State state);

        int getDbNum();

        void saveDbNum(int num);

        String getDbPwd();

        void saveDbPwd(String pwd);
    }


}