package com.mdSolutions.myPhoto;

import com.mdSolutions.myPhoto.gui.AppGui;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * myPhoto Application
 *
 */
public class App
{
    public static void main( String[] args )
    {
        JFrame frame = new JFrame("myPhoto");
        frame.setContentPane(AppGui.getInstance().getWindowPanel());
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("closing app");
                DbAccess.getInstance().closeConnection();
                System.exit(0);
            }
        });
        frame.pack();
        frame.setLocationRelativeTo(null);  //opens window in center of screen
        frame.setVisible(true);
    }
}
