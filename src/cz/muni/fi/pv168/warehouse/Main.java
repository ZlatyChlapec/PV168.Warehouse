package cz.muni.fi.pv168.warehouse;

import cz.muni.fi.pv168.warehouse.gui.MainWindow;
import cz.muni.fi.pv168.warehouse.gui.UpdateItemWindow;
import cz.muni.fi.pv168.warehouse.gui.UpdateShelfWindow;

import javax.swing.*;

/**
 * @author Slapy
 * @version 22.3.2014
 */
public class Main {

    public static void main(String...args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainWindow frame = new MainWindow();
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.setVisible(true);
                UpdateItemWindow updateFrame = new UpdateItemWindow();
                updateFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                updateFrame.setVisible(true);
                UpdateShelfWindow shelfFrame = new UpdateShelfWindow();
                shelfFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                shelfFrame.setVisible(true);
            }
        });
    }
}

