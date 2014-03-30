package cz.muni.fi.pv168.warehouse.tests;

import cz.muni.fi.pv168.warehouse.database.Tools;
import cz.muni.fi.pv168.warehouse.entities.Item;
import cz.muni.fi.pv168.warehouse.entities.Shelf;
import cz.muni.fi.pv168.warehouse.exceptions.MethodFailureException;
import cz.muni.fi.pv168.warehouse.exceptions.ShelfAttributeException;
import cz.muni.fi.pv168.warehouse.managers.ItemManagerImpl;
import cz.muni.fi.pv168.warehouse.managers.ShelfManagerImpl;
import cz.muni.fi.pv168.warehouse.managers.WarehouseManagerImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Tests class to Warehouse Manager.
 *
 * @author Oliver Mrázik & Martin Zaťko
 * @version 0.1
 */
public class WarehouseManagerImplTest {

    private WarehouseManagerImpl warehouseManager;
    private ShelfManagerImpl shelfManager;
    private ItemManagerImpl itemManager;
    private DataSource dataSource;

    private static DataSource prepareDataSource() {
        org.apache.tomcat.jdbc.pool.DataSource ds = new org.apache.tomcat.jdbc.pool.DataSource();
        ds.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
        ds.setUrl("jdbc:derby:memory:test-datab;create=true");
        return ds;
    }

    private Item expiredItem, notExpiredItem, item1, item2, item3;
    private Shelf shelf1, shelf2, shelf3, shelf4;

    private void preparedEntities() throws MethodFailureException {
        expiredItem = new Item();
        expiredItem.setWeight(24.3D);
        expiredItem.setInsertionDate(new Date(1394030059000L));
        expiredItem.setStoreDays(20);
        expiredItem.setDangerous(true);

        notExpiredItem = new Item();
        notExpiredItem.setWeight(24.3D);
        notExpiredItem.setInsertionDate(new Date(1395239659000L));
        notExpiredItem.setStoreDays(20);
        notExpiredItem.setDangerous(true);

        item1 = newItem(49.2D, 60, false);
        item2 = newItem(50.9D, 25, false);
        item3 = newItem(84.27D, 25, true);

        itemManager.createItem(item1);
        itemManager.createItem(item2);
        itemManager.createItem(item3);
        itemManager.createItem(notExpiredItem);
        itemManager.createItem(expiredItem);

        shelf1 = newShelf(0, 0, 200.00D, 4, true);
        shelf2 = newShelf(0, 0, 200.00D, 4, false);
        shelf3 = newShelf(0, 0, 200.00D, 1, false);
        shelf4 = newShelf(0, 0, 100.00D, 4, false);

        shelfManager.createShelf(shelf1);
        shelfManager.createShelf(shelf2);
        shelfManager.createShelf(shelf3);
        shelfManager.createShelf(shelf4);
    }

    @Before
    public void setUp() throws MethodFailureException, SQLException {
        dataSource = prepareDataSource();
        Tools.executeSQL(dataSource, WarehouseManagerImpl.class.getResource("CreateTables.sql"));
        warehouseManager = new WarehouseManagerImpl();
        warehouseManager.setDataSource(dataSource);
        shelfManager = new ShelfManagerImpl();
        shelfManager.setDataSource(dataSource);
        itemManager = new ItemManagerImpl();
        itemManager.setDataSource(dataSource);
        preparedEntities();
    }

    @After
    public void tearDown() throws SQLException, IOException, MethodFailureException {
        warehouseManager = null;
        Tools.executeSQL(dataSource, ShelfManagerImpl.class.getResource("DropTables.sql"));
    }

    @Test
    public void testFindShelfWithItem() throws MethodFailureException, ShelfAttributeException {
        warehouseManager.putItemOnShelf(shelf2, item1);

        Shelf result = warehouseManager.findShelfWithItem(item1);
        assertEquals(shelf2, result);
        assertDeepEquals(shelf2, result);
    }

    @Test(expected = NullPointerException.class)
    public void testFindShelfWithItemWrong() throws MethodFailureException {
        warehouseManager.findShelfWithItem(null);
    }

    @Test
    public void testListAllItemsOnShelf() throws MethodFailureException, ShelfAttributeException {
        warehouseManager.putItemOnShelf(shelf2, item1);
        warehouseManager.putItemOnShelf(shelf2, item2);

        List<Item> expected = Arrays.asList(itemManager.findItemById(item1.getId()), itemManager.findItemById(item2.getId()));
        List<Item> actual = warehouseManager.listAllItemsOnShelf(shelf2);

        Collections.sort(expected, itemIdComparator);
        Collections.sort(actual, itemIdComparator);

        assertEquals(expected, actual);
        for(int i = 0; i < expected.size(); i++) {
            assertDeepEquals(expected.get(i), actual.get(i));
        }
    }

