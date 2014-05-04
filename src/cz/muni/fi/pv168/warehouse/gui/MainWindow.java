package cz.muni.fi.pv168.warehouse.gui;

import cz.muni.fi.pv168.warehouse.database.SpringConfig;
import cz.muni.fi.pv168.warehouse.entities.Item;
import cz.muni.fi.pv168.warehouse.entities.Shelf;
import cz.muni.fi.pv168.warehouse.exceptions.MethodFailureException;
import cz.muni.fi.pv168.warehouse.managers.ItemManager;
import cz.muni.fi.pv168.warehouse.managers.ItemManagerImpl;
import cz.muni.fi.pv168.warehouse.managers.ShelfManager;
import cz.muni.fi.pv168.warehouse.managers.ShelfManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * @author Slapy
 */
public class MainWindow extends JFrame {

    public static final Logger logger = LoggerFactory.getLogger(MainWindow.class);

    private ResourceBundle myResources;
    private UpdateItemWindow updateFrame = new UpdateItemWindow();
    private UpdateShelfWindow shelfFrame = new UpdateShelfWindow();
    private SwingWorkerAddItem swingWorkerAddItem;
    private SwingWorkerAddShelf swingWorkerAddShelf;
    private SwingWorkerDeleteItem swingWorkerDeleteItem;
    private SwingWorkerDeleteShelf swingWorkerDeleteShelf;
    private SwingWorkerUpdateItem swingWorkerUpdateItem;
    private SwingWorkerUpdateShelf swingWorkerUpdateShelf;

    public MainWindow() {

        try {
            myResources = ResourceBundle.getBundle("lang", Locale.getDefault());
        } catch (MissingResourceException e) {
            logger.debug("Default resource bundle not found", e);
            myResources = ResourceBundle.getBundle("lang", new Locale("en", "GB"));
        }
        try {
            initComponents();
        } catch (UnsupportedEncodingException e) {
            logger.debug("Error while initializing components");
            e.printStackTrace();
        }
    }

    private void insertItemButtonActionPerformed(ActionEvent e) {
        if (swingWorkerAddItem != null) {

            throw new IllegalStateException("Operation is already in progress");
        }

        insertItemButton.setEnabled(false);

        Item item = new Item();
        item.setWeight(Double.parseDouble(weightSpinner.getValue().toString()));
        item.setStoreDays(Integer.parseInt(storeDaysSpinner.getValue().toString()));
        item.setDangerous(dangerousCheckBox.isSelected());

        swingWorkerAddItem = new SwingWorkerAddItem(item);
        swingWorkerAddItem.execute();
    }

    class SwingWorkerAddItem extends SwingWorker<Item, Void> {
        private ItemManager itemManager;
        private Item item;

        public SwingWorkerAddItem(Item item) {
            this.item = item;
        }

        @Override
        protected Item doInBackground() throws Exception {
            ApplicationContext springContext = new AnnotationConfigApplicationContext(SpringConfig.class);
            itemManager = springContext.getBean("itemManager", ItemManagerImpl.class);
            try {
                itemManager.createItem(item);
            } catch (MethodFailureException ex) {
                ex.printStackTrace();
            }

            return item;
        }

        @Override
        protected void done() {
            listAllItems();
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

        swingWorkerAddShelf = new SwingWorkerAddShelf(shelf);
        swingWorkerAddShelf.execute();
    }

    class SwingWorkerAddShelf extends SwingWorker<Shelf, Void> {
        private ShelfManager shelfManager;
        private Shelf shelf;

        public SwingWorkerAddShelf(Shelf shelf) {
            this.shelf = shelf;
        }

        @Override
        protected Shelf doInBackground() throws Exception {
            ApplicationContext springContext = new AnnotationConfigApplicationContext(SpringConfig.class);
            shelfManager = springContext.getBean("shelfManager", ShelfManagerImpl.class);
            try {
                shelfManager.createShelf(shelf);
            } catch (MethodFailureException ex) {
                ex.printStackTrace();
            }

            return shelf;
        }

