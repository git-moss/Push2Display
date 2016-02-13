package push22bitwig.model.grid;

import push22bitwig.LayoutSettings;

import javafx.util.Pair;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Label;
import java.util.ArrayList;
import java.util.List;


/**
 * An element in the grid which contains several text items. Each item can be selected.
 *
 * Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class ListGridElement extends AbstractGridElement
{
    private final List<Pair<String, Boolean>> items = new ArrayList<> (6);


    /**
     * Constructor.
     *
     * @param items The list items
     */
    public ListGridElement (final List<Pair<String, Boolean>> items)
    {
        super (null, false, null, null, null, false);
        this.items.addAll (items);
    }


    /** {@inheritDoc} */
    @Override
    public void draw (final Graphics2D gc, final int left, final int width, final int height, final LayoutSettings layoutSettings)
    {
        final int size = this.items.size ();
        final int itemHeight = DISPLAY_HEIGHT / size;

        final Color textColor = layoutSettings.getTextColor ();
        final Color borderColor = layoutSettings.getBorderColor ();

        for (int i = 0; i < size; i++)
        {
            final Pair<String, Boolean> item = this.items.get (i);
            final boolean isSelected = item.getValue ().booleanValue ();
            final int itemLeft = left + SEPARATOR_SIZE;
            final int itemTop = i * itemHeight;
            final int itemWidth = width - SEPARATOR_SIZE;

            gc.setColor (isSelected ? textColor : borderColor);
            gc.fillRect (itemLeft, itemTop + SEPARATOR_SIZE, itemWidth, itemHeight - 2 * SEPARATOR_SIZE);

            gc.setColor (isSelected ? borderColor : textColor);
            gc.setFont (layoutSettings.getTextFont (itemHeight / 2));
            drawTextInBounds (gc, item.getKey (), itemLeft + INSET, itemTop, itemWidth - 2 * INSET, itemHeight, Label.LEFT);
        }
    }
}
