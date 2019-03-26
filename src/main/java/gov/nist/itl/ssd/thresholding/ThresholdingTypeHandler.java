/*
 * This software was developed at the National Institute of Standards and
 * Technology by employees of the Federal Government in the course of
 * their official duties. Pursuant to title 17 Section 105 of the United
 * States Code this software is not subject to copyright protection and is
 * in the public domain. This software is an experimental system. NIST assumes
 * no responsibility whatsoever for its use by other parties, and makes no
 * guarantees, expressed or implied, about its quality, reliability, or
 * any other characteristic. We would appreciate acknowledgement if the
 * software is used.
 */
package gov.nist.itl.ssd.thresholding;

/**
*
* @author Peter Bajcsy <peter.bajcsy at nist.gov>
* @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
*/

public class ThresholdingTypeHandler {
	
    public static ThresholdingType matchThresholdingType(String text){
    	if(text == null){
    		return ThresholdingType.Invalid;
    	}
    	switch(text){
	    	case "Manual":
	    		return ThresholdingType.Manual;
	    	case "IJDefault":
	    		return ThresholdingType.IJDefault;
	    	case "Huang":
	    		return ThresholdingType.Huang;
	    	case "Huang2":
	    		return ThresholdingType.Huang2;
	    	case "Intermodes":
	    		return ThresholdingType.Intermodes;
	    	case "IsoData":
	    		return ThresholdingType.IsoData;
	    	case "Li":
	    		return ThresholdingType.Li;
	    	case "MaxEntropy":
	    		return ThresholdingType.MaxEntropy;
	    	case "Mean":
	    		return ThresholdingType.Mean;
	    	case "MinErrorI":
	    		return ThresholdingType.MinErrorI;
	    	case "Minimum":
	    		return ThresholdingType.Minimum;
	    	case "Moments":
	    		return ThresholdingType.Moments;
	    	case "Otsu":
	    		return ThresholdingType.Otsu;
	    	case "Percentile":
	    		return ThresholdingType.Percentile;
	    	case "RenyiEntropy":
	    		return ThresholdingType.RenyiEntropy;
	    	case "Shanbhag":
	    		return ThresholdingType.Shanbhag;
	    	case "Triangle":
	    		return ThresholdingType.Triangle;
	    	case "Yen":
	    		return ThresholdingType.Yen;	    		
	    	default:
	    		return ThresholdingType.Invalid;		
	    	}
    }
    
}
