package com.gis.heartio.SignalProcessSubsysII.transformer;

import com.gis.heartio.SignalProcessSubsysII.utilities.Tag;
import com.gis.heartio.SignalProcessSubsysII.utilities.Methodoligies;


public class EasyFilter {
	/*
	 * TYPE I
	 * Low Pass Filter(FIR)
	 * Least Squares, Fs 8000Hz, Fpass 3000Hz, Fstop 3500Hz
	 * Discrete-Time FIR Filter (real)
	 * -------------------------------
	 * Filter Structure  : Direct-Form FIR
	 * Filter Length     : 65
	 * Stable            : Yes
	 * Linear Phase      : Yes (Type 4)
	 */

	private Methodoligies ma = new Methodoligies();
	public final double[] mDblArrLPFS8000FP500E800 = new double[]{
			0.002443023069782, 0.005884256722524, 0.008915723157158,  0.01026587532208,
			0.008776048991462,  0.00384037768714,-0.004212742311064, -0.01393158125396,
			-0.02284240694462,  -0.0278673861351, -0.02599539427444, -0.01505966181761,
			0.005573206284121,  0.03459682237263,  0.06877880460874,   0.1034329471221,
			0.133270828531,   0.1534508810148,   0.1605832866511,   0.1534508810148,
			0.133270828531,   0.1034329471221,  0.06877880460874,  0.03459682237263,
			0.005573206284121, -0.01505966181761, -0.02599539427444,  -0.0278673861351,
			-0.02284240694462, -0.01393158125396,-0.004212742311064,  0.00384037768714,
			0.008776048991462,  0.01026587532208, 0.008915723157158, 0.005884256722524,
			0.002443023069782
	};

	/*
	 * TYPE II
	 * Low Pass Filter(FIR)
	 * Chebyshev Type II, Fs 125Hz, Fstop 16Hz, Fpass 8Hz
	 * Discrete-Time FIR Filter (real)
	 * -------------------------------
	 * Filter Structure    : Direct-Form II, Second-Order Sections
	 * Number of Sections  : 33
	 * Stable              : Yes
	 * Linear Phase        : No
	 */

	public final double[] mDblArrLPFS125FP8E16 = new double[] {
			-0.004317266612765,  0.00349562376444, 0.006795193275435, 0.009942915922559,
			0.01024773085309, 0.005594627615781,-0.004367563104579, -0.01736749002494,
			-0.02853687953886, -0.03155315937265, -0.02072824229787, 0.006656181338304,
			0.04869801525595,  0.09856754859709,   0.1459931992271,   0.1800478963007,
			0.192431625885,   0.1800478963007,   0.1459931992271,  0.09856754859709,
			0.04869801525595, 0.006656181338304, -0.02072824229787, -0.03155315937265,
			-0.02853687953886, -0.01736749002494,-0.004367563104579, 0.005594627615781,
			0.01024773085309, 0.009942915922559, 0.006795193275435,  0.00349562376444,
			-0.004317266612765
	};

	/*
	 * TYPE III
	 * High Pass Filter(FIR)
	 * Chebyshev Type II, Fs 8000Hz, Fstop 64Hz, Fpass 96Hz
	 * Discrete-Time FIR Filter (real)
	 * -------------------------------
	 * Filter Structure    : Direct-Form II, Second-Order Sections
	 * Number of Sections  : 65
	 * Stable              : Yes
	 * Linear Phase        : No
	 */

