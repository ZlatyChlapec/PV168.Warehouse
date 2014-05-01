package cz.muni.fi.pv168.warehouse.gui;

import org.apache.derby.client.am.DateTime;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;

/**
 * @author Slapy
 */
public class MainWindow extends JFrame {
    public MainWindow() {
        initComponents();
    }

    private void itemButtonActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void shelfbuttonActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void deleteItemButtonActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void deleteShelfButtonActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void itemsTableComponentAdded(ContainerEvent e) {
        // TODO add your code here
    }

    private void shelvesTableComponentAdded(ContainerEvent e) {
        // TODO add your code here
    }

    private void initComponents() {
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
        capcityTextField = new JTextField();
        shelfbutton = new JButton();
        printoutPanel = new JPanel();
        itemsScrollPane = new JScrollPane();
        itemsTable = new JTable();
        shelvesScrollPane = new JScrollPane();
        shelvesTable = new JTable();
        deleteItemButton = new JButton();
        deleteShelfButton = new JButton();
        listAllItemsLabel = new JLabel();
        listAllShelvesLabel = new JLabel();
        infoPanel = new JPanel();
        textOutputLabel = new JLabel();

        //======== this ========
        setForeground(Color.black);
        Container contentPane = getContentPane();

        //======== submitItemPanel ========
        {

            //---- panelTitleLabel ----
            panelTitleLabel.setText("Item");
            panelTitleLabel.setFont(new Font("Century", Font.BOLD, 18));

            //---- weightLabel ----
            weightLabel.setText("Weight");
            weightLabel.setFont(new Font("Century", Font.PLAIN, 14));

            //---- dangerousCheckBox ----
            dangerousCheckBox.setFont(new Font("Century", Font.PLAIN, 16));

            //---- storeDaysLabel ----
            storeDaysLabel.setText("Days to store");
            storeDaysLabel.setFont(new Font("Century", Font.PLAIN, 14));

            //---- dangerousLabel ----
            dangerousLabel.setText("Dangerous");
            dangerousLabel.setFont(new Font("Century", Font.PLAIN, 14));

            //---- itemButton ----
            itemButton.setText("Insert");
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
                                    .addContainerGap()
                                    .addComponent(panelTitleLabel)
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                                                    .addComponent(dangerousCheckBox))
                                            .addGroup(submitItemPanelLayout.createSequentialGroup()
                                                    .addComponent(storeDaysLabel, GroupLayout.PREFERRED_SIZE, 112, GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(dangerousLabel)))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                                    .addComponent(itemButton, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE)
                                    .addGap(29, 29, 29))
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
                                            .addComponent(itemButton, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE))
                                    .addGap(74, 74, 74))
            );
        }

        //======== submitShelfPanel ========
        {

            //---- panelTitleLabel2 ----
            panelTitleLabel2.setText("Shelf");
            panelTitleLabel2.setFont(new Font("Century", Font.BOLD, 18));

            //---- columnLabel ----
            columnLabel.setText("Column");
            columnLabel.setFont(new Font("Century", Font.PLAIN, 14));

            //---- secureCheckBox ----
            secureCheckBox.setFont(new Font("Century", Font.PLAIN, 16));

            //---- rowLabel ----
            rowLabel.setText("Row");
            rowLabel.setFont(new Font("Century", Font.PLAIN, 14));

            //---- secureLabel ----
            secureLabel.setText("Secure");
            secureLabel.setFont(new Font("Century", Font.PLAIN, 14));

            //---- maxWeightLabel ----
            maxWeightLabel.setText("Maximal weight");
            maxWeightLabel.setFont(new Font("Century", Font.PLAIN, 14));

            //---- capacityLabel ----
            capacityLabel.setText("Capacity");
            capacityLabel.setFont(new Font("Century", Font.PLAIN, 14));

            //---- shelfbutton ----
            shelfbutton.setText("Insert");
            shelfbutton.setFont(new Font("Century", Font.PLAIN, 14));
            shelfbutton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    shelfbuttonActionPerformed(e);
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
                                                    .addComponent(capcityTextField, GroupLayout.PREFERRED_SIZE, 130, GroupLayout.PREFERRED_SIZE)
                                                    .addGap(30, 30, 30)
                                                    .addComponent(secureCheckBox)))
                                    .addGap(35, 35, 35)
                                    .addComponent(shelfbutton, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE)
                                    .addContainerGap(16, Short.MAX_VALUE))
            );
            submitShelfPanelLayout.setVerticalGroup(
                    submitShelfPanelLayout.createParallelGroup()
                            .addGroup(GroupLayout.Alignment.TRAILING, submitShelfPanelLayout.createSequentialGroup()
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(submitShelfPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                            .addComponent(shelfbutton, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE)
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
                                                            .addComponent(capcityTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(columnTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(secureCheckBox))))
                                    .addGap(74, 74, 74))
            );
        }

        //======== printoutPanel ========
        {

            //======== itemsScrollPane ========
            {

                //---- itemsTable ----
                itemsTable.setFont(new Font("Century", Font.PLAIN, 14));
                itemsTable.setModel(new DefaultTableModel(
                        new Object[][]{
                                {null, null, null},
                                {null, null, null},
                                {null, null, null},
                                {null, null, null}
                        },
                        new String[]{
                                "Weight", "Expiration", "Dangerous"
                        }
                ) {
                    Class[] types = new Class[]{
                            Double.class, DateTime.class, Boolean.class
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
                itemsTable.getTableHeader().setReorderingAllowed(false);
                itemsTable.getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                if (itemsTable.getColumnModel().getColumnCount() > 0) {
                    itemsTable.getColumnModel().getColumn(0).setResizable(false);
                    itemsTable.getColumnModel().getColumn(1).setResizable(false);
                    itemsTable.getColumnModel().getColumn(2).setResizable(false);
                }
                itemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);// zabezpečuje výber len jednej položky potom môžme odmazať keď tak
                itemsTable.addContainerListener(new ContainerAdapter() {
                    @Override
                    public void componentAdded(ContainerEvent e) {
                        itemsTableComponentAdded(e);
                    }
                });

                itemsScrollPane.setViewportView(itemsTable);
            }

            //======== shelvesScrollPane ========
            {

                //---- shelvesTable ----
                shelvesTable.setFont(new Font("Century", Font.PLAIN, 14));
                shelvesTable.setModel(new DefaultTableModel(
                        new Object [][] {
                                {null, null, null, null, null},
                                {null, null, null, null, null},
                                {null, null, null, null, null},
                                {null, null, null, null, null}
                        },
                        new String [] {
                                "Column", "Row", "Maximal Weight", "Capacity", "Secure"
                        }
                ){
                    Class[] types = new Class [] {
                            Integer.class, Integer.class, Double.class, Integer.class, Boolean.class
                    };
                    boolean[] canEdit = new boolean [] {
                            false, false, false, false, false
                    };

                    public Class getColumnClass(int columnIndex) {
                        return types [columnIndex];
                    }

                    public boolean isCellEditable(int rowIndex, int columnIndex) {
                        return canEdit [columnIndex];
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
                }
                shelvesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);// zabezpečuje výber len jednej položky potom môžme odmazať keď tak
                shelvesTable.addContainerListener(new ContainerAdapter() {
                    @Override
                    public void componentAdded(ContainerEvent e) {
                        shelvesTableComponentAdded(e);
                    }
                });

                shelvesScrollPane.setViewportView(shelvesTable);
            }

            //---- deleteItemButton ----
            deleteItemButton.setText("Delete selected item");
            deleteItemButton.setFont(new Font("Century", Font.PLAIN, 14));
            deleteItemButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    deleteItemButtonActionPerformed(e);
                }
            });

            //---- deleteShelfButton ----
            deleteShelfButton.setText("Delete selected shelf");
            deleteShelfButton.setFont(new Font("Century", Font.PLAIN, 14));
            deleteShelfButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    deleteShelfButtonActionPerformed(e);
                }
            });

            //---- listAllItemsLabel ----
            listAllItemsLabel.setText("List of all items");
            listAllItemsLabel.setFont(new Font("Century", Font.PLAIN, 16));

            //---- listAllShelvesLabel ----
            listAllShelvesLabel.setText("List of all shelves");
            listAllShelvesLabel.setFont(new Font("Century", Font.PLAIN, 16));

            GroupLayout printoutPanelLayout = new GroupLayout(printoutPanel);
            printoutPanel.setLayout(printoutPanelLayout);
            printoutPanelLayout.setHorizontalGroup(
                    printoutPanelLayout.createParallelGroup()
                            .addGroup(printoutPanelLayout.createSequentialGroup()
                                    .addGap(34, 34, 34)
                                    .addComponent(itemsScrollPane, GroupLayout.PREFERRED_SIZE, 320, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                                    .addComponent(shelvesScrollPane, GroupLayout.PREFERRED_SIZE, 478, GroupLayout.PREFERRED_SIZE)
                                    .addContainerGap())
                            .addGroup(GroupLayout.Alignment.TRAILING, printoutPanelLayout.createSequentialGroup()
                                    .addGap(108, 108, 108)
                                    .addComponent(deleteItemButton, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 271, Short.MAX_VALUE)
                                    .addComponent(deleteShelfButton, GroupLayout.PREFERRED_SIZE, 175, GroupLayout.PREFERRED_SIZE)
                                    .addGap(154, 154, 154))
                            .addGroup(printoutPanelLayout.createSequentialGroup()
                                    .addGap(130, 130, 130)
                                    .addComponent(listAllItemsLabel)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 322, Short.MAX_VALUE)
                                    .addComponent(listAllShelvesLabel)
                                    .addGap(186, 186, 186))
            );
            printoutPanelLayout.setVerticalGroup(
                printoutPanelLayout.createParallelGroup()
                    .addGroup(GroupLayout.Alignment.TRAILING, printoutPanelLayout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addGroup(printoutPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(listAllItemsLabel, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
                            .addComponent(listAllShelvesLabel, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(printoutPanelLayout.createParallelGroup()
                            .addComponent(itemsScrollPane, GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
                            .addGroup(printoutPanelLayout.createSequentialGroup()
                                .addComponent(shelvesScrollPane, GroupLayout.PREFERRED_SIZE, 305, GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(printoutPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(deleteShelfButton, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE)
                            .addComponent(deleteItemButton, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(13, Short.MAX_VALUE))
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
                            .addGroup(GroupLayout.Alignment.TRAILING, infoPanelLayout.createSequentialGroup()
                                    .addContainerGap(88, Short.MAX_VALUE)
                                    .addComponent(textOutputLabel, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE)
                                    .addContainerGap())
            );
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(GroupLayout.Alignment.LEADING, contentPaneLayout.createSequentialGroup()
                                                .addComponent(submitItemPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(infoPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addComponent(submitShelfPanel, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(17, Short.MAX_VALUE))
                        .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                                .addContainerGap(15, Short.MAX_VALUE)
                                .addComponent(printoutPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addComponent(submitItemPanel, GroupLayout.PREFERRED_SIZE, 94, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(infoPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(submitShelfPanel, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
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
    private JTextField capcityTextField;
    private JButton shelfbutton;
    private JPanel printoutPanel;
    private JScrollPane itemsScrollPane;
    private JTable itemsTable;
    private JScrollPane shelvesScrollPane;
    private JTable shelvesTable;
    private JButton deleteItemButton;
    private JButton deleteShelfButton;
    private JLabel listAllItemsLabel;
    private JLabel listAllShelvesLabel;
    private JPanel infoPanel;
    private JLabel textOutputLabel;
}
