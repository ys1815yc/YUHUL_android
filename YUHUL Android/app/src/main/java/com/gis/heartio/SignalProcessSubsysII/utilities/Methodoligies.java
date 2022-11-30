package com.gis.heartio.SignalProcessSubsysII.utilities;

import java.util.Arrays;

public class Methodoligies {
	public Methodoligies() {
	}
	
	public int[][] calSpectrumBitImgMap(double[][] dImg, int iStart, int iEnd, double dScale){
		int[][] r = new int[iEnd-iStart+1][dImg[0].length];
		for (int x = 0; x < r.length; x++) {
			for(int y = 0; y < r[0].length; y++) {
				if(dImg[x + iStart][y] > dScale) {
					r[x][y] = 1;
				}else {
					r[x][y] = 0;
				}
			}
		}
		return r;
	}

	public double calCompareXortoSum(int[][] dImg, int iLowPos, int iHighPos,int iStart, int iOffset, int iLength) {
		double sum = 0;
		int iX = dImg.length, iY = dImg[0].length;
		
		for (int x = 0; x < iLength; x++) {
			for (int y = iLowPos; y <= iHighPos; y++) {
				if((dImg[iStart + x][y] ^ dImg[iStart + x + iOffset][y]) == 0) {
					sum +=1;
				}
			}
		}
		return sum;
	}

	public double getArrSum(int[] Input, int iStart, int iEnd) {
		double dMean = 0;
		iEnd = Math.min(iEnd, Input.length-1);
		for (int iVar = iStart; iVar <= iEnd; iVar++) {
			dMean += Input[iVar];
		}
		return dMean;
	}
	public double getArrSum(double[] dsInput, int iStart, int iEnd) {
		iStart = Math.max(0, iStart);
		iEnd = Math.min(iEnd, dsInput.length-1);
		double dMean = 0;
		for (int iVar = iStart; iVar <= iEnd; iVar++) {
			dMean += dsInput[iVar];
		}
		return dMean;
	}

	public double getArrMean(int[] dsInput, int iStart, int iEnd) {
		double dMean = 0;
		iEnd = Math.min(iEnd, dsInput.length-1);
		for (int iVar = iStart; iVar <= iEnd; iVar++) {
			dMean += dsInput[iVar];
		}
		dMean = dMean / (double) (iEnd-iStart+1);
		return dMean;
	}
	public double getArrMean(double[] dsInput, int iStart, int iEnd) {
		double dMean = 0;
		iEnd = Math.min(iEnd, dsInput.length-1);
		for (int iVar = iStart; iVar <= iEnd; iVar++) {
			dMean += dsInput[iVar];
		}
		dMean = dMean / (double) (iEnd-iStart+1);
		return dMean;
	}

	public double[] getArrStrengthGM(double[] dsInput, int iStart, int iEnd) {
		//AB-> = (a, b), AC-> = (c,d), Area = |ad -bc|
		double[] dsSort = Arrays.copyOfRange(dsInput, iStart, iEnd);
		//Arrays.sort(dsSort);
		for(int i = 1; i< dsSort.length; i++) {
			dsSort[i] += dsSort[i-1];
		}

		double[] nowPoint = new double[] {0, dsSort[0]};
		double[] startPoint = new double[] {0, dsSort[0]};
		double[] vStart2Now = new double[] {dsSort[Tag.mIntX] - dsSort[Tag.mIntX],
				dsSort[Tag.mIntY] - dsSort[Tag.mIntY]};
		double[] vStart2End = new double[] {dsSort.length - 1,
				dsSort[dsSort.length - 1]-dsSort[0]};
		double[] dsGM = new double[]{0, 0};


		double dAreaMax = - Double.MAX_VALUE;
		double[] dArea = new double[dsSort.length];
		for (int i = 0; i<dsSort.length; i++) {
			nowPoint[Tag.mIntX] = i;
			nowPoint[Tag.mIntY] = dsSort[i];

			vStart2Now[Tag.mIntX] = nowPoint[Tag.mIntX] - startPoint[Tag.mIntX];
			vStart2Now[Tag.mIntY] = nowPoint[Tag.mIntY] - startPoint[Tag.mIntY];

			dArea[i] = vStart2Now[Tag.mIntY] * vStart2End[Tag.mIntX] - vStart2Now[Tag.mIntX]*vStart2End[Tag.mIntY]  ;
			if (dArea[i] > dAreaMax) {
				dsGM[Tag.mIntX] = i;
				dsGM[Tag.mIntY] = dsSort[i];
				dAreaMax = dArea[i];
			}
		}
		dsGM[Tag.mIntX] += iStart;

		return dsGM;
		//return getArrMean(dsSort, (int)dsGM[Tag.mIntX], dsSort.length-1);
	}


