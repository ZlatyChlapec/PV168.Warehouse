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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Slapy
 */
public class MainWindow extends JFrame {

    public static final Logger logger = LoggerFactory.getLogger(MainWindow.class);

    private ResourceBundle myResources;
    private InsertItemFrame updateItemFrame;
    private InsertShelfFrame updateShelfFrame;
    private SwingWorkerAddItem swingWorkerAddItem;
    private SwingWorkerAddShelf swingWorkerAddShelf;
    private SwingWorkerDeleteItem swingWorkerDeleteItem;
    private SwingWorkerDeleteShelf swingWorkerDeleteShelf;
    private SwingWorkerUpdateItem swingWorkerUpdateItem;
    private SwingWorkerUpdateShelf swingWorkerUpdateShelf;
    private WarehouseManagerImpl warehouseManager;
    private MainWindow window;

    public MainWindow() {

        window = this;
        ApplicationContext springContext = new AnnotationConfigApplicationContext(SpringConfig.class);
        warehouseManager = springContext.getBean("warehouseManager", WarehouseManagerImpl.class);
        try {
            myResources = ResourceBundle.getBundle("cz/muni/fi/pv168/warehouse/resources/lang", Locale.getDefault());
//            updateItemFrame = new InsertItemFrame();
//            updateShelfFrame = new InsertShelfFrame();
        } catch (MissingResourceException e) {
            logger.error("Default resource bundle not found", e);
            myResources = ResourceBundle.getBundle("lang", new Locale("en", "GB"));
//            updateItemFrame = new InsertItemFrame();
//            updateShelfFrame = new InsertShelfFrame();
        }
        initComponents();
    }

    private String printOut(String value) {
        return myResources.getString(value);
    }

    private void insertItemButtonActionPerformed(ActionEvent e) {
        if (swingWorkerAddItem != null) {
            logger.error("Operation is already in progress", e);
            throw new IllegalStateException("Operation is already in progress");
        }

        insertItemButton.setEnabled(false);

        Item item = new Item();
        item.setWeight(Double.parseDouble(weightSpinner.getValue().toString()));
        item.setStoreDays(Integer.parseInt(storeDaysSpinner.getValue().toString()));
        item.setDangerous(dangerousCheckBox.isSelected());
        if (Math.abs(item.getWeight() - 0.01) <= 0.01 && item.getStoreDays() == 1 && !item.isDangerous()) {
            int result = JOptionPane.showConfirmDialog(this, printOut("insertDefualtItem"), printOut("warning"), JOptionPane.YES_NO_OPTION);
            if (result == 0) {
                swingWorkerAddItem = new SwingWorkerAddItem(item);
                swingWorkerAddItem.execute();
            } else {
                insertItemButton.setEnabled(true);
            }
        } else {
            swingWorkerAddItem = new SwingWorkerAddItem(item);
            swingWorkerAddItem.execute();
        }
    }

    class SwingWorkerAddItem extends SwingWorker<Item, Void> {
        private ItemManager itemManager;
        private Item item;
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
                    int totalWeight = 0;
                    for (Item item : warehouseManager.listAllItemsOnShelf(shelf)) {
                        totalWeight += item.getWeight();
                    }
                    if (shelf.isSecure() == item.isDangerous() && shelf.getMaxWeight() >= totalWeight + item.getWeight()) {
                        itemManager.createItem(item);
                        warehouseManager.putItemOnShelf(shelf, item);
                        inserted = true;
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
            listAllItems();
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
            swingWorkerAddItem = null;
        }
    }

    private void insertShelfButtonActionPerformed(ActionEvent e) {
        if (swingWorkerAddShelf != null) {
            throw new IllegalStateException("Operation is already in progress");
        }

        insertShelfButton.setEnabled(false);

        Shelf shelf = new Shelf();
        shelf.setColumn(Integer.parseInt(columnSpinner.getValue().toString()));
        shelf.setRow(Integer.parseInt(rowSpinner.getValue().toString()));
        shelf.setCapacity(Integer.parseInt(capacitySpinner.getValue().toString()));
        shelf.setMaxWeight(Double.parseDouble(maxWeightSpinner.getValue().toString()));
        shelf.setSecure(secureCheckBox.isSelected());

        if (Math.abs(shelf.getMaxWeight() - 0.01) <= 0.01 && shelf.getColumn() == 1 && shelf.getRow() == 1 && shelf.getCapacity() == 1 && !shelf.isSecure()) {
            int result = JOptionPane.showConfirmDialog(this, printOut("insertDefualtShelf"), printOut("warning"), JOptionPane.YES_NO_OPTION);
            if (result == 0) {
                swingWorkerAddShelf = new SwingWorkerAddShelf(shelf);
                swingWorkerAddShelf.execute();
            } else {
                insertShelfButton.setEnabled(true);
            }
        } else {
            swingWorkerAddShelf = new SwingWorkerAddShelf(shelf);
            swingWorkerAddShelf.execute();
        }
    }