	public final double[] mDblArrHPFS8000FP96E64 = new double[] {
			-0.008210235003472,-0.008787817175668,-0.009366941224323, -0.00994614344332,
			-0.01052394059776, -0.01109883494142, -0.01166931932728, -0.01223388239075,
			-0.0127910137852, -0.01333920944889, -0.01387697688208, -0.01440284041319,
			-0.01491534643272, -0.01541306857369, -0.01589461281748, -0.01635862250441,
			-0.01680378322834, -0.01722882759541, -0.01763253982722, -0.01801376018961,
			-0.01837138922862, -0.01870439179631, -0.01901180084956, -0.01929272100624,
			-0.0195463318439, -0.01977189092737, -0.01996873655251, -0.02013629019485,
			-0.02027405865275, -0.02038163587626,   -0.020458704474, -0.02050503689176,
			0.9794795037422, -0.02050503689176,   -0.020458704474, -0.02038163587626,
			-0.02027405865275, -0.02013629019485, -0.01996873655251, -0.01977189092737,
			-0.0195463318439, -0.01929272100624, -0.01901180084956, -0.01870439179631,
			-0.01837138922862, -0.01801376018961, -0.01763253982722, -0.01722882759541,
			-0.01680378322834, -0.01635862250441, -0.01589461281748, -0.01541306857369,
			-0.01491534643272, -0.01440284041319, -0.01387697688208, -0.01333920944889,
			-0.0127910137852, -0.01223388239075, -0.01166931932728, -0.01109883494142,
			-0.01052394059776, -0.00994614344332,-0.009366941224323,-0.008787817175668,
			-0.008210235003472
	};

	/*
	 * TYPE IV
	 * High Pass Filter(FIR)
	 * Least-squares, Fs 125Hz, Fstop 0.1Hz, Fpass 0.5Hz
	 * Discrete-Time FIR Filter (real)
	 * -------------------------------
	 * Filter Structure    : Direct-Form II, Second-Order Sections
	 * Number of Sections  : 65
	 * Stable              : Yes
	 * Linear Phase        : No
	 */

	public final double[] mDblArrHPFS125FP01E05 = new double[] {
			-0.002004987347028,-0.002006863048451,-0.002008620735404,-0.002010259811581,
			-0.00201177972063,-0.002013179946403,-0.002014460013189,-0.002015619485926,
			-0.002016657970398,-0.002017575113408,-0.002018370602937,-0.002019044168287,
			-0.0020195955802, -0.00202002465096,-0.002020331234479,-0.002020515226362,
			0.997979423436,-0.002020515226362,-0.002020331234479, -0.00202002465096,
			-0.0020195955802,-0.002019044168287,-0.002018370602937,-0.002017575113408,
			-0.002016657970398,-0.002015619485926,-0.002014460013189,-0.002013179946403,
			-0.00201177972063,-0.002010259811581,-0.002008620735404,-0.002006863048451,
			-0.002004987347028
	};
	/*
	 * TYPE V
	 * High Pass Filter(FIR)
	 * Least-squares, Fs 125Hz, Fstop 2Hz, Fpass 4Hz
	 * Discrete-Time FIR Filter (real)
	 * -------------------------------
	 * Filter Structure    : Direct-Form II, Second-Order Sections
	 * Number of Sections  : 37
	 * Stable              : Yes
	 * Linear Phase        : No
	 */

	public final double[] mDblArrHPFS125FP2E4 = new double[] {
			0.06322925984674, -0.03507930836426,  -0.0298180397147, -0.02729042232323,
			-0.02660717952567, -0.02717652664017, -0.02857451771088,  -0.0304911926174,
			-0.03266826368899, -0.03502597913721, -0.03733940403684, -0.03959590598359,
			-0.04170289853679, -0.04348233705972, -0.04512197434919, -0.04640993355919,
			-0.04736801541107, -0.04787205747393,   0.9519420662548, -0.04787205747393,
			-0.04736801541107, -0.04640993355919, -0.04512197434919, -0.04348233705972,
			-0.04170289853679, -0.03959590598359, -0.03733940403684, -0.03502597913721,
			-0.03266826368899,  -0.0304911926174, -0.02857451771088, -0.02717652664017,
			-0.02660717952567, -0.02729042232323,  -0.0298180397147, -0.03507930836426,
			0.06322925984674
	};

	/*
	 * TYPE IV
	 * High Pass Filter(FIR)
	 * Least-squares, Fs 125Hz, Fstop 4Hz, Fpass 6Hz
	 * Discrete-Time FIR Filter (real)
	 * -------------------------------
	 * Filter Structure    : Direct-Form II, Second-Order Sections
	 * Number of Sections  : 65
	 * Stable              : Yes
	 * Linear Phase        : No
	 */

