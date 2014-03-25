package cz.muni.fi.pv168.warehouse.managers;

import cz.muni.fi.pv168.warehouse.entities.Item;
import cz.muni.fi.pv168.warehouse.exceptions.MethodFailureException;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class will serve to manage items.
 *
 * @author Oliver Mrázik & Martin Zaťko
 * @version 0.1
 */
public class ItemManagerImpl implements ItemManager {
    private Connection connection;
    public static final Logger logger = Logger.getLogger(ItemManagerImpl.class.getName());

    public ItemManagerImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createItem(Item item) throws MethodFailureException {

        itemCheck(item);
        if (item.getId() != null) {
            throw new NullPointerException("Error createItem: item is already set");
        }

        try (PreparedStatement query = connection.prepareStatement("INSERT INTO ADMIN.ITEM(WEIGHT, STOREDAYS, DANGEROUS) " +
                "VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            connection.setAutoCommit(false);
            query.setDouble(1, item.getWeight());
            query.setInt(2, item.getStoreDays());
            query.setBoolean(3, item.isDangerous());

            int addedRows = query.executeUpdate();
            if (addedRows != 1) {
                throw new MethodFailureException("Error: More than 1 row added");
            }

            ResultSet rs = query.getGeneratedKeys();
            if (rs.next()) {
                if (rs.getMetaData().getColumnCount() != 1) {
                    throw new MethodFailureException("Error: More than 1 key found");
                }
                item.setId(rs.getInt(1));
            }
            connection.commit();

        } catch (SQLException ex) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex1) {
                logger.log(Level.SEVERE, "Error rollback database", ex1);
                throw new MethodFailureException("Error rollback database", ex1);
            }
            logger.log(Level.SEVERE, "Error creating item", ex);
            throw new MethodFailureException("Error creating item", ex);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ex) {
                    logger.log(Level.SEVERE, "Error setting autoCommit to true", ex);
                }
//            if (query != null) {
//                try {
//                    query.close();
//                } catch (SQLException ex) {
//                    logger.log(Level.SEVERE, "Error closing preparedStatement", ex);
//                }
//            }
//            if (connection != null) {
//                try {
//                    connection.setAutoCommit(true);
//                } catch (SQLException ex){
//                    logger.log(Level.SEVERE, "Error setting autoCommit to true", ex);
//                }
//                try {
//                    connection.close();
//                } catch (SQLException ex){
//                    logger.log(Level.SEVERE, "Error closing connection", ex);
//                }
//            }
            }
        }
    }

    @Override
    public Item deleteItem(Item item) throws MethodFailureException {

        try (PreparedStatement query = connection.prepareStatement("DELETE FROM ADMIN.ITEM WHERE ID = ?")) {
            //connection = DriverManager.getConnection("jdbc:derby://localhost:1527/datab");
            connection.setAutoCommit(false);
            //query = connection.prepareStatement("DELETE FROM ADMIN.ITEM WHERE ID = ?");
            query.setInt(1, item.getId());

            int deletedRows = query.executeUpdate();
            if (deletedRows != 1) {
                throw new MethodFailureException("Error: More than 1 row deleted");
            }

            connection.commit();
            return item;

        } catch (SQLException ex) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex1) {
                logger.log(Level.SEVERE, "Error rollback database", ex1);
                throw new MethodFailureException("Error rollback database", ex1);
            }
            logger.log(Level.SEVERE, "Error deleting item", ex);
            throw new MethodFailureException("Error deleting item", ex);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ex) {
                    logger.log(Level.SEVERE, "Error setting autoCommit to true", ex);
                }
            }
        }
    }

    @Override
    public List<Item> listAllItems() throws MethodFailureException {
        List<Item> result = new ArrayList<>();

        try (PreparedStatement query = connection.prepareStatement("SELECT ID, WEIGHT, INSERTIONDATE, STOREDAYS, DANGEROUS FROM ADMIN.ITEM")) {
            //connection = DriverManager.getConnection("jdbc:derby://localhost:1527/datab");
            //query = connection.prepareStatement("SELECT ID, WEIGHT, INSERTIONDATE, STOREDAYS, DANGEROUS FROM ADMIN.ITEM");

            ResultSet rs = query.executeQuery();
            while (rs.next()) {
                Item item = resultToItem(rs);
                result.add(item);
            }

            return result;

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error listing all items", ex);
            throw new MethodFailureException("Error listing all items", ex);
        }
    }

    @Override
    public Item findItemById(int id) throws MethodFailureException {

        try (PreparedStatement query = connection.prepareStatement("SELECT ID, WEIGHT, INSERTIONDATE, STOREDAYS, DANGEROUS FROM ADMIN.ITEM WHERE ID = ?")) {
            //connection = DriverManager.getConnection("jdbc:derby://localhost:1527/datab");
            //query = connection.prepareStatement("SELECT ID, WEIGHT, INSERTIONDATE, STOREDAYS, DANGEROUS FROM ADMIN.ITEM WHERE ID = ?");
            query.setInt(1, id);

            ResultSet rs = query.executeQuery();
            if (rs.next()) {
                Item item = resultToItem(rs);
                if (rs.next()) {
                    throw new MethodFailureException("Error: More rows with same id found");
                }
                return item;
            } else {
                return null;
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error finding item", ex);
            throw new MethodFailureException("Error finding item", ex);
        }
    }

    @Override
    public Item updateItem(Item item) throws MethodFailureException {

        itemCheck(item);
        if (item.getId() == null) {
            throw new NullPointerException("Error updateItem: item id is null");
        }
        if (item.getId() <= 0) {
            throw new IllegalArgumentException("Error updateItem: item id is null");
        }

        try (PreparedStatement query = connection.prepareStatement("UPDATE ADMIN.ITEM SET WEIGHT =?, STOREDAYS =?, DANGEROUS=? WHERE ID =?")) {
            //connection = DriverManager.getConnection("jdbc:derby://localhost:1527/datab");
            //query = connection.prepareStatement("UPDATE ADMIN.ITEM SET WEIGHT =?, STOREDAYS =?, DANGEROUS=? WHERE ID =?");
            query.setDouble(1, item.getWeight());
            query.setInt(2, item.getStoreDays());
            query.setBoolean(3, item.isDangerous());
            query.setInt(4, item.getId());

            int updatedRows = query.executeUpdate();
            if (updatedRows != 1) {
                throw new MethodFailureException("Error: More than 1 items with that id");
            }

            connection.commit();
            return item;

        } catch (SQLException ex) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex1) {
                logger.log(Level.SEVERE, "Error rollback database", ex1);
                throw new MethodFailureException("Error rollback database", ex1);
            }
            logger.log(Level.SEVERE, "Error updating item", ex);
            throw new MethodFailureException("Error updating item", ex);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ex) {
                    logger.log(Level.SEVERE, "Error setting autoCommit to true", ex);
                }
            }
        }
    }

    @Override
    public Date getExpiration(Item item) throws MethodFailureException {

        try (PreparedStatement query = connection.prepareStatement("SELECT INSERTIONDATE, STOREDAYS FROM ADMIN.ITEM WHERE ID =?")) {
            //connection = DriverManager.getConnection("jdbc:derby://localhost:1527/datab");
            //query = connection.prepareStatement("SELECT INSERTIONDATE, STOREDAYS FROM ADMIN.ITEM WHERE ID =?");
            query.setInt(1, item.getId());
            ResultSet rs = query.executeQuery();

            if (rs.next()) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(rs.getDate("insertiondate"));
                cal.add(Calendar.DATE, rs.getInt("storedays"));
                if (rs.next()) {
                    throw new MethodFailureException("Error: More rows with same id found");
                }
                return cal.getTime();
            } else {
                return null;
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving expiration", ex);
            throw new MethodFailureException("Error retrieving expiration", ex);
        }
    }

    private void itemCheck(Item item) {
        if (item == null) {
            throw new NullPointerException("Error: item = null");
        }
        if (item.getWeight() <= 0) {
            throw new IllegalArgumentException("Error: item weight is 0 or negative number");
        }
        if (item.getStoreDays() <= 0) {
            throw new IllegalArgumentException("Error: item storedays is 0 or negative number");
        }
    }

    private Item resultToItem(ResultSet rs) throws SQLException {
        Item item = new Item();
        item.setId(rs.getInt("ID"));
        item.setWeight(rs.getDouble("WEIGHT"));
        item.setInsertionDate(rs.getDate("INSERTIONDATE"));
        item.setStoreDays(rs.getInt("STOREDAYS"));
        item.setDangerous(rs.getBoolean("DANGEROUS"));
        return item;
    }
}
