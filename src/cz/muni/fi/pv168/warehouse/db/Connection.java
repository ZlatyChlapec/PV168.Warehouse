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

    private final static Logger logger = Logger.getLogger(Connection.class.getName());

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

    public static String[] sqlParser(BufferedReader br) throws MethodFailureException {
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

    /**
     * This method will execute all commands from {@code is} on {@code con}.
     * @param con connection on which we want to use commands
     * @param is to get is use executeSQL(con, className.class.getResourceAsStream("fileName")); where class
     *           name is name of class in same folder as file we want to read and fileName is name of file with
     *           ending we want to read. Sure if you know how you can use different way.
     * @throws MethodFailureException if executing fail this error will show up
     */
    public static void executeSQL(java.sql.Connection con, InputStream is) throws MethodFailureException {
        try {
            con.setAutoCommit(false);
            String[] commands = sqlParser(new BufferedReader(new InputStreamReader(is)));
            int counter = 0;
            for (String s : commands) {
                con.prepareStatement(s).executeUpdate();
            }
            if (counter != commands.length) {
                throw new SQLException("Something went wrong while executing.");
            }
            con.commit();
            con.setAutoCommit(true);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Crash while executing commands DB.", e);
            throw new MethodFailureException("Crash while executing commands DB.", e);
        }
    }
}