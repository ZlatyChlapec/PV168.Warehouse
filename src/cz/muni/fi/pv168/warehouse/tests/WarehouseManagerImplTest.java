package cz.muni.fi.pv168.warehouse.tests;

import cz.muni.fi.pv168.warehouse.entities.Item;
import cz.muni.fi.pv168.warehouse.entities.Shelf;
import cz.muni.fi.pv168.warehouse.exceptions.MethodFailureException;
import cz.muni.fi.pv168.warehouse.exceptions.ShelfCapacityException;
import cz.muni.fi.pv168.warehouse.exceptions.ShelfSecurityException;
import cz.muni.fi.pv168.warehouse.exceptions.ShelfWeightException;
import cz.muni.fi.pv168.warehouse.managers.ShelfManagerImpl;
import cz.muni.fi.pv168.warehouse.managers.WarehouseManager;
import cz.muni.fi.pv168.warehouse.managers.WarehouseManagerImpl;
import org.apache.derby.jdbc.ClientDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static cz.muni.fi.pv168.warehouse.db.Connection.executeSQL;
import static org.junit.Assert.assertEquals;

/**
 * Tests class to Warehouse Manager.
 *
 * @author Oliver Mrázik & Martin Zaťko
 * @version 0.1
 */
public class WarehouseManagerImplTest {

    private WarehouseManager warehouseManager;
    private Connection con;

    private void preparedEntities() {
        Item item1 = new Item();
        item1.setWeight(24.3D);
        item1.setInsertionDate(new Date(1394030059000L));
        item1.setStoreDays(20);
        item1.setDangerous(true);

        Item item2 = new Item();
        item2.setWeight(24.3D);
        item2.setInsertionDate(new Date(1395239659000L));
        item2.setStoreDays(20);
        item2.setDangerous(true);

        Item item3 = newItem(40.0D, 60, false);
    }

    @Before
    public void setUp() throws MethodFailureException, SQLException {
        ClientDataSource ds = new ClientDataSource();
        ds.setDatabaseName("datab;create=true");
        con = ds.getConnection();
        executeSQL(con, ShelfManagerImpl.class.getResourceAsStream("CreateTables.sql"));

        warehouseManager = new WarehouseManagerImpl(ds) {
            @Override
            public Date currentDate() {
                return new Date(1395930859000L);
            }
        };
    }

    @After
    public void tearDown() throws SQLException, IOException, MethodFailureException {
        warehouseManager = null;
        executeSQL(con, ShelfManagerImpl.class.getResource("DropTables.sql").openStream());
        con.close();
    }
    /*
    @Test
    public void testFindShelfWithItem() {
        Item item = newItem(28.8D, 20, true);
        Shelf shelf = newShelf(0, 0, 200.00D, 4, true);

        warehouseManager.putItemOnShelf(shelf, item);

        Shelf result = warehouseManager.findShelfWithItem(item);
        assertEquals(shelf, result);
        assertDeepEquals(shelf, result);
    }

    @Test(expected = NullPointerException.class)
    public void testFindShelfWithItemWrong() throws Exception {
        warehouseManager.findShelfWithItem(null);
    }
    */
    @Test
    public void testListAllItemsOnShelf() throws MethodFailureException {
        Shelf shelf = newShelf(0, 0, 200.00D, 4, false);

        Item item1 = newItem(28.8D, 20, true);
        Item item2 = newItem(128.7D, 24, false);
        Item item3 = newItem(40.0D, 60, false);

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
    public void testPutItemOnShelf() throws MethodFailureException {
        Shelf shelf = newShelf(0, 0, 200.00D, 4, false);
        Item item = newItem(128.7D, 24, false);

        warehouseManager.putItemOnShelf(shelf, item);
        Item actual = warehouseManager.withdrawItemFromShelf(item);

        assertEquals(item, actual);
        assertDeepEquals(item, actual);
    }

    @Test(expected = ShelfSecurityException.class)
    public void testPutItemOnShelfWrongSecurity() throws MethodFailureException {
        Shelf shelf = newShelf(0, 0, 200.00D, 4, false);
        Item item = newItem(128.7D, 24, true);

        warehouseManager.putItemOnShelf(shelf, item);
    }

    @Test(expected = ShelfCapacityException.class)
    public void testPutItemOnShelfWrongCapacity() throws MethodFailureException {
        Shelf shelf = newShelf(0, 0, 200.00D, 1, false);
        Item item1 = newItem(18.2D, 16, false);
        Item item2 = newItem(4.7D, 24, false);

        warehouseManager.putItemOnShelf(shelf, item1);
        warehouseManager.putItemOnShelf(shelf, item2);
    }

    @Test(expected = ShelfWeightException.class)
    public void testPutItemOnShelfWrongWeight() throws MethodFailureException {
        Shelf shelf = newShelf(0, 0, 100.00D, 4, false);
        Item item1 = newItem(49.2D, 16, false);
        Item item2 = newItem(50.9D, 24, false);

        warehouseManager.putItemOnShelf(shelf, item1);
        warehouseManager.putItemOnShelf(shelf, item2);
    }

    @Test(expected = NullPointerException.class)
    public void testPutItemOnShelfWithNullItem() throws MethodFailureException {
        Shelf shelf = newShelf(0, 0, 24.0D, 6, true);
        warehouseManager.putItemOnShelf(shelf, null);
    }

    @Test(expected = NullPointerException.class)
    public void testPutItemOnShelfWithNullShelf() throws MethodFailureException {
        Item item = newItem(45.6D, 10, false);
        warehouseManager.putItemOnShelf(null, item);
    }

    @Test
    public void testWithdrawItemFromShelf() throws MethodFailureException {
        Item item = newItem(21.4D, 14, true);
        Shelf shelf = newShelf(0, 0, 78.6D, 1, true);

        warehouseManager.putItemOnShelf(shelf, item);
        Item actual = warehouseManager.withdrawItemFromShelf(item);

        assertEquals(item, actual);
        assertDeepEquals(item, actual);
    }

    @Test
    public void testRemoveAllExpiredItems() throws MethodFailureException {
        Item item1 = new Item();
        item1.setWeight(24.3D);
        item1.setInsertionDate(new Date(1394030059000L));
        item1.setStoreDays(20);
        item1.setDangerous(true);

        Item item2 = new Item();
        item2.setWeight(24.3D);
        item2.setInsertionDate(new Date(1395239659000L));
        item2.setStoreDays(20);
        item2.setDangerous(true);

        Shelf shelf = newShelf(0, 0, 100.5D, 2, true);

        warehouseManager.putItemOnShelf(shelf, item1);
        warehouseManager.putItemOnShelf(shelf, item2);
        warehouseManager.removeAllExpiredItems(warehouseManager.currentDate());
        List<Item> list = warehouseManager.listAllItemsOnShelf(shelf);
        for (Item i : list) {
            assertEquals(1, list.size());
            assertEquals(i, item2);
            assertDeepEquals(i, item2);
        }
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
