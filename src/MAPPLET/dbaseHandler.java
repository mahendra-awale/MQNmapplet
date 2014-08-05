package MAPPLET;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import javax.imageio.ImageIO;

/**
 * 
 * This class handles all the databases related activities. The class will be
 * mostly called in "appletMain" class. e.g when user select Specific database
 * from the APPLET window, then this class will be called, which set up all the
 * parameters and required files for Corresponding selected database.
 *
 * Created 20-Sep-2011
 *
 * @author mahendra
 */
public class dbaseHandler {

    /**
     * This method called only once from the appleMain class: to get the list of
     * available databases.
     *
     * Note: if You want to add new database: put the database name in the
     * string array below:
     *
     */
    public String[] listDBs() {
        return new String[]{"ZINC","Pubchem.60","GDB-13"};
    }

    /**
     * As we know for each database we have several different maps: So this
     * method designed to give the list of these maps. List of maps then used to
     * set up the list in the "COMBOX"
     */
    public String[] mapsOfDB(String db) {
        return new String[]{"ring", "ringatom", "rbc", "carbon", "hba", "hac", "occupancy"};
    }

    /**
     * ***************************************************************************************
     */
    /**
     * Opens dens.gz of a database and determines size with it. gz to keep data
     * transfer amount low
     */
    public Point getMapSize(String db) throws Exception {
        //read in densities two times. First to define map size, then to read values
        try {
            //first run through counting lines and colums to define size
            //REMEMBER: lines = y (size), columns = x (size)
            BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(
                    getClass().getResourceAsStream("/dbases/" + db + "/dens.gz"))));
            String s;
            short ysize = 0;
            short xsize = 0;
            while ((s = br.readLine()) != null) {
                ysize++;
                xsize = (short) s.split("\t").length;
            }
            br.close();
            return new Point(xsize, ysize);

        } catch (Exception e) {
            //e.printStackTrace();
            throw new Exception("ERROR: Cannot determine size (" + e.toString() + ")!");
        }
    }

    /**
     * ***************************************************************************************
     */
    /**
     * Opens dens.gz of a database and fills in densities (for textfield)
     */
    public int[][] getDensities(String db, Point size) throws Exception {
        //second run actually loads freq values
        try {
            int[][] dens = new int[size.x][size.y];
            BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(
                    getClass().getResourceAsStream("/dbases/" + db + "/dens.gz"))));
            //REMEMBER: lines = y (size), columns = x (size)
            for (int x = 0; x < size.x; x++) {
                String[] sarr = br.readLine().split("\t");
                for (int y = 0; y < size.y; y++) {
                    dens[x][y] = Integer.parseInt(sarr[y]);
                }
            }
            br.close();
            return dens;
        } catch (Exception e) {
            //e.printStackTrace();
            throw new Exception("ERROR: Cannot load densities (" + e.toString() + ")!");
        }
    }

    /**
     * ***************************************************************************************
     */
    /*
     * Loads "average molecules" for the mviewpane in the main applet
     */
    public String[][] getAvgMols(String db, Point size) throws Exception {
        //reading array of avg mols
        try {
            String[][] avgmols = new String[size.x][size.y];
            BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(
                    getClass().getResourceAsStream("/dbases/" + db + "/avgmol.gz"))));
            //REMEMBER: lines = y (size), columns = x (size)
            for (int x = 0; x < size.x; x++) {
                String[] sarr = br.readLine().split("\t");
                for (int y = 0; y < size.y; y++) {
                    if (sarr[y].equals("-")) {
                        avgmols[x][y] = "";
                    } else {
                        avgmols[x][y] = sarr[y];
                    }
                }
            }
            br.close();
            return avgmols;
        } catch (Exception e) {
            throw new Exception("ERROR: Cannot load avg mols (" + e.toString() + ")!\n");
        }
    }

    /**
     * ***************************************************************************************
     */
    /*
     * Loads "average molecules information" file: file specially needed for
     * CATAGORY MAPS
     */
    public String[][] getAvgMolInfo(String db, Point size) throws Exception {
        //reading array of avg mols
        String[][] avgmols = new String[size.x][size.y];
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(
                    getClass().getResourceAsStream("/dbases/" + db + "/avgmolInfo.gz"))));
            //REMEMBER: lines = y (size), columns = x (size)
            for (int x = 0; x < size.x; x++) {
                String[] sarr = br.readLine().split("\t");
                for (int y = 0; y < size.y; y++) {
                    if (sarr[y].equals("-")) {
                        avgmols[x][y] = "";
                    } else {
                        avgmols[x][y] = sarr[y];
                    }
                }
            }
            br.close();
            return avgmols;
        } catch (Exception e) {
            return avgmols;
        }
    }

    /**
     * ***************************************************************************************
     */
    /**
     * Opens a map / png format usually. very simple thanks to ImageIO
     */
    public BufferedImage getMapAsImg(String db, String file) throws Exception {
        try {
            return ImageIO.read(getClass().getResource("/dbases/" + db + "/maps/" + file));
        } catch (Exception e) {
            throw new Exception("ERROR: Cannot load map picture (" + e.toString() + ")!\n");
        }

    }

    /**
     * ***************************************************************************************
     */
    /**
     * Loads averages/stddevs of a map, usually its the file MAP.as.gz. Since
     * the values have two significant digits maximum they can be loaded to
     * floats (instead of doubles, needs less memory).
     */
    public float[][][] getAvgStdevOfMap(String db, String map, Point imgOrigSize) throws Exception {
        try {
            //load avg and stddev values
            float[][] avg = new float[imgOrigSize.x][imgOrigSize.y];
            float[][] stdev = new float[imgOrigSize.x][imgOrigSize.y];
            BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(
                    getClass().getResourceAsStream("/dbases/" + db + "/maps/" + map + ".as.gz"))));
            for (int x = 0; x < imgOrigSize.x; x++) {
                String[] sarr = br.readLine().split("\t");
                for (int y = 0; y < imgOrigSize.y; y++) {
                    String[] sarr2 = sarr[y].split(";");
                    avg[x][y] = Float.parseFloat(sarr2[0]);
                    stdev[x][y] = Float.parseFloat(sarr2[1]);
                }
            }
            br.close();
            //crazy code to hand over TWO arrays in one return: make an array with arrayOfArray =)
            return new float[][][]{avg, stdev};

        } catch (Exception e) {
            //e.printStackTrace();
            throw new Exception("ERROR: Cannot load map values of " + map + " (" + e.toString() + ")!\n");
        }
    }
    /**
     * ***************************************************************************************
     */
}
