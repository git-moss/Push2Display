package push22bitwig.model.grid;

import push22bitwig.LayoutSettings;
import push22bitwig.model.ChannelType;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Label;
import java.io.IOException;


/**
 * An element in the grid which contains a fader and text for a value.
 *
 * Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class ParamGridElement extends ChannelSelectionGridElement
{
    private final String  paramName;
    private final int     paramValue;
    private final String  paramValueText;
    private final boolean isTouched;


    /**
     * Constructor.
     *
     * @param menuName The text for the menu
     * @param isMenuSelected True if the menu is selected
     * @param name The of the grid element (track name, parameter name, etc.)
     * @param type The channel type if any
     * @param color The color to use for the header, may be null
     * @param isSelected True if the grid element is selected
     * @param paramName The name of the parameter
     * @param paramValue The value of the fader
     * @param paramValueText The textual form of the faders value
     * @param isTouched True if touched
     */
    public ParamGridElement (final String menuName, final boolean isMenuSelected, final String name, final ChannelType type, final Color color, final boolean isSelected, final String paramName, final int paramValue, final String paramValueText, final boolean isTouched)
    {
        super (menuName, isMenuSelected, name, color, isSelected, type);

        this.paramName = paramName;
        this.paramValue = paramValue;
        this.paramValueText = paramValueText;
        this.isTouched = isTouched;
    }


    /** {@inheritDoc} */
    @Override
    public void draw (final Graphics2D gc, final int left, final int width, final int height, final LayoutSettings layoutSettings) throws IOException
    {
        this.drawMenu (gc, left, width, layoutSettings);

        final boolean isValueMissing = this.paramValue == 16383; // == -1

        final int trackRowTop = height - TRACK_ROW_HEIGHT - UNIT - SEPARATOR_SIZE;
        final String name = this.getName ();
        if (name != null && name.length () > 0)
            this.drawTrackInfo (gc, left, width, height, trackRowTop, name, layoutSettings);

        // Element is off if the name is empty
        if (this.paramName == null || this.paramName.length () == 0)
            return;

        final int elementWidth = width - 2 * INSET;
        final int elementHeight = (trackRowTop - CONTROLS_TOP - INSET) / 3;

        // Draw the background
        final Color backgroundColor = layoutSettings.getBackgroundColor ();
        gc.setColor (this.isTouched ? backgroundColor.brighter () : backgroundColor);
        gc.fillRect (left, MENU_HEIGHT + 1, width, trackRowTop - (isValueMissing ? CONTROLS_TOP + elementHeight : MENU_HEIGHT + 1));

        // Draw the name and value texts
        final Color textColor = layoutSettings.getTextColor ();
        gc.setColor (textColor);
        gc.setFont (layoutSettings.getTextFont (elementHeight * 2 / 3));
        drawTextInBounds (gc, this.paramName, left + INSET - 1, CONTROLS_TOP - INSET, elementWidth, elementHeight, Label.CENTER);
        drawTextInBounds (gc, this.paramValueText, left + INSET - 1, CONTROLS_TOP - INSET + elementHeight, elementWidth, elementHeight, Label.CENTER);

        // Value slider
        if (isValueMissing)
            return;
        final int elementInnerWidth = elementWidth - 2;
        final double maxValue = getMaxValue ();
        final int valueWidth = (int) (this.paramValue >= maxValue - 1 ? elementInnerWidth : elementInnerWidth * this.paramValue / maxValue);
        final int innerTop = CONTROLS_TOP + 2 * elementHeight + 1;
        final Color borderColor = layoutSettings.getBorderColor ();
        gc.setColor (borderColor);
        gc.fillRect (left + INSET - 1, CONTROLS_TOP + 2 * elementHeight, elementWidth, elementHeight);
        gc.setColor (layoutSettings.getFaderColor ());
        gc.fillRect (left + INSET, innerTop, valueWidth, elementHeight - 2);
        gc.setColor (layoutSettings.getEditColor ());
        final int w = this.isTouched ? 3 : 1;
        gc.fillRect (left + INSET + Math.max (0, valueWidth - w), innerTop, w, elementHeight - 2);
    }
}
