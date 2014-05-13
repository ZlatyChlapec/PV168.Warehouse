package cz.muni.fi.pv168.warehouse.gui;

import cz.muni.fi.pv168.warehouse.database.SpringConfig;
import cz.muni.fi.pv168.warehouse.entities.Item;
import cz.muni.fi.pv168.warehouse.entities.Shelf;
import cz.muni.fi.pv168.warehouse.exceptions.MethodFailureException;
import cz.muni.fi.pv168.warehouse.managers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * All GUI features and logic.
 * @author Oliver Mrázik & Martin Zaťko
 * @version 2014-05-12
 */
public class MainWindow extends JFrame {

    public static final Logger logger = LoggerFactory.getLogger(MainWindow.class);

    private static final int decNum = 2;

    private ResourceBundle myResources;
    private InsertItemFrame updateItemFrame;
    private InsertShelfFrame updateShelfFrame;
    private WarehouseManagerImpl warehouseManager;
    private MainWindow window;

    public MainWindow() {

        window = this;
        ApplicationContext springContext = new AnnotationConfigApplicationContext(SpringConfig.class);
        warehouseManager = springContext.getBean("warehouseManager", WarehouseManagerImpl.class);
        try {
            myResources = ResourceBundle.getBundle("cz/muni/fi/pv168/warehouse/resources/lang", Locale.getDefault());
        } catch (MissingResourceException e) {
            myResources = ResourceBundle.getBundle("cz/muni/fi/pv168/warehouse/resources/lang", new Locale("en", "GB"));
            JOptionPane.showMessageDialog(window, printOut("bundleNotFound"), printOut("info"), JOptionPane.INFORMATION_MESSAGE);
            logger.info("Default resource bundle not found", e);
        }
        initComponents();
    }

    private String printOut(String value) {
        return myResources.getString(value);
    }

