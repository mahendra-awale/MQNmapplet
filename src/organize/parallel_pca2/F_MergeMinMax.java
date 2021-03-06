package organize.parallel_pca2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Merges various min/max files into global min/max. These values help then to hash/bin these
 * floating coordinates into NxN maps (1000x1000 or so)
 * Created 10-Nov-2009
 * @author lori
 */
public class F_MergeMinMax {
 public static void main(String[] args) throws FileNotFoundException, IOException {
        if(args.length==0) {
            System.out.println("arg0=totalminmax.dat arg1/2/3...=minmax.dat\n" +
                    "arg=0 output file with overall min&max of PC1&2\n" +
                    "arg>0 input files with min&max of PC1&2");
            System.exit(0);
        }

        System.out.println("READING MINMAX FILES");
        double[] mmpc = new double[4];
        mmpc[0] = Double.MAX_VALUE;
        mmpc[1] = Double.MIN_VALUE;
        mmpc[2] = Double.MAX_VALUE;
        mmpc[3] = Double.MIN_VALUE;
        for (int i = 1; i < args.length; i++) {
            System.out.println("READING FILE "+args[i]);
            BufferedReader br = new BufferedReader(new FileReader(args[i]));
            String[] sarr = br.readLine().split(";| ");
            br.close();
            double[] mmarr = new double[4];
            for (int j = 0; j < mmarr.length; j++) {
                mmarr[j] = Double.parseDouble(sarr[j]);
            }
            if(mmpc[0]>mmarr[0])
                mmpc[0]=mmarr[0];
            if(mmpc[1]<mmarr[1])
                mmpc[1]=mmarr[1];
            if(mmpc[2]>mmarr[2])
                mmpc[2]=mmarr[2];
            if(mmpc[3]<mmarr[3])
                mmpc[3]=mmarr[3];
        }

        System.out.println("WRITING TOTAL MINMAX");
        FileWriter fw = new FileWriter(args[0]);
        fw.write(mmpc[0] + ";" + mmpc[1] + " " + mmpc[2] + ";" + mmpc[3] + "\n");
        fw.close();
        System.out.println("END");
    }
}
