package cz.muni.fi.pv168.warehouse.entities;

import java.util.Date;

/**
 * This class represent item.
 * @author Oliver Mrázik & Martin Zaťko
 * @version 2014-03-30
 */
public class Item {
    private Integer id;
    private double weight;
    private Date insertionDate;
    private int storeDays;
    private boolean dangerous;

    /**
     * Empty constructor.
     */
    public Item() {
    }

    /**
     * Method returns id of an item.
     * @return id.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Method sets unique id of item.
     * @param id unique id.
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Method returns weight of an item.
     * @return weight.
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Method sets weight of an item.
     * @param weight weight.
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    /**
     * Method returns date of insertionDate of an item.
     * @return date of insertionDate.
     */
    public Date getInsertionDate() {
        return insertionDate;
    }

    /**
     * Method sets date of insertionDate of an item.
     * @param insertionDate date of insertionDate.
     */
    public void setInsertionDate(Date insertionDate) {
        this.insertionDate = insertionDate;
    }

    /**
     * Method returns number of days to store an item.
     * @return days in the storage.
     */
    public int getStoreDays() {
        return storeDays;
    }

    /**
     * Method sets number of days to store an item.
     * @param storeDays days in the storage.
     */
    public void setStoreDays(int storeDays) {
        this.storeDays = storeDays;
    }

    /**
     * Method returns if item is dangerous or not.
     * @return true if item is dangerous
     *         false if item is not dangerous.
     */
    public boolean isDangerous() {
        return dangerous;
    }

    /**
     * Method sets if item is dangerous or not.
     * @param dangerous true if item is dangerous
     *                  false if item is not dangerous.
     */
    public void setDangerous(boolean dangerous) {
        this.dangerous = dangerous;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;

        Item item = (Item) o;

        return id.equals(item.id);
    }

    @Override
    public int hashCode() {
        int prime = 13;
        return prime * id;
    }
}
