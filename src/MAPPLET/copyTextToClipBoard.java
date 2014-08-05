/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MAPPLET;

import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.*;

/**
 * This class do the two things:
 *
 * 1: Copy the given String to the clipboard: In our case we will use it for
 * copying the smile of average molecule
 *
 * 2: After copying the smile it display window containing message for the user
 *
 * This class will be call from image handler
 *
 * @author mahendra
 */
public class copyTextToClipBoard implements ClipboardOwner, ActionListener {

    /*
     * hold the smi string
     */
    String smi = "";

    /*
     * if this is true do not show the massage window anymore
     */
    static boolean doNotshowTheLog = false;

    /*
     * this is to show the massage window
     */
    JFrame logFrame;

    /*
     * check box for user to set wether massage window to be display or not
     */
    JCheckBox chk;

    /*
     * jlabel to display massage
     */
    JLabel jl;

    /*
     * ok button
     */
    JButton ok;

    /*
     * constructor of the class
     */
    copyTextToClipBoard() {

        logFrame = new JFrame();
        logFrame.setSize(400, 100);
        logFrame.setResizable(false);
        logFrame.setLayout(new FlowLayout());


        jl = new JLabel("Average Molecule Smile Copied to Clipboard!");
        chk = new JCheckBox("Do not show this message again!");
        ok = new JButton("OK");

        ok.addActionListener(this);
        logFrame.getContentPane().add(jl);
        logFrame.getContentPane().add(ok);
        logFrame.getContentPane().add(chk);
        logFrame.setLocation(300,300);
        logFrame.setUndecorated(true);
        logFrame.getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
    }

    /*
     * set the string as smi
     */
    void setString(String smi) {
        this.smi = smi;
    }

    @Override
    public void lostOwnership(Clipboard clpbrd, Transferable t) {
    }

    /*
     * method to get the content of clipboard: THIS METHOD IS NOT USED
     */
    public String getClipboardContents() throws IOException {
        String result = "";
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        //odd: the Object param of getContents is not currently used
        Transferable contents = clipboard.getContents(null);
        boolean hasTransferableText =
                (contents != null)
                && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
        if (hasTransferableText) {
            try {
                result = (String) contents.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException ex) {
                //highly unlikely since we are using a standard DataFlavor
            } catch (IOException ex) {
            }
        }
        return result;
    }

    /*
     * this method will set/copy the given string to clipboard
     */
    public void setClipboardContents(String aString) {
        StringSelection stringSelection = new StringSelection(aString);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, this);
    }

    /*
     * action to invoke after pressing copying of the smile
     */
    @Override
    public void actionPerformed(ActionEvent ae) {

        if (ae.getActionCommand().equals("OK")) {
            this.logFrame.setVisible(false);
            copyTextToClipBoard.doNotshowTheLog = this.chk.isSelected();

        } else {
            this.setClipboardContents(smi);
            this.displayClipBoardLogWindow();
        }
    }

    /*
     * display the log for clipBoard Activity
     */
    public String displayClipBoardLogWindow() {

        if (copyTextToClipBoard.doNotshowTheLog) {
            return null;
        } else {

            /*
             * close the any window if its open
             */
            logFrame.setVisible(false);

            /*
             * open the new window
             */
            logFrame.setVisible(true);
            logFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            return null;
        }
    }
       
}

