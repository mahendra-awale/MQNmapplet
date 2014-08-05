/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MAPPLET;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 * Display The MSG
 *
 * @author mahendra
 */
public class displayInfoOnBinView implements ActionListener {

    boolean doNotshowTheMsg = false;
    JFrame msgFrame;
    JCheckBox chk;
    JLabel jl;
    JButton ok;

    displayInfoOnBinView() throws InterruptedException {

        msgFrame = new JFrame();
        msgFrame.setSize(600, 100);
        msgFrame.setResizable(false);
        msgFrame.setLayout(new FlowLayout());

        jl = new JLabel("Displaying 1000 structures only (evenly sample from bin)!!");
        chk = new JCheckBox("Do not show this message again!");
        ok = new JButton("OK");

        ok.addActionListener(this);
        msgFrame.getContentPane().add(jl);
        msgFrame.getContentPane().add(ok);
        msgFrame.getContentPane().add(chk);
        msgFrame.setLocation(300, 300);

        msgFrame.setUndecorated(true);
        msgFrame.getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {

        if (ae.getActionCommand().equals("OK")) {
            this.msgFrame.setVisible(false);
            doNotshowTheMsg = this.chk.isSelected();
        }
    }

    public String displayMsgWindow() {

        if (this.doNotshowTheMsg) {
            return null;
        } else {

            msgFrame.setVisible(false);
            msgFrame.setVisible(true);
            msgFrame.setAlwaysOnTop(true);

            msgFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            return null;
        }
    }
}