package organize.parallel_pca2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Merges partial ASF maps to final ASF main map (overlaying of different maps basically)
 * Created 16-Nov-2009
 * @author lori
 */
public class H_MapMerger {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        if (args.length == 0) {
            System.out.println("arg0=outname arg1=size arg2/3...=PartMaps\n" +
                    "arg=0 output file name (final merged ASF map readable with CSAview)\n" +
                    "arg=1 size" +
                    "arg=2/3... input files (partmaps)");
            System.exit(0);
        }

        //get size from first partmap
        System.out.println("SIZE "+args[1]);
        int size = Integer.parseInt(args[1]);
        
        double sums[][] = new double[size][size];
        double sumsqs[][] = new double[size][size];
        long freq[][] = new long[size][size];
        System.out.println("READING MAPS");
        for (int i = 2; i < args.length; i++) {
            System.out.println("READING FILE " + args[i]);
            BufferedReader br = new BufferedReader(new FileReader(args[i]));
            //for all x
            for (int j = 0; j < sums.length; j++) {
                String[] arr = br.readLine().split("\t");
                //for all y
                for (int k = 0; k < arr.length; k++) {
                    String[] sqf = arr[k].split(";");
                    sums[j][k] += Double.parseDouble(sqf[0]);
                    sumsqs[j][k] += Double.parseDouble(sqf[1]);
                    freq[j][k] += Long.parseLong(sqf[2]);
                }
            }
            br.close();
        }

        System.out.println("WRITING MAP " + args[0]);
        BufferedWriter bw = new BufferedWriter(new FileWriter(args[0]));
        bw.write("#Type: ASF map\n" +
                "#Size:\t" + size + "\t" + size + "\n" +
                "#Meaning:\tAverage\tStdev\tFrequency\n" +
                "#Map (leftToRight=y topToBottom=x)\n");
        //simplest way: for(i)for(j) => sequence x0y0 x0y1 x0y2 \newline x1y0 x1y1 etc...
        //BEWARE OF THAT WHEN CREATING THE PNG PICTURES with convertASFtoMAP!!!
        for (int j = 0; j < sums.length; j++) {
            for (int k = 0; k < sums[j].length; k++) {
                long num = freq[j][k];
                double avg = sums[j][k] * 1.0 / num;
                double stdev = Math.sqrt((1.0 * sumsqs[j][k] - (1.0 * sums[j][k] * sums[j][k] / num)) / (num - 1.0));
                bw.write(avg + ";" + stdev + ";" + num + "\t");
            }
            bw.write("\n");
        }
        bw.close();
        System.out.println("END");

    }
}
