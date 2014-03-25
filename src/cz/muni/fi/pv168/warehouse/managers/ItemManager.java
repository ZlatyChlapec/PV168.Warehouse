package cz.muni.fi.pv168.warehouse.managers;

import cz.muni.fi.pv168.warehouse.entities.Item;
import cz.muni.fi.pv168.warehouse.exceptions.MethodFailureException;

import java.util.Date;
import java.util.List;

/**
 * Class containing methods for managing items.
 *
 * @author Oliver Mrázik & Martin Zaťko
 * @version 0.1
 */
public interface ItemManager {

    /**
     * Method will create item.
     *
     * @param item item we want to create.
     */
    void createItem(Item item) throws MethodFailureException;

    /**
     * Method will delete item.
     *
     * @param item item we want to delete.
     * @return deleted item.
     */
    Item deleteItem(Item item) throws MethodFailureException;

    /**
     * Method return list of all items.
     *
     * @return list of all items.
     */
    List<Item> listAllItems() throws MethodFailureException;

    /**
     * Method will find item by id.
     *
     * @param id id of item we want to find.
     * @return item with id we wanted.
     */
    Item findItemById(int id) throws MethodFailureException;

    /**
     * Method will update item.
     *
     * @param item item we want to update.
     * @return updated item.
     */
    Item updateItem(Item item) throws MethodFailureException;

    /**
     * Method calculate expiration date for given item.
     *
     * @param item item we want to get expiration date.
     * @return Date of give item.
     */
    Date getExpiration(Item item) throws MethodFailureException;
}