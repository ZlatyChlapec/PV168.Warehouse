package cz.muni.fi.pv168.warehouse.database;

import cz.muni.fi.pv168.warehouse.exceptions.MethodFailureException;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains methods for parsing SQL scripts.
 * @author Oliver Mrázik & Martin Zaťko
 * @version 2014-03-29
 */
public class Tools {

    private final static Logger logger = Logger.getLogger(Tools.class.getName());

    /**
     * Method parse sql document to array of sql queries.
     * @param url .sql file url.
     * @return array of sql queries.
     * @throws MethodFailureException when IOException is caught.
     */
    public static String[] sqlParser(URL url) throws MethodFailureException {

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
            StringBuilder result = new StringBuilder();

            String line = bufferedReader.readLine();
            while (line != null) {
                result.append(line);
                line = bufferedReader.readLine();
            }

            return result.toString().split(";");

        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Reading from URL was not successful.", ex);
            throw new MethodFailureException("Reading from URL was not successful.", ex);
        }
    }

    /**
     * Method executes sql queries.
     * @param dataSource database data source.
     * @param url .sql file url.
     * @throws MethodFailureException when preparedStatement() or connection fails.
     */
    public static void executeSQL(DataSource dataSource, URL url) throws MethodFailureException {

        try (Connection connection = dataSource.getConnection()) {

            try {
                connection.setAutoCommit(false);

                for (String sqlStatement : sqlParser(url)) {
                    connection.prepareStatement(sqlStatement).executeUpdate();
                }

                connection.commit();
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Crash while executing commands DB.", ex);
            throw new MethodFailureException("Crash while executing commands DB.", ex);
        }
    }
}