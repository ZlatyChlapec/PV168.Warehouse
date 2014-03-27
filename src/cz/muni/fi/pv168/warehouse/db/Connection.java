package cz.muni.fi.pv168.warehouse.db;

import cz.muni.fi.pv168.warehouse.exceptions.MethodFailureException;
import org.apache.derby.jdbc.ClientConnectionPoolDataSource;

import javax.naming.NamingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Slapy
 * @version 26.3.2014
 */
public class Connection {

    private final static Logger logger = Logger.getLogger(Connection.class .getName());

    public static ClientConnectionPoolDataSource ConnectionData() throws NamingException {
        ClientConnectionPoolDataSource cpds = new ClientConnectionPoolDataSource();
        cpds.setMaxStatements(20);
        cpds.setServerName("localhost");
        cpds.setPortNumber(1527);
        cpds.setDatabaseName("datab");
        cpds.setUser("admin");
        cpds.setPassword("admin");
        return cpds;
    }

    public static String[] sqlReaderParse(BufferedReader br) throws MethodFailureException {
        StringBuilder result = new StringBuilder("");

        try {
            String line = br.readLine();

            while (line != null) {
                result.append(line);
                line = br.readLine();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Reading of file was not successful.", e);
            throw new MethodFailureException("Reading of file was not successful.", e);
        }
        return result.toString().split(";");
    }

    public static void executeSQL(java.sql.Connection con, InputStream is) throws MethodFailureException {
        try {
            String[] createTables = sqlReaderParse(new BufferedReader(new InputStreamReader(is)));

            for (String s : createTables) {
                con.prepareStatement(s).executeUpdate();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Crash while inserting into DB.", e);
            throw new MethodFailureException("Crash while inserting into DB.", e);
        }
    }
}