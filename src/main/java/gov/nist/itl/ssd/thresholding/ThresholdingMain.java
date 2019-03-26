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
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
*
* @author Peter Bajcsy <peter.bajcsy at nist.gov>
* @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
*/

public class ThresholdingMain {

	private static final Logger LOG = Logger.getLogger(
			ThresholdingMain.class.getName());
	
	
	public static void main(String[] args) throws IOException, Exception {
		// sanity checks
		int i;
		System.out.println("argument length=" + args.length);
		for (i = 0; i < args.length; i++) {
			System.out.println("args[" + i + "]:" + args[i]);
		}	

		Options options = new Options();

		Option input = new Option("i", "input", true, "input folder");
		input.setRequired(true);
		options.addOption(input);

		Option output = new Option("o", "output", true, "output folder");
		output.setRequired(true);
		options.addOption(output);

		Option thresholdType = new Option("ttype", "thresholdtype", true, "threshold method type");
		thresholdType.setRequired(true);
		options.addOption(thresholdType);


		Option thresholdValue = new Option("tvalue", "thresholdvalue", true, "threshold value for Manual threshold method type");
		thresholdValue.setRequired(false);
		options.addOption(thresholdValue);	        

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("utility-name", options);

			System.exit(1);
			return;
		}

		String inputFileDir = cmd.getOptionValue("input");
		String outputFileDir = cmd.getOptionValue("output");

		System.out.println(inputFileDir);
		System.out.println(outputFileDir);

		String threshTypeStr = cmd.getOptionValue("thresholdtype");
		String threshValStr = cmd.getOptionValue("thresholdvalue");

		double threshold = 0.0;
		int nbCpus = 1;

		File inputFolder = new File (inputFileDir);
		File outputFolder = new File (outputFileDir);

		ThresholdingType threshType = ThresholdingTypeHandler.matchThresholdingType(threshTypeStr);
		if(threshType.equals(ThresholdingType.Invalid)){
			System.err.println("ERROR: the threshold method type is invalid");
			return;
		}
		
		if(threshType.equals(ThresholdingType.Manual)){
			threshold = Double.valueOf(threshValStr);//21800.0;			
		}

		System.out.println("threshType : " + threshType);
			
		LOG.info("Launching Thresholding!!");
		
		ThresholdingProcessor tp = new ThresholdingProcessor(inputFolder, outputFolder, threshType, threshold, nbCpus);
		tp.runTresh();

		int exitVal = 0;
		String err = "";
		
		if (exitVal != 0){
			throw new RuntimeException(err);
		}
		LOG.info("The end of Thresholding!");  
	}
	
}
