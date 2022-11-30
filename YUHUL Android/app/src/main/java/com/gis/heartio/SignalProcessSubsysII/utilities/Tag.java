package com.gis.heartio.SignalProcessSubsysII.utilities;

public class Tag {

	//Mode
	public static int INT_PROCEDURE_MODE_PWR = 0;
	public static int INT_PROCEDURE_MODE_WU = 1;
	public static int INT_PROCEDURE_MODE_HUMAN = 0;
	public static int INT_PROCEDURE_MODE_FAKE = 1;

	//Windows
	public static int INT_WND_HAMMING = 0;
	
	//Position Index
	public static int INT_POS_TAGSIZE = 3;
	public static int INT_POS_IDX = 0;
	public static int INT_POS_VAL = 1;
	public static int INT_POS_VALext1 = 2;

	public static int mIntXYSIZE = 2;
	public static int mIntX = 0;
	public static int mIntY = 1;

	//SpectrumAmpArrayAllElementFilter Direct
	public static int INT_SPEC_FIL_VERTICAL = 0;
	public static int INT_SPEC_FIL_HORIZONTAL = 1;
	
	
	//Caculating Tag
	public static int INT_TEST = 0;
	public static int INT_BEFORE = 0;
	public static int INT_AFTER = 1;
	public static int INT_LARGER = 0;
	public static int INT_SMALLER = 1;
	public static int INT_FORWARD = 0;
	public static int INT_REWARD = 1;
	public static int INT_VALLEY = 0;
	public static int INT_PEAK = 1;
	public static int INT_SORT_MAX = 0;
	public static int INT_SORT_MIN = 1;

	public static int INT_VPK_CALCULATE = 0;
	public static int INT_VPK_MAPPING = 1;

	//BVCaculate Tag
	public static int INT_CAL_VTI_TAG = 7;
	public static int INT_CAL_VTI_START = 0;
	public static int INT_CAL_VTI_END = 1;
	public static int INT_CAL_VTI_BWR = 2;
	public static int INT_CAL_VTI_MPID = 3;
	public static int INT_CAL_VTI_VPK = 4;
	public static int INT_CAL_VTI_VPK_RAW = 5;
	public static int INT_CAL_VTI_VTI = 6;

	//Similiar Tag
	public static int INT_SIMILIAR_TAG = 2;
	public static int INT_SIMILIAR_MEAN = 0;
	public static int INT_SIMILIAR_SUM = 1;


	//VTI RESULT Tag
	public static int INT_RESULT_VTI_TAG = 5;
	public static int INT_RESULT_VTI_VTI_Peak = 0;
	public static int INT_RESULT_VTI_VTI_Sum = 1;
	public static int INT_RESULT_VTI_VTI_F = 2;
	public static int INT_RESULT_VTI_VF_s = 3;
	public static int INT_RESULT_VTI_VF_e = 4;


}
