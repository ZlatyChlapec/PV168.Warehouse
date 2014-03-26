package cz.muni.fi.pv168.warehouse.managers;

import cz.muni.fi.pv168.warehouse.entities.Shelf;
import cz.muni.fi.pv168.warehouse.exceptions.MethodFailureException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
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

    private DataSource ds;

    public ShelfManagerImpl(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public Shelf createShelf(Shelf shelf) throws MethodFailureException {
        try (Connection con = ds.getConnection()) {
            try (PreparedStatement query = con.prepareStatement("INSERT INTO shelf(col, row, maxWeight, capacity," +
                    "secure) VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {

                con.setAutoCommit(false);
                checkAllExceptId(shelf);
                if (shelf.getId() != null) {
                    throw new IllegalArgumentException("Id can't be set before.");
                }

                query.setInt(1, shelf.getColumn());
                query.setInt(2, shelf.getRow());
                query.setDouble(3, shelf.getMaxWeight());
                query.setInt(4, shelf.getCapacity());
                query.setBoolean(5, shelf.isSecure());

                if (query.executeUpdate() == 1) {
                    try (ResultSet rs = query.getGeneratedKeys()) {
                        while (rs.next()) {
                            shelf.setId(rs.getInt(1));
                        }
                        con.commit();
                        con.setAutoCommit(true);
                        return shelf;
                    }
                } else {
                    throw new SQLException("Too many inserts.");
                }
            } catch (SQLException e) {
                try {
                    con.rollback();
                } catch (SQLException e1) {
                    logger.log(Level.SEVERE, "Crash while inserting into DB.", e1);
                    throw new MethodFailureException("Crash while inserting into DB.", e1);
                }
                logger.log(Level.SEVERE, "Crash while inserting into DB.", e);
                throw new MethodFailureException("Crash while inserting into DB.", e);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Crash while inserting into DB.", e);
            throw new MethodFailureException("Crash while inserting into DB.", e);
        }
    }

    @Override
    public Shelf deleteShelf(Shelf shelf) throws MethodFailureException {
        if (shelf.getId() == null || shelf == null) {
            throw new NullPointerException("Id or shelf was null.");
        }

        try (Connection con = ds.getConnection()) {
            try (PreparedStatement query = con.prepareStatement("DELETE FROM shelf WHERE id = ?")) {
                query.setInt(1, shelf.getId());
                int counter = query.executeUpdate();
                if (counter != 1) {
                    throw new SQLException("I think you deleted more then one record.");
                }
                return shelf;
            } catch (SQLException e) {
                try {
                    con.rollback();
                } catch (SQLException e1) {
                    logger.log(Level.SEVERE, "Crash while inserting into DB.", e1);
                    throw new MethodFailureException("Crash while inserting into DB.", e1);
                }
                logger.log(Level.SEVERE, "Crash while inserting into DB.", e);
                throw new MethodFailureException("Crash while inserting into DB.", e);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Crash while inserting into DB.", e);
            throw new MethodFailureException("Crash while inserting into DB.", e);
        }
    }

    @Override
    public List<Shelf> listAllShelves() throws MethodFailureException {
        List<Shelf> list = new ArrayList<>();

        try (Connection con = ds.getConnection()) {
            try (PreparedStatement query = con.prepareStatement("SELECT id,col,row,maxWeight,capacity,secure FROM shelf")) {
                try (ResultSet rs = query.executeQuery()) {
                    while (rs.next()) {
                        list.add(fillShelf(rs));
                    }
                    return list;
                }
            } catch (SQLException e) {
                try {
                    con.rollback();
                } catch (SQLException e1) {
                    logger.log(Level.SEVERE, "Crash while inserting into DB.", e1);
                    throw new MethodFailureException("Crash while inserting into DB.", e1);
                }
                logger.log(Level.SEVERE, "Crash while inserting into DB.", e);
                throw new MethodFailureException("Crash while inserting into DB.", e);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Crash while inserting into DB.", e);
            throw new MethodFailureException("Crash while inserting into DB.", e);
        }
    }

    @Override
    public Shelf findShelfById(int id) throws MethodFailureException {
        try (Connection con = ds.getConnection()) {
            try (PreparedStatement query = con.prepareStatement("SELECT id,col,row,maxWeight,capacity,secure " +
                    "FROM shelf WHERE id = ?")) {
                query.setInt(1, id);
                ResultSet rs = query.executeQuery();

                if (rs.next()) {
                    Shelf shelf = fillShelf(rs);
                    if (rs.next()) {
                        throw new SQLException("Withdraw more than was supposed to.");
                    }
                    return shelf;
                } else
                    throw new SQLException("Nothing withdrew.");

            } catch (SQLException e) {
                try {
                    con.rollback();
                } catch (SQLException e1) {
                    logger.log(Level.SEVERE, "Crash while inserting into DB.", e1);
                    throw new MethodFailureException("Crash while inserting into DB.", e1);
                }
                logger.log(Level.SEVERE, "Crash while inserting into DB.", e);
                throw new MethodFailureException("Crash while inserting into DB.", e);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Crash while inserting into DB.", e);
            throw new MethodFailureException("Crash while inserting into DB.", e);
        }
    }

    @Override
    public void updateShelf(Shelf shelf) throws MethodFailureException {
        try (Connection con = ds.getConnection()) {
            try (PreparedStatement query = con.prepareStatement("UPDATE shelf SET col = ?,row = ?," +
                    "maxWeight = ?,capacity = ?,secure = ? WHERE id = ?")) {

                checkAllExceptId(shelf);
                if (shelf.getId() == null) {
                    throw new NullPointerException("id");
                }
                if (shelf.getId() < 0) {
                    throw new IllegalArgumentException("id smaller then zero");
                }

                query.setInt(1, shelf.getColumn());
                query.setInt(2, shelf.getRow());
                query.setDouble(3, shelf.getMaxWeight());
                query.setInt(4, shelf.getCapacity());
                query.setBoolean(5, shelf.isSecure());
                query.setInt(6, shelf.getId());

                int counter = query.executeUpdate();
                if (counter != 1) {
                    throw new SQLException("Tried to update more than one row.");
                }
            } catch (SQLException e) {
                try {
                    con.rollback();
                } catch (SQLException e1) {
                    logger.log(Level.SEVERE, "Crash while inserting into DB.", e1);
                    throw new MethodFailureException("Crash while inserting into DB.", e1);
                }
                logger.log(Level.SEVERE, "Crash while inserting into DB.", e);
                throw new MethodFailureException("Crash while inserting into DB.", e);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Crash while inserting into DB.", e);
            throw new MethodFailureException("Crash while inserting into DB.", e);
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

    private void checkAllExceptId(Shelf shelf) {
        if (shelf == null) {
            throw new NullPointerException("shelf");
        }
        if (shelf.getColumn() < 0) {
            throw new IllegalArgumentException("column smaller then zero");
        }
        if (shelf.getRow() < 0) {
            throw new IllegalArgumentException("row smaller then zero");
        }
        if (shelf.getMaxWeight() <= 0.0D) {
            throw new IllegalArgumentException("maxWeight can't be zero and lower");
        }
        if (shelf.getCapacity() <= 0) {
            throw new IllegalArgumentException("capacity can't be zero and lower");
        }
    }
}