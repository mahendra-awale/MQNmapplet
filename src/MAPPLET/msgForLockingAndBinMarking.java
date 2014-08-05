/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MAPPLET;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 *
 * This Class is design to DISPLAY MSG when user view the bin.
 *
 * @author mahendra
 *
 */
public class msgForLockingAndBinMarking implements ActionListener {

    boolean doNotshowTheMsg = false;
    JFrame msgFrame;
    JCheckBox chk1;
    JCheckBox chk2;
    JTextArea jl;
    JButton ok1;
    static boolean lockingAndMarkingAllowed = true;

    msgForLockingAndBinMarking() {

        Color clr=new Color(235,235,235);
        msgFrame = new JFrame();
        msgFrame.setSize(600, 150);
        msgFrame.setResizable(false);
        msgFrame.setLayout(new FlowLayout());

        jl = new JTextArea("1: Bin position marked with flag!\n\n");
        jl.append("2: Mouse movement over map and some other functions are lock:"
                + "\nTO UNLOCK PLEASE CLICK ON MAP!\n");
        jl.setEditable(false);
        jl.setBackground(clr);
        jl.setForeground(Color.red);
        chk1 = new JCheckBox("Do not show this message again!");
        chk2 = new JCheckBox("Deactivate Marking and Locking!");
        chk1.setBackground(clr);
        chk2.setBackground(clr);
        ok1 = new JButton("OK");

        ok1.addActionListener(this);
        msgFrame.getContentPane().add(chk1);
        msgFrame.getContentPane().add(chk2);
        msgFrame.getContentPane().add(ok1);
        msgFrame.getContentPane().add(jl);
        msgFrame.setLocation(300, 100);
        msgFrame.getContentPane().setBackground(clr);

        msgFrame.setUndecorated(true);
        msgFrame.getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);

    }

    @Override
    public void actionPerformed(ActionEvent ae) {

        if (ae.getActionCommand().equals("OK")) {
            this.msgFrame.setVisible(false);
            doNotshowTheMsg = this.chk1.isSelected();

            if (this.chk2.isSelected()) {
                doNotshowTheMsg = true;
                lockingAndMarkingAllowed = false;
            }
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
