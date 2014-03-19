package cz.muni.fi.pv168.warehouse.tests;

import cz.muni.fi.pv168.warehouse.entities.Item;
import cz.muni.fi.pv168.warehouse.managers.ItemManagerImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static junit.framework.Assert.*;

/**
 * tests class to Item Manager.
 * @author Oliver Mrazik
 * @version 2014-3-7
 */
public class ItemManagerImplTest {
    private ItemManagerImpl manager;

    @Before
    public void setUp() throws Exception {
        manager = new ItemManagerImpl();
    }

    @After
    public void tearDown() throws Exception {
        manager = null;
    }

    @Test
    public void testCreateItem() throws Exception {
        Item item = newItem(28.8D, 25, true);

        manager.createItem(item);

        Integer itemId = item.getId();
        assertNotNull(itemId);
        Item result = manager.findItemById(itemId);
        assertEquals(item, result);
        assertDeepEquals(item, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateItemNull() throws Exception {
        manager.createItem(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateItemZeroWeight() throws Exception {
        Item item = newItem(0.0D, 25, true);
        manager.createItem(item);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateItemNegativeWeight() throws Exception {
        Item item = newItem(-1.0D, 25, true);
        manager.createItem(item);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateItemZeroStoreDays() throws Exception {
        Item item = newItem(28.8D, 0, true);
        manager.createItem(item);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateItemNegativeStoreDays() throws Exception {
        Item item = newItem(28.8D, -1, true);
        manager.createItem(item);
    }

    @Test
    public void testDeleteItem() throws Exception {
        Item item = newItem(28.8D, 25, true);
        manager.createItem(item);

        assertNotNull(manager.findItemById(item.getId()));

        manager.deleteItem(manager.findItemById(item.getId()));
        assertNull(manager.findItemById(item.getId()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteItemNull() throws Exception {
        manager.deleteItem(null);
    }

    @Test
    public void testListAllItems() throws Exception {
        Item item1 = newItem(28.8D, 25, true);
        Item item2 = newItem(47.1D, 10, false);

        manager.createItem(item1);
        manager.createItem(item2);

        List<Item> expected = Arrays.asList(item1, item2);
        List<Item> actual = manager.listAllItems();

        Collections.sort(expected, comparatorId);
        Collections.sort(actual, comparatorId);

        assertEquals(expected, actual);
        for(int i = 0; i < expected.size(); i++) {
            assertDeepEquals(expected.get(i), actual.get(i));
        }
    }

    @Test
    public void testFindItemById() throws Exception {
        Item item = newItem(28.8D, 25, true);
        manager.createItem(item);

        assertNotNull(item.getId());
        Integer itemId = item.getId();

        assertEquals(item, manager.findItemById(itemId));
        assertDeepEquals(item, manager.findItemById(itemId));
    }

    @Test
    public void testUpdateItemWeight() {
        Item item = newItem(28.8D, 25, true);
        manager.createItem(item);

        assertNotNull(item.getId());
        Integer itemId = item.getId();

        item = manager.findItemById(itemId);
        item.setWeight(50.0D);
        manager.updateItem(item);

        assertEquals(50.0D, item.getWeight());
        assertEquals(25, item.getStoreDays());
        assertEquals(true, item.isDangerous());
    }

    @Test
    public void testUpdateItemStoreDays() {
        Item item = newItem(28.8D, 25, true);
        manager.createItem(item);

        assertNotNull(item.getId());
        Integer itemId = item.getId();

        item = manager.findItemById(itemId);
        item.setStoreDays(32);
        manager.updateItem(item);

        assertEquals(28.8D, item.getWeight());
        assertEquals(32, item.getStoreDays());
        assertEquals(true, item.isDangerous());
    }

    @Test
    public void testUpdateItemDangerous() {
        Item item = newItem(28.8D, 25, true);
        manager.createItem(item);

        assertNotNull(item.getId());
        Integer itemId = item.getId();

        item = manager.findItemById(itemId);
        item.setDangerous(false);
        manager.updateItem(item);

        assertEquals(28.8D, item.getWeight());
        assertEquals(25, item.getStoreDays());
        assertEquals(false, item.isDangerous());
    }

    @Test(expected = NullPointerException.class)
    public void testUpdateItemNull() throws Exception {
        manager.updateItem(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateItemWrongId() throws Exception {
        Item item = newItem(28.8D, 25, true);
        manager.createItem(item);

        assertNotNull(item.getId());
        Integer itemId = item.getId();

        item = manager.findItemById(itemId);
        item.setId(-1);
        manager.updateItem(item);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateItemZeroWeight() throws Exception {
        Item item = newItem(28.8D, 25, true);
        manager.createItem(item);

        assertNotNull(item.getId());
        Integer itemId = item.getId();

        item = manager.findItemById(itemId);
        item.setWeight(0.0D);
        manager.updateItem(item);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testUpdateItemNegativeWeight() throws Exception {
        Item item = newItem(28.8D, 25, true);
        manager.createItem(item);

        assertNotNull(item.getId());
        Integer itemId = item.getId();

        item = manager.findItemById(itemId);
        item.setWeight(-1.00D);
        manager.updateItem(item);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateItemZeroStoreDays() throws Exception {
        Item item = newItem(28.8D, 25, true);
        manager.createItem(item);

        assertNotNull(item.getId());
        Integer itemId = item.getId();

        item = manager.findItemById(itemId);
        item.setStoreDays(0);
        manager.updateItem(item);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testUpdateItemNegativeStoreDays() throws Exception {
        Item item = newItem(28.8D, 25, true);
        manager.createItem(item);

        assertNotNull(item.getId());
        Integer itemId = item.getId();

        item = manager.findItemById(itemId);
        item.setStoreDays(-1);
        manager.updateItem(item);
    }

    private static Item newItem(Double weight, int storeDays, boolean dangerous) {
        Item item = new Item();
        item.setWeight(weight);
        item.setInsertionDate(new Date());
        item.setStoreDays(storeDays);
        item.setDangerous(dangerous);

        return item;
    }

    public void assertDeepEquals(Item expected, Item actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getInsertionDate(), actual.getInsertionDate());
        assertEquals(expected.getWeight(), actual.getWeight());
    }

    public static Comparator<Item> comparatorId = new Comparator<Item>() {

        @Override
        public int compare(Item o1, Item o2) {
            return o1.getId().compareTo(o2.getId());
        }
    };
}
