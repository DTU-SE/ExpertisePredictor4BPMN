package moderare.expertise.utils;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

public class ChartUtils {

	/**
	 * 
	 * @param chart
	 * @param path
	 * @param width
	 * @param height
	 * @param info
	 * @throws IOException
	 * 
	 * @see https://github.com/pentaho/pentaho-platform/blob/master/core/src/main/java/org/pentaho/platform/uifoundation/chart/JFreeChartEngine.java#L1143
	 */
	public static void saveChartAsSVG(final JFreeChart chart, final File path, final int width, final int height) throws IOException {
		// Get a DOMImplementation
		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
		
		// Create an instance of org.w3c.dom.Document
		Document document = domImpl.createDocument(null, "svg", null);
		
		// Create an instance of the SVG Generator
		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
		svgGenerator.setSVGCanvasSize(new Dimension(width, height));
		
		// set the precision to avoid a null pointer exception in Batik 1.5
		svgGenerator.getGeneratorContext().setPrecision(6);
		
		// Ask the chart to render into the SVG Graphics2D implementation
		chart.draw(svgGenerator, new Rectangle2D.Double(0, 0, width, height), new ChartRenderingInfo());
		
		// Finally, stream out SVG to a file using UTF-8 character to byte encoding
		boolean useCSS = false;
		Writer out = new OutputStreamWriter(new FileOutputStream(path));
		svgGenerator.stream(out, useCSS);
	}
}
