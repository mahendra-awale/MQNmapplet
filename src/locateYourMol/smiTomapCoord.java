/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package locateYourMol;

import MAPPLET.logWindow;
import chemaxon.formats.MolFormatException;
import chemaxon.struc.Molecule;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This class will take the "SMILE" of query molecule and do MQN, PCA
 * calculation and find out its CO-ordinates on map! class is pretty much simple
 * to understand:
 *
 *
 * This class will be called from "drawMolandSubmit.java" class.
 *
 * @author mahendra
 */
public class smiTomapCoord {

    /*
     * this is the name of database
     */
    String db;

    /*
     * this is map which is user currently playing with
     */
    String map;

    /*
     * size of the map
     */
    int mapSize;

    /*
     * this is the smile from the user:user draw the structure in JChem and
     * submit
     */
    String smi;

    /*
     * Molecule
     */
    Molecule mol;

    /*
     * this is mqn fingerptint
     */
    String mqn_FP;

    /*
     * mqn fingerpting as array
     */
    double mqnMC_FParray[] = new double[42];

    /*
     * avgs which we used for our training dataset(it is dataset which) was used
     * to produce maps
     */
    double[] avgs;

    /*
     * this is the two eigen vectors we got earlier from this databse: note that
     * two eigen vectors can be PC1-PC2 or PC2-PC3 pr PC1-PC3: its depend which
     * two eigen vectors you used while generation of map
     */
    double[][] eigenV_loadings = new double[2][42];

    /*
     * this is two PCs for new molecule enter by user
     */
    double[] newPC = new double[2];

    /*
     * this is overall min max in two PCs this database
     */
    double pcMin;
    double pcMax;

    /*
     * this is the co-ordinate for user molecule to locate on this map
     */
    int mapCoordX;
    int mapCoordY;


    /*
     * constructor of the class
     */
    smiTomapCoord(String smi, String db, String map, int mapSize) {
        this.smi = smi;
        this.db = db;
        this.map = map;
        this.mapSize = mapSize;
    }

    /*
     * get the map co-ordinate for molecule
     */
    public int[] getCoordForYourMol() throws MolFormatException, FileNotFoundException, IOException {

        if (this.smi.isEmpty()) {
            logWindow.updateLog("\nNo Molecule Found: Please Draw Molecule!\n");
            return null;
        }

        try {
            calculateMQNonServer cmos = new calculateMQNonServer();
            mqn_FP = cmos.calculateMQN(smi);

            if (mqn_FP.contains("ERROR")) {
                logWindow.updateLog("\nProbelm in MQN Calculation!\n");
                return null;
            }

        } catch (Exception e) {
            logWindow.updateLog("\nProbelm in MQN Calculation!\n");
            return null;
        }

        try {
            this.readAvgFile();
        } catch (Exception e) {
            logWindow.updateLog("\nProbelm in Average File!\n");
            return null;
        }

        try {
            this.meanCentralization();
        } catch (Exception e) {
            logWindow.updateLog("\nProbelm during Mean Centralization!\n");
            return null;
        }

        try {
            this.readEigenVectors();
        } catch (Exception e) {
            logWindow.updateLog("\nProbelm in reading Eigen Vectors!\n");
            return null;
        }

        /*
         * Pubchem Extended and Pubchem.60 requires special correction
         */
        if (db.equals("Pubchem.60")) {

            try {
                this.calcPC2PC3forPubchem60();
            } catch (Exception e) {
                logWindow.updateLog("\nProbelm in calculating PCs!\n");
                return null;
            }

        } else if (db.equals("ZINC")) {
            try {
                this.calPC1PC2v2();
            } catch (Exception e) {
                logWindow.updateLog("\nProbelm in calculating PCs!\n");
                return null;
            }
        } else {

            try {
                this.calPC1PC2();
            } catch (Exception e) {
                logWindow.updateLog("\nProbelm in calculating PCs!\n");
                return null;
            }


        }
        /**
         * ********************************************************************
         */
        try {
            this.readTotalminMax();
        } catch (Exception e) {
            logWindow.updateLog("\nProbelm in total min max file!\n");
            return null;
        }

        this.getMapCoordinates();

        int molCoord[] = new int[2];
        molCoord[0] = this.mapCoordX;
        molCoord[1] = this.mapCoordY;

        /*
         * if co-ordinate is out of map: just return null
         */
        if (molCoord[0] > this.mapSize || molCoord[1] > this.mapSize) {
            logWindow.updateLog("\nYour Molecule is Out of Map!\n");
            return null;
        }


        /*
         * if co-ordinate is negative(specially happen with Pubchem and Category
         * : just return null
         */
        if (molCoord[0] < 0 || molCoord[1] < 0) {
            logWindow.updateLog("\nYour Molecule is Out of Map!\n");
            return null;
        }

        return molCoord;
    }

