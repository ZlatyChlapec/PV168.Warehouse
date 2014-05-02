package cz.muni.fi.pv168.warehouse.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Slapy
 */
public class UpdateItemWindow extends JFrame {
    public UpdateItemWindow() {
        initComponents();
    }

    private void itemButtonActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void initComponents() {

        submitItemPanel = new JPanel();
        weightTextField = new JTextField();
        weightLabel = new JLabel();
        storeDaysTextField = new JTextField();
        dangerousCheckBox = new JCheckBox();
        storeDaysLabel = new JLabel();
        dangerousLabel = new JLabel();
        itemButton = new JButton();

        //======== this ========
        setTitle("Update selected record");
        setUndecorated(true);
        setResizable(false);
        getRootPane().setWindowDecorationStyle(JRootPane.WHEN_IN_FOCUSED_WINDOW);
        Container contentPane = getContentPane();

        //======== submitItemPanel ========
        {

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
            itemButton.setText("Update");
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
                                                    .addComponent(dangerousLabel)))
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            submitItemPanelLayout.setVerticalGroup(
                    submitItemPanelLayout.createParallelGroup()
                            .addGroup(GroupLayout.Alignment.TRAILING, submitItemPanelLayout.createSequentialGroup()
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(submitItemPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                            .addGroup(submitItemPanelLayout.createSequentialGroup()
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

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(submitItemPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(submitItemPanel, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pack();
        setLocationRelativeTo(getOwner());
    }

    private JPanel submitItemPanel;
    private JTextField weightTextField;
    private JLabel weightLabel;
    private JTextField storeDaysTextField;
    private JCheckBox dangerousCheckBox;
    private JLabel storeDaysLabel;
    private JLabel dangerousLabel;
    private JButton itemButton;
}
