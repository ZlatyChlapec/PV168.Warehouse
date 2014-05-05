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
import java.awt.*;
import java.awt.event.*;
import java.io.UnsupportedEncodingException;
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

    public MainWindow() {

        try {
            myResources = ResourceBundle.getBundle("lang", Locale.getDefault());
            updateItemFrame = new InsertItemFrame();
            updateShelfFrame = new InsertShelfFrame();
        } catch (MissingResourceException e) {
            logger.debug("Default resource bundle not found", e);
            myResources = ResourceBundle.getBundle("lang", new Locale("en", "GB"));
            updateItemFrame = new InsertItemFrame();
            updateShelfFrame = new InsertShelfFrame();
        }
        initComponents();
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
            JOptionPane.showMessageDialog(printoutPanel, printOut("notSelectedRow"), printOut("info"), JOptionPane.INFORMATION_MESSAGE);
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
                int result = JOptionPane.showConfirmDialog(printoutPanel, printOut("deletedConfirmation"), printOut("warning"), JOptionPane.YES_NO_OPTION);

                if (result == 0) {
                    item = itemManager.findItemById(id);
                    itemManager.deleteItem(item);
                    JOptionPane.showMessageDialog(printoutPanel, printOut("deleteItemSuccess"), printOut("deleted"), JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (MethodFailureException ex) {
                JOptionPane.showMessageDialog(printoutPanel, printOut("deleteError"), printOut("error"), JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(printoutPanel, printOut("notSelectedRow"), printOut("info"), JOptionPane.INFORMATION_MESSAGE);
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
                int result = JOptionPane.showConfirmDialog(printoutPanel, printOut("deletedConfirmation"), printOut("warning"), JOptionPane.YES_NO_OPTION);

                if (result == 0) {
                    shelf = shelfManager.findShelfById(id);
                    shelfManager.deleteShelf(shelf);
                    JOptionPane.showMessageDialog(printoutPanel, printOut("deleteItemSuccess"), printOut("deleted"), JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (MethodFailureException ex) {
                JOptionPane.showMessageDialog(printoutPanel, printOut("deleteError"), printOut("error"), JOptionPane.ERROR_MESSAGE);
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
        if (updateShelfFrame.isVisible()) {
            updateShelfFrame.requestFocus();
        } else if (updateItemFrame.isVisible()) {
            updateItemFrame.requestFocus();
        } else {
            updateItemFrame.setVisible(true);
        }

        if (swingWorkerUpdateItem != null) {
            throw new IllegalStateException("Operation is already in progress");
        }

        //updateItemButton.setEnabled(false);

        //System.out.println(itemsTable.getModel().getValueAt(itemsTable.getSelectedRow(), 0));

        //swingWorkerUpdateItem = new SwingWorkerUpdateItem(item);
        //swingWorkerUpdateItem.execute();
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

            try {
                itemManager.updateItem(item);
                JOptionPane.showMessageDialog(updateItemFrame, printOut("updateItemSuccess"), printOut("updated"), JOptionPane.INFORMATION_MESSAGE);
            } catch (MethodFailureException e) {
                JOptionPane.showMessageDialog(printoutPanel, printOut("updateError"), printOut("error"), JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
            return item;
        }
    }

    private void updateShelfButtonActionPerformed(ActionEvent e) {
        if (updateItemFrame.isVisible()) {
            updateItemFrame.requestFocus();
        } else if (updateShelfFrame.isVisible()){
            updateShelfFrame.requestFocus();
        } else {
            updateShelfFrame.setVisible(true);
        }

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

    private String printOut(String value) {
        try {
            return new String(myResources.getString(value).getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "Unsupported encoding.";
        }
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

    private void editItemButtonActionPerformed(ActionEvent e) {

    }

    private void cancelInsertItemButtonActionPerformed(ActionEvent e) {
        updateItemFrame.dispose();
    }

    private void editShelfButtonActionPerformed(ActionEvent e) {

    }

    private void cancelInsertShelfButtonActionPerformed(ActionEvent e) {
        updateShelfFrame.dispose();
    }

    private void initComponents() {

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
        }
        itemsTable.removeColumn(itemsTable.getColumnModel().getColumn(0));
        itemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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

            shelvesTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
            shelvesTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
            shelvesTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
            shelvesTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
            shelvesTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        }
        shelvesTable.removeColumn(shelvesTable.getColumnModel().getColumn(0));
        shelvesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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

        logoLabel.setFont(new Font("Century", 0, 62));
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

    class InsertItemFrame extends JFrame {

        private Point mouseDownCompCoords;
        public InsertItemFrame() {
            initComponents();
        }

        private void initComponents() {

            submitItemPanel = new JPanel();
            itemPanelTitleLable = new JLabel();
            weightLabel = new JLabel();
            weightSpinner = new JSpinner();
            storeDaysLabel = new JLabel();
            storeDaysSpinner = new JSpinner();
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

            submitShelfPanel = new JPanel();
            shelfPanelTitleLable1 = new JLabel();
            columnLabel = new JLabel();
            columnSpinner = new JSpinner();
            rowLabel = new JLabel();
            rowSpinner = new JSpinner();
            maxWeightLabel = new JLabel();
            maxWeightSpinner = new JSpinner();
            capacitytLabel = new JLabel();
            capacitySpinner = new JSpinner();
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