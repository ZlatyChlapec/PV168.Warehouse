package cz.muni.fi.pv168.warehouse.db;

import org.apache.derby.jdbc.ClientConnectionPoolDataSource;

import javax.naming.NamingException;

/**
 * @author Slapy
 * @version 26.3.2014
 */
public class Connection {

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
}
