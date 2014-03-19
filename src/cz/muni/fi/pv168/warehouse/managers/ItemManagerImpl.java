package cz.muni.fi.pv168.warehouse.managers;

import cz.muni.fi.pv168.warehouse.entities.Item;

import java.util.Date;
import java.util.List;

/**
 * Class will serve to manage items.
 *
 * @author Oliver Mrázik & Martin Zaťko
 * @version 0.1
 */
public class ItemManagerImpl implements ItemManager {
    @Override
    public Item createItem(Item item) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Item deleteItem(Item item) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Item> listAllItems() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Item findItemById(int id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Item updateItem(Item item) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Date getExpiration(Item item) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
