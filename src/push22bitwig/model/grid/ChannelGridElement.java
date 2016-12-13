package push22bitwig.model.grid;

import push22bitwig.LayoutSettings;
import push22bitwig.SVGImage;
import push22bitwig.model.ChannelType;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Label;
import java.awt.Paint;
import java.awt.image.BufferedImage;
import java.io.IOException;


/**
 * An element in the grid which contains the channel settings: Volume, VU, Pan, Mute, Solo and Arm.
 *
 * Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class ChannelGridElement extends ChannelSelectionGridElement
{
    /** Edit volume. */
    public static final int EDIT_TYPE_VOLUME     = 0;
    /** Edit panorma. */
    public static final int EDIT_TYPE_PAN        = 1;
    /** Edit crossfader setting. */
    public static final int EDIT_TYPE_CROSSFADER = 2;
    /** Edit all settings. */
    public static final int EDIT_TYPE_ALL        = 3;

    private final int       editType;
    private final int       volumeValue;
    private final String    volumeText;
    private final int       panValue;
    private final String    panText;
    private final int       vuValue;
    private final boolean   isMute;
    private final boolean   isSolo;
    private final boolean   isArm;
    private final int       crossfadeMode;


    /**
     * Constructor.
     *
     * @param editType What to edit, 0 = Volume, 1 = Pan, 2 = Crossfade Mode
     * @param menuName The text for the menu
     * @param isMenuSelected True if the menu is selected
     * @param name The of the grid element (track name, parameter name, etc.)
     * @param color The color to use for the header, may be null
     * @param isSelected True if the grid element is selected
     * @param type The type of the track
     * @param volumeValue The value of the volume
     * @param volumeText The textual form of the volumes value
     * @param panValue The value of the panorama
     * @param panText The textual form of the panorama
     * @param vuValue The value of the VU
     * @param isMute True if muted
     * @param isSolo True if soloed
     * @param isArm True if recording is armed
     * @param crossfadeMode The crossfader mode: 0 = A, 1 = AB, B = 2, -1 turns it off
     */
    public ChannelGridElement (final int editType, final String menuName, final boolean isMenuSelected, final String name, final Color color, final boolean isSelected, final ChannelType type, final int volumeValue, final String volumeText, final int panValue, final String panText, final int vuValue, final boolean isMute, final boolean isSolo, final boolean isArm, final int crossfadeMode)
    {
        super (menuName, isMenuSelected, name, color, isSelected, type);

        this.editType = editType;
        this.volumeValue = volumeValue;
        this.volumeText = volumeText;
        this.panValue = panValue;
        this.panText = panText;
        this.vuValue = vuValue;
        this.isMute = isMute;
        this.isSolo = isSolo;
        this.isArm = isArm;
        this.crossfadeMode = crossfadeMode;
    }


    /** {@inheritDoc} */
    @Override
    public void draw (final Graphics2D gc, final int left, final int width, final int height, final LayoutSettings layoutSettings) throws IOException
    {
        final int halfWidth = width / 2;

        final int trackRowTop = height - TRACK_ROW_HEIGHT - UNIT - SEPARATOR_SIZE;

        final int controlWidth = halfWidth - HALF_UNIT - HALF_UNIT / 2;
        final int controlStart = left + halfWidth + HALF_UNIT - HALF_UNIT / 2;

        final int panWidth = controlWidth - 2;
        final int panStart = controlStart + 1;
        final int panTop = CONTROLS_TOP + 1;
        final int panHeight = UNIT - SEPARATOR_SIZE;
        final int panTextTop = panTop + panHeight;

        final int faderOffset = controlWidth / 4;
        final int faderTop = panTop + panHeight + SEPARATOR_SIZE + 1;
        final int faderLeft = controlStart + SEPARATOR_SIZE + faderOffset;
        final int faderHeight = trackRowTop - faderTop - INSET + 1;
        final int faderInnerHeight = faderHeight - 2 * SEPARATOR_SIZE;

        final int volumeTextWidth = (int) (1.4 * controlWidth);
        final int volumeTextLeft = faderLeft - volumeTextWidth - 2;

        final int buttonHeight = (faderHeight - 4 * SEPARATOR_SIZE) / 3;

        //
        // Drawing
        //

        final Color textColor = layoutSettings.getTextColor ();
        this.drawMenu (gc, left, width, layoutSettings);

        final String name = this.getName ();
        // Element is off if the name is empty
        if (name == null || name.length () == 0)
            return;

        final Color backgroundColor = layoutSettings.getBackgroundColor ();
        this.drawTrackInfo (gc, left, width, height, trackRowTop, name, layoutSettings);

        // Draw the background
        gc.setColor (this.isSelected () ? backgroundColor.brighter () : backgroundColor);
        gc.fillRect (left, MENU_HEIGHT + 1, width, trackRowTop - (MENU_HEIGHT + 1));

        // Background of pan and slider area
        final Color borderColor = layoutSettings.getBorderColor ();
        gc.setColor (borderColor);
        gc.fillRect (controlStart, CONTROLS_TOP, halfWidth - UNIT + HALF_UNIT / 2 + 1, UNIT);
        gc.fillRect (controlStart, faderTop, controlWidth, faderHeight);

        final Color backgroundDarker = backgroundColor.darker ();
        final Color editColor = layoutSettings.getEditColor ();

        final ChannelType type = this.getType ();
        if (type != ChannelType.MASTER && type != ChannelType.LAYER && this.crossfadeMode != -1)
        {
            // Crossfader A|B
            final int crossWidth = controlWidth / 3;
            final Color selColor = this.editType == EDIT_TYPE_CROSSFADER || this.editType == EDIT_TYPE_ALL ? editColor : textColor;
            final BufferedImage crossfaderAIcon = SVGImage.getSVGImage ("/images/track/crossfade_a.svg", this.crossfadeMode == 0 ? selColor : backgroundDarker);
            gc.drawImage (crossfaderAIcon, left + INSET + (crossWidth - crossfaderAIcon.getWidth ()) / 2, CONTROLS_TOP + (panHeight - crossfaderAIcon.getHeight ()) / 2, null);
            final BufferedImage crossfaderABIcon = SVGImage.getSVGImage ("/images/track/crossfade_ab.svg", this.crossfadeMode == 1 ? selColor : backgroundDarker);
            gc.drawImage (crossfaderABIcon, crossWidth + left + INSET + (crossWidth - crossfaderAIcon.getWidth ()) / 2, CONTROLS_TOP + (panHeight - crossfaderAIcon.getHeight ()) / 2, null);
            final BufferedImage crossfaderBIcon = SVGImage.getSVGImage ("/images/track/crossfade_b.svg", this.crossfadeMode == 2 ? selColor : backgroundDarker);
            gc.drawImage (crossfaderBIcon, 2 * crossWidth + left + INSET + (crossWidth - crossfaderAIcon.getWidth ()) / 2, CONTROLS_TOP + (panHeight - crossfaderAIcon.getHeight ()) / 2, null);
        }

        // Panorama
        gc.setColor (backgroundDarker);
        gc.fillRect (panStart, panTop, panWidth, panHeight);
        gc.setColor (borderColor);
        final int panRange = panWidth / 2;
        final int panMiddle = panStart + panRange;
        gc.drawLine (panMiddle, panTop, panMiddle, panTop + panHeight);
        final double maxValue = getMaxValue ();
        final double halfMax = maxValue / 2;
        final Color faderColor = layoutSettings.getFaderColor ();
        gc.setColor (faderColor);
        final boolean isPanTouched = this.panText.length () > 0;
        if (this.panValue > halfMax)
        {
            // Panned to the right
            final int v = (int) ((this.panValue - halfMax) * panRange / halfMax);
            gc.fillRect (panMiddle + 1, CONTROLS_TOP + 1, v, panHeight);
            if (this.editType == EDIT_TYPE_PAN || this.editType == EDIT_TYPE_ALL)
            {
                gc.setColor (editColor);
                final int w = isPanTouched ? 3 : 1;
                gc.fillRect (Math.min (panMiddle + panRange - w, panMiddle + v), CONTROLS_TOP + 1, w, panHeight);

            }
        }
        else
        {
            // Panned to the left
            final int v = (int) (panRange - this.panValue * panRange / halfMax);
            gc.fillRect (panMiddle - v, CONTROLS_TOP + 1, v, panHeight);
            if (this.editType == EDIT_TYPE_PAN || this.editType == EDIT_TYPE_ALL)
            {
                gc.setColor (editColor);
                final int w = isPanTouched ? 3 : 1;
                gc.fillRect (Math.max (panMiddle - panRange, panMiddle - v), CONTROLS_TOP + 1, w, panHeight);

            }
        }

        // Volume slider
        // Ensure that maximum value is reached even if rounding errors happen
        final int volumeWidth = controlWidth - 2 * SEPARATOR_SIZE - faderOffset;
        final int volumeHeight = (int) (this.volumeValue >= maxValue - 1 ? faderInnerHeight : faderInnerHeight * this.volumeValue / maxValue);
        final int volumeOffset = faderInnerHeight - volumeHeight;
        final int volumeTop = faderTop + SEPARATOR_SIZE + volumeOffset;
        gc.setColor (faderColor);
        gc.fillRect (faderLeft, volumeTop, volumeWidth, volumeHeight);
        final boolean isVolumeTouched = this.volumeText.length () > 0;
        if (this.editType == EDIT_TYPE_VOLUME || this.editType == EDIT_TYPE_ALL)
        {
            gc.setColor (editColor);
            final int h = isVolumeTouched ? 3 : 1;
            gc.fillRect (faderLeft, Math.min (volumeTop + volumeHeight - h, volumeTop), volumeWidth, h);
        }

        // VU
        gc.setColor (backgroundDarker);
        final int vuHeight = (int) (this.vuValue >= maxValue - 1 ? faderInnerHeight : faderInnerHeight * this.vuValue / maxValue);
        final int vuOffset = faderInnerHeight - vuHeight;
        gc.fillRect (controlStart + SEPARATOR_SIZE, faderTop + SEPARATOR_SIZE, faderOffset - SEPARATOR_SIZE, faderInnerHeight);
        gc.setColor (layoutSettings.getVuColor ());
        gc.fillRect (controlStart + SEPARATOR_SIZE, faderTop + SEPARATOR_SIZE + vuOffset, faderOffset - SEPARATOR_SIZE, vuHeight);

        int buttonTop = faderTop;

        if (type != ChannelType.LAYER)
        {
            // Rec Arm
            drawButton (gc, left + INSET - 1, buttonTop, controlWidth - 1, buttonHeight - 1, backgroundColor, Color.RED, textColor, this.isArm, "/images/channel/record_arm.svg", layoutSettings);
        }

        // Solo
        buttonTop += buttonHeight + 2 * SEPARATOR_SIZE;
        drawButton (gc, left + INSET - 1, buttonTop, controlWidth - 1, buttonHeight - 1, backgroundColor, Color.YELLOW, textColor, this.isSolo, "/images/channel/solo.svg", layoutSettings);

        // Mute
        buttonTop += buttonHeight + 2 * SEPARATOR_SIZE;
        drawButton (gc, left + INSET - 1, buttonTop, controlWidth - 1, buttonHeight - 1, backgroundColor, new Color (245, 129, 17), textColor, this.isMute, "/images/channel/mute.svg", layoutSettings);

        // Draw panorama text on top if set
        if (isPanTouched)
        {
            gc.setColor (backgroundDarker);
            gc.fillRect (controlStart, panTextTop, controlWidth, UNIT);
            gc.setColor (borderColor);
            gc.drawRect (controlStart, panTextTop, controlWidth - 1, UNIT);
            gc.setFont (layoutSettings.getTextFont (UNIT));
            gc.setColor (textColor);
            drawTextInBounds (gc, this.panText, controlStart, panTextTop, controlWidth, UNIT, Label.CENTER);
        }

        // Draw volume text on top if set
        if (isVolumeTouched)
        {
            final int volumeTextTop = this.volumeValue >= maxValue - 1 ? faderTop : Math.min (volumeTop - 1, faderTop + faderInnerHeight + SEPARATOR_SIZE - UNIT + 1);
            gc.setColor (backgroundDarker);
            gc.fillRect (volumeTextLeft, volumeTextTop, volumeTextWidth, UNIT);
            gc.setColor (borderColor);
            gc.drawRect (volumeTextLeft, volumeTextTop, volumeTextWidth - 1, UNIT);
            gc.setFont (layoutSettings.getTextFont (UNIT));
            gc.setColor (textColor);
            drawTextInBounds (gc, this.volumeText, volumeTextLeft, volumeTextTop, volumeTextWidth, UNIT, Label.CENTER);
        }
    }


    /**
     * Draws a button a gradient background.
     *
     * @param gc The graphics context
     * @param left The left bound of the drawing area
     * @param top The top bound of the drawing area
     * @param width The width of the drawing area
     * @param height The height of the drawing area
     * @param backgroundColor The background color
     * @param isOnColor The color if the button is on
     * @param textColor The color of the buttons text
     * @param isOn True if the button is on
     * @param iconName The name of the buttons icon
     * @param layoutSettings The layout settings
     * @throws IOException Could not load a SVG image
     */
    private static void drawButton (final Graphics2D gc, final int left, final int top, final int width, final int height, final Color backgroundColor, final Color isOnColor, final Color textColor, final boolean isOn, final String iconName, final LayoutSettings layoutSettings) throws IOException
    {
        final Color borderColor = layoutSettings.getBorderColor ();

        gc.setColor (borderColor);
        gc.drawRoundRect (left, top, width, height, 5, 5);

        if (isOn)
        {
            gc.setColor (isOnColor);
            gc.fillRoundRect (left + 1, top + 1, width - 1, height - 1, 5, 5);
        }
        else
        {
            final Color brighter = backgroundColor.brighter ();
            gc.setColor (brighter.brighter ());
            gc.drawRoundRect (left + 1, top + 1, width - 2, height - 2, 5, 5);

            final Paint oldPaint = gc.getPaint ();
            final GradientPaint gp = new GradientPaint (left, top + 1, backgroundColor, left, top + height, brighter);
            gc.setPaint (gp);
            gc.fillRoundRect (left + 2, top + 2, width - 2, height - 2, 5, 5);
            gc.setPaint (oldPaint);
        }

        final BufferedImage icon = SVGImage.getSVGImage (iconName, isOn ? borderColor : textColor);
        gc.drawImage (icon, left + (width - icon.getWidth ()) / 2, top + (height - icon.getHeight ()) / 2, null);
    }
}
