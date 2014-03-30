package cz.muni.fi.pv168.warehouse.managers;

import cz.muni.fi.pv168.warehouse.entities.Item;
import cz.muni.fi.pv168.warehouse.entities.Shelf;
import cz.muni.fi.pv168.warehouse.exceptions.MethodFailureException;
import cz.muni.fi.pv168.warehouse.exceptions.ShelfAttributeException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class will serve to manage warehouse.
 * @author Oliver Mrázik & Martin Zaťko
 * @version 2014-03-30
 */
public class WarehouseManagerImpl implements WarehouseManager {

    private final static Logger logger = Logger.getLogger(WarehouseManagerImpl.class.getName());
    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("Error: Data source is not set");
        }
    }

    //TODO co ked item nema shelf ??? Preco exceptiony ? nemala by byt aj moznost s NULL?

    @Override
    public Shelf findShelfWithItem(Item item) throws MethodFailureException {
        int shelfid;
        Shelf shelf;

        checkDataSource();
        checkObject(item);

        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement query = con.prepareStatement("SELECT ADMIN.ITEM.shelfid FROM ADMIN.ITEM JOIN " +
                    "ADMIN.SHELF ON ADMIN.SHELF.id = ADMIN.ITEM.shelfid WHERE ADMIN.ITEM.id = ?")) {
                query.setInt(1, item.getId());
                try (ResultSet rs = query.executeQuery()) {
                    if (rs.next()) {
                        shelfid = rs.getInt("shelfid");
                        if (rs.next()) {
                            throw new SQLException("Error: More rows with same id found");
                        }
                    } else {
                        throw new SQLException("Something went wrong while withdrawing from db.");
                    }

                    try (PreparedStatement query1 = con.prepareStatement("SELECT id,col,row,maxWeight,capacity,secure " +
                            "FROM ADMIN.SHELF WHERE id = ?")) {
                        query1.setInt(1, shelfid);
                        try (ResultSet rs1 = query1.executeQuery()) {
                            if (rs1.next()) {
                                shelf = fillShelf(rs1);
                                if (rs1.next()) {
                                    throw new SQLException("Error: More rows with same id found");
                                }
                                return shelf;
                            } else {
                                throw new SQLException("Something went wrong while withdrawing from db.");
                            }
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error finding item", ex);
            throw new MethodFailureException("Error finding item", ex);
        }
    }

    @Override
    public List<Item> listAllItemsOnShelf(Shelf shelf) throws MethodFailureException {
        checkDataSource();
        checkObject(shelf);

        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement query = con.prepareStatement("SELECT ADMIN.ITEM.id, weight, insertionDate, " +
                    "storeDays, dangerous FROM ADMIN.ITEM JOIN ADMIN.SHELF ON ADMIN.SHELF.id = ADMIN.ITEM.shelfid " +
                    "WHERE ADMIN.SHELF.id = ?")) {
                query.setInt(1, shelf.getId());

                try (ResultSet rs = query.executeQuery()) {
                    List<Item> list = new ArrayList<>();
                    while (rs.next()) {
                       list.add(fillItem(rs));
                    }
                    return list;
                }
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error listing all items on shelf", ex);
            throw new MethodFailureException("Error listing all items on shelf", ex);
        }
    }

    @Override
    public void putItemOnShelf(Shelf shelf, Item item) throws MethodFailureException, ShelfAttributeException {
        checkDataSource();
        checkObject(shelf);
        checkObject(item);

        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement query = con.prepareStatement("UPDATE ADMIN.ITEM SET shelfid = ? WHERE id = ? AND " +
                    "SHELFID IS NULL")) {
                query.setInt(1, shelf.getId());
                query.setInt(2, item.getId());

                checkShelfAttribute(shelf, item);

                if (query.executeUpdate() != 1) {
                    throw new SQLException("Error: More than 1 item with that id");
                }
                con.commit();
            } catch (SQLException ex) {
                try {
                    if (con != null) {
                        con.rollback();
                    }
                } catch (SQLException ex1) {
                    logger.log(Level.SEVERE, "Error rollback database", ex1);
                    throw new MethodFailureException("Error rollback database", ex1);
                } finally {
                    try {
                        if (con != null) {
                            con.setAutoCommit(true);
                        }
                    } catch (SQLException ex2) {
                        logger.log(Level.SEVERE, "Error setting autoCommit to true", ex2);
                    }
                }
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error putting item on shelf", ex);
            throw new MethodFailureException("Error putting item on shelf", ex);
        }
    }

    @Override
    public Item withdrawItemFromShelf(Item item) throws MethodFailureException {
        checkDataSource();
        checkObject(item);

        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement query = con.prepareStatement("UPDATE ADMIN.ITEM SET shelfid = NULL WHERE id = ?")) {
                query.setInt(1, item.getId());

                if (query.executeUpdate() != 1) {
                    throw new SQLException("Error: Item can't be withdraw");
                }
                con.commit();
                return item;
            } catch (SQLException ex) {
                if (con != null) {
                    con.rollback();
                }
                logger.log(Level.SEVERE, "Error rollback database", ex);
                throw new MethodFailureException("Error rollback database", ex);
            } finally {
                try {
                    if (con != null) {
                        con.setAutoCommit(true);
                    }
                } catch (SQLException ex2) {
                    logger.log(Level.SEVERE, "Error setting autoCommit to true", ex2);
                }
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Crash while updating DB.", ex);
            throw new MethodFailureException("Crash while updating DB.", ex);
        }
    }

    @Override
    public List<Item> removeAllExpiredItems(Date currentDate) throws MethodFailureException {
        checkDataSource();

        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement query = con.prepareStatement("SELECT id, weight, insertiondate, storedays, " +
                    "dangerous FROM ADMIN.ITEM")) {
                try (ResultSet rs = query.executeQuery()) {
                    List<Item> list = new ArrayList<>();
                    while (rs.next()) {
                        Item temp = fillItem(rs);
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(temp.getInsertionDate());
                        cal.add(Calendar.DATE, temp.getStoreDays());
                        if (cal.getTime().compareTo(currentDate) > 0) {
                            list.add(temp);
                            withdrawItemFromShelf(temp);
                        }
                    }
                    return list;
                }
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error removing all expired items", ex);
            throw new MethodFailureException("Error removing all expired items", ex);
        }
    }

    @Override
    public List<Shelf> listShelvesWithSomeFreeSpace() throws MethodFailureException {
        checkDataSource();

        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement query = con.prepareStatement("SELECT ADMIN.SHELF.id, col, row, maxWeight, " +
                    "capacity, secure FROM ADMIN.SHELF LEFT JOIN ADMIN.ITEM ON ADMIN.SHELF.id = ADMIN.ITEM.shelfid " +
                    "GROUP BY ADMIN.SHELF.id, col, row, maxWeight, capacity, secure HAVING COUNT (ADMIN.ITEM.id) < capacity")) {
                try (ResultSet rs = query.executeQuery()) {
                    List<Shelf> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(fillShelf(rs));
                    }
                    return list;
                }
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error listing shelves with some free space", ex);
            throw new MethodFailureException("Error listing shelves with some free space", ex);
        }
    }

    @Override
    public List<Item> listAllItemsWithoutShelf() throws MethodFailureException {
        checkDataSource();

        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement query = con.prepareStatement("SELECT id, weight, insertiondate, storedays, " +
                    "dangerous FROM ADMIN.ITEM WHERE shelfid IS NULL")) {
                try (ResultSet rs = query.executeQuery()) {
                    List<Item> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(fillItem(rs));
                    }
                    return list;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error listing items without shelf", e);
            throw new MethodFailureException("Error listing items without shelf", e);
        }
    }
    /**
     * Method creates shelf from table result set.
     * @param rs table result set.
     * @return newly created shelf.
     * @throws SQLException when some attribute is null.
     */
    private Shelf fillShelf(ResultSet rs) throws SQLException{
        Shelf shelf = new Shelf();
        shelf.setId(rs.getInt("id"));
        shelf.setColumn(rs.getInt("col"));
        shelf.setRow(rs.getInt("row"));
        shelf.setMaxWeight(rs.getDouble("maxWeight"));
        shelf.setCapacity(rs.getInt("capacity"));
        shelf.setSecure(rs.getBoolean("secure"));
        return shelf;
    }
    /**
     * Method creates item from table result set.
     * @param rs table result set.
     * @return newly created item.
     * @throws SQLException when some attribute is null.
     */
    private Item fillItem(ResultSet rs) throws SQLException{
        Item item = new Item();
        item.setId(rs.getInt("id"));
        item.setWeight(rs.getDouble("weight"));
        item.setInsertionDate(rs.getDate("insertionDate"));
        item.setStoreDays(rs.getInt("storeDays"));
        item.setDangerous(rs.getBoolean("dangerous"));
        return item;
    }

    /**
     * Method checks if given object is not null. (Valid objects are Shelf and Item).
     * @param obj tested object.
     */
    private void checkObject(Object obj) {
        if(obj instanceof Item) {
            if (((Item) obj).getId() == null) {
                throw new NullPointerException("id");
            }
        }
        if(obj instanceof Shelf) {
            if (((Shelf) obj).getId() == null) {
                throw new NullPointerException("id");
            }
        }
    }

    /**
     * Method checks if inserting item into shelf is valid.
     * @param shelf given shelf.
     * @param item given item.
     * @throws MethodFailureException when listing all actual items on shelf throw exception.
     * @throws ShelfAttributeException when any of the conditions will not be met.
     */
    private void checkShelfAttribute(Shelf shelf, Item item) throws MethodFailureException, ShelfAttributeException {
        double weight = 0;
        List<Item> list = listAllItemsOnShelf(shelf);
        for (Item i : list) {
            weight += i.getWeight();
        }

        if (shelf.getMaxWeight() < weight + item.getWeight()) {
            throw new ShelfAttributeException("weight");
        }
        if (shelf.getCapacity() < list.size() + 1) {
            throw new ShelfAttributeException("capacity");
        }
        if (shelf.isSecure() != item.isDangerous()) {
            throw new ShelfAttributeException("security");
        }
    }
}