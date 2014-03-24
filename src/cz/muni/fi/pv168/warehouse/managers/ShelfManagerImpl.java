package cz.muni.fi.pv168.warehouse.managers;

import cz.muni.fi.pv168.warehouse.entities.Shelf;

import java.sql.*;
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
            logger.log(Level.SEVERE, "Crash while inserting into DB.", e);
            return null;
        }
    }

    @Override
    public Shelf deleteShelf(Shelf shelf) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Shelf> listAllShelves() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Shelf findShelfById(int id) {
        try (PreparedStatement query = con.prepareStatement("SELECT col,row,maxWeight,capacity,secure " +
                "FROM shelf WHERE id = ?")) {

            query.setInt(1, id);

            ResultSet rs = query.executeQuery();
            if (rs.next()) {

                Shelf shelf = new Shelf();
                shelf.setId(id);
                shelf.setColumn(rs.getInt("col"));
                shelf.setRow(rs.getInt("row"));
                shelf.setMaxWeight(rs.getDouble("maxWeight"));
                shelf.setCapacity(rs.getInt("capacity"));
                shelf.setSecure(rs.getBoolean("secure"));

                return shelf;
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
        throw new UnsupportedOperationException("Not supported yet.");
    }
}