        @Override
        protected void done() {
            listAllShelves();
            insertShelfButton.setEnabled(true);
            columnSpinner.setValue(1);
            rowSpinner.setValue(1);
            capacitySpinner.setValue(1);
            maxWeightSpinner.setValue(0.01);
            secureCheckBox.setSelected(false);
            swingWorkerAddShelf = null;
        }
    }

    private void deleteItemButtonActionPerformed(ActionEvent e) {
        if (swingWorkerAddItem != null) {
            throw new IllegalStateException("Operation is already in progress");
        }

        if (itemsTable.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(printoutPanel, "Select row in table, which you want to delete", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        deleteItemButton.setEnabled(false);

        swingWorkerDeleteItem = new SwingWorkerDeleteItem((Integer) itemsTable.getModel().getValueAt(itemsTable.getSelectedRow(), 0));
        swingWorkerDeleteItem.execute();
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
                int result = JOptionPane.showConfirmDialog(printoutPanel, "Remove this object ?", "Warning", JOptionPane.YES_NO_OPTION);

                if (result == 0) {
                    item = itemManager.findItemById(id);
                    itemManager.deleteItem(item);
                    JOptionPane.showMessageDialog(printoutPanel, "Item successfully deleted", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (MethodFailureException ex) {
                JOptionPane.showMessageDialog(printoutPanel, "Error while deleting", "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
            return item;
        }

        @Override
        protected void done() {
            listAllItems();
            deleteItemButton.setEnabled(true);
            swingWorkerDeleteItem = null;
        }
    }

    private void deleteShelfButtonActionPerformed(ActionEvent e) {
        if (swingWorkerDeleteShelf != null) {
            throw new IllegalStateException("Operation is already in progress");
        }

        if (shelvesTable.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(printoutPanel, "Select row in table, which you want to delete", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        deleteShelfButton.setEnabled(false);

        swingWorkerDeleteShelf = new SwingWorkerDeleteShelf((Integer) shelvesTable.getModel().getValueAt(shelvesTable.getSelectedRow(), 0));
        swingWorkerDeleteShelf.execute();
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
                int result = JOptionPane.showConfirmDialog(printoutPanel, "Remove this object ?", "Warning", JOptionPane.YES_NO_OPTION);

                if (result == 0) {
                    shelf = shelfManager.findShelfById(id);
                    shelfManager.deleteShelf(shelf);
                    JOptionPane.showMessageDialog(printoutPanel, "Shelf successfully deleted", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (MethodFailureException ex) {
                JOptionPane.showMessageDialog(printoutPanel, "Error while deleting", "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
            return shelf;
        }

        @Override
        protected void done() {
            listAllShelves();
            deleteShelfButton.setEnabled(true);
            swingWorkerDeleteShelf = null;
        }
    }

    private void updateItemButtonActionPerformed(ActionEvent e) {
        if (shelfFrame.isVisible()) {
            shelfFrame.requestFocus();
        } else if (updateFrame.isVisible()) {
            updateFrame.requestFocus();
        } else {
            updateFrame.setVisible(true);
        }
        updateFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        if (swingWorkerUpdateItem != null) {
            throw new IllegalStateException("Operation is already in progress");
        }

        updateItemButton.setEnabled(false);

        System.out.println(itemsTable.getModel().getValueAt(itemsTable.getSelectedRow(), 0));

        //swingWorkerUpdateItem = new SwingWorkerUpdateItem(item);
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
            itemManager = springContext.getBean("itemManger", ItemManagerImpl.class);

            try{
                itemManager.updateItem(item);
                JOptionPane.showMessageDialog(updateFrame, "Item successfully updated", "Updated", JOptionPane.INFORMATION_MESSAGE);
            } catch (MethodFailureException e) {
                JOptionPane.showMessageDialog(printoutPanel, "Error while updating", "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
            return item;
        }
    }

    private void updateShelfButtonActionPerformed(ActionEvent e) {
        if (updateFrame.isVisible()) {
            updateFrame.requestFocus();
        } else if (shelfFrame.isVisible()){
            shelfFrame.requestFocus();
        } else {
            shelfFrame.setVisible(true);
        }
        shelfFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        if (swingWorkerUpdateShelf != null) {
            throw new IllegalStateException("Operation is already in progress");
        }


    }

    class SwingWorkerUpdateShelf extends SwingWorker<Shelf, Void> {

        @Override
        protected Shelf doInBackground() throws Exception {
            return null;
        }
    }

    private String printOut(String value) throws UnsupportedEncodingException {
        return new String(myResources.getString(value).getBytes("ISO-8859-1"), "UTF-8");
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
            for (Item i : worker.get()) {
                model.addRow(new Object[]{i.getId(), i.getWeight(), getExpirationTime(i.getInsertionDate(), i.getStoreDays()), i.isDangerous()});
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
            for (Shelf i : worker.get()) {
                model.addRow(new Object[]{i.getId(), i.getColumn(), i.getRow(), i.getMaxWeight(), i.getCapacity(), i.isSecure()});
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    class SwingWorkerListAllItems extends SwingWorker<List<Item>, Void> {

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

    private void initComponents() throws UnsupportedEncodingException {

        SpinnerNumberModel storeDays = new SpinnerNumberModel(1, 1, 365, 1);
        SpinnerNumberModel column = new SpinnerNumberModel(1, 1, 365, 1);
        SpinnerNumberModel row = new SpinnerNumberModel(1, 1, 365, 1);
        SpinnerNumberModel capacity = new SpinnerNumberModel(1, 1, 365, 1);
        SpinnerNumberModel weight = new SpinnerNumberModel(0.01, 0.01, 200.00, 0.01);
        SpinnerNumberModel maxWeight = new SpinnerNumberModel(0.01, 0.01, 800.00, 0.01);

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

        setTitle("Warehouse Manager");
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        itemPanelTitleLable.setFont(new java.awt.Font("Century", 1, 18)); // NOI18N
        itemPanelTitleLable.setHorizontalAlignment(SwingConstants.LEFT);
        itemPanelTitleLable.setText(printOut("item"));

        weightLabel.setFont(new java.awt.Font("Century", 0, 14)); // NOI18N
        weightLabel.setHorizontalAlignment(SwingConstants.LEFT);
        weightLabel.setText(printOut("weight"));

        weightSpinner.setFont(new java.awt.Font("Century", 0, 14)); // NOI18N

        storeDaysLabel.setFont(new java.awt.Font("Century", 0, 14)); // NOI18N
        storeDaysLabel.setHorizontalAlignment(SwingConstants.LEFT);
        storeDaysLabel.setText(printOut("storeDays"));

        storeDaysSpinner.setFont(new java.awt.Font("Century", 0, 14)); // NOI18N

        dangerousLabel.setFont(new java.awt.Font("Century", 0, 14)); // NOI18N
        dangerousLabel.setHorizontalAlignment(SwingConstants.LEFT);
        dangerousLabel.setText(printOut("dangerous"));

        insertItemButton.setFont(new java.awt.Font("Century", 0, 14)); // NOI18N
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

        shelfPanelTitleLable1.setFont(new java.awt.Font("Century", 1, 18)); // NOI18N
        shelfPanelTitleLable1.setHorizontalAlignment(SwingConstants.LEFT);
        shelfPanelTitleLable1.setText(printOut("shelf"));

        columnLabel.setFont(new java.awt.Font("Century", 0, 14)); // NOI18N
        columnLabel.setHorizontalAlignment(SwingConstants.LEFT);
        columnLabel.setText(printOut("column"));

        columnSpinner.setFont(new java.awt.Font("Century", 0, 14)); // NOI18N

        rowLabel.setFont(new java.awt.Font("Century", 0, 14)); // NOI18N
        rowLabel.setHorizontalAlignment(SwingConstants.LEFT);
        rowLabel.setText(printOut("row"));

        rowSpinner.setFont(new java.awt.Font("Century", 0, 14)); // NOI18N

        maxWeightLabel.setFont(new java.awt.Font("Century", 0, 14)); // NOI18N
        maxWeightLabel.setHorizontalAlignment(SwingConstants.LEFT);
        maxWeightLabel.setText(printOut("maxWeight"));

        maxWeightSpinner.setFont(new java.awt.Font("Century", 0, 14)); // NOI18N

        capacitytLabel.setFont(new java.awt.Font("Century", 0, 14)); // NOI18N
        capacitytLabel.setHorizontalAlignment(SwingConstants.LEFT);
        capacitytLabel.setText(printOut("capacity"));

        capacitySpinner.setFont(new java.awt.Font("Century", 0, 14)); // NOI18N

        secureLabel.setFont(new java.awt.Font("Century", 0, 14)); // NOI18N
        secureLabel.setHorizontalAlignment(SwingConstants.LEFT);
        secureLabel.setText(printOut("secure"));

        insertShelfButton.setFont(new java.awt.Font("Century", 0, 14)); // NOI18N
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

        listAllItemsLabel.setFont(new java.awt.Font("Century", 0, 14)); // NOI18N
        listAllItemsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        listAllItemsLabel.setText(printOut("listOfAllItems"));

        itemsScrollPane.setFont(new java.awt.Font("Century", 0, 14)); // NOI18N

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        itemsTable.setModel(new DefaultTableModel(
                new Object[][]{

                },
                new String[]{
                        "id", printOut("weight"), printOut("expiration"), printOut("dangerous")
                }
        ) {
            Class[] types = new Class[]{
                    Integer.class, Double.class, Object.class, Boolean.class
            };
            boolean[] canEdit = new boolean[]{
                    false, false, false, false
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

            itemsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
            itemsTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
            itemsTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
            itemsTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        }
        itemsTable.removeColumn(itemsTable.getColumnModel().getColumn(0));
        itemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listAllItems();

        updateItemButton.setFont(new java.awt.Font("Century", 0, 14)); // NOI18N
        updateItemButton.setText(printOut("updateSelectedItem"));
        updateItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateItemButtonActionPerformed(e);
            }
        });

        deleteItemButton.setFont(new java.awt.Font("Century", 0, 14)); // NOI18N
        deleteItemButton.setText(printOut("deleteSelectedItem"));
        deleteItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteItemButtonActionPerformed(e);
            }
        });

        listAllShelvesLabel.setFont(new java.awt.Font("Century", 0, 14)); // NOI18N
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

            shelvesTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
            shelvesTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
            shelvesTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
            shelvesTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
            shelvesTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
            shelvesTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        }
        shelvesTable.removeColumn(shelvesTable.getColumnModel().getColumn(0));
        shelvesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listAllShelves();

        updateShelfButton.setFont(new java.awt.Font("Century", 0, 14)); // NOI18N
        updateShelfButton.setText(printOut("updateSelectedShelf"));
        updateShelfButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateShelfButtonActionPerformed(e);
            }
        });

        deleteShelfButton.setFont(new java.awt.Font("Century", 0, 14)); // NOI18N
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

        GroupLayout logoPanelLayout = new GroupLayout(logoPanel);
        logoPanel.setLayout(logoPanelLayout);
        logoPanelLayout.setHorizontalGroup(
                logoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        logoPanelLayout.setVerticalGroup(
                logoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );

        logoLabel.setFont(new java.awt.Font("Century", 0, 62)); // NOI18N
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        logoLabel.setText("PV168.Warehouse");

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(printoutPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(submitShelfPanel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                .addComponent(submitItemPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(logoLabel, GroupLayout.PREFERRED_SIZE, 548, GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(logoPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(submitItemPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(logoPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(logoLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
}