	public final double[] mDblArrHPFS125FP1E2 = new double[] {
			-0.01616908078161, -0.01705469018321, -0.01791789280615, -0.01875430785803,
			-0.01955964843852, -0.02032974993254, -0.02106059773681, -0.02174835411197,
			-0.02238938395859, -0.02298027932263, -0.02351788244556, -0.02399930718457,
			-0.02442195864038, -0.02478355084392, -0.02508212236704, -0.02531604973863,
			-0.02548405856395, -0.02558523226213,   0.9743809816446, -0.02558523226213,
			-0.02548405856395, -0.02531604973863, -0.02508212236704, -0.02478355084392,
			-0.02442195864038, -0.02399930718457, -0.02351788244556, -0.02298027932263,
			-0.02238938395859, -0.02174835411197, -0.02106059773681, -0.02032974993254,
			-0.01955964843852, -0.01875430785803, -0.01791789280615, -0.01705469018321,
			-0.01616908078161
	};

	/*
	 * TYPE IV
	 * Low Pass Filter(FIR)
	 * Least-squares, Fs 125Hz, Fstop 1Hz, Fpass 2Hz
	 * Discrete-Time FIR Filter (real)
	 * -------------------------------
	 * Filter Structure    : Direct-Form II, Second-Order Sections
	 * Number of Sections  : 65
	 * Stable              : Yes
	 * Linear Phase        : No
	 */

	public final double[] mDblArrLPFS125FP1E2 = new double[] {
			0.01616908078161,  0.01705469018321,  0.01791789280615,  0.01875430785803,
			0.01955964843852,  0.02032974993254,  0.02106059773681,  0.02174835411197,
			0.02238938395859,  0.02298027932263,  0.02351788244556,  0.02399930718457,
			0.02442195864038,  0.02478355084392,  0.02508212236704,  0.02531604973863,
			0.02548405856395,  0.02558523226213,  0.02561901835544,  0.02558523226213,
			0.02548405856395,  0.02531604973863,  0.02508212236704,  0.02478355084392,
			0.02442195864038,  0.02399930718457,  0.02351788244556,  0.02298027932263,
			0.02238938395859,  0.02174835411197,  0.02106059773681,  0.02032974993254,
			0.01955964843852,  0.01875430785803,  0.01791789280615,  0.01705469018321,
			0.01616908078161
	};

	/*
	 * TYPE IV
	 * Hig Pass Filter(FIR)
	 * Least-squares, Fs 125Hz, Fstop 8Hz, Fpass 16Hz
	 * Discrete-Time FIR Filter (real)
	 * -------------------------------
	 * Filter Structure    : Direct-Form II, Second-Order Sections
	 * Number of Sections  : 65
	 * Stable              : Yes
	 * Linear Phase        : No
	 */

	public final double[] mDblArrHPFS125FP8E16 = new double[] {
			-0.0003564468349158,-0.002650810363236,-0.005667076725613,-0.007956286012789,
			-0.007536529046283,-0.002789128228873, 0.006431028607636,  0.01797454056212,
			0.02737051575306,  0.02884677514689,  0.01722117551526,-0.009943909109768,
			-0.05076327360127, -0.09876135736974,  -0.1442140004445,  -0.1767869436467,
			0.8113786579332,  -0.1767869436467,  -0.1442140004445, -0.09876135736974,
			-0.05076327360127,-0.009943909109768,  0.01722117551526,  0.02884677514689,
			0.02737051575306,  0.01797454056212, 0.006431028607636,-0.002789128228873,
			-0.007536529046283,-0.007956286012789,-0.005667076725613,-0.002650810363236,
			-0.0003564468349158
	};

	public int[] filterFIR(boolean bAbsValue, double[] dsFIRWnd, int[] iArr) {
		int[] fil = new int[iArr.length];
		int iHalfWnd = dsFIRWnd.length/2;
		int iStart = iHalfWnd;
		int iEnd = iArr.length - iHalfWnd -1;
		for(int i=0; i<iArr.length; i++) {
			if ((i < iStart) || i >= iEnd) {
				fil[i] = 0;
			}else {
				double sum = 0.0;
				int iCnt = 0;
				for (int ii = (0 - iHalfWnd); ii <= iHalfWnd; ii++) {
					sum += (double)iArr[i-ii] * dsFIRWnd[iCnt++];
				}

				if (bAbsValue) {
					fil[i] = (int)sum;
				} else {
					fil[i] = Math.abs((int)sum);
				}

			}

		}
		return fil;
	}

