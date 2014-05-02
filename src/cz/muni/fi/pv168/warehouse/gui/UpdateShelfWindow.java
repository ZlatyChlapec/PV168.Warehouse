package cz.muni.fi.pv168.warehouse.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Slapy
 */
public class UpdateShelfWindow extends JFrame {
    public UpdateShelfWindow() {
        initComponents();
    }

    private void shelfbuttonActionPerformed(ActionEvent e) {
        dispose();
    }

    private void initComponents() {
        submitShelfPanel = new JPanel();
        columnTextField = new JTextField();
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

        //======== this ========
        setTitle("Update selected record");
        setUndecorated(true);
        setResizable(false);
        getRootPane().setWindowDecorationStyle(JRootPane.WHEN_IN_FOCUSED_WINDOW);
        Container contentPane = getContentPane();

        //======== submitShelfPanel ========
        {

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
            shelfbutton.setText("Update");
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
                                    .addGap(14, 14, 14)
                                    .addGroup(submitShelfPanelLayout.createParallelGroup()
                                            .addComponent(columnLabel)
                                            .addComponent(columnTextField, GroupLayout.PREFERRED_SIZE, 130, GroupLayout.PREFERRED_SIZE))
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
                                    .addComponent(shelfbutton)
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            submitShelfPanelLayout.setVerticalGroup(
                    submitShelfPanelLayout.createParallelGroup()
                            .addGroup(GroupLayout.Alignment.TRAILING, submitShelfPanelLayout.createSequentialGroup()
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(submitShelfPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                            .addComponent(shelfbutton)
                                            .addGroup(submitShelfPanelLayout.createSequentialGroup()
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

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addComponent(submitShelfPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 1, Short.MAX_VALUE))
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addComponent(submitShelfPanel, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
        );
        pack();
        setLocationRelativeTo(getOwner());
    }

    private JPanel submitShelfPanel;
    private JTextField columnTextField;
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
}
