package cz.muni.fi.pv168.warehouse.tests;

import cz.muni.fi.pv168.warehouse.entities.Item;
import cz.muni.fi.pv168.warehouse.exceptions.MethodFailureException;
import cz.muni.fi.pv168.warehouse.managers.ItemManagerImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;

/**
 * tests class to Item Manager.
 *
 * @author Oliver Mrazik
 * @version 2014-3-23
 */
public class ItemManagerImplTest {
    private ItemManagerImpl manager;
    private Connection connection;

    @Before
    public void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:derby://localhost:1527/datab");
        connection.prepareStatement("CREATE TABLE ADMIN.ITEM ("
                + " ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,"
                + " WEIGHT DOUBLE NOT NULL,"
                + " INSERTIONDATE DATE DEFAULT CURRENT DATE,"
                + " STOREDAYS INT NOT NULL,"
                + " DANGEROUS BOOLEAN NOT NULL )").executeUpdate();
        manager = new ItemManagerImpl(connection);
    }

    @After
    public void tearDown() throws SQLException {
        connection.prepareStatement("DROP TABLE ADMIN.ITEM").execute();
        connection.close();
        manager = null;
    }

    @Test
    public void testCreateItem() throws MethodFailureException {
        Item item = newItem(28.8D, 25, true);

        manager.createItem(item);

        Integer itemId = item.getId();
        assertNotNull(itemId);
        Item result = manager.findItemById(itemId);
        assertEquals(item, result);
        assertDeepEquals(item, result);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateItemNull() throws MethodFailureException {
        manager.createItem(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateItemZeroWeight() throws MethodFailureException {
        Item item = newItem(0.0D, 25, true);
        manager.createItem(item);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateItemNegativeWeight() throws MethodFailureException {
        Item item = newItem(-1.0D, 25, true);
        manager.createItem(item);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateItemZeroStoreDays() throws MethodFailureException {
        Item item = newItem(28.8D, 0, true);
        manager.createItem(item);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateItemNegativeStoreDays() throws MethodFailureException {
        Item item = newItem(28.8D, -1, true);
        manager.createItem(item);
    }

    @Test
    public void testDeleteItem() throws MethodFailureException {
        Item item = newItem(28.8D, 25, true);
        manager.createItem(item);

        assertNotNull(manager.findItemById(item.getId()));

        manager.deleteItem(manager.findItemById(item.getId()));
        assertNull(manager.findItemById(item.getId()));
    }

    @Test(expected = NullPointerException.class)
    public void testDeleteItemNull() throws MethodFailureException {
        manager.deleteItem(null);
    }

    @Test
    public void testListAllItems() throws MethodFailureException {
        Item item1 = newItem(28.8D, 25, true);
        Item item2 = newItem(47.1D, 10, false);

        manager.createItem(item1);
        manager.createItem(item2);

        List<Item> expected = Arrays.asList(item1, item2);
        List<Item> actual = manager.listAllItems();

        Collections.sort(expected, comparatorId);
        Collections.sort(actual, comparatorId);

        assertEquals(expected, actual);
        for (int i = 0; i < expected.size(); i++) {
            assertDeepEquals(expected.get(i), actual.get(i));
        }
    }

    @Test
    public void testFindItemById() throws MethodFailureException {
        Item item = newItem(28.8D, 25, true);
        manager.createItem(item);

        assertNotNull(item.getId());
        Integer itemId = item.getId();

        assertEquals(item, manager.findItemById(itemId));
        assertDeepEquals(item, manager.findItemById(itemId));
    }

    @Test
    public void testUpdateItemWeight() throws MethodFailureException {
        Item item = newItem(28.8D, 25, true);
        manager.createItem(item);

        assertNotNull(item.getId());
        Integer itemId = item.getId();

        item = manager.findItemById(itemId);
        item.setWeight(50.0D);
        manager.updateItem(item);

        assertEquals(50.0D, item.getWeight(), 0.01D);
        assertEquals(25, item.getStoreDays());
        assertEquals(true, item.isDangerous());
    }

    @Test
    public void testUpdateItemStoreDays() throws MethodFailureException {
        Item item = newItem(28.8D, 25, true);
        manager.createItem(item);

        assertNotNull(item.getId());
        Integer itemId = item.getId();

        item = manager.findItemById(itemId);
        item.setStoreDays(32);
        manager.updateItem(item);

        assertEquals(28.8D, item.getWeight(), 0.01D);
        assertEquals(32, item.getStoreDays());
        assertEquals(true, item.isDangerous());
    }

    @Test
    public void testUpdateItemDangerous() throws MethodFailureException {
        Item item = newItem(28.8D, 25, true);
        manager.createItem(item);

        assertNotNull(item.getId());
        Integer itemId = item.getId();

        item = manager.findItemById(itemId);
        item.setDangerous(false);
        manager.updateItem(item);

        assertEquals(28.8D, item.getWeight(), 0.01D);
        assertEquals(25, item.getStoreDays());
        assertEquals(false, item.isDangerous());
    }

    @Test(expected = NullPointerException.class)
    public void testUpdateItemNull() throws Exception {
        try {
            manager.updateItem(null);
        } catch (MethodFailureException e) {
            e.printStackTrace();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateItemWrongId() throws MethodFailureException {
        Item item = newItem(28.8D, 25, true);
        manager.createItem(item);

        assertNotNull(item.getId());
        Integer itemId = item.getId();

        item = manager.findItemById(itemId);
        item.setId(-1);
        manager.updateItem(item);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateItemZeroWeight() throws MethodFailureException {
        Item item = newItem(28.8D, 25, true);
        manager.createItem(item);

        assertNotNull(item.getId());
        Integer itemId = item.getId();

        item = manager.findItemById(itemId);
        item.setWeight(0.0D);
        manager.updateItem(item);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testUpdateItemNegativeWeight() throws MethodFailureException {
        Item item = newItem(28.8D, 25, true);
        manager.createItem(item);

        assertNotNull(item.getId());
        Integer itemId = item.getId();

        item = manager.findItemById(itemId);
        item.setWeight(-1.00D);
        manager.updateItem(item);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateItemZeroStoreDays() throws MethodFailureException {
        Item item = newItem(28.8D, 25, true);
        manager.createItem(item);

        assertNotNull(item.getId());
        Integer itemId = item.getId();

        item = manager.findItemById(itemId);
        item.setStoreDays(0);
        manager.updateItem(item);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testUpdateItemNegativeStoreDays() throws MethodFailureException {
        Item item = newItem(28.8D, 25, true);
        manager.createItem(item);

        assertNotNull(item.getId());
        Integer itemId = item.getId();

        item = manager.findItemById(itemId);
        item.setStoreDays(-1);
        manager.updateItem(item);
    }

    private static Item newItem(Double weight, int storeDays, boolean dangerous) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Item item = new Item();
        item.setWeight(weight);
        String date = dateFormat.format(new Date());
        try {
            item.setInsertionDate(dateFormat.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        item.setStoreDays(storeDays);
        item.setDangerous(dangerous);

        return item;
    }

    public void assertDeepEquals(Item expected, Item actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getInsertionDate(), actual.getInsertionDate());
        assertEquals(expected.getWeight(), actual.getWeight(), 0.01D);
    }

    public static Comparator<Item> comparatorId = new Comparator<Item>() {

        @Override
        public int compare(Item o1, Item o2) {
            return o1.getId().compareTo(o2.getId());
        }
    };
}