    class SwingWorkerAddShelf extends SwingWorker<Shelf, Void> {
        private ShelfManager shelfManager;
        private Shelf shelf;
        private boolean duplicit = false;

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
                        duplicit = true;
                    }
                }
                if (!duplicit) {
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
            try {
                get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error(e.getMessage(),e);
            }

            if (duplicit) {
                JOptionPane.showMessageDialog(window, printOut("duplicateShelfCoords"), printOut("error"), JOptionPane.ERROR_MESSAGE);
            }

            insertShelfButton.setEnabled(true);
            columnSpinner.setValue(0);
            rowSpinner.setValue(0);
            capacitySpinner.setValue(1);
            maxWeightSpinner.setValue(0.01);
            secureCheckBox.setSelected(false);
            swingWorkerAddShelf = null;
        }
    }

    private void deleteItemButtonActionPerformed(ActionEvent e) {
        if (swingWorkerAddItem != null) {
            logger.error("Operation is already in progress", e);
            throw new IllegalStateException("Operation is already in progress");
        }

        if (itemsTable.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, printOut("notSelectedRow"), printOut("info"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(window, printOut("deletedConfirmation"), printOut("warning"), JOptionPane.YES_NO_OPTION);
        if (result == 0) {
            deleteItemButton.setEnabled(false);
            swingWorkerDeleteItem = new SwingWorkerDeleteItem((Integer) itemsTable.getModel().getValueAt(itemsTable.getSelectedRow(), 0));
            swingWorkerDeleteItem.execute();
        }
    }

    class SwingWorkerDeleteItem extends SwingWorker<Item, Void> {
        private ItemManager itemManager;
        private Integer id;
        private Item item;

        public SwingWorkerDeleteItem(Integer id) {
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
            listAllItems();
            try {
                get();
                JOptionPane.showMessageDialog(window, printOut("deleteItemSuccess"), printOut("deleted"), JOptionPane.INFORMATION_MESSAGE);
            } catch (InterruptedException | ExecutionException e) {
                logger.error(e.getMessage(),e);
                JOptionPane.showMessageDialog(window, printOut("deleteError"), printOut("error"), JOptionPane.ERROR_MESSAGE);
            }
            deleteItemButton.setEnabled(true);
            swingWorkerDeleteItem = null;
        }
    }

    private void deleteShelfButtonActionPerformed(ActionEvent e) {
        if (swingWorkerDeleteShelf != null) {
            logger.error("Operation is already in progress", e);
            throw new IllegalStateException("Operation is already in progress");
        }

        if (shelvesTable.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, printOut("notSelectedRow"), printOut("info"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(window, printOut("deletedConfirmation"), printOut("warning"), JOptionPane.YES_NO_OPTION);

        if (result == 0) {
            deleteShelfButton.setEnabled(false);
            swingWorkerDeleteShelf = new SwingWorkerDeleteShelf((Integer) shelvesTable.getModel().getValueAt(shelvesTable.getSelectedRow(), 0));
            swingWorkerDeleteShelf.execute();
        }
    }

    class SwingWorkerDeleteShelf extends SwingWorker<Shelf, Void> {
        private ShelfManager shelfManager;
        private Integer id;
        private Shelf shelf;

        public SwingWorkerDeleteShelf(Integer id) {
            this.id = id;
        }

        @Override
        protected Shelf doInBackground() throws Exception {
            ApplicationContext springContext = new AnnotationConfigApplicationContext(SpringConfig.class);
            shelfManager = springContext.getBean("shelfManager", ShelfManagerImpl.class);
            try {
                    shelf = shelfManager.findShelfById(id);
                    shelfManager.deleteShelf(shelf);
            } catch (MethodFailureException ex) {
                logger.error(ex.getMessage(), ex);
                throw new ExecutionException(ex);
            }
            return shelf;
        }

        @Override
        protected void done() {
            listAllShelves();
            try {
                get();
                JOptionPane.showMessageDialog(window, printOut("deleteShelfSuccess"), printOut("deleted"), JOptionPane.INFORMATION_MESSAGE);
            } catch (InterruptedException | ExecutionException e) {
                logger.error(e.getMessage(), e);
                JOptionPane.showMessageDialog(window, printOut("deleteError"), printOut("error"), JOptionPane.ERROR_MESSAGE);
            }

            deleteShelfButton.setEnabled(true);
            swingWorkerDeleteShelf = null;
        }
    }

    private void updateItemButtonActionPerformed(ActionEvent e) {
//        if (updateShelfFrame.isVisible()) {
//            updateShelfFrame.requestFocus();
//        } else if (updateItemFrame.isVisible()) {
//            updateItemFrame.requestFocus();
//        } else {
//            updateItemFrame.setVisible(true);
//        }

        updateItemFrame = new InsertItemFrame();
        updateItemFrame.setVisible(true);
    }

    private void updateShelfButtonActionPerformed(ActionEvent e) {
//        if (updateItemFrame.isVisible()) {
//            updateItemFrame.requestFocus();
//        } else if (updateShelfFrame.isVisible()){
//            updateShelfFrame.requestFocus();
//        } else {
//            updateShelfFrame.setVisible(true);
//        }
        updateShelfFrame = new InsertShelfFrame();
        updateShelfFrame.setVisible(true);
    }

    private String getExpirationTime(Date insertionDate, int storeDays) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(insertionDate);
        cal.add(Calendar.DATE, storeDays);
        SimpleDateFormat myFormat = new SimpleDateFormat("dd-MM-yyyy");
        return myFormat.format(cal.getTime());
    }

    private void listAllItems() {

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
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
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
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
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
                e.printStackTrace();
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
                e.printStackTrace();
            }
            return list;
        }
    }

    private void editItemButtonActionPerformed(ActionEvent e) {
        if (swingWorkerUpdateItem != null) {
            logger.error("Operation is already in progress", e);
            throw new IllegalStateException("Operation is already in progress");
        }
        Item item = new Item();
        int id = (Integer)itemsTable.getModel().getValueAt(itemsTable.getSelectedRow(), 0);
        item.setId(id);
        item.setWeight(Double.parseDouble(updateItemFrame.weightSpinner.getValue().toString()));
        item.setStoreDays(Integer.parseInt(updateItemFrame.storeDaysSpinner.getValue().toString()));
        item.setDangerous(updateItemFrame.dangerousCheckBox.isSelected());

        updateItemButton.setEnabled(false);

        swingWorkerUpdateItem = new SwingWorkerUpdateItem(item);
        swingWorkerUpdateItem.execute();
    }


    class SwingWorkerUpdateItem extends SwingWorker<Item, Void> {
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
                itemManager.updateItem(item);
            } catch (MethodFailureException e) {
                logger.error(e.getMessage(), e);
                throw new ExecutionException(e);
            }
            return item;
        }

        @Override
        protected void done() {
            listAllItems();
            try {
                get();
                JOptionPane.showMessageDialog(window, printOut("updateItemSuccess"), printOut("updated"), JOptionPane.INFORMATION_MESSAGE);
            } catch (InterruptedException | ExecutionException e) {
                logger.error(e.getMessage(), e);
                JOptionPane.showMessageDialog(window, printOut("updateError"), printOut("error"), JOptionPane.ERROR_MESSAGE);
            }
            updateItemFrame.dispose();
            updateItemButton.setEnabled(true);
            swingWorkerUpdateItem = null;
        }
    }

    private void cancelInsertItemButtonActionPerformed(ActionEvent e) {
        updateItemFrame.dispose();
    }

    private void editShelfButtonActionPerformed(ActionEvent e) {
        if (swingWorkerUpdateShelf != null) {
            logger.error("Operation is already in progress", e);
            throw new IllegalStateException("Operation is already in progress");
        }
        Shelf shelf = new Shelf();
        int id = (Integer)shelvesTable.getModel().getValueAt(shelvesTable.getSelectedRow(), 0);
        shelf.setId(id);
        shelf.setColumn(Integer.parseInt(updateShelfFrame.columnSpinner.getValue().toString()));
        shelf.setRow(Integer.parseInt(updateShelfFrame.rowSpinner.getValue().toString()));
        shelf.setMaxWeight(Double.parseDouble(updateShelfFrame.maxWeightSpinner.getValue().toString()));
        shelf.setCapacity(Integer.parseInt(updateShelfFrame.rowSpinner.getValue().toString()));
        shelf.setSecure(updateShelfFrame.secureCheckBox.isSelected());

        updateShelfButton.setEnabled(false);

        swingWorkerUpdateShelf = new SwingWorkerUpdateShelf(shelf);
        swingWorkerUpdateShelf.execute();
    }

    class SwingWorkerUpdateShelf extends SwingWorker<Shelf, Void> {
        private ShelfManager shelfManager;
        private Shelf shelf;

        public SwingWorkerUpdateShelf(Shelf shelf) {
            this.shelf = shelf;
        }

        @Override
        protected Shelf doInBackground() throws Exception {
            ApplicationContext springContext = new AnnotationConfigApplicationContext(SpringConfig.class);
            shelfManager = springContext.getBean("shelfManager", ShelfManagerImpl.class);

            try {
                shelfManager.updateShelf(shelf);
            } catch (MethodFailureException e) {
                logger.error(e.getMessage(), e);
                throw new ExecutionException(e);
            }
            return shelf;
        }

        @Override
        protected void done() {
            listAllShelves();
            try {
                get();
                JOptionPane.showMessageDialog(window, printOut("updateShelfSuccess"), printOut("updated"), JOptionPane.INFORMATION_MESSAGE);
            } catch (InterruptedException | ExecutionException e) {
                logger.error(e.getMessage(), e);
                JOptionPane.showMessageDialog(window, printOut("updateError"), printOut("error"), JOptionPane.ERROR_MESSAGE);
            }
            updateShelfFrame.dispose();
            updateShelfButton.setEnabled(true);
            swingWorkerUpdateShelf = null;
        }

    }

    private void cancelInsertShelfButtonActionPerformed(ActionEvent e) {
        updateShelfFrame.dispose();
    }

    private void deleteExpiretItesmButtonActionPerformed(ActionEvent e) {

    }

    private void selectShelfAllItems(MouseEvent e) {
        ApplicationContext springContext = new AnnotationConfigApplicationContext(SpringConfig.class);
        ShelfManager shelfManager = springContext.getBean("shelfManager", ShelfManagerImpl.class);
        StringBuilder data = new StringBuilder("<html>" +
                                                    "<table>" +
                                                        "<thead>" +
                                                            "<tr>" +
                                                                "<th>"+ printOut("weight") +"</th>" +
                                                                "<th>"+ printOut("expiration") +"</th>" +
                                                                "<th>"+ printOut("dangerous") +"</th>" +
                                                            "</tr>" +
                                                        "</thead>");

        try {
            int id = (Integer)shelvesTable.getModel().getValueAt(shelvesTable.getSelectedRow(), 0);
            int count = 0;
            List<Item> list = warehouseManager.listAllItemsOnShelf(shelfManager.findShelfById(id));
            for (Item i : list) {
                data.append("<tr>");
                    data.append("<td>"+ i.getWeight() +"</td>");
                    data.append("<td>"+ getExpirationTime(i.getInsertionDate(), i.getStoreDays()) +"</td>");
                    data.append("<td>"+ i.isDangerous() +"</td>");
                data.append("</tr>");
                count++;
            }
            data.append("</table><p>Space left: "+ (list.size() - count) +"</p></html>");
            JOptionPane.showMessageDialog(this, data.toString(), printOut("info"), JOptionPane.INFORMATION_MESSAGE);
        } catch (MethodFailureException e1) {
            e1.printStackTrace();
        }
    }

    //need control
    class ColumnHeaderToolTips extends MouseMotionAdapter {
        TableColumn curCol;
        Map tips = new HashMap();
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
                header.setToolTipText((String) tips.get(col));
                curCol = col;
            }
        }
    }
    //need control

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
        logoLabel = new JLabel();
        deleteExpiretItesmButton = new JButton();
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
        //need control
        JTableHeader itemsHeader = itemsTable.getTableHeader();

        ColumnHeaderToolTips itemsTips = new ColumnHeaderToolTips();
        for (int c = 0; c < itemsTable.getColumnCount(); c++) {
            TableColumn col = itemsTable.getColumnModel().getColumn(c);
            itemsTips.setToolTip(col, (String)col.getHeaderValue());
        }
        itemsHeader.addMouseMotionListener(itemsTips);
        //need control
        listAllItems();

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
        //need control
        JTableHeader shelvesHeader = shelvesTable.getTableHeader();

        ColumnHeaderToolTips shelvesTips = new ColumnHeaderToolTips();
        for (int c = 0; c < shelvesTable.getColumnCount(); c++) {
            TableColumn col = shelvesTable.getColumnModel().getColumn(c);
            shelvesTips.setToolTip(col, (String)col.getHeaderValue());
        }
        shelvesHeader.addMouseMotionListener(shelvesTips);
        //need control
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

        deleteExpiretItesmButton.setFont(new Font("Century", 0, 14));
        deleteExpiretItesmButton.setText(printOut("deleteExipredItems"));
        deleteExpiretItesmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteExpiretItesmButtonActionPerformed(e);
            }
        });

        putItemWithShelfButton.setFont(new Font("Century", 0, 14)); // NOI18N
        putItemWithShelfButton.setText(printOut("putItemOnShelf"));

        GroupLayout logoPanelLayout = new GroupLayout(logoPanel);
        logoPanel.setLayout(logoPanelLayout);
        logoPanelLayout.setHorizontalGroup(
                logoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(logoPanelLayout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addGroup(logoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(putItemWithShelfButton)
                                        .addComponent(deleteExpiretItesmButton))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        logoPanelLayout.setVerticalGroup(
                logoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, logoPanelLayout.createSequentialGroup()
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(deleteExpiretItesmButton)
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
    private JLabel logoLabel;
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
    private JButton deleteExpiretItesmButton;
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
}