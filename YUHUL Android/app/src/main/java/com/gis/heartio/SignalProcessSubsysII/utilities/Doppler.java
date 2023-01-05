package com.gis.heartio.SignalProcessSubsysII.utilities;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.gis.heartio.GIS_Algorithm;
import com.gis.heartio.SignalProcessSubsysII.parameters.BloodVelocityConfig;
import com.gis.heartio.SignalProcessSubsysII.transformer.FastDctLee;
import com.gis.heartio.SupportSubsystem.SystemConfig;
import com.gis.heartio.SupportSubsystem.Utilitys;
import com.gis.heartio.UIOperationControlSubsystem.MainActivity;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Doppler {
	//		 private boolean  HR_fail_flag=false;
//		 private boolean  SEG_fail_flag=false;
//		 private boolean  SNR_fail_flag=false;
//		 private double Heart_rate;
	private static final String TAG = "Doppler";
	private final Context context;
	public static boolean cavinTest = true;
	public static boolean cavinDCOffset = false;
	public static boolean usingNN = false;
	static double cavinMultiple = 1;
	static double cavinMultiple2 = 1;
	static double[] tmpIpc;

	public Doppler(Context context) {
		this.context = context;
	}

	public static double sum(double[] arr)
	  { 
	      double sum = 0; // initialize sum 
	      for (int i = 0; i < arr.length; i++) {
			  sum += arr[i];
		  }
	      return sum; 
	  }

	public static double[][] FFT2(double[] x, double[][] RE_M, double[][] IM_M)
	{
		int n = x.length;  // assume n is a power of 2

		double[] xre = new double[n];
		double[] xim = new double[n];
		double[][] result;
		result=new double[n][2];

		double  p, nu,arg, sum_re,sum_im;
		for (int i = 0; i < n; i++)
		{
			xre[i]= 0.0;
			xim[i]= 0.0;
		}
		double[][] re_M;


		for (int k = 0; k < n; k++)
		{
			sum_re=0;
			sum_im=0;
			{
				for (int i = 0; i < n; i++)
				{
					nu=x[i];
					sum_re=sum_re+RE_M[k][i]*nu;
					sum_im=sum_im+IM_M[k][i]*nu;
				}
			}

			result[k][0]=sum_re;
			result[k][1]=sum_im;
		}
		return result ;
	}
	public static double[] maxvote(double[] arr, double th)
	{
		int lng=arr.length;
		double[] vot_n =new double[lng];
		double[] vot_v =new double[lng];
		double[] result =new double[3];
		int temp=0;
		int mid;
		double mv,temp_v;
		double dif;
		mid=0;
		mv=arr[0];
		for (int i = 0; i < arr.length; i++)
		{
			if(arr[i]!=0)
			{
				temp=0;
				temp_v=0;
				for (int j = 0; j < arr.length; j++)
				{
					dif=  arr[i]-arr[j];
					if (Math.abs(dif)<(th/100)*arr[i])
					{
						temp=temp+1;
						temp_v=temp_v+arr[j];
					}
				}
				vot_n[i]=temp;
				vot_v[i]=temp_v;
			}
			else
			{
				vot_n[i]=0;
				vot_v[i]=0;
			}
		}
		mid=maximum_id(vot_n);
		mv=arr[mid];
		result[0]=vot_n[mid];
		result[1]=mv;
		result[2]=vot_v[mid]/(double)vot_n[mid];
		return result;
	}

	static int maximum_id(double[] a)
	{
		int index=0,i=1;
		double maximum=a[0];
		while(i<a.length)
		{
			if(maximum<a[i])
			{
				maximum=a[i];
				index=i;
			}
			i++;
		}
		return index;
	}

	static int min_id(double[] a)
	{
		int index=0,i=1;
		double minv=a[0];
		while(i<a.length)
		{
			if(minv>a[i])
			{
				minv=a[i];
				index=i;
			}
			i++;
		}
		return index;
	}

	public static double[][] normal(double[][] arr)
	{
		  int depth=arr.length;
		  int width=arr[0].length;
		  double maxValue = arr[0][0];
		  double minValue = arr[0][0];
		  double[][] o_arr;
	      o_arr=new double[depth][width];
		  for(int j=0;j < depth;j++)
		  for(int i=0;i < width;i++)
		    {
		      if(arr[j][i] > maxValue){
		         maxValue =arr[j][i]; }
		      if(arr[j][i] <minValue){
			         minValue =arr[j][i]; }
	        }
//		  System.out.println( "Max="+maxValue);
//		  System.out.println( "MIn="+minValue);
		  for(int j=0;j < depth;j++)
		  for(int i=0;i < width;i++)
		{ 
				o_arr[j][i]=((arr[j][i]-minValue)/(maxValue-minValue))*255.0;
//				System.out.println( "Value="+o_arr[j][i]);
		}
		  return o_arr;
	}

	public static int getHRByVFSNSI(double[][] arr){
		int depth=arr.length;
		int width=arr[0].length;
		double nv=0;
		double thv=90;     // threshold value for velocity

		double v_sum=0;
		double mc_sum=0;
		double max_pline,Mc,Mcy,max_array,Lsnr;
		double Lpower,Hpower,M_power,HRr;
		double[][] P_ratio,filter_o,normal_o;

		filter_o=arr;

		P_ratio=new double[depth][width];
		double[] im_col=new double[depth];
		double[] imvfp,imvf3,snsi_vf,Tpower;

		Tpower=new double[width];
		imvfp=new double[width];

//		max_array=getMax2D(filter_o);
		Lpower=0;
		Hpower=0;
		for (int ii=0;ii<width;ii++){
			im_col=column(filter_o,ii);
			Tpower[ii]=sum(im_col);
		}
		snsi_vf=GM_SNSI_VF(filter_o);

		for (int ii=0; ii<width;ii++) {
			im_col=column(filter_o,ii);
//			max_pline=getMax(im_col);
//			Lsnr=max_pline/max_array;

			for (int ki=0; ki<64;ki++) {
				Lpower=im_col[ki]+Lpower;
			}
			for (int ki=0; ki<65;ki++) {
				Hpower=Hpower+im_col[ki+64];
			}

			v_sum=0;
			mc_sum=0;
			for (int jj=0;jj<depth;jj++) {
				v_sum=v_sum+im_col[jj];
				mc_sum=mc_sum+(im_col[jj]*(jj+1));
				P_ratio[jj][ii]=(v_sum/Tpower[ii])*100;
				if ((jj>1)&&(P_ratio[jj][ii]>thv)&&(P_ratio[jj-1][ii]<thv)) {
					nv=jj;
				}
			}

			if (cavinTest){
				if (Tpower[ii]>cavinMultiple2*(getMax(Tpower))){
					imvfp[ii]=nv;
				} else {
					imvfp[ii]=0;

				}
			} else{
				if (Tpower[ii]>0.02*(getMax(Tpower))) {
					imvfp[ii]=nv;
				}else{
					imvfp[ii]=0;
				}
			}

			Mc=mc_sum/(Tpower[ii]);
			int Mcx;
			if (Mc>1){
				Mcx=(int)Mc;
			}
			else{
				Mcx=0;
			}
			Mcy=P_ratio[Mcx][ii];

			Lpower=0;
			Hpower=0;
		}

		imvf3=MOV_AVG(imvfp, 6);   // cavin test 20210702

		double[][] HR_buf;

		HR_buf=HR3_test(imvf3);
		double HR_int=1.0;
		if (HR_buf[0][0]!=1){
			HR_int=HR_buf[0][0];
		}
		HRr=((double)125/HR_int)*60;
		return (int)HRr;
	}

	public static double[][] VF_SNSI(double[][] arr)
	{
		int depth=arr.length;
		double sp_lng;
		int width=arr[0].length;
		double na=0,nb=0,nv=0;
		double thv=90;     // threshold value for velocity
		double tha=90;     // threshold value(H) for angle estimation
		double thb=50;     // threshold value(L) for angle estimation
		double v_sum=0;
		double mc_sum=0;
		double max_pline,Mc,Mcy,max_array,Lsnr;
		double Lpower,Hpower,M_power,HRr;
		double[][] P_ratio,result,filter_o,normal_o;
		int[][] HR_seg;
//		double[][] HR_seg;
//		  normal_o=normal(arr);
//		  filter_o=spectrum_filter(arr);
		filter_o=arr;
//		  arr=filter_o;
		P_ratio=new double[depth][width];
		result=new double[60][width];
		double[] im_col=new double[depth];
		double[] HF_col=new double[65];
		double[] LF_col=new double[64];
		double[] snr=new double[width];
		double[] snrf=new double[width];
		double[] snr_id=new double[width];
		double[] imvf,imvfp,imvfg,imvf2,imvf3,snsi_vf,Tpower,MTP,ang,cout;
		double[] seg_id=new double[width];
		imvf=new double[width];
		Tpower=new double[width];
		MTP=new double[width];
		ang=new double[width];
		imvfp=new double[width];
		imvf2=new double[width];
		imvfg=new double[width];
		snsi_vf=new double[width];
//	      cout=new double[width];
		int snr_fail=0;
		max_array=getMax2D(filter_o);
		Lpower=0;
		sp_lng=depth;
		Hpower=0;
		for (int ii=0;ii<width;ii++)
		{
			im_col=column(filter_o,ii);
			Tpower[ii]=sum(im_col);
		}
		snsi_vf=GM_SNSI_VF(filter_o);

		for (int ii=0; ii<width;ii++)
		{
			im_col=column(filter_o,ii);
			max_pline=getMax(im_col);
			Lsnr=max_pline/max_array;

			for (int ki=0; ki<64;ki++)
			{
				Lpower=im_col[ki]+Lpower;
			}
			for (int ki=0; ki<65;ki++)
			{
				Hpower=Hpower+im_col[ki+64];
			}
//	    	  Tpower[ii]=sum(im_col);
			snr[ii]=((Lpower-Hpower)/Tpower[ii])*100;
			v_sum=0;
			mc_sum=0;
			for (int jj=0;jj<depth;jj++)
			{
				v_sum=v_sum+im_col[jj];
				mc_sum=mc_sum+(im_col[jj]*(jj+1));
				P_ratio[jj][ii]=(v_sum/Tpower[ii])*100;
				if ((jj>1)&&(P_ratio[jj][ii]>thv)&&(P_ratio[jj-1][ii]<thv))
				{
					nv=jj;
				}
			}

			// Cavin Test 20210507
			if (cavinTest){
				if (Tpower[ii]>cavinMultiple2*(getMax(Tpower))
//						|| Tpower[ii]*cavinMultiple<(getMax(Tpower))
				){
					imvfp[ii]=nv;
				} else {
					imvfp[ii]=0;
					// Cavin closed 20210903
//					snsi_vf[ii]=0;
				}
			} else{
				// Cavin close for test 20210507
				if (Tpower[ii]>0.02*(getMax(Tpower)))
//				if (Tpower[ii]>0.03*(getMax(Tpower)))
//	    	  if (snr[ii]>50)
				{
					imvfp[ii]=nv;
				}
				else
				{
					imvfp[ii]=0;
					// Cavin closed 20210903
//					snsi_vf[ii]=0;
				}
				// Cavin close for test 20210507
			}

			Mc=mc_sum/(Tpower[ii]);
			int Mcx;
			if (Mc>1)
			{
				Mcx=(int)Mc;
			}
			else
			{
				Mcx=0;
			}
			Mcy=P_ratio[Mcx][ii];
//	    	  double ldx,ldy,pdx,pdy,Ling,png,cosv,sinv,GMv;

//	    	  double[] dng=new double[129];
//	    	  for (int jj=0;jj<129;jj++)
//	    	  {
//	    	    dng[jj]=0;
//	    	  }
//	    	  ldx=129-(Mc);
//	    	  ldy=100-Mcy;
			//          Ling=Math.sqrt(ldx*ldx+ldy*ldy);
//         for (int jj=((int)Mc+1);jj<depth;jj++)
//	    	  {
//	    	    pdy=P_ratio[jj][ii]-Mcy;
//	    	    pdx=jj-Mc;
//	    	    png=Math.sqrt(pdx*pdx+pdy*pdy);
//	    	    cosv=(ldx*pdx+ldy*pdy)/(png*Ling);
//	    	    sinv=Math.sqrt(1-cosv*cosv);
//	    	    dng[jj]=png*sinv;
//	    	  }
//           GMv=maximum_id(dng);


			Lpower=0;
			Hpower=0;
			mc_sum=0;
			v_sum=0;
			if ((Mcy>10)&&(Lsnr>0.1))
//	    	  if (snr[ii]>50)
			{
				imvf[ii]=snsi_vf[ii];
//	    		  imvf[ii]=3.3*Mc;
			}
			else
			{
				imvf[ii]=snsi_vf[ii];
//	    	  imvf[ii]=imvfp[ii];
//	    		  imvf[ii]=3.3*Mc;
			}
//	    	  ang[ii]=((na-nb)/nb);
			result[0][ii]=imvf[ii]; //
			result[1][ii]=snsi_vf[ii]; //SNSI算出來的流速頻率(fmax)
			result[2][ii]=imvfp[ii]; //vfp VF的power積分
//	    	  result[1][ii]=Math.atan(ang[ii]*2.7)*180/3.14159;
//	    	  if (100*snr<2)
//	    	  {
//	    	  result[2][ii]=0;
//	    	  snr_fail=snr_fail+1;
//	    	  }
//	    	  else
//	    	  {
			result[3][ii]=snr[ii]; //訊號和雜訊的比值(訊雜比)
//	    	  }

		}

//	      imvf2=frequency_to_velocity(imvf,0);
		M_power=getMax(Tpower);
		for (int ii=0; ii<width;ii++)
		{
//	    	  result[2][ii]=imvf2[ii];
			// Cavin Test 20210507
			if (cavinTest){
				if ((Tpower[ii]<M_power*cavinMultiple2)){
//				  Log.d("Doppler","mtRatio = "+mtRadio +",   ii = "+ii);
					imvf[ii]=0;
//	      	  ang[ii]=0;
					result[0][ii]=0;
					result[1][ii]=0;
					result[2][ii]=0;
					result[3][ii]=0;
					snr[ii]=0;
					snr_fail=snr_fail+1;
				}
			}else {
				// Cavin close for test 20210507
//				if ((Tpower[ii]<M_power*0.05)||(snr[ii]<95))
					if ((Tpower[ii]<M_power*0.02))
				{
					imvf[ii]=0;
//	      	  ang[ii]=0;
					result[0][ii]=0;
					result[1][ii]=0;
					result[2][ii]=0;
					result[3][ii]=0;
					snr[ii]=0;
					snr_fail=snr_fail+1;
				}
				else
				{
					result[3][ii]=100;
					snr[ii]=100;
				}
				// Cavin close for test 20210507
			}

		}

		if ((snr_fail>1000)||(snr_fail<100))
		{
			result[4][1]=1;  // Set up SNR_fail_flag
//	    	  SNR_fail_flag=true;
		}
		else
		{
			result[4][1]=0; // Set up SNR_fail_flag
//	    	  SNR_fail_flag=false;
		}

		result[4][0]=snr_fail;
		MTP=MOV_AVG(Tpower,10);
//		imvfg=MOV_AVG(imvf,10);    // imvf2: GM_90% combined velocity profile
		imvfg=MOV_AVG(imvf,6);    // cavin test 20210702
//		imvf3=MOV_AVG(imvfp,10);   // imvfp: 90% velocity profile
		imvf3=MOV_AVG(imvfp, 6);   // cavin test 20210702
		snrf=MOV_AVG(snr,15);
//	      seg_id=PN_Sequence_filer(imvfg);
	      seg_id=PN_Sequence_filer(MTP);
//	      double[] temp_cor=new double[700];
//	      int st=100;
//	      for (int ti=st;ti<700;ti++)
//	      {
//	    	  temp_cor[ti-st]=imvf3[ti];
//	      }
// 	  cout=Cor(temp_cor, 250, 10);
// 	  double[][] pk_buf;
// 	  pk_buf=f_peak(cout,10,200);
		double[][] HR_buf;
// 	  npk_buf=f_npeak(imvf3,10,20);
// 	  npk_buf=HR(imvf3,10,20);
		HR_buf=HR3_test(imvf3);
		double HR_int=HR_buf[0][12];
		HRr=((double)125/HR_int)*60;
// 	  HRr=150.5474;
		result[4][4]=HRr;
		if (cavinTest){
			imvf2=frequency_to_velocity_Cavin(imvfg, arr);
		}else{
			imvf2=frequency_to_velocity(imvfg,HRr);
		}

		for (int ii=0; ii<imvf3.length;ii++)
		{
			result[0][ii]=imvfg[ii];
			result[2][ii]=imvf2[ii];	// Cavin Added for test 20210514
			result[5][ii]=imvf3[ii];
			result[6][ii]=seg_id[ii];
		}
//	      int kk=0;
//	      while (HR_buf[0][kk]!=0)
//	      {
//	    	  kk=kk+1;
//	      }
//	      result[4][1]=kk;

//	      for (int ii=0; ii<pk_buf[0].length;ii++)
//		  {
//	    	  result[5][ii]=pk_buf[0][ii];
//	    	  result[6][ii]=pk_buf[1][ii];
//		  }
		for (int ii=0; ii<HR_buf.length;ii++)
		{
			for (int k=0; k<13;k++)
			{
				result[ii+7][k]=HR_buf[ii][k];
			}
		}

//	      Heart_rate=1/(HR_int*64/8000);
		if (HR_int==0)
		{
			result[4][3]=1;  // Set up SEG_fail_flag
//	    	  HR_fail_flag=true;
		}
		else
		{
			result[4][3]=0; // Set up SEG_fail_flag
//	    	  HR_fail_flag=false;

		}
//		HR_seg=HR_seg_PID_in(seg_id,HR_int);
		if (HR_int!=0){
			HR_seg = HR_SEG_0630(HR_int,Tpower,imvf2);

			//HR_seg=HR_SEG(HR_int,Tpower);
			if (HR_seg.length==0)
			{
				result[4][2]=1;  // Set up SEG_fail_flag
//	    	  SEG_fail_flag=true;
			}
			else
			{
				result[4][2]=0; // Set up SEG_fail_flag
//	    	  SEG_fail_flag=false;
				// Cavin added 20210903
				// reset imvf2
				for (int i=0;i<HR_seg.length;i++){
					int start = HR_seg[i][0];
					int end = HR_seg[i][1];
					for (int j=start;j<end;j++){
						imvf[j] = snsi_vf[j];
					}
				}
				imvfg=MOV_AVG(imvf,6);    // imvf2: GM_90% combined velocity profile
				if (!usingNN){
					if (cavinTest){
						imvf2=frequency_to_velocity_Cavin(imvfg, arr);
					}else{
						imvf2=frequency_to_velocity(imvfg,HRr);
					}
				}else{
					// Cavin testNN 20211201
					neuroNetwork myNN = new neuroNetwork();
					//myNN.testNet();
//					double[] xinmax = {128.7504, 125.1729, 79.1101};
					double[] xinmax = {70, 70, 129};
//					double[] xinmin = {14, 9, 10.4535};
					double[] xinmin = {0, 0, 0};
					int xoutmax = 1;
					int xoutmin = -1;
					double yinmax = 1.20692600456032;
					double yinmin = 0.149207885179161;
					int youtmax = 1;
					int youtmin = -1;
					double[][] test_in = new double[3][width];
					double[][] nnInput = MC_Smax_VF(arr);

					double[] tmpMC = MOV_AVG(nnInput[0],10);
					double[] tmpSmax = MOV_AVG(nnInput[1],10);
					double[] tmpVF = MOV_AVG(snsi_vf,10);

					System.arraycopy(tmpMC,0,nnInput[0],0,tmpMC.length);
					System.arraycopy(tmpSmax,0,nnInput[1],0,tmpSmax.length);
					System.arraycopy(tmpVF,0,nnInput[2],0,tmpVF.length);

//		test_in=(xoutmax-xoutmin)./(xinmax-xinmin).*(Train_data-xinmin)+xoutmin;
					for (int ii=0;ii<width;ii++){
						double dividendX = xoutmax-xoutmin;
						double[] divisorX = new double[3];
						double[] multiplicandX = new double[3];
						for (int jj=0;jj<divisorX.length;jj++){
							divisorX[jj] = xinmax[jj]-xinmin[jj];
							multiplicandX[jj] = dividendX / divisorX[jj];
							test_in[jj][ii] = multiplicandX[jj]*(nnInput[jj][ii]-xinmin[jj])+xoutmin;
							if(ii>100&&ii<120){
								Log.d("Doppler","nnInput["+jj+"]["+ii+"]="+nnInput[jj][ii]);
							}
						}

					}
					double[] ym = myNN.net_8L(test_in);
					double[] yout = new double[width];
//					double[] ym1 = MOV_AVG(ym,9);
//		yout=(yinmax-yinmin)./(youtmax-youtmin).*(ym-youtmin)+yinmin;
					for (int ii=0;ii<width;ii++){
						double dividendY = yinmax-yinmin;
						double divisorY = youtmax-youtmin;
						double multiplicandY = dividendY/divisorY;
						yout[ii] = multiplicandY * (ym[ii] - youtmin)+yinmin;
					}
					double[] yout1 = MOV_AVG(yout,5);
//					yout1 = MOV_AVG(yout,9);

					for (int ii=0;ii<width;ii++){
						imvfg[ii] = (129/2)*yout1[ii];
						imvf2[ii] = yout1[ii];
						Log.d("Doppler","imvf2["+ii+"]="+imvf2[ii]
//								+" ,yout["+ii+"]="+yout[ii]
						);
					}
					System.arraycopy(imvf2,0,result[2],0,imvf2.length);
				}
				System.arraycopy(imvfg, 0, result[0], 0, imvf3.length);
				// Cavin added 20210903 end
			}

			double VTI=0,VPK=0;
			double [] vpk_vti=new double[2];
			double [] vpk=new double[HR_seg.length];
			double [] vti=new double[HR_seg.length];
			for (int ii=0; ii<HR_seg.length;ii++)
			{
				vpk_vti=VPK_VTI(imvf2,(int)HR_seg[ii][0],(int)HR_seg[ii][1],0);

				for (int k=15; k<17;k++)
				{
					result[ii+7][k]=HR_seg[ii][k-15];
				}
				if(HR_seg[ii][0]!=0)
				{
					result[ii+7][17]=vpk_vti[0];
					result[ii+7][18]=vpk_vti[1];
					vpk[ii]=vpk_vti[0];
					vti[ii]=vpk_vti[1];
//					if (vpk[ii]>VPK) {
//						VPK = vpk[ii];
//						VTI = vti[ii];
//					}
//					if (vti[ii]>VTI) {
//						VPK = vpk[ii];
//						VTI = vti[ii];
//					}
					if (SystemConfig.mIntSingleVpkEnabled == SystemConfig.INT_SINGLE_VPK_ENABLED_YES){
//							VTI = VPK_Org(imvfg,HR_seg[ii][0],(int)HR_seg[ii][1],0);
						VTI = VPK_Org(MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdxByMovingAverage,HR_seg[ii][0],HR_seg[ii][1],0);
						int vpkIndex = VPK_index(MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdxByMovingAverage,HR_seg[ii][0],HR_seg[ii][1],0,(int)VTI);
						im_col=column(arr,vpkIndex);
						tmpIpc=IPC(im_col);
//						System.out.print("IPC:[");
//						for (int k=0;k<tmpIpc.length;k++) {
//							System.out.print(tmpIpc[k]+" ,\n");
//						}
//						System.out.println("]");
					}else{
						VTI = vti[0];
					}
					VPK = vpk[0];
				}
			}

//			double[] maxvote_1=new double[3];
//			double VTI,VPK;
//	      maxvote_1=maxvote(vpk,2);
//		  VPK=maxvote_1[1];
//			VPK=getMax(vpk);
//		  maxvote_1=maxvote(vti,2);
//		  VTI=maxvote_1[1];
//			VTI=getMax(vti);
			System.out.print(" Heart_rate =" );
			System.out.print(HRr );
			System.out.print(" VTI =" );
			System.out.print(VTI );
			System.out.print(" VPK =" );
			System.out.print(VPK );
			result[4][5]=VPK;
			result[4][6]=VTI;
		}


		return result;
	}

	public static void exportIPC(Activity ctx){
		String exportDirPath = Utilitys.getDownloadBaseFilePath(ctx);
		File exportDir = new File(exportDirPath,"");
		if (!exportDir.exists()){
			exportDir.mkdir();
		}
		SimpleDateFormat df;

		//df = new SimpleDateFormat("yyyyMMdd_HHmmssSSS");
		df = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String strDate = df.format(new Date());
		String filename = "tmpIPC_"+ strDate +".csv";
		File exportFile = new File(exportDir,filename);
		try {
			if (exportFile.createNewFile()){
				CSVWriter csvWriter = new CSVWriter(new FileWriter(exportFile));

				String[] strTitle = {   "IPC "};
				csvWriter.writeNext(strTitle);
				for (int i=0;i<tmpIpc.length;i++){
					// while (curCSV.moveToNext()){
					String[] arrStr = {""+tmpIpc[i]};
					csvWriter.writeNext(arrStr);
				}
				csvWriter.close();
			}else{
				Log.d("Doppler","file "+filename+" existed.");
			}
		}catch(Exception sqlEx){
			sqlEx.printStackTrace();
		}
	}

	public static double[] GM_SNSI_VF(double[][] arr)
	{
		int depth=arr.length;
//	   double sp_lng;
		int width=arr[0].length;
		double[] im_col=new double[depth];
		double[] ipc=new double[depth];
		double[] SNSI_vf,Tpower;
		double[] result;
		double Mpower;
		SNSI_vf=new double[width];
		Tpower=new double[width];
		result=new double[width];
		int vf_temp;
		for (int ii=0;ii<width;ii++)
		{
			im_col=column(arr,ii);
			Tpower[ii]=sum(im_col);
		}
		Mpower=getMax(Tpower);
		if (cavinTest){
			double min_power = getNonZeroMin(Tpower);
			Methodoligies mMeth = new Methodoligies();
			double[] sortedTpower = mMeth.getArrSort(Tag.INT_SORT_MIN,Tpower);
			double mean_power = mean(Tpower);

			double[] tmpMinTpower = new double[(int)(sortedTpower.length*0.4)];
			System.arraycopy(sortedTpower,0,tmpMinTpower,0,tmpMinTpower.length);
			double meanMinPower = mean(tmpMinTpower);

			cavinMultiple = Mpower / ((mean_power+Mpower)/2);
			cavinMultiple2 = meanMinPower/Mpower;
			Log.d("Doppler","mean_power="+mean_power+", min_power="+min_power
					+", cavinMultiple="+cavinMultiple+", cavinMultiple2="+cavinMultiple2);
		}

		for (int ii=0; ii<width;ii++)
		{
			im_col=column(arr,ii);
			ipc=IPC(im_col);

			// Cavin test 20210510
			if (cavinTest){
				if (Tpower[ii]>Mpower*cavinMultiple2){
					vf_temp = GM_SNSI(ipc);
				}else{
					vf_temp = 0;
				}
			}else{
				// Cavin close for test 20210510
				if (Tpower[ii]>0.02*Mpower)
				{
					vf_temp=GM_SNSI(ipc);
				}
				else
				{
					vf_temp=0;
				}
				// Cavin close for test 20210510 end
			}

			result[ii]=vf_temp+1;
//	    	  result[ii][1]=Tpower[ii];
		}
//	      for (int ii=0; ii<width;ii++)
//		  {
//		      if (Tpower[ii]<0.03*Mpower)
//		      {
//		    	  result[ii][0]=0;
//		      }
//		  }


		return result;
	}

	public static int GM_SNSI(double[] arr)
	{
		int lng=arr.length;
		double[] Slope;
		Slope=new double[lng];
		double result;
//	   result=new double[6];
		for(int i=1;i < lng-1;i++)
		{
			Slope[i]=(arr[i+1]-arr[i-1])/2;
		}
		int Fsx;
		double Fsy;
		double ldx,ldy,pdx,pdy,Ling,png,cosv,sinv,GMx,GMy,Nsx,Nsy,Ms,Mn,Msn,xx;
		double[] dng=new double[lng];
		Slope[0]=Slope[1];
		Slope[lng-1]=Slope[lng-2];
		Fsx=maximum_id(Slope);
		Fsy=arr[Fsx];
		ldx=lng-Fsx;
		ldy=1-Fsy;
		Ling=Math.sqrt(ldx*ldx+ldy*ldy);
		for (int jj=0;jj<=Fsx;jj++)
		{
			dng[jj]=0;
		}
		for (int jj=(Fsx+1);jj<lng;jj++)
		{
			pdy=arr[jj]-Fsy;
			pdx=jj-Fsx;
			png=Math.sqrt(pdx*pdx+pdy*pdy);
			cosv=(ldx*pdx+ldy*pdy)/(png*Ling);
			sinv=Math.sqrt(1-cosv*cosv);
			dng[jj]=png*sinv;
		}
		GMx=maximum_id(dng);
		GMy=arr[(int)GMx];
		Nsx=2*GMx-Fsx;
		if (Nsx>=(lng-1))
		{
			Nsx=lng-1;
		}
		Nsy=arr[(int)Nsx];
		Ms=Slope[Fsx];
		Mn=(1-Nsy)/(lng-Nsx);
		double[] XXI={0, 13.8, 14.5, 16.05,18,19.7,20,50};
		double[] YYI={ 0.001,0.0025,0.004,0.05,0.25,0.28,0.35,0.4};

		if (cavinTest){
			xx=0.1;		// CavinTest
		}else{
			xx= Mapping(XXI,YYI,(GMx-Fsx));
		}
		Msn=xx*Ms+(1-xx)*Mn;
//		Log.d("Doppler","xx="+xx);
//    System.out.println( "  Msn="+Msn+"  Ms="+Ms+"  Mn="+Mn+"  Fsx="+Fsx+"  Fsy="+Fsy+"  GMx="+GMx+"  GMy="+GMy+"  Nsx="+Nsx+"  Nsy="+Nsy);
		int kk=(int)Nsx;
		if (Nsx>0){
			kk=(int)Nsx-1;
		}
		if (kk>0&&!Double.isNaN(Msn)){
			while (Slope[kk]<Msn && kk>GMx)
			{
				kk=kk-1;
			}
		}

		return kk;
	}

 static  public  double[][] MC_Smax_VF(double[][] arr)
 {
  int depth=arr.length;
//  double sp_lng;
  int width=arr[0].length;
  double[] im_col=new double[depth];
  double[] im_col_mag=new double[depth];
  double[] imc=new double[depth];
  double[] SVF,MC,Tpower,SMAX;
  double[][] result;
  double Mpower,Mspec;
  int Smax,p05,p95;
  SVF=new double[width];
  MC=new double[width];
  SMAX=new double[width];
  Tpower=new double[width];
  result=new double[3][width];
  for (int ii=0;ii<width;ii++)
	  {
   	  im_col=column(arr,ii);
   	  im_col_mag=MOV_AVG2(im_col,5);
   	  for(int jj=0;jj<depth;jj++)
   	  {
   		  imc[jj]=(jj+1)*im_col[jj];
   	  }
   	  p05=Percent(im_col,0.05);
   	  p95=Percent(im_col,0.95);
   	  double[] imc2=new double[p95-p05+1];
   	  double[] imc3=new double[p95-p05+1];
//   	  if (p05==0){
//   	  	p05=1;
//	  }
//   	  System.arraycopy(im_col,p05-1,imc2,0,imc2.length);
//   	  for (int jj=0;jj<imc3.length;jj++){
//   	  	imc3[jj] = imc2[jj]*(jj+1);
//	  }
   	  for(int jj=p05;jj<p95+1;jj++)
   	  {
   		  imc2[jj-p05]=im_col[jj];
   		  imc3[jj-p05]=im_col[jj]*(jj- p05 +1);
   	  }
		  Tpower[ii]=sum(im_col);	// Cavin test 20211207
		  if(Tpower[ii]==0){
		  	Tpower[ii]=0.0000001;
		  }
//   	  MC[ii]=(sum(imc3)/sum(im_col))+p05+1;
		  MC[ii]=(sum(imc3)/Tpower[ii])+p05+1;
   	  Mspec=getMax(im_col_mag);
   	  Smax=maximum_id(im_col_mag)+1;
   	  SMAX[ii]=Smax;
   	  int kk=depth-1;
   	  int fg=1;
   	  while ((im_col_mag[kk]<0.1*Mspec)&&(im_col_mag[kk-1]<0.1*Mspec)&&(kk>Smax))
//   	  while(fg==1)
         {
   		  kk=kk-1;
//            if ((kk<=Smax)||((im_col_mag[kk]>0.1*Mspec)&&(im_col_mag[kk-1]>0.1*Mspec)))
//            {
//           	 fg=0;
//            }
         }
   	  SVF[ii]=kk+1;
	  }
	   Mpower=getMax(Tpower);
     for (int ii=0; ii<width;ii++)
	  {
	  	if(cavinTest){
			if (Tpower[ii]>Mpower*cavinMultiple2) {
				result[0][ii]=0;
				result[1][ii]=0;
				result[2][ii]=0;

			}else{
				result[0][ii]=MC[ii];
				result[1][ii]=SMAX[ii];
				result[2][ii]=SVF[ii];
//			  result[0][ii]=SVF[ii];
//			  result[1][ii]=SMAX[ii];
//			  result[2][ii]=MC[ii];
			}
		}else{
			if ((Tpower[ii]<0.02*Mpower))
			{
				result[0][ii]=0;
				result[1][ii]=0;
				result[2][ii]=0;

			}
			else
			{
				result[0][ii]=MC[ii];
				result[1][ii]=SMAX[ii];
				result[2][ii]=SVF[ii];
//			  result[0][ii]=SVF[ii];
//			  result[1][ii]=SMAX[ii];
//			  result[2][ii]=MC[ii];
			}
		}

	  }
  return result;
 }

 public static int Percent(double[] arr, double Per_set)
 {
	   int lng=arr.length;
	   double sum=0;
	   double[] Per;
	   Per=new double[lng];
	   double[] SV;
	   SV=new double[lng];
	   for(int i=0;i < lng;i++)
	    {
	       sum=sum+arr[i];
	       SV[i]=sum;
	     }
	   for(int i=0;i < lng;i++)
	    {
	       Per[i]=SV[i]/sum;
	     }
	    int k=0;

	    while (Per[k]<Per_set && k<lng)
		   {
			 k=k+1;
		    }
	   return k;

 }


	  public static double[] MOV_AVG2(double[] arr, float Mn) // MN: 為奇數
	   {
		   int lng=arr.length;
		   int k;
		   k=Math.floorMod((int)Mn,2);
		   double sum=0;
		   double[] MA_out;
		   MA_out=new double[lng];
		   if (k==1)
		 {
//		   int mid=(int)(Math.floor((Mn/2)+0.5));
		   for(int i=0;i < Mn;i++)
		    {
		       MA_out[i]=arr[i];
		     }
		   for(int i=(lng-(int)Mn);i < lng;i++)
		    {
		       MA_out[i]=arr[i];
		     }
		   for(int i=(int)Mn;i < (lng-(int)Mn);i++)
		    {
		       int ld=(int)Math.floor(Mn/2);
			   for(int j=(i-ld);j<(i+ld+1);j++)
		       {
		    	   sum=sum+arr[j];
		       }
		       MA_out[i]=sum/Mn;
		       sum=0;
		     }
		    }
		   else
		   {
			int h1=(int)Math.floor(Mn/2);
			for(int i=0;i < h1;i++)
		    {
		       MA_out[i]=arr[i];
		     }
			for(int i=(lng-h1-1);i <lng;i++)
		    {
		       MA_out[i]=arr[i];
		     }
			for(int i=h1;i < (lng-h1-1);i++)
		    {

			   for(int j=(i-h1);j<(i+h1);j++)
		       {
		    	   sum=sum+arr[j];
		       }
		       MA_out[i]=sum/Mn;
		       sum=0;
		     }

		   }

		   return MA_out;
		}




	public static double Mapping(double[] arr_x,double[] arr_y,double X_in)
	{
		int lng=arr_x.length;
		double y_out=0;
		if (X_in<=arr_x[0])
		{
			y_out=arr_y[0];
		}
		if (X_in>=arr_x[lng-1])
		{
			y_out=arr_y[lng-1];
		}
		if (X_in>arr_x[0] && X_in<arr_x[lng-1])
		{
			int k=0;
			while (k<lng && arr_x[k]<=X_in)
			{
				k=k+1;
			}
			if (k>1 && k<lng)
			{
				y_out=((X_in-arr_x[k-1])/(arr_x[k]-arr_x[k-1]))*(arr_y[k]-arr_y[k-1])+arr_y[k-1];
			}
		}
		return y_out;
	}

	public static double[] IPC(double[] arr)
	{
		int lng=arr.length;
		double sum=0;
		double[] Per;
		Per=new double[lng];
		double[] SV;
		SV=new double[lng];
		for(int i=0;i < lng;i++)
		{
			sum=sum+arr[i];
			SV[i]=sum;
		}
		for(int i=0;i < lng;i++)
		{
			Per[i]=SV[i]/sum;
		}
		return Per;
	}

	public static GM3Result GM3(double[] p_in){
	  	double tm = sum(p_in);
	  	int lng = p_in.length;
	  	double[] p_perc = new double[lng];
//	  	double[] ms = new double[lng];
	  	double[][] fpkResult = fpk_snsi(p_in,5,1);
	  	Methodoligies md = new Methodoligies();
	  	double[] maxY = md.getArrMaxPos(fpkResult[1],0,(fpkResult[1].length-1));
	  	GM3Result gm3Result = new GM3Result();
	  	gm3Result.pcx = (int)fpkResult[0][(int)maxY[0]];
	  	int jk = gm3Result.pcx;
		while (jk<p_in.length && p_in[jk] > (0.7*maxY[1])){
			jk +=1;
		}
		gm3Result.se = jk;
		gm3Result.st = (int)Math.floor(0.3*(double)gm3Result.pcx);
		double TP1 = tm/129;
		double p99 = 129;
//		ms[0] = p_in[0];
		for (int i=0;i<lng;i++){
//			ms[i] = p_in[i]*i;
			double[] seg_i = new double[i+1];
			System.arraycopy(p_in,0,seg_i,0,i+1);
			p_perc[i] = (sum(seg_i)/tm)*100;
		}
//		double mcx = sum(ms)/tm;
//		double mcxx = Math.floor(mcx);
		int gsx = gm3Result.se;
		double gsy = p_perc[gsx];
		double dfx0 = p99 - gsx;
		double dfy0 = 100 - gsy;
		double v1_lng = Math.pow((Math.pow(dfx0,2)+Math.pow(dfy0,2)),0.5);
		double mL_ang = dfy0/v1_lng;
		double dfy1, dfx1;
		int lng2 = (int)(p99-gsx);
		double[] v_dis = new double[lng2];
		for (int i=0;i<(p99-gsx);i++){
			dfy1 = p_perc[gsx+i] - gsy;
			dfx1 = i+1;
			double v2_lng = Math.pow((Math.pow(dfx1,2)+Math.pow(dfy1,2)),0.5);
			double v1dv2 = dfx0*dfx1+dfy0*dfy1;
			double cos_v = v1dv2/(v1_lng*v2_lng);
			double sin_v = Math.pow((1- Math.pow(cos_v,2)),0.5);
			v_dis[i] = v2_lng*sin_v;
		}

		double[] vdisMax = md.getArrMaxPos(v_dis,0,(v_dis.length-1));
		gm3Result.GMx = (int)Math.floor(vdisMax[0]+gsx);
		gm3Result.GMy = p_perc[gm3Result.GMx]/100;

		gm3Result.ns = gm3Result.GMx+gm3Result.GMx-gm3Result.se;
		if (gm3Result.ns>=129){
			gm3Result.ns=100;
		}
		double TP2 = md.getArrSum(p_in,gm3Result.ns,129)/(129-gm3Result.ns);
		gm3Result.pSNR = (TP1-TP2)/TP2;
		if ((gm3Result.GMx-gm3Result.se)<((129-gm3Result.se)/3)) {
			gm3Result.G_fg = 1;
		}else{
			gm3Result.G_fg = 0;
		}

	  	return gm3Result;
	}

	public static MCBWResult MC_BW(double[] data_in){
	  	MCBWResult result = new MCBWResult();
	  	result.p05 = Percent(data_in,0.05);
	  	result.p95 = Percent(data_in,0.95);
	  	int dataLen = result.p95-result.p05+1;
	  	double[] data = new double[dataLen];
	  	System.arraycopy(data_in,result.p05,data,0,dataLen);
	  	double[] i_data = new double[dataLen];
	  	for (int i=0;i<dataLen;i++){
	  		i_data[i] = (i+1)*data[i];
		}
	  	double t_power = sum(data_in);
	  	result.mc = (sum(i_data)/t_power)+result.p05;
	  	double[] mc2s = new double[dataLen];
	  	for (int i=0;i<dataLen;i++){
	  		mc2s[i] = Math.pow(((i+1)-result.mc),2)*data[i];
		}
	  	result.bw = Math.sqrt((sum(mc2s)/t_power));
	  	return result;
	}

//	public static int Percent(double[] data_in,double per){
//	  	double[] T_data = new double[data_in.length];
//		for (int i=0;i<data_in.length;i++){
//			double[] seg_i = new double[i+1];
//			System.arraycopy(data_in,0,seg_i,0,i);
//			T_data[i] = sum(seg_i);
//		}
//		double t_power = sum(data_in);
//		double[] P_data = new double[data_in.length];
//		for (int i=0;i<data_in.length;i++){
//			P_data[i] = T_data[i]/t_power;
//		}
//		int p_id = 0;
//		while (P_data[p_id]<per){
//			p_id += 1;
//		}
//		return p_id;
//	}

	public static SNSIFmaxResult SNSI_fmax2(double[] data1){
	  	SNSIFmaxResult result = new SNSIFmaxResult();
		double[] data = new double[data1.length];
		int dlng = data.length;
		System.arraycopy(data1,0,data,0,data1.length);
		double[] T_data = new double[data1.length];
		double[] MC_data = new double[data1.length];
		MC_data[0] = data[0];
		for (int i=1;i<data.length;i++){
			double[] seg_i = new double[i];
			System.arraycopy(data,0,seg_i,0,i);
			T_data[i] = sum(seg_i);
			MC_data[i] = i * data[i];
		}
		GM3Result gm3Result = GM3(data);
		double t_power = sum(data);
		double MC_power = sum(MC_data);
		double Mcx = MC_power/t_power;
		double[] IPC_data = new double[T_data.length];
		for (int i=0;i<T_data.length;i++){
			IPC_data[i] = T_data[i]/t_power;
		}
		double[] IPC_slope = new double[dlng];
		for (int i=1;i<dlng-1;i++){
			IPC_slope[i] = (IPC_data[i+1]-IPC_data[i-1])/2;
		}
		IPC_slope[0] = 0;
		IPC_slope[dlng-1] = 0;
		int ps11 = gm3Result.st;
		int PE11 = gm3Result.se;

		int p90 = Percent(data,0.90);
		int p99 = Percent(data,0.995);

		int cy_dataLen = PE11 - ps11 + 1;
		double[] cy_data11 = new double[cy_dataLen];
		double[] cx_data11 = new double[cy_dataLen];
		System.arraycopy(IPC_data,ps11,cy_data11,0,cy_dataLen);
		for (int i=ps11;i<=PE11;i++){
			cx_data11[i-ps11] = i;
		}
		double[] p1 = new double[2];
		p1[0] = IPC_slope[gm3Result.pcx];
		p1[1] = IPC_data[gm3Result.pcx]-IPC_slope[gm3Result.pcx]*gm3Result.pcx;

		double[] p2 = new double[2];
		p2[0] = (1-IPC_data[gm3Result.ns])/(129-gm3Result.ns);
		p2[1] = IPC_data[gm3Result.ns]-p2[0]*gm3Result.ns;

		result.PSNR = gm3Result.pSNR;

		int PS2 = gm3Result.ns;
		int pe2 = 129;

		int cy_data2Len = pe2 - PS2 + 1;
		double[] cy_data2 = new double[cy_data2Len];
		double[] cx_data2 = new double[cy_data2Len];
		System.arraycopy(IPC_data,PS2-1,cy_data2,0,cy_data2Len);
		for (int i=PS2;i<=pe2;i++){
			cx_data2[i-PS2] = i;
		}
//		WeightedObservedPoints points = new WeightedObservedPoints();
////		Log.d("Doppler","cy_data2Len ="+cy_data2Len);
//		for (int i=0;i<cy_data2Len;i++){
//			points.add(cx_data2[i],cy_data2[i]);
//		}
//		PolynomialCurveFitter fitter = PolynomialCurveFitter.create(1);
//		double[] p2 = fitter.fit(points.toList());


		result.Ms = p1[0];
		result.Mn = p2[0];

//
		double Msn2 = 0.1 * result.Ms + 0.9 * result.Mn;
//		double Msn2 = result.Ms;   // Cavin Test

		int fmax1 = gm3Result.ns;
		double ks = IPC_slope[fmax1];
		while (ks<Msn2){
			ks = IPC_slope[fmax1];
			fmax1 -= 1;
			if (fmax1<0){
				break;
			}
		}
		if (fmax1>gm3Result.se && fmax1<=gm3Result.ns){
			result.vf2_SNSI = fmax1;
		}else if(fmax1<=gm3Result.se){
			result.vf2_SNSI = gm3Result.se;
		}else {
			result.vf2_SNSI = gm3Result.ns;
		}

		return result;
	}

	public static SNSIResult SNSI_VF3(double[][] p_in){
	  	SNSIResult result = new SNSIResult(p_in);
//	  	double[][] arrP = new double[p_in.length][p_in[0].length];
//	  	System.arraycopy(p_in,0,arrP,0,p_in.length);


		int width = p_in[0].length;
		int depth = p_in.length;
		double[] t_power2 = new double[width];
	  	for(int npx=0;npx<width;npx++){
			double[] sel_im_line2 = column(p_in,npx);
			t_power2[npx] = sum(sel_im_line2);
		}
	  	double meanTPower2 = mean(t_power2);
	  	for (int npsd=0;npsd<width;npsd++){
	  		double[] ps_line = column(p_in,npsd);
	  		double[] ipc = IPC(ps_line);
	  		for (int i=0;i<depth;i++){
				result.NIPC[i][npsd] = ipc[i];
			}
	  		MCBWResult mcbwResult = MC_BW(ps_line);
	  		SNSIFmaxResult snsiFmaxResult = SNSI_fmax2(ps_line);
	  		if (snsiFmaxResult.PSNR>1 && snsiFmaxResult.vf2_SNSI>=1 &&(t_power2[npsd]/meanTPower2)>0.15){
				result.vfbw[npsd] = mcbwResult.bw;
				result.m_center[npsd] = mcbwResult.mc;
				result.vf[npsd] = snsiFmaxResult.vf2_SNSI;
			}else{
				result.vfbw[npsd] = 0;
				result.m_center[npsd] = 0;
				result.vf[npsd] = 1;
			}
	  		result.SNR_pf[npsd] = snsiFmaxResult.PSNR;
	  		result.m_center[npsd] = mcbwResult.mc;
		}
	  	double[] vf_out = MOV_AVG(result.vf,8);
	  	for (int i=0;i<vf_out.length;i++){
	  		if (vf_out[i]>=129){
	  			vf_out[i] = 0;
			}
		}
	  	double[] psL_out = new double[129];
		Arrays.fill(psL_out, 0);
		for (int npsd=0;npsd<width;npsd++){
			Arrays.fill(psL_out,0,(int)vf_out[npsd]-1,1);
			Arrays.fill(psL_out, (int)vf_out[npsd],depth-1,0);
			for (int i=0;i<depth;i++){
				result.p_out[i][npsd] = psL_out[i];
			}
		}

	  	return result;
	}
	  
	  public static double[][] VF_test(double[][] arr)
	  {
		   int depth=arr.length;
		   double sp_lng;
		   int width=arr[0].length;
		   double na=0,nb=0,nv=0;
		   double thv=90;     // threshold value for velocity
		   double tha=90;     // threshold value(H) for angle estimation
		   double thb=50;     // threshold value(L) for angle estimation
		   double v_sum=0;
		   double mc_sum=0;
		   double max_pline,Mc,Mcy,max_array,Lsnr;
		   double Lpower,Hpower,M_power,HRr;
		   double[][] P_ratio,result,filter_o,normal_o;
		   int[][] HR_seg;
//		  normal_o=normal(arr);
		  filter_o=spectrum_filter(arr);
//		  filter_o=adaptive_spectrum_filter(arr);
		  arr=filter_o;
	      P_ratio=new double[depth][width];
	      result=new double[60][width];
	      double[] im_col=new double[depth];
	      double[] HF_col=new double[65];
		  double[] LF_col=new double[64];
		  double[] snr=new double[width];
		  double[] snrf=new double[width];
		  double[] snr_id=new double[width];
		  double[] imvf,imvfp,imvfg,imvf2,imvf3,Tpower,ang,cout;
		  double[] seg_id=new double[width];
	      imvf=new double[width];
	      Tpower=new double[width];
	      ang=new double[width];
		  imvfp=new double[width];
	      imvf2=new double[width];
		  imvfg=new double[width];
//		  cout=new double[width];
		  int snr_fail=0;
		  max_array=getMax2D(filter_o);
		  Lpower=0;
		  sp_lng=depth;
		  Hpower=0;
	      for (int ii=0; ii<width;ii++)
		  {
	    	  im_col=column(filter_o,ii);
			  Tpower[ii]=sum(im_col);
		  }

		  for (int ii=0; ii<width;ii++)
		  {
			  im_col=column(filter_o,ii);
			  max_pline=getMax(im_col);
			  Lsnr=max_pline/max_array;
			  for (int ki=0; ki<64;ki++)
			  {
				  Lpower=im_col[ki]+Lpower;
			  }
			  for (int ki=0; ki<65;ki++)
			  {
				  Hpower=Hpower+im_col[ki+64];
			  }
//	    	  Tpower[ii]=sum(im_col);
			  snr[ii]=((Lpower-Hpower)/Tpower[ii])*100;
			  v_sum=0;
			  mc_sum=0;
	    	  for (int jj=0;jj<depth;jj++)
	    	  {
	    		 v_sum=v_sum+im_col[jj];
	    		 mc_sum=mc_sum+(im_col[jj]*(jj+1));
	    		 P_ratio[jj][ii]=(v_sum/Tpower[ii])*100;
	    		 if ((jj>1)&&(P_ratio[jj][ii]>thv)&&(P_ratio[jj-1][ii]<thv))
	    		 {
	    			nv=jj;
	    		 }

	    	  }

	    	  // Cavin Test 20210318
			  if (cavinTest){
				  if (Tpower[ii]>getMax(Tpower)*cavinMultiple2){
					  imvfp[ii]=nv;
				  }else{
					  imvfp[ii]=0;
				  }
			  }else{
			  	// Cavin close for test 20210318
			  	if (Tpower[ii]>0.02*(getMax(Tpower)))
//					if (Tpower[ii]>0.05*(getMax(Tpower)))
			  	//		    	  if (snr[ii]>50)
			  	{
				  	imvfp[ii]=nv;
			  	}
			  	else
			  	{
			  		imvfp[ii]=0;
			  	}
			  	// Cavin close for test 20210318
			  }


			  Mc=mc_sum/(Tpower[ii]);
			  int Mcx;
			  if (Mc>1)
			  {
				  Mcx=(int)Mc;
			  }
			  else
			  {
				  Mcx=0;
			  }
			  Mcy=P_ratio[Mcx][ii];
			  double ldx,ldy,pdx,pdy,Ling,png,cosv,sinv,GMv;
			  double[] dng=new double[129];
			  for (int jj=0;jj<129;jj++)
			  {
				  dng[jj]=0;
			  }
			  ldx=129-(Mc);
			  ldy=100-Mcy;
			  Ling=Math.sqrt(ldx*ldx+ldy*ldy);
			  for (int jj=((int)Mc+1);jj<depth;jj++)
			  {
				  pdy=P_ratio[jj][ii]-Mcy;
				  pdx=jj-Mc;
				  png=Math.sqrt(pdx*pdx+pdy*pdy);
				  cosv=(ldx*pdx+ldy*pdy)/(png*Ling);
				  sinv=Math.sqrt(1-cosv*cosv);
				  dng[jj]=png*sinv;
			  }

			  GMv=maximum_id(dng);
			  Lpower=0;
			  Hpower=0;
			  mc_sum=0;
	    	  v_sum=0;
			  if ((Mcy>50)&&(Lsnr>0.1))
			  //		    	  if (snr[ii]>50)
			  {
//				  imvf[ii]=GMv;
				  imvf[ii]=3.3*Mc;
			  }
			  else
			  {
//				  imvf[ii]=imvfp[ii];
				  imvf[ii]=3.3*Mc;
			  }
//	    	  ang[ii]=((na-nb)/nb);
	    	  result[0][ii]=imvf[ii];
			  result[1][ii]=GMv;
			  result[2][ii]=imvfp[ii];
//	    	  result[1][ii]=Math.atan(ang[ii]*2.7)*180/3.14159;
//			  if (100*snr<2)
//	    	  {
//	    	  result[2][ii]=0;
//	    	  snr_fail=snr_fail+1;
//	    	  }
//	    	  else
//	    	  {
				  result[3][ii]=snr[ii];
//	    	  }
		  } 
//	      imvf2=frequency_to_velocity(imvf,0);
		  imvf2=frequency_to_velocity(imvfp,0);		// Cavin test 20210412
		  M_power=getMax(Tpower);
	      for (int ii=0; ii<width;ii++)
		  {
	    	  result[2][ii]=imvf2[ii];     // Cavin test 20210412
			  double mtRadio = M_power/Tpower[ii];


			  // Cavin Test 20210318
			  if (cavinTest){
				  if ((Tpower[ii]<M_power*cavinMultiple2)){
				  	Log.d("Doppler","mtRatio = "+mtRadio +",   ii = "+ii);
					  imvf[ii]=0;
					  result[0][ii]=0;
				  }
			  }else{
			  	// Cavin close for test 20210318
	    	  	if ((Tpower[ii]<M_power*0.02))
//					if ((Tpower[ii]<M_power*0.05)||(snr[ii]<95))
	    		{
	    	  		imvf[ii]=0;
//	      	  		ang[ii]=0;
	      	  		result[0][ii]=0;
	      	  		result[1][ii]=0;
	      	  		result[2][ii]=0;
					result[3][ii]=0;
					snr[ii]=0;
					snr_fail=snr_fail+1;
				}
			  	else
			  	{
				  	result[3][ii]=100;
				  	snr[ii]=100;
			  	}
			  	// Cavin close for test 20210318
			  }


		  }
		  if ((snr_fail>1000)||(snr_fail<100))
		  {
			  result[4][1]=1;  // Set up SNR_fail_flag
//	    	  SNR_fail_flag=true;
		  }
		  else
		  {
			  result[4][1]=0; // Set up SNR_fail_flag
//	    	  SNR_fail_flag=false;
		  }
		  result[4][0]=snr_fail;
		  imvfg=MOV_AVG(imvf,10);    // imvf2: GM_90% combined velocity profile
		  imvf3=MOV_AVG(imvfp,10);   // imvfp: 90% velocity profile
		  snrf=MOV_AVG(snr,15);
		  seg_id=PN_Sequence_filer(imvf3);
//	      double[] temp_cor=new double[700];
//	      int st=100;
//	      for (int ti=st;ti<700;ti++)
//	      {
//	    	  temp_cor[ti-st]=imvf3[ti];
//	      }
//    	  cout=Cor(temp_cor, 250, 10);
//    	  double[][] pk_buf;
//    	  pk_buf=f_peak(cout,10,200);
		  double[][] HR_buf;
//    	  npk_buf=f_npeak(imvf3,10,20);
//    	  npk_buf=HR(imvf3,10,20);
		  HR_buf=HR3_test(imvf3);
		  double HR_int=HR_buf[0][12];
		  HRr=((double)125/HR_int)*60;
		  //	    	  HRr=150.5474;
		  result[4][4]=HRr;
		  imvf2=frequency_to_velocity(imvfg,HRr);
		  for (int ii=0; ii<imvf3.length;ii++)
		  {
			  result[0][ii]=imvfg[ii];
			  result[5][ii]=imvf3[ii];
			  result[6][ii]=seg_id[ii];
		  }
//		  int kk=0;
//		  while (HR_buf[0][kk]!=0)
//		  {
//			  kk=kk+1;
//		  }
//		  result[4][1]=kk;

//	      for (int ii=0; ii<pk_buf[0].length;ii++)
//		  {
//	    	  result[5][ii]=pk_buf[0][ii];
//	    	  result[6][ii]=pk_buf[1][ii];
//		  }
		  for (int ii=0; ii<HR_buf.length;ii++)
		  {
			  for (int k=0; k<13;k++)
			  {
				  result[ii+7][k]=HR_buf[ii][k];
			  }
		  }

//		  Heart_rate=1/(HR_int*64/8000);
		  if (HR_int==0)
		  {
			  result[4][3]=1;  // Set up SEG_fail_flag
//	    	  HR_fail_flag=true;
		  }
		  else
		  {
			  result[4][3]=0; // Set up SEG_fail_flag
//	    	  HR_fail_flag=false;

		  }
//		  HR_seg=HR_seg_PID_in(seg_id,HR_int);

		  HR_seg=HR_SEG(HR_int,Tpower);
		  if (HR_seg.length==0)
		  {
			  result[4][2]=1;  // Set up SEG_fail_flag
//	    	  SEG_fail_flag=true;
		  }
		  else
		  {
			  result[4][2]=0; // Set up SEG_fail_flag
//	    	  SEG_fail_flag=false;

		  }
		  double [] vpk_vti=new double[2];
		  double [] vpk=new double[HR_seg.length];
		  double [] vti=new double[HR_seg.length];
		  for (int ii=0; ii<HR_seg.length;ii++)
		  {
			  vpk_vti=VPK_VTI(imvf2,HR_seg[ii][0],HR_seg[ii][1],2);	// 1103 for VTI test
			  for (int k=15; k<17;k++)
			  {
				  result[ii+7][k]=HR_seg[ii][k-15];
			  }
			  if(HR_seg[ii][0]!=0)
			  {
				  result[ii+7][17]=vpk_vti[0];
				  result[ii+7][18]=vpk_vti[1];
				  vpk[ii]=vpk_vti[0];
				  vti[ii]=vpk_vti[1];
			  }
		  }
		  double[] maxvote_1=new double[3];
		  double VTI,VPK;
//		  maxvote_1=maxvote(vpk,2);
//		  VPK=maxvote_1[1];
		  VPK=getMax(vpk);
//		  maxvote_1=maxvote(vti,2);
//		  VTI=maxvote_1[1];
		  VTI=getMax(vti);
		  System.out.print(" Heart_rate =" );
		  System.out.print(HRr );
		  System.out.print(" VTI =" );
		  System.out.print(VTI );
		  System.out.print(" VPK =" );
		  System.out.print(VPK );
		  result[4][5]=VPK;
		  result[4][6]=VTI;
		  return result;
	  }
	public static double[] frequency_to_velocity2(double[] arr, double ang_rad)
	{

		int leng=arr.length;
		double[] vf_out;
		vf_out=new double[leng];
		double tp1,tp2;
		double probe_F=(0.03/0.095);
		double wav_lng=1540.0/2500000.0;
		double p1=0.5835,p2=0.1728,p3=0.1370;
		tp2=(4000/129)/((2/wav_lng)*(Math.cos(ang_rad)+probe_F*Math.sin(ang_rad)));
//			  System.out.println(" factor="+tp2 );
		for (int ii=0; ii<leng;ii++)
		{
			tp1=arr[ii]*tp2;
			vf_out[ii]=(p1*tp1*tp1+p2*tp1+p3);
		}

		return vf_out;

	}

	public static final int PHANTON_C = 1450;
	public static final int HUMAN_C = 1540;

	//Leslie add
	public static double frequency_to_velocity_By_Angle(double para, int speedOfSound, double rxAngle){
		final int c = speedOfSound;
		final double ftxFreq = BloodVelocityConfig.DOUBLE_ULTRASOUND_SENSOR_WAVE_FREQ;
		double txAngle = rxAngle - 5.0;
		double cos_rxAngle = Math.cos(((rxAngle / 180.0) * Math.PI));
		double cos_txAngle = Math.cos(((txAngle / 180.0) * Math.PI));
		double fD = para * GIS_Algorithm.ONE_SEGMENT_WITH_N_HZ;
		return (c * fD) / (ftxFreq * (cos_txAngle + cos_rxAngle));
	}

	public static double[] frequency_to_velocity_Cavin(double[] arr, double[][] arr2){
		double[] VelocityFromFreq = new double[arr.length];
		SystemConfig.rxAngle = GIS_Algorithm.findDopplerAngle();
		Log.e("Leslie", SystemConfig.rxAngle+"");
		for (int count = 0 ; count < arr.length ; count++){
			VelocityFromFreq[count] = frequency_to_velocity_By_Angle(arr[count], HUMAN_C, SystemConfig.rxAngle);
		}
		return VelocityFromFreq;
	}

	// ********* This function  convert  frequency  to velocity for every segment   **************//
	  public static double[] frequency_to_velocity(double[] arr, double Hr)
	  {
		  
		  int leng=arr.length;
		  double[] vf_out;
		  vf_out=new double[leng];
		  double vftp;
		  double PN21=0.00000001348626852370272;	// PN2_P
		  double PN22=-0.000003115831498247;		// PN2_P
		  double PN23=0.0001599186555013349;		// PN2_P
		  double PN11=-0.0000003532149831989584;   	// PN1_P
		  double PN12=0.0001092911646238443;		// PN1_P
		  double PN13=0.014170212592345;			// PN1_P
		  double PN01=0.00001436423815211106;		// PN0_P
		  double PN02=-0.002380918122445;			// PN0_P
		  double PN03=-0.033938407413338;			// PN0_P
		  double pn0,pn1,pn2;
		  pn2=PN21*Hr*Hr+PN22*Hr+PN23;
		  pn1=PN11*Hr*Hr+PN12*Hr+PN13;
		  pn0=PN01*Hr*Hr+PN02*Hr+PN03;

		  System.out.print(" pn2=");
		  System.out.println(pn2);
		  System.out.print(" pn1=");
		  System.out.println(pn1);
		  System.out.print(" pn0=");
		  System.out.println(pn0);

		  for (int ii=0; ii<leng;ii++)
		   {
			  vftp=arr[ii];
			  vf_out[ii]=pn2*vftp*vftp+pn1*vftp+pn0;
			  if (vf_out[ii]<0){
//			  	Log.d("Doppler","vf_out["+ii+"]= "+vf_out[ii]);
			  	vf_out[ii] = 0;
			  }
		   }
		  return vf_out;
		  
	  }

	static int VPK_Org(double[] vf, int seg_s, int seg_e, int PL)
	{
		double[] vf_r =new double[seg_e-seg_s+1];
		int result = 0;
		for(int i=0;i<(seg_e-PL)-(seg_s+PL)+1;i++)
		{
			vf_r[i]=vf[seg_s+i];
//			   		System.out.println(vfs[i] );
		}

		result= (int)getMax(vf_r);
		return result;
	}
	static int VPK_index(int[] vf, int seg_s, int seg_e, int PL,int targetFreq)
	{
		int[] vf_r =new int[seg_e-seg_s+1];
		int result = 0;
		for(int i=0;i<(seg_e-PL)-(seg_s+PL)+1;i++)
		{
			vf_r[i]=vf[seg_s+i];
			if (vf_r[i]==targetFreq){
				result = i;
			}
		}

		return result;
	}

	static int VPK_Org(int[] vf, int seg_s, int seg_e, int PL)
	{
		int[] vf_r =new int[seg_e-seg_s+1];
		int result = 0;
		for(int i=0;i<(seg_e-PL)-(seg_s+PL)+1;i++)
		{
			vf_r[i]=vf[seg_s+i];
//			   		System.out.println(vfs[i] );
		}

		result= (int)getMax(vf_r);
		return result;
	}
	// This function calculate VPK & VTI for one HR interval

	static double[] VPK_VTI(double[] vf, int seg_s, int seg_e, int PL)
	{
		double[] vf_r =new double[seg_e-seg_s+1];
		double[] result =new double[2];
		for(int i=0;i<(seg_e-PL)-(seg_s+PL)+1;i++)
		{
			vf_r[i]=vf[seg_s+i];
//			   		System.out.println(vfs[i] );
		}

		result[0]=getMax(vf_r);
		//cavin test 20210910
//		result[1]=sum(vf_r)*0.8;
		result[1]=sum(vf_r);
		//cavin test 20210910 end
//		System.out.println(sum(vf_r));
		return result;
	}


	static double[] VPK_VTI2(double[] vf, double[] ang, int seg_s, int seg_e)
	{
		double[] vfs =new double[seg_e-seg_s+1];
		double[] angs =new double[seg_e-seg_s+1];
		double[] vf_r =new double[seg_e-seg_s+1];
		double[] result =new double[2];
		double ang_sum,ang_vf,vf_th;

		for(int i=0;i<seg_e-seg_s+1;i++)
		{
			vfs[i]=vf[seg_s+i];
			angs[i]=ang[seg_s+i];
//	   		System.out.println(vfs[i] );
		}
		vf_th=(getMax(vfs)-getMin(vfs))/1.5+getMin(vfs);
//	   	System.out.print("  VF_th= " );
//	   	System.out.println(vf_th);
		ang_sum=0;
		int ang_n=0;
		for(int i=0;i<seg_e-seg_s+1;i++)
		{
			if (vfs[i]>=vf_th)
			{
				ang_sum=ang_sum+angs[i];
				ang_n=ang_n+1;
			}
		}
		ang_vf=Math.atan((ang_sum/(double)ang_n)*2.7);
//	   	System.out.print("  Angle sum= " );
//	   	System.out.println(ang_sum );
		vf_r=frequency_to_velocity(vfs,ang_vf);
//	   	for (int j=0;j<vf_r.length;j++)
//	   	{
//	   		System.out.println(vf_r[j] );
//	   	}

		result[0]=getMax(vf_r);
		result[1]=sum(vf_r)*(64.0/8000.0);
//	   	System.out.println(sum(vf_r));
		return result;
	}
	public static double[][] adaptive_spectrum_filter(double[][] arr)
	{
		int depth=arr.length;
		int width=arr[0].length;
		int m_id;
		double[][] f_im;
		f_im=new double[depth][width];
		double[] im_col;
		im_col=new double[depth];
		double[] im_power;
		im_power=new double[width];
		for (int ii=0; ii<width;ii++)
		{
			im_col=column(arr,ii);
			im_power[ii]=sum(im_col);
		}
		m_id=maximum_id(im_power);
		im_col=column(arr,m_id);
		double p20,p2040;
		p20=0;
		p2040=0;
		for (int ii=0; ii<20;ii++)
		{
			p20=p20+im_col[ii];
		}
		for (int ii=20; ii<40;ii++)
		{
			p2040=p2040+im_col[ii];
		}
		if (p2040>=p20)
		{
			f_im=spectrum_filter(arr);
		}
		else
		{
			for (int ii=0; ii<width;ii++)
			{
				for (int jj=0; jj<depth;jj++)
				{
					f_im[jj][ii]=arr[jj][ii];
				}
			}
		}
		return(f_im);

	}

	public static double[][] spectrum_filter(double[][] arr)
	  {
		   int depth=arr.length;
		   int width=arr[0].length;
		   double[][] f_im;
	      f_im=new double[depth][width];
	      double[] im_1D;
	      im_1D=new double[width];
	      double[] im_col;
	      im_col=new double[depth];
	      double[] filtered_col;
	      filtered_col=new double[depth];
	      double max_p,max_p2;
//	      im_1D=sum_2D(f_im);
//		   max_p=getMax2D(arr)*0.0015;
		   
//	       max_p=0.5;
		   int t_fg=0;
		   int jd=20;
		   int tp=0;
		   for (int ii=0; ii<width;ii++)
		   {
			   im_col=column(arr,ii);
			   max_p=(getMax(im_col)-getMin(im_col))/300;
//			   System.out.println(" maxmum="+ max_p);
			   while (t_fg==0 && (jd+1)<depth)
			   {
				   if ((im_col[jd-1]<max_p)&&(im_col[jd+1]<max_p)&&(im_col[jd]<max_p))
//				   if (im_col[jd+1]<max_p)
				 {
					 t_fg=1;
				 }
				 tp=jd-1;
				 jd=jd+1;
			    }
			  for(int kk=0;kk<tp;kk++)
			  {
				  f_im[kk][ii]=im_col[kk];
			  }
			  for(int kk=tp;kk<depth;kk++)
			  {
				  f_im[kk][ii]=0;
			  }
			  tp=0;
			  jd=20;
			  t_fg=0;
		    }	   
		   return f_im;
	  }
	  
	  public static double[] column(double[][] arr, int c_no)
	  { 
		   int depth=arr.length;
//		   int width=arr[0].length;
		   double[] col_1D;
	      col_1D=new double[depth];
	      for (int i = 0; i < depth; i++)
	      {
	   	   col_1D[i]=arr[i][c_no];
	      }
	      return col_1D;       
	  }
	  
	  
	  public static double[] sum_2D(double[][] arr)
	  { 
	      int kk=arr[0].length;
	      double[] sumv;
		   sumv=new double[kk];
	      double temp=0;
	      int width=arr[0].length;
	      for(int j=0;j<width;j++)
	      {
	      for (int i = 0; i < arr.length; i++) 
	      {  
	   	   temp +=  arr[i][j];      
	      }
	      sumv[j]=temp;
	      temp=0;
	      }
	      return sumv; 
	  } 
	    
	  public static double mean(double[] arr)
	  { 
	      double sum = 0; // initialize sum
	      double avg;
	      int i; 
	      for (i = 0; i < arr.length; i++) 
	         sum +=  arr[i];
	      avg=sum/arr.length;
	      return avg; 
	  }

	public static int getMax(int[] inputArray)
	{
		if (inputArray.length==0){
			return 0;
		}
		int maxValue = inputArray[0];
		for(int i=1;i < inputArray.length;i++)
		{
			if(inputArray[i] > maxValue){
				maxValue = inputArray[i];
			}
		}
		return maxValue;
	}
	  
	  public static double getMax(double[] inputArray)
	  {
	  	if (inputArray.length==0){
	  		return 0.0;
		}
		    double maxValue = inputArray[0]; 
		    for(int i=1;i < inputArray.length;i++)
		    { 
		      if(inputArray[i] > maxValue){ 
		         maxValue = inputArray[i]; 
		      } 
		    } 
		    return maxValue; 
	  }
	  public static double getMax2D(double[][] inputArray)
	  { 
		    double maxValue = inputArray[0][0];
		    for(int j=0;j < inputArray.length;j++)
		    for(int i=0;i < inputArray[0].length;i++)
		    { 
		      if(inputArray[j][i] > maxValue){ 
		         maxValue = inputArray[j][i]; 
		      } 
		    } 
		    return maxValue; 
		  }
	  

	  public static double getMin(double[] inputArray)
	  { 
		    double minValue = inputArray[0]; 
		    for(int i=1;i < inputArray.length;i++)
		    { 
		      if(inputArray[i] < minValue){ 
		         minValue = inputArray[i]; 
		      } 
		    } 
		    return minValue; 
	  }

	  public static double getNonZeroMin(double[] inputArray){
	  	double nonZeroMinValue = inputArray[0];
	  	for (int i=1;i<inputArray.length;i++){
	  		if (inputArray[i] < nonZeroMinValue && inputArray[i]!=0){
	  			nonZeroMinValue = inputArray[i];
			}
			if (nonZeroMinValue==0 && inputArray[i]!=0){
				nonZeroMinValue = inputArray[i];
			}
		}
	  	return nonZeroMinValue;
	  }


	public static double[] MOV_AVG(double[] arr, float Mn) // MN: 為奇數
	{
		int lng=arr.length;
		int k;
		k=Math.floorMod((int)Mn,2);
		double sum=0;
		double[] MA_out;
		MA_out=new double[lng];
		if (k==1)
		{
//			   int mid=(int)(Math.floor((Mn/2)+0.5));
			for(int i=0;i < Mn;i++)
			{
				MA_out[i]=arr[i];
			}
			for(int i=(lng-(int)Mn);i < lng;i++)
			{
				MA_out[i]=arr[i];
			}
			for(int i=(int)Mn;i < (lng-(int)Mn);i++)
			{
				int ld=(int)Math.floor(Mn/2);
				for(int j=(i-ld);j<(i+ld+1);j++)
				{
					sum=sum+arr[j];
				}
				MA_out[i]=sum/Mn;
				sum=0;
			}
		}
		else
		{
			int h1=(int)Math.floor(Mn/2);
			for(int i=0;i < h1;i++)
			{
				MA_out[i]=arr[i];
			}
			for(int i=(lng-h1-1);i <lng;i++)
			{
				MA_out[i]=arr[i];
			}
			for(int i=h1;i < (lng-h1-1);i++)
			{

				for(int j=(i-h1);j<(i+h1);j++)
				{
					sum=sum+arr[j];
				}
				MA_out[i]=sum/Mn;
				sum=0;
			}

		}

		return MA_out;
	}

	//
	public static double[][] fpk_snsi(double[] in_vf, int in_thp, int npfg){
	  	int fpkn = 0;
	  	int lng = in_vf.length;
	  	double[] FL_S = new double[in_thp];
	  	double[] FR_S = new double[in_thp];
	  	int[] FPK_id = new int[lng];
	  	double[] FPK_y = new double[lng];
	  	double[] FPK_x = new double[lng];

	  	for (int ffpi = (in_thp-1); ffpi<lng-in_thp;ffpi++){
	  		System.arraycopy(in_vf,(ffpi-(in_thp-1)),FL_S,0,in_thp);
			System.arraycopy(in_vf,ffpi,FR_S,0,in_thp);

			if (npfg == 1){
				if (getMax(FL_S)==in_vf[ffpi] && getMax(FR_S)==in_vf[ffpi]){
					FPK_id[ffpi] = 1;
					fpkn += 1;
					FPK_y[fpkn] = in_vf[ffpi];
					FPK_x[fpkn] = ffpi;
				}else{
					FPK_id[ffpi] = 0;
				}
			}
			if (npfg == 0){
				if (getMin(FL_S) == in_vf[ffpi] && getMin(FR_S)==in_vf[ffpi]){
					FPK_id[ffpi] = 1;
					fpkn += 1;
					FPK_y[fpkn] = in_vf[ffpi];
					FPK_x[fpkn] = ffpi;
				}else{
					FPK_id[ffpi] = 0;
				}
			}
		}
	  	int ffpi2 = 0;
	  	int fpkn2 = 0;
	  	double[] FPKY2 = new double[fpkn];
	  	double[] FPKX2 = new double[fpkn];
	  	while(ffpi2<fpkn-1){
	  		if (FPK_y[ffpi2] != FPK_y[ffpi2+1]){
	  			FPKY2[fpkn2] = FPK_y[ffpi2];
				FPKX2[fpkn2] = FPK_x[ffpi2];
				fpkn2 += 1;
			}
	  		ffpi2 += 1;
		}
	  	if (fpkn >=1){
	  		fpkn2 += 1;
			FPKY2[fpkn2-1] = FPK_y[fpkn-1];
			FPKX2[fpkn2-1] = FPK_x[fpkn-1];
		}else{
	  		fpkn2 = 0;
	  		FPKY2[0] = 0;
	  		FPKX2[0] = 0;
		}
	  	double m_pky = 0.5*getMax(FPKY2);
	  	int lng2 = FPKY2.length;
	  	double[][] result = new double[2][lng2];

	  	for (int i=0;i<lng2;i++){
	  		result[0][i] = FPKX2[i];
	  		result[1][i] = FPKY2[i];
		}
		return result;
	}

	public static double[][] f_pk(double[] arr,int th) // th: 前後點數
	{
		int lng=arr.length;
		int id=0;
		double[][] result=new double[lng][2];
		double[] L_arr;
		L_arr=new double[th];
		double[] R_arr,peak_id,peak_v;
		R_arr=new double[th];
		peak_id=new double[lng];
		peak_v=new double[lng];

		for(int j=th;j<lng-th;j++)
		{
			for(int i=1;i<th;i++)
			{
				L_arr[i]=arr[j-i];
				R_arr[i]=arr[j+i];
			}
			if((arr[j]>getMax(L_arr))&&(arr[j]>getMax(R_arr)))
			{
				peak_id[id]=j;
				peak_v[id]=arr[j];
				id=id+1;
			}
		}
		int id2=0;
		int pkn=0;
		if (id==1)
		{
			result[pkn][0]=peak_id[0];
			result[pkn][1]=peak_v[0];
			pkn++;
		}
		if (id==0)
		{
			result[pkn][0]=0;
			result[pkn][1]=0;
			pkn++;
		}
		if (id>=2)
		{
			while(id2<id)
			{

				if (peak_id[id2]!=peak_id[id2+1]-1)
				{
					result[pkn][0]=peak_id[id2];
					result[pkn][1]=peak_v[id2];
					pkn++;
				}
				id2++;
			}
		}
		double[][] result1=new double[pkn][2];
		for (int kj=0;kj<pkn;kj++)
		{
			result1[kj][0]=result[kj][0];
			result1[kj][1]=result[kj][1];
		}
		return(result1);
	}




	public static double[][] FFT(double[] x)
	{
		int n = x.length;  // assume n is a power of 2

		double[] xre = new double[n];
		double[] xim = new double[n];
		double[][] result;
		result=new double[n][2];
		double  p, nu,arg, re, im,sum_re,sum_im;
		for (int i = 0; i < n; i++)
		{
			xre[i]= 0.0;
			xim[i]= 0.0;
		}

		for (int k = 0; k < n; k++)
		{
			sum_re=0;
			sum_im=0;
			{
				for (int i = 0; i < n; i++)
				{
					p = k*i;
					nu=x[i];
					arg = -2 * (double) Math.PI * p / n;
					re = (double) Math.cos (arg);
					im = (double) Math.sin (arg);
					sum_re=sum_re+re*nu;
					sum_im=sum_im+im*nu;
				}
			}

			result[k][0]=sum_re;
			result[k][1]=sum_im;
		}
		return result ;
	}
	public static double[] Hamming( int Mn)
	{
		double[] w;
		w=new double[Mn];
		int ln=Mn-1;
		double temp;
		for(int i=0;i < Mn;i++)
		{
			temp=2*Math.PI*i/(ln-1);
			w[i]=0.54-0.46*Math.cos(temp);
		}
		return w;
	}


	public static double[] Cor(double[] arr, int k) // k: 運算資料片段號碼
	{
		int lng=arr.length;
		int template_lng=200;
		int target_lng=500;
		int step_size=100;

		double[] COR_out;
		COR_out=new double[target_lng-template_lng];
		double[][] Bin_arr;
		Bin_arr=new double[129][lng];
		double[][] template;
		template=new double[129][template_lng];
		double[][] target;
		target=new double[129][target_lng];
		for(int j=0;j<lng;j++)
		{
			for(int i=0;i<(int)arr[j];i++)
			{
				Bin_arr[i][j]=1;
			}
			for(int i=(int)arr[j];i<129;i++)
			{
				Bin_arr[i][j]=0;
			}
		}
		int frame_no=((lng-target_lng)/step_size);
		int cor_sum=0;
//	   for(int k=0;k<frame_no;k++)
//    {
		if (k<frame_no)
		{
			for(int i=0;i<129;i++)
			{
				for(int j=step_size*k;j<(step_size*k+template_lng);j++)
				{
					template[i][j-(step_size*k)]=Bin_arr[i][j];
				}
				for(int j=step_size*k;j<(step_size*k+target_lng);j++)
				{
					target[i][j-(step_size*k)]=Bin_arr[i][j];
				}
			}

			for(int sf=0;sf<(target_lng-template_lng);sf++)
			{
				for(int i=0;i<129;i++)
				{
					for (int j=sf;j<(sf+template_lng);j++)
					{
						if (template[i][j-sf]==target[i][j])
						{
							cor_sum++;
						}
					}
				}
				COR_out[sf]=(double)cor_sum/(double)(129*template_lng);
				cor_sum=0;
			}
		}
		else
		{
			COR_out[0]=0;
		}
		return(COR_out);
	}
	public static double[][] HR3_test(double[] arr) // Cn:計算HR 之VF長度； Th:最小 peak值；arr(): 速度曲線
	{

		double[] cor_buf,maxvote_r;
		int pkn;
		double[] peak_id=new double[12];
		double[][] pk_buf;
		double[][] result=new double[2][13];

		for (int kk = 0; kk<12; kk++)
		{
			cor_buf=Cor(arr,kk);
			pk_buf=f_pk(cor_buf,30);
			double[] pk_id=new double[pk_buf.length];
			double[] pk_v=new double[pk_buf.length];
			if (pk_buf.length>0)
			{
				pkn=0;
				for (int ki=0;ki<pk_buf.length;ki++)
				{
//					if ((pk_buf[ki][0]>45)&&(pk_buf[ki][0]<250))
					if ((pk_buf[ki][0]>30)&&(pk_buf[ki][0]<250))	// 20211103 Cavin test
					{
						pk_id[pkn]=pk_buf[ki][0];
						pk_v[pkn]=pk_buf[ki][1];
						pkn++;
					}
				}

				if (pkn==2)
				{

					if (pk_v[0]>(pk_v[1]*0.95))
					{
						int mid=0;
						result[0][kk]=pk_id[mid];
						result[1][kk]=pk_v[mid];
						peak_id[kk]=pk_id[mid];
					}
					else
					{
						int mid=1;
						result[0][kk]=pk_id[mid];
						result[1][kk]=pk_v[mid];
						peak_id[kk]=pk_id[mid];
					}
				}
				if (pkn>=3)
				{

					if (pk_v[0]>(pk_v[1]))
					{
						int mid=0;
						result[0][kk]=pk_id[mid];
						result[1][kk]=pk_v[mid];
						peak_id[kk]=pk_id[mid];
					}
					else
					{
						int mid=1;
						result[0][kk]=pk_id[mid];
						result[1][kk]=pk_v[mid];
						peak_id[kk]=pk_id[mid];
					}
				}
				if (pkn==1)
				{
					int mid=0;
					result[0][kk]=pk_id[mid];
					result[1][kk]=pk_v[mid];
					peak_id[kk]=pk_id[mid];
				}
				if (pkn==0)
				{
					result[0][kk]=0;
					result[1][kk]=0;
					peak_id[kk]=1;
				}
			}
//			Log.d("HR3","peak_id["+kk+"]="+peak_id[kk]);
		}
		maxvote_r=maxvote(peak_id,3);
		result[0][12]=maxvote_r[1];
		return result;
	}

	public static double[][] HR2_test(double[] arr) // Cn:計算HR 之VF長度； Th:最小 peak值；arr(): 速度曲線
	{

		double[] cor_buf,maxvote_r;
//	int lng=arr.length;
		int pkn;
		double[] peak_id=new double[12];
		double[][] pk_buf;
		double[][] result=new double[2][13];
		//		double[] pk_id=new double[5];
//		double[] pk_v=new double[5];

		for (int kk = 0; kk<12; kk++)
		{
			cor_buf = Cor(arr, kk);
//		for (int i=0;i<300;i++)
//		{
//			result[2+i][kk]=cor_buf[i];
//		}
			pk_buf = f_pk(cor_buf, 30);

			double[] pk_id = new double[pk_buf.length];
			double[] pk_v = new double[pk_buf.length];
			if (pk_buf.length > 0)
			{
				pkn = 0;
				for (int ki = 0; ki < pk_buf.length; ki++)
				{
					if ((pk_buf[ki][0] > 45) && (pk_buf[ki][0] < 250)) {
						pk_id[pkn] = pk_buf[ki][0];
						pk_v[pkn] = pk_buf[ki][1];
//						System.out.print(" kk=  ");
//					System.out.print(kk);
//				    System.out.print(" peak_id=  ");
//					System.out.print(pk_id[pkn]);
//					 System.out.print(" peak_v=  ");
//					System.out.println(pk_v[pkn]);
						pkn++;
					}
				}
				//			System.out.print(" pkn(HR2_test)=  ");
//			System.out.print(pkn);

				if (pkn > 1) {

					if (((pk_id[1] / pk_id[0]) > 1.9) && (Math.abs(pk_v[1] - pk_v[0]) < 0.15)) {
						int mid = 0;
						result[0][kk] = pk_id[mid];
						result[1][kk] = pk_v[mid];
						peak_id[kk] = pk_id[mid];
					} else {
						int mid = maximum_id(pk_v);
						result[0][kk] = pk_id[mid];
						result[1][kk] = pk_v[mid];
						peak_id[kk] = pk_id[mid];
					}
					//			    System.out.print(" Peak_max=  ");
//				System.out.print( pk_max);
//			    System.out.print(" Peak_min=  ");
//				System.out.println( pk_min);
				}
				if (pkn == 1)
				{
					int mid = 0;
					result[0][kk] = pk_id[mid];
					result[1][kk] = pk_v[mid];
					peak_id[kk] = pk_id[mid];
				}
				if (pkn == 0) {
					result[0][kk] = 0;
					result[1][kk] = 0;
					peak_id[kk] = 1;
				}
			}

		}
		maxvote_r=maxvote(peak_id,3);
		result[0][12]=maxvote_r[1];

		return result;
	}
	public static double[][]    Spectrogram(double[] x, int w_size, int overlap)
	{
		double[] w = new double[w_size];
		double[] temp = new double[w_size];
		double[] temp2= new double[w_size];
		double[][] fft_bf = new double[w_size][2];
		int lng=x.length;
		double p,arg;
		int n=w_size;
		double[][] re_M;
		re_M=new double[n][n];
		double[][] im_M;
		im_M=new double[n][n];
		for (int k = 0; k < n; k++)
		{

			for (int i = 0; i < n; i++)
			{
				p = k*i;
				arg = -2 * (double) Math.PI * p / n;
				re_M[k][i] = (double) Math.cos (arg);
				im_M[k][i] = (double) Math.sin (arg);
			}
		}
		int frame_no,step_no,st_ad;
		double factor;
		factor=0.0000012334;
		frame_no=lng/(w_size-overlap);
		step_no=(w_size-overlap);

		double[][] result = new double[129][frame_no];
		w=Hamming(w_size);

		for(int j=0;j<frame_no-5;j++)
		{
			st_ad=step_no*j;
			for(int i=st_ad;i<st_ad+w_size;i++)
			{
				temp[i-st_ad]=x[i]*w[i-st_ad];
			}
			fft_bf=FFT2(temp,re_M,im_M);
			for(int jj=0;jj<129;jj++) {
				temp2[jj] = (fft_bf[jj][0] * fft_bf[jj][0] + fft_bf[jj][1] * fft_bf[jj][1]) * factor;
//		    	result[jj][j]=temp2*factor;
			}
				result[0][j]=temp2[0];
				for(int jj=1;jj<129;jj++)
				{
					result[jj][j]=temp2[jj]*2;
				}

		}
		return result;
	}
	public static double[] PN_Sequence_filer2(double[] arr)
	{
		int lng=arr.length;
		int[] temp =new int[lng];
		int[] flag =new int[lng];
		double[] result =new double[lng];
		int[] PKY =new int[lng];
		int[] PKX =new int[lng];
		int temp_N=0,pkn=0;
		double max_v,min_v,mean_v,pk_th,pk_sum=0;
//		max_v=getMax(arr);
//		min_v=getMin(arr);
//		mean_v=(max_v+min_v)/2;
		mean_v=mean(arr);
		for (int i = 0; i < arr.length; i++)
		{
			if (arr[i]<mean_v)
			{
				flag[i]=100;
			}
			else
			{
				flag[i]=1;
			}
			result[i]=0;
		}
		for (int i = 0; i < arr.length; i++)
		{
			if (flag[i]==100)
			{
				temp_N ++;
				temp[i]=temp_N;
			}
			else
			{
				if (temp_N!=0)
				{
					PKX[pkn]=i;
					PKY[pkn]=temp_N;
					pk_sum=pk_sum+temp_N;
					pkn++;
				}
				temp[i]=0;
				temp_N=0;
			}
		}
		pk_th=pk_sum/(double)pkn;
		System.out.print("pkth=");
		System.out.println(pk_th);
		for (int i = 0; i < pkn; i++)
		{
			temp_N=0;

			if (PKY[i]>=pk_th)
			{
//		  System.out.print("start=");
//		  System.out.println(PKX[i]-PKY[i]);
//		  System.out.print("end=");
//		  System.out.println(PKX[i]);
				{
					for(int j=(PKX[i]-PKY[i]); j < PKX[i]; j++)
					{
						temp_N++;
						result[j]=temp_N;
					}
				}
			}
		}
		return result;
	}

	public static double[] PN_Sequence_filer(double[] arr)
	{
		int lng=arr.length;
		double[] temp =new double[lng];
		double[] flag =new double[lng];
		double[] result=new double[lng];
		int[] PKY=new int[lng];
		int[] PKX=new int[lng];
		int temp_N=0,pkn=0;
		double max_v,min_v,mean_v,pk_th,pk_sum=0;
//		max_v=getMax(arr);
//		min_v=getMin(arr);
//		mean_v=(max_v+min_v)/2;
		mean_v=mean(arr);
		for (int i = 0; i < arr.length; i++)
		{
			if (arr[i]<mean_v)
			{
				flag[i]=100;
			}
			else
			{
				flag[i]=1;
			}
		}

		for (int i = 0; i < arr.length; i++)
		{
			if (flag[i]==100)
			{
				temp_N ++;
				temp[i]=temp_N;
			}
			else
			{
				temp[i]=0;
				temp_N=0;
			}
		}

		return temp;
	}

	public static int[][] HR_SEG_0630(double HR_int, double[] TP_in, double[] vf){
//	  	double[] vf_s = MOV_AVG(TP_in, 10);
		double[] vf_s = MOV_AVG(TP_in, 5);
	  	int lng = (int)Math.floor(HR_int/5);
		double[][] pk_tr = f_peak(vf_s,lng,0);
//		double[][] pk_tr = f_peak(vf_s,13,0);
		int[] SEG_s=new int[60];
		int[] SEG_e=new int[60];

		double PS1,MPS;
		int th1 = 13;//(int)(HR_int/4);		// Cavin modified for testing 20210803
		double th2 = mean(TP_in) * 0.8;
		int seg_no = 0;
		int st,se,NPK_s;
		int pkn = (int)pk_tr[0][0];//pk_tr.length;

		Log.d("Doppler","th1 = "+th1+" ,HR_int="+HR_int+" ,pkn="+pkn);

		double[] tmpTp = new double[vf_s.length-20];
		System.arraycopy(vf_s,20,tmpTp,0,vf_s.length-21);
//		double th3 = getMax(tmpTp) * 0.1;
//		double sumPkTr = 0;
//		for (double[] doubles : pk_tr) {
//			sumPkTr += doubles[0];
//		}
		Methodoligies mMeth = new Methodoligies();
		double[] sortedTpower = mMeth.getArrSort(Tag.INT_SORT_MAX,tmpTp);
		double[] tmpMaxTpower = new double[(int)(sortedTpower.length*0.5)];
		System.arraycopy(sortedTpower,0,tmpMaxTpower,0,tmpMaxTpower.length);
		//double th3 = mean(tmpMaxTpower); //Math.min(sumPkTr / (2*pk_tr.length),getMax(tmpTp)*0.1);
		double th3 = mean(tmpTp);
		double th4 = cavinMultiple2 * getMax(tmpTp);
		int th5 = (int)HR_int;	// modified 20211013
//		if (HR_int < 63 ){
//			th5 = (int)(HR_int * 0.86);
//		}else{
//			th5 = (int)(HR_int*3 / 4);  // 50
//		}

//		Log.d("Doppler","th4 = "+th4+" ,HR_int="+HR_int);
		if (HR_int>0){
			for (int i=1;i<pkn;i++){
				st=(int)pk_tr[i][0];
				se=(int)pk_tr[i+1][0];
				PS1=SEG_sel(TP_in,st,se);
				MPS=PS1/(se-st);
				NPK_s=se-st;
				//cavin test 20210831
//				if ((vf_s[st]>th3)&&(NPK_s>th1))
				if ((NPK_s>th1))
				//if ((MPS>th2)&&(NPK_s>th1))
				{
					Log.d("Doppler","i="+i+",th3 = "+th3+" ,vf_s["+st+"]="+vf_s[st]+" ,NPK_s="+NPK_s);
					boolean skip = false;
					int tmpSe = 0;
					for (int j=st;j<se;j++){
						if (vf[j]<0.1&&
//								j-st<(NPK_s*2/3)
								j-st<=(NPK_s/2+3)&&j-st>=(NPK_s/2-3)
						){
							if (j-st<13) {
								Log.d("Doppler","vf["+j+"]="+vf[j]+", j-st="+(j-st));
								skip = true;
								break;
							}
						}
						if (vf_s[j]<=th4){
							if(j-st<=(NPK_s/2+3)&&j-st>=(NPK_s/2-3)){  // middle 7 line
								if (j-st<13){
									Log.d("Doppler","th4 = "+th4+" ,vf_s["+j+"]="+vf_s[j]+", j-st="+(j-st));
									skip = true;
									break;
								}
							}
						}
					}

					if (skip) continue;
				 	//cavin test 20210831 end
					//cavin test 20210901
					int vtiLen = se - st +1;
					Log.d("Doppler","VTI length = "+vtiLen);
					if (seg_no < SEG_s.length-1){
							if (seg_no>=1){
//								Log.d("Doppler","SEG_e["+(seg_no-1)+"]="+SEG_e[seg_no-1]+", st="+st);
								if(SEG_e[seg_no-1]==st){
									continue;
								}
							}
							int mid = ((st+se)/2);
							double midPwr = (vf_s[mid-1]+vf_s[mid]+vf_s[mid+1])/3;
							double[] pwrMultiple = new double[se-st+1];
							int pCount = 0;
							for (int k=st;k<=se;k++){
								//Log.d("Doppler","vf_s["+k+"]="+vf_s[k]);
								pwrMultiple[pCount] = vf_s[k]/midPwr;
//								Log.d("Doppler","pwrMultiple["+pCount+"]="+pwrMultiple[pCount]);
								pCount++;

							}
							double meanPwrMultiple = mean(pwrMultiple);
//							double maxMultiple = getMax(pwrMultiple);
							double maxMultiple = (pwrMultiple[0]+pwrMultiple[1]
													+pwrMultiple[pCount-2]+pwrMultiple[pCount-1])/4;
							double multiplePwrThreshold = (maxMultiple*0.3 > 10) ? (maxMultiple*0.3):10;

//							if (maxMultiple > 4000){
//								Log.d("Doppler","skip this maxMultiple = "+maxMultiple);
//								continue;
//							}

							// find minimum and maximum velocity.
							double minVF1 = 2;
							double minVF2 = 2;
							double maxVF = 0;
							for (int k=st;k<=se;k++){
								if (k<mid){
									if (vf[k]<minVF1){
										minVF1 = vf[k];
									}
								}else{
									if (vf[k]<minVF2){
										minVF2 = vf[k];
									}
								}

								if (vf[k]>maxVF){
									maxVF = vf[k];
								}
							}
							Log.d("Doppler","minVF1="+minVF1+", minVF2="+minVF2);
							// skip large power
							pCount = 0;
							int reduceIndex = -1;
//								Log.d("Doppler","multiplePwrThreshold = "+multiplePwrThreshold+" ,mid="+mid);
							for (int k=st;k<=se;k++) {
//								Log.d("Doppler","pwrMultiple["+pCount+"]="+pwrMultiple[pCount]+" ,k="+k);
								if (pwrMultiple[pCount]> multiplePwrThreshold&&(se-st)>(0.3*HR_int)&&pwrMultiple[pCount]<maxMultiple){
								Log.d("Doppler","meanPwrMultiple="+meanPwrMultiple+"  ,maxMultiple="+maxMultiple+
										" ,multiplePwrThreshold="+multiplePwrThreshold);
								if (k<=mid){

									Log.d("Doppler","pwrMultiple["+pCount+"]="+pwrMultiple[pCount]);
									st+=1;
									Log.d("Doppler","Too large power st++ , st="+st);
								}else{
									Log.d("Doppler","Too large power end reduce to "+ (k-1));
//										se = k-1;
									if ((k-1-st)>13){
										reduceIndex = k-1;
									}
									break;
								}
							}
							pCount++;
						}
						if (reduceIndex!=-1){
							se = reduceIndex;
						}
						// Find relative low value of velocity
						for (int k=st;k<=se;k++){
							if (k<(mid-3)){
								if (vf[k] == minVF1 && minVF1<0.5){
									Log.d("Doppler","segment start set to "+ k);
									st = k;
								}
							}else if(k>(mid+3)){
								if (vf[k] == minVF2 && minVF2<0.5){
									Log.d("Doppler","segment end set to "+ k);
									se = k;
								}
							}
						}

							int vtiWidth = se - st + 1;

							if (vtiWidth>th1 && vtiWidth<th5){
								Log.d("Doppler","vtiWidth = "+vtiWidth);
								if(vtiWidth>th5/2){
									Log.d("Doppler","injection time more the half heart beat time!");
								}
								SEG_s[seg_no]=st;
								SEG_e[seg_no]=se;
								seg_no=seg_no+1;
							}else{
								Log.d("Doppler","vtiWidth = "+vtiWidth+" ,th5="+th5);
							}
					}else{
						break;
					}
					//cavin test 20210901 end
				}else{
					Log.d("Doppler","i="+i+",th3 = "+th3+" ,vf_s["+st+"]="+vf_s[st]+" ,NPK_s="+NPK_s);
					Log.d("Doppler","something wrong??????");
				}
			}
			Log.d("Doppler","seg_no = "+seg_no);
		}

		int[][] result;
		int[][] SEG = new int[seg_no][2];
		for (int i=0;i<seg_no;i++) {
			SEG[i][0]=SEG_s[i];
			SEG[i][1]=SEG_e[i];
		}
		if (seg_no>=2){
			// cavin test 20210831
			//result=SEG_sel(SEG,HR_int,TP_in);
			result = VTI_sel(SEG,HR_int,vf,TP_in);
			// cavin test 20210831 end
		}else{
			result=new int[1][2];
			result[0][0]=1;
			result[0][1]=1;
		}
		return result;
	}

	public static int[][] HR_SEG(double HR_int, double[] TP_in)
	{
		int lng=TP_in.length;
		double[] TP;
		double[][] pk_tr;
		int[] SEG_s;
		int[] SEG_e;
		SEG_s=new int[40];
		SEG_e=new int[40];
//		result=new double[lng][2];
		TP=new double[lng];
		TP=TP_in;
//	   TP=MOV_AVG(TP_in,3);
		int th1,seg_no;
		th1=(int)(HR_int/3);
		double th2,PS1,MPS;
		int st,se,NPK_s;
		th2=mean(TP_in)*0.8;
		pk_tr=f_npeak(TP_in,10,1);
		int pkn=pk_tr.length;
		seg_no=0;
		for (int i=0;i<pkn-1;i++)
		{
			st=(int)pk_tr[i][0];
			se=(int)pk_tr[i+1][0];
			PS1=SEG_sel(TP,st,se);
			MPS=PS1/(se-st);
			NPK_s=se-st;
			if ((MPS>th2)&&(NPK_s>th1))
			{
				SEG_s[seg_no]=st;
				SEG_e[seg_no]=se;
				seg_no=seg_no+1;
			}
		}
		int[][] SEG;
		SEG=new int [seg_no][2];
		for (int i=0;i<seg_no;i++)
		{
			SEG[i][0]=SEG_s[i];
			SEG[i][1]=SEG_e[i];
		}
		int[][] result;
		if (seg_no>2)
		{
			result=SEG_sel(SEG,HR_int,TP_in);
		}
		else
		{
			result=new int[1][2];
			result[0][0]=1;
			result[0][1]=5;
		}
		return result;
	}


	public static double[][] HR_seg_PID_in(double[] arr,double HR_int) // 目標 B-W 比
	{
		int lng=arr.length;
		double[][] result,pk_tr;
		result=new double[40][2];
		pk_tr=f_peak(arr,10,0);
		System.out.print(" pk_number=  ");
		System.out.println( pk_tr[0][0]);
		int lng2;
		lng2=(int)pk_tr[0][0];
		double[] peak_id,peak_v;
		peak_id=new double[lng2+2];
		peak_v=new double[lng2+2];
		for(int j=1;j<(pk_tr[0][0]+1);j++)
		{
//		peak_id[j-1]=0;
//		peak_v[j-1]=0;
			peak_id[j-1]=pk_tr[j][0];
			peak_v[j-1]=pk_tr[j][1];
//		System.out.print(pk_tr[j][0] );
//		System.out.print(" xx  ");
//		System.out.println( pk_tr[j][0]);
		}
		double[] peak_id2,peak_v2;
		peak_id2=new double[pk_tr.length];
		peak_v2=new double[pk_tr.length];
		double[] SEG_s=new double[pk_tr.length];
		double[] SEG_e=new double[pk_tr.length];
		peak_v2=new double[pk_tr.length];
		double id_max,id_min,th1;
		id_max=getMax(peak_v);
		id_min=getMin(peak_v);
//	System.out.print(id_min );
//	System.out.print(" max_min ");
//	System.out.println(id_max );

		int pkn=0;
		int seg_no=0;
		int ki,d1,d2;
		th1=0.15*HR_int;
		for(int j=0;j<lng2;j++)
		{
			if (peak_v[j]>(id_max-id_min)*0.02)
			{
				peak_id2[pkn]=peak_id[j];
				peak_v2[pkn]=peak_v[j];
//			System.out.print(peak_id2[pkn] );
//			System.out.print(" real peak  ");
//			System.out.println( peak_v[pkn]);
				pkn++;
			}
		}
		if (pkn>3)
		{
			seg_no=0;
			ki=0;
			while (ki<pkn-2 )
			{
				d1=Math.abs((int)peak_id2[ki+1]-(int)peak_id2[ki]-(int)HR_int);
				d2=Math.abs((int)peak_id2[ki+2]-(int)peak_id2[ki]-(int)HR_int);
//    	 System.out.print(" d1= ");
//		 System.out.println( d1);
//		 System.out.print(" d2= ");
//		 System.out.println( d2);
				if ((d1<d2)&&(d1<th1))
				{
					SEG_s[seg_no]=peak_id2[ki]-(int)(peak_v2[ki]/2);
					SEG_e[seg_no]=peak_id2[ki+1]-(int)(peak_v2[ki+1]/2);
					seg_no++;
					ki=ki+1;
				}
				else if ((d1>d2)&&(d2<th1))
				{
					SEG_s[seg_no]=peak_id2[ki]-(int)(peak_v2[ki]/2);
					SEG_e[seg_no]=peak_id2[ki+2]-(int)(peak_v2[ki+2]/2);
					seg_no++;
					ki=ki+2;
				}
				else
				{
					ki=ki+1;
				}
			}
			if (seg_no==0)
			{
				SEG_s[seg_no]=1;
				SEG_e[seg_no]=1;
				seg_no=1;
			}
			if (pkn<3)
			{
				SEG_s[0]=1;
				SEG_e[0]=1;
				seg_no=1;
			}
		}

		for(int j=0;j<seg_no;j++)
		{
			result[j][0]=SEG_s[j];
			result[j][1]=SEG_e[j];
//			System.out.print(" st= ");
//			System.out.print( SEG_s[j]);
//			System.out.print(" se= ");
//			System.out.println(SEG_e[j] );
		}
		return(result);
	}

	public static double[][] f_npeak(double[] arr,int th,int th2) // th: 前後點數； th2: 最小Peak value
	{
		int lng=arr.length;
		int id=0;
		double[][] result=new double[lng][2];
		double[] L_arr;
		L_arr=new double[th];
		double[] R_arr,peak_id,peak_v;
		R_arr=new double[th];
		peak_id=new double[lng];
		peak_v=new double[lng];
		for(int i=0;i<th;i++)
		{
			L_arr[i]=200;
			R_arr[i]=200;
		}


		for(int j=th;j<lng-th;j++)
		{

			for(int i=1;i<th+1;i++)
			{
				L_arr[i-1]=arr[j-i];
				R_arr[i-1]=arr[j+i];
			}
//			System.out.print(j);
//		     System.out.print(':');
//		     System.out.print(arr[j]);
//		     System.out.print(':');
//			System.out.print(getMin(L_arr));
//		     System.out.print(',');
//		     System.out.println(getMin(R_arr));

//	 	 if((arr[j]<=getMin(L_arr))&&(arr[j]<getMin(R_arr))&&(getMax(R_arr)>0))
		if((arr[j]<=getMin(L_arr))&&(arr[j]<getMin(R_arr)))
	 	 {
	 	   peak_id[id]=j;
	 	   peak_v[id]=arr[j];
	 	  id=id+1;
//	 	 System.out.print("ID:");
//	 	 System.out.print(j);
//	     System.out.print(',');
//	     System.out.println(arr[j]);
	 	 }
	    }

		int id2=0;
		int pkn=0;
		System.out.print("id=");
		System.out.println(id);
		while(id2<id)
		{
			if (peak_v[id2]<=th2)
			{
				result[pkn][0]=peak_id[id2];
				result[pkn][1]=peak_v[id2];
				pkn++;
//			 	 System.out.print("ID:");
//			 	 System.out.print(peak_id[id2]);
//			     System.out.print(',');
//			     System.out.println(peak_v[id2]);
			}
			id2++;
		}
//		result=new double[pkn][2];
//		result[0][0]=pkn-1;
//		result[0][1]=pkn-1;
		return(result);

	}




	public static double[][] f_peak(double[] arr,int th,int th2) // th1: 前後點數； th2: 最小Peak value
	{
		int lng=arr.length;
		int id=0;
		int rid=0;
		double[][] result;
		result=new double[lng][2];
		double[] L_arr;
		L_arr=new double[th];
		double[] R_arr,peak_id,peak_v;
		R_arr=new double[th];
		peak_id=new double[lng];
		peak_v=new double[lng];

		for(int j=th;j<lng-th;j++)
		{
			for(int i=1;i<th;i++)
			{
				L_arr[i]=arr[j-i];
				R_arr[i]=arr[j+i];
			}
			if((arr[j]>getMax(L_arr))&&(arr[j]>getMax(R_arr)))
			{
				peak_id[id]=j;
				peak_v[id]=arr[j];
				id=id+1;
			}
		}
		int id2=0;
		int pkn=1;

		while(id2<id)
		{
			if (peak_v[id2]>=th2)
			{
				result[pkn][0]=peak_id[id2];
				result[pkn][1]=peak_v[id2];
				pkn++;
			}
			id2++;
		}
//	result=new double[pkn][2];
		result[0][0]=pkn-1;
		result[0][1]=pkn-1;
		return(result);
	}

	public static int[][] VTI_sel(int[][] arr, double HR_int, double[] VF_in, double[] TP_in)
	{
		int depth=arr.length;
		int width=arr[0].length;
		int[][] result=new int[40][2];
//		int[][] result1=new int[3][2];
		double[] VF=new double[40];
		double[] TPM = new double[40];
		int[] TopId;
		int[] meanId;
		int sn,ii;
		double PFS1,PFS2,PFS3;
		ii=0;
		sn=0;
		for(int i=0;i< result.length;i++){
			VF[i]=0;
			result[i][1]=0;
			result[i][0]=0;
		}
		int[] vtiWidth = new int[depth];
		for (ii=0;ii<depth;ii++){
//			if ((arr[ii+2][1]-arr[ii][0])>(HR_int*0.9)){
				result[ii][0] = arr[ii][0];
				result[ii][1] = arr[ii][1];
				VF[ii] = SEG_sel(VF_in,arr[ii][0],arr[ii][1]);
				vtiWidth[ii] = arr[ii][1]-arr[ii][0];

				double[] tmpSeg = new double[13];
//			System.arraycopy(VF_in,arr[ii][0],tmpSeg,0,tmpSeg.length);
				int mID = (arr[ii][0]+arr[ii][1])/2;
				System.arraycopy(TP_in,mID-6,tmpSeg,0,tmpSeg.length);
//				TPM[ii] = mean(tmpSeg);
				TPM[ii] = SEG_sel(tmpSeg,0,tmpSeg.length-1);
				Log.d("Doppler","TPM["+ii+"]="+TPM[ii]+" ,VF["+ii+"]="+VF[ii]+" ,VTI width["+ii+"]="+vtiWidth[ii]);
//			}
		}
		sn = ii;
//		while (ii<depth-2){
//			if ((arr[ii+2][1]-arr[ii][0])>HR_int){
//				int st1=arr[ii][0];
//				int ed1=arr[ii][1];
//				PFS1=SEG_sel(VF_in,st1,ed1);
//				int st2=arr[ii+1][0];
//				int ed2=arr[ii+1][1];
//				PFS2=SEG_sel(VF_in,st2,ed2);
//				int st3=arr[ii+2][0];
//				int ed3=arr[ii+2][1];
//				PFS3=SEG_sel(VF_in,st3,ed3);
//				if ((PFS1<PFS2)&&(PFS3<PFS2)){
//					result[sn][0]=arr[ii+1][0];
//					result[sn][1]=arr[ii+1][1];
//					TP[sn]=PFS2;
//					sn=sn+1;
//					ii=ii+2;
//				}else{
//					ii=ii+1;
//				}
//
//			}else{
//				ii=ii+1;
//			}
//		}
		System.out.println( sn);
		int tmpSn = sn;
		double meanWidth = 0;
		if (sn>=5){
			Methodoligies mMeth = new Methodoligies();
			double[] sortedVF = mMeth.getArrSort(Tag.INT_SORT_MAX,VF);
			int[] sortedWidth = mMeth.getArrSort(Tag.INT_SORT_MAX,vtiWidth);
			double[] sortedTPM = mMeth.getArrSort(Tag.INT_SORT_MAX, TPM);
			int[] tmpSortedWidth = new int[sn-2];
			System.arraycopy(sortedWidth,1,tmpSortedWidth,0,tmpSortedWidth.length);
			double[] tmpTPM = new double[sn-4];
			System.arraycopy(sortedTPM, 2,tmpTPM,0,tmpTPM.length);
			double medianVTI = sortedVF[sn/2];
			meanWidth = mMeth.getArrMean(sortedWidth,1,sn-2);
//			meanWidth = mMeth.getArrMean(sortedWidth,0,sn-1);
//			double meanTPM = mMeth.getArrMean(sortedTPM,2,sn-3);
			double meanTPM = mMeth.getArrMean(sortedTPM,0,sn-1);
			double VTIWidthSD = Methodoligies.getArraySD(tmpSortedWidth);
//			double VTIWidthSD = Methodoligies.getArraySD(sortedWidth);
			double TPMSD = Methodoligies.getArraySD(tmpTPM);
			Log.d("Doppler","medianVTI="+medianVTI);
			Log.d("Doppler","meanTPM="+meanTPM+" ,TPMSD="+TPMSD);
			Log.d("Doppler","meanWidth="+meanWidth+" ,VTIWidthSD="+VTIWidthSD);
			for (int i=0;i<sn;i++){
				if (TPM[i]==sortedTPM[0]||TPM[i]==sortedTPM[sn-1]){
					Log.d("Doppler","max or min TPM["+i+"]="+TPM[i]+" ,VF["+i+"]="+VF[i]);
					vtiWidth[i]=0;
					VF[i] = 0;
					TPM[i] = 0;
					tmpSn -= 1;
					continue;
				}
//				Log.d("Doppler","sortedVF["+i+"]="+sortedVF[i]);
				if (VTIWidthSD>0){
					if (vtiWidth[i]<(meanWidth-2*VTIWidthSD)||vtiWidth[i]>(meanWidth+2*VTIWidthSD)){
						Log.d("Doppler","vtiWidth["+i+"]="+vtiWidth[i]+">"+(meanWidth+2*VTIWidthSD)+" or < "+(meanWidth-2*VTIWidthSD));
//						Log.d("Doppler","TPM["+i+"]="+TPM[i]+" ,VF["+i+"]="+VF[i]);
						vtiWidth[i] = 0;
						VF[i] = 0;
						TPM[i] = 0;
						tmpSn -= 1;
						continue;
					}
				}
//				if (TPMSD>0){
//					if (TPM[i]<(meanTPM-2*TPMSD)||TPM[i]>(meanTPM+2*TPMSD)){
//						Log.d("Doppler","TPM["+i+"]="+TPM[i]+" ,VF["+i+"]="+VF[i]);
//						vtiWidth[i] = 0;
//						VF[i] = 0;
//						TPM[i] = 0;
//						tmpSn -= 1;
//					}
//				}
//				if (
////						VF[i]<0.4*medianVTI || VF[i]>1.4*medianVTI&&
//				vtiWidth[i]<(meanWidth-2*VTIWidthSD)||vtiWidth[i]>(meanWidth+2*VTIWidthSD)
//						||TPM[i]<(meanTPM-2*TPMSD)||TPM[i]>(meanTPM+2*TPMSD)
////						||TPM[i]<0.2*meanTPM||TPM[i]>4*meanTPM
//				){
//					Log.d("Doppler","TPM["+i+"]="+TPM[i]+" ,VF["+i+"]="+VF[i]);
//					vtiWidth[i] = 0;
//					VF[i] = 0;
//					TPM[i] = 0;
//					tmpSn -= 1;
//				}

			}
		}
		int outputN = 3;
		int[][] result1=new int[outputN][2];
		Log.d("Doppler","tmpSn="+tmpSn);
//		if (sn>=3){
		if (tmpSn >=3) {
//			TopId=topn_id(VF,3);
//			for (int i=0;i<3;i++){
//				result1[i][0]=result[TopId[2-i]][0];
//				result1[i][1]=result[TopId[2-i]][1];
//			}
//			result1[0][0]=result[TopId[0]][0];
//			result1[0][1]=result[TopId[0]][1];
//			result1[1][0]=result[TopId[1]][0];
//			result1[1][1]=result[TopId[1]][1];
//			result1[2][0]=result[TopId[2]][0];
//			result1[2][1]=result[TopId[2]][1];

			// Cavin add for test 20210913
//			meanId = topn_id(TPM, sn);

			double target = HR_int*0.3;
			Log.d("Doppler","target VTI length="+target);
			meanId = closest_ids(vtiWidth,outputN,target);
			for (int i=0;i<outputN;i++){
				//			for (int i=0;i<sn;i++){
//				Log.d("Doppler","meanId["+i+"]="+meanId[i]+" ,vti width="+vtiWidth[meanId[i]]);
				result1[i][0] = result[meanId[i]][0];
				result1[i][1] = result[meanId[i]][1];
			}
		}else if (tmpSn ==2){
//		}else if (sn==2){
			result1[0][0]=result[0][0];
			result1[0][1]=result[0][1];
			result1[1][0]=result[1][0];
			result1[1][1]=result[1][1];
			result1[2][0]=1;
			result1[2][1]=2;
		}else if (tmpSn ==1){
//		}else if (sn==1){
			result1[0][0]=result[0][0];
			result1[0][1]=result[0][1];
			result1[1][0]=1;
			result1[1][1]=2;
			result1[2][0]=1;
			result1[2][1]=2;
		}else{
			result1[0][0]=1;
			result1[0][1]=2;
			result1[1][0]=1;
			result1[1][1]=2;
			result1[2][0]=1;
			result1[2][1]=2;
		}
		return result1;
	}

public static int[][] SEG_sel(int[][] arr, double HR_int, double[] TP_in)
{
   int depth=arr.length;
   int width=arr[0].length;
   int[][] result;
   result=new int[20][2];
   int[][] result1;
   result1=new int[3][2];
   double[] TP;
   TP=new double[20];
   int[] Topid;
   Topid=new int[3];
   int sn,ii;
   double PFS1,PFS2,PFS3;
   ii=0;
   sn=0;
   for(int i=0;i<20;i++)
   {
  	 TP[i]=0;
  	 result[i][1]=0;
  	 result[i][0]=0;
   }
   while (ii<depth-2)
   {
  	if ((arr[ii+2][1]-arr[ii][0])>HR_int)
  	{
  		int st1=arr[ii][0];
  		int ed1=arr[ii][1];
  		PFS1=SEG_sel(TP_in,st1,ed1);
  		int st2=arr[ii+1][0];
  		int ed2=arr[ii+1][1];
  		PFS2=SEG_sel(TP_in,st2,ed2);
  		int st3=arr[ii+2][0];
  		int ed3=arr[ii+2][1];
  		PFS3=SEG_sel(TP_in,st3,ed3);
  		if ((PFS1<PFS2)&&(PFS3<PFS2))
  		{
  			result[sn][0]=arr[ii+1][0];
  			result[sn][1]=arr[ii+1][1];
  			TP[sn]=PFS2;
  			sn=sn+1;
  			ii=ii+2;
  		}
  		else
  		{
  			ii=ii+1;
  		}

  	}
  	else
  	{
  		ii=ii+1;
  	}
  }
   System.out.println( sn);
  if (sn>=3)
  {
  	Topid=topn_id(TP,3);
  	result1[0][0]=result[Topid[0]][0];
  	result1[0][1]=result[Topid[0]][1];
  	result1[1][0]=result[Topid[1]][0];
  	result1[1][1]=result[Topid[1]][1];
  	result1[2][0]=result[Topid[2]][0];
  	result1[2][1]=result[Topid[2]][1];
  	return result1;
  }
  else if (sn==2)
  {
  	result1[0][0]=result[0][0];
  	result1[0][1]=result[0][1];
  	result1[1][0]=result[1][0];
  	result1[1][1]=result[1][1];
  	result1[2][0]=1;
  	result1[2][1]=2;
  	return result1;
  }
  else if (sn==1)
  {
  	result1[0][0]=result[0][0];
  	result1[0][1]=result[0][1];
  	result1[1][0]=1;
  	result1[1][1]=2;
  	result1[2][0]=1;
  	result1[2][1]=2;
  	return result1;
  }
  else
  {
  	result1[0][0]=1;
  	result1[0][1]=2;
  	result1[1][0]=1;
  	result1[1][1]=2;
  	result1[2][0]=1;
  	result1[2][1]=2;
  	return result1;
  }
}

public static double SEG_sel(double[] arr, int ST, int ED)
{
//   int lng=arr.length;
   double temp=0;
   for (int ii=ST; ii<ED+1;ii++)
   {
      temp=temp+arr[ii];

   }
   return temp;
}

static int closest_id(int[] arr, double target){
	  	int result=0;
	  	double distance = Math.abs(arr[0]-target);
	  	for (int i=1;i<arr.length;i++){
	  		double cdistance = Math.abs(arr[i]-target);
	  		if (cdistance < distance){
	  			result = i;
	  			distance = cdistance;
			}
		}
	  	return result;
}

	static double max_distance(int[] arr, double target){
		double distance = Math.abs(arr[0]-target);
		double result=distance;
		for (int i=1;i<arr.length;i++){
			if (arr[i]!=0){
				double cdistance = Math.abs(arr[i]-target);
				if (cdistance > distance){
					result = cdistance;
					distance = cdistance;
				}
			}
		}
		return result;
	}

public static int[] closest_ids(int[] arr, int n, double target){
	  	int[] result = new int[n];
	  	int[] tmpArr = new int[arr.length];
	  	System.arraycopy(arr,0,tmpArr,0,arr.length);
		double maxDistance = max_distance(arr,target);
		int cn=0;
		int mid;
		while (cn<n){
			mid=closest_id(tmpArr,target);
			Log.d("closest_ids","tmpArr["+mid+"]="+tmpArr[mid]+" ,maxDistance="+maxDistance);
			tmpArr[mid]=(int)maxDistance+(int)target;
			result[cn]=mid;
			cn++;
		}
	  	return result;
}

public static int[] topn_id(double[] arr, int n)
{
	  int lng=arr.length;
	  int[] result =new int[n];
	  int cn=0;
	  int mid;
	  double arr_min;
	  arr_min=getMin(arr);
	  while(cn<n)
     {
       mid=maximum_id(arr);
       arr[mid]=arr_min;
       result[cn]=mid;
       cn++;
     }
	  return result;
}
public static double[][] normal2(double[][] arr)
{
	  int depth=arr.length;
	  int width=arr[0].length;
	  double maxValue = arr[0][0];
	  double minValue = arr[0][0];
	  double[][] o_arr;
    o_arr=new double[depth][width];
    double[] TP,MTP,TSC;
    TP=new double[width];
    MTP=new double[width];
    TSC=new double[width];
    double p_temp,p_max,p_min;
    for(int j=1;j <width;j++)
    {
  	  p_temp=0;
  	  for(int i=1;i <depth;i++)
	   {
		  p_temp=p_temp+arr[i][j];
	   }
  	  TP[j]=p_temp;
	   }
     MTP=MOV_AVG(TP,5);
     p_max=getMax(MTP);
     p_min=getMin(MTP);
     for(int j=1;j <width;j++)
     {
  	   TSC[j]=(1/(p_max-p_min))*(MTP[j]);
     }
	  for(int j=1;j < depth;j++)
	  for(int i=1;i < width;i++)
	    {
	      if(arr[j][i] > maxValue){
	         maxValue =arr[j][i]; }
	      if(arr[j][i] <minValue){
		         minValue =arr[j][i]; }
      }

//	  System.out.println( "Max="+maxValue);
//	  System.out.println( "MIn="+minValue);
	  for(int j=0;j < depth;j++)
	  for(int i=0;i < width;i++)
	{
			o_arr[j][i]=((arr[j][i]-0)/(maxValue-0))*255.0*TSC[i];
//			System.out.println( "Value="+o_arr[j][i]);
	}
	  return o_arr;

}

	public static short[] mDblArrayUltrasoundDCT(short[] dsIn) {
		int iLen = dsIn.length;
		double[] mDblArrayUltrasoundDCT;
		if (Integer.bitCount(iLen) != 1) {
			int nT = Integer.highestOneBit(iLen) << 1;
			mDblArrayUltrasoundDCT = new double[nT];
//			ma.setArrCopyOfRange(mDblArrayUltrasoundDCT, 0, dsIn, 0, iLen);
//
		} else {
			//mDblArrayUltrasoundDCT = dsIn;
//		double[] mDblArrayUltrasoundDCT = Arrays.copyOf(dsIn, iLen);
			mDblArrayUltrasoundDCT = new double[iLen];
		}
		for (int i=0;i<iLen;i++){
			mDblArrayUltrasoundDCT[i] = dsIn[i];
		}

		FastDctLee.transform(mDblArrayUltrasoundDCT);

		for (int i = 0; i < (mDblArrayUltrasoundDCT.length/30); i++) {
			mDblArrayUltrasoundDCT[i] = 0;
		}

		for (int i = (int) (mDblArrayUltrasoundDCT.length * 0.97); i < (mDblArrayUltrasoundDCT.length); i++) {
			mDblArrayUltrasoundDCT[i] = 0;
		}

		FastDctLee.inverseTransform(mDblArrayUltrasoundDCT);
		short[] outputArray = new short[iLen];
		for (int i = 0; i < mDblArrayUltrasoundDCT.length; i++)
			mDblArrayUltrasoundDCT[i] /= (mDblArrayUltrasoundDCT.length / 2.0);
//		mDblArrayUltrasoundDCT = Arrays.copyOf(mDblArrayUltrasoundDCT, iLen);
		for (int i=0;i<iLen;i++){
			outputArray[i] = (short) mDblArrayUltrasoundDCT[i];
		}
		return outputArray;
	}

	public static short[] mDblArrayUltrasoundDCTInverse(short[] dsIn) {
		int iLen = dsIn.length;
		double[] mDblArrayUltrasoundDCT;
		short[] outputArray = new short[iLen];

		if (Integer.bitCount(iLen) != 1) {
			int nT = Integer.highestOneBit(iLen) << 1;
			mDblArrayUltrasoundDCT = new double[nT];
//			ma.setArrCopyOfRange(mDblArrayUltrasoundDCT, 0, dsIn, 0, iLen);
//
		} else {
			//mDblArrayUltrasoundDCT = dsIn;
//		double[] mDblArrayUltrasoundDCT = Arrays.copyOf(dsIn, iLen);
			mDblArrayUltrasoundDCT = new double[iLen];
		}
		for (int i=0;i<iLen;i++){
			mDblArrayUltrasoundDCT[i] = dsIn[i];
		}

		FastDctLee.transform(mDblArrayUltrasoundDCT);

		for (int i = 0; i < (mDblArrayUltrasoundDCT.length/30); i++) {
			mDblArrayUltrasoundDCT[i] = 0;
		}

		for (int i = (int) (mDblArrayUltrasoundDCT.length * 0.97); i < (mDblArrayUltrasoundDCT.length); i++) {
			mDblArrayUltrasoundDCT[i] = 0;
		}

		double temp;
		for (int i = 0; i < mDblArrayUltrasoundDCT.length / 2; i++) {
			temp = mDblArrayUltrasoundDCT[i];
			mDblArrayUltrasoundDCT[i] = mDblArrayUltrasoundDCT[mDblArrayUltrasoundDCT.length - i - 1];
			mDblArrayUltrasoundDCT[mDblArrayUltrasoundDCT.length - i - 1] = temp;
		}

		FastDctLee.inverseTransform(mDblArrayUltrasoundDCT);

		for (int i = 0; i < mDblArrayUltrasoundDCT.length; i++)
			mDblArrayUltrasoundDCT[i] /= (mDblArrayUltrasoundDCT.length / 2.0);
//		mDblArrayUltrasoundDCT = Arrays.copyOf(mDblArrayUltrasoundDCT, iLen);
		for (int i=0;i<iLen;i++){
			outputArray[i] = (short) mDblArrayUltrasoundDCT[i];
		}
		return outputArray;
	}


}








