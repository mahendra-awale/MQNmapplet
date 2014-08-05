/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package locateYourMol;

import MAPPLET.appletMain;
import MAPPLET.imageHandler;
import MAPPLET.logWindow;
import chemaxon.marvin.beans.MSketchPane;
import chemaxon.struc.Molecule;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

/**
 * This class is for drawing the molecule and submit it as query for location of
 * molecule in map..!!
 *
 * @author mahendra
 */
public class drawMolandSubmit extends JFrame implements ActionListener {

    private final MSketchPane msp0;
    Container cnt;
    JButton submit;
    JLabel info;
    appletMain ma;
    imageHandler ih;

    public drawMolandSubmit(appletMain ma, imageHandler ih) {
        this.ma = ma;
        this.ih = ih;

        this.setSize(450, 470);
        this.setLayout(null);
        this.setTitle("Locate Molecule on Map");
        cnt = this.getContentPane();

        /*
         * marvin sketch pane construct
         */
        msp0 = new MSketchPane();

        /*
         * hide menu bar
         */
        msp0.getJMenuBar().setVisible(false);

        /*
         * set size & location
         */
        msp0.setSize(430, 400);
        msp0.setLocation(0, 0);

        /*
         * create new JButton, set it size and location
         */
        submit = new JButton("Submit");
        submit.setSize(100, 30);
        submit.setLocation(330, 400);
        submit.addActionListener(this);

        /*
         * create the information Lable
         */
        info = new JLabel("Draw a Molecule and Click on Submit");
        info.setSize(300, 30);
        info.setLocation(5, 400);

        /*
         * add the marvin sketch pane and JButton to container
         */
        cnt.add(msp0);
        cnt.add(submit);
        cnt.add(info);

        /*
         * do not allow for resizing
         */
        this.setResizable(false);

        /*
         * set the default close operation
         */
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    /**
     * ***************************************************************************************
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        /*
         * update the log
         */
        logWindow.updateLog("Please Wait!!.\n");
        logWindow.updateLog("Locating Your Mol");
        logWindow.updateLog(".....");

        /*
         * do not allow user to do anything else
         */
        this.ma.toggleDuringWork(false);

        /*
         * processed the user smile input
         */
        Molecule mol = msp0.getMol();
        mol.aromatize();
        String smi = mol.toFormat("smiles:u");
        String dbName = appletMain.dbName;
        String mapName = appletMain.mapName;

        smiTomapCoord mapMol = new smiTomapCoord(smi, dbName, mapName, appletMain.mapSize.x);
        try {
            int[] mapCoord = mapMol.getCoordForYourMol();

            try {
                ih.locateMolPixcel(mapCoord[0], mapCoord[1]);
                logWindow.updateLog(" Done\n");
                this.ma.toggleDuringWork(true);

            } catch (Exception ex) {
                logWindow.updateLog("Falied to Locate Mol!\n");
                this.ma.toggleDuringWork(true);
            }
        } catch (Exception ex) {
            logWindow.updateLog("Falied to Locate Mol!\n");
            this.ma.toggleDuringWork(true);
        }
    }
    /**
     * ***************************************************************************************
     */
}
