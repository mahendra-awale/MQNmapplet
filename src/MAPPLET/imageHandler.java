/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MAPPLET;

import chemaxon.formats.MolFormatException;
import chemaxon.struc.Molecule;
import chemaxon.util.MolHandler;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPopupMenu.Separator;
import javax.swing.*;

/**
 * 
 * Important and Nasty CLASS: Read Carefully lots of transformation of
 * Co-ORDINATES Lots of variables and many more!!!!
 *
 * In below code you will find some FLIPPING of x,y CO-ordinates and
 * some correction for y CO-ordinates.
 *
 * Why & How!!I Will try(!) to Explain!
 *
 * A: Normal Graph(map) looks like this(see below): and our PNG map is
 *  also drawn in this way:
 *****************************************************************************
 * (this is y axis)
 *
 * 4
 * -------------------------
 * 3                    (*)
 * -------------------------
 * 2
 * -------------------------
 * 1
 * -------------------------
 * 0
 * -------------------------
 *      0   1   2   3   4   (this is x axis)
 *
 * the point * is having the CO-ordinate of (4,3)
 *
 * B: Java x,y CO-ordinates start at upper left corner and when we
 * put our map in java and Display it: it will display it properly,
 * but the CO-ordinate mismatched will happen. But How ?
 *****************************************************************************
 *
 * considered our map in step A: we put into java GUI, it will still
 * display in same way but the CO-ordinate you will get for * point
 * from java is different(Below is JAVA GUI CO_ORDINATE SYSTEM).
 *
 *      0    1   2   3   4 (this is x-axis)
 * ------------------------
 * 0
 * ------------------------
 * 1                     (*)
 * ------------------------
 * 2
 * ------------------------
 * 3
 * ------------------------
 * 4
 * (This is y-axis)
 *
 * in this case java CO-ordinate for (*) point is (4,1) which is different
 * than CO-ordinate we displayed in step A:(4,3).
 *
 * So for displaying the CO-ordinates in human readable graph way:you need
 * to do correction for the y-CO-ordinates in following way.
 *
 * yforDisplay=(max Y axis-your current y value from java)
 *             yforDisplay=(4-1)=3;
 *
 *             we do not need to correct for x CO-ordinates:
 *
 * So you now have Transfered the java CO-ordinates in to the, CO-ordinates
 * which will be display in normal human readable graph.
 *
 * this CO-ordinate i called as: "xforDisplay and yforDisplay"
 *
 * C: Our data for each map is in the same format as our map is:
 * But the way it stored in matrix and way we RETRIVED data required some
 * correction..why & How!!
 *****************************************************************************
 * MAP and data is in same format:
 * like display below!!
 *
 * (this is y axis)
 *
 * 4
 * -------------------------
 * 3                    (*)
 * -------------------------
 * 2
 * -------------------------
 * 1
 * -------------------------
 * 0
 * -------------------------
 *      0   1   2   3   4   (this is x axis)
 *
 * data point (*) is at point: (4,3)
 *
 * the way we stored the data in matrix is also the same, but in matrix 
 * CO-ordinates changes it meaning: x become y and y become x: do not get 
 * confuse, let me explain it!!
 *
 * look after loading data in matrix what it will look like:
 *
 * *      0    1   2   3   4 (this is y-axis)
 * --------------------------
 * 0
 * --------------------------
 * 1                     (*)
 * --------------------------
 * 2
 * --------------------------
 * 3
 * --------------------------
 * 4
 * (This is x-axis)
 *
 * Note the below points:
 *
 * according to normal human graph point (* ) is at location (4,3).
 * In our matrix:-data for point (*) is at location (1,4).
 * In java the CO-ordinate for point (*) is at location (4,1).
 * 
 * in this case to ReTRIVED the correct data for point (*), what
 * we need to do is flip the x and y CO-ordinates which you get from
 * java: so in this case
 *
 * e.g (java coordinates y,y=(4,1) and matrix CO-ordinates (1,4))
 * x(to look up in matrix)= y from java=1
 * y(to look up in matrix)= x from java=4
 *
 * I called these two points as:
 * xForMatrix
 * yForMatrix
 *
 ****************************************************************************
 *
 * Enjoy!Still confused??keep on reading
 *
 ****************************************************************************
 * Note: It is Important to know how the GUI COMPONENET are arranged:
 * in appletMain class: Also its necessary here to explain,
 * as in this class we are playing around with GUI::SO LETS START!!
 *
 *
 * MainApplet Window----->it contains JscrollPane(called it as JSP1)
 *                  ----->it contains Four JButtons
 *                  ----->it contains Four JTextFields
 *
 * and some other COMPONENETS: what important to us is JSP1 and JButtons
 *
 * JSP1--------it contains JPanel (this is "JPanel for image")
 *
 * I created my custom JPanel:which is nothing but this class our maps
 * (PNG FILE) will be drawn on this class JPanel..called it as
 * "THISCLASSJPANEL"
 *
 * so whenever user loads the new database, change the maps or when zoom
 * In or zoom Out with  maps what exactly happen is
 *
 * each time image is redrawn with specified size on "THISCLASSJPANEL"
 * and "THISCLASSJPANEL" then put into panelForImage.
 *
 * to redrawn image we need o called the paint method of "THISCLASSJPANEL"
 *
 * Also one of the important thing is setting correct position for
 * JViewPort of JSP1...if it not set correctly you will get the zoom In/zoom
 * out effects but then you will see the different region of image:
 * 
 * so we always need to find out correct position for JViewPort, so that
 * we see the same image part before & after zoom In or zoom Out.
 *
 * Standard Process for repainting is: 
 * 1st remove the COMPONENET from its parent then repaint it and then again put
 * it back in Parent.!!
 *
 * Its not possible to explain all the things here: I will suggest to run the
 *  application and see what happens!
 * ****************************************************************************
 *               
 * @author mahendra
 */
public class imageHandler extends JPanel implements MouseWheelListener, MouseListener, MouseMotionListener, ActionListener {

    /*image object*/
    Image img;

    /*this is original size of image*/
    static int ori_imageSizeX;
    static int ori_imageSizeY;

    /*this is display size of image*/
    static int display_imageSizeX;
    static int display_imageSizeY;

    /*minimum zoom to be mantained*/
    static int minZoomtoKeep_X;
    static int minZoomtoKeep_Y;

    /*maximum zoom allow::we will calculate it based upon image size*/
    static int maxZoomAllow_X;
    static int maxZoomAllow_Y;