	public double getArrStrengthRangeMean(double[] dsInput, double dP1, double dP2) {
		double[] dsSort = Arrays.copyOfRange(dsInput, 0, dsInput.length - 1);
		Arrays.sort(dsSort);
		int iS = (int)(dsSort.length * dP1);
		int iE = (int)(dsSort.length * dP2);
		iE = Math.min(dsSort.length - 1, iE);
		double dM = 0;
		int iC = 0;
		for (int i = iS; i <= iE; i++) {
			dM += dsSort[i];
			iC ++;
		}
		
		return (dM / (double)iC);
	}

	
	public double getArrStrengthOrder(double[] dsInput, double dPercentage, int iStart, int iEnd) {
		double[] dsSort = Arrays.copyOfRange(dsInput, iStart, iEnd);
		Arrays.sort(dsSort);
		int dMaxIdx = (int)(dPercentage * dsSort.length) - 1;
		if (dMaxIdx < 0) dMaxIdx = 0;
		return dsSort[dMaxIdx];
	}


	public double[] getArrMaxPos(double[] dsInput, int iStart, int iEnd) {
		double dMax = - Double.MAX_VALUE;
		int iPos = 0;
		iStart = Math.max(iStart, 0);
		iStart = Math.min(iStart, dsInput.length - 1);
		iEnd = Math.max(iEnd, iStart);
		iEnd = Math.min(iEnd, dsInput.length - 1);
		double[] dsPos = new double[Tag.INT_POS_TAGSIZE];
		for (int iVar = iStart; iVar <= iEnd; iVar++) {
			if (dMax < dsInput[iVar]) {
				dMax = dsInput[iVar];
				iPos = iVar;
			}
		}
		dsPos[Tag.INT_POS_IDX] = iPos;
		dsPos[Tag.INT_POS_VAL] = dMax;
		return dsPos;
	}
	
	public int[] getArrMaxPos(int[] isInput, int iStart, int iEnd) {
		int iMax = Integer.MIN_VALUE;
		int iPos = 0;
		int[] isPos = new int[Tag.INT_POS_TAGSIZE];
		for (int iVar = iStart; iVar <= iEnd; iVar++) {
			if (iMax < isInput[iVar]) {
				iMax = isInput[iVar];
				iPos = iVar;
			}
		}
		isPos[Tag.INT_POS_IDX] = iPos;
		isPos[Tag.INT_POS_VAL] = iMax;
		return isPos;
	}

	public int getArrMax(int[] isInput, int iStart, int iEnd) {
		int iMax = - Integer.MAX_VALUE;
		iEnd = Math.min(isInput.length - 1, iEnd);
		for (int iVar = iStart; iVar <= iEnd; iVar++) {
			if (iMax < isInput[iVar]) {
				iMax = isInput[iVar];
			}
		}
		return iMax;
	}
	
	public double getArrMax(double[] isInput) {
		double iMax = - Double.MAX_VALUE;
		for (double v : isInput) {
			if (iMax < v) {
				iMax = v;
			}
		}
		return iMax;
	}
	

	public double getArrMax(double[] dsInput, int iStart, int iEnd) {
		double dMax = - Double.MAX_VALUE;
		iStart = Math.max(0, iStart);
		iEnd = Math.min(dsInput.length - 1, iEnd);
		for (int iVar = iStart; iVar <= iEnd; iVar++) {
			if (dMax < dsInput[iVar]) {
				dMax = dsInput[iVar];
			}
		}
		return dMax;
	}



	public int getArrMin(int[] isInput, int iStart, int iEnd) {
		int Min = Integer.MAX_VALUE;
		for (int iVar = iStart; iVar <= iEnd; iVar++) {
			if (Min > isInput[iVar]) {
				Min = isInput[iVar];
			}
		}
		return Min;
	}
	
