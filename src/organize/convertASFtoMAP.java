package organize;

import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.util.zip.GZIPOutputStream;
import javax.imageio.ImageIO;
import organize.tools.ArgReader;
import organize.tools.HSLColor;

/**
 * Converts ASF maps from PCA run to PNG picture and .as.gz file with avg/stdev values for mapplet2
 * The -m option allows to make nice gray/lightness in the final picture
 * The -c option works a bit different: Setting the minimum SHIFTS the hue circle so that
 * the minimum avg value yields in this color. The maximim STRETCHES the circle so that the
 * max avg value yields in this color. Recommendation: Play around and see...
 * Created 11-Sep-2011
 * @author lori
 */
public class convertASFtoMAP {

    public static void main(String[] args) throws Exception {
        ArgReader argr = new ArgReader("Creates PNG file from ASF map\n"
                + "-i file    ASF input\n"
                + "-o base    output basename, creates file.png and file.as.gz\n"
                + "[-c min,max] set minimum/maximum color/hue\n"
                + "[-m a_min,a_max,s_min,s_max,f_min,f_max] border values for avg,stdev,freq, auto if none set\n", args);

        //Parse arguments, go to subroutines if necessary
        String infile = argr.getArg("-i");
        String outbase = argr.getArg("-o");

        int minhue = 240, maxhue = 360;
        if (argr.isArg("-c")) {
            String[] sarr = argr.getArg("-c").split(",");
            minhue = Integer.parseInt(sarr[0]);
            maxhue = Integer.parseInt(sarr[1]);
        }
        //maxdegrees to use is minhue to 0 (e.g. 240 to 0), then 0(=360) to maxhue (e.g. 360 - 300)
        int maxdegrees = minhue + (360 - maxhue);

        //read max from cmdline or find out...
        double maxa, maxs, maxf, mina, mins, minf;
        if (argr.isArg("-m")) {
            System.out.println("Max ASF from command line");
            String[] sarr = argr.getArg("-m").split(",");
            maxa = Double.parseDouble(sarr[0]);
            mina = Double.parseDouble(sarr[1]);
            maxs = Double.parseDouble(sarr[2]);
            mins = Double.parseDouble(sarr[3]);
            maxf = Double.parseDouble(sarr[4]);
            minf = Double.parseDouble(sarr[5]);
        } else {
            double[] ret = findMinMax(infile);
            maxa = ret[0];
            mina = ret[1];
            maxs = ret[2];
            mins = ret[3];
            maxf = ret[4];
            minf = ret[5];
        }

        //picture and later output file
        int size = findSize(infile);
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);

        //go through file and set
        System.out.println("GOING THROUGH ASF " + infile);
        BufferedReader br = new BufferedReader(new FileReader(infile));
        String line;
        /* BIG FAT WARNING: The plot origin (0,0) is on the bottom left in a normal drawing, the
         * x-axis goes from left to right and the y-axis from bottom to top.
         * BUT for simpler coding, in ASF maps zero is on the TOP left, the y-axis from left to
         * right and the x-axis from top to bottom. The currentX and currentY values have to be
         * transposed somewhen in the program!!!
         */
        int currX = -1; //remember: lines=x, the future bottom line is on top of inputfile
        double[][] avgarr = new double[size][size];
        double[][] stdevarr = new double[size][size];
        while ((line = br.readLine()) != null) {
            if (line.startsWith("#")) {
                continue; //ignore comments/header
            }
            currX++; //next line = next x coordinate
            String[] sarr = line.split("\t");
            //next packet = next Y coordinate
            for (int currY = 0; currY < sarr.length; currY++) {
                //Parse the values
                String[] sarr2 = sarr[currY].split(";");
                double avg = Double.parseDouble(sarr2[0]);
                double stdev = Double.parseDouble(sarr2[1]);
                double freq = Double.parseDouble(sarr2[2]);

                //put values into "as" arrays with new coordinates!!!
                //new coors are to move picture origin from top to bottom left (mirror horiz)
                int newX = currX;
                int newY = size -1 - currY;
                avgarr[newX][newY] = avg;
                stdevarr[newX][newY] = stdev;

                //Make percentages
                int aPerc = (int) Math.round((avg - mina) * 100.0 / (maxa - mina));
                aPerc = aPerc < 0 ? 0 : aPerc;
                aPerc = aPerc > 100 ? 100 : aPerc;

                int sPerc = (int) Math.round((stdev - mins) * 100.0 / (maxs - mins));
                sPerc = sPerc < 0 ? 0 : sPerc;
                sPerc = sPerc > 100 ? 100 : sPerc;

                int fPerc = (int) Math.round((freq - minf) * 100.0 / (maxf - minf));
                fPerc = fPerc < 0 ? 0 : fPerc;
                fPerc = fPerc > 100 ? 100 : fPerc;

                //FIRST hue. This one is bit more complicated
                //convert percent to degrees. Since I want to go counter clockwise
                int degrees = maxdegrees * aPerc / 100;
                //start from minimum, go degrees
                int hue = minhue - degrees;

                //SECOND saturation 0(gray)-100(color)
                //I want large s to be gray, so "100 - ..."
                int sat = 100 - sPerc;

                //THIRD lightness 0(black)-50(full color)-100(pure white)
                //I only want up to 50, so scale percent to max 50
                //for white bg pics it must be in range 50-100 logically
                int light = fPerc / 2;

                int rgb = HSLColor.toRGB(hue, sat, light).getRGB();
                image.setRGB(newX,newY, rgb);
            }

        }

