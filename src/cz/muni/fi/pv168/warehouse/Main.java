package cz.muni.fi.pv168.warehouse;

import cz.muni.fi.pv168.warehouse.gui.MainWindow;

import java.awt.*;

/**
 * @author Slapy
 * @version 22.3.2014
 */
public class Main {

    public static void main(String...args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainWindow frame = new MainWindow();
                frame.setVisible(true);
            }
        });
    }
}

