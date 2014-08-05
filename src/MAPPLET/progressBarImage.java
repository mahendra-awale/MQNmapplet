/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MAPPLET;

import java.awt.Color;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;

/**
 *
 * @author mahendra
 */
public class progressBarImage extends JWindow {

    progressBarImage() {
        
        this.setSize(260, 60);
        JLabel j1 = new JLabel();
        java.net.URL imageURL1 = getClass().getResource("/images/progressBar.gif");
        ImageIcon icon = new ImageIcon(imageURL1);
        j1.setIcon(icon);

        this.getContentPane().setBackground(Color.BLUE);
        this.getContentPane().add(j1);
        this.setLocationRelativeTo(this);
    }
}