	public double getArrMin(double[] dsInput, int iStart, int iEnd) {
		iStart = Math.max(0, iStart);
		iEnd = Math.min(iEnd, dsInput.length - 1);
		
		double dMin = Double.MAX_VALUE;
		for (int iVar = iStart; iVar <= iEnd; iVar++) {
			if (dMin > dsInput[iVar]) {
				dMin = dsInput[iVar];
			}
		}
		return dMin;
	}

	public double[] getArrMinPos(double[] dsInput, int iStart, int iEnd) {
		double[] isResult = new double[Tag.INT_POS_TAGSIZE]; 
		double dMin = Double.MAX_VALUE;
		iEnd = Math.min(iEnd, dsInput.length - 1);
		int iPos = 0;
		for (int iVar = iStart; iVar <= iEnd; iVar++) {
			if (dMin > dsInput[iVar]) {
				dMin = dsInput[iVar];
				iPos = iVar;
			}
		}
		isResult[Tag.INT_POS_IDX] = iPos;
		isResult[Tag.INT_POS_VAL] = dMin;
		return isResult;
	}

	private int[] getArrMinPos(int[] dsInput, int iStart, int iEnd) {
		int[] isResult = new int[2]; 
		int iMin = Integer.MAX_VALUE;
		int iPos = 0;
		for (int iVar = iStart; iVar <= iEnd; iVar++) {
			if (iMin > dsInput[iVar]) {
				iMin = dsInput[iVar];
				iPos = iVar;
				
			}
		}
		isResult[0] = iPos;
		isResult[1] = iMin;
		return isResult;
	}
	

		public double[][] getArrValleyPos(int strMode, double[] dsInput, int deNoiseWidth,
			int iStart, int iEnd) {
		double dMin = Double.MAX_VALUE;
		int iLen = dsInput.length;
		int iCnt = ((iEnd-iStart+1)/deNoiseWidth);
		int iPos = 0;
		double[][] isMin = new double[iCnt][Tag.INT_POS_TAGSIZE]; //[0][] = Position, [1][] = Value
		double[] isMean = new double[iCnt]; //[][0] = Position, [1][] = Value
		double[][] isValley = new double[Tag.INT_POS_TAGSIZE][iCnt]; //[0][] = Position, [1][] = Value
		for (int iVar = 0; iVar < iCnt; iVar++) {
			int iS = iStart + iVar * deNoiseWidth;
			int iE = iS + deNoiseWidth -1;
			if(iE < dsInput.length) {
				if(Tag.INT_VALLEY == strMode) {
					isMin[iVar] = this.getArrMinPos(dsInput, iS,iE);
				}else if(Tag.INT_PEAK == strMode){
					isMin[iVar] = this.getArrMaxPos(dsInput, iS,iE);
				}
				
				isMean[iVar] = this.getArrMean(dsInput, iS,iE);
			}
		}
		
		int iValley = 0;
		int iMode = 0;
		if(Tag.INT_VALLEY == strMode) {
			iMode = 1;
		}else if(Tag.INT_PEAK == strMode){
			iMode = -1;			
		}
		for (int i = 1; i < iCnt-1; i++) {
			double preVal = iMode * isMean[i-1];
			double nowVal = iMode * isMean[i];
			double aftVal = iMode * isMean[i+1];
			if ((preVal >= nowVal) && (aftVal > nowVal)) {
				isValley[Tag.INT_POS_IDX][iValley] = isMin[i][Tag.INT_POS_IDX];
				isValley[Tag.INT_POS_VAL][iValley] = isMin[i][Tag.INT_POS_VAL];
				iValley ++;
			}
		}
		double[][] isValleyResult = new double[Tag.INT_POS_TAGSIZE][iValley]; //[0][] = Position, [1][] = Value
		for (int i = 0; i < iValley; i++) {
			//if ((isMin[i-1][1]>=isMin[i][1]) && (isMin[i+1][1]>isMin[i][1])) {
			isValleyResult[Tag.INT_POS_IDX][i]=isValley[Tag.INT_POS_IDX][i];
			isValleyResult[Tag.INT_POS_VAL][i]=isValley[Tag.INT_POS_VAL][i];
		}

		return isValleyResult;
	}

