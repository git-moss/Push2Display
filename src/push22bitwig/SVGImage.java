package push22bitwig;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGElement;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


/**
 * A Scalable Vector Graphic (SVG) image file.
 * 
 * Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class SVGImage
{
    private static final Map<String, Map<Color, SVGImage>> CACHE      = new HashMap<> ();
    private static final Object                            CACHE_LOCK = new Object ();

    private BufferedImage                                  bufferedImage;


    /**
     * Get a SVG image as a buffered image. The image is expected to be monochrome: 1 color and the
     * a transparent background. The given color replaces the color of the image. The images are
     * cached by name and color.
     *
     * @param imageName The name (absolute path) of the image
     * @param color The color for replacement
     * @return The buffered image
     * @throws IOException Could not load the image
     */
    public static BufferedImage getSVGImage (final String imageName, final Color color) throws IOException
    {
        SVGImage svgImage;
        synchronized (CACHE_LOCK)
        {
            Map<Color, SVGImage> images = CACHE.get (imageName);
            if (images == null)
            {
                images = new HashMap<> ();
                CACHE.put (imageName, images);
            }
            svgImage = images.get (color);
            if (svgImage == null)
            {
                svgImage = new SVGImage (imageName, color);
                images.put (color, svgImage);
            }
        }
        return svgImage.getImage ();
    }


    /**
     * Clears the image cache.
     */
    public static void clearCache ()
    {
        synchronized (CACHE_LOCK)
        {
            CACHE.clear ();
        }
    }


    /**
     * Constructor.
     *
     * @param imageName The name of the image (absolute path) to load
     * @param color The replacement color
     * @throws IOException Could not load the image
     */
    public SVGImage (final String imageName, final Color color) throws IOException
    {
        final BufferedImageTranscoder trans = new BufferedImageTranscoder ();
        try (InputStream file = this.getClass ().getResourceAsStream (imageName))
        {
            final Document doc = this.loadDocument (file, color);
            trans.transcode (new TranscoderInput (doc), null);
            this.bufferedImage = trans.getBufferedImage ();
        }
        catch (final TranscoderException | SAXException | ParserConfigurationException ex)
        {
            throw new IOException (ex);
        }
    }


    /**
     * Get the buffered image.
     *
     * @return The buffered image
     */
    public BufferedImage getImage ()
    {
        return this.bufferedImage;
    }


    /**
     * Load the SVG image from an input stream and replaces its color. The SVG format is a XML
     * format.
     *
     * @param inputStream From which to load the image
     * @param color The replacement color
     * @return The loaded SVG image document
     * @throws SAXException Parsing error
     * @throws IOException Could not load the image
     * @throws ParserConfigurationException Problem with XML parser
     */
    public Document loadDocument (final InputStream inputStream, final Color color) throws SAXException, IOException, ParserConfigurationException
    {
        final String parser = XMLResourceDescriptor.getXMLParserClassName ();
        final SAXSVGDocumentFactory f = new SAXSVGDocumentFactory (parser);

        final SVGDocument document = f.createSVGDocument ("xxx", inputStream);

        changeColorOfElement (color, document, "polygon");
        changeColorOfElement (color, document, "circle");
        changeColorOfElement (color, document, "path");
        changeColorOfElement (color, document, "rect");

        return document;
    }


    /**
     * Adds a new fill color to all given elements.
     *
     * @param color The replacement color
     * @param document The document in which to replace the color
     * @param elementName The name of an XML element for which to replace the color
     */
    private static void changeColorOfElement (final Color color, final SVGDocument document, final String elementName)
    {
        final NodeList nodes = document.getElementsByTagName (elementName);
        for (int i = 0; i < nodes.getLength (); i++)
        {
            if (nodes.item (i) instanceof SVGElement)
            {
                final SVGElement element = (SVGElement) nodes.item (i);
                element.setAttribute ("fill", toText (color));
            }
        }
    }


    /**
     * Formats the given color as CSS rgb string.
     *
     * @param color The color to format
     * @return The formatted color string
     */
    private static final String toText (final Color color)
    {
        return new StringBuilder (20).append ("rgb(").append (color.getRed ()).append (',').append (color.getGreen ()).append (',').append (color.getBlue ()).append (')').toString ();
    }

    /**
     * Stores the SVG image in a buffered image during the transcoding process.
     */
    public class BufferedImageTranscoder extends ImageTranscoder
    {
        private BufferedImage img;


        /** {@inheritDoc} */
        @Override
        public BufferedImage createImage (final int width, final int height)
        {
            return new BufferedImage (width, height, BufferedImage.TYPE_INT_ARGB);
        }


        /** {@inheritDoc} */
        @Override
        public void writeImage (final BufferedImage img, final TranscoderOutput to) throws TranscoderException
        {
            this.img = img;
        }


        /**
         * Get the buffered image.
         *
         * @return The image
         */
        public BufferedImage getBufferedImage ()
        {
            return this.img;
        }
    }
}
