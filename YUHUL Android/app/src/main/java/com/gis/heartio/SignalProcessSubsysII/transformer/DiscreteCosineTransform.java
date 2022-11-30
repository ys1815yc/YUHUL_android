package com.gis.heartio.SignalProcessSubsysII.transformer;
public class DiscreteCosineTransform {
	
	/* Computes the discrete cosine transform (one-dimension)
	 * http://c.csie.org/~itct/slide/DCT_larry.pdf
	 * fDCT = X(m) = u(m)sqr(2/N)*Sigma[i=0:N-1](x(i)cos((2i+1)m*pi/2N), for m = 0,1,...N-1,
	 * 
	 * iDCT = x(i) = sqr(2/N)*sigma[m=0:N-1](u(m)X(m)cos((2i+1)m*pi/2N)
	 * u(m) = 1, m=0; 1/sqr(sqr(2)), others
	 * 
	 */

	/* Computes the discrete cosine transform
	 * u(m) = 1, m=0; 1/sqr(sqr(2)), others
	 */
	 public final double[] dCoefficients(double[] c) 
	 {
	    final int N = c.length;
	    final double value = 1/Math.sqrt(2.0);

	    for (int i=1; i<N; i++) 
	    {
	            c[i]=value;
	    }
	    c[0] = 1;
	    return c;
	}

	/* Computes the discrete cosine transform
	 * fDCT = X(m) = u(m)sqr(2/N)*Sigma[i=0:N-1](x(i)cos((2i+1)m*pi/2N), for m = 0,1,...N-1,
	 */
	public double[] fDCT(double[] x, int iType) 
	{
	    final int iN = x.length;
	    final double dN = iN;
	    final double dSqr2oN = Math.sqrt(2/dN);
	    final double dPIo2N = Math.PI/(2*dN);

	    double[] u = new double[iN];
	    u = dCoefficients(u);

	    double[] X = new double[iN];

	    for (int m=0; m<iN; m++) 
	    {
    		double dTemp_m = m * dPIo2N;
	        double dSum = 0.0;
	    	for (int i=0; i<iN; i++) 
		    {
	    		dSum += x[i] * Math.cos((2*i+1) * dTemp_m);
		    }
            X[m] = u[m] * dSqr2oN * dSum;
            if(iType==1) {
            	X[m] = Math.abs(X[m]);
            }
            //System.out.println(X[m]);
	    }
	    return X;
	}

	/* 
	 * Computes the inverse discrete cosine transform
	 * iDCT = x(i) = sqr(2/N)*sigma[m=0:N-1](u(m)X(m)cos((2i+1)m*pi/2N)
	 */
	public double[] iDCT(double[] X) 
	{
	    final int iN = X.length;
	    final double dN = iN;
	    final double dSqr2oN = Math.sqrt(2/dN);
	    final double dPIo2N = Math.PI/(2*dN);

	    double[] u = new double[iN];
	    u = dCoefficients(u);

	    double[] x = new double[iN];

	    for (int i=0; i<iN; i++) 
	    {
    		double dTemp_i = (2*i+1) * dPIo2N;
	        double dSum = 0.0;
	    	for (int m=0; m<iN; m++) 
		    {
	    		dSum += u[m] * X[m] * Math.cos(m * dTemp_i);
		    }
            x[i] = dSqr2oN * dSum;
	    }
	    return x;
	}
}
