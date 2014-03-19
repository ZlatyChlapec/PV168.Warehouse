package cz.muni.fi.pv168.warehouse.managers;

import cz.muni.fi.pv168.warehouse.entities.Item;
import cz.muni.fi.pv168.warehouse.entities.Shelf;

import java.util.List;

/**
 * Class will serve to manage warehouse.
 *
 * @author Oliver Mrázik & Martin Zaťko
 * @version 0.1
 */
public class WarehouseManagerImpl implements WarehouseManager{
    @Override
    public Shelf findShelfWithItem(Item item) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Item> listAllItemsOnShelf(Shelf shelf) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void putItemOnShelf(Shelf shelf, Item item) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Item withdrawItemFromShelf(Shelf shelf, Item item) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Item> removeAllExpiredItems() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}