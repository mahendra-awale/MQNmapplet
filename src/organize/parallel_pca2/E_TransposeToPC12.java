package organize.parallel_pca2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import organize.tools.SMIReadWriter;

/**
 * Transposes molecules to new coordinates by using eWeV file form before.
 * Simultaneously also calculates min/max of coordinates and writes these out in a .minmax file
 * which is merged in the next step
 * Created 09-Nov-2009
 * @author lori
 */
public class E_TransposeToPC12 {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        SMIReadWriter smio = new SMIReadWriter(
                "usage: -i meancentralized[.gz] -o transposed[.gz] -e ewev.dat -m minmax.dat\n"
                + "-i mean centralized input file with smi + centralized mqns + csaproperties\n"
                + "-o output file with smi + transposed PC1;PC2 + csaproperties\n"
                + "-e input file with sorted eigenvalues + eigenvectors(;)\n"
                + "-m output file with min&max of PC1&2", args);

        //Check arguments immediately
        System.out.println("IN: " + smio.getArg("-i") + " OUT: " + smio.getArg("-o")
                + " EWEV: " + smio.getArg("-e") + " MINMAX: " + smio.getArg("-m"));

        /*Read 1st Three Eigen Vectors */
        double[][] loadings = new double[3][A_CalcMqnAndProps.fplength];
        BufferedReader br = new BufferedReader(new FileReader(smio.getArg("-e")));
        for (int i = 0; i < loadings.length; i++) {
            String[] rarr = br.readLine().split(" ");
            System.err.println("Eigenwert " + i + ": " + rarr[0]);
            String[] larr = rarr[1].split(";");
            for (int j = 0; j < loadings[i].length; j++) {
                loadings[i][j] = Double.parseDouble(larr[j]);
            }
        }
        br.close();

        /*Transpose to 1st three Pcs: Note:*/

        /*1st two PCs from file *.eWeV will be used to tranposed the molecule and to
         *produced the two dimentional map: 3rd PC will be used for sorting of molecules
         *in bins..
         *
         *You can decide by yourself which PCs you want to used for MAP production and
         *which for sorting bin molecules.
         *
         *As you know *.eWeV file contains the PCs (Eigen Vectors) from PC1...PCn
         *so if you want to used the PC1-PC2 for MAP production and PC3 for sorting molecules
         *in bin...You do not need to change the *.eWeV..programm will take the 1st
         *three eigen vectors and do the things for you..
         *
         *But If you want to used different PCs for MAP you can do so by:
         *changing the order of 1st three PCs in *.eWeV as you want:
         *
         *E.g if you want to put the PC2-3 for MAP production: then what you will do
         *    move the PC2 to Line 1 in File
         *    move the PC3 to Line 2 in File
         *    move the PC1 to Line 3 in File
         *
         *So in this case your map will be produced according to PC2-PC3 and
         *your bin molecules will be sorted by PC1
         *
         *IMPT: as you know re-ordering of PCs in file changes the position
         *of PC1,PC2,PC3...
         *
         *But for clearity here i always used the name PC1 for the 1st eigen Vector
         *from *.eWeV file and PC2 will be 2nd eigen vector from *.eWeV file and
         *3rd line as 3rd eigen vector...IT will not make any difference!!!
         *
         */

        System.out.println("READING IN, TRANSPOSING TO 1st THREE PCs (ONLY) AND WRITING OUT");
        String s;
        double[] mmpc = new double[4];
        mmpc[0] = Double.MAX_VALUE;
        mmpc[1] = Double.MIN_VALUE;
        mmpc[2] = Double.MAX_VALUE;
        mmpc[3] = Double.MIN_VALUE;
        while ((s = smio.readLine()) != null) {
            smio.displayReadCounter(100000);
            String[] sarr = s.split(" ");
            String[] pcarr = sarr[1].split(";");
            double origvals[] = new double[A_CalcMqnAndProps.fplength];
            for (int i = 0; i < pcarr.length; i++) {
                origvals[i] = Double.parseDouble(pcarr[i]);
            }
            double[] newPC = new double[3];
            for (int i = 0; i < newPC.length; i++) {
                for (int j = 0; j < origvals.length; j++) {
                    newPC[i] += (origvals[j] * loadings[i][j]);
                }
            }
            //Do the rotation
            double[] rotatepoint = rotatepoint(newPC[0], newPC[1], 45.0);
            newPC[0]=rotatepoint[0];
            newPC[1]=rotatepoint[1];            
///////////////////////////////////////////////////////////////////////////////////////////////////
            //0=minPC1 1=maxPC1 2=maxPC2 3=maxPC2
            if (mmpc[0] > newPC[0]) {
                mmpc[0] = newPC[0];
            }
            if (mmpc[1] < newPC[0]) {
                mmpc[1] = newPC[0];
            }
            if (mmpc[2] > newPC[1]) {
                mmpc[2] = newPC[1];
            }
            if (mmpc[3] < newPC[1]) {
                mmpc[3] = newPC[1];
            }
            
            //write smi+tag newpc1;newpc2 map-values mqns. pleas note the switch of sequence
            //sarr[2] then sarr[1] because sarr[2] is used in "G" and sarr[1] then only in "I"
            //kind of stupid I think now, but now its also too late...
            smio.writeLine(sarr[0] + " " + newPC[0] + ";" + newPC[1] + " " + sarr[2] + " " + sarr[1] + " "+ newPC[2]);
        }
        smio.end();

        System.out.println("WRITING MINMAX");
        FileWriter fw = new FileWriter(smio.getArg("-m"));
        fw.write(mmpc[0] + ";" + mmpc[1] + " " + mmpc[2] + ";" + mmpc[3] + "\n");
        fw.close();
        System.out.println("END");
    }
    
    /**
     * Method rotates a point in a two dimensional plane.
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
            double newx = radius * Math.cos((newalpha/180)*Math.PI);
            double newy = radius * Math.sin((newalpha/180)*Math.PI);
            newcoor = new double[]{newx,newy};
        } else {
            System.out.println("CAN NOT MAKE COORD");
            /* program cannot make new coordinates */
            return new double[]{coor1, coor2};
        }
        return newcoor;
    }
}