    /*this is "JPanel for image" in applet: on which we are going to
     *put the "THISCLASSJPANEL" class which contain actual image
     */
    JPanel panelForImage;

    /*JVIEWPORT OF JSP1 which is focuing on "JPanel for image" which
    ultamately containing THISCLASSJPANEL*/
    JViewport scrVP;

    /*for handling the mouse double click we need some variables:
    java do not provide direct way to monitor double click,,so we 
    need to do this with some logic*/
    int clickCount = 0;
    int clickatX = 0;
    int clickatY = 0;
    long clickTime;

    /*some parameters need for mouse dragg function*/
    Point jvpLocationPre;
    int mouseX = 0;
    int mouseY = 0;

    /*this is pop up: when user right click on map: popup will be display*/
    private JPopupMenu pop;

    /*These are the menus that will show on pop up(right click)
     *popMol==it gives user option to visulized the bin
     *molecules:
     *popTitle show the co-ordinates on map
     *save Bin: provide option to save bin molecules
     *CopyAvgMolSMI: copy the smile of avg molecule to clipboard
     */
    private JMenuItem popMol;
    private JMenuItem popTitle;
    private JMenuItem saveBin;
    private JMenuItem CopyAvgMolSMI;
    private JMenuItem help;

    /*this is seperator for our pop menus*/
    JSeparator jSeparator1;
    JSeparator jSeparator2;
    JSeparator jSeparator3;
    JSeparator jSeparator4;
    /*this is bin file to open: note the files are arranged in different
     *folders for each X co-ordinate on map we have one folder and that
     *folder containd the files for each y co-ordinates on map
     *(so x,y coordinate tells us which bin file to open).
     */
    int binFileX;
    int binFileY;

    /*required for category map: if the molecule is tooLarge
    we will just display category of molecule*/
    String molCatagory = "";

    /*Atom count of the molecule*/
    static int atmCount;

    /*class to copy the smile of avg molecule to clipboard and
    display MSG*/
    copyTextToClipBoard cp;

    /*MAP MARKER*/
    Image mapMarkerImg;
    int mapMarkerCoordX;
    int mapMarkerCoordY;
    boolean mapMarkerIsON = false;

    /*Marked postions on the map*/
    ArrayList<Integer> markerPositonX = new ArrayList<Integer>();
    ArrayList<Integer> markerPositonY = new ArrayList<Integer>();
    
    /*Info for the selected Bin on Right Click*/
    int atmCountAtBin = 0;
    boolean problemInMolAtBin = false;
    int xForBin;
    int yForBin;

    /*Constructor of class: too Big*/
    imageHandler(int imageSizeX, int imageSizeY, JViewport scrVP, JPanel panelForImage) throws IOException {

        imageHandler.ori_imageSizeX = imageSizeX;
        imageHandler.ori_imageSizeY = imageSizeY;
        this.scrVP = scrVP;
        this.panelForImage = panelForImage;

        display_imageSizeX = 600;
        minZoomtoKeep_X = 600;
        display_imageSizeY = 600;
        minZoomtoKeep_Y = 600;

        /*set the maximum zoom allow for image*/
        imageHandler.maxZoomAllow_X = imageHandler.ori_imageSizeX * 15;
        imageHandler.maxZoomAllow_Y = imageHandler.ori_imageSizeY * 15;

        /*add the mouse Listner*/
        this.addMouseWheelListener(this);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        /*set buffering::good for image creation activity*/
        this.setDoubleBuffered(true);

        /*initialized the pop up related variables*/
        pop = new JPopupMenu();
        popTitle = new JMenuItem();
        popMol = new JMenuItem();
        saveBin = new JMenuItem();
        CopyAvgMolSMI = new JMenuItem();
        help = new JMenuItem();

        jSeparator1 = new Separator();
        jSeparator2 = new Separator();
        jSeparator3 = new Separator();
        jSeparator4 = new Separator();

        popTitle.setEnabled(false);
        pop.add(popTitle);
        pop.add(jSeparator1);

        popMol.setText("Show Bin");
        popMol.setToolTipText("View the content of bin (max display 1000 Molecules)");
        popMol.addActionListener(this);
        pop.add(popMol);
        pop.add(jSeparator2);

        saveBin.setText("Save Bin");
        saveBin.setToolTipText("Save the contents of bin");
        saveBin.addActionListener(this);
        pop.add(saveBin);
        pop.add(jSeparator3);

        CopyAvgMolSMI.setText("Copy smile");
        pop.add(CopyAvgMolSMI);
        pop.add(jSeparator4);
        cp = new copyTextToClipBoard();
        CopyAvgMolSMI.addActionListener(cp);

        help.setText("HELP");
        help.addActionListener(this);
        help.setToolTipText("Open Help doucment in Browser");
        pop.add(help);

        /*this is SwingWorker thread for blinking pixels*/
        markPixels.clear();
        killedSwingWrker();
        blink = new blickingThread();

        /*set the map marker*/
        mapMarkerImg = appletMain.toImage(ImageIO.read(getClass().getResource("/images/marker.png")));
        appletMain.MapMarker.addActionListener(this);
        appletMain.MapMarker.setName("MARKER");    
        appletMain.removeMapMarkers.addActionListener(this);
        isLockOn=false;
    }

    /******************************************************************************************/
    /*this method actually draw the image on THISCLASSJPANEL*/
    @Override
    public void paintComponent(Graphics g) {

        /*draw the image on this JPanel*/
        g.drawImage(img, 0, 0, display_imageSizeX, display_imageSizeY, this);

        /**********************************************************************/
        /*if the map marker is ON, then start drawing marker over the position
        of mouse: also take care of the previosuly mark positions*/
        if (mapMarkerIsON) {
            g.drawImage(mapMarkerImg, mapMarkerCoordX, mapMarkerCoordY, this);

            if (!this.markerPositonX.isEmpty()) {
                for (int i = 0; i < markerPositonX.size(); i++) {

                    int[] positionForMarker = getPositionForMarker(this.markerPositonX.get(i),
                            this.markerPositonY.get(i), display_imageSizeX, display_imageSizeY,
                            imageHandler.ori_imageSizeX, imageHandler.ori_imageSizeY);
                    g.drawImage(mapMarkerImg, positionForMarker[0], positionForMarker[1], this);
                }
            }
        }
        /**********************************************************************/
        /*if the Map Marker is OFF: just take care of the previosuly marked Points*/
        if (!this.markerPositonX.isEmpty() && !mapMarkerIsON) {
            for (int i = 0; i < markerPositonX.size(); i++) {
                int[] positionForMarker = getPositionForMarker(this.markerPositonX.get(i),
                        this.markerPositonY.get(i), display_imageSizeX, display_imageSizeY,
                        imageHandler.ori_imageSizeX, imageHandler.ori_imageSizeY);

                g.drawImage(mapMarkerImg, positionForMarker[0], positionForMarker[1], this);
            }
        }
    }

