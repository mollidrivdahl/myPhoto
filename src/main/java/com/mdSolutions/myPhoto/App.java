package com.mdSolutions.myPhoto;

import com.mdSolutions.myPhoto.gui.AppGui;
import javax.swing.*;

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
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);  //opens window in center of screen
        frame.setVisible(true);
    }
}
