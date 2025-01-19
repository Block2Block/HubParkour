package me.block2block.hubparkour.api.gui;

import me.block2block.hubparkour.api.gui.exception.InvalidColumnException;
import me.block2block.hubparkour.api.gui.exception.InvalidRowException;

import java.util.HashMap;
import java.util.Map;

/**
 * The GUIRow class represents a row in a GUI, where each row is identified by a unique row number
 * and can contain a specified number of GUIItems. It enforces row and column constraints to ensure
 * proper initialization and manipulation within a GUI grid structure.
 */
public class GUIRow {

    private final Map<Integer, GUIItem> row;
    private final int rowNo;

    /**
     * Constructs a new instance of {@code GUIRow} with a specified row number.
     * Ensures that the row number provided is within a valid range.
     *
     * @param rowNo The row number to initialize. Must be between 0 and 5 inclusive.
     * @throws InvalidRowException If the provided row number is not within the range 0-5.
     */
    public GUIRow(int rowNo) {
        row = new HashMap<>();
        if (rowNo > 5 || rowNo < 0) {
            throw new InvalidRowException("row number must be between 0 and 5, gave:" + rowNo);
        }
        this.rowNo = rowNo;
    }

    /**
     * Sets a {@link GUIItem} at the specified column index within the row.
     *
     * @param i The column index where the {@code GUIItem} should be placed.
     *          Must be between 0 and 8 inclusive.
     * @param item The {@code GUIItem} to be placed in the specified column.
     * @throws InvalidColumnException If the specified column index is not within the range 0-8.
     */
    public void setItem(int i, GUIItem item) {
        if (i > 8 || i < 0) {
            throw new InvalidColumnException("column number must be between 0 and 8, gave:" + i);
        }

        row.put(i, item);
    }

    /**
     * Retrieves the row number associated with this instance of {@code GUIRow}.
     *
     * @return The row number, which is a value between 0 and 5 inclusive, representing the position
     *         of the row within the grid structure.
     */
    @SuppressWarnings("unused")
    public int getRowNo() {
        return rowNo;
    }

    /**
     * Retrieves a copy of the row map, representing the mapping of column indices to {@link GUIItem}
     * objects within this row.
     * The returned map ensures the original data remains unmodifiable from outside the instance.
     *
     * @return A new map containing the column indices as keys and their
     *         respective {@code GUIItem} objects as values. The map may
     *         be empty if no items have been added to the row.
     */
    @SuppressWarnings("unused")
    public Map<Integer, GUIItem> getRow() {
        return new HashMap<>(row);
    }

    /**
     * Retrieves the {@link GUIItem} located at the specified column index within this row.
     *
     * @param column The column index to retrieve the {@code GUIItem} from.
     *               Typically expected to be between 0 and 8 inclusive.
     * @return The {@link GUIItem} at the specified column, or {@code null} if no item exists
     *         at the given index.
     */
    public GUIItem getItem(int column) {
        return row.get(column);
    }

}
