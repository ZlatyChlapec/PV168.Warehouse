package cz.muni.fi.pv168.warehouse.managers;

import cz.muni.fi.pv168.warehouse.entities.Shelf;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class will serve to manage items.
 *
 * @author Oliver Mrázik & Martin Zaťko
 * @version 0.1
 */
public class ShelfManagerImpl implements ShelfManager {

    private final static Logger logger = Logger.getLogger(ShelfManagerImpl.class .getName());

    private Connection con;

    public ShelfManagerImpl(Connection con) {
        this.con = con;
    }

    @Override
    public Shelf createShelf(Shelf shelf) throws IllegalArgumentException {
        try (PreparedStatement query = con.prepareStatement("INSERT INTO shelf(col, row, maxWeight, capacity, secure)" +
                "VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {

            if (shelf == null)
                throw new NullPointerException("shelf");
            if (shelf.getId() != null)
                throw new IllegalArgumentException("null id");
            if (shelf.getColumn() < 0)
                throw new IllegalArgumentException("column smaller then zero");
            if (shelf.getRow() < 0)
                throw new IllegalArgumentException("row smaller then zero");
            if (shelf.getMaxWeight() <= 0.0D)
                throw new IllegalArgumentException("zero or minus maxWeight");
            if (shelf.getCapacity() <= 0)
                throw new IllegalArgumentException("zero or minus capacity");

            query.setInt(1, shelf.getColumn());
            query.setInt(2, shelf.getRow());
            query.setDouble(3, shelf.getMaxWeight());
            query.setInt(4, shelf.getCapacity());
            query.setBoolean(5, shelf.isSecure());

            if (query.executeUpdate() == 1) {
                ResultSet rs = query.getGeneratedKeys();
                while (rs.next()) {
                    shelf.setId(rs.getInt(1));
                }
                return shelf;
            } else {
                throw new SQLException("Too many inserts.");
            }
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException e1) {
                logger.log(Level.SEVERE, "Crash while inserting into DB.", e1);
            }
            logger.log(Level.SEVERE, "Crash while inserting into DB.", e);
            return null;
        }
    }

    @Override
    public Shelf deleteShelf(Shelf shelf) {

        if (shelf.getId() == null || shelf == null)
            throw new NullPointerException("Id or shelf was null.");

        try (PreparedStatement query = con.prepareStatement("DELETE FROM shelf WHERE id = ?")) {

            query.setInt(1, shelf.getId());

            int counter = query.executeUpdate();

            if (counter != 1)
                throw new SQLException("I think you deleted more then one record.");
            return shelf;
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException e1) {
                logger.log(Level.SEVERE, "Crash while deleting.", e1);
            }
            logger.log(Level.SEVERE, "Crash while deleting.", e);
            return null;
        }
    }

    @Override
    public List<Shelf> listAllShelves() {

        List<Shelf> list = new LinkedList<>();
        List<Shelf> temp = new LinkedList<>();

        try (PreparedStatement query = con.prepareStatement("SELECT id,col,row,maxWeight,capacity,secure FROM shelf")) {

            ResultSet rs = query.executeQuery();

            while (rs.next()) {
                temp.add(fillShelf(rs));
            }

            list.addAll(temp);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Crash while withdrawing from DB.", e);
            return list;
        }
        return list;
    }

    @Override
    public Shelf findShelfById(int id) {
        try (PreparedStatement query = con.prepareStatement("SELECT id,col,row,maxWeight,capacity,secure " +
                "FROM shelf WHERE id = ?")) {

            query.setInt(1, id);

            ResultSet rs = query.executeQuery();
            if (rs.next()) {

                return fillShelf(rs);
            }
            else
                throw new SQLException("Nothing withdrew.");


        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Crash while withdrawing from DB.", e);
            return null;
        }
    }

    @Override
    public void updateShelf(Shelf shelf) {
        try {
            PreparedStatement query = con.prepareStatement("UPDATE shelf SET col = ?,row = ?," +
                    "maxWeight = ?,capacity = ?,secure = ? WHERE id = ?");

            if (shelf == null)
                throw new NullPointerException("null shelf");
            if (shelf.getId() == null)
                throw new NullPointerException("null id");
            if (shelf.getId() < 0)
                throw new IllegalArgumentException("id smaller then zero");
            if (shelf.getColumn() < 0)
                throw new IllegalArgumentException("column smaller then zero");
            if (shelf.getRow() < 0)
                throw new IllegalArgumentException("row smaller then zero");
            if (shelf.getMaxWeight() <= 0.0D)
                throw new IllegalArgumentException("zero or minus maxWeight");
            if (shelf.getCapacity() <= 0)
                throw new IllegalArgumentException("zero or minus capacity");

            query.setInt(1, shelf.getColumn());
            query.setInt(2, shelf.getRow());
            query.setDouble(3, shelf.getMaxWeight());
            query.setInt(4, shelf.getCapacity());
            query.setBoolean(5, shelf.isSecure());
            query.setInt(6, shelf.getId());

            int counter = query.executeUpdate();
            if (counter != 1)
                throw new SQLException("Tried to update more than one row.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Crash while updating.", e);
        }
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
}