/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package organize.tools;

/**
 *
 * @author mahendra
 */
public class test {
    
    
    public static void main(String args[])
    {
    
        double newPC[]=new double[2];
        newPC[0]=4.145494353509326;
        newPC[1]=6.557293940358997;
        
        double[] rotatepoint = rotatepoint(newPC[0], newPC[1], 45.0);
    
        System.out.println(rotatepoint[0]+" "+rotatepoint[1]);
    }
    
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
