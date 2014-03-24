package cz.muni.fi.pv168.warehouse.tests;

import cz.muni.fi.pv168.warehouse.entities.Shelf;
import cz.muni.fi.pv168.warehouse.managers.ShelfManagerImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;


/**
 * @author Oliver Mrázik & Martin Zaťko
 * @version 2014-03-15
 */
public class ShelfManagerImplTest {

    private ShelfManagerImpl manager;
    private Connection con;

    @Before
    public void setUp() throws SQLException {
        con = DriverManager.getConnection("jdbc:derby:memory:GraveManagerTest;create=true");
        con.prepareStatement("CREATE TABLE shelf ("
                + "id bigint primary key generated always as identity,"
                + "col int,"
                + "row int,"
                + "maxWeight int not null,"
                + "capacity int not null,"
                + "secure boolean not null)").executeUpdate();
        manager = new ShelfManagerImpl(con);
    }

    @After
    public void tearDown() throws SQLException {
        con.prepareStatement("DROP TABLE shelf").executeUpdate();
        con.close();
    }
    @Test
    public void testCreateShelf() {
        Shelf shelf = newShelf(9, 4, 300.0D, 10, false);
        manager.createShelf(shelf);

        Integer shelfId = shelf.getId();
        assertNotNull(shelfId);

        Shelf result = manager.findShelfById(shelfId);

        assertEquals(shelf, result);
        assertNotSame(shelf, result);
        assertDeepEquals(shelf, result);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateShelfWithNull() {
        manager.createShelf(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateShelfWithId() {
        Shelf shelf = newShelf(7, 3, 342.0D, 17, true);
        shelf.setId(14);
        manager.createShelf(shelf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateShelfWithWrongColumn() {
        Shelf shelf = newShelf(-1, 3, 342.0D, 17, true);
        manager.createShelf(shelf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateShelfWithWrongRow() {
        Shelf shelf = newShelf(7, -1, 342.0D, 17, true);
        manager.createShelf(shelf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateShelfWithWrongMaxWeight() {
        Shelf shelf = newShelf(7, 3, 0.0D, 17, true);
        manager.createShelf(shelf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateShelfWithMinusMaxWeight() {
        Shelf shelf = newShelf(7, 3, -1.0D, 17, true);
        manager.createShelf(shelf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateShelfWithWrongCapacity() {
        Shelf shelf = newShelf(7, 3, 342.0D, 0, true);
        manager.createShelf(shelf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateShelfWithMinusCapacity() {
        Shelf shelf = newShelf(7, 3, 342.0D, -1, true);
        manager.createShelf(shelf);
    }

    @Test
    public void testDeleteShelf() {
        Shelf shelf1 = newShelf(9, 4, 300.0D, 10, false);
        manager.createShelf(shelf1);

        assertNotNull(manager.findShelfById(shelf1.getId()));

        manager.deleteShelf(shelf1);

        assertNull(manager.findShelfById(shelf1.getId()));
    }

    @Test(expected = NullPointerException.class)
    public void testDeleteShelfWithNull() {
        manager.deleteShelf(null);
    }

    @Test(expected = NullPointerException.class)
    public void testDeleteShelfWithNullId() {
        Shelf shelf = newShelf(7, 3, 342.0D, 17, true);
        shelf.setId(null);
        manager.deleteShelf(shelf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteShelfWithId() {
        Shelf shelf = newShelf(7, 3, 342.0D, 17, true);
        shelf.setId(9);
        manager.deleteShelf(shelf);
    }

    @Test
    public void testListAllShelves() {
        assertTrue(manager.listAllShelves().isEmpty());

        Shelf shelf1 = newShelf(9, 4, 300.0D, 10, false);
        Shelf shelf2 = newShelf(5, 8, 150.0D, 4, true);
        Shelf shelf3 = newShelf(3, 6, 245.0D, 7, false);

        manager.createShelf(shelf1);
        manager.createShelf(shelf2);
        manager.createShelf(shelf3);

        List<Shelf> expected = Arrays.asList(shelf1, shelf2, shelf3);
        List<Shelf> actual = manager.listAllShelves();

        Collections.sort(actual, itemComparator);
        Collections.sort(expected, itemComparator);

        assertEquals(expected, actual);
        assertDeepEquals(expected, actual);
    }

    @Test
    public void testFindShelfById() {
        assertNull(manager.findShelfById(9));

        Shelf shelf = newShelf(3, 4, 750.0D, 34, true);
        manager.createShelf(shelf);

        Shelf result = manager.findShelfById(shelf.getId());
        assertEquals(shelf, result);
        assertDeepEquals(shelf, result);
    }

    @Test
    public void testUpdateShlefColumn() {
        Shelf shelf = newShelf(2, 4, 45.0D, 10, true);
        manager.createShelf(shelf);
        shelf = manager.findShelfById(shelf.getId());

        shelf.setColumn(3);
        manager.updateShelf(shelf);
        assertEquals(3, shelf.getColumn());
        assertEquals(4, shelf.getRow());
        assertEquals(45.0D, shelf.getMaxWeight(), 0.01D);
        assertEquals(10, shelf.getCapacity());
        assertEquals(true, shelf.isSecure());
    }

    @Test
    public void testUpdateShlefRow() {
        Shelf shelf = newShelf(2, 4, 45.0D, 10, true);
        manager.createShelf(shelf);
        shelf = manager.findShelfById(shelf.getId());

        shelf.setRow(2);
        manager.updateShelf(shelf);
        assertEquals(3, shelf.getColumn());
        assertEquals(2, shelf.getRow());
        assertEquals(45.0D, shelf.getMaxWeight(), 0.01D);
        assertEquals(10, shelf.getCapacity());
        assertEquals(true, shelf.isSecure());
    }

    @Test
    public void testUpdateShlefMaxWeight() {
        Shelf shelf = newShelf(2, 4, 45.0D, 10, true);
        manager.createShelf(shelf);
        shelf = manager.findShelfById(shelf.getId());

        shelf.setMaxWeight(25.0D);
        manager.updateShelf(shelf);
        assertEquals(3, shelf.getColumn());
        assertEquals(2, shelf.getRow());
        assertEquals(25.00D, shelf.getMaxWeight(), 0.01D);
        assertEquals(10, shelf.getCapacity());
        assertEquals(true, shelf.isSecure());
    }

    @Test
    public void testUpdateShlefCapacity() {
        Shelf shelf = newShelf(2, 4, 45.0D, 10, true);
        manager.createShelf(shelf);
        shelf = manager.findShelfById(shelf.getId());

        shelf.setCapacity(4);
        manager.updateShelf(shelf);
        assertEquals(3, shelf.getColumn());
        assertEquals(2, shelf.getRow());
        assertEquals(25.0D, shelf.getMaxWeight(), 0.01D);
        assertEquals(4, shelf.getCapacity());
        assertEquals(true, shelf.isSecure());
    }

    @Test
    public void testUpdateShlefSecure() {
        Shelf shelf = newShelf(2, 4, 45.0D, 10, true);
        manager.createShelf(shelf);
        shelf = manager.findShelfById(shelf.getId());

        shelf.setSecure(false);
        manager.updateShelf(shelf);
        assertEquals(3, shelf.getColumn());
        assertEquals(2, shelf.getRow());
        assertEquals(25.0D, shelf.getMaxWeight(), 0.01D);
        assertEquals(4, shelf.getCapacity());
        assertEquals(false, shelf.isSecure());
    }

    @Test(expected = NullPointerException.class)
    public void testUpdateShelfByIdWithNull() {
        manager.updateShelf(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateShelfByIdWithWrongId() {
        Shelf shelf = newShelf(2, 4, 45.00D, 10, true);
        manager.createShelf(shelf);
        shelf = manager.findShelfById(shelf.getId());

        shelf.setId(-1);
        manager.updateShelf(shelf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateShelfByIdWithWrongColumn() {
        Shelf shelf = newShelf(2, 4, 45.00D, 10, true);
        manager.createShelf(shelf);
        shelf = manager.findShelfById(shelf.getId());

        shelf.setColumn(-1);
        manager.updateShelf(shelf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateShelfByIdWithWrongRow() {
        Shelf shelf = newShelf(2, 4, 45.00D, 10, true);
        manager.createShelf(shelf);
        shelf = manager.findShelfById(shelf.getId());

        shelf.setRow(-1);
        manager.updateShelf(shelf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateShelfByIdWithWrongMaxWeight() {
        Shelf shelf = newShelf(2, 4, 45.00D, 10, true);
        manager.createShelf(shelf);
        shelf = manager.findShelfById(shelf.getId());

        shelf.setMaxWeight(0.0D);
        manager.updateShelf(shelf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateShelfByIdWithMinusMaxWeight() {
        Shelf shelf = newShelf(2, 4, 45.00D, 10, true);
        manager.createShelf(shelf);
        shelf = manager.findShelfById(shelf.getId());

        shelf.setMaxWeight(-1.0D);
        manager.updateShelf(shelf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateShelfByIdWithWrongCapacity() {
        Shelf shelf = newShelf(2, 4, 45.00D, 10, true);
        manager.createShelf(shelf);
        shelf = manager.findShelfById(shelf.getId());

        shelf.setCapacity(0);
        manager.updateShelf(shelf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateShelfByIdWithMinusCapacity() {
        Shelf shelf = newShelf(2, 4, 45.00D, 10, true);
        manager.createShelf(shelf);
        shelf = manager.findShelfById(shelf.getId());

        shelf.setCapacity(-1);
        manager.updateShelf(shelf);
    }

    private Shelf newShelf(int column, int row, double maxWeight, int capacity, boolean secure) {
        Shelf shelf = new Shelf();
        shelf.setColumn(column);
        shelf.setRow(row);
        shelf.setMaxWeight(maxWeight);
        shelf.setCapacity(capacity);
        shelf.setSecure(secure);
        return shelf;
    }

    private void assertDeepEquals(List<Shelf> expectedList, List<Shelf> actualList) {
        for (int i = 0; i < expectedList.size(); i++) {
            Shelf expected = expectedList.get(i);
            Shelf actual = actualList.get(i);
            assertDeepEquals(expected, actual);
        }
    }

    private void assertDeepEquals(Shelf expected, Shelf actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getColumn(), actual.getColumn());
        assertEquals(expected.getRow(), actual.getRow());
        assertEquals(expected.getMaxWeight(), actual.getMaxWeight(), 0.01D);
        assertEquals(expected.getCapacity(), actual.getCapacity());
        assertEquals(expected.isSecure(), actual.isSecure());
    }

    private Comparator<Shelf> itemComparator = new Comparator<Shelf>() {

        @Override
        public int compare(Shelf o1, Shelf o2) {
            return o1.getId().compareTo(o2.getId());
        }
    };
}
