package push22bitwig;

import push22bitwig.util.FontCache;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;

import java.awt.Color;
import java.awt.Font;


/**
 * Manages the settings of the layout (color and fonts).
 *
 * Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class LayoutSettings
{
    private static final Color                    DEFAULT_COLOR_TEXT       = new Color (0xF2, 0xF2, 0xF2);
    private static final Color                    DEFAULT_COLOR_BACKGROUND = new Color (0x4D, 0x4D, 0x4D);
    private static final Color                    DEFAULT_COLOR_BORDER     = Color.BLACK;
    private static final Color                    DEFAULT_COLOR_FADER      = new Color (83, 58, 33);
    private static final Color                    DEFAULT_COLOR_VU         = Color.GREEN;
    private static final Color                    DEFAULT_COLOR_EDIT       = new Color (240, 127, 17);

    private final SimpleObjectProperty<FontCache> textFontProperty         = new SimpleObjectProperty<> (new FontCache ());
    private final SimpleObjectProperty<Color>     textColorProperty        = new SimpleObjectProperty<> (DEFAULT_COLOR_TEXT);
    private final SimpleObjectProperty<Color>     backgroundColorProperty  = new SimpleObjectProperty<> (DEFAULT_COLOR_BACKGROUND);
    private final SimpleObjectProperty<Color>     borderColorProperty      = new SimpleObjectProperty<> (DEFAULT_COLOR_BORDER);
    private final SimpleObjectProperty<Color>     faderColorProperty       = new SimpleObjectProperty<> (DEFAULT_COLOR_FADER);
    private final SimpleObjectProperty<Color>     vuColorProperty          = new SimpleObjectProperty<> (DEFAULT_COLOR_VU);
    private final SimpleObjectProperty<Color>     editColorProperty        = new SimpleObjectProperty<> (DEFAULT_COLOR_EDIT);


    /**
     * Set a new font.
     *
     * @param fontName The name of the font
     */
    public void setTextFont (final String fontName)
    {
        this.textFontProperty.set (new FontCache (fontName));
    }


    /**
     * Get the currently selected font.
     *
     * @return The font
     */
    public Font getTextFont ()
    {
        return this.textFontProperty.get ().getBaseFont ();
    }


    /**
     * Get the currently selected font with the preferred size.
     *
     * @param size The size of the font
     * @return The font
     */
    public Font getTextFont (final int size)
    {
        return this.textFontProperty.get ().getFont (size);
    }


    /**
     * Set a new color for the text.
     *
     * @param textColor The new color
     */
    public void setTextColor (final Color textColor)
    {
        this.textColorProperty.set (textColor);
    }


    /**
     * Get the current color for the text.
     *
     * @return The color
     */
    public Color getTextColor ()
    {
        return this.textColorProperty.get ();
    }


    /**
     * Set a new background color for the text.
     *
     * @param backgroundColor The new color
     */
    public void setBackgroundColor (final Color backgroundColor)
    {
        this.backgroundColorProperty.set (backgroundColor);
    }


    /**
     * Get the current background color for the text.
     *
     * @return The color
     */
    public Color getBackgroundColor ()
    {
        return this.backgroundColorProperty.get ();
    }


    /**
     * Set a new border color for the text.
     *
     * @param borderColor The new color
     */
    public void setBorderColor (final Color borderColor)
    {
        this.borderColorProperty.set (borderColor);
    }


    /**
     * Get the current border color for the text.
     *
     * @return The color
     */
    public Color getBorderColor ()
    {
        return this.borderColorProperty.get ();
    }


    /**
     * Set a new fader color for the text.
     *
     * @param faderColor The new color
     */
    public void setFaderColor (final Color faderColor)
    {
        this.faderColorProperty.set (faderColor);
    }


    /**
     * Get the current fader color for the text.
     *
     * @return The color
     */
    public Color getFaderColor ()
    {
        return this.faderColorProperty.get ();
    }


    /**
     * Set a new VU color for the text.
     *
     * @param vuColor The new color
     */
    public void setVuColor (final Color vuColor)
    {
        this.vuColorProperty.set (vuColor);
    }


    /**
     * Get the current VU color for the text.
     *
     * @return The color
     */
    public Color getVuColor ()
    {
        return this.vuColorProperty.get ();
    }


    /**
     * Set a new edit color for the text.
     *
     * @param editColor The new color
     */
    public void setEditColor (final Color editColor)
    {
        this.editColorProperty.set (editColor);
    }


    /**
     * Get the current edit color for the text.
     *
     * @return The color
     */
    public Color getEditColor ()
    {
        return this.editColorProperty.get ();
    }


    /**
     * Reset the font and color settings.
     */
    public void reset ()
    {
        this.textFontProperty.set (new FontCache ());
        this.textColorProperty.set (DEFAULT_COLOR_TEXT);
        this.backgroundColorProperty.set (DEFAULT_COLOR_BACKGROUND);
        this.borderColorProperty.set (DEFAULT_COLOR_BORDER);
        this.faderColorProperty.set (DEFAULT_COLOR_FADER);
        this.vuColorProperty.set (DEFAULT_COLOR_VU);
        this.editColorProperty.set (DEFAULT_COLOR_EDIT);
    }


    /**
     * Get the text font property.
     *
     * @return The property
     */
    public SimpleObjectProperty<FontCache> getTextFontProperty ()
    {
        return this.textFontProperty;
    }


    /**
     * Get the background color property.
     *
     * @return The property
     */
    public SimpleObjectProperty<Color> getBackgroundColorProperty ()
    {
        return this.backgroundColorProperty;
    }


    /**
     * Get the text color property.
     *
     * @return The property
     */
    public SimpleObjectProperty<Color> getTextColorProperty ()
    {
        return this.textColorProperty;
    }


    /**
     * Get the border color property.
     *
     * @return The property
     */
    public SimpleObjectProperty<Color> getBorderColorProperty ()
    {
        return this.borderColorProperty;
    }


    /**
     * Get the fader color property.
     *
     * @return The property
     */
    public SimpleObjectProperty<Color> getFaderColorProperty ()
    {
        return this.faderColorProperty;
    }


    /**
     * Get the VU color property.
     *
     * @return The property
     */
    public SimpleObjectProperty<Color> getVUColorProperty ()
    {
        return this.vuColorProperty;
    }


    /**
     * Get the edit color property.
     *
     * @return The property
     */
    public SimpleObjectProperty<Color> getEditColorProperty ()
    {
        return this.editColorProperty;
    }


    /**
     * Add a listener which gets informed if a layout font changes.
     *
     * @param listener The listener to register
     */
    public void addFontChangeListener (final ChangeListener<FontCache> listener)
    {
        this.textFontProperty.addListener (listener);
    }


    /**
     * Add a listener which gets informed if any color of the layout changes.
     *
     * @param listener The listener to register
     */
    public void addColorChangeListener (final ChangeListener<Color> listener)
    {
        this.textColorProperty.addListener (listener);
        this.backgroundColorProperty.addListener (listener);
        this.borderColorProperty.addListener (listener);
        this.faderColorProperty.addListener (listener);
        this.vuColorProperty.addListener (listener);
        this.editColorProperty.addListener (listener);
    }
}
