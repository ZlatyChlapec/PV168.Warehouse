package cz.muni.fi.pv168.warehouse.tests;

import cz.muni.fi.pv168.warehouse.entities.Shelf;
import cz.muni.fi.pv168.warehouse.exceptions.MethodFailureException;
import cz.muni.fi.pv168.warehouse.managers.ShelfManager;
import cz.muni.fi.pv168.warehouse.managers.ShelfManagerImpl;
import org.apache.derby.jdbc.ClientDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static cz.muni.fi.pv168.warehouse.db.Connection.executeSQL;
import static org.junit.Assert.*;


/**
 * @author Oliver Mrázik & Martin Zaťko
 * @version 2014-03-15
 */
public class ShelfManagerImplTest {

    private ShelfManager manager;
    private Connection con;

    @Before
    public void setUp() throws SQLException, MethodFailureException {
        ClientDataSource ds = new ClientDataSource();
        ds.setDatabaseName("datab;create=true");
        con = ds.getConnection();
        executeSQL(con, ShelfManagerImpl.class.getResourceAsStream("CreateTables.sql"));
        manager = new ShelfManagerImpl(ds);
    }

    @After
    public void tearDown() throws SQLException, IOException, MethodFailureException {
        manager = null;
        executeSQL(con, ShelfManagerImpl.class.getResource("DropTables.sql").openStream());
        con.close();
    }
    @Test
    public void testCreateShelf() throws MethodFailureException {
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
    public void testCreateShelfWithNull() throws MethodFailureException {
        manager.createShelf(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateShelfWithId() throws MethodFailureException {
        Shelf shelf = newShelf(7, 3, 342.0D, 17, true);
        shelf.setId(14);
        manager.createShelf(shelf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateShelfWithWrongColumn() throws MethodFailureException {
        Shelf shelf = newShelf(-1, 3, 342.0D, 17, true);
        manager.createShelf(shelf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateShelfWithWrongRow() throws MethodFailureException {
        Shelf shelf = newShelf(7, -1, 342.0D, 17, true);
        manager.createShelf(shelf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateShelfWithWrongMaxWeight() throws MethodFailureException {
        Shelf shelf = newShelf(7, 3, 0.0D, 17, true);
        manager.createShelf(shelf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateShelfWithMinusMaxWeight() throws MethodFailureException {
        Shelf shelf = newShelf(7, 3, -1.0D, 17, true);
        manager.createShelf(shelf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateShelfWithWrongCapacity() throws MethodFailureException {
        Shelf shelf = newShelf(7, 3, 342.0D, 0, true);
        manager.createShelf(shelf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateShelfWithMinusCapacity() throws MethodFailureException {
        Shelf shelf = newShelf(7, 3, 342.0D, -1, true);
        manager.createShelf(shelf);
    }

    @Test
    public void testDeleteShelf() throws MethodFailureException {
        Shelf shelf1 = newShelf(9, 4, 300.0D, 10, false);
        manager.createShelf(shelf1);

        assertNotNull(manager.findShelfById(shelf1.getId()));

        manager.deleteShelf(shelf1);
    }

    @Test(expected = NullPointerException.class)
    public void testDeleteShelfWithNull() throws MethodFailureException {
        manager.deleteShelf(null);
    }

    @Test(expected = NullPointerException.class)
    public void testDeleteShelfWithNullId() throws MethodFailureException {
        Shelf shelf = newShelf(7, 3, 342.0D, 17, true);
        shelf.setId(null);
        manager.deleteShelf(shelf);
    }

    @Test
    public void testListAllShelves() throws MethodFailureException {
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
    public void testFindShelfById() throws MethodFailureException {
        Shelf shelf = newShelf(3, 4, 750.0D, 34, true);
        manager.createShelf(shelf);

        Shelf result = manager.findShelfById(shelf.getId());
        assertEquals(shelf, result);
        assertDeepEquals(shelf, result);
    }

    @Test
    public void testUpdateShlefColumn() throws MethodFailureException {
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
    public void testUpdateShlefRow() throws MethodFailureException {
        Shelf shelf = newShelf(2, 4, 45.0D, 10, true);
        manager.createShelf(shelf);
        shelf = manager.findShelfById(shelf.getId());

        shelf.setRow(2);
        manager.updateShelf(shelf);
        assertEquals(2, shelf.getColumn());
        assertEquals(2, shelf.getRow());
        assertEquals(45.0D, shelf.getMaxWeight(), 0.01D);
        assertEquals(10, shelf.getCapacity());
        assertEquals(true, shelf.isSecure());
    }

    @Test
    public void testUpdateShlefMaxWeight() throws MethodFailureException {
        Shelf shelf = newShelf(2, 4, 45.0D, 10, true);
        manager.createShelf(shelf);
        shelf = manager.findShelfById(shelf.getId());

        shelf.setMaxWeight(25.0D);
        manager.updateShelf(shelf);
        assertEquals(2, shelf.getColumn());
        assertEquals(4, shelf.getRow());
        assertEquals(25.00D, shelf.getMaxWeight(), 0.01D);
        assertEquals(10, shelf.getCapacity());
        assertEquals(true, shelf.isSecure());
    }

    @Test
    public void testUpdateShlefCapacity() throws MethodFailureException {
        Shelf shelf = newShelf(2, 4, 45.0D, 10, true);
        manager.createShelf(shelf);
        shelf = manager.findShelfById(shelf.getId());

        shelf.setCapacity(4);
        manager.updateShelf(shelf);
        assertEquals(2, shelf.getColumn());
        assertEquals(4, shelf.getRow());
        assertEquals(45.0D, shelf.getMaxWeight(), 0.01D);
        assertEquals(4, shelf.getCapacity());
        assertEquals(true, shelf.isSecure());
    }

    @Test
    public void testUpdateShlefSecure() throws MethodFailureException {
        Shelf shelf = newShelf(2, 4, 45.0D, 10, true);
        manager.createShelf(shelf);
        shelf = manager.findShelfById(shelf.getId());

        shelf.setSecure(false);
        manager.updateShelf(shelf);
        assertEquals(2, shelf.getColumn());
        assertEquals(4, shelf.getRow());
        assertEquals(45.0D, shelf.getMaxWeight(), 0.01D);
        assertEquals(10, shelf.getCapacity());
        assertEquals(false, shelf.isSecure());
    }

    @Test(expected = NullPointerException.class)
    public void testUpdateShelfByIdWithNull() throws MethodFailureException {
        manager.updateShelf(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateShelfByIdWithWrongId() throws MethodFailureException {
        Shelf shelf = newShelf(2, 4, 45.00D, 10, true);
        manager.createShelf(shelf);
        shelf = manager.findShelfById(shelf.getId());

        shelf.setId(-1);
        manager.updateShelf(shelf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateShelfByIdWithWrongColumn() throws MethodFailureException {
        Shelf shelf = newShelf(2, 4, 45.00D, 10, true);
        manager.createShelf(shelf);
        shelf = manager.findShelfById(shelf.getId());

        shelf.setColumn(-1);
        manager.updateShelf(shelf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateShelfByIdWithWrongRow() throws MethodFailureException {
        Shelf shelf = newShelf(2, 4, 45.00D, 10, true);
        manager.createShelf(shelf);
        shelf = manager.findShelfById(shelf.getId());

        shelf.setRow(-1);
        manager.updateShelf(shelf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateShelfByIdWithWrongMaxWeight() throws MethodFailureException {
        Shelf shelf = newShelf(2, 4, 45.00D, 10, true);
        manager.createShelf(shelf);
        shelf = manager.findShelfById(shelf.getId());

        shelf.setMaxWeight(0.0D);
        manager.updateShelf(shelf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateShelfByIdWithMinusMaxWeight() throws MethodFailureException {
        Shelf shelf = newShelf(2, 4, 45.00D, 10, true);
        manager.createShelf(shelf);
        shelf = manager.findShelfById(shelf.getId());

        shelf.setMaxWeight(-1.0D);
        manager.updateShelf(shelf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateShelfByIdWithWrongCapacity() throws MethodFailureException {
        Shelf shelf = newShelf(2, 4, 45.00D, 10, true);
        manager.createShelf(shelf);
        shelf = manager.findShelfById(shelf.getId());

        shelf.setCapacity(0);
        manager.updateShelf(shelf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateShelfByIdWithMinusCapacity() throws MethodFailureException {
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