		public double[] getArrTypeDbl(int[] in) {
			double[] out = new double[in.length];
			for(int i = 0; i < in.length; i++) {
				out[i] = in[i];
			}
			return out;
		}

		 public double[][] getArrValleyPos(int strMode, int strDirection, double[] dsInput, double dSignalBase,int deNoiseWidth,
					int iStart, int iEnd) {

				int iLen = Math.abs(iEnd - iStart) + 1;
				int iCnt = ((iLen)/deNoiseWidth);
				double[][] r;
				double[][] rT = new double[Tag.INT_POS_TAGSIZE][iCnt]; //[0][] = Position, [1][] = Value
				
				int iMode = 0;
				if(Tag.INT_VALLEY == strMode) {
					iMode = 1;
				}else if(Tag.INT_PEAK == strMode){
					iMode = -1;			
				}
				double dMin = Double.MAX_VALUE * iMode;
				int iDir = 0;
				if(Tag.INT_FORWARD == strDirection) {
					iDir = 1;
				}else if(Tag.INT_REWARD == strDirection){
					iDir = -1;			
				}

				
				int iPosCnt = 0;
				double[][] pos = new double[iCnt][Tag.INT_POS_TAGSIZE]; //[0][] = Position, [1][] = Value
				double[] mean = new double[iCnt]; //[][0] = Position, [1][] = Value

				for (int iVar = 0; iVar < iCnt; iVar++) {
					int iS = iStart + iDir * iVar * deNoiseWidth;
						iS = Math.min(iS, dsInput.length - 1);	
						iS = Math.max(iS, 0);
					int iE = iS + iDir * (deNoiseWidth -1);
						iE = Math.min(iE, dsInput.length - 1);
						iE = Math.max(iE, 0);
						
					if(Tag.INT_REWARD == strDirection) {
						int iT = iS;
						iS = iE;
						iE = iT;
					}
					if(Tag.INT_VALLEY == strMode) {
						pos[iVar] = this.getArrMinPos(dsInput, iS,iE);
					}else if(Tag.INT_PEAK == strMode){
						pos[iVar] = this.getArrMaxPos(dsInput, iS,iE);
					}
					mean[iVar] = this.getArrMean(dsInput, iS,iE);
				}
				
				for (int i = 1; i < iCnt-1; i++) {
					double preVal = iMode * mean[i-1];
					double nowVal = iMode * mean[i];
					double aftVal = iMode * mean[i+1];
					boolean bStrength = false;
					if(Tag.INT_VALLEY == strMode) {
						bStrength = pos[i][Tag.INT_POS_VAL] <= dSignalBase;
					}else if(Tag.INT_PEAK == strMode){
						bStrength = pos[i][Tag.INT_POS_VAL] >= dSignalBase;
					}

					if ((preVal >= nowVal) && (aftVal > nowVal) && (bStrength)) {
						rT[Tag.INT_POS_IDX][iPosCnt] = pos[i][Tag.INT_POS_IDX];
						rT[Tag.INT_POS_VAL][iPosCnt] = pos[i][Tag.INT_POS_VAL];
						iPosCnt ++;
					}
				}
				
				r = new double[Tag.INT_POS_TAGSIZE][iPosCnt]; //[0][] = Position, [1][] = Value
				for (int i = 0; i < iPosCnt; i++) {
					//if ((isMin[i-1][1]>=isMin[i][1]) && (isMin[i+1][1]>isMin[i][1])) {
					r[Tag.INT_POS_IDX][i] = rT[Tag.INT_POS_IDX][i];
					r[Tag.INT_POS_VAL][i] = rT[Tag.INT_POS_VAL][i];
				}

				return r;
			}
		
