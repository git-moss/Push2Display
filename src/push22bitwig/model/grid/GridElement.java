package push22bitwig.model.grid;

import push22bitwig.LayoutSettings;

import java.awt.Graphics2D;
import java.io.IOException;


/**
 * An element in the grid.
 * 
 * Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public interface GridElement
{
    /** The full width of the drawing area. */
    static final int        DISPLAY_WIDTH  = 960;
    /** The full height of the drawing area. */
    static final int        DISPLAY_HEIGHT = 160;

    /** The size to use for separator spacing. */
    static final int        SEPARATOR_SIZE = 2;
    /** A drawing 'unit'. */
    static final int        UNIT           = DISPLAY_HEIGHT / 12;
    /** 2 units. */
    static final int        DOUBLE_UNIT    = 2 * UNIT;
    /** Half a unit. */
    static final int        HALF_UNIT      = UNIT / 2;
    /** The height of the menu on top. */
    static final int        MENU_HEIGHT    = UNIT + 2 * SEPARATOR_SIZE;
    /** Insets on the top and bottom of the element. */
    static final int        INSET          = SEPARATOR_SIZE / 2 + HALF_UNIT;
    /** Where the controls drawing area starts. */
    static final int        CONTROLS_TOP   = MENU_HEIGHT + INSET;

    /** The maximum possible value for a parameter. */
    public static final int MAX_VALUE      = 1024;


    /**
     * Draw the element.
     *
     * @param gc The graphic context
     * @param left The left bound of the drawing area of the element
     * @param width The width of the drawing area of the element
     * @param height The height of the drawing area of the element
     * @param layoutSettings The layout settings to use
     * @throws IOException Could not load a SVG image
     */
    void draw (final Graphics2D gc, final int left, final int width, final int height, final LayoutSettings layoutSettings) throws IOException;
}
