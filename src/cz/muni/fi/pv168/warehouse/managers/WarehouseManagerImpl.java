package cz.muni.fi.pv168.warehouse.managers;

import cz.muni.fi.pv168.warehouse.entities.Item;
import cz.muni.fi.pv168.warehouse.entities.Shelf;
import cz.muni.fi.pv168.warehouse.exceptions.MethodFailureException;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Class will serve to manage warehouse.
 *
 * @author Oliver Mrázik & Martin Zaťko
 * @version 0.1
 */
public class WarehouseManagerImpl implements WarehouseManager {

    private final static Logger logger = Logger.getLogger(WarehouseManagerImpl.class.getName());

    private DataSource ds;

    public WarehouseManagerImpl(DataSource ds) throws MethodFailureException {
        this.ds = ds;
    }

    //@Override
    //public Shelf findShelfWithItem(Item item) throws MethodFailureException {
    //    throw new UnsupportedOperationException("Not supported yet.");
    //}

    @Override
    public List<Item> listAllItemsOnShelf(Shelf shelf) throws MethodFailureException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void putItemOnShelf(Shelf shelf, Item item) throws MethodFailureException {

    }

    @Override
    public Item withdrawItemFromShelf(Item item) throws MethodFailureException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Item> removeAllExpiredItems(Date currentDate) throws MethodFailureException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Date currentDate() {
        return new Date();
    }
}