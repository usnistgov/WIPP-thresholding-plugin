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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


import ij.IJ;
import ij.ImagePlus;
import ij.io.Opener;
import ij.process.ImageProcessor;

/**
*
* @author Peter Bajcsy <peter.bajcsy at nist.gov>
* @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
*/

public class ThresholdingProcessor {
	
	public File inputFolder;
	public File outputFolder;
	public ThresholdingType thresholdingType;
	public double threshold;
	public int nbCpus;

    public ThresholdingProcessor(File inputFolder, File outputFolder, ThresholdingType thresholdingType,
			double threshold, int nbCpus) {
		super();
		this.inputFolder = inputFolder;
		this.outputFolder = outputFolder;
		this.thresholdingType = thresholdingType;
		this.threshold = threshold;
		this.nbCpus = nbCpus;
	}

	public void runTresh() throws IOException {
        if (inputFolder == null) {
            throw new NullPointerException("Input folder is null");
        }

        File[] tiles =  inputFolder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".tif");
			}
		});

        if (tiles == null) {
        	throw new NullPointerException("Input folder is empty");
        }

        boolean created = outputFolder.mkdirs();
        if (!created && !outputFolder.exists()) {
            throw new IOException("Can not create folder " + outputFolder);
        }

        for(File tile : tiles){
        
        	Opener opener = new Opener();  
	        ImagePlus imp = opener.openImage(tile.getAbsolutePath());  
        
        	ImageProcessor ip = imp.getProcessor();
        
			int xe = ip.getWidth();
			int ye = ip.getHeight();
			int x, y, c=0;
			boolean noBlack = false;
			boolean noWhite = false;
			boolean doIwhite = true;
			boolean doIset = true;
			boolean doIlog = false;
			boolean doIstack=false; 
			boolean doIstackHistogram=false;

			int b = ip.getBitDepth()==8?255:65535;
			if (doIwhite){
				c=b;
				b=0;
			}
			int [] data = (ip.getHistogram());
			int [] temp = new int [data.length];
			///////////////////////////////////////////
			if (noBlack) data[0]=0;
			if (noWhite) data[data.length - 1]=0;

			// bracket the histogram to the range that holds data to make it quicker
			int minbin=-1, maxbin=-1;
			for (int i=0; i<data.length; i++){
				if (data[i]>0) maxbin = i;
			}
			for (int i=data.length-1; i>=0; i--){
				if (data[i]>0) minbin = i;
			}
			//IJ.log (""+minbin+" "+maxbin);
			int [] data2 = new int [(maxbin-minbin)+1];
			for (int i=minbin; i<=maxbin; i++){
				data2[i-minbin]= data[i];;
			}
			//////////////////////////////////////////

			double threshold = 0.0;
			
			// Apply the selected algorithm
			if (data2.length < 2){
				threshold = 0;
			}else if(this.thresholdingType == ThresholdingType.Manual){
				threshold = this.threshold; // use the value specified by a user
			}
			else if(this.thresholdingType == ThresholdingType.IJDefault){
				threshold = ThresholdingMethods.IJDefault(data2); // re-implemeted so we can ignore black/white and set the bright or dark objects
			}
			else if(this.thresholdingType == ThresholdingType.Huang){
				threshold =  ThresholdingMethods.Huang(data2);
			}
			else if(this.thresholdingType == ThresholdingType.Huang2){
				threshold =  ThresholdingMethods.Huang2(data2);
			}
			else if(this.thresholdingType == ThresholdingType.Intermodes){
				threshold = ThresholdingMethods.Intermodes(data2);
			}
			else if(this.thresholdingType == ThresholdingType.IsoData){
				threshold = ThresholdingMethods.IsoData (data2);
			}
			else if(this.thresholdingType == ThresholdingType.Li){
				threshold = ThresholdingMethods.Li(data2);
			}
			else if(this.thresholdingType == ThresholdingType.MaxEntropy){
				threshold = ThresholdingMethods.MaxEntropy(data2);
			}
			else if(this.thresholdingType == ThresholdingType.Mean){
				threshold = ThresholdingMethods.Mean(data2);
			}
			else if(this.thresholdingType == ThresholdingType.MinErrorI){
				threshold = ThresholdingMethods.MinErrorI(data2);
			}
			else if(this.thresholdingType == ThresholdingType.Minimum){
				threshold = ThresholdingMethods.Minimum(data2);
			}
			else if(this.thresholdingType == ThresholdingType.Moments){
				threshold = ThresholdingMethods.Moments(data2);
			}
			else if(this.thresholdingType == ThresholdingType.Otsu){
				threshold = ThresholdingMethods.Otsu(data2);
			}
			else if(this.thresholdingType == ThresholdingType.Percentile){
				threshold = ThresholdingMethods.Percentile(data2);
			}
			else if(this.thresholdingType == ThresholdingType.RenyiEntropy){
				threshold = ThresholdingMethods.RenyiEntropy(data2);
			}
			else if(this.thresholdingType == ThresholdingType.Shanbhag){
				threshold = ThresholdingMethods.Shanbhag(data2);
			}
			else if(this.thresholdingType == ThresholdingType.Triangle){
				threshold = ThresholdingMethods.Triangle(data2); 
			}
			else if(this.thresholdingType == ThresholdingType.Yen){
				threshold = ThresholdingMethods.Yen(data2);
			}

			if(this.thresholdingType != ThresholdingType.Manual){
				threshold+=minbin; // add the offset of the histogram
			}
			
			
			/////////////////////////////////////////
			double min = ip.getMin();
			double max = ip.getMax();
			System.out.println("INFO: min ="+ min + ", max=" + max);
			
//			threshold = 0.5*(max+min);//this.threshold;
			System.out.println("INFO: thresh ="+ threshold);
			
			// saves out the threshold value from auto-threshold methods
/*			File f = new File(outputFolder, "threshold.txt");
			String str = Double.toString(threshold);
			FileOutputStream outputStream;
			try {
				outputStream = new FileOutputStream(f);
				byte[] strToBytes = str.getBytes();
			    outputStream.write(strToBytes);
			    outputStream.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}*/
	
			if (threshold>-1) { 
				//threshold it
/*				if (doIset){
					if (doIwhite) 
						ip.setThreshold(threshold+1, data.length - 1, ImageProcessor.RED_LUT);//IJ.setThreshold(threshold+1, data.length - 1);
					else
						ip.setThreshold(0, threshold, ImageProcessor.RED_LUT);//IJ.setThreshold(0,threshold);
				}
				else{*/
					for( y=0;y<ye;y++) {
						for(x=0;x<xe;x++){
							if(ip.getPixel(x,y)>threshold)
								ip.putPixel(x,y,c);
							else
								ip.putPixel(x,y,b);
						}
					}
					ip.setThreshold(data.length - 1, data.length - 1, ImageProcessor.NO_LUT_UPDATE);
				//}
			}
			
			ImagePlus res = new ImagePlus(tile.getName(), ip);			
			File outputImg = new File(outputFolder, tile.getName());
			IJ.saveAsTiff(res, outputImg.getAbsolutePath());
        }

    }
}

