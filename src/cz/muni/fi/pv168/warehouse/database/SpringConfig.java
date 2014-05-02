package cz.muni.fi.pv168.warehouse.database;

import cz.muni.fi.pv168.warehouse.managers.ItemManagerImpl;
import cz.muni.fi.pv168.warehouse.managers.ShelfManagerImpl;
import cz.muni.fi.pv168.warehouse.managers.WarehouseManagerImpl;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Spring Java configuration class. See http://static.springsource.org/spring/docs/current/spring-framework-reference/html/beans.html#beans-java
 *
 * @author Martin Zaťko
 */
@Configuration  //je to konfigurace pro Spring
@EnableTransactionManagement //bude řídit transakce u metod označených @Transactional
public class SpringConfig {

    @Bean
    public DataSource dataSource() {

        BasicDataSource bds = new BasicDataSource();
        Properties prop = new Properties();

        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("config.properties")) {
            prop.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        bds.setDriverClassName(prop.getProperty("driverClassName"));
        bds.setUrl(prop.getProperty("url"));
        bds.setUsername(prop.getProperty("username"));
        bds.setPassword(prop.getProperty("password"));
        return bds;
    }

    @Bean //potřeba pro @EnableTransactionManagement
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    public WarehouseManagerImpl warehouseManager() {
        WarehouseManagerImpl warehouseManager = new WarehouseManagerImpl();
        warehouseManager.setDataSource(new TransactionAwareDataSourceProxy(dataSource()));
        return warehouseManager;
    }

    @Bean
    public ShelfManagerImpl shelfManager() {
        ShelfManagerImpl shelfManager = new ShelfManagerImpl();
        shelfManager.setDataSource(new TransactionAwareDataSourceProxy(dataSource()));
        return shelfManager;
    }

    @Bean
    public ItemManagerImpl itemManager() {
        ItemManagerImpl itemManager = new ItemManagerImpl();
        itemManager.setDataSource(new TransactionAwareDataSourceProxy(dataSource()));
        return itemManager;
    }
}
