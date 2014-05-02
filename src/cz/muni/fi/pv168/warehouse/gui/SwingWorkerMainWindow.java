package cz.muni.fi.pv168.warehouse.gui;

import cz.muni.fi.pv168.warehouse.database.SpringConfig;
import cz.muni.fi.pv168.warehouse.entities.Item;
import cz.muni.fi.pv168.warehouse.exceptions.MethodFailureException;
import cz.muni.fi.pv168.warehouse.managers.ItemManager;
import cz.muni.fi.pv168.warehouse.managers.ItemManagerImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Slapy
 */
public class SwingWorkerMainWindow extends SwingWorker<List<Item>, Void> {


    private ItemManager itemManager;

    @Override
    protected List<Item> doInBackground() throws Exception {
        List<Item> list = new ArrayList<>();
        ApplicationContext springContext = new AnnotationConfigApplicationContext(SpringConfig.class);
        itemManager = springContext.getBean("itemManager", ItemManagerImpl.class);
        try {
             list.addAll(itemManager.listAllItems());
        } catch (MethodFailureException e) {
            e.printStackTrace();
        }
        return list;
    }
}
