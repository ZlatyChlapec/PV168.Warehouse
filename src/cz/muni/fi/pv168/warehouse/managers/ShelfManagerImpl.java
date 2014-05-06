package cz.muni.fi.pv168.warehouse.managers;

import cz.muni.fi.pv168.warehouse.entities.Shelf;
import cz.muni.fi.pv168.warehouse.exceptions.MethodFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class will serve to manage items.
 * @author Oliver Mrázik & Martin Zaťko
 * @version 2014-03-30
 */
public class ShelfManagerImpl implements ShelfManager {

    public static final Logger logger = LoggerFactory.getLogger(ShelfManager.class);
    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void checkDataSource() {
        if (dataSource == null) {
            logger.error("Error: Data source is not set");
            throw new IllegalStateException("Error: Data source is not set");
        }
    }

    @Override
    public Shelf createShelf(Shelf shelf) throws MethodFailureException {
        checkDataSource();
        checkAllExceptId(shelf);
        if (shelf.getId() != null) {
            logger.error("Error: shelf id is already set");
            throw new IllegalArgumentException("Error: shelf id is already set");
        }

        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement query = con.prepareStatement("INSERT INTO ADMIN.SHELF(col, row, maxWeight, " +
                    "capacity, secure) VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                con.setAutoCommit(false);
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
                        return shelf;
                    }
                } else {
                    logger.error("Error: More than 1 row added");
                    throw new SQLException("Error: More than 1 row added");
                }
            } catch (SQLException ex) {
                try {
                    if(con != null) {
                        con.rollback();
                    }
                } catch (SQLException ex1) {
                    logger.error("Error rollback database", ex1);
                    throw new MethodFailureException("Error rollback database", ex1);
                } finally {
                    try {
                        if (con != null) {
                            con.setAutoCommit(true);
                        }
                    } catch (SQLException ex2) {
                        logger.error("Error setting autoCommit to true", ex2);
                    }
                }
                logger.error("Error creating shelf", ex);
                throw new MethodFailureException("Error creating shelf", ex);
            }
        } catch (SQLException ex) {
            logger.error("Error creating shelf", ex);
            throw new MethodFailureException("Error creating shelf", ex);
        }
    }

    @Override
    public Shelf deleteShelf(Shelf shelf) throws MethodFailureException {
        checkDataSource();
        if (shelf == null) {
            logger.error("Error: shelf = null");
            throw new NullPointerException("Error: shelf = null");
        }
        if (shelf.getId() == null) {
            logger.error("Error: shelf id is no set");
            throw new NullPointerException("Error: shelf id is no set");
        }

        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement query = con.prepareStatement("DELETE FROM ADMIN.SHELF WHERE id = ?")) {
                query.setInt(1, shelf.getId());
                con.setAutoCommit(false);
                int counter = query.executeUpdate();
                if (counter != 1) {
                    logger.error("Error: More than 1 row deleted");
                    throw new SQLException("Error: More than 1 row deleted");
                }

                con.commit();
                return shelf;
            } catch (SQLException ex) {
                try {
                    if (con != null) {
                        con.rollback();
                    }
                } catch (SQLException ex1) {
                    logger.error("Error rollback database", ex1);
                    throw new MethodFailureException("Error rollback database", ex1);
                } finally {
                    try {
                        if (con != null) {
                            con.setAutoCommit(true);
                        }
                    } catch (SQLException ex2) {
                        logger.error("Error setting autoCommit to true", ex2);
                    }
                }
                logger.error("Error deleting shelf", ex);
                throw new MethodFailureException("Error deleting shelf", ex);
            }
        } catch (SQLException ex) {
            logger.error("Error deleting shelf", ex);
            throw new MethodFailureException("Error deleting shelf", ex);
        }
    }

    @Override
    public List<Shelf> listAllShelves() throws MethodFailureException {
        checkDataSource();
        List<Shelf> list = new ArrayList<>();
        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement query = con.prepareStatement("SELECT id,col,row,maxWeight,capacity,secure " +
                    "FROM ADMIN.SHELF")) {
                try (ResultSet rs = query.executeQuery()) {
                    while (rs.next()) {
                        list.add(fillShelf(rs));
                    }
                    return list;
                }
            }
        } catch (SQLException ex) {
            logger.error("Error listing all shelves", ex);
            throw new MethodFailureException("Error listing all shelves", ex);
        }
    }

    @Override
    public Shelf findShelfById(int id) throws MethodFailureException {
        checkDataSource();
        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement query = con.prepareStatement("SELECT id,col,row,maxWeight,capacity,secure " +
                    "FROM ADMIN.SHELF WHERE id = ?")) {
                query.setInt(1, id);
                ResultSet rs = query.executeQuery();
                if (rs.next()) {
                    Shelf shelf = fillShelf(rs);
                    if (rs.next()) {
                        logger.error("Error: More rows with same id found");
                        throw new SQLException("Error: More rows with same id found");
                    }
                    return shelf;
                } else
                    logger.error("Error: No such shelf found");
                    throw new SQLException("Error: No such shelf found");
            }
        } catch (SQLException ex) {
            logger.error("Error finding shelf", ex);
            throw new MethodFailureException("Error finding shelf", ex);
        }
    }

    @Override
    public void updateShelf(Shelf shelf) throws MethodFailureException {
        checkDataSource();
        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement query = con.prepareStatement("UPDATE ADMIN.SHELF SET col = ?,row = ?," +
                    "maxWeight = ?,capacity = ?,secure = ? WHERE id = ?")) {

                con.setAutoCommit(false);
                checkAllExceptId(shelf);
                if (shelf.getId() == null) {
                    logger.error("Error: shelf id is null");
                    throw new NullPointerException("Error: shelf id is null");
                }
                if (shelf.getId() <= 0) {
                    logger.error("Error: shelf id is not below/equal zero");
                    throw new IllegalArgumentException("Error: shelf id is not below/equal zero");
                }

                query.setInt(1, shelf.getColumn());
                query.setInt(2, shelf.getRow());
                query.setDouble(3, shelf.getMaxWeight());
                query.setInt(4, shelf.getCapacity());
                query.setBoolean(5, shelf.isSecure());
                query.setInt(6, shelf.getId());

                int counter = query.executeUpdate();
                if (counter != 1) {
                    logger.error("Error: More than 1 shelf with that id");
                    throw new SQLException("Error: More than 1 shelf with that id");
                }
                con.commit();

            } catch (SQLException ex) {
                try {
                    if (con != null) {
                        con.rollback();
                    }
                } catch (SQLException ex1) {
                    logger.error("Error rollback database", ex1);
                    throw new MethodFailureException("Error rollback database", ex1);
                } finally {
                    try {
                        if (con != null) {
                            con.setAutoCommit(true);
                        }
                    } catch (SQLException ex2) {
                        logger.error("Error setting autoCommit to true", ex2);
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("Error updating shelf", ex);
            throw new MethodFailureException("Error updating shelf", ex);
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
     * Method checks if item is not null; weight and store days are bigger than zero.
     * @param shelf shelf we want to have checked.
     */
    private void checkAllExceptId(Shelf shelf) {
        if (shelf == null) {
            logger.error("shelf");
            throw new NullPointerException("shelf");
        }
        if (shelf.getColumn() < 0) {
            logger.error("column smaller then zero");
            throw new IllegalArgumentException("column smaller then zero");
        }
        if (shelf.getRow() < 0) {
            logger.error("row smaller then zero");
            throw new IllegalArgumentException("row smaller then zero");
        }
        if (shelf.getMaxWeight() <= 0.0D) {
            logger.error("maxWeight can't be zero and lower");
            throw new IllegalArgumentException("maxWeight can't be zero and lower");
        }
        if (shelf.getCapacity() <= 0) {
            logger.error("capacity can't be zero and lower");
            throw new IllegalArgumentException("capacity can't be zero and lower");
        }
    }
}