	public double[] filterFIR(int intMode, double[] dsFIRWnd, double[] dArr) {
		int iStart;
		int iEnd = dArr.length;
		if(intMode == Tag.INT_BEFORE) {
			iStart = dsFIRWnd.length;
			iEnd = dArr.length - 1;
		}else {
			iStart = 0;
			iEnd = dArr.length - dsFIRWnd.length -1;
		}

		double[] fil = new double[dArr.length];
		for(int i=0; i<dArr.length; i++) {
			if ((i < iStart) || i >= iEnd) {
				fil[i] = dArr[i] * ma.getArrSum(dsFIRWnd);
			}else {
				double sum = 0.0;
				int iCnt = 0;
				if(intMode == Tag.INT_BEFORE) {
					for (int ii = 0; ii < dsFIRWnd.length; ii++) {
						sum += (double)dArr[i-ii] * dsFIRWnd[iCnt++];
					}
				}else {
					for (int ii = 0; ii < dsFIRWnd.length; ii++) {
						sum += (double)dArr[i+ii] * dsFIRWnd[iCnt++];
					}
				}

				fil[i] = sum;

			}

		}
		return fil;
	}

	public int[] filterFIR(int strMode, double[] dsFIRWnd, int[] iArr) {
		if(strMode != Tag.INT_BEFORE) return null;

		int[] fil = new int[iArr.length];
		int iStart = dsFIRWnd.length;
		int iEnd = iArr.length - dsFIRWnd.length -1;
		for(int i=0; i<iArr.length; i++) {
			if ((i < iStart) || i >= iEnd) {
				fil[i] = 0;
			}else {
				double sum = 0.0;
				int iCnt = 0;
				for (int ii = 0; ii < dsFIRWnd.length; ii++) {
					sum += (double)iArr[i-ii] * dsFIRWnd[iCnt++];
				}

				fil[i] = (int)sum;

			}

		}
		return fil;
	}

	public int[] filterFIR(double[] dsFIRWnd, int[] iArr) {
		int[] fil = new int[iArr.length];
		int iHalfWnd = dsFIRWnd.length/2;
		int iStart = iHalfWnd;
		int iEnd = iArr.length - iHalfWnd -1;
		for(int i=0; i<iArr.length; i++) {
			if ((i < iStart) || i >= iEnd) {
				fil[i] = iArr[i];
			}else {
				double sum = 0.0;
				int iCnt = 0;
				for (int ii = (0 - iHalfWnd); ii <= iHalfWnd; ii++) {
					sum += (double)iArr[i-ii] * dsFIRWnd[iCnt++];
				}

				fil[i] = (int)sum;

			}

		}
		return fil;
	}


	public double[] filterShoot(double[] dArr, int Field, double Shoot) {
		double[] r = ma.getArrCopyOfRange(dArr, 0, dArr.length - 1);
		for(int i=0; i< r.length; i++) {
			int iS = i;
			int iE = iS + Field;
			if (iE < dArr.length) {
				double dBilaterMax = Math.max(dArr[iS], dArr[iE]);
				double dBilaterMean = (dArr[iS] + dArr[iE]) / 2;
				for (int j = iS + 1; j < iE; j++) {
					if((r[j] - dBilaterMax) > Shoot) {
						r[j] = dBilaterMean;
					}
				}

			}
		}
		return r;
	}


	public double[] filterFIR(double[] dsFIRWnd, double[] dArr) {
		double[] fil = new double[dArr.length];
		int iHalfWnd = dsFIRWnd.length/2;
		int iStart = iHalfWnd;
		int iEnd = dArr.length - iHalfWnd -1;
		for(int i=0; i<dArr.length; i++) {
			if ((i < iStart) || i >= iEnd) {
				fil[i] = dArr[i];
			}else {
				double sum = 0.0;
				int iCnt = 0;
				for (int ii = (0 - iHalfWnd); ii <= iHalfWnd; ii++) {
					sum += (double)dArr[i-ii] * dsFIRWnd[iCnt++];
				}
				fil[i] = sum;

			}

		}
		return fil;
	}


