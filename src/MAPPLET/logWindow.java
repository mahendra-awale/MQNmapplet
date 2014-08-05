/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MAPPLET;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

/**
 * LOG FILE
 *
 * @author mahendra
 */
public class logWindow extends JFrame implements AdjustmentListener {

    public static JTextArea logFile;
    public static JScrollPane jspForlogFile;
    static logWindow lgw;

    logWindow() {

        /*
         * create child JScrollPane for displaying log file
         */
        this.setSize(400, 200);

        /*
         * open the log file
         */
        logFile = new JTextArea(10, 10);
        logFile.setText("LOG:");
        logFile.append("\n====================");
        Date date = new Date();
        logFile.append("\n" + date + "\n");
        logFile.append("====================");
        logFile.append("\n");
        logFile.setEditable(false);

        /*
         * add the logfile to scrollpane
         */
        jspForlogFile = new JScrollPane(logFile);
        jspForlogFile.getVerticalScrollBar().addAdjustmentListener(this);
        jspForlogFile.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jspForlogFile.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        this.getContentPane().add(jspForlogFile);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        lgw = this;
    }

    /**
     * ***************************************************************************************
     */
    /*
     * method to update the log file
     */
    public static void updateLog(String str) {
        jspForlogFile.getVerticalScrollBar().addAdjustmentListener(lgw);
        logWindow.logFile.append(str);

    }

    /**
     * ***************************************************************************************
     */
    @Override
    public void adjustmentValueChanged(AdjustmentEvent ae) {
        ae.getAdjustable().setValue(ae.getAdjustable().getMaximum());
        jspForlogFile.getVerticalScrollBar().removeAdjustmentListener(lgw);
    }
    /**
     * ***************************************************************************************
     */
}
