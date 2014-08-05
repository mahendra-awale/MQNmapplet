package organize.parallel_pca2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

/**
 * Creates partial ASF maps for input (transposed.gz) files using the map-info column.
 * Requires the totalminmax file to hash correctly into NxN map, creates then overlayable ASF maps.
 *
 * BIG FAT WARNING: IN HERE IS SOME HARD CODING INFLUENCING A_CalcMQNandPROPS!!! it's names[]!!!
 * Created 09-Nov-2009
 * @author lori
 */
public class G_CreatePartMaps2 {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        if (args.length == 0) {
            System.out.println("arg0=basename arg1=totalminmax.dat arg2=size arg3=transposed[.gz]\n" +
                    "arg=0 basename for the map files (.hac.asf, .mass.asf, etc...)\n" +
                    "arg=1 input file with overall min&max of PC1&2\n" +
                    "arg=2 size for (quadratic) maps\n" +
                    "arg=3 input files with smi + transposed PC1;PC2 + csaproperties");
            System.exit(0);
        }

        System.out.println("BASE: " + args[0] + " TOTALMINMAX: " + args[1] + " SIZE: " + args[2]);
        String base = args[0];

        //skewed comes from old version where PC2 axis was streched - now it is not done anymore
        System.out.println("READING TOTAL MINMAX - NONSKEWED");
        BufferedReader br = new BufferedReader(new FileReader(args[1]));
        String[] sarr = br.readLine().split(";| ");
        br.close();
        double pcMin = Double.parseDouble(sarr[0]) < Double.parseDouble(sarr[2]) ?
            Double.parseDouble(sarr[0]) : Double.parseDouble(sarr[2]);
        //UH-OH nonskewed bug, < xchg by >
        double pcMax = Double.parseDouble(sarr[1]) > Double.parseDouble(sarr[3]) ?
            Double.parseDouble(sarr[1]) : Double.parseDouble(sarr[3]);


        System.out.println("DETERMINING SIZE AND ZERO POINT");
        int size = Integer.parseInt(args[2]);
        int arrmax = size - 1; //arrays only go to size-1, so define that here
        int zerox = (int) (Math.floor((0 - pcMin) * arrmax / (pcMax - pcMin)));
        int zeroy = (int) (Math.floor((0 - pcMin) * arrmax / (pcMax - pcMin)));
        System.out.println("(0/0) is on neuron (" + zerox + "/" + zeroy + ")");

        System.out.println("READING IN " + args[3] + " CREATING MAPS");
        //CHANGEME
        String[] names = {"CA-Ratio","RA-Ratio","CDB-Ratio","PC1"};

        double[][][] sums = new double[names.length][size][size];
        double[][][] sumsqs = new double[names.length][size][size];
        long[][][] freq = new long[names.length][size][size];

        BufferedReader brgz = null;
        if (args[3].endsWith(".gz")) {
            brgz = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(args[3]))));
        } else {
            brgz = new BufferedReader(new FileReader(args[3]));
        }
        String s;
        int cnt = 0;
        while ((s = brgz.readLine()) != null) {
            if (++cnt % 100000 == 0) {
                System.out.println("Line " + cnt);
            }

            String[] starr = s.split(" ");
            String[] xyarr = starr[1].split(";");
            //WARNING: THE OLD VERSION OF THE VIEWER DID SWAP THE Y COORDINATES BECAUSE
            //OF PROGRAMMING BUG. BE CAREFUL TAKING THE NEW VIEWER
            //x=int(($2-minA)*400/(maxA-minA))     y=int(($3-minB)*400/(maxB-minB))
            int x = (int) (Math.floor((Double.parseDouble(xyarr[0]) -pcMin) * arrmax / (pcMax - pcMin)));
            int y = (int) (Math.floor((Double.parseDouble(xyarr[1]) - pcMin) * arrmax / (pcMax - pcMin)));
            String[] proparr = starr[2].split(";");
            for (int j = 0; j < sums.length; j++) {
                double propval = Double.parseDouble(proparr[j]);
                if(x==1000||y==1000) {
                    System.out.println("MAXX "+s);
                }
                sums[j][x][y] += propval;
                sumsqs[j][x][y] += propval * propval;
                freq[j][x][y]++;
            }
        }
        brgz.close();

        System.out.println("WRITING MAPS");
        for (int i = 0; i < sums.length; i++) {
            System.out.println("WRITING MAP " + names[i]);
            BufferedWriter bw = new BufferedWriter(new FileWriter(base + "." + names[i] + ".partmap"));
            //simplest way: for(i)for(j) => sequence x0y0 x0y1 x0y2 \newline x1y0 x1y1 etc...
            //BEWARE OF THAT WHEN CREATING THE PNG PICTURES with convertASFtoMAP!!!
            for (int j = 0; j < sums[i].length; j++) {
                for (int k = 0; k < sums[i][j].length; k++) {
                    bw.write(sums[i][j][k] + ";" + sumsqs[i][j][k] + ";" + freq[i][j][k] + "\t");
                }
                bw.write("\n");
            }
            bw.close();
        }
        System.out.println("END");
    }
}