	public int[][] getArrValleyPos(int strMode, int[] dsInput, int deNoiseWidth) {
		double dMin = Double.MAX_VALUE;
		int iLen = dsInput.length;
		int iCnt = (dsInput.length/deNoiseWidth);
		int iPos = 0;
		int[][] isMin = new int[iCnt][Tag.INT_POS_TAGSIZE]; //[0][] = Position, [1][] = Value
		double[] isMean = new double[iCnt]; //[][0] = Position, [1][] = Value
		int[][] isValley = new int[Tag.INT_POS_TAGSIZE][iCnt]; //[0][] = Position, [1][] = Value
		for (int iVar = 0; iVar < iCnt; iVar++) {
			int iS = iVar * deNoiseWidth;
			int iE = iS + deNoiseWidth -1;
			if(iE < dsInput.length) {
				if(Tag.INT_VALLEY == strMode) {
					isMin[iVar] = this.getArrMinPos(dsInput, iS,iE);
				}else if(Tag.INT_PEAK == strMode){
					isMin[iVar] = this.getArrMaxPos(dsInput, iS,iE);
				}
				
				isMean[iVar] = this.getArrMean(dsInput, iS,iE);
			}
		}
		
		int iValley = 0;
		int iMode = 0;
		if(Tag.INT_VALLEY == strMode) {
			iMode = 1;
		}else if(Tag.INT_PEAK == strMode){
			iMode = -1;			
		}
		for (int i = 1; i < iCnt-1; i++) {
			double preVal = iMode * isMean[i-1];
			double nowVal = iMode * isMean[i];
			double aftVal = iMode * isMean[i+1];
			//double preVal = iMode * isMin[i-1][Tag.mIntPOSVALTAG];
			//double nowVal = iMode * isMin[i][Tag.mIntPOSVALTAG];
			//double aftVal = iMode * isMin[i+1][Tag.mIntPOSVALTAG];
			//if ((isMin[i-1][1]>=isMin[i][1]) && (isMin[i+1][1]>isMin[i][1])) {
			if ((preVal >= nowVal) && (aftVal > nowVal)) {
				isValley[Tag.INT_POS_IDX][iValley] = isMin[i][Tag.INT_POS_IDX];
				isValley[Tag.INT_POS_VAL][iValley] = isMin[i][Tag.INT_POS_VAL];
				iValley ++;
			}
		}
		int[][] isValleyResult = new int[Tag.INT_POS_TAGSIZE][iValley]; //[0][] = Position, [1][] = Value
		for (int i = 0; i < iValley; i++) {
			//if ((isMin[i-1][1]>=isMin[i][1]) && (isMin[i+1][1]>isMin[i][1])) {
			isValleyResult[Tag.INT_POS_IDX][i]=isValley[Tag.INT_POS_IDX][i];
			isValleyResult[Tag.INT_POS_VAL][i]=isValley[Tag.INT_POS_VAL][i];
		}

		return isValleyResult;
	}
	
	public void setArrCopyOfRange(double[] dist, int distStart, double [] source, int sourceStart, int length){
		if (dist.length < (distStart + length)) return;
		if (source.length < (sourceStart + length)) return;

		for(int i = 0; i < length; i++) {
			dist[distStart + i] = source[sourceStart + i];
		}
	}

	public int[] getArrCopyOfRange(int[] sInput, int iX1, int iX2){
		iX1 = Math.max(0, iX1);
		iX2 = Math.min(sInput.length - 1, iX2);
		int[] sResult = new int [iX2-iX1+1];
		for(int i = iX1; i <= iX2; i++) {
				sResult[i-iX1] = sInput[i];
		}
		return sResult;
		
	}
	public double[] getArrCopyOfRange(double[] sInput, int iX1, int iX2){
		iX1 = Math.max(0, iX1);
		iX2 = Math.min(sInput.length - 1, iX2);
		double[] sResult = new double [iX2-iX1+1];
		for(int i = iX1; i <= iX2; i++) {
				sResult[i-iX1] = sInput[i];
		}
		return sResult;
		
	}

	public double[] getArrDCtoAC(double[] dIn){
		double mean = this.getArrMean(dIn, 0, dIn.length);
		double[] r = new double [dIn.length];
		for(int i = 0; i < dIn.length; i++) {
				r[i] = dIn[i] - mean;
		}
		return r;
	}

	public int[][] getArrCopyOfRange(int[][] ssInput, int iX1, int iX2, int iY1, int iY2){
		int[][] ssResult = new int [iX2-iX1+1][iY2-iY1+1];
		for(int i = iX1; i <= iX2; i++) {
			for (int j = iY1; j <= iY2; j++) {
				ssResult[i-iX1][j-iY1] = ssInput[i][j];
			}
		}
		return ssResult;
		
	}

	public void forArrCopy(double[] dsO, double[] dsI){
		int xLen = Math.min(dsO.length, dsI.length);
		for(int i = 0; i < xLen; i++) {
			dsO[i] = dsI[i];
		}
	}

