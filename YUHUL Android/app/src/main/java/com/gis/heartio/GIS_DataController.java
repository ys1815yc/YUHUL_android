package com.gis.heartio;

import com.gis.heartio.SupportSubsystem.SystemConfig;
import com.gis.heartio.SupportSubsystem.dataInfo;
import com.gis.heartio.UIOperationControlSubsystem.MainActivity;
import com.gis.heartio.UIOperationControlSubsystem.UserManagerCommon;

public class GIS_DataController {
    private final String TAG = getClass().getSimpleName();

    public dataInfo getMeasureResult() {
        dataInfo mDataInfo = new dataInfo();

        mDataInfo.HR = (int) SystemConfig.mDopplerInfo.HR;
        mDataInfo.Vpk = MainActivity.mVtiAndVpkResultByGIS.vpkResult;
        mDataInfo.VTI = MainActivity.mVtiAndVpkResultByGIS.vtiResult;

        double doubleRadius = UserManagerCommon.mDoubleUserPulmDiameterCm / 2.0;
        double doubleCSArea = doubleRadius * doubleRadius * Math.PI;
        mDataInfo.SV = mDataInfo.VTI * doubleCSArea;
        //mDataInfo.CO = mDataInfo.SV * mDataInfo.HR / 1000.0;
        mDataInfo.CO = mDataInfo.SV * SystemConfig.mDopplerInfo.HR / 1000.0;

        mDataInfo.ErrCode = MainActivity.mBVSignalProcessorPart1.mIntHRErrCode;
        GIS_Log.e(TAG, "error code = " + mDataInfo.ErrCode);

        return mDataInfo;
    }

}