    /******************************************************************************************/
    /*this method will set the image with the present setting of
    size and location:*/
    public void setImage(Image img) {

        /*set the image*/
        this.img = img;

        /*set the size of this JPanel*/
        this.setSize(display_imageSizeX, display_imageSizeY);

        /*remove the conent of "panelForImage"*/
        panelForImage.removeAll();

        /*repaint the panel of this class with image of required size:
        note repaint will call paint method*/
        this.repaint();
        this.revalidate();

        /*add "this" JPanel(with image) to applet "JPanel for Image"*/
        panelForImage.setPreferredSize(new Dimension(display_imageSizeX, display_imageSizeY));
        panelForImage.add(this);

        /*repaint the panelForImage*/
        panelForImage.repaint();
        panelForImage.revalidate();       
    }
    /******************************************************************************************/

    /*Java do not provide direct way to implement double click:
     *so we need to do some trick to find out double click:
     *trick is: monitor the system time and click position:
     *if system time is sufficientyly small and click is in same
     *area then it is double click: upon double click we will
     *zoom the image at the click point*/

    @Override
    public void mouseClicked(MouseEvent me) {

        /*****************************Right click*******************************/
        /*right click = start popup menu*/
        if (me.getButton() == MouseEvent.BUTTON3 || me.isPopupTrigger()) {

            /*this is java co-ordinates*/
            int x = me.getX();
            int y = me.getY();
            xForBin=x;
            yForBin=y;
            
            /*convert the scaled co-ordinates to co-ordinates w.r.t original size!!!*/
            int test1[] = this.ScaledCoordTooriCoord(x, y, ori_imageSizeX, ori_imageSizeY, display_imageSizeX, display_imageSizeY);

            /*convert the actual image co-ordinate represented by java to human graph co-ordinates*/
            int test2[] = this.JavaCoordTonormalGraphCoord(test1[0], test1[1], this.ori_imageSizeX);

            /*the "title" of the menu is x/y coordinates(normal graph) user clicked*/
            popTitle.setText(test2[0] + "," + test2[1]);

            /*update the bin file co-ordinates*/
            binFileX = test2[0];
            binFileY = test2[1];

            atmCountAtBin = atmCount;
            problemInMolAtBin = problemInReadingMol;

            /*the "show bin mols" should only be activated if density!=0
            and the atom count size is less than 100*/
            if (appletMain.dens[test1[1]][test1[0]] == 0 || isLockOn) {
                popMol.setEnabled(false);
                saveBin.setEnabled(false);
                CopyAvgMolSMI.setEnabled(false);
            } else {

                /*allow the copy of Avg mol smile*/
                CopyAvgMolSMI.setEnabled(true);
                cp.setString(appletMain.avgmols[appletMain.xforMatrix][appletMain.yforMatrix]);

                popMol.setEnabled(true);
                saveBin.setEnabled(true);
            }

            /*show the pop at proper location: just used java co-ordinates herein*/
            pop.show(this, x, y);
            return;
        }

        /*****************************Double click*******************************/
        if (Math.abs(clickatX - me.getX()) <= 10 && Math.abs(clickatY - me.getY()) <= 10) {
            if ((System.currentTimeMillis() - clickTime) < 300) {
                maxzoomInV2(clickatX, clickatY);
            }
        } else {
            clickatX = me.getX();
            clickatY = me.getY();
            clickTime = System.currentTimeMillis();
        }

        /********************************************************************************/
        /*If the Map Marker is ON  and mouse is click then draw the marker at this point*/
        if (this.mapMarkerIsON && !isLockOn) {

            /*this is mouse co-ordinate*/
            this.mapMarkerCoordX = me.getX();
            this.mapMarkerCoordY = me.getY();
            drawMapMarker();
            this.mapMarkerIsON = false;

            /*find "absolute" x and y positions on MAP: you need to remember this
            positions*/
            int x = imageHandler.ori_imageSizeX * me.getX() / display_imageSizeX;
            int y = imageHandler.ori_imageSizeY * me.getY() / display_imageSizeY;

            this.markerPositonX.add(x);
            this.markerPositonY.add(y);
        }
        
        if (this.isLockOn)
        {
            this.unLoackMouseMovement();
        }
    }

    /******************************************************************************************/
    @Override
    public void mousePressed(MouseEvent me) {

        /*get JViewPort and its Location*/
        JViewport jv = this.scrVP;
        Point p = jv.getViewPosition();

        this.setCursor(new Cursor(Cursor.HAND_CURSOR));
        this.jvpLocationPre = p;
        this.mouseX = me.getX();
        this.mouseY = me.getY();
    }