    @Test
    public void testPutItemOnShelf() throws MethodFailureException, ShelfAttributeException {
        warehouseManager.putItemOnShelf(shelf2, item1);
        Item actual = warehouseManager.withdrawItemFromShelf(item1);

        assertEquals(item1, actual);
        assertDeepEquals(item1, actual);
    }

    @Test(expected = ShelfAttributeException.class)
    public void testPutItemOnShelfWrongSecurity() throws MethodFailureException, ShelfAttributeException {
        warehouseManager.putItemOnShelf(shelf2, item3);
    }

    @Test(expected = ShelfAttributeException.class)
    public void testPutItemOnShelfWrongCapacity() throws MethodFailureException, ShelfAttributeException {
        warehouseManager.putItemOnShelf(shelf3, item1);
        warehouseManager.putItemOnShelf(shelf3, item2);
    }

    @Test(expected = ShelfAttributeException.class)
    public void testPutItemOnShelfWrongWeight() throws MethodFailureException, ShelfAttributeException {
        warehouseManager.putItemOnShelf(shelf4, item1);
        warehouseManager.putItemOnShelf(shelf4, item2);
    }

    @Test(expected = NullPointerException.class)
    public void testPutItemOnShelfWithNullItem() throws MethodFailureException, ShelfAttributeException {
        warehouseManager.putItemOnShelf(shelf1, null);
    }

    @Test(expected = NullPointerException.class)
    public void testPutItemOnShelfWithNullShelf() throws MethodFailureException, ShelfAttributeException {
        warehouseManager.putItemOnShelf(null, item1);
    }

    @Test
    public void testWithdrawItemFromShelf() throws MethodFailureException, ShelfAttributeException {
        warehouseManager.putItemOnShelf(shelf2, item1);
        Item actual = warehouseManager.withdrawItemFromShelf(item1);

        assertEquals(item1, actual);
        assertDeepEquals(item1, actual);
    }

    @Test
    public void listShelvesWithSomeFreeSpace() throws MethodFailureException, ShelfAttributeException {
        warehouseManager.putItemOnShelf(shelf3, item1);
        warehouseManager.putItemOnShelf(shelf1, item3);
        warehouseManager.putItemOnShelf(shelf2, item2);

        List<Shelf> expected = Arrays.asList(shelf1, shelf2, shelf4);
        List<Shelf> actual = warehouseManager.listShelvesWithSomeFreeSpace();
        Collections.sort(expected, shelfIdComparator);
        Collections.sort(actual, shelfIdComparator);

        assertEquals(expected, actual);
        for (int i = 0; i < expected.size(); i++) {
            assertDeepEquals(expected.get(i), actual.get(i));
        }
    }

    @Test
    public void listAllItemsWithoutShelf() throws MethodFailureException, ShelfAttributeException {
        warehouseManager.putItemOnShelf(shelf3, item1);
        warehouseManager.putItemOnShelf(shelf1, item3);
        warehouseManager.putItemOnShelf(shelf2, item2);

        List<Item> expected = Arrays.asList(itemManager.findItemById(expiredItem.getId()),
                itemManager.findItemById(notExpiredItem.getId()));
        List<Item> actual = warehouseManager.listAllItemsWithoutShelf();
        Collections.sort(expected, itemIdComparator);
        Collections.sort(actual, itemIdComparator);

        assertEquals(expected, actual);
        for (int i = 0; i < expected.size(); i++) {
            assertDeepEquals(expected.get(i), actual.get(i));
        }
    }

    @Test
    public void testRemoveAllExpiredItems() throws MethodFailureException, ShelfAttributeException {
        warehouseManager.putItemOnShelf(shelf1, expiredItem);
        warehouseManager.putItemOnShelf(shelf1, notExpiredItem);
        warehouseManager.removeAllExpiredItems(new Date(1395930859000L));
        List<Item> list = warehouseManager.listAllItemsOnShelf(shelf1);
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

    private static Comparator<Shelf> shelfIdComparator = new Comparator<Shelf>() {
        @Override
        public int compare(Shelf o1, Shelf o2) {
            return o1.getId().compareTo(o2.getId());
        }
    };

    private static Comparator<Item> itemIdComparator = new Comparator<Item>() {
        @Override
        public int compare(Item o1, Item o2) {
            return o1.getId().compareTo(o2.getId());
        }
    };
}
