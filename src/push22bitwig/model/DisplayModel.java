package push22bitwig.model;

import push22bitwig.model.grid.GridElement;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;


/**
 * Contains the data for the display content.
 *
 * Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class DisplayModel
{
    private final SimpleStringProperty        logMessage     = new SimpleStringProperty ();
    private final SimpleBooleanProperty       shutdownSignal = new SimpleBooleanProperty ();
    private final ObservableList<GridElement> gridElements   = FXCollections.observableArrayList (new ArrayList<> (8));


    /**
     * Adds a listener for grid element changes
     *
     * @param listener A listener
     */
    public void addGridElementChangeListener (final ListChangeListener<? super GridElement> listener)
    {
        this.gridElements.addListener (listener);
    }


    /**
     * Add a listener for a shutdown request.
     *
     * @param listener A listener
     */
    public void addShutdownListener (final ChangeListener<? super Boolean> listener)
    {
        this.shutdownSignal.addListener (listener);
    }


    /**
     * Get the log text message property.
     *
     * @return The property
     */
    public SimpleStringProperty getLogMessageProperty ()
    {
        return this.logMessage;
    }


    /**
     * Adds a logging message.
     *
     * @param message The message to add
     */
    public synchronized void addLogMessage (final String message)
    {
        Platform.runLater ( () -> {
            final String string = this.logMessage.get ();
            final StringBuilder sb = new StringBuilder ();
            if (string != null)
                sb.append (string);
            this.logMessage.set (sb.append (message).append ('\n').toString ());
            System.out.println (message);
        });
    }


    /**
     * Signal shutdown.
     */
    public void shutdown ()
    {
        this.shutdownSignal.set (true);
    }


    /**
     * Sets the grid elements.
     *
     * @param elements The elements to set
     */
    public void setGridElements (final List<GridElement> elements)
    {
        this.gridElements.setAll (elements);
    }


    /**
     * Get the grid elements.
     *
     * @return The elemtens
     */
    public List<GridElement> getGridElements ()
    {
        return new ArrayList<GridElement> (this.gridElements);
    }
}
