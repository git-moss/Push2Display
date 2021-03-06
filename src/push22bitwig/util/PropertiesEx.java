package push22bitwig.util;

import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.Window;


/**
 * A little bit improved properties file.
 *
 * Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class PropertiesEx extends java.util.Properties
{
    private static final long   serialVersionUID = -739562636951242581L;

    private static final String MAIN             = "Main";
    private static final String WINDOW           = "Window";
    private static final char   H                = 'H';
    private static final char   W                = 'W';
    private static final char   Y                = 'Y';
    private static final char   X                = 'X';
    private static final String MAXIMIZED        = "Maximized";


    /**
     * Read a string-property. Returns null if the property was not found or if the found string has
     * a length of 0.
     *
     * @param key The key of the string-property
     * @return The value of the string-property
     */
    public String getString (final String key)
    {
        return this.getString (key, null);
    }


    /**
     * Read a string-property. If the found string has a length of 0, null is returned.
     *
     * @param key The key of the string-property
     * @param defaultValue Value that is returned if the property was not found
     * @return The value of the string-property
     */
    public String getString (final String key, final String defaultValue)
    {
        final String value = this.getProperty (key, defaultValue);
        return value == null || value.length () == 0 ? null : value;
    }


    /**
     * Writes a string-property.
     *
     * @param key Id of the property
     * @param value The value to set
     */
    public void putString (final String key, final String value)
    {
        this.put (key, value);
    }


    /**
     * Read an integer-property. Returns '-1' if the key is not present.
     *
     * @param key Id of the property
     * @return The value of the property
     */
    public int getInt (final String key)
    {
        return this.getInt (key, -1);
    }


    /**
     * Read an integer-property.
     *
     * @param key Id of the property
     * @param defaultValue Value that is returned if the property was not found
     * @return The value of the property
     */
    public int getInt (final String key, final int defaultValue)
    {
        return Integer.parseInt (this.getProperty (key, Integer.toString (defaultValue)));
    }


    /**
     * Writes an integer-property.
     *
     * @param key Id of the property
     * @param value The value to set
     */
    public void putInt (final String key, final int value)
    {
        this.put (key, Integer.toString (value));
    }


    /**
     * Read a long-property. Returns '-1' if the key is not present.
     *
     * @param key Id of the property
     * @return The value of the property
     */
    public long getLong (final String key)
    {
        return this.getLong (key, -1L);
    }


    /**
     * Read a long-property.
     *
     * @param key Id of the property
     * @param defaultValue Value that is returned if the property was not found
     * @return The value of the property
     */
    public long getLong (final String key, final long defaultValue)
    {
        return Long.parseLong (this.getProperty (key, Long.toString (defaultValue)));
    }


    /**
     * Read a double-property. Returns '0' if the key is not present.
     *
     * @param key Id of the property
     * @return The value of the property
     */
    public double getDouble (final String key)
    {
        return this.getDouble (key, 0);
    }


    /**
     * Read a double-property.
     *
     * @param key Id of the property
     * @param defaultValue Value that is returned if the property was not found
     * @return The value of the property
     */
    public double getDouble (final String key, final double defaultValue)
    {
        return Double.parseDouble (this.getProperty (key, Double.toString (defaultValue)));
    }


    /**
     * Writes a double-property.
     *
     * @param key Id of the property
     * @param value The value to set
     */
    public void putDouble (final String key, final double value)
    {
        this.put (key, Double.toString (value));
    }


    /**
     * Reads a boolean-property. Defaults to false when the key is not present.
     *
     * @param key Id of the property
     * @return The value of the property
     */
    public boolean getBoolean (final String key)
    {
        return this.getBoolean (key, false);
    }


    /**
     * Reads a boolean-property. Defaults to false when the key is not present.
     *
     * @param key Id of the property
     * @param defaultValue Value that is returned if the property was not found
     * @return The value of the property
     */
    public boolean getBoolean (final String key, final boolean defaultValue)
    {
        return Boolean.parseBoolean (this.getProperty (key, Boolean.toString (defaultValue)));
    }


    /**
     * Writes a boolean-property.
     *
     * @param key Id of the property
     * @param value The value to set
     */
    public void putBoolean (final String key, final boolean value)
    {
        this.put (key, Boolean.toString (value));
    }


    /**
     * Reads an enumerated property.
     *
     * @param enumType The enumeration type of the property
     * @param key Id of the property
     * @param <T> The enumeration type
     * @return The enumeration constant of the property.
     */
    public <T extends Enum<T>> T getEnum (final Class<T> enumType, final String key)
    {
        return this.getEnum (enumType, key, null);
    }


    /**
     * Reads an enumerated property.
     *
     * @param enumType The enumeration type of the property
     * @param key Id of the property
     * @param defaultValue Value that is returned if the property was not found
     * @param <T> The enumeration type
     * @return The enumeration constant of the property.
     */
    public <T extends Enum<T>> T getEnum (final Class<T> enumType, final String key, final T defaultValue)
    {
        final String property = this.getProperty (key);
        return property == null ? defaultValue : Enum.valueOf (enumType, property);
    }


    /**
     * Writes an enumaration property.
     *
     * @param key Id of the property
     * @param value The value to set
     * @param <T> The enumeration type
     */
    public <T extends Enum<T>> void putEnum (final String key, final T value)
    {
        this.put (key, value.name ());
    }


    /**
     * Maps the specified key to the specified value in this properties. If the value is null the
     * key is removed from the properties.
     *
     * @param key the properties key.
     * @param value the value.
     * @return The previous value of the specified key or null if removed
     */
    @Override
    public synchronized Object put (final Object key, final Object value)
    {
        if (value != null && !(value instanceof String && ((String) value).length () == 0))
            return super.put (key, value);
        this.remove (key);
        return null;
    }


    /**
     * Stores the window position, dimension and state of an applications mainframe. Only stores the
     * information if the frame is visible.
     *
     * @param stage The main stage of an application
     */
    public void storeStagePlacement (final Stage stage)
    {
        this.putWindowPlacement (MAIN, -1, stage);
        this.putBoolean (MAIN + MAXIMIZED, stage.isMaximized ());
    }


    /**
     * Restores the window position and dimension of an applications main stage.
     *
     * @param stage The main stage of an application
     */
    public void restoreStagePlacement (final Stage stage)
    {
        this.restoreWindowPlacement (MAIN, stage);
        stage.setMaximized (this.getBoolean (MAIN + MAXIMIZED));
    }


    /**
     * Restores the window position and dimension.
     *
     * @param name The name of the window
     * @param window The window to restore
     */
    public void restoreWindowPlacement (final String name, final Window window)
    {
        this.restoreWindowPlacement (name, -1, window);
    }


    /**
     * Restores the window position and dimension.
     *
     * @param name The name of the window
     * @param id An identifier for the window
     * @param window The window to restore
     */
    public void restoreWindowPlacement (final String name, final int id, final Window window)
    {
        final Rectangle bounds = this.getWindowPlacement (name, id);
        if (bounds == null)
            return;
        window.setX (bounds.getX ());
        window.setY (bounds.getY ());
        window.setWidth (bounds.getWidth ());
        window.setHeight (bounds.getHeight ());
    }


    /**
     * Get the size of the main window.
     *
     * @param name The name of the window
     * @param id An additional index for the window, e.g. to count child windows
     * @return The size of the window
     */
    public Rectangle getWindowPlacement (final String name, final int id)
    {
        final String prop = buildPropertyName (name, id);
        final double width = this.getDouble (prop + W);
        final double height = this.getDouble (prop + H);
        return width <= 0 || height <= 0 ? null : new Rectangle (this.getDouble (prop + X), this.getDouble (prop + Y), width, height);
    }


    /**
     * Store the position and dimensions of a window.
     *
     * @param name The name of the window
     * @param window The window for which to store the dimensions
     */
    public void putWindowPlacement (final String name, final Window window)
    {
        this.putWindowPlacement (name, -1, window);
    }


    /**
     * Store the position and dimensions of a window.
     *
     * @param name The name of the window
     * @param id An identifier for the window
     * @param window The window for which to store the dimensions
     */
    public void putWindowPlacement (final String name, final int id, final Window window)
    {
        final String prop = buildPropertyName (name, id);
        this.putDouble (prop + X, window.getX ());
        this.putDouble (prop + Y, window.getY ());
        this.putDouble (prop + W, window.getWidth ());
        this.putDouble (prop + H, window.getHeight ());
    }


    /**
     * Builds a window property name from the given name and id.
     *
     * @param name A name
     * @param id An id
     * @return The property name
     */
    private static String buildPropertyName (final String name, final int id)
    {
        final StringBuilder propStr = new StringBuilder (name);
        if (id != -1)
            propStr.append (id);
        return propStr.append (WINDOW).toString ();
    }
}
