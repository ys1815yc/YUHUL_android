package com.gis.heartio.SignalProcessSubsysII.utilities;
public class Type {

	public static int[] toInt(short[] input, int iStart, int iEnd) {
		short[] src = input;
		int[] dest = new int[input.length];
		for (int i=iStart; i<=iEnd; i++) {
			dest[i] = (int)src[i];
		}
		return dest;
	}

	public static int[] toInt(double[] in) {
		int[] r = new int[in.length];
		for (int i=0; i< in.length; i++) {
			r[i] = (int) in[i];
		}
		return r;
	}

	public static double[] toDbl(int[] input, int iStart, int iEnd) {
		int[] src = input;
		double[] dest = new double[iEnd - iStart +1];
		for (int i=0; i< dest.length; i++) {
			dest[i] = src[i];
		}
		return dest;
	}

	public static double[] toDbl(short[] input, int iStart, int iEnd) {
		short[] src = input;
		double[] dest = new double[iEnd - iStart +1];
		for (int i=0; i< dest.length; i++) {
			dest[i] = src[i];
		}
		return dest;
	}
	
	public static double[] toDbl(int[] input) {
		int[] src = input;
		double[] dest = new double[input.length];
		for (int i=0; i<input.length; i++) {
			dest[i] = src[i];
		}
		return dest;
	}
}
