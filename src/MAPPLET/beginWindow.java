/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MAPPLET;

import java.awt.Color;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * This is window display when we start the program:
 *
 * @author mahendra
 */
class beginWindow extends JFrame {

    beginWindow() throws InterruptedException {
        this.setSize(650, 480);
        JLabel j1 = new JLabel();
        java.net.URL imageURL1 = getClass().getResource("/images/BeginWindowImage.png");
        ImageIcon icon = new ImageIcon(imageURL1);
        j1.setIcon(icon);

        this.getContentPane().setBackground(Color.WHITE);
        this.getContentPane().add(j1);
        setUndecorated(true);
        this.setLocationRelativeTo(this);
        this.setVisible(true);
    }
}