        System.out.println("WRITING PNG " + outbase);
        ImageIO.write(image, "png", new File(outbase));

        System.out.println("WRITING AVGSTDEV " + outbase + ".as.gz");
        DecimalFormat df = new DecimalFormat("###0.##");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(outbase + ".as.gz"))));
        //remember for writing out: y are lines x are colums
        for (int y = 0; y < avgarr.length; y++) {
            for (int x = 0; x < avgarr[y].length; x++) {
                String avgout = Double.isNaN(avgarr[x][y]) ? "NaN" : df.format(avgarr[x][y]);
                String stout = Double.isNaN(stdevarr[x][y]) ? "NaN" : df.format(stdevarr[x][y]);
                bw.write(avgout + ";" + stout + "\t");
            }
            bw.newLine();
        }
        bw.close();
        //Prints out a legend with the integer-avg-value colors
        //eg 0=blue 1=light green 2=pink etc...
        System.out.print("LEGEND WHICH INTEGER AVG HAS WHICH HUE: ");
        int int_maxh = (int) Math.round(maxa);
        for (int i = 0; i <= int_maxh; i++) {
            int aPerc = (int) Math.round((i - mina) * 100.0 / (maxa - mina));
            aPerc = aPerc < 0 ? 0 : aPerc;
            aPerc = aPerc > 100 ? 100 : aPerc;
            int degrees = maxdegrees * aPerc / 100;
            int hue = minhue - degrees;
            System.out.print(i + "=>" + hue + " ");
        }
        System.out.println("");
        System.out.println("END");
    }

    private static double[] findMinMax(String arg) throws Exception {
        System.out.print("Min/Max ASF from file " + arg + " ");
        BufferedReader br = new BufferedReader(new FileReader(arg));
        String line;
        double maxa = Double.MIN_VALUE, maxs = Double.MIN_VALUE, maxf = Double.MIN_VALUE;
        double mina = Double.MAX_VALUE, mins = Double.MAX_VALUE, minf = Double.MAX_VALUE;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("#")) {
                continue;
            }
            String[] sarr = line.split("\t");
            for (int i = 0; i < sarr.length; i++) {
                String[] sarr2 = sarr[i].split(";");
                double h = Double.parseDouble(sarr2[0]);
                if (h > maxa) {
                    maxa = h;
                }
                if (h < mina) {
                    mina = h;
                }
                double s = Double.parseDouble(sarr2[1]);
                if (s > maxs) {
                    maxs = s;
                }
                if (s < mins) {
                    mins = s;
                }
                double l = Double.parseDouble(sarr2[2]);
                if (l > maxf) {
                    maxf = l;
                }
                if (l < minf) {
                    minf = l;
                }
            }
        }
        br.close();
        DecimalFormat df = new DecimalFormat("#.##");
        System.out.println(df.format(maxa) + "," + df.format(mina) + ","
                + df.format(maxs) + "," + df.format(mins) + ","
                + df.format(maxf) + "," + df.format(minf));

        return new double[]{maxa, mina, maxs, mins, maxf, minf};
    }

    private static int findSize(String arg) throws Exception {
        System.out.print("Finding size of picture " + arg + " ");
        int size = -1;
        BufferedReader br = new BufferedReader(new FileReader(arg));
        String s;
        while ((s = br.readLine()) != null) {
            if (s.startsWith("#Size")) {
                size = Integer.parseInt(s.split("\t")[1]);
                break;
            }
        }
        br.close();

        if (size == -1) {
            throw new Exception("Size not found in header!");
        } else {
            System.out.println(size);
        }
        return size;
    }
}