    /******************************************************************************************/
    @Override
    public void mouseReleased(MouseEvent me) {
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    @Override
    public void mouseEntered(MouseEvent me) {
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }

    /******************************************************************************************/

    /*This method will dragg the image inside the scrollpane*/
    @Override
    public void mouseDragged(MouseEvent me) {

        /*calculate the new position for viewPort*/
        int newPositionX = this.jvpLocationPre.x + (this.mouseX - me.getX());
        int newPositionY = this.jvpLocationPre.y + (this.mouseY - me.getY());

        //prevent going over the borders. maximum=picture width minus scroll pane view width
        int maxX = display_imageSizeX - scrVP.getWidth();
        int maxY = display_imageSizeY - scrVP.getHeight();

        if (newPositionX > maxX) {
            newPositionX = maxX;
        }
        if (newPositionY > maxY) {
            newPositionY = maxY;
        }

        if (newPositionX < 0) {
            newPositionX = 0;
        }
        if (newPositionY < 0) {
            newPositionY = 0;
        }

        /*set the new Position for viewPort*/
        scrVP.setViewPosition(new Point(newPositionX, newPositionY));

        /*store the new values as old*/
        this.jvpLocationPre = scrVP.getViewPosition();
    }
    /******************************************************************************************/
    /*mouse moment over Image will be track herein*/
    Molecule mol = null;
    static boolean problemInReadingMol = false;

    @Override
    public void mouseMoved(MouseEvent me) {

        /***************************************************************/
        /*First check out wether Marker is on or not: minus of co-ordinates
        is just to make the drawing of marker at proper position with
        respect to mouse position*/
        if (this.mapMarkerIsON) {
            this.mapMarkerCoordX = me.getX() - 5;
            this.mapMarkerCoordY = me.getY() - 29;

            this.drawMapMarker();
        }

        /***************************************************************/
        /*find "absolute" x and y positions*/
        int x = imageHandler.ori_imageSizeX * me.getX() / display_imageSizeX;
        int y = imageHandler.ori_imageSizeY * me.getY() / display_imageSizeY;

        /*this is correction for co-ordinates::As i discussed previosly*/
        appletMain.xtoDisplay = x;
        appletMain.ytoDisplay = (ori_imageSizeY - 1) - y;

        appletMain.xforMatrix = y;
        appletMain.yforMatrix = x;

        /*update co-ordinate to display & density*/
        appletMain.coOrdinateDisplay.setText(appletMain.xtoDisplay + "," + appletMain.ytoDisplay);
        appletMain.densityDisplay.setText("" + appletMain.dens[appletMain.xforMatrix][appletMain.yforMatrix]);

        atmCount = 0;
        molCatagory = appletMain.avgmolInfo[appletMain.xforMatrix][appletMain.yforMatrix];

        try {
            mol = new MolHandler(appletMain.avgmols[appletMain.xforMatrix][appletMain.yforMatrix]).getMolecule();
            atmCount = mol.getAtomCount();
            this.setMoleculeOrMSG(atmCount, mol, molCatagory);

        } catch (MolFormatException ex) {

            /*some problem with reading molecule??*/
            appletMain.jPanel3.removeAll();
            appletMain.jPanel3.revalidate();
            JTextArea msg = new JTextArea("\n");
            msg.setEditable(false);
            msg.setFont(new Font("Serif", Font.BOLD, 12));
            msg.setForeground(Color.BLACK);

            if (molCatagory == null || molCatagory.isEmpty()) {
                msg.append("\n\n\n");
                msg.append("         COULD NOT DISPLAY\n");
                msg.append("         MOLECULE\n\n");
                appletMain.jPanel3.add(msg);
                appletMain.jPanel3.repaint();
                problemInReadingMol = true;
            } else {
                msg.append("\n\n\n");
                msg.append("      COULD NOT DISPLAY\n");
                msg.append("      MOLECULE\n\n");

                msg.append("      CATEGORY OF MOLECULE:\n");
                msg.append("      " + molCatagory);
                appletMain.jPanel3.add(msg);
                appletMain.jPanel3.repaint();
                problemInReadingMol = true;
            }
        }

        /*update avg stddev, put nothing if undefined*/
        if (appletMain.dens[appletMain.xforMatrix][appletMain.yforMatrix] == 0) {
            appletMain.displayAverage.setText("");
            appletMain.displayStdev.setText("");
        } else {
            appletMain.displayAverage.setText("" + appletMain.avg[appletMain.xforMatrix][appletMain.yforMatrix]);
            appletMain.displayStdev.setText("" + appletMain.stdev[appletMain.xforMatrix][appletMain.yforMatrix]);

        }
    }

    /******************************************************************************************/

    /*zoom In method*/
    void zoomIn() {

        /*1st check out wether zooming In is more than maximum zoom size*/
        if (imageHandler.maxZoomAllow_X <= display_imageSizeX || isLockOn) {
            return;
        }

        /*Before doing ZoomIn, we need some information on:
         *JViewPort Location of Applet,
         *Centre Point of JViewPort
         *we will map Centre Point of JViewPort to corrosponding point in
         *centre point in our image
         */

        /*get JViewPort and its Location*/
        JViewport jv = this.scrVP;
        Point p = jv.getViewPosition();
        int viewPortCentreX = jv.getWidth() / 2;
        int viewPortCentreY = jv.getHeight() / 2;


        /*calculate JViewPort centre w.r.t actual Position*/
        viewPortCentreX = viewPortCentreX + p.x;
        viewPortCentreY = viewPortCentreY + p.y;

        /*find out viewPortCentreX and viewPortCentreY corrosponds to which column and row
        on Image*/
        int centreColumn = imageHandler.ori_imageSizeX * viewPortCentreX / display_imageSizeX;
        int centreRow = imageHandler.ori_imageSizeY * viewPortCentreY / display_imageSizeY;

        /*Do the ZoomIn: Increase the size of JPanel and image by 500*500 pixcels*/
        display_imageSizeX = display_imageSizeX + 500;
        display_imageSizeY = display_imageSizeY + 500;

        this.setSize(new Dimension(display_imageSizeX, display_imageSizeY));

        /*need to set up applet paramters*/
        appletMain.panelForImage.removeAll();

        /*repaint the image on this JPanel*/
        this.repaint();
        this.revalidate();

        /*add "this" JPanel(image) to applet "JPanel for Image"*/
        appletMain.panelForImage.setPreferredSize(new Dimension(display_imageSizeX, display_imageSizeY));
        appletMain.panelForImage.add(this);
        appletMain.panelForImage.repaint();
        appletMain.panelForImage.revalidate();

        /*we will set the JViewPort in such a way that it's centre Point will
        again corrosponds to centreRow and centreColumn in Image*/

        /*calculate the new Co-ordinate for centreColumn and CurrentCentreRow*/
        int newCoordForColumn = (imageHandler.display_imageSizeX * centreColumn) / (ori_imageSizeX - 1);
        int newCoordForRow = (imageHandler.display_imageSizeY * centreRow) / (ori_imageSizeY - 1);

        /*its better to do it here: B/C putting it above creating unexpected Behaviour*/

        /*find out new Location for JViewPort*/
        int newPositionX = newCoordForColumn - (jv.getWidth() / 2);
        int newPositionY = newCoordForRow - (jv.getHeight() / 2);

        /*its better to do it here: B/C putting it above creating unexpected Behaviour*/
        jv.setViewPosition(new Point(newPositionX, newPositionY));
        jv.setViewPosition(new Point(newPositionX, newPositionY));//do not remove duplication of line
    }

    /******************************************************************************************/

    /*zoomVersion 2: it is same to above method only difference is that
    it take the x,y co-ordinate as input as argument: which is equivalent
    to providing the viewPortCentreX and viewPortCentreY*/
    void zoomInV2(int xCenter, int yCenter) {

        /*1st check out wether zooming In is more than maximum zoom size*/
        if (imageHandler.maxZoomAllow_X <= display_imageSizeX) {
            return;
        }

        /*find out viewPortCentreX and viewPortCentreY corrosponds to which column and row
        on Image*/
        int centreColumn = imageHandler.ori_imageSizeX * xCenter / display_imageSizeX;
        int centreRow = imageHandler.ori_imageSizeY * yCenter / display_imageSizeY;

        /*Do the ZoomIn: Increase the size of JPanel and image by 500*500 pixcels*/
        display_imageSizeX = display_imageSizeX + 500;
        display_imageSizeY = display_imageSizeY + 500;

        this.setSize(new Dimension(display_imageSizeX, display_imageSizeY));

        /*need to set up applet paramters*/
        appletMain.panelForImage.removeAll();

        /*repaint the image on this JPanel*/
        this.repaint();
        this.revalidate();

        /*add "this" JPanel(image) to applet "JPanel for Image"*/
        appletMain.panelForImage.setPreferredSize(new Dimension(display_imageSizeX, display_imageSizeY));
        appletMain.panelForImage.add(this);
        appletMain.panelForImage.repaint();
        appletMain.panelForImage.revalidate();

        /*we will set the JViewPort in such a way that it's centre Point will
        again corrosponds to centreRow and centreColumn in Image*/

        /*calculate the new Co-ordinate for centreColumn and CurrentCentreRow*/
        int newCoordForColumn = (display_imageSizeX * centreColumn) / (ori_imageSizeX - 1);
        int newCoordForRow = (display_imageSizeY * centreRow) / (ori_imageSizeY - 1);

        /*find out new Location for JViewPort*/
        int newPositionX = newCoordForColumn - (this.scrVP.getWidth() / 2);
        int newPositionY = newCoordForRow - (this.scrVP.getHeight() / 2);

        /*its better to do it here: B/C putting it above creating unexpected Behaviour*/
        this.scrVP.setViewPosition(new Point(newPositionX, newPositionY));
    }

    /******************************************************************************************/
    void maxzoomInV2(int xCenter, int yCenter) {

        /*find out viewPortCentreX and viewPortCentreY corrosponds to which column and row
        on Image*/
        int centreColumn = imageHandler.ori_imageSizeX * xCenter / display_imageSizeX;
        int centreRow = imageHandler.ori_imageSizeY * yCenter / display_imageSizeY;

        /*Do the ZoomIn: Increase the size of JPanel and image by 500*500 pixcels*/
        display_imageSizeX = imageHandler.maxZoomAllow_X;
        display_imageSizeY = imageHandler.maxZoomAllow_Y;

        this.setSize(new Dimension(display_imageSizeX, display_imageSizeY));

        /*need to set up applet paramters*/
        appletMain.panelForImage.removeAll();

        /*repaint the image on this JPanel*/
        this.repaint();
        this.revalidate();

        /*add "this" JPanel(image) to applet "JPanel for Image"*/
        appletMain.panelForImage.setPreferredSize(new Dimension(display_imageSizeX, display_imageSizeY));
        appletMain.panelForImage.add(this);
        appletMain.panelForImage.repaint();
        appletMain.panelForImage.revalidate();

        /*we will set the JViewPort in such a way that it's centre Point will
        again corrosponds to centreRow and centreColumn in Image*/

        /*calculate the new Co-ordinate for centreColumn and CurrentCentreRow*/
        int newCoordForColumn = (display_imageSizeX * centreColumn) / (ori_imageSizeX - 1);
        int newCoordForRow = (display_imageSizeY * centreRow) / (ori_imageSizeY - 1);

        /*find out new Location for JViewPort*/
        int newPositionX = newCoordForColumn - (this.scrVP.getWidth() / 2);
        int newPositionY = newCoordForRow - (this.scrVP.getHeight() / 2);

        /*its better to do it here: B/C putting it above creating unexpected Behaviour*/
        this.scrVP.setViewPosition(new Point(newPositionX, newPositionY));
        this.scrVP.setViewPosition(new Point(newPositionX, newPositionY));//do not remove duplication of line
    }

    /******************************************************************************************/
    void zoomOut() {

        /*1st check out wether zooming out is less than mimumum zoom size
        to mantained*/
        if (display_imageSizeX <= imageHandler.minZoomtoKeep_X || isLockOn) {
            return;
        }
        /*Before doing ZoomOut, we need some information on:
         *JViewPort Location of Applet,
         *Centre Point of JViewPort
         *we will map Centre Point of JViewPort to corrosponding point in
         *centre point in out image
         */

        /*get JViewPort and its Location*/
        JViewport jv = this.scrVP;
        Point p = jv.getViewPosition();

        /*Centre Point for JViewPort (considering its origin at 0,0)*/
        int viewPortCentreX = jv.getWidth() / 2;
        int viewPortCentreY = jv.getHeight() / 2;

        /*calculate JViewPort centre w.r.t actual Position*/
        viewPortCentreX = viewPortCentreX + p.x;
        viewPortCentreY = viewPortCentreY + p.y;

        /*find out viewPortCentreX and viewPortCentreY corrosponds to which column and row
        on Image*/
        int centreColumn = imageHandler.ori_imageSizeX * viewPortCentreX / display_imageSizeX;
        int centreRow = imageHandler.ori_imageSizeY * viewPortCentreY / display_imageSizeY;

        /*Do the ZoomOut: decrease the size of JPanel and image by 500*/
        display_imageSizeX = display_imageSizeX - 500;
        display_imageSizeY = display_imageSizeY - 500;

        if (display_imageSizeX < imageHandler.minZoomtoKeep_X) {
            display_imageSizeX = imageHandler.minZoomtoKeep_X;
            display_imageSizeY = imageHandler.minZoomtoKeep_Y;
        }

        this.setSize(new Dimension(display_imageSizeX, display_imageSizeY));

        /*need to set up applet paramters*/
        appletMain.panelForImage.removeAll();

        /*repaint the image on this JPanel*/
        this.repaint();
        this.revalidate();

        /*add "this" JPanel(image) to applet "JPanel for Image"*/
        appletMain.panelForImage.setPreferredSize(new Dimension(display_imageSizeX, display_imageSizeY));
        appletMain.panelForImage.add(this);
        appletMain.panelForImage.repaint();
        appletMain.panelForImage.revalidate();

        /*we will set the JViewPort in such a way that it's centre Point will
        again corrosponds to centreRow and centreColumn in Image*/

        /*calculate the new Co-ordinate for centreColumn and CurrentCentreRow*/
        int newCoordForColumn = (display_imageSizeX * centreColumn) / (ori_imageSizeX - 1);
        int newCoordForRow = (display_imageSizeY * centreRow) / (ori_imageSizeY - 1);

        /*find out new Location for JViewPort*/
        int newPositionX = newCoordForColumn - (jv.getWidth() / 2);
        int newPositionY = newCoordForRow - (jv.getHeight() / 2);

        /*its better to do it here: B/C putting it above creating unexpected Behaviour*/
        jv.setViewPosition(new Point(newPositionX, newPositionY));
    }

    /******************************************************************************************/
    /*based upon mouse wheel motion we need to zoomIn or zoomOut the image*/
    @Override
    public void mouseWheelMoved(MouseWheelEvent mwe) {


        /*disable the marker on the map 1st*/
        this.mapMarkerIsON = false;
        
        if (!isLockOn)
        {
        if (mwe.getWheelRotation() == 1) {
            zoomOut();
        } else {
            zoomIn();
        }
        }
    }

    /******************************************************************************************/
    /*method to mark the pixel of given co-ordinate*/
    public Image markGivenPixcel(int x, int y, int imageSizeX, int imageSizeY, Image img) throws InterruptedException {

        int pixels[] = new int[imageSizeX * imageSizeY];
        PixelGrabber pg = new PixelGrabber(img, 0, 0, imageSizeX, imageSizeY, pixels, 0, imageSizeX);
        pg.grabPixels();

        int pixceltoChange = 0;
        pixceltoChange = y * imageSizeX;
        pixceltoChange = pixceltoChange + x;

        Color colorModel = new Color(1f, 1f, 1f);
        pixels[pixceltoChange] = colorModel.getRGB();

        /*keep record of mark pixels*/
        markPixels.add(pixceltoChange);

        /*reconstruct the image*/
        img = createImage(new MemoryImageSource(imageSizeX, imageSizeY, pixels, 0, imageSizeX));

        /*transffered to BuffImage*/
        return img;
    }

    /******************************************************************************************/
    /*convienience method to change NormalGraph co-ordinate to way java
     * represent that co-ordinate
     */
    int[] normalGraphCoordToJavaCoord(int x, int y, int sizeY) {
        int newCoordX = x;
        int newCoordY = (sizeY - 1) - y;

        int newCoordXY[] = {newCoordX, newCoordY};
        return newCoordXY;
    }

    int[] JavaCoordTonormalGraphCoord(int x, int y, int sizeY) {
        int newCoordX = x;
        int newCoordY = (sizeY - 1) - y;

        int newCoordXY[] = {newCoordX, newCoordY};
        return newCoordXY;
    }

    /*conviniece method for: getting x,y co-ordinate for
    given pixcel after scaling*/
    int[] oriCoordToScaledCoord(int x, int y, int oriSizeX, int oriSizeY, int scaledSizeX, int scaledSizeY) {

        int newX = scaledSizeX * x / oriSizeX;
        int newY = scaledSizeY * y / oriSizeY;

        int newCoordXY[] = {newX, newY};
        return newCoordXY;
    }

    /*conviniece method for: getting x,y co-ordinate for
    given pixcel after scaling*/
    int[] ScaledCoordTooriCoord(int x, int y, int oriSizeX, int oriSizeY, int scaledSizeX, int scaledSizeY) {

        int newX = oriSizeX * x / scaledSizeX;
        int newY = oriSizeY * y / scaledSizeY;

        int newCoordXY[] = {newX, newY};
        return newCoordXY;
    }

    /******************************************************************************************/
    /*method to mark the pixel for given molecule co-ordinate:
    molecule co-ordinate is in normal human graph format*/
    public void locateMolPixcel(int x, int y) throws InterruptedException {

        if (isLockOn)
        {
        return;
        }
        /*first convert the normal graph co-ordinate to java co-ordinate*/
        int coordXY[] = this.normalGraphCoordToJavaCoord(x, y, imageHandler.ori_imageSizeY);

        /*mark that co-ordinate in image*/
        img = this.markGivenPixcel(coordXY[0], coordXY[1], ori_imageSizeX, ori_imageSizeY, img);

        int centreColumn = coordXY[0];
        int centreRow = coordXY[1];

        /*Do the ZoomIn: Increase the size of JPanel and image by 500*500 pixcels*/
        display_imageSizeX = imageHandler.maxZoomAllow_X;
        display_imageSizeY = imageHandler.maxZoomAllow_Y;

        this.setSize(new Dimension(display_imageSizeX, display_imageSizeY));

        /*need to set up applet paramters*/
        appletMain.panelForImage.removeAll();

        /*repaint the image on this JPanel*/
        this.repaint();
        this.revalidate();

        /*add "this" JPanel(image) to applet "JPanel for Image"*/
        appletMain.panelForImage.setPreferredSize(new Dimension(display_imageSizeX, display_imageSizeY));
        appletMain.panelForImage.add(this);
        appletMain.panelForImage.repaint();
        appletMain.panelForImage.revalidate();

        /*we will set the JViewPort in such a way that it's centre Point will
        again corrosponds to centreRow and centreColumn in Image*/

        /*calculate the new Co-ordinate for centreColumn and CurrentCentreRow*/
        int newCoordForColumn = (display_imageSizeX * centreColumn) / (ori_imageSizeX - 1);
        int newCoordForRow = (display_imageSizeY * centreRow) / (ori_imageSizeY - 1);

        /*find out new Location for JViewPort*/
        int newPositionX = newCoordForColumn - (this.scrVP.getWidth() / 2);
        int newPositionY = newCoordForRow - (this.scrVP.getHeight() / 2);

        /*its better to do it here: B/C putting it above creating unexpected Behaviour*/
        final int fX = newPositionX;
        final int fY = newPositionY;
        scrVP.setViewPosition(new Point(fX, fY));
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                scrVP.setViewPosition(new Point(fX, fY));
            }
        });
        blink.execute();
    }
    /******************************************************************************************/
    /*This action either shows the bin file or save bin file or
    do MQN search for average molecule*/
    String file;
    @Override
    public void actionPerformed(ActionEvent ae) {

        file = "/dbases/" + appletMain.dbName + "/bins/" + binFileX + "/" + binFileY;
        String cmd = ae.getActionCommand();

        /*save bin file*/
        if (cmd.equals("Save Bin")) {
            try {
                /*do this job in background*/
                String defaultNameForFileToSave = binFileX + "-" + binFileY + ".gz";
                fileSaveBox(file, defaultNameForFileToSave);
            } catch (IOException ex) {
                Logger.getLogger(imageHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        /******************************************************************************************/
        if (cmd.equals("Show Bin")) {

            new SwingWorker() {

                @Override
                protected Object doInBackground() throws Exception {

                    try {
                        if (appletMain.pgb != null) {
                            appletMain.pgb.setVisible(false);
                        }
                        appletMain.pgb.setVisible(true);
                        MolWindow wnd = new MolWindow();

                        if (atmCountAtBin > 60 || problemInMolAtBin) {
                            wnd.handlerForLargeMolecules(file);
                            wnd.setTitle(binFileX + "-" + binFileY);
                            wnd.setVisible(true);
                            lockMouseMovement();
                            drawMarkerOnBinOpening(xForBin, yForBin);
                        } else {
                            wnd.setMolsFile(file);
                            wnd.setTitle(binFileX + "-" + binFileY);
                            wnd.setVisible(true);
                            lockMouseMovement();
                            drawMarkerOnBinOpening(xForBin, yForBin);
                        }
                    } catch (Exception e) {
                        appletMain.pgb.setVisible(false);
                    }
                    return null;
                }

                public void done() {
                    appletMain.pgb.setVisible(false);
                }
            }.execute();
        }
        /******************************************************************************************/
        if (cmd.equals("HELP")) {
            connectToInternet hp = new connectToInternet("http://pubs.acs.org/doi/abs/10.1021/ci300513m?prevSearch=MQN%2Bmapplet&searchHistoryKey=");
        }

        /*Turn ON the Map Marker:*/
        try {
            JButton btn = (JButton) ae.getSource();
            if (btn.getName().equals("MARKER")) {
                if (!isLockOn)
                {
                this.mapMarkerIsON = true;}
            }
        } catch (Exception e) {
        }
        
        
        try {
        JButton btn = (JButton) ae.getSource();
        if (btn.getName().equals("removeMapMarkers")) {
        this.markerPositonX.clear();
        this.markerPositonY.clear();
        this.repaint();
        
            }
        } catch (Exception e) {
        }
    }
    /******************************************************************************************/

    /*This is swing worker thread: for blinking the pixels*/
    static ArrayList<Integer> markPixels = new ArrayList<Integer>();
    /*swing worker*/
    blickingThread blink;

    /*keep the record of swing worker created*/
    static ArrayList<blickingThread> swingWorkerList = new ArrayList<blickingThread>();

    class blickingThread extends SwingWorker {

        String setColor = "white";
        Color whiteClr = new Color(Color.WHITE.getRGB());
        Color redClr = new Color(Color.RED.getRGB());
        boolean control = false;

        @Override
        protected Object doInBackground() throws Exception {

            swingWorkerList.add(this);
            control = true;
            while (control) {

                if (markPixels.isEmpty()) {
                    return null;
                }

                if (setColor.contains("white")) {
                    markGivenPixcelV2(redClr);
                    setColor = "red";
                } else {
                    markGivenPixcelV2(whiteClr);
                    setColor = "white";
                }

                Thread.sleep(500);
            }
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    /******************************************************************************************/
    /*method to mark the pixel*/
    public void markGivenPixcelV2(Color clr) throws InterruptedException {

        int pixels[] = new int[imageHandler.ori_imageSizeX * imageHandler.ori_imageSizeY];
        PixelGrabber pg = new PixelGrabber(img, 0, 0, imageHandler.ori_imageSizeX, imageHandler.ori_imageSizeY, pixels, 0, imageHandler.ori_imageSizeX);
        pg.grabPixels();

        for (int i = 0; i
                < markPixels.size(); i++) {

            int pixceltoChange = markPixels.get(i);
            pixels[pixceltoChange] = clr.getRGB();
        }

        /*reconstruct the image*/
        img = createImage(new MemoryImageSource(imageHandler.ori_imageSizeX, imageHandler.ori_imageSizeY, pixels, 0, imageHandler.ori_imageSizeX));

        /*need to set up applet paramters*/
        appletMain.panelForImage.removeAll();

        /*repaint the image on this JPanel*/
        this.repaint();
        this.revalidate();

        /*add "this" JPanel(image) to applet "JPanel for Image"*/
        appletMain.panelForImage.setPreferredSize(new Dimension(display_imageSizeX, display_imageSizeY));
        appletMain.panelForImage.add(this);
        appletMain.panelForImage.repaint();
        appletMain.panelForImage.revalidate();


    }

    /******************************************************************************************/
    static void killedSwingWrker() {
        for (int i = 0; i
                < swingWorkerList.size(); i++) {
            swingWorkerList.get(i).control = false;
        }
    }

    /******************************************************************************************/
    /*this will display the file save box to user: basically it is 
    for saving the bin file from Host to the user computer*/
    void fileSaveBox(String binFileFromHost, String defaultFileNameTosave) throws IOException {

        JFrame j1 = new JFrame();
        j1.setSize(300, 300);

        FileDialog fd = new FileDialog(j1, "Save File", FileDialog.SAVE);
        fd.setFile(defaultFileNameTosave);
        fd.setVisible(true);

        String fileName = fd.getFile();
        String dirName = fd.getDirectory();

        String saveToFile = dirName + fileName;
        saveBinMols saveBinMols = new saveBinMols(binFileFromHost, saveToFile);
        saveBinMols.execute();
    }
    /******************************************************************************************/
    /*this method will store the bin file from host machine to
    specified file in user computer*/

    class saveBinMols extends SwingWorker {

        String binFileFromHost;
        String saveToFile;

        saveBinMols(String binFileFromHost, String saveToFile) {
            this.binFileFromHost = binFileFromHost;
            this.saveToFile = saveToFile;
        }

        @Override
        protected Object doInBackground() throws Exception {

            getBinFileFromServer gbfs = new getBinFileFromServer();
            gbfs.saveBinFile(binFileFromHost, saveToFile);
            return null;
        }
    }

    /******************************************************************************************/
    String setMoleculeOrMSG(int atmCount, Molecule m, String molCatagory) {

        if (atmCount <= 60 && atmCount > 0) {
            appletMain.jPanel3.removeAll();
            appletMain.jPanel3.add(appletMain.mViewPane);
            appletMain.jPanel3.repaint();
            appletMain.jPanel3.revalidate();
            appletMain.mViewPane.setM(0, m);
            problemInReadingMol = false;
            return null;
        }


        if (!(molCatagory == null) && !(molCatagory.isEmpty())) {
            appletMain.jPanel3.removeAll();
            appletMain.jPanel3.revalidate();
            JTextArea msg = new JTextArea("\n");
            msg.setFont(new Font("Serif", Font.BOLD, 12));
            msg.setForeground(Color.BLACK);
            msg.append("\n\n\n");
            msg.append("      MOLECULE IS TOO LARGE\n");
            msg.append("      TO DISPLAY\n\n");

            msg.append("      CATEGORY OF MOLECULE:\n");
            msg.append("      " + molCatagory);
            appletMain.jPanel3.add(msg);
            appletMain.jPanel3.repaint();
            return null;
        }

        if (atmCount > 60) {

            appletMain.jPanel3.removeAll();
            appletMain.jPanel3.revalidate();

            JTextArea msg = new JTextArea("\n");
            msg.setEditable(false);
            msg.setFont(new Font("Serif", Font.BOLD, 12));
            msg.setForeground(Color.BLACK);
            msg.append("\n\n\n");
            msg.append("      MOLECULE IS TOO LARGE\n");
            msg.append("      TO DISPLAY\n\n");

            appletMain.jPanel3.add(msg);
            appletMain.jPanel3.repaint();
            return null;
        }

        appletMain.jPanel3.removeAll();
        appletMain.jPanel3.add(appletMain.mViewPane);
        appletMain.jPanel3.repaint();
        appletMain.jPanel3.revalidate();
        appletMain.mViewPane.setM(0, m);
        return null;
    }

    /******************************************************************************************/

    /*draw the marker on the map with respect to moving location of mouse*/
    void drawMapMarker() {

        /*set the size of this JPanel*/
        this.setSize(display_imageSizeX, display_imageSizeY);

        /*remove the conent of "panelForImage"*/
        panelForImage.removeAll();

        /*repaint the panel of this class with image of required size:
        note repaint will call paint method*/
        this.repaint();
        this.revalidate();

        /*add "this" JPanel(with image) to applet "JPanel for Image"*/
        panelForImage.setPreferredSize(new Dimension(display_imageSizeX, display_imageSizeY));
        panelForImage.add(this);

        /*repaint the panelForImage*/
        panelForImage.repaint();
        panelForImage.revalidate();
    }

    /*get the position for marker: to be place on the center of pixel
    hard coded for the marker of size 32*32*/
    public static int[] getPositionForMarker(int pixelX, int pixelY, int currSizeX, int currSizeY, int oriSizeX, int oriSizeY) {

        int out[] = new int[2];


        int x1 = currSizeX * pixelX / oriSizeX;
        int y1 = currSizeY * pixelY / oriSizeY;

        int x2 = currSizeX * (pixelX + 1) / oriSizeX;
        int y2 = currSizeY * (pixelY + 1) / oriSizeY;

        /*calulate middle point*/
        int x3 = (int) ((x1 + x2) / (double) 2);
        int y3 = (int) ((y1 + y2) / (double) 2);

        out[0] = x3 - 16;
        out[1] = y3 - 31;

        return out;
    }  
    
    static boolean isLockOn=false;
    String lockMouseMovement()
    {
    
    appletMain.msgForLockingAndBinMarking.displayMsgWindow();    
    if (!msgForLockingAndBinMarking.lockingAndMarkingAllowed)
    {
    return null;
    }
            
    isLockOn=true;
    this.removeMouseMotionListener(this);
    appletMain.panelForImage.removeMouseMotionListener(this);
    appletMain.coOrdinateDisplay.setText(this.binFileX + "," + this.binFileY);
    appletMain.densityDisplay.setText("" + appletMain.dens[(ori_imageSizeY-1)-this.binFileY][this.binFileX]);
    atmCount = 0;
    molCatagory = appletMain.avgmolInfo[(ori_imageSizeY-1)-this.binFileY][this.binFileX];

    try {
            mol = new MolHandler(appletMain.avgmols[(ori_imageSizeY-1)-this.binFileY][this.binFileX]).getMolecule();
            atmCount = mol.getAtomCount();
            this.setMoleculeOrMSG(atmCount, mol, molCatagory);

        } catch (MolFormatException ex) {

            /*some problem with reading molecule??*/
            appletMain.jPanel3.removeAll();
            appletMain.jPanel3.revalidate();
            JTextArea msg = new JTextArea("\n");
            msg.setEditable(false);
            msg.setFont(new Font("Serif", Font.BOLD, 12));
            msg.setForeground(Color.BLACK);

            if (molCatagory == null || molCatagory.isEmpty()) {
                msg.append("\n\n\n");
                msg.append("         COULD NOT DISPLAY\n");
                msg.append("         MOLECULE\n\n");
                appletMain.jPanel3.add(msg);
                appletMain.jPanel3.repaint();
                problemInReadingMol = true;
            } else {
                msg.append("\n\n\n");
                msg.append("      COULD NOT DISPLAY\n");
                msg.append("      MOLECULE\n\n");

                msg.append("      CATEGORY OF MOLECULE:\n");
                msg.append("      " + molCatagory);
                appletMain.jPanel3.add(msg);
                appletMain.jPanel3.repaint();
                problemInReadingMol = true;
            }
    }
    
    appletMain.displayAverage.setText("" + appletMain.avg[(ori_imageSizeY-1)-this.binFileY][this.binFileX]);
    appletMain.displayStdev.setText("" + appletMain.stdev[(ori_imageSizeY-1)-this.binFileY][this.binFileX]);
    return null;
}
    
    String unLoackMouseMovement()
    {   
    isLockOn=false;
    this.addMouseMotionListener(this);
    return null;
    }
    
    String drawMarkerOnBinOpening(int x, int y)
    {
    
        appletMain.msgForLockingAndBinMarking.displayMsgWindow();
    if (!msgForLockingAndBinMarking.lockingAndMarkingAllowed)
    {
    return null;
    }
            /*this is mouse co-ordinate*/
            this.mapMarkerCoordX = x;
            this.mapMarkerCoordY = y;
            drawMapMarker();

            /*find "absolute" x and y positions on MAP: you need to remember this
            positions*/
            int x1 = imageHandler.ori_imageSizeX * x / display_imageSizeX;
            int y1 = imageHandler.ori_imageSizeY * y / display_imageSizeY;

            this.markerPositonX.add(x1);
            this.markerPositonY.add(y1);
            return null;
    }
}
