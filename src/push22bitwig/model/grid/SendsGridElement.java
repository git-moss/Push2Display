package push22bitwig.model.grid;

import push22bitwig.LayoutSettings;
import push22bitwig.model.ChannelType;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Label;
import java.io.IOException;


/**
 * An element in the grid which contains a menu and a channels' sends 1-4 or 5-8.
 *
 * Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class SendsGridElement extends ChannelSelectionGridElement
{
    final String []    sendNames  = new String []
                                    {
                                            "",
                                            "",
                                            "",
                                            ""
                                    };
    final String []    sendTexts  = new String []
                                    {
                                            "",
                                            "",
                                            "",
                                            ""
                                    };
    final int []       sendValues = new int []
                                    {
                                            0,
                                            0,
                                            0,
                                            0
                                    };
    private boolean [] sendEdited = new boolean []
                                    {
                                            false,
                                            false,
                                            false,
                                            false
                                    };
    private boolean    isExMode;


    /**
     * Constructor.
     *
     * @param sendNames The names of the send tracks
     * @param sendTexts The texts of the sends volumes
     * @param sendValues The values of the sends volumes
     * @param sendEdited The states of which send can be edited
     * @param menuName The text for the menu
     * @param isMenuSelected True if the menu is selected
     * @param name The of the grid element (track name, parameter name, etc.)
     * @param color The color to use for the header, may be null
     * @param isSelected True if the grid element is selected
     * @param type The type of the track
     * @param isExMode True if the sends grid element is an extension for a track grid element
     */
    public SendsGridElement (final String [] sendNames, final String [] sendTexts, final int [] sendValues, final boolean [] sendEdited, final String menuName, final boolean isMenuSelected, final String name, final Color color, final boolean isSelected, final ChannelType type, final boolean isExMode)
    {
        super (menuName, isMenuSelected, name, color, isSelected, type);
        for (int i = 0; i < 4; i++)
        {
            this.sendNames[i] = sendNames[i];
            this.sendTexts[i] = sendTexts[i];
            this.sendValues[i] = sendValues[i];
            this.sendEdited[i] = sendEdited[i];
        }

        this.isExMode = isExMode;
    }


    /** {@inheritDoc} */
    @Override
    public void draw (final Graphics2D gc, final int left, final int width, final int height, final LayoutSettings layoutSettings) throws IOException
    {
        super.draw (gc, left, width, height, layoutSettings);

        final String name = this.getName ();
        // Element is off if the name is empty
        if ((name == null || name.length () == 0) && !this.isExMode)
            return;

        final int trackRowTop = height - TRACK_ROW_HEIGHT - UNIT - SEPARATOR_SIZE;
        final int sliderWidth = width - 2 * INSET - 1;
        final int t = MENU_HEIGHT + 1;
        final int h = trackRowTop - t;
        final int sliderAreaHeight = h;
        // 4 rows of Texts and 4 rows of faders
        final int sendRowHeight = sliderAreaHeight / 8;
        final int sliderHeight = sendRowHeight - 2 * SEPARATOR_SIZE;

        // Background of slider area
        final Color backgroundColor = layoutSettings.getBackgroundColor ();
        gc.setColor (this.isSelected () || this.isExMode ? backgroundColor.brighter () : backgroundColor);
        gc.fillRect (this.isExMode ? left - SEPARATOR_SIZE : left, t, this.isExMode ? width + SEPARATOR_SIZE : width, this.isExMode ? h - 2 : h);

        int topy = MENU_HEIGHT + (this.isExMode ? 0 : SEPARATOR_SIZE);

        gc.setFont (layoutSettings.getTextFont (sendRowHeight));
        final Color textColor = layoutSettings.getTextColor ();
        final Color borderColor = layoutSettings.getBorderColor ();
        final Color faderColor = layoutSettings.getFaderColor ();
        final Color editColor = layoutSettings.getEditColor ();
        final int faderLeft = left + INSET;
        for (int i = 0; i < 4; i++)
        {
            if (this.sendNames[i].length () == 0)
                break;

            gc.setColor (textColor);
            drawTextInBounds (gc, this.sendNames[i], faderLeft, topy + SEPARATOR_SIZE, width, sendRowHeight, Label.LEFT);
            topy += sendRowHeight;
            gc.setColor (borderColor);
            gc.fillRect (faderLeft, topy + SEPARATOR_SIZE, sliderWidth, sliderHeight);
            final int valueWidth = (int) (this.sendValues[i] * sliderWidth / getMaxValue ());
            gc.setColor (faderColor);
            final int faderTop = topy + SEPARATOR_SIZE + 1;
            gc.fillRect (faderLeft + 1, faderTop, valueWidth - 1, sliderHeight - 2);

            if (this.sendEdited[i])
            {
                gc.setColor (editColor);
                final boolean isTouched = this.sendTexts[i] != null && this.sendTexts[i].length () > 0;
                final int w = isTouched ? 3 : 1;
                gc.fillRect (Math.min (faderLeft + sliderWidth - w - 1, faderLeft + valueWidth + 1), faderTop, w, sliderHeight - 2);
            }

            topy += sendRowHeight;
        }

        // Draw volume text on top if set
        final int boxWidth = sliderWidth / 2;
        final int boxLeft = faderLeft + sliderWidth - boxWidth;
        topy = MENU_HEIGHT;
        final Color backgroundDarker = backgroundColor.darker ();
        final Font textFont = layoutSettings.getTextFont (UNIT);
        for (int i = 0; i < 4; i++)
        {
            topy += sendRowHeight;

            if (this.sendTexts[i].length () > 0)
            {
                final int volumeTextTop = topy + sliderHeight + 1 + (this.isExMode ? 0 : SEPARATOR_SIZE);
                gc.setColor (backgroundDarker);
                gc.fillRect (boxLeft, volumeTextTop, boxWidth, UNIT);
                gc.setColor (borderColor);
                gc.drawRect (boxLeft, volumeTextTop, boxWidth - 1, UNIT);
                gc.setFont (textFont);
                gc.setColor (textColor);
                drawTextInBounds (gc, this.sendTexts[i], boxLeft, volumeTextTop, boxWidth, UNIT, Label.CENTER);
            }

            topy += sendRowHeight;
        }
    }
}
