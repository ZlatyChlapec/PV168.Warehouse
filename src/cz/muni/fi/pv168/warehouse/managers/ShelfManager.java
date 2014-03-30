package cz.muni.fi.pv168.warehouse.managers;

import cz.muni.fi.pv168.warehouse.entities.Shelf;
import cz.muni.fi.pv168.warehouse.exceptions.MethodFailureException;

import java.util.List;

/**
 * Class containing methods for managing shelves.
 * @author Oliver Mrázik & Martin Zaťko
 * @version 2014-03-16
 */
public interface ShelfManager {
    /**
     * Method will create shelf.
     * @param shelf shelf we want to create.
     * @return created shelf.
     */
    Shelf createShelf(Shelf shelf) throws MethodFailureException;

    /**
     * Method will delete shelf.
     * @param shelf shelf we want to delete.
     */
    Shelf deleteShelf(Shelf shelf) throws MethodFailureException;

    /**
     * Method lists all shelves.
     * @return the list of all shelves.
     */
    List<Shelf> listAllShelves() throws MethodFailureException;

    /**
     * Method will find shelf by id.
     * @param id id of a shelf we want to find
     * @return the searched shelf.
     */
    Shelf findShelfById(int id) throws MethodFailureException;

    /**
     * Method will update existing shelf.
     * @param shelf shelf we want to update.
     */
    void updateShelf(Shelf shelf) throws MethodFailureException;

}
