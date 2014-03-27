package cz.muni.fi.pv168.warehouse.managers;

import cz.muni.fi.pv168.warehouse.entities.Item;
import cz.muni.fi.pv168.warehouse.entities.Shelf;
import cz.muni.fi.pv168.warehouse.exceptions.MethodFailureException;

import java.util.List;

/**
 * Class containing methods for managing warehouse.
 *
 * @author Oliver Mrázik & Martin Zaťko
 * @version 0.1
 */
public interface WarehouseManager {

    ///**
    // * Method will find shelf with specified item.
    // * @param item specified item.
    // * @return required shelf
    // */
    //Shelf findShelfWithItem(Item item) throws MethodFailureException;

    /**
     * Method will list all items on specified shelf.
     * @param shelf specified shelf.
     * @return list of required items.
     */
    List<Item> listAllItemsOnShelf(Shelf shelf) throws MethodFailureException ;

    /**
     * Method puts item on a shelf.
     * @param shelf specified shelf.
     * @param item specified item.
     */
    void putItemOnShelf(Shelf shelf, Item item) throws MethodFailureException ;

    /**
     * Method withdraw item from a shelf.
     // * @param shelf specified shelf.
     * @param item specified item.
     */
    Item withdrawItemFromShelf(Shelf shelf, Item item) throws MethodFailureException ;

    /**
     * Method will remove all expired items from database.
     * @return list of removed items.
     */
    List<Item> removeAllExpiredItems() throws MethodFailureException ;
}