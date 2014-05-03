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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    private void itemButtonActionPerformed(ActionEvent e) {
        if (swingWorkerAddItem != null) {

            throw new IllegalStateException("Operation is already in progress");
        }

        itemButton.setEnabled(false);

        Item item = new Item();
        item.setWeight(Double.parseDouble(weightTextField.getText()));
        item.setStoreDays(Integer.parseInt(storeDaysTextField.getText()));
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
            weightTextField.setText("");
            storeDaysTextField.setText("");
            dangerousCheckBox.setSelected(false);
            itemButton.setEnabled(true);
            swingWorkerAddItem = null;
        }
    }

    private void shelfButtonActionPerformed(ActionEvent e) {
        if (swingWorkerAddShelf != null) {
            throw new IllegalStateException("Operation is already in progress");
        }

        shelfButton.setEnabled(false);

        Shelf shelf = new Shelf();
        shelf.setColumn(Integer.parseInt(columnTextField.getText()));
        shelf.setRow(Integer.parseInt(rowTextField.getText()));
        shelf.setCapacity(Integer.parseInt(capacityTextField.getText()));
        shelf.setMaxWeight(Double.parseDouble(maxWeightTextField.getText()));
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
            shelfButton.setEnabled(true);
            columnTextField.setText("");
            rowTextField.setText("");
            capacityTextField.setText("");
            maxWeightTextField.setText("");
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
        submitItemPanel = new JPanel();
        weightTextField = new JTextField();
        panelTitleLabel = new JLabel();
        weightLabel = new JLabel();
        storeDaysTextField = new JTextField();
        dangerousCheckBox = new JCheckBox();
        storeDaysLabel = new JLabel();
        dangerousLabel = new JLabel();
        itemButton = new JButton();
        submitShelfPanel = new JPanel();
        columnTextField = new JTextField();
        panelTitleLabel2 = new JLabel();
        columnLabel = new JLabel();
        rowTextField = new JTextField();
        secureCheckBox = new JCheckBox();
        rowLabel = new JLabel();
        secureLabel = new JLabel();
        maxWeightLabel = new JLabel();
        maxWeightTextField = new JTextField();
        capacityLabel = new JLabel();
        capacityTextField = new JTextField();
        shelfButton = new JButton();
        printoutPanel = new JPanel();
        itemsScrollPane = new JScrollPane();
        itemsTable = new JTable();
        shelvesScrollPane = new JScrollPane();
        shelvesTable = new JTable();
        deleteItemButton = new JButton();
        deleteShelfButton = new JButton();
        listAllItemsLabel = new JLabel();
        listAllShelvesLabel = new JLabel();
        updateItemButton = new JButton();
        updateShelfButton = new JButton();
        infoPanel = new JPanel();
        textOutputLabel = new JLabel();

        //======== this ========
        setTitle("Warehouse Manager");
        setResizable(false);
        Container contentPane = getContentPane();

        //======== submitItemPanel ========
        {

            //---- panelTitleLabel ----
            panelTitleLabel.setText(printOut("item"));
            panelTitleLabel.setFont(new Font("Century", Font.BOLD, 18));

            //---- weightLabel ----
            weightLabel.setText(printOut("weight"));
            weightLabel.setFont(new Font("Century", Font.PLAIN, 14));

            //---- dangerousCheckBox ----
            dangerousCheckBox.setFont(new Font("Century", Font.PLAIN, 16));

            //---- storeDaysLabel ----
            storeDaysLabel.setText(printOut("storeDays"));
            storeDaysLabel.setFont(new Font("Century", Font.PLAIN, 14));

            //---- dangerousLabel ----
            dangerousLabel.setText(printOut("dangerous"));
            dangerousLabel.setFont(new Font("Century", Font.PLAIN, 14));

            //---- itemButton ----
            itemButton.setText(printOut("insert"));
            itemButton.setFont(new Font("Century", Font.PLAIN, 14));
            itemButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    itemButtonActionPerformed(e);
                }
            });

            GroupLayout submitItemPanelLayout = new GroupLayout(submitItemPanel);
            submitItemPanel.setLayout(submitItemPanelLayout);
            submitItemPanelLayout.setHorizontalGroup(
                    submitItemPanelLayout.createParallelGroup()
                            .addGroup(submitItemPanelLayout.createSequentialGroup()
                                    .addGroup(submitItemPanelLayout.createParallelGroup()
                                            .addGroup(submitItemPanelLayout.createSequentialGroup()
                                                    .addContainerGap()
                                                    .addComponent(panelTitleLabel))
                                            .addGroup(submitItemPanelLayout.createSequentialGroup()
                                                    .addGap(14, 14, 14)
                                                    .addGroup(submitItemPanelLayout.createParallelGroup()
                                                            .addComponent(weightLabel)
                                                            .addComponent(weightTextField, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE))
                                                    .addGap(30, 30, 30)
                                                    .addGroup(submitItemPanelLayout.createParallelGroup()
                                                            .addGroup(submitItemPanelLayout.createSequentialGroup()
                                                                    .addComponent(storeDaysTextField, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
                                                                    .addGap(40, 40, 40)
                                                                    .addComponent(dangerousCheckBox)
                                                                    .addGap(50, 50, 50)
                                                                    .addComponent(itemButton))
                                                            .addGroup(submitItemPanelLayout.createSequentialGroup()
                                                                    .addComponent(storeDaysLabel, GroupLayout.PREFERRED_SIZE, 112, GroupLayout.PREFERRED_SIZE)
                                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                    .addComponent(dangerousLabel)))))
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            submitItemPanelLayout.setVerticalGroup(
                    submitItemPanelLayout.createParallelGroup()
                            .addGroup(GroupLayout.Alignment.TRAILING, submitItemPanelLayout.createSequentialGroup()
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(submitItemPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                            .addGroup(submitItemPanelLayout.createSequentialGroup()
                                                    .addComponent(panelTitleLabel, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                    .addGroup(submitItemPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                            .addComponent(weightLabel)
                                                            .addComponent(storeDaysLabel)
                                                            .addComponent(dangerousLabel))
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                    .addGroup(submitItemPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                            .addComponent(weightTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(storeDaysTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                    .addGap(4, 4, 4))
                                            .addComponent(dangerousCheckBox)
                                            .addComponent(itemButton))
                                    .addGap(74, 74, 74))
            );
        }

        //======== submitShelfPanel ========
        {

            //---- panelTitleLabel2 ----
            panelTitleLabel2.setText(printOut("shelf"));
            panelTitleLabel2.setFont(new Font("Century", Font.BOLD, 18));

            //---- columnLabel ----
            columnLabel.setText(printOut("column"));
            columnLabel.setFont(new Font("Century", Font.PLAIN, 14));

            //---- secureCheckBox ----
            secureCheckBox.setFont(new Font("Century", Font.PLAIN, 16));

            //---- rowLabel ----
            rowLabel.setText(printOut("row"));
            rowLabel.setFont(new Font("Century", Font.PLAIN, 14));

            //---- secureLabel ----
            secureLabel.setText(printOut("secure"));
            secureLabel.setFont(new Font("Century", Font.PLAIN, 14));

            //---- maxWeightLabel ----
            maxWeightLabel.setText(printOut("maxWeight"));
            maxWeightLabel.setFont(new Font("Century", Font.PLAIN, 14));

            //---- capacityLabel ----
            capacityLabel.setText(printOut("capacity"));
            capacityLabel.setFont(new Font("Century", Font.PLAIN, 14));

            //---- shelfButton ----
            shelfButton.setText(printOut("insert"));
            shelfButton.setFont(new Font("Century", Font.PLAIN, 14));
            shelfButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    shelfButtonActionPerformed(e);
                }
            });

            GroupLayout submitShelfPanelLayout = new GroupLayout(submitShelfPanel);
            submitShelfPanel.setLayout(submitShelfPanelLayout);
            submitShelfPanelLayout.setHorizontalGroup(
                    submitShelfPanelLayout.createParallelGroup()
                            .addGroup(submitShelfPanelLayout.createSequentialGroup()
                                    .addGroup(submitShelfPanelLayout.createParallelGroup()
                                            .addGroup(submitShelfPanelLayout.createSequentialGroup()
                                                    .addContainerGap()
                                                    .addComponent(panelTitleLabel2))
                                            .addGroup(submitShelfPanelLayout.createSequentialGroup()
                                                    .addGap(14, 14, 14)
                                                    .addGroup(submitShelfPanelLayout.createParallelGroup()
                                                            .addComponent(columnLabel)
                                                            .addComponent(columnTextField, GroupLayout.PREFERRED_SIZE, 130, GroupLayout.PREFERRED_SIZE))))
                                    .addGap(30, 30, 30)
                                    .addGroup(submitShelfPanelLayout.createParallelGroup()
                                            .addGroup(submitShelfPanelLayout.createSequentialGroup()
                                                    .addComponent(rowTextField, GroupLayout.PREFERRED_SIZE, 130, GroupLayout.PREFERRED_SIZE)
                                                    .addGap(30, 30, 30)
                                                    .addComponent(maxWeightTextField, GroupLayout.PREFERRED_SIZE, 130, GroupLayout.PREFERRED_SIZE))
                                            .addGroup(submitShelfPanelLayout.createSequentialGroup()
                                                    .addComponent(rowLabel, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
                                                    .addGap(118, 118, 118)
                                                    .addComponent(maxWeightLabel, GroupLayout.PREFERRED_SIZE, 112, GroupLayout.PREFERRED_SIZE)))
                                    .addGap(30, 30, 30)
                                    .addGroup(submitShelfPanelLayout.createParallelGroup()
                                            .addGroup(submitShelfPanelLayout.createSequentialGroup()
                                                    .addComponent(capacityLabel, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE)
                                                    .addGap(85, 85, 85)
                                                    .addComponent(secureLabel))
                                            .addGroup(submitShelfPanelLayout.createSequentialGroup()
                                                    .addComponent(capacityTextField, GroupLayout.PREFERRED_SIZE, 130, GroupLayout.PREFERRED_SIZE)
                                                    .addGap(30, 30, 30)
                                                    .addComponent(secureCheckBox)))
                                    .addGap(35, 35, 35)
                                    .addComponent(shelfButton)
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            submitShelfPanelLayout.setVerticalGroup(
                    submitShelfPanelLayout.createParallelGroup()
                            .addGroup(GroupLayout.Alignment.TRAILING, submitShelfPanelLayout.createSequentialGroup()
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(submitShelfPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                            .addComponent(shelfButton)
                                            .addGroup(submitShelfPanelLayout.createSequentialGroup()
                                                    .addComponent(panelTitleLabel2, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                    .addGroup(submitShelfPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                            .addComponent(columnLabel)
                                                            .addComponent(rowLabel)
                                                            .addComponent(maxWeightLabel)
                                                            .addComponent(capacityLabel)
                                                            .addComponent(secureLabel))
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                    .addGroup(submitShelfPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                            .addComponent(rowTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(maxWeightTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(capacityTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(columnTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(secureCheckBox))))
                                    .addGap(74, 74, 74))
            );
        }

        //======== printoutPanel ========
        {

            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(JLabel.CENTER);
            //======== itemsScrollPane ========
            {

                //---- itemsTable ----
                itemsTable.setFont(new Font("Century", Font.PLAIN, 14));
                itemsTable.setModel(new DefaultTableModel(
                        new Object[][]{
                        },
                        new String[]{
                                printOut("id"), printOut("weight"), printOut("expiration"), printOut("dangerous")
                        }
                ) {
                    Class[] types = new Class[]{
                            Integer.class, Double.class, String.class, Boolean.class
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
                itemsTable.getTableHeader().setReorderingAllowed(false);
                itemsTable.getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
                itemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);// zabezpečuje výber len jednej položky potom môžme odmazať keď tak
                listAllItems();

                itemsScrollPane.setViewportView(itemsTable);
            }

            //======== shelvesScrollPane ========
            {

                //---- shelvesTable ----
                shelvesTable.setFont(new Font("Century", Font.PLAIN, 14));
                shelvesTable.setModel(new DefaultTableModel(
                        new Object[][]{
                        },
                        new String[]{
                                printOut("id"), printOut("column"), printOut("row"), printOut("maxWeight"), printOut("capacity"), printOut("secure")
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
                shelvesTable.getTableHeader().setReorderingAllowed(false);
                shelvesTable.getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
                shelvesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);// zabezpečuje výber len jednej položky potom môžme odmazať keď tak
                listAllShelves();

                shelvesScrollPane.setViewportView(shelvesTable);
            }

            //---- deleteItemButton ----
            deleteItemButton.setText(printOut("deleteSelectedItem"));
            deleteItemButton.setFont(new Font("Century", Font.PLAIN, 14));
            deleteItemButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    deleteItemButtonActionPerformed(e);
                }
            });

            //---- deleteShelfButton ----
            deleteShelfButton.setText(printOut("deleteSelectedShelf"));
            deleteShelfButton.setFont(new Font("Century", Font.PLAIN, 14));
            deleteShelfButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    deleteShelfButtonActionPerformed(e);
                }
            });

            //---- listAllItemsLabel ----
            listAllItemsLabel.setText(printOut("listOfAllItems"));
            listAllItemsLabel.setFont(new Font("Century", Font.PLAIN, 16));

            //---- listAllShelvesLabel ----
            listAllShelvesLabel.setText(printOut("listOfAllShelves"));
            listAllShelvesLabel.setFont(new Font("Century", Font.PLAIN, 16));

            //---- updateItemButton ----
            updateItemButton.setText(printOut("updateSelectedItem"));
            updateItemButton.setFont(new Font("Century", Font.PLAIN, 14));
            updateItemButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateItemButtonActionPerformed(e);
                }
            });

            //---- updateShelfButton ----
            updateShelfButton.setText(printOut("updateSelectedShelf"));
            updateShelfButton.setFont(new Font("Century", Font.PLAIN, 14));
            updateShelfButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateShelfButtonActionPerformed(e);
                }
            });

            GroupLayout printoutPanelLayout = new GroupLayout(printoutPanel);
            printoutPanel.setLayout(printoutPanelLayout);
            printoutPanelLayout.setHorizontalGroup(
                    printoutPanelLayout.createParallelGroup()
                            .addGroup(printoutPanelLayout.createSequentialGroup()
                                    .addGap(132, 132, 132)
                                    .addComponent(listAllItemsLabel)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 326, Short.MAX_VALUE)
                                    .addComponent(listAllShelvesLabel)
                                    .addGap(186, 186, 186))
                            .addGroup(printoutPanelLayout.createSequentialGroup()
                                    .addGap(12, 12, 12)
                                    .addGroup(printoutPanelLayout.createParallelGroup()
                                            .addGroup(printoutPanelLayout.createSequentialGroup()
                                                    .addComponent(updateItemButton)
                                                    .addGap(18, 18, 18)
                                                    .addComponent(deleteItemButton)
                                                    .addGap(0, 0, Short.MAX_VALUE))
                                            .addComponent(itemsScrollPane, GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE))
                                    .addGroup(printoutPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                            .addGroup(printoutPanelLayout.createSequentialGroup()
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                                                    .addComponent(shelvesScrollPane, GroupLayout.PREFERRED_SIZE, 483, GroupLayout.PREFERRED_SIZE)
                                                    .addGap(20, 20, 20))
                                            .addGroup(printoutPanelLayout.createSequentialGroup()
                                                    .addGap(73, 73, 73)
                                                    .addComponent(updateShelfButton)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 75, Short.MAX_VALUE)
                                                    .addComponent(deleteShelfButton)
                                                    .addGap(60, 60, 60))))
            );
            printoutPanelLayout.setVerticalGroup(
                    printoutPanelLayout.createParallelGroup()
                            .addGroup(GroupLayout.Alignment.TRAILING, printoutPanelLayout.createSequentialGroup()
                                    .addGroup(printoutPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(listAllItemsLabel, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(listAllShelvesLabel, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(printoutPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                            .addComponent(itemsScrollPane, GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
                                            .addComponent(shelvesScrollPane, GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addGroup(printoutPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(deleteItemButton)
                                            .addComponent(updateItemButton)
                                            .addComponent(deleteShelfButton)
                                            .addComponent(updateShelfButton))
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
        }

        //======== infoPanel ========
        {

            //---- textOutputLabel ----
            textOutputLabel.setFont(new Font("Century", Font.PLAIN, 14));
            textOutputLabel.setText("There is space for same info.");

            GroupLayout infoPanelLayout = new GroupLayout(infoPanel);
            infoPanel.setLayout(infoPanelLayout);
            infoPanelLayout.setHorizontalGroup(
                    infoPanelLayout.createParallelGroup()
                            .addGroup(infoPanelLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(textOutputLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addContainerGap())
            );
            infoPanelLayout.setVerticalGroup(
                    infoPanelLayout.createParallelGroup()
                            .addGroup(infoPanelLayout.createSequentialGroup()
                                    .addComponent(textOutputLabel, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE)
                                    .addGap(0, 67, Short.MAX_VALUE))
            );
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addComponent(submitItemPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(infoPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addGap(54, 54, 54))
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addComponent(submitShelfPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addContainerGap(90, Short.MAX_VALUE))
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addComponent(printoutPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 4, Short.MAX_VALUE))))
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addComponent(submitItemPanel, GroupLayout.PREFERRED_SIZE, 94, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(infoPanel, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(submitShelfPanel, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(printoutPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pack();
        setLocationRelativeTo(getOwner());
    }

    private JPanel submitItemPanel;
    private JTextField weightTextField;
    private JLabel panelTitleLabel;
    private JLabel weightLabel;
    private JTextField storeDaysTextField;
    private JCheckBox dangerousCheckBox;
    private JLabel storeDaysLabel;
    private JLabel dangerousLabel;
    private JButton itemButton;
    private JPanel submitShelfPanel;
    private JTextField columnTextField;
    private JLabel panelTitleLabel2;
    private JLabel columnLabel;
    private JTextField rowTextField;
    private JCheckBox secureCheckBox;
    private JLabel rowLabel;
    private JLabel secureLabel;
    private JLabel maxWeightLabel;
    private JTextField maxWeightTextField;
    private JLabel capacityLabel;
    private JTextField capacityTextField;
    private JButton shelfButton;
    private JPanel printoutPanel;
    private JScrollPane itemsScrollPane;
    private JTable itemsTable;
    private JScrollPane shelvesScrollPane;
    private JTable shelvesTable;
    private JButton deleteItemButton;
    private JButton deleteShelfButton;
    private JLabel listAllItemsLabel;
    private JLabel listAllShelvesLabel;
    private JButton updateItemButton;
    private JButton updateShelfButton;
    private JPanel infoPanel;
    private JLabel textOutputLabel;
}
