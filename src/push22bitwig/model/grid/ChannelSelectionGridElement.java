package push22bitwig.model.grid;

import push22bitwig.LayoutSettings;
import push22bitwig.SVGImage;
import push22bitwig.model.ChannelType;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Label;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.EnumMap;


/**
 * An element in the grid which contains a menu and a channels' icon, name and color.
 * 
 * Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class ChannelSelectionGridElement extends AbstractGridElement
{
    private static final EnumMap<ChannelType, String> ICONS            = new EnumMap<ChannelType, String> (ChannelType.class);

    protected static final int                        TRACK_ROW_HEIGHT = (int) (1.6 * UNIT);


    static
    {
        ICONS.put (ChannelType.AUDIO, "/images/track/audio_track.svg");
        ICONS.put (ChannelType.INST, "/images/track/instrument_track.svg");
        ICONS.put (ChannelType.GROUP, "/images/track/group_track.svg");
        ICONS.put (ChannelType.EFFECT, "/images/track/return_track.svg");
        ICONS.put (ChannelType.HYBRID, "/images/track/hybrid_track.svg");
        ICONS.put (ChannelType.MASTER, "/images/track/master_track.svg");
        ICONS.put (ChannelType.LAYER, "/images/track/multi_layer.svg");
    }

    private final ChannelType type;


    /**
     * Constructor.
     *
     * @param menuName The text for the menu
     * @param isMenuSelected True if the menu is selected
     * @param name The of the grid element (track name, parameter name, etc.)
     * @param color The color to use for the header, may be null
     * @param isSelected True if the grid element is selected
     * @param type The type of the track
     */
    public ChannelSelectionGridElement (final String menuName, final boolean isMenuSelected, final String name, final Color color, final boolean isSelected, final ChannelType type)
    {
        super (menuName, isMenuSelected, null, name, color, isSelected);
        this.type = type;
    }


    /**
     * Get the type of the channel.
     *
     * @return The type
     */
    public ChannelType getType ()
    {
        return this.type;
    }


    /** {@inheritDoc} */
    @Override
    public String getIcon ()
    {
        return ICONS.get (this.type);
    }


    /** {@inheritDoc} */
    @Override
    public void draw (final Graphics2D gc, final int left, final int width, final int height, final LayoutSettings layoutSettings) throws IOException
    {
        this.drawMenu (gc, left, width, layoutSettings);

        final String name = this.getName ();
        // Element is off if the name is empty
        if (name == null || name.length () == 0)
            return;

        final int trackRowTop = height - TRACK_ROW_HEIGHT - UNIT - SEPARATOR_SIZE;
        this.drawTrackInfo (gc, left, width, height, trackRowTop, name, layoutSettings);
    }


    /**
     * Draws the tracks info, like icon, color and name.
     *
     * @param gc The graphics context
     * @param left The left bound of the drawing area
     * @param width The width of the drawing area
     * @param height The height of the drawing area
     * @param trackRowTop The top of the drawing area
     * @param name The name of the track
     * @param layoutSettings The layout settings
     * @throws IOException Could not load a SVG image
     */
    protected void drawTrackInfo (final Graphics2D gc, final int left, final int width, final int height, final int trackRowTop, final String name, final LayoutSettings layoutSettings) throws IOException
    {
        // Draw the background
        final Color backgroundColor = layoutSettings.getBackgroundColor ();
        gc.setColor (this.isSelected () ? backgroundColor.brighter () : backgroundColor);
        gc.fillRect (left, trackRowTop + 1, width, height - UNIT - 1);

        // The tracks icon and name
        final String iconName = this.getIcon ();
        if (iconName != null)
        {
            final Color textColor = layoutSettings.getTextColor ();
            final BufferedImage icon = SVGImage.getSVGImage (iconName, textColor);
            gc.drawImage (icon, left + (DOUBLE_UNIT - icon.getWidth ()) / 2, height - TRACK_ROW_HEIGHT - UNIT + (TRACK_ROW_HEIGHT - icon.getHeight ()) / 2, null);
            gc.setColor (textColor);
            gc.setFont (layoutSettings.getTextFont ((int) (1.2 * UNIT)));
            drawTextInBounds (gc, name, left + DOUBLE_UNIT, trackRowTop, width, TRACK_ROW_HEIGHT, Label.LEFT);
        }

        // The track color section
        gc.setColor (this.getColor ());
        gc.fillRect (left, height - UNIT, width, UNIT);
    }
}
