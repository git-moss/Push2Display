package push22bitwig;

import push22bitwig.model.DisplayModel;
import push22bitwig.model.grid.GridElement;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;


/**
 * Draws the content of the display based on the model into a bitmap.
 * 
 * Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class VirtualDisplay
{
    private static final int     DISPLAY_WIDTH     = 960;
    private static final int     DISPLAY_HEIGHT    = 160;

    private final DisplayModel   model;
    private final BufferedImage  image1            = new BufferedImage (DISPLAY_WIDTH, DISPLAY_HEIGHT, BufferedImage.TYPE_INT_ARGB);
    private final BufferedImage  image2            = new BufferedImage (DISPLAY_WIDTH, DISPLAY_HEIGHT, BufferedImage.TYPE_INT_ARGB);
    private BufferedImage        currentImage      = this.image1;
    private final Object         imageExchangeLock = new Object ();
    private final LayoutSettings layoutSettings;


    /**
     * Constructor.
     *
     * @param model Stores the data for drawing the display
     * @param layoutSettings The layout settings to use for drawing
     */
    public VirtualDisplay (final DisplayModel model, final LayoutSettings layoutSettings)
    {
        this.model = model;
        this.model.addGridElementChangeListener (c -> this.redrawGrid ());
        this.layoutSettings = layoutSettings;
        this.layoutSettings.addFontChangeListener ( (observable, oldValue, newValue) -> this.redrawGrid ());
        this.layoutSettings.addColorChangeListener ( (observable, oldValue, newValue) -> this.redrawGrid ());
    }


    /**
     * Redraw the display.
     */
    public void redrawGrid ()
    {
        synchronized (this.imageExchangeLock)
        {
            final BufferedImage drawImage = this.currentImage == this.image1 ? this.image2 : this.image1;
            this.drawGrid (configureGraphics (drawImage));
            this.currentImage = drawImage;
        }
    }


    /**
     * Get the drawn image.
     *
     * @return The image
     */
    public BufferedImage getImage ()
    {
        return this.currentImage;
    }


    /**
     * Draws the N grid elements of the grid.
     *
     * @param gc The graphics context to draw into
     */
    public void drawGrid (final Graphics2D gc)
    {
        // Clear display
        gc.setColor (this.layoutSettings.getBorderColor ());
        gc.fillRect (0, 0, DISPLAY_WIDTH, DISPLAY_HEIGHT);

        final List<GridElement> elements = this.model.getGridElements ();
        final int size = elements.size ();
        if (size == 0)
            return;
        final int gridWidth = DISPLAY_WIDTH / size;
        final int paintWidth = gridWidth - GridElement.SEPARATOR_SIZE;
        final int offsetX = GridElement.SEPARATOR_SIZE / 2;

        try
        {
            for (int i = 0; i < size; i++)
                elements.get (i).draw (gc, i * gridWidth + offsetX, paintWidth, DISPLAY_HEIGHT, this.layoutSettings);
        }
        catch (final IOException ex)
        {
            this.model.addLogMessage ("Could not load SVG image: " + ex.getLocalizedMessage ());
        }
    }


    /**
     * Makes several graphic settings on the graphics.
     *
     * @param image The image for which to create a graphics context
     * @return The created graphics context
     */
    private static Graphics2D configureGraphics (final BufferedImage image)
    {
        final Graphics2D g = image.createGraphics ();
        g.setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint (RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint (RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        return g;
    }
}