    /**
     * ************************************************************
     */
    /*
     * step 1: MQN calculation done using calculteMQNonServer class
     */
    /**
     * ************************************************************
     */
    /*
     * step 2: read in the avergae file
     */
    void readAvgFile() throws FileNotFoundException, IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/dbases/" + db + "/misc/" + db + ".avgs")));
        String[] avgarr = br.readLine().split(";");
        avgs = new double[42];
        for (int i = 0; i < avgs.length; i++) {
            avgs[i] = Double.parseDouble(avgarr[i]);
        }
        br.close();

    }

    /**
     * ************************************************************
     */
    /*
     * step 3: mean centralization of query MQN data
     */
    void meanCentralization() {
        String[] mqns = this.mqn_FP.split(";");
        double[] mqnd = new double[42];

        for (int i = 0; i < mqns.length; i++) {
            mqnd[i] = Double.parseDouble(mqns[i]);
            mqnMC_FParray[i] = mqnd[i] - avgs[i];
        }
    }

    /**
     * ************************************************************
     */
    /*
     * step 4: read in the eigen vectors
     */
    void readEigenVectors() throws FileNotFoundException, IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/dbases/" + db + "/misc/" + db + ".eWeV")));

        for (int i = 0; i < eigenV_loadings.length; i++) {
            String[] rarr = br.readLine().split(" ");
            String[] larr = rarr[1].split(";");
            for (int j = 0; j < eigenV_loadings[i].length; j++) {
                eigenV_loadings[i][j] = Double.parseDouble(larr[j]);
            }
        }
        br.close();
    }

    /**
     * ************************************************************
     */
    /*
     * step 5a: Find new PCs for molecule::
     */
    void calPC1PC2() {

        for (int i = 0; i < newPC.length; i++) {
            for (int j = 0; j < mqnMC_FParray.length; j++) {
                newPC[i] += (mqnMC_FParray[j] * this.eigenV_loadings[i][j]);
            }
        }
    }

    /**
     * ************************************************************
     */
    void calcPC2PC3forPubchemExtended() {

        for (int i = 0; i < newPC.length; i++) {
            for (int j = 0; j < mqnMC_FParray.length; j++) {
                newPC[i] += (mqnMC_FParray[j] * this.eigenV_loadings[i][j]);
            }
        }

        double PC1 = newPC[0];
        double PC2 = newPC[1];
        double[] deltaPC = new double[2];

        /*
         * 5 times correction Points
         */
        double[][] correctionPoints = new double[5][2];
        correctionPoints[0][0] = -0.6676918546931182;
        correctionPoints[0][1] = -0.5461361125287867;
        correctionPoints[1][0] = 0.5759941126772117;
        correctionPoints[1][1] = -0.05754432136130604;
        correctionPoints[2][0] = -0.0013821016771921575;
        correctionPoints[2][1] = 9.192851813587637E-4;
        correctionPoints[3][0] = 7.185747534504654E-5;
        correctionPoints[3][1] = -1.1499151863264449E-4;
        correctionPoints[4][0] = 0.0;
        correctionPoints[4][1] = 0.0;

        /*
         * start iterative correction
         */
        for (int i = 0; i < 5; i++) {

            /*
             * calculate delta of PCs w.r.t to correction PCs
             */
            deltaPC[0] = PC1 - correctionPoints[i][0];
            deltaPC[1] = PC2 - correctionPoints[i][1];

            /*
             * calculate radial distance
             */
            double distance = Math.sqrt((deltaPC[0] * deltaPC[0]) + (deltaPC[1] * deltaPC[1]));

            /*
             * sqrt correction for distance
             */
            double distancePrime = Math.pow((double) (distance + 1), (double) 1 / 4) - 1;

            /*
             * get the dPrime/d
             */
            double factor = distancePrime / distance;

            /*
             * calculate the new PCs
             */
            double[] nPC = new double[3];
            nPC[0] = deltaPC[0] * factor;
            nPC[1] = deltaPC[1] * factor;

            if (String.valueOf(factor).contains("NaN")) {
                nPC[0] = 0.0;
                nPC[1] = 0.0;
            }

            if (nPC[0] == -0.0) {
                nPC[0] = 0.0;
            }

            if (nPC[1] == -0.0) {
                nPC[1] = 0.0;
            }

            PC1 = nPC[0];
            PC2 = nPC[1];
        }
        newPC[0] = PC1;
        newPC[1] = PC2;
    }

    /**
     * This calculation of PCs will drive you crazy. But No option i have to do
     * it! ******************************************************************
     */
    void calcPC2PC3forPubchem60() {

        for (int i = 0; i < newPC.length; i++) {
            for (int j = 0; j < mqnMC_FParray.length; j++) {
                newPC[i] += (mqnMC_FParray[j] * this.eigenV_loadings[i][j]);
            }
        }

        double PC1 = newPC[0];
        double PC2 = newPC[1];

        double oriMinPC1 = -39.96140811431469;
        double oriMaxPC1 = 66.02572938657983;
        double originalDiff_PC1 = oriMaxPC1 - oriMinPC1;

        double oriMinPC2 = -74.8601199307172;
        double oriMaxPC2 = 63.2062185254597;
        double originalDiff_PC2 = oriMaxPC2 - oriMinPC2;

        double scaleMin = -125;
        double scaleMax = 125;
        double scaleDiff = scaleMax - scaleMin;

        double d1 = PC1;
        double d2 = PC2;

        /**
         * ***************1st do the scaling for PC1****************
         */
        double perct1 = (d1 - oriMinPC1);
        if (perct1 != 0) {
            perct1 = (perct1 / originalDiff_PC1) * 100;
        } else {
            perct1 = 0.0;
        }

        double t1 = scaleDiff * perct1;
        if (t1 != 0) {
            t1 = t1 / 100;
        }

        PC1 = scaleMin + t1;

        /**
         * ***************2nd do the scaling for PC1****************
         */
        double perct2 = (d2 - oriMinPC2);
        if (perct2 != 0) {
            perct2 = (perct2 / originalDiff_PC2) * 100;
        } else {
            perct2 = 0.0;
        }

        double t2 = scaleDiff * perct2;
        if (t2 != 0) {
            t2 = t2 / 100;
        }

        PC2 = scaleMin + t2;

        /**
         * ***************Do the 2 times Radial Correction****************
         */
        double[][] correctionPoints = new double[5][2];
        double[] deltaPC = new double[2];
        correctionPoints[0][0] = 56.398435540922435;
        correctionPoints[0][1] = 4.576682916090419;
        correctionPoints[1][0] = -9.572655769644975;
        correctionPoints[1][1] = 0.22397661818376224;

        /*
         * start iterative correction
         */
        for (int i = 0; i < 2; i++) {
            deltaPC[0] = PC1 - correctionPoints[i][0];
            deltaPC[1] = PC2 - correctionPoints[i][1];

            /*
             * calculate radial distance
             */
            double distance = Math.sqrt((deltaPC[0] * deltaPC[0]) + (deltaPC[1] * deltaPC[1]));

            /*
             * sqrt correction for distance
             */
            double distancePrime = Math.pow((double) (distance + 1), (double) 1 / 2) - 1;

            /*
             * get the dPrime/d
             */
            double factor = distancePrime / distance;

            /*
             * calculate the new PCs
             */
            double[] nPC = new double[3];
            nPC[0] = deltaPC[0] * factor;
            nPC[1] = deltaPC[1] * factor;

            if (String.valueOf(factor).contains("NaN")) {
                nPC[0] = 0.0;
                nPC[1] = 0.0;
            }

            if (nPC[0] == -0.0) {
                nPC[0] = 0.0;
            }

            if (nPC[1] == -0.0) {
                nPC[1] = 0.0;
            }

            PC1 = nPC[0];
            PC2 = nPC[1];
        }
        /**
         * **************Now do the scaling Again*************************
         */
        oriMinPC1 = -0.9818585774156412;
        oriMaxPC1 = 3.070802226403504;
        originalDiff_PC1 = oriMaxPC1 - oriMinPC1;

        oriMinPC2 = -2.1750121547261236;
        oriMaxPC2 = 1.7649862858499141;
        originalDiff_PC2 = oriMaxPC2 - oriMinPC2;

        scaleMin = -3.070802226403504;
        scaleMax = 3.070802226403504;
        scaleDiff = scaleMax - scaleMin;

        d1 = PC1;
        d2 = PC2;

        /**
         * ***************1st do the scaling for PC1****************
         */
        perct1 = (d1 - oriMinPC1);
        if (perct1 != 0) {
            perct1 = (perct1 / originalDiff_PC1) * 100;
        } else {
            perct1 = 0.0;
        }

        t1 = scaleDiff * perct1;
        if (t1 != 0) {
            t1 = t1 / 100;
        }

        newPC[0] = scaleMin + t1;

        /**
         * ***************2nd do the scaling for PC1****************
         */
        perct2 = (d2 - oriMinPC2);
        if (perct2 != 0) {
            perct2 = (perct2 / originalDiff_PC2) * 100;
        } else {
            perct2 = 0.0;
        }

        t2 = scaleDiff * perct2;
        if (t2 != 0) {
            t2 = t2 / 100;
        }

        newPC[1] = scaleMin + t2;
    }

    /**
     * ************************************************************
     */
    /*
     * step 6: read total min max file and store global pcMin and pcMax
     */
    void readTotalminMax() throws FileNotFoundException, IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/dbases/" + db + "/misc/" + db + ".totalminmax")));
        String[] sarr = br.readLine().split(";| ");
        br.close();
        pcMin = Double.parseDouble(sarr[0]) < Double.parseDouble(sarr[2])
                ? Double.parseDouble(sarr[0]) : Double.parseDouble(sarr[2]);
        pcMax = Double.parseDouble(sarr[1]) > Double.parseDouble(sarr[3])
                ? Double.parseDouble(sarr[1]) : Double.parseDouble(sarr[3]);
    }

    /**
     * ************************************************************
     */
    /*
     * step 7: get the actual co-ordinates of point on map
     */
    void getMapCoordinates() {

        int size = (mapSize - 1);
        mapCoordX = (int) (Math.floor((newPC[0] - pcMin) * size / (pcMax - pcMin)));
        mapCoordY = (int) (Math.floor((newPC[1] - pcMin) * size / (pcMax - pcMin)));
    }

    /**
     * Method rotates a point in a two dimensional plane.
     *
     * @param coor1
     * @param coor2
     * @param dalpha
     * @return
     */
    static double[] rotatepoint(double coor1, double coor2, double dalpha) {
        double newcoor[] = new double[2];
        double radius = Math.sqrt((coor1 * coor1) + (coor2 * coor2));
        if (radius != 0) {
            double alpha = Math.acos(coor1 / radius);
            alpha /= Math.PI;
            alpha *= 180;
            if (coor2 < 0) {
                alpha = 360 - alpha;
            }
            double newalpha = alpha + dalpha;
            if (newalpha > 360) {
                newalpha -= 360;
            }
            double newx = radius * Math.cos((newalpha / 180) * Math.PI);
            double newy = radius * Math.sin((newalpha / 180) * Math.PI);
            newcoor = new double[]{newx, newy};
        } else {
            System.out.println("CAN NOT MAKE COORD");
            /*
             * program cannot make new coordinates
             */
            return new double[]{coor1, coor2};
        }
        return newcoor;
    }

    /**
     * ************************************************************
     */
    /*
     * step 5a: Find new PCs for molecule::
     */
    void calPC1PC2v2() {

        for (int i = 0; i < newPC.length; i++) {
            for (int j = 0; j < mqnMC_FParray.length; j++) {
                newPC[i] += (mqnMC_FParray[j] * this.eigenV_loadings[i][j]);
            }
        }

        double[] rotatepoint = rotatepoint(newPC[0], newPC[1], 45.0);
        newPC[0] = rotatepoint[0];
        newPC[1] = rotatepoint[1];
    }
}
