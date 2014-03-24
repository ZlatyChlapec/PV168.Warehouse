package cz.muni.fi.pv168.warehouse.entities;

/**
 * This class represent shelf.
 *
 * @author Oliver Mrázik & Martin Zaťko
 * @version 0.1
 */
public class Shelf {

    private Integer id;
    private int column;
    private int row;
    private double maxWeight;
    private int capacity;
    private boolean secure;

    /**
     * Empty constructor.
     */
    public Shelf() {

    }

//    /**
//     * Constructor with all parameters. Creates new shelf.
//     *
//     * @param id Unique id of shelf.
//     * @param column Specifies the column in which it is located.
//     * @param row Specifies the row in which it is located.
//     * @param maxWeight Specifies maximum load of a shelf.
//     * @param capacity Specifies number of items that can be stored.
//     * @param secure Specifies if shelf can store dangerous items.
//     */
//    public Shelf(Integer id, int column, int row, double maxWeight, int capacity, boolean secure) {
//        this.id = id;
//        this.column = column;
//        this.row = row;
//        this.maxWeight = maxWeight;
//        this.capacity = capacity;
//        this.secure = secure;
//    }

    /**
     * Method returns id of a shelf.
     * @return id.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Method sets id of a shelf.
     * @param id unique id.
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Method returns column of a shelf.
     * @return number of a column.
     */
    public int getColumn() {
        return column;
    }

    /**
     * Method sets column of a shelf.
     * @param column column in which it is located.
     */
    public void setColumn(int column) {
        this.column = column;
    }

    /**
     * Method returns row of a shelf.
     * @return number of a row.
     */
    public int getRow() {
        return row;
    }

    /**
     * Method sets row of a shelf.
     * @param row row in which it is located.
     */
    public void setRow(int row) {
        this.row = row;
    }

    /**
     * Method returns maximum weight of a shelf.
     * @return maximum weight.
     */
    public double getMaxWeight() {
        return maxWeight;
    }

    /**
     * Method sets maximum weight of a shelf.
     * @param maxWeight maximum load.
     */
    public void setMaxWeight(double maxWeight) {
        this.maxWeight = maxWeight;
    }

    /**
     * Method returns capacity of a shelf.
     * @return maximum capacity.
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Method sets capacity of a shelf.
     * @param capacity maximum number of items.
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * Method returns if shelf is secure or not.
     * @return returns true if shelf is secured
     *                 false if shelf is not secured.
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * Method sets if shelf is secured or not.
     * @param secure true if is secured
     *               false if is not secured.
     */
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    @Override
    public String toString() {
        return "id = "+ id +"; column = "+ column +"; row = "+ row +"; maxWeight = "+
                maxWeight +"; capacity = "+ capacity +"; secure = "+ secure +";";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Shelf)) return false;

        Shelf shelf = (Shelf) o;

        //if (!id.equals(shelf.id)) return false;

        return id.equals(shelf.id);
    }

    @Override
    public int hashCode() {
        return id;
    }
}
