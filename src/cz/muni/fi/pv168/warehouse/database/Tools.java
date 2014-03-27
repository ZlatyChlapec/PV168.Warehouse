package cz.muni.fi.pv168.warehouse.database;

import cz.muni.fi.pv168.warehouse.exceptions.MethodFailureException;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.Connection;

/**
 * @author Slapy
 * @version 26.3.2014
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

        try {
            StringBuilder result = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));

            String line = bufferedReader.readLine();
            while (line != null) {
                result.append(line);
                line = bufferedReader.readLine();
            }

            return result.toString().split(";");

        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Reading of file was not successful.", ex);
            throw new MethodFailureException("Reading of file was not successful.", ex);
        }
    }

    /**
     * Method executes sql queries.
     * @param dataSource database data source.
     * @param url .sql file url.
     * @throws MethodFailureException when preparedStatement() or connection fails.
     */
    public static void executeSQL(DataSource dataSource, URL url) throws MethodFailureException {

        try (Connection connection = dataSource.getConnection()){

            for (String sqlStatement : sqlParser(url)) {
                connection.prepareStatement(sqlStatement).executeUpdate();
            }

            connection.commit();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Crash while executing commands DB.", ex);
            throw new MethodFailureException("Crash while executing commands DB.", ex);
        }
    }
}