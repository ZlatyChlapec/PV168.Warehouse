package cz.muni.fi.pv168.warehouse.managers;

import cz.muni.fi.pv168.warehouse.entities.Item;
import cz.muni.fi.pv168.warehouse.entities.Shelf;
import cz.muni.fi.pv168.warehouse.exceptions.MethodFailureException;
import cz.muni.fi.pv168.warehouse.exceptions.ShelfAttributeException;

import java.util.Date;
import java.util.List;

/**
 * Class containing methods for managing warehouse.
 * @author Oliver Mrázik & Martin Zaťko
 * @version 2014-03-30
 */
public interface WarehouseManager {

    /**
    * Method will find shelf with specified item.
    * @param item specified item.
    * @return required shelf.
    * @throws MethodFailureException when database operation fails.
    */
    Shelf findShelfWithItem(Item item) throws MethodFailureException;

    /**
     * Method will list all items on specified shelf.
     * @param shelf specified shelf.
     * @return list of required items.
     * @throws MethodFailureException when database operation fails.
     */
    List<Item> listAllItemsOnShelf(Shelf shelf) throws MethodFailureException;

    /**
     * Method puts item on a shelf.
     * @param shelf specified shelf.
     * @param item specified item.
     * @throws MethodFailureException when database operation fails.
     * @throws ShelfAttributeException when input shelf have wrong attributes.
     */
    void putItemOnShelf(Shelf shelf, Item item) throws MethodFailureException, ShelfAttributeException;

    /**
     * Method withdraw item from a shelf.
     * @param item specified item.
     * @throws MethodFailureException when database operation fails.
     */
    Item withdrawItemFromShelf(Item item) throws MethodFailureException;

    /**
     * Method will remove all expired items from database.
     * @return list of removed items.
     * @throws MethodFailureException when database operation fails.
     */
    List<Item> removeAllExpiredItems(Date currentDate) throws MethodFailureException;

    /**
     * Method will return list of shelves with free space.
     * @return list of shelves with free space.
     * @throws MethodFailureException when database operation fails.
     */
    List<Shelf> listShelvesWithSomeFreeSpace() throws MethodFailureException;

    /**
     * Method will return all items which are not stored on a shelf.
     * @return list of items without shelf.
     * @throws MethodFailureException when database operation fails.
     */
    List<Item> listAllItemsWithoutShelf() throws MethodFailureException;
}