	public int[] filterWavtoTriangle(int[] iArr) {
		int[] fil = this.filterFIR(this.mDblArrHPFS125FP8E16, iArr);
		int sum = 0;
		for(int i=0; i<iArr.length; i++) {
			if (fil[i] > 0) {
				sum = 0;
			}else {
				sum += 1;
			}
			fil[i] = sum;
		}
		return fil;
	}

	public int[] filterWavtoTriangle(int iMovingSize, int[] iArr) {
		double[] dsWnd = new double[iMovingSize];
		for(int i = 0; i<iMovingSize; i++) {
			dsWnd[i] = 1.0/(double) iMovingSize;
		}

		int[] fil = this.filterFIR(dsWnd, iArr);
		for(int i=0; i<iArr.length; i++) {
			double sum = 0.0;
			int iCnt = 0;

			if (fil[i] > (iArr[i])) {
				fil[i] = 1;
			}else {
				fil[i] =  0;
			}
			if (i>1) {
				if ((fil[i]==fil[i-2]) && (fil[i]!=fil[i-1])) {
					fil[i-1] = fil[i];
				}
			}
		}
		for(int i=1; i<iArr.length; i++) {
			double sum = 0.0;
			if (fil[i] == 1) {
				if(fil[i-1] < 0) {
					fil[i] = 0;
				}else {
					fil[i] = fil[i-1] + 1;
				}
			}else {
				if(fil[i-1] > 0) {
					fil[i] = 0;
				}else {
					fil[i] = fil[i-1] - 1;
				}
			}
		}

/*
		for(int i=1; i<iArr.length; i++) {
			double sum = 0.0;
			int iCnt = 0;
			if (fil[i] > iArr[i]) {
				if(fil[i-1] < 0) {
					fil[i] = 0;
				}else {
					fil[i] = fil[i-1] + 1;
				}
			}else {
				if(fil[i-1] > 0) {
					fil[i] = 0;
				}else {
					fil[i] = fil[i-1] - 1;
				}
			}
		}
*/

		return fil;
	}

	public int[] filterWavtoTriangle1(int iMovingSize, int[] iArr) {
		double[] dsWnd = new double[iMovingSize];
		for(int i = 0; i<iMovingSize; i++) {
			dsWnd[i] = 1.0/(double) iMovingSize;
		}

		int[] fil = new int[iArr.length];
		int iHalfWnd = iMovingSize/2;
		int iStart = iHalfWnd;
		int iEnd = iArr.length - iHalfWnd -1;
		for(int i=0; i<iArr.length; i++) {
			if ((i < iStart) || i >= iEnd) {
				fil[i] = 0;
			}else {
				double sum = 0.0;
				int iCnt = 0;
				for (int ii = (0 - iHalfWnd); ii <= iHalfWnd; ii++) {
					sum += (double)iArr[i-ii] * dsWnd[iCnt++];
				}

				/* square
				if (sum > iArr[i]) {
					fil[i] = 1;
				}else {
					fil[i] = 0;
				}
				//*/

				//* triangle
				if (sum > iArr[i]) {
					if(fil[i-1] < 0) {
						fil[i] = 0;
					}else {
						fil[i] = fil[i-1] + 1;
					}
				}else {
					if(fil[i-1] > 0) {
						fil[i] = 0;
					}else {
						fil[i] = fil[i-1] - 1;
					}
					//*/

				}
			}

		}
		return fil;
	}


//	public int[] wavFilLP(int[] iArr){
//		int len = iArr.length;
//		int[] env = new int[len];
//		env = this.filterFIR(1, iArr);
//		return env;
//	}

	public int[] wavSquare(int[] iArr){
		int[] fil = this.filterFIR(mDblArrHPFS8000FP96E64, iArr);
		int[] env = this.envelop(Tag.INT_PEAK, fil, 64);

		for (int i=0; i<iArr.length; i++) {
			if (fil[i]>0) fil[i]=1;
			else fil[i]=-1;
			fil[i] = fil[i] * env[i/64];
		}
		return fil;
	}