	public double[][] getArrCopyOfRange(double[][] dssInput, int iX1, int iX2, int iY1, int iY2){
		double[][] dssResult = new double [iX2-iX1+1][iY2-iY1+1];
		for(int i = iX1; i <= iX2; i++) {
			for (int j = iY1; j <= iY2; j++) {
				dssResult[i-iX1][j-iY1] = dssInput[i][j];
			}
		}
		return dssResult;
		
	}

	public double[] getArrSort(int strMode, double[] dsI){
		if(dsI.length == 0) {
			return dsI;
		}
		int iLen = dsI.length;
		
		double[]isSort = new double[iLen];
		double iMaxIdx;

		int iMode = 0;
		if(Tag.INT_SORT_MAX == strMode) {
			iMode = -1;
		}else if(Tag.INT_SORT_MIN == strMode) {
			iMode = 1;
		}
		
		for (int i = 0; i<iLen; i++) {
				isSort[i] = iMode * Double.MAX_VALUE;
		}

		for (int i = 0; i< iLen; i++) {
			iMaxIdx = dsI[i];

			for (int j = 0; j<=i; j++) {
				if (iMode * iMaxIdx <= iMode * isSort[j]) {
					for (int k = iLen-1; k > j; k--) {
							isSort[k] = isSort[k-1];
					}
						isSort[j] = dsI[i];
					break;
				} 
			}
		}
		
		return isSort;

		
	}

	public int[] getArrSort(int strMode, int[] isInput){
		if(isInput.length == 0) {
			return isInput;
		}
		int iLen = isInput.length;
		
		int[]isSort = new int[iLen];
		int iMaxIdx;

		int iMode = 0;
		if(Tag.INT_SORT_MAX == strMode) {
			iMode = -1;
		}else if(Tag.INT_SORT_MIN == strMode) {
			iMode = 1;
		}
		
		for (int i = 0; i<iLen; i++) {
				isSort[i] = iMode * Integer.MAX_VALUE;
		}

		for (int i = 0; i< iLen; i++) {
			iMaxIdx = isInput[i];

			for (int j = 0; j<=i; j++) {
				if (iMode * iMaxIdx <= iMode * isSort[j]) {
					for (int k = iLen-1; k > j; k--) {
							isSort[k] = isSort[k-1];
					}
						isSort[j] = isInput[i];
					break;
				} 
			}
		}
		
		return isSort;

		
	}

	public static double[][] getArrTransform(double[][] inputArr){
		double [][] output;
		int depth = inputArr[0].length;
		int width = inputArr.length;
		output = new double[depth][width];
		for (int tl = 0; tl < depth; tl++)
		{
			for (int kl = 0; kl < width; kl++)
			{
				output[tl][kl] = inputArr[kl][tl];
			}
		}
		return output;
	}


	public double[][] getArrSort(int strMode, double[][] dssInput, int iIdx){
		if(Tag.INT_POS_TAGSIZE != dssInput.length && dssInput[iIdx].length <=1) {
			return dssInput;
		}
		
		int iLen = dssInput[0].length;
		int iItem = dssInput.length;
		
		double[][]dssSort = new double[iItem][iLen];
		double[] dsMaxIdx = new double[iItem];
		

		int iMode = 0;
		if(Tag.INT_SORT_MAX == strMode) {
			iMode = -1;
		}else if(Tag.INT_SORT_MIN == strMode) {
			iMode = 1;
		}
		
		for (int i = 0; i<iLen; i++) {
			for(int l=0; l <iItem; l++) {
				dssSort[l][i] = iMode * Integer.MAX_VALUE;
			}
		}

		for (int i = 0; i< iLen; i++) {
			for(int l=0; l <iItem; l++) {
				dsMaxIdx[l] = dssInput[l][i];
			}

			for (int j = 0; j<=i; j++) {
				if (iMode * dsMaxIdx[iIdx] <= iMode * dssSort[iIdx][j]) {
					for (int k = iLen-1; k > j; k--) {
						for(int l=0; l <iItem; l++) {
							dssSort[l][k] = dssSort[l][k-1];
						}
					}
					for(int l=0; l <iItem; l++) {
						dssSort[l][j] = dssInput[l][i];
					}
					break;
				} 
			}
		}
		
		return dssSort;
		
	}
	

