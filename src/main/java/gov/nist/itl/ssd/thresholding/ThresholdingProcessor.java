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
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import gov.nist.itl.ssd.thresholding.utils.BioFormatsUtils;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import loci.common.DebugTools;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.codec.CompressionType;
import loci.formats.ome.OMEXMLMetadata;
import loci.formats.out.OMETiffWriter;
import loci.formats.services.OMEXMLService;
import ome.xml.model.enums.PixelType;

/**
 *
 * @author Peter Bajcsy <peter.bajcsy at nist.gov>
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 */

public class ThresholdingProcessor {

	// Tile size used in WIPP
	private static final int TILE_SIZE = 1024;
	
	private static final Logger LOGGER = Logger.getLogger(ThresholdingProcessor.class.getName());
	private int width;
	private int height;

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

	public void runTresh() throws IOException, Exception {
		
		if (inputFolder == null) {
			throw new NullPointerException("Input folder is null");
		}

		File[] images =  inputFolder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".tif");
			}
		});

		if (images == null || images.length == 0) {
			throw new NullPointerException("Input folder is empty or no images were found.");
		}

		boolean created = outputFolder.mkdirs();
		if (!created && !outputFolder.exists()) {
			throw new IOException("Can not create folder " + outputFolder);
		}

		for(File image : images){

			//Reading a tiled tiff with Bioformats and converting it to an ImagePlus 
			ImagePlus imp = BioFormatsUtils.readImage(image.getAbsolutePath());

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
			LOGGER.log(Level.INFO, "min = " + min + ", max=" + max);
			LOGGER.log(Level.INFO, "thresh = " + threshold);

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
			
			File outputFile = new File(outputFolder, image.getName());
			OMEXMLMetadata metadata = getMetadata(image);
			PixelType pxlType = metadata.getPixelsType(0);
			int bpp = FormatTools.getBytesPerPixel(pxlType.getValue());
			LOGGER.log(Level.INFO, "bpp: " + bpp);

			byte[] bytesArr = null;
			
			switch(pxlType) {
				case UINT8:
					LOGGER.log(Level.INFO, image.getName() + " is an 8bpp image");
					bytesArr = (byte[]) ip.getPixels();
					break;
				case UINT16: 
					LOGGER.log(Level.INFO, image.getName() + " is a 16bpp image");
					short[] shorts = (short[]) ip.getPixels();
					
					//Converting short array to bytes array
					ByteBuffer byteShortBuf = ByteBuffer.allocate(shorts.length*2);
					for (short s: shorts) {
						byteShortBuf.putShort(s);
					}
					bytesArr = byteShortBuf.array();
					break;
				case FLOAT:
					LOGGER.log(Level.WARNING, image.getName() + " is a 32bpp image.");
					LOGGER.log(Level.SEVERE, "32bpp images are not handled by the thresholding plugin. Please convert the image to an 8bpp or a 16bpp image.");
					throw new UnsupportedOperationException("Unsupported image type.");
				default:
					LOGGER.log(Level.WARNING, "the type of this image: " + image.getName() + " is not 8bpp nor 16bpp");
					LOGGER.log(Level.SEVERE, "Please convert the image type to 8bpp or 16bpp.");
					throw new UnsupportedOperationException("Unsupported image type.");
			}


			//Writing the output tiled tiff
			try (OMETiffWriter imageWriter = new OMETiffWriter()) {
				imageWriter.setMetadataRetrieve(metadata);
				imageWriter.setTileSizeX(TILE_SIZE);
				imageWriter.setTileSizeY(TILE_SIZE);
				imageWriter.setInterleaved(metadata.getPixelsInterleaved(0));
				imageWriter.setCompression(CompressionType.LZW.getCompression());
				imageWriter.setId(outputFile.getPath());

				// Determined the number of tiles to read and write
				int nXTiles = this.width / TILE_SIZE;
				int nYTiles = this.height / TILE_SIZE;
				if (nXTiles * TILE_SIZE != this.width) nXTiles++;
				if (nYTiles * TILE_SIZE != this.height) nYTiles++;
				
				for (int k=0; k<nYTiles; k++) {
					for (int l=0; l<nXTiles; l++) {
						
						int tileX = l * TILE_SIZE;
						int tileY = k * TILE_SIZE;
						
						int effTileSizeX = (tileX + TILE_SIZE) < this.width ? TILE_SIZE : this.width - tileX;
						int effTileSizeY = (tileY + TILE_SIZE) < this.height ? TILE_SIZE : this.height - tileY;
						
						// Get values of current tile
						byte[] buf = new byte[effTileSizeX * effTileSizeY * bpp];
						int offset, i, bufIndex = 0;
						for (int indexY = tileY; indexY < (tileY + effTileSizeY); indexY ++) {
							offset = indexY * this.width * bpp;
							for (int indexX = tileX; indexX < (tileX + effTileSizeX); indexX ++) {
								i = offset + indexX * bpp;
								for (int bppIndex = 0; bppIndex < bpp; bppIndex ++) {
									buf[bufIndex] = bytesArr[i + bppIndex];
									bufIndex ++;
								}
							}
						}
						// Write tile
						imageWriter.saveBytes(0, buf, tileX, tileY, effTileSizeX, effTileSizeY);
					}
				}
				
			} catch (FormatException | IOException ex) {
				throw new RuntimeException("Error while setting up image writer for file "
						+ outputFile + ": " + ex.getMessage(), ex);
			}
		}
	}
	
	//Inspired from the WIPP-image-assembling-plugin
	private OMEXMLMetadata getMetadata(File tile) {
		OMEXMLMetadata metadata;
		try {
			OMEXMLService omeXmlService = new ServiceFactory().getInstance(
					OMEXMLService.class);
			metadata = omeXmlService.createOMEXMLMetadata();
		} catch (DependencyException ex) {
			throw new RuntimeException("Cannot find OMEXMLService", ex);
		} catch (ServiceException ex) {
			throw new RuntimeException("Cannot create OME metadata", ex);
		}
		try (ImageReader imageReader = new ImageReader()) {
			IFormatReader reader;
			reader = imageReader.getReader(tile.getPath());
			reader.setOriginalMetadataPopulated(false);
			reader.setMetadataStore(metadata);
			reader.setId(tile.getPath());
			this.width = reader.getSizeX();
			this.height = reader.getSizeY();
			if (reader.getRGBChannelCount() > 1) {
				throw new RuntimeException("RGB images not supported by this plugin.");
			}
		} catch (FormatException | IOException ex) {
			throw new RuntimeException("No image reader found for file "
					+ tile, ex);
		}

		return metadata;
	}
}

