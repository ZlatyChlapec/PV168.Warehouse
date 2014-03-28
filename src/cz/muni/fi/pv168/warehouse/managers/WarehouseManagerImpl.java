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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class will serve to manage warehouse.
 *
 * @author Oliver Mrázik & Martin Zaťko
 * @version 0.1
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

    @Override
    public Shelf findShelfWithItem(Item item) throws MethodFailureException {
        int shelfid;
        Shelf shelf;

        checkDataSource();
        checkItem(item);

        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement query = con.prepareStatement("SELECT ADMIN.ITEM.shelfid FROM ADMIN.ITEM JOIN " +
                    "ADMIN.SHELF ON ADMIN.SHELF.id = ADMIN.ITEM.shelfid WHERE ADMIN.ITEM.id = ?")) {
                query.setInt(1, item.getId());
                try (ResultSet rs = query.executeQuery()) {
                    if (rs.next()) {
                        shelfid = rs.getInt("shelfid");
                        if (rs.next()) {
                            throw new SQLException("Too many things in rs.");
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
                                    throw new SQLException("Too many things in rs.");
                                }
                                return shelf;
                            } else {
                                throw new SQLException("Something went wrong while withdrawing from db.");
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Crash while searching for item.", e);
            throw new MethodFailureException("Crash while searching for item.", e);
        }
    }

    @Override
    public List<Item> listAllItemsOnShelf(Shelf shelf) throws MethodFailureException {
        checkDataSource();
        checkShelf(shelf);

        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement query = con.prepareStatement("SELECT ADMIN.ITEM.id,weight,insertionDate," +
                    "storeDays,dangerous FROM ADMIN.ITEM JOIN ADMIN.SHELF ON ADMIN.SHELF.id = ADMIN.ITEM.shelfid " +
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
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Crash while reading DB.", e);
            throw new MethodFailureException("Crash while reading DB.", e);
        }
    }

    @Override
    public void putItemOnShelf(Shelf shelf, Item item) throws MethodFailureException, ShelfAttributeException {
        checkDataSource();
        checkShelf(shelf);
        checkItem(item);

        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement query = con.prepareStatement("UPDATE ADMIN.ITEM SET shelfid = ? WHERE id = ?")) {
                query.setInt(1, shelf.getId());
                query.setInt(2, item.getId());

                checkShelfAttribute(shelf, item);

                if (query.executeUpdate() != 1) {
                    throw new SQLException("Something went wrong while putting on shelf.");
                }
            } catch (SQLException e) {
                con.rollback();
                logger.log(Level.SEVERE, "Crash while updating DB.", e);
                throw new MethodFailureException("Crash while updating DB.", e);
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Crash while updating DB.", e);
            throw new MethodFailureException("Crash while updating DB.", e);
        }
    }

    @Override
    public Item withdrawItemFromShelf(Shelf shelf, Item item) throws MethodFailureException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Item> removeAllExpiredItems(Date currentDate) throws MethodFailureException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Shelf> listShelvesWithSomeFreeSpace() throws MethodFailureException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Item> listAllItemsWithoutShelf() throws MethodFailureException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

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

    private Item fillItem(ResultSet rs) throws SQLException{
        Item item = new Item();
        item.setId(rs.getInt("id"));
        item.setWeight(rs.getDouble("weight"));
        item.setInsertionDate(rs.getDate("insertionDate"));
        item.setStoreDays(rs.getInt("storeDays"));
        item.setDangerous(rs.getBoolean("dangerous"));
        return item;
    }

    private void checkItem(Item item) {
        if (item.getId() == null) {
            throw new NullPointerException("id");
        }
    }

    private void checkShelf(Shelf shelf) {
        if (shelf.getId() == null) {
            throw new NullPointerException("id");
        }
    }

    private void checkShelfAttribute(Shelf shelf, Item item) throws MethodFailureException, ShelfAttributeException {
        int weight = 0;
        List<Item> list = listAllItemsOnShelf(shelf);
        for (Item i : list) {
            weight += i.getWeight();
        }

        if (shelf.getMaxWeight() < weight + item.getWeight()) {
            throw new ShelfAttributeException("ShelfWeightException");
        }
        if (shelf.getCapacity() < list.size() + 1) {
            throw new ShelfAttributeException("ShelfCapacityException");
        }
        if (shelf.isSecure() != item.isDangerous()) {
            throw new ShelfAttributeException("ShelfSecurityException");
        }
    }
}