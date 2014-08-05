package organize.parallel_pca2;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * Very inefficent code to create Molecules-Per-Pixel files, plus an average molecule file
 * for mapplet2 side pane. You see, the newer the code the worse...
 * Created 12-Sep-2011
 * @author lori
 */
public class I_CreateBins {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Creates files in outfolder with molecules per bin for this row\n"
                    + "plus additional 'avg' file with average molecules\n"
                    + "arg0=base/out/folder arg1=totminmaxfile arg2=TotColumns arg3=TotRows arg4=fromrow arg5=torow arg6,7=transposed.gz");
        }
        System.out.println("BINNING FROM " + args[4] + " TO " + args[5] + " OF " + args[3] + " WRT TO " + args[0]);

        //first read this to put molecules into right bins
        System.out.println("READING TOTAL MINMAX");
        BufferedReader br = new BufferedReader(new FileReader(args[1]));
        String[] sarr = br.readLine().split(";| ");
        br.close();
        double pcMin = Double.parseDouble(sarr[0]) < Double.parseDouble(sarr[2])
                ? Double.parseDouble(sarr[0]) : Double.parseDouble(sarr[2]);
        double pcMax = Double.parseDouble(sarr[1]) > Double.parseDouble(sarr[3])
                ? Double.parseDouble(sarr[1]) : Double.parseDouble(sarr[3]);

        //parse arguments
        int totColumns = Integer.parseInt(args[2]);
        int totRows = Integer.parseInt(args[3]);
        int fromrow = Integer.parseInt(args[4]);
        int torow = Integer.parseInt(args[5]);

        //maximum column is total - 1 (999 instead of 1000 because we start at 0)
        int colMax = totColumns - 1;
        int rowMax = totRows - 1;
        //array to store the sums of the mqns per pixel on pixel (row/col)
        double[][][] sums = new double[totRows][totColumns][42];
        //store number of cpds per pixel on row ROW
        int[][] freq = new int[totRows][totColumns];

        //this run is only to find out which are the average mqns for each pixel.
        System.out.println("FINDING OUT AVG MQNS PER PIXEL");
        for (int i = 6; i < args.length; i++) {
            System.out.println("AFILE " + args[i]);
            BufferedReader brgz;
            if (args[i].endsWith(".gz")) {
                brgz = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(args[i]))));
            } else {
                brgz = new BufferedReader(new FileReader(args[i]));
            }
            String s;
            int cnt = 0;
            while ((s = brgz.readLine()) != null) {
                if (++cnt % 1000000 == 0) {
                    System.out.println("Line " + cnt);
                }

                String[] starr = s.split(" ");
                String[] xyarr = starr[1].split(";");
                int x = (int) (Math.floor((Double.parseDouble(xyarr[0]) - pcMin) * rowMax / (pcMax - pcMin)));
                if (x < fromrow || x > torow) {

                    continue;
                }

                int y = (int) (Math.floor((Double.parseDouble(xyarr[1]) - pcMin) * colMax / (pcMax - pcMin)));

                //new coors are to move picture origin from top to bottom left (mirror horiz)
                int newX = x;
                int newY = rowMax - y;

                String[] mqnarr = starr[3].split(";");
                for (int j = 0; j < sums[newX][newY].length; j++) {
                    double propval = Double.parseDouble(mqnarr[j]);
                    sums[newX][newY][j] += propval;
//                    freq[newX][newY]++;
                }
                 freq[newX][newY]++;
            }
            brgz.close();
        }

        //calculate average mqns for each pixel. The average mols are the ones closest to this
        //average mqns
        double[][][] avgs = new double[totRows][totColumns][42];
        double[][] avgMolDists = new double[totRows][totColumns];
        String[][] avgMols = new String[totRows][totColumns];
        for (int i = 0; i < totRows; i++) {
            for (int j = 0; j < totColumns; j++) {
                for (int k = 0; k < 42; k++) {
                    avgs[i][j][k] = sums[i][j][k] / freq[i][j];
                }
                avgMolDists[i][j] = Double.MAX_VALUE;
            }
        }

        //second go through all files (very stupid not), this time putting the molecules into
        //files and taking the closest to avg molecules as avgmols
        System.out.println("CREATING BINS FOR ROW & FINDING AVG MOLS");
        for (int i = 6; i < args.length; i++) {
            System.out.println("MFILE " + args[i]);
            BufferedReader brgz;
            if (args[i].endsWith(".gz")) {
                brgz = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(args[i]))));
            } else {
                brgz = new BufferedReader(new FileReader(args[i]));
            }
            String s;
            int cnt = 0;
            while ((s = brgz.readLine()) != null) {
                if (++cnt % 1000000 == 0) {
                    System.out.println("Line " + cnt);
                }

                String[] starr = s.split(" ");
                String[] xyarr = starr[1].split(";");
                int x = (int) (Math.floor((Double.parseDouble(xyarr[0]) - pcMin) * rowMax / (pcMax - pcMin)));
                if (x < fromrow || x > torow) {
                    continue;
                }

                int y = (int) (Math.floor((Double.parseDouble(xyarr[1]) - pcMin) * colMax / (pcMax - pcMin)));
                //new coors are to move picture origin from top to bottom left (mirror horiz)

                int newX = x;
                int newY = rowMax - y;

                //here is the actual moving: open file / attach mol to file
                File f = new File(args[0] + "/" + newX + "/" + newY);
                if (!f.getParentFile().exists()) {
                    f.getParentFile().mkdirs();
                }

                FileWriter fw = new FileWriter(f, true);
                fw.write(starr[0].replace(';', ' ') + " " + starr[(starr.length - 1)] + "\n");
                fw.close();

                //calculate kind of mqn distance, if closer than so far then update
                String[] mqnarr = starr[3].split(";");
                double disttoc = 0;
                for (int j = 0; j < sums[newX][newY].length; j++) {
                    double propval = Double.parseDouble(mqnarr[j]);
                    disttoc += Math.abs(avgs[newX][newY][j] - propval);
                }
                if (disttoc < avgMolDists[newX][newY]) {

                    //System.out.println("UPDATING " + y + " DIST " + disttoc + " SMI " + starr[0]);
                    avgMolDists[newX][newY] = disttoc;
                    avgMols[newX][newY] = starr[0] + ";" + starr[(starr.length - 1)];
                }
            }
            brgz.close();
        }

        //finally write out these avg mols, if none just print an empty line
        System.out.println("WRITING OUT AVG MOLS TO avg");
        for (int x = fromrow; x <= torow; x++) {
            File f = new File(args[0] + "/" + x + "/avg");
            if (!f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }
            FileWriter fw = new FileWriter(f);
            for (int y = 0; y < avgMols.length; y++) {
                if (avgMols[x][y] != null) {
                    fw.write(avgMols[x][y].replace(';', ' ') + "\n");
                } else {
                    fw.write("\n");
                }
            }
            fw.close();
        }

        //done finally done yess!!!
        System.out.println("END");
    }
}