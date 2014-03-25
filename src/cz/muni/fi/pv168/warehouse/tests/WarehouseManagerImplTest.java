package cz.muni.fi.pv168.warehouse.tests;

import cz.muni.fi.pv168.warehouse.entities.Item;
import cz.muni.fi.pv168.warehouse.entities.Shelf;
import cz.muni.fi.pv168.warehouse.exceptions.ShelfCapacityException;
import cz.muni.fi.pv168.warehouse.exceptions.ShelfSecurityException;
import cz.muni.fi.pv168.warehouse.exceptions.ShelfWeightException;
import cz.muni.fi.pv168.warehouse.managers.ItemManagerImpl;
import cz.muni.fi.pv168.warehouse.managers.ShelfManagerImpl;
import cz.muni.fi.pv168.warehouse.managers.WarehouseManagerImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * tests class to Warehouse Manager.
 * @author Oliver Mrazik
 * @version 2014-3-7
 */
public class WarehouseManagerImplTest {
    private WarehouseManagerImpl warehouseManager;
    private ItemManagerImpl itemManager;
    private ShelfManagerImpl shelfManager;

    @Before
    public void setUp() throws Exception {
        warehouseManager = new WarehouseManagerImpl();
//        itemManager = new ItemManagerImpl();
//        shelfManager = new ShelfManagerImpl();
    }

    @After
    public void tearDown() throws Exception {
        warehouseManager = null;
//        itemManager = null;
//        shelfManager = null;
    }

    @Test
    public void testFindShelfWithItem() throws Exception {
        Item item = newItem(28.8D, 20, true);

        Shelf shelf = newShelf(0, 0, 200.00D, 4, true);

//        itemManager.createItem(item);
//        shelfManager.createShelf(shelf);

        warehouseManager.putItemOnShelf(shelf, item);

        Shelf result = warehouseManager.findShelfWithItem(item);
        assertEquals(shelf, result);
        assertDeepEquals(shelf, result);
    }

    @Test(expected = NullPointerException.class)
    public void testFindShelfWithItemWrong() throws Exception {
        warehouseManager.findShelfWithItem(null);
    }

    @Test
    public void testListAllItemsOnShelf() throws Exception {
        Shelf shelf = newShelf(0, 0, 200.00D, 4, false);

        Item item1 = newItem(28.8D, 20, true);
        Item item2 = newItem(128.7D, 24, false);
        Item item3 = newItem(40.0D, 60, false);

//        shelfManager.createShelf(shelf);
//        itemManager.createItem(item1);
//        itemManager.createItem(item2);
//        itemManager.createItem(item3);

        warehouseManager.putItemOnShelf(shelf, item1);
        warehouseManager.putItemOnShelf(shelf, item2);
        warehouseManager.putItemOnShelf(shelf, item3);

        List<Item> expected = Arrays.asList(item1, item2, item3);
        List<Item> actual = warehouseManager.listAllItemsOnShelf(shelf);

        Collections.sort(expected, itemIdComparator);
        Collections.sort(actual, itemIdComparator);

        assertEquals(expected, actual);
        for(int i = 0; i < expected.size(); i++) {
            assertDeepEquals(expected.get(i), actual.get(i));
        }
    }

    @Test
    public void testPutItemOnShelf() throws Exception {
        Shelf shelf = newShelf(0, 0, 200.00D, 4, false);

        Item item = newItem(128.7D, 24, false);

//        shelfManager.createShelf(shelf);
//        itemManager.createItem(item);

        warehouseManager.putItemOnShelf(shelf, item);

        Item actual = warehouseManager.withdrawItemFromShelf(shelf, item);

        assertEquals(item, actual);
        assertDeepEquals(item, actual);
    }

    @Test(expected = ShelfSecurityException.class)
    public void testPutItemOnShelfWrongSecurity() throws Exception {
        Shelf shelf = newShelf(0, 0, 200.00D, 4, false);
        Item item = newItem(128.7D, 24, true);

        warehouseManager.putItemOnShelf(shelf, item);
    }

    @Test(expected = ShelfCapacityException.class)
    public void testPutItemOnShelfWrongCapacity() throws Exception {
        Shelf shelf = newShelf(0, 0, 200.00D, 1, false);
        Item item1 = newItem(18.2D, 16, false);
        Item item2 = newItem(4.7D, 24, false);

        warehouseManager.putItemOnShelf(shelf, item1);
        warehouseManager.putItemOnShelf(shelf, item2);
    }

    @Test(expected = ShelfWeightException.class)
    public void testPutItemOnShelfWrongWeight() throws Exception {
        Shelf shelf = newShelf(0, 0, 100.00D, 4, false);
        Item item1 = newItem(49.2D, 16, false);
        Item item2 = newItem(50.9D, 24, false);

        warehouseManager.putItemOnShelf(shelf, item1);
        warehouseManager.putItemOnShelf(shelf, item2);
    }

    @Test
    public void testWithdrawItemFromShelf() throws Exception {

    }

    @Test
    public void testRemoveAllExpiredItems() throws Exception {

    }

    private static Shelf newShelf(int column, int row, double maxWeight, int capacity, boolean secure) {
        Shelf shelf = new Shelf();
        shelf.setColumn(column);
        shelf.setRow(row);
        shelf.setMaxWeight(maxWeight);
        shelf.setCapacity(capacity);
        shelf.setSecure(secure);

        return shelf;
    }

    private static Item newItem(Double weight, int storeDays, boolean dangerous) {
        Item item = new Item();
        item.setWeight(weight);
        item.setInsertionDate(new Date());
        item.setStoreDays(storeDays);
        item.setDangerous(dangerous);

        return item;
    }

    public void assertDeepEquals(Object expected, Object actual) {
        if(expected instanceof Item && actual instanceof Item) {
            assertEquals(((Item) expected).getId(), ((Item) actual).getId());
            assertEquals(((Item) expected).getInsertionDate(), ((Item) actual).getInsertionDate());
            assertEquals(((Item) expected).getWeight(), ((Item) actual).getWeight(), 0.01D);
            assertEquals(((Item) expected).isDangerous(), ((Item) actual).isDangerous());
        }

        if(expected instanceof Shelf && actual instanceof Shelf) {
            assertEquals(((Shelf) expected).getId(), ((Shelf) actual).getId());
            assertEquals(((Shelf) expected).getCapacity(), ((Shelf) actual).getCapacity());
            assertEquals(((Shelf) expected).getColumn(), ((Shelf) actual).getColumn());
            assertEquals(((Shelf) expected).getRow(), ((Shelf) actual).getRow());
            assertEquals(((Shelf) expected).getMaxWeight(), ((Shelf) actual).getMaxWeight(), 0.01D);
            assertEquals(((Shelf) expected).isSecure(), ((Shelf) actual).isSecure());
        }
    }

    private static Comparator<Item> itemIdComparator = new Comparator<Item>() {
        @Override
        public int compare(Item o1, Item o2) {
            return o1.getId().compareTo(o2.getId());
        }
    };
}
