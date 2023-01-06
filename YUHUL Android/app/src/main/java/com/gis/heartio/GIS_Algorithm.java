package com.gis.heartio;

import com.gis.heartio.SignalProcessSubsysII.parameters.BloodVelocityConfig;
import com.gis.heartio.UIOperationControlSubsystem.MainActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GIS_Algorithm {
    //private final static String TAG = GIS_Algorithm.class.getSimpleName();
    private final static int ALL_SET_IN_SEG = 129;
    public final static  double ONE_SEGMENT_WITH_N_HZ = 4000.0 / (double) ALL_SET_IN_SEG;
    private final static int INVALID_FREQUENCY = 19;
    public final static int PKS_ARRAY = 0;
    public final static int LOC_ARRAY = 1;
    private final static int PKS_AND_LOC_ARRAY_NUM = LOC_ARRAY + 1;

    private enum WaveCondition{
        FIND_TURNING_POINT,
        GOT_TURNING_POINT,
        FIND_ABOVE_TWO_TURNING_POINT,
        SIM_VALID_POINT
    }

    private static class SnsiVf3Result{
        public List<Double> vf;
        public List<Double> xx;

        public SnsiVf3Result(){
            vf = new ArrayList<>();
            xx = new ArrayList<>();
        }
    }

    private static class SnsiFmax2Result{
        public double vf2_SNSI;
        public double PSNR;
    }

    private static class Gm3Result {
        public int GMX;
        public double PSNR;
        public int SE;
        public int NS;
        public double PCX;
    }

    private static class fpkResult{
        public double[] FPKX;
        public double[] FPKY;
        public fpkResult(int length){
            FPKX = new double[length];
            FPKY = new double[length];
        }
    }

    //For GIS subroutine---------------------------------------------------
    public static double Doppler_angle(double[] fmaxArray, double[][] spectrum ){
        ArrayList<List<Double>> resultArrayList = MeasurementByBW(fmaxArray,spectrum,1);
        double[] STFT_Out = resultArrayList.get(0).stream().mapToDouble(i -> i).toArray();
        double[] mean_BW = resultArrayList.get(1).stream().mapToDouble(i -> i).toArray();
        int size = STFT_Out.length;
        int[] degree = new int[size];

        double[] STD_angle = {42, 52, 62, 72};
        for(int j = 0; j < size ; j++){
            double[] bwDivideFmax = cross_point(STFT_Out[j] * ONE_SEGMENT_WITH_N_HZ);
            degree[j] = (int) Math.round(Interpolation(bwDivideFmax, STD_angle, mean_BW[j]));
        }

        return findAvgAngleByVote(degree, 5);
    }

    private static double[] cross_point(double fmax_value){
        double[] bwDivideFmax = new double[4];
        Arrays.fill(bwDivideFmax, 0);
        bwDivideFmax[0] = 0.6479 * Math.exp(-0.002268 * fmax_value) + 0.1099 * Math.exp(-0.000152 * fmax_value);
        bwDivideFmax[1] = 0.7782 * Math.exp(-0.002152 * fmax_value) + 0.09131 * Math.exp(0.00007828 * fmax_value);
        bwDivideFmax[2] = 0.7532 * Math.exp(-0.002485 * fmax_value) + 0.1351 * Math.exp(0.0001182 * fmax_value);
        bwDivideFmax[3] = 0.504 * Math.exp(-0.001811 * fmax_value) + 0.1563 * Math.exp(0.0001374 * fmax_value);
        return bwDivideFmax;
    }

    //old function name: Mapping
    private static double Interpolation(double[] period, double[] targetValue, double inputValue){
        double result = 0;
        int interStart, interEnd;
        int periodLength = period.length;
        if(inputValue <= period[0]){
            result = period[0];
        }

        if(inputValue >= period[periodLength-1]){
            result = period[periodLength-1];
        }

        if((inputValue < period[periodLength-1]) && (inputValue > period[0])){
            interStart = interEnd = 0;
            for(int i = 0 ; i < periodLength-1 ; i++){
                if((period[i] < inputValue ) && (period[i+1] > inputValue)){
                    interStart = i;
                    interEnd = i + 1;
                }
            }
            result=((targetValue[interEnd] - targetValue[interStart]) * (inputValue - period[interStart])/
                    (period[interEnd] - period[interStart])) + targetValue[interStart];
        }

        return result;
    }

    //old function name: max_vote
    @SuppressWarnings("SameParameterValue")
    private static double findAvgAngleByVote(int[] values, int percentage){
        int maxCount = 0;
        double meanValue = 0;

        for (int value : values) {
            int count = 0;
            double sum = 0;
            for (int i : values) {
                if (value != 0) {
                    if (Math.abs(value - i) <= (value * (percentage / 100.0))) {
                        count++;
                        sum = sum + i;
                    }
                } else {
                    sum = 0;
                }
            }

            if (count > maxCount) {
                maxCount = count;
                meanValue = sum / (double) count;
            }
        }

        return meanValue;
    }

    //old function name: find_top_n_pk,uncheck
    @SuppressWarnings("SameParameterValue")
    private static double[][] findValidPeaks(double[] sigSpectrum, int pkNum, double fmax){
        /*matlab宣告的變數*/
        double[][] findPeakResult = FindPeaks(sigSpectrum); //[0]存pks；[1]存locs
        double[] pksSortH2L = findPeakResult[PKS_ARRAY].clone();
        double[] fSortH2L = new double[findPeakResult[LOC_ARRAY].length];

        /*java排序(降冪)*/
        Arrays.sort(pksSortH2L);
        pksSortH2L = reverse(pksSortH2L);

        /*index排序*/
        for(int i = 0 ; i < pksSortH2L.length ; i++){
            int getIndex = findIndex(findPeakResult[PKS_ARRAY], pksSortH2L[i]);
            fSortH2L[i] = findPeakResult[LOC_ARRAY][getIndex]; //取出存入的index locations
        }

        int count = 0;

        List<Double> pksValueTemp = new ArrayList<>();
        List<Double> locValueTemp = new ArrayList<>();
        boolean flag = true;
        int pksCount = 0;

        while(flag && (count < pkNum)) {
            if (pksCount < findPeakResult[LOC_ARRAY].length) {
                if (!(fSortH2L[pksCount] < INVALID_FREQUENCY || (fmax - fSortH2L[pksCount]) <= 1)) {
                    pksValueTemp.add(pksSortH2L[pksCount]);
                    locValueTemp.add(fSortH2L[pksCount]);
                    count++;
                }
                pksCount++;
            } else if (count == 0) {
                flag = false;
            } else {
                count++;
            }
        }
        double[][] output = new double[PKS_AND_LOC_ARRAY_NUM][locValueTemp.size()]; //[0]存npks；[1]存locations
        if(count != 0){
            output[PKS_ARRAY] = pksValueTemp.stream().mapToDouble(i -> i).toArray();
            output[LOC_ARRAY] = locValueTemp.stream().mapToDouble(i -> i).toArray();
        }
        return output;
    }

    //old function name: Measurement_BW
    public static ArrayList<List<Double>> MeasurementByBW(double[] fmaxArray, double[][] spectrum, int findPeakNum){
        List<Integer> tempCnt_idx = new ArrayList<>();
        List<Double> STFT_Out = new ArrayList<>();
        List<Double> mean_BW_Fmax = new ArrayList<>();
        ArrayList<List<Double>> result = new ArrayList<>();


        for (int time = 0; time < fmaxArray.length; time++) {
            int fmaxRight = 0;
            int fmaxLeft = 0;
            List<Integer> tempBW = new ArrayList<>();

            double[] singleSetSpectrum = getFromSpectrum(spectrum, time);
            double[][] peaks_locs = findValidPeaks(singleSetSpectrum, 4, fmaxArray[time]);

            if(fmaxArray[time] != 0 && peaks_locs[PKS_ARRAY].length != 0){
                for(int peaksCount = 0; peaksCount < peaks_locs[PKS_ARRAY].length; peaksCount++){
                    int fLoc = (int)peaks_locs[LOC_ARRAY][peaksCount];
                    double fpeak = peaks_locs[PKS_ARRAY][peaksCount];
                    int fr1 = fLoc + 1;
                    int fl1 = fLoc - 1;
                    boolean findBWCounterflag = true;
                    int findSimCount = 1;

                    WaveCondition waveCondition = WaveCondition.FIND_TURNING_POINT;
                    boolean findPeakOverlapFlag = true;

                    //find right valid point
                    while (findBWCounterflag && fLoc < (ALL_SET_IN_SEG - 2)){
                        if(getFromSpectrum(spectrum, time)[fr1] > (fpeak * 0.05) && fr1 < (ALL_SET_IN_SEG - 1)){
                            switch(waveCondition){
                                case FIND_TURNING_POINT:
                                    if(singleSetSpectrum[fr1] < singleSetSpectrum[fr1 + 1]){
                                        if(findPeakOverlapFlag && singleSetSpectrum[fr1] >= fpeak * 0.5){
                                            waveCondition = WaveCondition.GOT_TURNING_POINT;
                                        } else {
                                            if(singleSetSpectrum[fr1]  > fpeak * 0.1) {
                                                if (findPeakNum == 2)
                                                    waveCondition = WaveCondition.FIND_ABOVE_TWO_TURNING_POINT;
                                                else
                                                    waveCondition = WaveCondition.SIM_VALID_POINT;
                                            } else {
                                                fr1++;
                                                findBWCounterflag = false;
                                            }
                                        }
                                    } else {
                                        fr1++;
                                    }
                                break;

                                case GOT_TURNING_POINT:
                                    if(!(singleSetSpectrum[fr1] < singleSetSpectrum[fr1 + 1])) {
                                        waveCondition = WaveCondition.FIND_TURNING_POINT;
                                        findPeakOverlapFlag = false;
                                    }
                                    fr1++;
                                break;

                                case FIND_ABOVE_TWO_TURNING_POINT:
                                    if(singleSetSpectrum[fr1] < singleSetSpectrum[fr1 + 1]) {
                                        fr1++;
                                        if (findSimCount >= 3) {
                                            fr1 = fr1 - 3;
                                            double diffSpectrumPoint = singleSetSpectrum[fr1] - singleSetSpectrum[fr1 - 1];
                                            int step = 1;
                                            while (singleSetSpectrum[fr1] + step * diffSpectrumPoint >= 0 && step < 10) {
                                                step++;
                                            }
                                            step = (step > 4) ? 0 : step;
                                            fr1 = fr1 + step;
                                            findBWCounterflag = false;
                                        } else {
                                            findSimCount++;
                                        }
                                    }else {
                                        waveCondition = WaveCondition.SIM_VALID_POINT;
                                    }
                                break;

                                case SIM_VALID_POINT:
                                    if(singleSetSpectrum[fr1] > singleSetSpectrum[fr1 + 1]) {
                                        fr1++;
                                    } else{
                                        double diffSpectrumPoint = singleSetSpectrum[fr1] - singleSetSpectrum[fr1 - 1];
                                        int step = 1;
                                        while (singleSetSpectrum[fr1] + step * diffSpectrumPoint >= 0 && step < 10) {
                                            step++;
                                        }
                                        step = (step > 4) ? 0 : step;
                                        fr1 = fr1 + step;
                                        findBWCounterflag = false;
                                    }
                                break;
                            }
                        } else {
                            findBWCounterflag = false;
                        }
                    }

                    //find left valid point
                    findBWCounterflag = true;
                    waveCondition = WaveCondition.FIND_TURNING_POINT;
                    findPeakOverlapFlag = true;
                    findSimCount = 1;

                    while (findBWCounterflag){
                        if(getFromSpectrum(spectrum, time)[fl1] > (fpeak * 0.05) && fl1 > 1){
                            switch(waveCondition){
                                case FIND_TURNING_POINT:
                                    if(singleSetSpectrum[fl1] < singleSetSpectrum[fl1 - 1]){
                                        if(findPeakOverlapFlag && singleSetSpectrum[fl1] >= fpeak * 0.5){
                                            waveCondition = WaveCondition.GOT_TURNING_POINT;
                                        } else {
                                            if(singleSetSpectrum[fl1]  > fpeak * 0.1) {
                                                if (findPeakNum == 2)
                                                    waveCondition = WaveCondition.FIND_ABOVE_TWO_TURNING_POINT;
                                                else
                                                    waveCondition = WaveCondition.SIM_VALID_POINT;
                                            } else {
                                                fl1--;
                                                findBWCounterflag = false;
                                            }
                                        }
                                    } else {
                                        fl1--;
                                    }
                                    break;

                                case GOT_TURNING_POINT:
                                    if(!(singleSetSpectrum[fl1] < singleSetSpectrum[fl1 - 1])) {
                                        waveCondition = WaveCondition.FIND_TURNING_POINT;
                                        findPeakOverlapFlag = false;
                                    }
                                    fl1--;
                                    break;

                                case FIND_ABOVE_TWO_TURNING_POINT:
                                    if(singleSetSpectrum[fl1] < singleSetSpectrum[fl1 - 1]) {
                                        fl1--;
                                        if (findSimCount >= 3) {
                                            fl1 = fl1 + 3;
                                            double diffSpectrumPoint = singleSetSpectrum[fl1] - singleSetSpectrum[fl1 + 1];
                                            int step = 1;
                                            while (singleSetSpectrum[fl1] + step * diffSpectrumPoint >= 0 && step < 10) {
                                                step++;
                                            }
                                            step = (step > 4) ? 0 : step;
                                            fl1 = fl1 - step;
                                            findBWCounterflag = false;
                                        } else {
                                            findSimCount++;
                                        }
                                    }else {
                                        waveCondition = WaveCondition.SIM_VALID_POINT;
                                    }
                                    break;

                                case SIM_VALID_POINT:
                                    if(singleSetSpectrum[fl1] > singleSetSpectrum[fl1 - 1]) {
                                        fl1--;
                                    } else{
                                        double diffSpectrumPoint = singleSetSpectrum[fl1] - singleSetSpectrum[fl1 + 1];
                                        int step = 1;
                                        while (singleSetSpectrum[fl1] + step * diffSpectrumPoint >= 0 && step < 10) {
                                            step++;
                                        }
                                        step = (step > 4) ? 0 : step;
                                        fl1 = fl1 - step;
                                        findBWCounterflag = false;
                                    }
                                    break;
                            }
                        } else {
                            findBWCounterflag = false;
                        }
                    }

                    if (!(fr1 == fmaxRight && fl1 == fmaxLeft)){
                        fmaxRight = fr1;
                        fmaxLeft = fl1;
                        tempBW.add(fmaxRight - fmaxLeft);
                    }
                }
                tempCnt_idx.add(time);
                mean_BW_Fmax.add(mean(tempBW.stream().mapToInt(i -> i).toArray()) /
                        fmaxArray[time]);
            }
        }

        int[] cnt_idx = tempCnt_idx.stream().mapToInt(i -> i).toArray();
        for (int cntIdx : cnt_idx) {
            STFT_Out.add(fmaxArray[cntIdx]);
        }

        result.add(STFT_Out);
        result.add(mean_BW_Fmax);
        return result;
    }

    private static double[][] FindPeaks(double[] x){
        int N = x.length;
        double[][] peaksAndLocations = new double[PKS_AND_LOC_ARRAY_NUM][N];

        int j = 0; // counter for peaks and locations

        // case for when data has only one element, it's a peak by default
        if(N == 1){
            peaksAndLocations[PKS_ARRAY][j] = x[0]; //peak
            peaksAndLocations[LOC_ARRAY][j] = 0; // location
        }else{
            // case when the peaks lie in the middle of the array data
            for(int i = 1; i < N-1; i++){
                if((x[i] >= x[i - 1]) && (x[i] > x[i + 1])){ // compare element with next element before and after
                    peaksAndLocations[PKS_ARRAY][j] = x[i]; // peak
                    peaksAndLocations[LOC_ARRAY][j] = i; // location
                    j++;
                }
            }

            // check if last element is a peak
            if(x[N-1] > x[N-2]){
                peaksAndLocations[PKS_ARRAY][j] = x[N - 1]; // peak
                peaksAndLocations[LOC_ARRAY][j] = N - 1; // location
                j++;
            }
        }

        // trimming the extra zeros in the peaksAndLocations array
        int numberOfPeaks = j;
        double[][] actualPeaksAndLocations = new double[PKS_AND_LOC_ARRAY_NUM][numberOfPeaks]; // sized to the actual number of peaks and/or locations

        for(int i = 0; i < numberOfPeaks; i++){ // up to numberOfPeaks to ignore extra zeros
            actualPeaksAndLocations[PKS_ARRAY][i] = peaksAndLocations[PKS_ARRAY][i];
            actualPeaksAndLocations[LOC_ARRAY][i] = peaksAndLocations[LOC_ARRAY][i];
        }
        return actualPeaksAndLocations;
    }

    private static int findIndex(double[] arr, double t){

        // if array is Null
        if (arr == null){
            return -1;
        }
        // find length of array
        int len = arr.length;
        int i = 0;

        // traverse in the array
        while (i < len){
            // if the i-th element is t
            // then return the index
            if (arr[i] == t){
                return i;
            }
            else{
                i++;
            }
        }
        return -1;
    }

    private static double[] reverse(double[] arr){
        int num = arr.length;
        double[] y = new double[num];

        for (double v : arr) {
            y[num - 1] = v;
            num = num - 1;
        }
        return y;
    }

    private static double mean(int[] data){
        double sum = 0;
        for(int temp : data){
            sum += temp;
        }
        return (sum / data.length);
    }

    //For common subroutine---------------------------------------------------
    private static double[] getFromSpectrum(double[][] spectrum, int set){
        double[] result = new double[spectrum.length];

        for(int i = 0 ; i < spectrum.length ; i++){
            result[i] = spectrum[i][set];
        }
        return result;
    }

    //For find Fmax from ITRI-------------------------------------------------
    private static double mean(double[] data){
        double sum = 0;
        for(double temp : data){
            sum += temp;
        }
        return (sum / data.length);
    }

    private static double maxFrom1DArray(double[] in){
        double result = in[0];
        for (double temp : in){
                result = Math.max(temp, result);
        }
        return result;
    }

    private static double maxFrom2DArray(double[][] in){
        double result = in[0][0];
        for (double[] temp1 : in) {
            for (double temp2 : temp1){
                result = Math.max(temp2, result);
            }
        }
        return result;
    }

    private static double minFrom2DArray(double[][] in){
        double result = in[0][0];
        for (double[] temp1 : in) {
            for (double temp2 : temp1){
                result = Math.min(temp2, result);
            }
        }
        return result;
    }

    private static double[][] normal(double[][] in){
        double max = maxFrom2DArray(in);
        double min = minFrom2DArray(in);
        double[][] result = new double[in.length][in[0].length];
        for(int i = 0 ; i < in.length ; i++){
            for(int j = 0 ; j < in[i].length ; j++){
                result[i][j] = 255 * (((1 / (max - min)) * (in[i][j] - min)));
            }
        }
        return result;
    }

    private static double[][] FFT2(double[] x, double[][] RE_M, double[][] IM_M){
        int n = x.length;  // assume n is a power of 2
        double[][] result;
        result = new double[n][2];
        double  nu, sum_re, sum_im;

        for (int k = 0 ; k < n ; k++)
        {
            sum_re = 0;
            sum_im = 0;
            for (int i = 0; i < n; i++)
            {
                nu = x[i];
                sum_re = sum_re + RE_M[k][i] * nu;
                sum_im = sum_im + IM_M[k][i] * nu;
            }
            result[k][0] = sum_re;
            result[k][1] = sum_im;
        }
        return result ;
    }

    private static double[] Hamming( int Mn) {
        double[] w;
        w = new double[Mn];
        double temp;
        for(int i = 0 ; i < Mn ; i++)
        {
            temp = 2 * Math.PI * i / (Mn - 1);
            w[i] = 0.54 - 0.46 * Math.cos(temp);
        }
        return w;
    }

    @SuppressWarnings("SameParameterValue")
    private static double[][] Spectrogram(double[] x, int w_size, int overlap) {
        double[] hammingSignal = new double[w_size];
        double[][] re_M = new double[w_size][w_size];
        double[][] im_M = new double[w_size][w_size];
        double[] w = Hamming(w_size);
        int resultSize = (w_size / 2) + 1;
        int stepNum = (w_size - overlap);
        int frameNum = (x.length - overlap) / stepNum;
        double[][] result = new double[resultSize][frameNum];

        double factor;

        double windowPowSum = 0;
        for(double tempw : w){
            windowPowSum += Math.pow(tempw,2);
        }

        factor = (double)BloodVelocityConfig.INTEGER_ULTRASOUND_SAMPLERATE * windowPowSum;

        for (int k = 0; k < w_size; k++) {
            for (int i = 0; i < w_size; i++) {
                double arg = -2.0 *  Math.PI * k * i / (double)w_size;
                re_M[k][i] = Math.cos (arg);
                im_M[k][i] = Math.sin (arg);
            }
        }

        for(int j = 0 ; j < frameNum ; j++) {
            int stepAdd = stepNum * j;

            for(int i = 0 ; i < w_size ; i++){
                hammingSignal[i] = x[i + stepAdd] * w[i];
            }

            double[][] fft_bf = FFT2(hammingSignal, re_M, im_M);
            result[0][j] = (Math.pow(fft_bf[0][0],2) + Math.pow(fft_bf[0][1],2)) / factor;
            for(int jj = 1 ; jj < resultSize - 1 ; jj++) {
                result[jj][j] = 2.0 * (Math.pow(fft_bf[jj][0],2) + Math.pow(fft_bf[jj][1],2)) / factor;
            }
            result[resultSize - 1][j] = (Math.pow(fft_bf[resultSize - 1][0],2) + Math.pow(fft_bf[resultSize - 1][1],2)) / factor;
        }
        return result;
    }

    //old function name: fpk
    private static fpkResult findPeaksUsed5Points(double[] in_vf){
        int in_thp = 5;
        fpkResult rtn;
        double[] FL_S = new double[in_thp];
        double[] FR_S = new double[in_thp];
        int fpkn = 0;
        int lng = in_vf.length;
        double[] FPK_y = new double[lng];
        double[] FPK_x = new double[lng];
        int ffpi2 = 0;
        int fpkn2 = 0;

        for (int ffpi = (in_thp - 1) ; ffpi < (lng - in_thp) ; ffpi++){
            System.arraycopy(in_vf, (ffpi - (in_thp - 1)), FL_S, 0, in_thp);
            System.arraycopy(in_vf, ffpi, FR_S, 0, in_thp);
            if (maxFrom1DArray(FL_S) == in_vf[ffpi] && maxFrom1DArray(FR_S) == in_vf[ffpi]){
                    FPK_y[fpkn] = in_vf[ffpi];
                    FPK_x[fpkn] = ffpi;
                    fpkn += 1;
            }
        }

        rtn = new fpkResult(fpkn);

        while(ffpi2 < fpkn-1){
            if (FPK_y[ffpi2] != FPK_y[ffpi2+1]){
                rtn.FPKY[fpkn2] = FPK_y[ffpi2];
                rtn.FPKX[fpkn2] = FPK_x[ffpi2];
                fpkn2 += 1;
            }
            ffpi2 += 1;
        }

        if (fpkn >=1){
            fpkn2 += 1;
            rtn.FPKY[fpkn2-1] = FPK_y[fpkn-1];
            rtn.FPKX[fpkn2-1] = FPK_x[fpkn-1];
        }else{
            rtn.FPKY[0] = 0;
            rtn.FPKX[0] = 0;
        }

        return rtn;
    }

    private static Gm3Result GM3(double[] p_in) {
        Gm3Result gm3Result = new Gm3Result();
        double mv = 0;
        double p99 = ALL_SET_IN_SEG;
        double tm = sum(p_in);
        double TP1 = tm / p99;
        double[] p_perc = new double[p_in.length];
        double temp;

        //findpeaks
        fpkResult peaks = findPeaksUsed5Points(p_in);

        //find max peak value and index
        for (int i = 0 ; i < peaks.FPKY.length ; i++){
            if(peaks.FPKY[i] > mv){
                mv = peaks.FPKY[i];
                gm3Result.PCX = peaks.FPKX[i];
                gm3Result.SE = (int)gm3Result.PCX;
            }
        }

        //find frequency on 0.7 * Vpeak and define to SE
        while(p_in[gm3Result.SE] > (0.7 * mv)){
            gm3Result.SE++;
        }

        //IPC with normalize
        temp = 0;
        for (int i = 0 ; i < p_in.length ; i++){
            temp = temp + p_in[i];
            p_perc[i] = (temp / tm) * 100.0;//normalize
        }

        //find max distance between line and point side of it
        int gsx = (int)gm3Result.PCX;
        double gsy = p_perc[(int)gm3Result.PCX];
        double dfx0 = p99-gsx-1;
        double dfy0 = 100-gsy;
        double v1_lng = Math.pow((Math.pow(dfx0, 2) + Math.pow(dfy0, 2)), 0.5);

        double[] v_dis = new double[(int)(p99 - gsx - 1)];
        for (int i = 0 ; i < (p99 - gsx - 1) ; i++){
            double dfy1 = p_perc[gsx + i + 1] - gsy;
            double dfx1 = i + 1;
            double v2_lng = Math.pow((Math.pow(dfx1, 2) + Math.pow(dfy1, 2)), 0.5);
            double v1dv2 = dfx0 * dfx1 + dfy0 * dfy1;
            double cos_v = v1dv2 / (v1_lng * v2_lng);
            double sin_v = Math.pow((1 - Math.pow(cos_v, 2)), 0.5);
            v_dis[i] = v2_lng * sin_v;
        }
        temp = v_dis[0];
        gm3Result.GMX = (gsx + 1);
        for (int i = 0 ; i < v_dis.length ; i++){
            if(v_dis[i] > temp){
                temp = v_dis[i];
                gm3Result.GMX = i + (gsx + 1);
            }
        }

        //find NS
        gm3Result.NS = gm3Result.GMX + gm3Result.GMX - gm3Result.SE;
        gm3Result.NS = gm3Result.NS >= p99 ? 99 : gm3Result.NS;


        //find PSNR
        temp = 0;
        for(int i = gm3Result.NS ; i < p99 ; i++){
            temp += p_in[i];
        }
        double TP2 = temp / (p99 - gm3Result.NS - 1);
        gm3Result.PSNR = (TP1 - TP2) / TP2;
        return gm3Result;
    }

    private static double Mapping(double[] XM, double[] YM, double X_in){
        int dlng = XM.length;
        int si = 0, ei = 0;

        if(X_in <= XM[0]) {
            return XM[0];
        }else if(X_in >= XM[dlng - 1]) {
            return XM[dlng - 1];
        }else{
            for(int i = 0 ; i < dlng - 1 ; i++){
                if ((XM[i] < X_in) && (XM[i+1] > X_in)){
                    si=i;
                    ei=i+1;
                }
            }
            return ((YM[ei]-YM[si])*(X_in-XM[si])/(XM[ei]-XM[si]))+YM[si];
        }
    }

    private static SnsiFmax2Result SNSI_fmax2(double[] data1){
        SnsiFmax2Result rtn = new SnsiFmax2Result();
        Gm3Result gm3Result = GM3(data1);
        double t_power2 = sum(data1);
        double[] T_data = new double[data1.length];
        double[] IPC_data = new double[data1.length];
        double[] IPC_slope = new double[data1.length];

        rtn.PSNR = gm3Result.PSNR;
        T_data[0] = data1[0];
        for (int i = 1 ; i < data1.length ; i++){
            T_data[i] = T_data[i-1] + data1[i];
        }

        for(int i = 0 ; i < T_data.length ; i++){
            IPC_data[i] = T_data[i] / t_power2;
        }

        Arrays.fill(IPC_slope, 0);
        for (int i = 1 ; i < data1.length - 1 ; i++){
            IPC_slope[i] = (IPC_data[i + 1] - IPC_data[i-1]) / 2;
        }

        double Ms = IPC_slope[(int)gm3Result.PCX];
        double Mn = (1 - IPC_data[gm3Result.NS]) / (ALL_SET_IN_SEG - 1 - gm3Result.NS);

        double[] YYI = {0.002, 0.006, 0.05, 0.14, 0.14};
        double[] XXI = {0, 13.3, 16.05, 19.7, 50};
        double xx = Mapping(XXI, YYI, (gm3Result.GMX - gm3Result.SE));
        double Msn2 = xx * Ms + (1 - xx) * Mn;

        int fmax1 = (int)Math.floor(gm3Result.NS + 0.5);
        double ks = (IPC_slope[fmax1]);
        while((ks < Msn2) && (fmax1 > 1)){
            ks = (IPC_slope[fmax1]);
            fmax1 = fmax1 - 1;
        }
        if (fmax1 > gm3Result.GMX && fmax1 <= gm3Result.NS){
            rtn.vf2_SNSI = fmax1;
        } else if(fmax1 <= gm3Result.GMX){
            rtn.vf2_SNSI = gm3Result.GMX;
        } else {
            rtn.vf2_SNSI = gm3Result.NS;
        }
        return rtn;
    }

    private static double sum(double[] arr) {
        double sum = 0; // initialize sum
        for (double element : arr) {
            sum += element;
        }
        return sum;
    }

    private static SnsiVf3Result SNSI_VF3(double[][] p_in){
        SnsiVf3Result rtn = new SnsiVf3Result();
        int width = p_in[0].length;
        double[] t_power2 = new double[width];

        for(int npx=0 ; npx < width ; npx++){
            double[] sel_im_line2 = getFromSpectrum(p_in, npx);
            t_power2[npx] = sum(sel_im_line2);
        }

        for (int npsd = 0 ; npsd < width ; npsd++) {
            double[] ps_line = getFromSpectrum(p_in, npsd);
            SnsiFmax2Result snsiFmax2Result = SNSI_fmax2(ps_line);
            if (snsiFmax2Result.PSNR > 1 && snsiFmax2Result.vf2_SNSI >= 0 &&
                    (t_power2[npsd] / mean(t_power2)) > 0.05) {
                rtn.vf.add(snsiFmax2Result.vf2_SNSI + 1);
                rtn.xx.add((double)npsd + 1);
            }
        }
        return rtn;
    }

    private static double[] VF_fit(double[] XM,double[] YM){
        List<Double> Z = new ArrayList<>();
        double step;
        if(XM[0] > 1){
            step = ((YM[0]-0) / XM[0]);
            for(int i = 1 ; i < XM[0] ; i++){
                Z.add(step * i);
            }
        }

        for(int i = 0 ; i < XM.length-1 ; i++){
            step = XM[i + 1] - XM[i];
            if(step == 1){
                Z.add(YM[i]);
            }else if(step >= 6) {
                Z.add(YM[i]);
                for(int j = 1 ; j < step ; j++) {
                    Z.add(0.0);
                }
            }else {
                for(int j = (int)XM[i] ; j < XM[i + 1] ; j++){
                    Z.add((( YM[i + 1] - YM[i] ) / step) * (j - XM[i]) + YM[i]);
                }
            }
        }
        Z.add(YM[YM.length-1]);
        return Z.stream().mapToDouble(i -> i).toArray();
    }

    //For final algorithm---------------------------------------------------
    public static double findDopplerAngle(){
        double[] rawDataNormalize = new double[BloodVelocityConfig.INTEGER_ULTRASOUND_CAPTURE_LENGTH_PTS];

        for (int i = 0 ; i < rawDataNormalize.length ; i++){
            rawDataNormalize[i] = MainActivity.mRawDataProcessor.mShortUltrasoundDataBeforeFilter[i] / 32768.0;
        }

        double rawDataNormalizeMean = mean(rawDataNormalize);
        double[] ZK = new double[rawDataNormalize.length];
        for (int i = 0 ; i < ZK.length ; i++){
            ZK[i] = 16 * (rawDataNormalize[i] - rawDataNormalizeMean);
        }

        double[][] pp1 = Spectrogram(ZK, 256, 192);
        double[][] p1 = normal(pp1);

        SnsiVf3Result vf =  SNSI_VF3(pp1);
        double[] vf2 = vf.vf.stream().mapToDouble(i -> i).toArray();
        double[] X2 = vf.xx.stream().mapToDouble(i -> i).toArray();
        double[] vf3 = VF_fit(X2 , vf2);
        return Doppler_angle(vf3, p1);
    }
}