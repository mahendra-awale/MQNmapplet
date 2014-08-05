/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MAPPLET;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * This class displays the 300*300 JFrame window with map painted on it with
 * moving rectangle:
 *
 * Why rectangle: When user play around with MAPS in main MAPPLET window
 *
 * with high "zoom In" he/she may not be able to know which part of map he/she
 * is looking: This JFrame contain map with small size and will display the
 * rectangle on it, CORROSPONDING to which part of map user viewing in main
 * MAPPLET window!
 *
 * This class using variables from ImageHandler, appletMain classes.
 *
 * @author mahendra
 */
public class mapTrace extends JFrame implements ChangeListener {

    Image img;
    int ori_imageSizeX;
    int ori_imageSizeY;
    int rectSizeX;
    int rectSizeY;
    int rectPositionX;
    int rectPositionY;
    int mapTrace_SizeX = 300;
    int mapTrace_SizeY = 300;

    /*
     * constructor img= image x = original size of image y = original size of
     * image
     */
    void setmapForTrace(Image img, int x, int y) {

        this.img = img;
        ori_imageSizeX = x;
        ori_imageSizeY = y;
        this.setSize(300, 300);
        this.setTitle("Map Trace");
        this.setResizable(false);
    }

    /*
     * Update the rectangle on Image/Map: this methos uses the varaibles from
     * image handler and appletMain class! Method is somewhat not coded
     * correctly:Butit works! May be if you want you can change the things???!!!
     */
    void updateRectangle() {

        /*
         * 1: Start location of JViewPort of "MAPPLET main window" on image
         */
        int x_JV_START = appletMain.scrVP.getViewPosition().x;
        int y_JV_START = appletMain.scrVP.getViewPosition().y;

        /*
         * 2: Size of JViewPort of "MAPPLET main window" on image
         */
        int vx = appletMain.scrVP.getSize().width;
        int vy = appletMain.scrVP.getSize().height;

        /*
         * 3: JViewPort end co-ordinate
         */
        int x_JV_END = vx + x_JV_START;
        int y_JV_END = vy + y_JV_START;

        /*
         * 4: current size of the display image in mapplet
         */
        int sizeX_image = imageHandler.display_imageSizeX;
        int sizeY_image = imageHandler.display_imageSizeY;

        /*
         * 5: determine the Position of Jviewport on actual image
         * size(unstreched)
         */
        int act_xSTART = this.ori_imageSizeX * x_JV_START / sizeX_image;
        int act_ySTART = this.ori_imageSizeY * y_JV_START / sizeY_image;

        int act_xEND = this.ori_imageSizeX * x_JV_END / sizeX_image;
        int act_yEND = this.ori_imageSizeY * y_JV_END / sizeY_image;

        /*
         * 6:Calculate the position of JViewPort with respect to size of the
         * image display in this JFrame
         */

        int new_xSTART = (this.mapTrace_SizeX * act_xSTART / this.ori_imageSizeX);
        int new_ySTART = (this.mapTrace_SizeY * act_ySTART / this.ori_imageSizeY);

        int new_xEND = (this.mapTrace_SizeX * act_xEND / this.ori_imageSizeX);
        int new_yEND = (this.mapTrace_SizeY * act_yEND / this.ori_imageSizeY);

        /*
         * 7: calculate the size of the JViewPort
         */
        int new_VPwidth = (new_xEND - new_xSTART);
        int new_VPHeigth = (new_yEND - new_ySTART);

        /*
         * This is something badly coded here: may be one can find alternative
         * way To do this
         */

        /*
         * Recatngle should not go outside the range
         */


        if (new_VPHeigth >= 295) {
            new_VPHeigth = 292;
        }

        if (new_VPwidth >= 295) {
            new_VPwidth = 292;
        }

        if (new_xSTART <= 5) {
            new_xSTART = 5;
        }

        if (new_ySTART <= 30) {
            new_ySTART = 35;
        }

        if ((new_VPwidth + new_xSTART) >= 299) {
            new_VPwidth = (Math.abs(299 - new_xSTART));
            new_xSTART = (new_xSTART - ((new_VPwidth + new_xSTART) - 293));
        }

        if ((new_VPHeigth + new_ySTART) >= 299) {
            new_VPHeigth = (Math.abs(299 - new_ySTART));
            new_ySTART = (new_ySTART - ((new_VPHeigth + new_ySTART) - 293));
        }

        /*
         * set the rectangle now
         */
        this.rectPositionX = new_xSTART;
        this.rectPositionY = new_ySTART;
        this.rectSizeX = (new_VPwidth);
        this.rectSizeY = (new_VPHeigth);
        this.repaint();
    }

    /**
     * ***************************************************************************************
     */
    @Override
    public void paint(Graphics g) {

        g.drawImage(img, 0, 0, mapTrace_SizeX, mapTrace_SizeY, this);
        g.setColor(Color.WHITE);
        g.drawRect(rectPositionX, rectPositionY, rectSizeX, rectSizeY);
        g.drawRect(rectPositionX + 1, rectPositionY + 1, rectSizeX, rectSizeY);
    }

    /**
     * ***************************************************************************************
     */
    @Override
    public void stateChanged(ChangeEvent ce) {
        updateRectangle();
    }
    /**
     * ***************************************************************************************
     */
}