	public double[] getSimilarValue(int[] iArr, double dRange) {
		double[] r = {0.0, 0.0};
		int l = iArr.length;
		int[] arr = Arrays.copyOf(iArr, iArr.length);
		//int[] rangeCnt = new int[iArr.length];
		int cnt;
		int cntMax = 0;
		int cntMaxIdx = 0;
		Arrays.sort(arr);
		for (int i = 0 ; i<l; i++) {
			if(arr[i] == 0) {continue;}
			cnt = 0;
			for(int j=i; j<l; j++) {
				double dif = Math.abs(arr[i]-arr[j]);
				dif = dif / (double) arr[i];
				if( dif <= dRange) {
					cnt ++;
				}
				if (cnt >= cntMax) {
					cntMaxIdx = i;
					cntMax = cnt;
				}
			}
		}
		int sum=0;
		for (int i = cntMaxIdx ; i<(cntMaxIdx+cntMax); i++) {
			sum += arr[i];
		}		
		r[0] = (double)sum / (double)cntMax;
		r[1] = (double)sum ;
		return r;
	}
	
	public double getArrSum(double[] arrI) {
		// TODO Auto-generated method stub
		double r = 0;
		for (double v : arrI) {
			r += v;
		}
		return r;
	}

	public double getArrSum(double[][] imgI) {
		// TODO Auto-generated method stub
		double r = 0;
		for (int i = 0; i < imgI.length; i++) {
			for (int j = 0; j < imgI[0].length; j++) {
				r += imgI[i][j];
			}
		}
		return r;
	}

	public double get_SpectrumStrengthRange(double[][] imgIn, int iLow, int iHigh, double rS, double rE) {
		double r;
		int pS = (int) (imgIn.length * rS);
		int pE = (int) (imgIn.length * rE);
		pE = Math.min(pE, imgIn.length - 1);
		double[] s = new double[imgIn.length];

		for (int i = 0; i < imgIn.length; i++) {
			s[i] = getArrMean(imgIn[i], iLow, iHigh);
		}

		Arrays.sort(s);

		r = getArrMean(s, pS, pE);
		return r;
	}


	public double[] getArrVelocity(double[] dsEnv, double[] dsTable) {
		double[] ds = new double[dsEnv.length];
		for (int i = 0; i < dsEnv.length; i++) {
			int iIdx = Math.min(dsTable.length - 1, (int) dsEnv[i]);
			iIdx = Math.max(0, iIdx);
			ds[i] = dsTable[iIdx];
		}
		return ds;
	}
	public double getVelocity(double[] dsTable, double i) {
		double r;
		int iIdx = Math.min(dsTable.length - 1, (int) i);
		iIdx = Math.max(0, iIdx);
		r = dsTable[iIdx];
		return r;
	}

	public double[] getVelocityCalculateTable() {
		double[] dT = new double[129];

//		double dScale = (s.DOUBLE_ULTRASOUND_SPEED_FOR_BODY_METER_PERSEC /* / 2 */)
//				/ (s.DOUBLE_ULTRASOUND_SENSOR_WAVE_FREQ);
		double dScale = 1540 / 2500000.0;
//		double dFr = ((double) s.mIntUltrasoundSamplerate / 2.0) / ((double) (s.mIntSubSeqFreqCnt - 1));
		double dFr = ((double) 8000 / 2.0) / ((double) (129 - 1));

		for (int i = 0; i < dT.length; i++) {
			dT[i] = i * dScale * dFr;
		}

		return dT;
	}

	public static double getArraySD(double[] numArray){
		double sum = 0.0;
		double standardDeviation = 0.0;
		int length = numArray.length;

		for (double num : numArray){
			sum += num;
		}

		double mean = sum/length;

		for (double num:numArray){
			standardDeviation += Math.pow(num - mean,2);
		}
		return Math.sqrt(standardDeviation/length);
	}

	public static double getArraySD(int[] numArray){
		double sum = 0.0;
		double standardDeviation = 0.0;
		int length = numArray.length;

		for (double num : numArray){
			sum += num;
		}

		double mean = sum/length;

		for (double num:numArray){
			standardDeviation += Math.pow(num - mean,2);
		}
		return Math.sqrt(standardDeviation/length);
	}

}