    public static double round(double value) {

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(decNum, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private void insertItemButtonActionPerformed(ActionEvent e) {
        SwingWorkerAddItem swingWorker;
        insertItemButton.setEnabled(false);

        Item item = new Item();
        item.setWeight(round(Double.parseDouble(weightSpinner.getValue().toString())));
        item.setStoreDays(Integer.parseInt(storeDaysSpinner.getValue().toString()));
        item.setDangerous(dangerousCheckBox.isSelected());
        if (Math.abs(item.getWeight() - 0.01) <= 0.01 && item.getStoreDays() == 1 && !item.isDangerous()) {
            int result = JOptionPane.showConfirmDialog(this, printOut("insertDefualtItem"), printOut("warning"), JOptionPane.YES_NO_OPTION);
            if (result == 0) {
                swingWorker = new SwingWorkerAddItem(item);
                swingWorker.execute();
            } else {
                insertItemButton.setEnabled(true);
            }
        } else {
            swingWorker = new SwingWorkerAddItem(item);
            swingWorker.execute();
        }
    }

    class SwingWorkerAddItem extends SwingWorker<Item, Void> {
        private ItemManager itemManager;
        private Item item = new Item();
        private boolean inserted = false;

        public SwingWorkerAddItem(Item item) {
            this.item = item;
        }

        @Override
        protected Item doInBackground() throws Exception {
            ApplicationContext springContext = new AnnotationConfigApplicationContext(SpringConfig.class);
            itemManager = springContext.getBean("itemManager", ItemManagerImpl.class);
            try {
                for (Shelf shelf : warehouseManager.listShelvesWithSomeFreeSpace()) {
                    double totalWeight = 0;

                    for (Item item : warehouseManager.listAllItemsOnShelf(shelf)) {
                        totalWeight += item.getWeight();
                    }

                    if (shelf.isSecure() == item.isDangerous() && shelf.getMaxWeight() >= totalWeight + item.getWeight()) {
                        itemManager.createItem(item);
                        warehouseManager.putItemOnShelf(shelf, item);
                        inserted = true;
                        break;
                    }
                }

                if (!inserted) {
                    itemManager.createItem(item);
                }
                return item;
            } catch (MethodFailureException ex) {
                logger.error(ex.getMessage(), ex);
                throw new ExecutionException(ex);
            }
        }

        @Override
        protected void done() {
            listAllShelves();
            listAllItems(-1);
            try {
                get();
                if (!inserted) {
                    JOptionPane.showMessageDialog(window, printOut("noSuitableShelf"), printOut("info"), JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Interrupted Exception", e);
            }

            weightSpinner.setValue(0.01);
            storeDaysSpinner.setValue(1);
            dangerousCheckBox.setSelected(false);
            insertItemButton.setEnabled(true);
        }
    }

    private void insertShelfButtonActionPerformed(ActionEvent e) {
        SwingWorkerAddShelf swingWorker;
        insertShelfButton.setEnabled(false);

        Shelf shelf = new Shelf();
        shelf.setColumn(Integer.parseInt(columnSpinner.getValue().toString()));
        shelf.setRow(Integer.parseInt(rowSpinner.getValue().toString()));
        shelf.setCapacity(Integer.parseInt(capacitySpinner.getValue().toString()));
        shelf.setMaxWeight(round(Double.parseDouble(maxWeightSpinner.getValue().toString())));
        shelf.setSecure(secureCheckBox.isSelected());

        if (Math.abs(shelf.getMaxWeight() - 0.01) <= 0.01 && shelf.getColumn() == 1 && shelf.getRow() == 1 && shelf.getCapacity() == 1 && !shelf.isSecure()) {
            int result = JOptionPane.showConfirmDialog(this, printOut("insertDefualtShelf"), printOut("warning"), JOptionPane.YES_NO_OPTION);
            if (result == 0) {
                swingWorker = new SwingWorkerAddShelf(shelf);
                swingWorker.execute();
            } else {
                insertShelfButton.setEnabled(true);
            }
        } else {
            swingWorker = new SwingWorkerAddShelf(shelf);
            swingWorker.execute();
        }
    }

    class SwingWorkerAddShelf extends SwingWorker<Shelf, Void> {
        private ShelfManager shelfManager;
        private Shelf shelf = new Shelf();
        private boolean duplicity = false;

        public SwingWorkerAddShelf(Shelf shelf) {
            this.shelf = shelf;
        }

        @Override
        protected Shelf doInBackground() throws Exception {
            ApplicationContext springContext = new AnnotationConfigApplicationContext(SpringConfig.class);
            shelfManager = springContext.getBean("shelfManager", ShelfManagerImpl.class);
            try {
                for (Shelf s : shelfManager.listAllShelves()) {
                    if (s.getColumn() == shelf.getColumn() && s.getRow() == shelf.getRow()) {
                        duplicity = true;
                    }
                }
                if (!duplicity) {
                    shelfManager.createShelf(shelf);
                } else {
                    shelf = null;
                }
            } catch (MethodFailureException ex) {
                logger.error(ex.getMessage(), ex);
                throw new ExecutionException(ex);
            }
            return shelf;
        }

        @Override
        protected void done() {
            listAllShelves();
            listAllItems(-1);
            try {
                get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error(e.getMessage(), e);
            }

            if (duplicity) {
                JOptionPane.showMessageDialog(window, printOut("duplicateShelfCoords"), printOut("error"), JOptionPane.ERROR_MESSAGE);
            }

            insertShelfButton.setEnabled(true);
            columnSpinner.setValue(0);
            rowSpinner.setValue(0);
            capacitySpinner.setValue(1);
            maxWeightSpinner.setValue(0.01);
            secureCheckBox.setSelected(false);
        }
    }

    private void deleteItemButtonActionPerformed(ActionEvent e) {
        SwingWorkerDeleteItem swingWorker;

        if (itemsTable.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, printOut("notSelectedRow"), printOut("info"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(window, printOut("deletedConfirmation"), printOut("warning"), JOptionPane.YES_NO_OPTION);
        if (result == 0) {
            deleteItemButton.setEnabled(false);
            swingWorker = new SwingWorkerDeleteItem((Integer) itemsTable.getModel().getValueAt(itemsTable.getSelectedRow(), 0));
            swingWorker.execute();
        }
    }

    class SwingWorkerDeleteItem extends SwingWorker<Item, Void> {
        private ItemManager itemManager;
        private int id;
        private Item item = new Item();

        public SwingWorkerDeleteItem(int id) {
            this.id = id;
        }

        @Override
        protected Item doInBackground() throws Exception {
            ApplicationContext springContext = new AnnotationConfigApplicationContext(SpringConfig.class);
            itemManager = springContext.getBean("itemManager", ItemManagerImpl.class);
            try {
                item = itemManager.findItemById(id);
                itemManager.deleteItem(item);

            } catch (MethodFailureException ex) {
                logger.error(ex.getMessage(), ex);
                throw new ExecutionException(ex);
            }
            return item;
        }

        @Override
        protected void done() {
            listAllShelves();
            listAllItems(-1);
            try {
                get();
                JOptionPane.showMessageDialog(window, printOut("deleteItemSuccess"), printOut("deleted"), JOptionPane.INFORMATION_MESSAGE);
            } catch (InterruptedException | ExecutionException e) {
                logger.error(e.getMessage(),e);
                JOptionPane.showMessageDialog(window, printOut("deleteError"), printOut("error"), JOptionPane.ERROR_MESSAGE);
            }
            deleteItemButton.setEnabled(true);
        }
    }

    private void deleteShelfButtonActionPerformed(ActionEvent e) {
        SwingWorkerDeleteShelf swingWorker;

        if (shelvesTable.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, printOut("notSelectedRow"), printOut("info"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(window, printOut("deletedConfirmation"), printOut("warning"), JOptionPane.YES_NO_OPTION);

        if (result == 0) {
            deleteShelfButton.setEnabled(false);
            swingWorker = new SwingWorkerDeleteShelf((Integer) shelvesTable.getModel().getValueAt(shelvesTable.getSelectedRow(), 0));
            swingWorker.execute();
        }
    }

    class SwingWorkerDeleteShelf extends SwingWorker<Shelf, Void> {
        private boolean containItems = false;
        private ShelfManager shelfManager;
        private int id;
        private Shelf shelf = new Shelf();

        public SwingWorkerDeleteShelf(int id) {
            this.id = id;
        }

        @Override
        protected Shelf doInBackground() throws Exception {
            ApplicationContext springContext = new AnnotationConfigApplicationContext(SpringConfig.class);
            shelfManager = springContext.getBean("shelfManager", ShelfManagerImpl.class);
            try {
                    shelf = shelfManager.findShelfById(id);
                    if (warehouseManager.listAllItemsOnShelf(shelf).size() != 0) {
                        containItems = true;
                    } else {
                        shelfManager.deleteShelf(shelf);
                    }
            } catch (MethodFailureException ex) {
                logger.error(ex.getMessage(), ex);
                throw new ExecutionException(ex);
            }
            return shelf;
        }

        @Override
        protected void done() {
            listAllShelves();
            listAllItems(-1);
            try {
                get();
                if (containItems) {
                    JOptionPane.showMessageDialog(window, printOut("shelfNotEmpty"), printOut("error"), JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(window, printOut("deleteShelfSuccess"), printOut("deleted"), JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (InterruptedException | ExecutionException e) {
                logger.error(e.getMessage(), e);
                JOptionPane.showMessageDialog(window, printOut("deleteError"), printOut("error"), JOptionPane.ERROR_MESSAGE);
            }

            deleteShelfButton.setEnabled(true);
        }
    }

    private void updateItemButtonActionPerformed(ActionEvent e) {
        if (itemsTable.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, printOut("notSelectedRowEdit"), printOut("info"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int id = (Integer)itemsTable.getModel().getValueAt(itemsTable.getSelectedRow(), 0);
        SwingWorkerLoadItem swingWorker = new SwingWorkerLoadItem(id);
        swingWorker.execute();

        updateItemFrame = new InsertItemFrame();
        updateItemFrame.setVisible(true);
        disableAllButtons();
    }

    class SwingWorkerLoadItem extends SwingWorker<Item, Void> {
        private int id;

        public SwingWorkerLoadItem(int id) {
            this.id = id;
        }

        @Override
        protected Item doInBackground() throws Exception {
            ApplicationContext springContext = new AnnotationConfigApplicationContext(SpringConfig.class);
            ItemManager itemManager = springContext.getBean("itemManager", ItemManagerImpl.class);
            try {
                return itemManager.findItemById(id);
            } catch (MethodFailureException ex) {
                logger.error(ex.getMessage(), ex);
                throw new ExecutionException(ex);
            }
        }

        @Override
        protected void done() {
            try {
                Item item = get();
                updateItemFrame.weightSpinner.setValue(item.getWeight());
                updateItemFrame.storeDaysSpinner.setValue(item.getStoreDays());
                updateItemFrame.dangerousCheckBox.setSelected(item.isDangerous());
            } catch (InterruptedException | ExecutionException e) {
                logger.error(e.getMessage(), e);
                JOptionPane.showMessageDialog(window, printOut("readingDB"), printOut("error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateShelfButtonActionPerformed(ActionEvent e) {
        if (shelvesTable.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, printOut("notSelectedRowEdit"), printOut("info"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int id = (Integer)shelvesTable.getModel().getValueAt(shelvesTable.getSelectedRow(), 0);
        SwingWorkerLoadShelf swingWorker = new SwingWorkerLoadShelf(id);
        swingWorker.execute();

        updateShelfFrame = new InsertShelfFrame();
        updateShelfFrame.setVisible(true);
        disableAllButtons();
    }

    class SwingWorkerLoadShelf extends SwingWorker<Shelf, Void> {
        private int id;

        public SwingWorkerLoadShelf(int id) {
            this.id = id;
        }

        @Override
        protected Shelf doInBackground() throws Exception {
            ApplicationContext springContext = new AnnotationConfigApplicationContext(SpringConfig.class);
            ShelfManager shelfManager = springContext.getBean("shelfManager", ShelfManagerImpl.class);
            try {
                return shelfManager.findShelfById(id);
            } catch (MethodFailureException ex) {
                logger.error(ex.getMessage(), ex);
                throw new ExecutionException(ex);
            }
        }

        @Override
        protected void done() {
            try {
                Shelf shelf = get();
                updateShelfFrame.columnSpinner.setValue(shelf.getColumn());
                updateShelfFrame.rowSpinner.setValue(shelf.getRow());
                updateShelfFrame.maxWeightSpinner.setValue(shelf.getMaxWeight());
                updateShelfFrame.capacitySpinner.setValue(shelf.getCapacity());
                updateShelfFrame.secureCheckBox.setSelected(shelf.isSecure());
            } catch (InterruptedException | ExecutionException e) {
                logger.error(e.getMessage(), e);
                JOptionPane.showMessageDialog(window, printOut("readingDB"), printOut("error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String getExpirationTime(Date insertionDate, int storeDays) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(insertionDate);
        cal.add(Calendar.DATE, storeDays);
        SimpleDateFormat myFormat = new SimpleDateFormat("dd-MM-yyyy");
        return myFormat.format(cal.getTime());
    }

    private void listAllItems(int selectedItem) {

        DefaultTableModel model = (DefaultTableModel) itemsTable.getModel();
        model.setRowCount(0);
        SwingWorkerListAllItems worker = new SwingWorkerListAllItems();
        worker.execute();

        try {
            Map<Item, Shelf> map = worker.get();
            for (Item i : map.keySet()) {
                Date insertionDate = i.getInsertionDate();
                int storeDays = i.getStoreDays();
                Calendar cal = Calendar.getInstance();
                cal.setTime(insertionDate);
                cal.add(Calendar.DATE, storeDays);

                Shelf shelf = map.get(i);
                StringBuilder coords = new StringBuilder("");
                if (shelf != null) {
                    coords.append(shelf.getColumn());
                    coords.append(" & ");
                    coords.append(shelf.getRow());
                } else {
                    coords.append("");
                }

                model.addRow(new Object[]{i.getId(), i.getWeight(), getExpirationTime(insertionDate, storeDays),
                        i.isDangerous(), cal.getTime().before(new Date()), coords.toString()});
            }
            if (selectedItem != -1) {
                itemsTable.setRowSelectionInterval(selectedItem, selectedItem);
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void listAllShelves() {

        DefaultTableModel model = (DefaultTableModel) shelvesTable.getModel();
        model.setRowCount(0);
        SwingWorkerListAllShelves worker = new SwingWorkerListAllShelves();
        worker.execute();

        try {
            for (Shelf s : worker.get()) {
                model.addRow(new Object[]{s.getId(), s.getColumn(), s.getRow(), s.getMaxWeight(), s.getCapacity(), s.isSecure()});
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage(), e);
        }
    }

    class SwingWorkerListAllItems extends SwingWorker<Map<Item, Shelf>, Void> {

        private ItemManager itemManager;

        @Override
        protected Map<Item, Shelf> doInBackground() throws Exception {
            Map<Item, Shelf> list = new HashMap<>();
            ApplicationContext springContext = new AnnotationConfigApplicationContext(SpringConfig.class);
            itemManager = springContext.getBean("itemManager", ItemManagerImpl.class);
            try {
                for (Item i : itemManager.listAllItems()) {

                    list.put(i, warehouseManager.findShelfWithItem(i));
                }
                return list;
            } catch (MethodFailureException e) {
                logger.error(e.getMessage(), e);
                throw new ExecutionException(e);
            }
        }
    }

    class SwingWorkerListAllShelves extends SwingWorker<List<Shelf>, Void> {
        private ShelfManager shelfManager;

        @Override
        protected List<Shelf> doInBackground() throws Exception {
            List<Shelf> list = new ArrayList<>();
            ApplicationContext springContext = new AnnotationConfigApplicationContext(SpringConfig.class);
            shelfManager = springContext.getBean("shelfManager", ShelfManagerImpl.class);
            try {
                list.addAll(shelfManager.listAllShelves());
            } catch (MethodFailureException e) {
                logger.error(e.getMessage(), e);
            }
            return list;
        }
    }

    private void editItemButtonActionPerformed(ActionEvent e) {
        SwingWorkerUpdateItem swingWorker;

        Item item = new Item();
        int id = (Integer)itemsTable.getModel().getValueAt(itemsTable.getSelectedRow(), 0);
        item.setId(id);
        item.setWeight(round(Double.parseDouble(updateItemFrame.weightSpinner.getValue().toString())));
        item.setStoreDays(Integer.parseInt(updateItemFrame.storeDaysSpinner.getValue().toString()));
        item.setDangerous(updateItemFrame.dangerousCheckBox.isSelected());

        updateItemButton.setEnabled(false);

        swingWorker = new SwingWorkerUpdateItem(item);
        swingWorker.execute();
    }


    class SwingWorkerUpdateItem extends SwingWorker<Item, Void> {
        private boolean dangerousChanged = false;
        private ItemManager itemManager;
        private Item item;

        public SwingWorkerUpdateItem(Item item) {
            this.item = item;
        }

        @Override
        protected Item doInBackground() throws Exception {
            ApplicationContext springContext = new AnnotationConfigApplicationContext(SpringConfig.class);
            itemManager = springContext.getBean("itemManager", ItemManagerImpl.class);

            try {
                Shelf shelf = warehouseManager.findShelfWithItem(item);
                if (shelf != null) {
                    double totalWeight = 0;
                    for (Item i : warehouseManager.listAllItemsOnShelf(shelf)) {
                        totalWeight += i.getWeight();
                    }
                    if (shelf.getMaxWeight() >= (totalWeight - itemManager.findItemById(item.getId()).getWeight())
                            + item.getWeight()) {
                        if (item.isDangerous() != itemManager.findItemById(item.getId()).isDangerous()) {
                            dangerousChanged = true;
                            warehouseManager.withdrawItemFromShelf(item);
                            itemManager.updateItem(item);
                        } else {
                            itemManager.updateItem(item);
                        }
                    } else {
                        return null;
                    }
                } else {
                    itemManager.updateItem(item);
                    return item;
                }
            } catch (MethodFailureException e) {
                logger.error(e.getMessage(), e);
                throw new ExecutionException(e);
            }
            return item;
        }

        @Override
        protected void done() {
            listAllShelves();
            listAllItems(-1);
            try {
                get();
                if (get() != null) {
                    JOptionPane.showMessageDialog(window, printOut("updateItemSuccess"), printOut("updated"), JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(window, printOut("weightOvercome"), printOut("error"), JOptionPane.ERROR_MESSAGE);
                }
                if (dangerousChanged) {
                    JOptionPane.showMessageDialog(window, printOut("itemDroped"), printOut("error"), JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (InterruptedException | ExecutionException e) {
                logger.error(e.getMessage(), e);
                JOptionPane.showMessageDialog(window, printOut("updateError"), printOut("error"), JOptionPane.ERROR_MESSAGE);
            }
            updateItemFrame.dispose();
            enableAllButtons();
        }
    }

    private void cancelInsertItemButtonActionPerformed(ActionEvent e) {
        updateItemFrame.dispose();
        enableAllButtons();
    }

    private void editShelfButtonActionPerformed(ActionEvent e) {
        SwingWorkerUpdateShelf swingWorker;

        Shelf shelf = new Shelf();
        int id = (Integer)shelvesTable.getModel().getValueAt(shelvesTable.getSelectedRow(), 0);
        shelf.setId(id);
        shelf.setColumn(Integer.parseInt(updateShelfFrame.columnSpinner.getValue().toString()));
        shelf.setRow(Integer.parseInt(updateShelfFrame.rowSpinner.getValue().toString()));
        shelf.setMaxWeight(round(Double.parseDouble(updateShelfFrame.maxWeightSpinner.getValue().toString())));
        shelf.setCapacity(Integer.parseInt(updateShelfFrame.capacitySpinner.getValue().toString()));
        shelf.setSecure(updateShelfFrame.secureCheckBox.isSelected());

        updateShelfButton.setEnabled(false);

        swingWorker = new SwingWorkerUpdateShelf(shelf);
        swingWorker.execute();
    }

    class SwingWorkerUpdateShelf extends SwingWorker<List<Item>, Void> {
        private boolean maxWeight = false;
        private boolean capcityError = false;
        private boolean changedSecurity = false;
        private ShelfManager shelfManager;
        private Shelf shelf;

        public SwingWorkerUpdateShelf(Shelf shelf) {
            this.shelf = shelf;
        }

        @Override
        protected List<Item> doInBackground() throws Exception {
            ApplicationContext springContext = new AnnotationConfigApplicationContext(SpringConfig.class);
            shelfManager = springContext.getBean("shelfManager", ShelfManagerImpl.class);

            try {
                int capacity = warehouseManager.listAllItemsOnShelf(shelf).size();
                double weight = 0;
                for (Item item : warehouseManager.listAllItemsOnShelf(shelf)) {
                    weight += item.getWeight();
                }

                if (shelf.getMaxWeight() < weight ) {
                    maxWeight = true;
                }
                if (shelf.getCapacity() < capacity) {
                    capcityError = true;
                }
                if (shelf.isSecure() != shelfManager.findShelfById(shelf.getId()).isSecure()) {
                    changedSecurity = true;
                    List<Item> list = warehouseManager.listAllItemsOnShelf(shelf);
                    for (Item item : list) {
                        warehouseManager.withdrawItemFromShelf(item);
                    }
                    shelfManager.updateShelf(shelf);
                    return list;
                }
                if (!maxWeight && !capcityError && !changedSecurity) {
                    shelfManager.updateShelf(shelf);
                }
                return null;
            } catch (MethodFailureException e) {
                logger.error(e.getMessage(), e);
                throw new ExecutionException(e);
            }
        }

        @Override
        protected void done() {
            listAllShelves();
            listAllItems(-1);
            try {
                if (get() != null) {
                    itemsPopup(get(), "droppedItemsWindow", null);
                }
                if (!maxWeight && !capcityError) {
                    JOptionPane.showMessageDialog(window, printOut("updateShelfSuccess"), printOut("updated"), JOptionPane.INFORMATION_MESSAGE);
                }
                if (maxWeight) {
                    JOptionPane.showMessageDialog(window, printOut("shelfWeight"), printOut("error"), JOptionPane.ERROR_MESSAGE);
                }
                if (capcityError) {
                    JOptionPane.showMessageDialog(window, printOut("shelfCapacity"), printOut("error"), JOptionPane.ERROR_MESSAGE);
                }
                if (changedSecurity) {
                    JOptionPane.showMessageDialog(window, printOut("dropedItems"), printOut("updated"), JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (InterruptedException | ExecutionException e) {
                logger.error(e.getMessage(), e);
                JOptionPane.showMessageDialog(window, printOut("updateError"), printOut("error"), JOptionPane.ERROR_MESSAGE);
            }
            updateShelfFrame.dispose();
            enableAllButtons();
        }

    }

    private void cancelInsertShelfButtonActionPerformed(ActionEvent e) {
        updateShelfFrame.dispose();
        enableAllButtons();
    }

    private void deleteExpiredItesmButtonActionPerformed(ActionEvent e) {
        int result = JOptionPane.showConfirmDialog(window, printOut("expiredItems"), printOut("warning"), JOptionPane.YES_NO_OPTION);
        if (result == 0) {
            SwingWorkerDeleteExpiredItems swingWorker = new SwingWorkerDeleteExpiredItems();
            swingWorker.execute();
            deleteExpiredItesmButton.setEnabled(false);
        }
    }

    class SwingWorkerDeleteExpiredItems extends SwingWorker<List<Item>, Void> {

        @Override
        protected List<Item> doInBackground() throws Exception {
            try {
                return warehouseManager.removeAllExpiredItems(new Date());
            } catch (MethodFailureException e) {
                logger.error(e.getMessage(), e);
                throw new ExecutionException(e);
            }
        }

        @Override
        protected void done() {
            listAllItems(-1);
            try {
                if (get().size() == 0) {
                    JOptionPane.showMessageDialog(window, printOut("nothingDeleted"), printOut("info"), JOptionPane.INFORMATION_MESSAGE);
                } else {
                    itemsPopup(get(), "deletedItems", null);
                }
            } catch (InterruptedException | ExecutionException e) {
                logger.error(e.getMessage(), e);
                JOptionPane.showMessageDialog(window, printOut("deleteError"), printOut("error"), JOptionPane.ERROR_MESSAGE);
            }
            deleteExpiredItesmButton.setEnabled(true);
        }

    }

    private void putItemWithShelfButtonActionPerformed(ActionEvent e) {
        int itemRow = itemsTable.getSelectedRow();
        int shelfRow = shelvesTable.getSelectedRow();

        if(itemRow == -1) {
            JOptionPane.showMessageDialog(window, printOut("selectItemError"), printOut("error"), JOptionPane.ERROR_MESSAGE);
        } else if(shelfRow == -1) {
            JOptionPane.showMessageDialog(window, printOut("selectShelfError"), printOut("error"), JOptionPane.ERROR_MESSAGE);
        } else {
            int itemId = (Integer)itemsTable.getModel().getValueAt(itemRow, 0);
            int shelfId = (Integer)shelvesTable.getModel().getValueAt(shelfRow, 0);

            SwingWorkerPutItemWithShelf swingWorker = new SwingWorkerPutItemWithShelf(itemId, shelfId, itemRow);
            swingWorker.execute();
            putItemWithShelfButton.setEnabled(false);
        }
    }

    class SwingWorkerPutItemWithShelf extends SwingWorker<Void, Void> {
        private boolean already = false;
        private boolean full = true;
        private boolean overWeight = false;
        private boolean sameType = true;
        private int itemId;
        private int shelfId;
        private int selectedItem;

        public SwingWorkerPutItemWithShelf(int itemId, int shelfId, int selectedItem) {
            this.itemId = itemId;
            this.shelfId = shelfId;
            this.selectedItem = selectedItem;
        }

        @Override
        protected Void doInBackground() throws Exception {
            ApplicationContext springContext = new AnnotationConfigApplicationContext(SpringConfig.class);
            ShelfManager shelfManager = springContext.getBean("shelfManager", ShelfManagerImpl.class);
            ItemManager itemManager = springContext.getBean("itemManager", ItemManagerImpl.class);
            try {
                Item item = itemManager.findItemById(itemId);
                Shelf shelf = shelfManager.findShelfById(shelfId);
                List<Item> list = warehouseManager.listAllItemsOnShelf(shelf);
                if (warehouseManager.findShelfWithItem(item) != null) {
                    if (warehouseManager.findShelfWithItem(item).equals(shelf)) {
                        already = true;
                    }
                }
                if (list.size() < shelf.getCapacity()) {
                    full = false;
                }
                double overalWeight = 0;
                for (Item items : list) {
                    overalWeight += items.getWeight();
                }
                if (shelf.getMaxWeight() < overalWeight + item.getWeight()) {
                    overWeight = true;
                }
                if (shelf.isSecure() != item.isDangerous()) {
                    sameType = false;
                }
                if(!full && !already && !overWeight && sameType) {
                    warehouseManager.putItemOnShelf(shelf, item);
                }
            } catch (MethodFailureException e) {
                logger.error(e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void done() {
            listAllItems(selectedItem);
            try {
                get();
                if(!full && !already && !overWeight && sameType) {
                    JOptionPane.showMessageDialog(window, printOut("itemMoved"), printOut("info"), JOptionPane.INFORMATION_MESSAGE);
                }
                if (already) {
                    JOptionPane.showMessageDialog(window, printOut("alreadyOnShelf"), printOut("error"), JOptionPane.ERROR_MESSAGE);
                }
                if (overWeight && !already) {
                    JOptionPane.showMessageDialog(window, printOut("moveCapacity"), printOut("error"), JOptionPane.ERROR_MESSAGE);
                }
                if (full && !already) {
                    JOptionPane.showMessageDialog(window, printOut("shelfAlreadyFull"), printOut("error"), JOptionPane.ERROR_MESSAGE);
                }
                if (!sameType) {
                    JOptionPane.showMessageDialog(window, printOut("differentSecurity"), printOut("error"), JOptionPane.ERROR_MESSAGE);
                }
            } catch (InterruptedException | ExecutionException e) {
                logger.error(e.getMessage(), e);
                JOptionPane.showMessageDialog(window, printOut("insertingItemIntoShelfError"), printOut("error"), JOptionPane.ERROR_MESSAGE);
            }
            putItemWithShelfButton.setEnabled(true);
        }

    }

    private void selectShelfAllItems(MouseEvent e) {
        ApplicationContext springContext = new AnnotationConfigApplicationContext(SpringConfig.class);
        ShelfManager shelfManager = springContext.getBean("shelfManager", ShelfManagerImpl.class);

        try {
            int id = (Integer)shelvesTable.getModel().getValueAt(shelvesTable.getSelectedRow(), 0);
            Shelf shelf = shelfManager.findShelfById(id);
            itemsPopup(warehouseManager.listAllItemsOnShelf(shelf), "listOfAllItems", shelf);
        } catch (MethodFailureException e1) {
            logger.error(e1.getMessage(), e1);
        }
    }

    private void itemsPopup(List<Item> list, String windowName, Shelf shelf) {
        ItemListFrame itemListFrame = new ItemListFrame();
        itemListFrame.listAllItemsLabel.setText(printOut(windowName));
        DefaultTableModel model = (DefaultTableModel) itemListFrame.itemsTable.getModel();
        model.setRowCount(0);
        for (Item i : list) {

            model.addRow(new Object[]{i.getWeight(), getExpirationTime(i.getInsertionDate(), i.getStoreDays()),
                    i.isDangerous()});
        }
        if (shelf != null) {
            for (int i = 0; i < shelf.getCapacity() - list.size(); i++) {
                model.addRow(new Object[]{null, null, null});
            }
        }
        itemListFrame.setVisible(true);
    }

    class ColumnHeaderToolTips extends MouseMotionAdapter {
        TableColumn curCol;
        Map<TableColumn, String> tips = new HashMap<>();
        public void setToolTip(TableColumn col, String tooltip) {
            if (tooltip == null) {
                tips.remove(col);
            } else {
                tips.put(col, tooltip);
            }
        }
        public void mouseMoved(MouseEvent evt) {
            JTableHeader header = (JTableHeader) evt.getSource();
            JTable table = header.getTable();
            TableColumnModel colModel = table.getColumnModel();
            int vColIndex = colModel.getColumnIndexAtX(evt.getX());
            TableColumn col = null;
            if (vColIndex >= 0) {
                col = colModel.getColumn(vColIndex);
            }
            if (col != curCol) {
                header.setToolTipText(tips.get(col));
                curCol = col;
            }
        }
    }

    private void disableAllButtons() {
        insertItemButton.setEnabled(false);
        insertShelfButton.setEnabled(false);
        deleteExpiredItesmButton.setEnabled(false);
        putItemWithShelfButton.setEnabled(false);
        updateItemButton.setEnabled(false);
        updateShelfButton.setEnabled(false);
        deleteItemButton.setEnabled(false);
        deleteShelfButton.setEnabled(false);
    }

    private void enableAllButtons() {
        insertItemButton.setEnabled(true);
        insertShelfButton.setEnabled(true);
        deleteExpiredItesmButton.setEnabled(true);
        putItemWithShelfButton.setEnabled(true);
        updateItemButton.setEnabled(true);
        updateShelfButton.setEnabled(true);
        deleteItemButton.setEnabled(true);
        deleteShelfButton.setEnabled(true);
    }

    private void initComponents() {

        SpinnerNumberModel storeDays = new SpinnerNumberModel(1, 1, 365, 1);
        SpinnerNumberModel column = new SpinnerNumberModel(0, 0, 50, 1);
        SpinnerNumberModel row = new SpinnerNumberModel(0, 0, 50, 1);
        SpinnerNumberModel capacity = new SpinnerNumberModel(1, 1, 100, 1);
        SpinnerNumberModel weight = new SpinnerNumberModel(0.01, 0.01, 500.00, 0.05);
        SpinnerNumberModel maxWeight = new SpinnerNumberModel(0.01, 0.01, 1000.00, 0.05);

        submitItemPanel = new JPanel();
        itemPanelTitleLable = new JLabel();
        weightLabel = new JLabel();
        weightSpinner = new JSpinner(weight);
        storeDaysLabel = new JLabel();
        storeDaysSpinner = new JSpinner(storeDays);
        dangerousLabel = new JLabel();
        dangerousCheckBox = new JCheckBox();
        insertItemButton = new JButton();
        submitShelfPanel = new JPanel();
        shelfPanelTitleLable1 = new JLabel();
        columnLabel = new JLabel();
        columnSpinner = new JSpinner(column);
        rowLabel = new JLabel();
        rowSpinner = new JSpinner(row);
        maxWeightLabel = new JLabel();
        maxWeightSpinner = new JSpinner(maxWeight);
        capacitytLabel = new JLabel();
        capacitySpinner = new JSpinner(capacity);
        secureLabel = new JLabel();
        secureCheckBox = new JCheckBox();
        insertShelfButton = new JButton();
        printoutPanel = new JPanel();
        listAllItemsLabel = new JLabel();
        itemsScrollPane = new JScrollPane();
        itemsTable = new JTable();
        updateItemButton = new JButton();
        deleteItemButton = new JButton();
        listAllShelvesLabel = new JLabel();
        shelvesScrollPane = new JScrollPane();
        shelvesTable = new JTable();
        updateShelfButton = new JButton();
        deleteShelfButton = new JButton();
        logoPanel = new JPanel();
        deleteExpiredItesmButton = new JButton();
        putItemWithShelfButton = new JButton();

        setTitle("Warehouse Manager");
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                String ObjButtons[] = {printOut("yes"), printOut("no")};
                int PromptResult = JOptionPane.showOptionDialog(null, printOut("exitMessage"),
                        "Warehouse Manager", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                        null, ObjButtons, ObjButtons[1]);
                if (PromptResult == 0) {
                    System.exit(0);
                }
            }
        });

        itemPanelTitleLable.setFont(new Font("Century", Font.BOLD, 18));
        itemPanelTitleLable.setHorizontalAlignment(SwingConstants.LEFT);
        itemPanelTitleLable.setText(printOut("item"));

        weightLabel.setFont(new Font("Century", 0, 14));
        weightLabel.setHorizontalAlignment(SwingConstants.LEFT);
        weightLabel.setText(printOut("weight"));

        weightSpinner.setFont(new Font("Century", 0, 14));

        storeDaysLabel.setFont(new Font("Century", 0, 14));
        storeDaysLabel.setHorizontalAlignment(SwingConstants.LEFT);
        storeDaysLabel.setText(printOut("storeDays"));

        storeDaysSpinner.setFont(new Font("Century", 0, 14));

        dangerousLabel.setFont(new Font("Century", 0, 14));
        dangerousLabel.setHorizontalAlignment(SwingConstants.LEFT);
        dangerousLabel.setText(printOut("dangerous"));

        insertItemButton.setFont(new Font("Century", 0, 14));
        insertItemButton.setText(printOut("insert"));
        insertItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                insertItemButtonActionPerformed(e);
            }
        });

        GroupLayout submitItemPanelLayout = new GroupLayout(submitItemPanel);
        submitItemPanel.setLayout(submitItemPanelLayout);
        submitItemPanelLayout.setHorizontalGroup(
                submitItemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(submitItemPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(submitItemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(submitItemPanelLayout.createSequentialGroup()
                                                .addGap(10, 10, 10)
                                                .addGroup(submitItemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(weightLabel, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(weightSpinner, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE))
                                                .addGap(30, 30, 30)
                                                .addGroup(submitItemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addGroup(submitItemPanelLayout.createSequentialGroup()
                                                                .addComponent(storeDaysSpinner, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE)
                                                                .addGap(49, 49, 49)
                                                                .addComponent(dangerousCheckBox)
                                                                .addGap(47, 47, 47)
                                                                .addComponent(insertItemButton))
                                                        .addGroup(submitItemPanelLayout.createSequentialGroup()
                                                                .addComponent(storeDaysLabel, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                                                                .addGap(32, 32, 32)
                                                                .addComponent(dangerousLabel, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE))))
                                        .addComponent(itemPanelTitleLable))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        submitItemPanelLayout.setVerticalGroup(
                submitItemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(submitItemPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(submitItemPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(insertItemButton)
                                        .addGroup(submitItemPanelLayout.createSequentialGroup()
                                                .addComponent(itemPanelTitleLable, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(submitItemPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(weightLabel)
                                                        .addComponent(storeDaysLabel)
                                                        .addComponent(dangerousLabel))
                                                .addGroup(submitItemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addGroup(submitItemPanelLayout.createSequentialGroup()
                                                                .addGap(10, 10, 10)
                                                                .addComponent(dangerousCheckBox))
                                                        .addGroup(submitItemPanelLayout.createSequentialGroup()
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(submitItemPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                                        .addComponent(weightSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(storeDaysSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        shelfPanelTitleLable1.setFont(new Font("Century", Font.BOLD, 18));
        shelfPanelTitleLable1.setHorizontalAlignment(SwingConstants.LEFT);
        shelfPanelTitleLable1.setText(printOut("shelf"));

        columnLabel.setFont(new Font("Century", 0, 14));
        columnLabel.setHorizontalAlignment(SwingConstants.LEFT);
        columnLabel.setText(printOut("column"));

        columnSpinner.setFont(new Font("Century", 0, 14));

        rowLabel.setFont(new Font("Century", 0, 14));
        rowLabel.setHorizontalAlignment(SwingConstants.LEFT);
        rowLabel.setText(printOut("row"));

        rowSpinner.setFont(new Font("Century", 0, 14));

        maxWeightLabel.setFont(new Font("Century", 0, 14));
        maxWeightLabel.setHorizontalAlignment(SwingConstants.LEFT);
        maxWeightLabel.setText(printOut("maxWeight"));

        maxWeightSpinner.setFont(new Font("Century", 0, 14));

        capacitytLabel.setFont(new Font("Century", 0, 14));
        capacitytLabel.setHorizontalAlignment(SwingConstants.LEFT);
        capacitytLabel.setText(printOut("capacity"));

        capacitySpinner.setFont(new Font("Century", 0, 14));

        secureLabel.setFont(new Font("Century", 0, 14));
        secureLabel.setHorizontalAlignment(SwingConstants.LEFT);
        secureLabel.setText(printOut("secure"));

        insertShelfButton.setFont(new Font("Century", 0, 14));
        insertShelfButton.setText(printOut("insert"));
        insertShelfButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                insertShelfButtonActionPerformed(e);
            }
        });

        GroupLayout submitShelfPanelLayout = new GroupLayout(submitShelfPanel);
        submitShelfPanel.setLayout(submitShelfPanelLayout);
        submitShelfPanelLayout.setHorizontalGroup(
                submitShelfPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(submitShelfPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(submitShelfPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(shelfPanelTitleLable1)
                                        .addGroup(submitShelfPanelLayout.createSequentialGroup()
                                                .addGap(10, 10, 10)
                                                .addGroup(submitShelfPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(columnLabel, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(columnSpinner, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE))
                                                .addGap(30, 30, 30)
                                                .addGroup(submitShelfPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(rowLabel, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(rowSpinner, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE))
                                                .addGap(30, 30, 30)
                                                .addGroup(submitShelfPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(maxWeightSpinner, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(maxWeightLabel, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE))
                                                .addGap(30, 30, 30)
                                                .addGroup(submitShelfPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addGroup(submitShelfPanelLayout.createSequentialGroup()
                                                                .addComponent(capacitySpinner, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE)
                                                                .addGap(52, 52, 52)
                                                                .addComponent(secureCheckBox)
                                                                .addGap(52, 52, 52)
                                                                .addComponent(insertShelfButton))
                                                        .addGroup(submitShelfPanelLayout.createSequentialGroup()
                                                                .addComponent(capacitytLabel, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                                                                .addGap(30, 30, 30)
                                                                .addComponent(secureLabel, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)))))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        submitShelfPanelLayout.setVerticalGroup(
                submitShelfPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(submitShelfPanelLayout.createSequentialGroup()
                                .addGroup(submitShelfPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(secureCheckBox)
                                        .addGroup(submitShelfPanelLayout.createSequentialGroup()
                                                .addComponent(shelfPanelTitleLable1, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(submitShelfPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(columnLabel)
                                                        .addComponent(rowLabel)
                                                        .addComponent(maxWeightLabel)
                                                        .addComponent(capacitytLabel)
                                                        .addComponent(secureLabel))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(submitShelfPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(columnSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(rowSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(maxWeightSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(capacitySpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(insertShelfButton))))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        listAllItemsLabel.setFont(new Font("Century", 0, 14));
        listAllItemsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        listAllItemsLabel.setText(printOut("listOfAllItems"));

        itemsScrollPane.setFont(new Font("Century", 0, 14));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        itemsTable.setModel(new DefaultTableModel(
                new Object[][]{

                },
                new String[]{
                        "id", printOut("weight"), printOut("expiration"), printOut("dangerous"), printOut("expired"), printOut("coords")
                }
        ) {
            Class[] types = new Class[]{
                    Integer.class, Double.class, Object.class, Boolean.class, Boolean.class, String.class
            };
            boolean[] canEdit = new boolean[]{
                    false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        itemsTable.setColumnSelectionAllowed(false);
        itemsTable.getTableHeader().setReorderingAllowed(false);
        itemsScrollPane.setViewportView(itemsTable);
        if (itemsTable.getColumnModel().getColumnCount() > 0) {
            itemsTable.getColumnModel().getColumn(0).setResizable(false);
            itemsTable.getColumnModel().getColumn(1).setResizable(false);
            itemsTable.getColumnModel().getColumn(2).setResizable(false);
            itemsTable.getColumnModel().getColumn(3).setResizable(false);
            itemsTable.getColumnModel().getColumn(4).setResizable(false);
            itemsTable.getColumnModel().getColumn(5).setResizable(false);

            itemsTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
            itemsTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
            itemsTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        }
        itemsTable.removeColumn(itemsTable.getColumnModel().getColumn(0));
        itemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemsTable.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    updateItemButtonActionPerformed(new ActionEvent(e.getSource(), e.getID(), e.paramString()));
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        JTableHeader itemsHeader = itemsTable.getTableHeader();

        ColumnHeaderToolTips itemsTips = new ColumnHeaderToolTips();
        for (int c = 0; c < itemsTable.getColumnCount(); c++) {
            TableColumn col = itemsTable.getColumnModel().getColumn(c);
            itemsTips.setToolTip(col, (String)col.getHeaderValue());
        }
        itemsHeader.addMouseMotionListener(itemsTips);

        listAllItems(-1);

        updateItemButton.setFont(new Font("Century", 0, 14));
        updateItemButton.setText(printOut("updateSelectedItem"));
        updateItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateItemButtonActionPerformed(e);
            }
        });

        deleteItemButton.setFont(new Font("Century", 0, 14));
        deleteItemButton.setText(printOut("deleteSelectedItem"));
        deleteItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteItemButtonActionPerformed(e);
            }
        });

        listAllShelvesLabel.setFont(new Font("Century", 0, 14));
        listAllShelvesLabel.setHorizontalAlignment(SwingConstants.CENTER);
        listAllShelvesLabel.setText(printOut("listOfAllShelves"));

        shelvesTable.setModel(new DefaultTableModel(
                new Object[][]{

                },
                new String[]{
                        "id", printOut("column"), printOut("row"), printOut("maxWeight"), printOut("capacity"), printOut("secure")
                }
        ) {
            Class[] types = new Class[]{
                    Integer.class, Integer.class, Integer.class, Double.class, Integer.class, Boolean.class
            };
            boolean[] canEdit = new boolean[]{
                    false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        shelvesTable.setColumnSelectionAllowed(false);
        shelvesTable.getTableHeader().setReorderingAllowed(false);
        shelvesScrollPane.setViewportView(shelvesTable);
        if (shelvesTable.getColumnModel().getColumnCount() > 0) {
            shelvesTable.getColumnModel().getColumn(0).setResizable(false);
            shelvesTable.getColumnModel().getColumn(1).setResizable(false);
            shelvesTable.getColumnModel().getColumn(2).setResizable(false);
            shelvesTable.getColumnModel().getColumn(3).setResizable(false);
            shelvesTable.getColumnModel().getColumn(4).setResizable(false);
            shelvesTable.getColumnModel().getColumn(5).setResizable(false);

            shelvesTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
            shelvesTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
            shelvesTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
            shelvesTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        }
        shelvesTable.removeColumn(shelvesTable.getColumnModel().getColumn(0));
        shelvesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        shelvesTable.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    selectShelfAllItems(e);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        JTableHeader shelvesHeader = shelvesTable.getTableHeader();

        ColumnHeaderToolTips shelvesTips = new ColumnHeaderToolTips();
        for (int c = 0; c < shelvesTable.getColumnCount(); c++) {
            TableColumn col = shelvesTable.getColumnModel().getColumn(c);
            shelvesTips.setToolTip(col, (String)col.getHeaderValue());
        }
        shelvesHeader.addMouseMotionListener(shelvesTips);

        listAllShelves();

        updateShelfButton.setFont(new Font("Century", 0, 14));
        updateShelfButton.setText(printOut("updateSelectedShelf"));
        updateShelfButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateShelfButtonActionPerformed(e);
            }
        });

        deleteShelfButton.setFont(new Font("Century", 0, 14));
        deleteShelfButton.setText(printOut("deleteSelectedShelf"));
        deleteShelfButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteShelfButtonActionPerformed(e);
            }
        });

        GroupLayout printoutPanelLayout = new GroupLayout(printoutPanel);
        printoutPanel.setLayout(printoutPanelLayout);
        printoutPanelLayout.setHorizontalGroup(
                printoutPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(printoutPanelLayout.createSequentialGroup()
                                .addGap(191, 191, 191)
                                .addComponent(listAllItemsLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(listAllShelvesLabel)
                                .addGap(209, 209, 209))
                        .addGroup(printoutPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(printoutPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(printoutPanelLayout.createSequentialGroup()
                                                .addComponent(updateItemButton)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(deleteItemButton))
                                        .addComponent(itemsScrollPane, GroupLayout.PREFERRED_SIZE, 534, GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(printoutPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(shelvesScrollPane, GroupLayout.PREFERRED_SIZE, 584, GroupLayout.PREFERRED_SIZE)
                                        .addGroup(printoutPanelLayout.createSequentialGroup()
                                                .addComponent(updateShelfButton)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(deleteShelfButton)))
                                .addContainerGap())
        );
        printoutPanelLayout.setVerticalGroup(
                printoutPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(printoutPanelLayout.createSequentialGroup()
                                .addGroup(printoutPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(listAllItemsLabel, GroupLayout.Alignment.TRAILING)
                                        .addComponent(listAllShelvesLabel))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(printoutPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(printoutPanelLayout.createSequentialGroup()
                                                .addComponent(itemsScrollPane, GroupLayout.PREFERRED_SIZE, 300, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(printoutPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(updateItemButton)
                                                        .addComponent(deleteItemButton)
                                                        .addComponent(updateShelfButton)))
                                        .addGroup(printoutPanelLayout.createSequentialGroup()
                                                .addComponent(shelvesScrollPane, GroupLayout.PREFERRED_SIZE, 300, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(deleteShelfButton)))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        deleteExpiredItesmButton.setFont(new Font("Century", 0, 14));
        deleteExpiredItesmButton.setText(printOut("deleteExipredItems"));
        deleteExpiredItesmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteExpiredItesmButtonActionPerformed(e);
            }
        });

        putItemWithShelfButton.setFont(new Font("Century", 0, 14));
        putItemWithShelfButton.setText(printOut("putItemOnShelf"));
        putItemWithShelfButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                putItemWithShelfButtonActionPerformed(e);
            }
        });

        GroupLayout logoPanelLayout = new GroupLayout(logoPanel);
        logoPanel.setLayout(logoPanelLayout);
        logoPanelLayout.setHorizontalGroup(
                logoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(logoPanelLayout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addGroup(logoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(putItemWithShelfButton)
                                        .addComponent(deleteExpiredItesmButton))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        logoPanelLayout.setVerticalGroup(
                logoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, logoPanelLayout.createSequentialGroup()
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(deleteExpiredItesmButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(putItemWithShelfButton)
                                .addContainerGap())
        );

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(printoutPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(submitItemPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(logoPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addComponent(submitShelfPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(submitItemPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(logoPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(submitShelfPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(printoutPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(getOwner());
    }


    private JSpinner capacitySpinner;
    private JLabel capacitytLabel;
    private JLabel columnLabel;
    private JSpinner columnSpinner;
    private JCheckBox dangerousCheckBox;
    private JLabel dangerousLabel;
    private JButton deleteItemButton;
    private JButton deleteShelfButton;
    private JButton insertItemButton;
    private JButton insertShelfButton;
    private JLabel itemPanelTitleLable;
    private JScrollPane itemsScrollPane;
    private JTable itemsTable;
    private JLabel listAllItemsLabel;
    private JLabel listAllShelvesLabel;
    private JPanel logoPanel;
    private JLabel maxWeightLabel;
    private JSpinner maxWeightSpinner;
    private JPanel printoutPanel;
    private JLabel rowLabel;
    private JSpinner rowSpinner;
    private JCheckBox secureCheckBox;
    private JLabel secureLabel;
    private JLabel shelfPanelTitleLable1;
    private JScrollPane shelvesScrollPane;
    private JTable shelvesTable;
    private JLabel storeDaysLabel;
    private JSpinner storeDaysSpinner;
    private JPanel submitItemPanel;
    private JPanel submitShelfPanel;
    private JButton updateItemButton;
    private JButton updateShelfButton;
    private JLabel weightLabel;
    private JSpinner weightSpinner;
    private JButton deleteExpiredItesmButton;
    private JButton putItemWithShelfButton;

    class InsertItemFrame extends JFrame {

        private Point mouseDownCompCoords;
        public InsertItemFrame() {
            initComponents();
        }

        private void initComponents() {

            SpinnerNumberModel weight = new SpinnerNumberModel(0.01, 0.01, 500.00, 0.05);
            SpinnerNumberModel storeDays = new SpinnerNumberModel(1, 1, 365, 1);

            submitItemPanel = new JPanel();
            itemPanelTitleLable = new JLabel();
            weightLabel = new JLabel();
            weightSpinner = new JSpinner(weight);
            storeDaysLabel = new JLabel();
            storeDaysSpinner = new JSpinner(storeDays);
            dangerousLabel = new JLabel();
            dangerousCheckBox = new JCheckBox();
            editItemButton = new JButton();
            cancelInsertItemButton = new JButton();

            setUndecorated(true);
            setResizable(false);
            getRootPane().setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.black));

            addMouseListener(new MouseListener() {
                public void mouseReleased(MouseEvent e) {
                    mouseDownCompCoords = null;
                }

                public void mousePressed(MouseEvent e) {
                    mouseDownCompCoords = e.getPoint();
                }

                public void mouseExited(MouseEvent e) {
                }

                public void mouseEntered(MouseEvent e) {
                }

                public void mouseClicked(MouseEvent e) {
                }
            });

            addMouseMotionListener(new MouseMotionListener() {
                public void mouseMoved(MouseEvent e) {
                }

                public void mouseDragged(MouseEvent e) {
                    Point currCoords = e.getLocationOnScreen();
                    setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
                }
            });

            itemPanelTitleLable.setFont(new Font("Century", Font.BOLD, 18));
            itemPanelTitleLable.setHorizontalAlignment(SwingConstants.LEFT);
            itemPanelTitleLable.setText(printOut("item"));

            weightLabel.setFont(new Font("Century", 0, 14));
            weightLabel.setHorizontalAlignment(SwingConstants.LEFT);
            weightLabel.setText(printOut("weight"));

            weightSpinner.setFont(new Font("Century", 0, 14));

            storeDaysLabel.setFont(new Font("Century", 0, 14));
            storeDaysLabel.setHorizontalAlignment(SwingConstants.LEFT);
            storeDaysLabel.setText(printOut("storeDays"));

            storeDaysSpinner.setFont(new Font("Century", 0, 14));

            dangerousLabel.setFont(new Font("Century", 0, 14));
            dangerousLabel.setHorizontalAlignment(SwingConstants.LEFT);
            dangerousLabel.setText(printOut("dangerous"));

            editItemButton.setFont(new Font("Century", 0, 14));
            editItemButton.setText(printOut("update"));
            editItemButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    editItemButtonActionPerformed(e);
                }
            });

            cancelInsertItemButton.setFont(new Font("Century", 0, 14));
            cancelInsertItemButton.setText(printOut("cancel"));
            cancelInsertItemButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancelInsertItemButtonActionPerformed(e);
                }
            });

            GroupLayout submitItemPanelLayout = new GroupLayout(submitItemPanel);
            submitItemPanel.setLayout(submitItemPanelLayout);
            submitItemPanelLayout.setHorizontalGroup(
                    submitItemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(submitItemPanelLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(submitItemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                            .addGroup(submitItemPanelLayout.createSequentialGroup()
                                                    .addGap(10, 10, 10)
                                                    .addGroup(submitItemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                            .addComponent(weightLabel, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(weightSpinner, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE))
                                                    .addGap(30, 30, 30)
                                                    .addGroup(submitItemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                            .addGroup(submitItemPanelLayout.createSequentialGroup()
                                                                    .addComponent(storeDaysLabel, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                                                                    .addGap(32, 32, 32)
                                                                    .addComponent(dangerousLabel, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE))
                                                            .addGroup(submitItemPanelLayout.createSequentialGroup()
                                                                    .addComponent(storeDaysSpinner, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE)
                                                                    .addGap(49, 49, 49)
                                                                    .addComponent(dangerousCheckBox)
                                                                    .addGap(46, 46, 46)
                                                                    .addComponent(editItemButton)))
                                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                            .addGroup(submitItemPanelLayout.createSequentialGroup()
                                                    .addComponent(itemPanelTitleLable)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(cancelInsertItemButton)
                                                    .addContainerGap())))
            );
            submitItemPanelLayout.setVerticalGroup(
                    submitItemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(submitItemPanelLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(submitItemPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(itemPanelTitleLable, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(cancelInsertItemButton))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(submitItemPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(weightLabel)
                                            .addComponent(storeDaysLabel)
                                            .addComponent(dangerousLabel))
                                    .addGroup(submitItemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                            .addGroup(submitItemPanelLayout.createSequentialGroup()
                                                    .addGap(10, 10, 10)
                                                    .addComponent(dangerousCheckBox))
                                            .addGroup(submitItemPanelLayout.createSequentialGroup()
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                    .addGroup(submitItemPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                            .addComponent(weightSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(storeDaysSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
                                    .addContainerGap(11, Short.MAX_VALUE))
                            .addGroup(GroupLayout.Alignment.TRAILING, submitItemPanelLayout.createSequentialGroup()
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(editItemButton)
                                    .addContainerGap())
            );

            GroupLayout layout = new GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(submitItemPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            );
            layout.setVerticalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(submitItemPanel, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            );

            pack();
            setLocationRelativeTo(getOwner());
        }

        private JCheckBox dangerousCheckBox;
        private JLabel dangerousLabel;
        private JButton editItemButton;
        private JLabel itemPanelTitleLable;
        private JLabel storeDaysLabel;
        private JSpinner storeDaysSpinner;
        private JPanel submitItemPanel;
        private JLabel weightLabel;
        private JSpinner weightSpinner;
        private JButton cancelInsertItemButton;
    }

    class InsertShelfFrame extends JFrame {

        private Point mouseDownCompCoords;

        public InsertShelfFrame() {
            initComponents();
        }

        private void initComponents() {

            SpinnerNumberModel column = new SpinnerNumberModel(0, 0, 50, 1);
            SpinnerNumberModel row = new SpinnerNumberModel(0, 0, 50, 1);
            SpinnerNumberModel capacity = new SpinnerNumberModel(1, 1, 100, 1);
            SpinnerNumberModel maxWeight = new SpinnerNumberModel(0.01, 0.01, 1000.00, 0.05);

            submitShelfPanel = new JPanel();
            shelfPanelTitleLable1 = new JLabel();
            columnLabel = new JLabel();
            columnSpinner = new JSpinner(column);
            rowLabel = new JLabel();
            rowSpinner = new JSpinner(row);
            maxWeightLabel = new JLabel();
            maxWeightSpinner = new JSpinner(maxWeight);
            capacitytLabel = new JLabel();
            capacitySpinner = new JSpinner(capacity);
            secureLabel = new JLabel();
            secureCheckBox = new JCheckBox();
            editShelfButton = new JButton();
            cancelInsertShelfButton = new JButton();

            setUndecorated(true);
            setResizable(false);
            getRootPane().setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.black));

            addMouseListener(new MouseListener() {
                public void mouseReleased(MouseEvent e) {
                    mouseDownCompCoords = null;
                }

                public void mousePressed(MouseEvent e) {
                    mouseDownCompCoords = e.getPoint();
                }

                public void mouseExited(MouseEvent e) {
                }

                public void mouseEntered(MouseEvent e) {
                }

                public void mouseClicked(MouseEvent e) {
                }
            });

            addMouseMotionListener(new MouseMotionListener() {
                public void mouseMoved(MouseEvent e) {
                }

                public void mouseDragged(MouseEvent e) {
                    Point currCoords = e.getLocationOnScreen();
                    setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
                }
            });

            shelfPanelTitleLable1.setFont(new Font("Century", Font.BOLD, 18));
            shelfPanelTitleLable1.setHorizontalAlignment(SwingConstants.LEFT);
            shelfPanelTitleLable1.setText(printOut("shelf"));

            columnLabel.setFont(new Font("Century", 0, 14));
            columnLabel.setHorizontalAlignment(SwingConstants.LEFT);
            columnLabel.setText(printOut("column"));

            columnSpinner.setFont(new Font("Century", 0, 14));

            rowLabel.setFont(new Font("Century", 0, 14));
            rowLabel.setHorizontalAlignment(SwingConstants.LEFT);
            rowLabel.setText(printOut("row"));

            rowSpinner.setFont(new Font("Century", 0, 14));

            maxWeightLabel.setFont(new Font("Century", 0, 14));
            maxWeightLabel.setHorizontalAlignment(SwingConstants.LEFT);
            maxWeightLabel.setText(printOut("maxWeight"));

            maxWeightSpinner.setFont(new Font("Century", 0, 14));

            capacitytLabel.setFont(new Font("Century", 0, 14));
            capacitytLabel.setHorizontalAlignment(SwingConstants.LEFT);
            capacitytLabel.setText(printOut("capacity"));

            capacitySpinner.setFont(new Font("Century", 0, 14));

            secureLabel.setFont(new Font("Century", 0, 14));
            secureLabel.setHorizontalAlignment(SwingConstants.LEFT);
            secureLabel.setText(printOut("secure"));

            editShelfButton.setFont(new Font("Century", 0, 14));
            editShelfButton.setText(printOut("update"));
            editShelfButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    editShelfButtonActionPerformed(e);
                }
            });

            cancelInsertShelfButton.setFont(new Font("Century", 0, 14));
            cancelInsertShelfButton.setText(printOut("cancel"));
            cancelInsertShelfButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancelInsertShelfButtonActionPerformed(e);
                }
            });

            GroupLayout submitShelfPanelLayout = new GroupLayout(submitShelfPanel);
            submitShelfPanel.setLayout(submitShelfPanelLayout);
            submitShelfPanelLayout.setHorizontalGroup(
                    submitShelfPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(submitShelfPanelLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(submitShelfPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                            .addGroup(submitShelfPanelLayout.createSequentialGroup()
                                                    .addComponent(shelfPanelTitleLable1)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(cancelInsertShelfButton))
                                            .addGroup(submitShelfPanelLayout.createSequentialGroup()
                                                    .addGap(10, 10, 10)
                                                    .addGroup(submitShelfPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                            .addComponent(columnLabel, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(columnSpinner, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE))
                                                    .addGap(30, 30, 30)
                                                    .addGroup(submitShelfPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                            .addComponent(rowLabel, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(rowSpinner, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE))
                                                    .addGap(30, 30, 30)
                                                    .addGroup(submitShelfPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                            .addComponent(maxWeightSpinner, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(maxWeightLabel, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE))
                                                    .addGap(30, 30, 30)
                                                    .addGroup(submitShelfPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                            .addGroup(submitShelfPanelLayout.createSequentialGroup()
                                                                    .addComponent(capacitySpinner, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE)
                                                                    .addGap(53, 53, 53)
                                                                    .addComponent(secureCheckBox)
                                                                    .addGap(51, 51, 51)
                                                                    .addComponent(editShelfButton))
                                                            .addGroup(submitShelfPanelLayout.createSequentialGroup()
                                                                    .addComponent(capacitytLabel, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                                                                    .addGap(30, 30, 30)
                                                                    .addComponent(secureLabel, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)))
                                                    .addGap(0, 0, Short.MAX_VALUE)))
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            submitShelfPanelLayout.setVerticalGroup(
                    submitShelfPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(submitShelfPanelLayout.createSequentialGroup()
                                    .addGroup(submitShelfPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                            .addComponent(secureCheckBox)
                                            .addGroup(submitShelfPanelLayout.createSequentialGroup()
                                                    .addGroup(submitShelfPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                            .addComponent(shelfPanelTitleLable1, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
                                                            .addGroup(GroupLayout.Alignment.LEADING, submitShelfPanelLayout.createSequentialGroup()
                                                                    .addContainerGap()
                                                                    .addComponent(cancelInsertShelfButton)))
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                    .addGroup(submitShelfPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                            .addComponent(columnLabel)
                                                            .addComponent(rowLabel)
                                                            .addComponent(maxWeightLabel)
                                                            .addComponent(capacitytLabel)
                                                            .addComponent(secureLabel))
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                    .addGroup(submitShelfPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                            .addComponent(columnSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(rowSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(maxWeightSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(capacitySpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(editShelfButton))))
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );

            GroupLayout layout = new GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(submitShelfPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            );
            layout.setVerticalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(submitShelfPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            );

            pack();
            setLocationRelativeTo(getOwner());
        }

        private JSpinner capacitySpinner;
        private JLabel capacitytLabel;
        private JLabel columnLabel;
        private JSpinner columnSpinner;
        private JButton editShelfButton;
        private JLabel maxWeightLabel;
        private JSpinner maxWeightSpinner;
        private JLabel rowLabel;
        private JSpinner rowSpinner;
        private JCheckBox secureCheckBox;
        private JLabel secureLabel;
        private JLabel shelfPanelTitleLable1;
        private JPanel submitShelfPanel;
        private JButton cancelInsertShelfButton;
    }

    class ItemListFrame extends JFrame {

        private Point mouseDownCompCoords;

        public ItemListFrame() {
            initComponents();
        }

        private void initComponents() {

            itemsScrollPane = new JScrollPane();
            itemsTable = new JTable();
            listAllItemsLabel = new JLabel();
            insertItemButton = new JButton();

            setUndecorated(true);
            setResizable(false);
            getRootPane().setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.black));

            addMouseListener(new MouseListener() {
                public void mouseReleased(MouseEvent e) {
                    mouseDownCompCoords = null;
                }

                public void mousePressed(MouseEvent e) {
                    mouseDownCompCoords = e.getPoint();
                }

                public void mouseExited(MouseEvent e) {
                }

                public void mouseEntered(MouseEvent e) {
                }

                public void mouseClicked(MouseEvent e) {
                }
            });

            addMouseMotionListener(new MouseMotionListener() {
                public void mouseMoved(MouseEvent e) {
                }

                public void mouseDragged(MouseEvent e) {
                    Point currCoords = e.getLocationOnScreen();
                    setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
                }
            });

            itemsScrollPane.setFont(new Font("Century", 0, 14));

            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(JLabel.CENTER);

            itemsTable.setModel(new DefaultTableModel(
                    new Object[][]{

                    },
                    new String[]{
                            printOut("weight"), printOut("expiration"), printOut("dangerous")
                    }
            ) {
                Class[] types = new Class[]{
                        Double.class, Object.class, Boolean.class
                };
                boolean[] canEdit = new boolean[]{
                        false, false, false
                };

                public Class getColumnClass(int columnIndex) {
                    return types[columnIndex];
                }

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit[columnIndex];
                }
            });
            itemsTable.setColumnSelectionAllowed(false);
            itemsTable.getTableHeader().setReorderingAllowed(false);
            if (itemsTable.getColumnModel().getColumnCount() > 0) {
                itemsTable.getColumnModel().getColumn(0).setResizable(false);
                itemsTable.getColumnModel().getColumn(1).setResizable(false);
                itemsTable.getColumnModel().getColumn(2).setResizable(false);

                itemsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
                itemsTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
            }
            itemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            JTableHeader itemsHeader = itemsTable.getTableHeader();

            ColumnHeaderToolTips itemsTips = new ColumnHeaderToolTips();
            for (int c = 0; c < itemsTable.getColumnCount(); c++) {
                TableColumn col = itemsTable.getColumnModel().getColumn(c);
                itemsTips.setToolTip(col, (String)col.getHeaderValue());
            }
            itemsHeader.addMouseMotionListener(itemsTips);

            itemsScrollPane.setViewportView(itemsTable);

            listAllItemsLabel.setFont(new Font("Century", 0, 14));

            insertItemButton.setFont(new Font("Century", 0, 14));
            insertItemButton.setText(printOut("close"));
            insertItemButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                    .addGap(181, 181, 181)
                                    .addComponent(listAllItemsLabel))
                            .addComponent(itemsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 534, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                    .addGap(121, 121, 121)
                                    .addComponent(insertItemButton))
            );
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                    .addComponent(listAllItemsLabel)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(itemsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(insertItemButton)
                                    .addGap(16, 16, 16))
            );

            pack();
            setLocationRelativeTo(getOwner());
        }

        private JScrollPane itemsScrollPane;
        private JTable itemsTable;
        private JLabel listAllItemsLabel;
        private JButton insertItemButton;
    }
}