	public int[] envelop(int iType, int[] iArr, int iPeriod){
		int len = iArr.length / iPeriod;
		int[] env = new int[len];
		int type = 0;
		if (iType==Tag.INT_PEAK) {
			type = 1;
		}else if(iType==Tag.INT_VALLEY) {
			type = 2;
		}
		for (int i = 0; i< len; i++) {
			int iCompareValue = 0;
			if (type ==1) {
				iCompareValue = Integer.MIN_VALUE;
			}else if(type==2) {
				iCompareValue = Integer.MAX_VALUE;
			}
			int iPos = i * iPeriod;
			for (int ii = 0 ; ii< iPeriod; ii++) {
				int iIdx = iPos + ii;
				if (iIdx > iArr.length) {break;}
				if (iCompareValue < iArr[iIdx] && type==1) {iCompareValue = iArr[iIdx];}
				if (iCompareValue > iArr[iIdx] && type==2) {iCompareValue = iArr[iIdx];}
			}
			env[i] = iCompareValue;
		}
		env = this.filterFIR(mDblArrLPFS125FP8E16, env);
		return env;
	}



	public double[] wndHamming(int size)
	{
		double[] r = new double[size];
		for (int i= 0; i < size; i++)
		{
			r[i] = ((( 0.5 * (1-Math.cos( (2 * Math.PI * i) / (size - 1)))))) ;
		}
		return r;
	}




	private double[] getMaxPos(double[] In) {
		double[] r = new double[2];
		r[0] = 0;
		r[1] = -Double.MAX_VALUE;
		for (int i = 0; i < In.length; i++) {
			if(r[1] <= In[i]) {
				r[0] = i;
				r[1] = In[i];
			}
		}
		return r;
	}

	private double[] getMinPos(double[] In) {
		double[] r = new double[2];
		r[0] = 0;
		r[1] = Double.MAX_VALUE;
		for (int i = 0; i < In.length; i++) {
			if(r[1] >= In[i]) {
				r[0] = i;
				r[1] = In[i];
			}
		}
		return r;
	}




	public double[][] Wufilter(double[][] In, int Level){
		double[][] r = new double[In.length][In[0].length];
		for (int x = 0; x < In.length; x++) {
			double[] MaxPos = getMaxPos(In[x]);
			double[] MinPos = getMinPos(In[x]);
			double threshold = MinPos[1] + ((MaxPos[1] - MinPos[1])/(double)Level);
			int iSearch = (int)MaxPos[0];
			if(iSearch > 20) iSearch = 20;

			boolean check = false;
			while(!check && (iSearch+5) < In[x].length) {
				if (
						In[x][iSearch] < threshold
								&& In[x][iSearch+1] < threshold
								&& In[x][iSearch+2] < threshold
								&& In[x][iSearch+3] < threshold
								&& In[x][iSearch+4] < threshold
								&& In[x][iSearch+5] < threshold

				) {
					check = true;
				}
				iSearch = iSearch + 1;
			}
			for(int y = 0; y < In[x].length; y++) {
				if (y <= iSearch) {
					r[x][y] = In[x][y];
				}else {
					r[x][y] = 0;
				}
			}
		}

		return r;
	}

	public double[][] matrixNormal(double[][] A
			, double max, double min
			, double targetMax){
		double[][] r = new double[A.length][A[0].length];
		for (int x = 0; x < A.length; x++) {
			for(int y = 0; y < A[x].length; y++) {
				r[x][y] = ((targetMax) / (max - min))
						* (A[x][y]- min);
				if(r[x][y]>targetMax)
					r[x][y] = targetMax;
				else if(r[x][y] < 0)
					r[x][y] = 0;
			}
		}
		return r;

	}


	public double[][] matrixWindow(int strWndType, double[][] A){
		double[][] r  = new double[A.length][A[0].length];
		double[] wnd = wndHamming(A[0].length);
		if(strWndType != Tag.INT_WND_HAMMING) {
			return A;
		}

		for(int x = 0; x < A.length; x++) {
			for(int y = 0; y < A[0].length; y++) {
				r[x][y] = A[x][y] * wnd[y];
			}

		}

		return r;
	}
}
