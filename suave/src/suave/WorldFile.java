/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package suave;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author owens
 */
public class WorldFile implements GeoTransformsConstants {

    private double A;
    private double D;
    private double B;
    private double E;
    private double C;
    private double F;
    private double Ap;
    private double Dp;
    private double Bp;
    private double Ep;
    private double Cp;
    private double Fp;

    // For documentation on world files see;
    //
    // http:// en.wikipedia.org/wiki/World_file
    //
    // and see;
    //
    // http://www.epsg.org/guides/docs/G7-2.pdf
    //
    // section "2.3.2.1 Affine Parametric Transformation" for reversing parameteric affine transformations
    // Regarding order of these args, see comment below on the constructor.
    private void init(double A, double D, double B, double E, double C, double F) {
        this.A = A;
        this.D = D;
        this.B = B;
        this.E = E;
        this.C = C;
        this.F = F;

        double denom = A * E - B * D;
        Cp = (B * F - E * C) / denom;
        Fp = (D * C - A * F) / denom;
        Ap = +E / denom;
        Bp = -B / denom;
        Dp = -D / denom;
        Ep = +A / denom;
    }

    // NOTE NOTE NOTE : THe ordering here is far from intuitive.  Sorry, I didn't invent world files.  
    // World files have six lines with numbers in THIS order.   Programs that generate world files also 
    // print out the numbers in THIS order.
    public WorldFile(double A, double D, double B, double E, double C, double F) {
        init(A, D, B, E, C, F);
    }

    public WorldFile(String filename) throws IOException {
        double a = 0, b = 0, c = 0, d = 0, e = 0, f = 0;
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(filename);
            bufferedReader = new BufferedReader(fileReader);

            // First line of VC telemetry log is headers (describing
            // what each column is) in a different format.
            String line = null;
            line = bufferedReader.readLine();
            a = Double.parseDouble(line);
            line = bufferedReader.readLine();
            d = Double.parseDouble(line);
            line = bufferedReader.readLine();
            b = Double.parseDouble(line);
            line = bufferedReader.readLine();
            e = Double.parseDouble(line);
            line = bufferedReader.readLine();
            c = Double.parseDouble(line);
            line = bufferedReader.readLine();
            f = Double.parseDouble(line);
            bufferedReader.close();
        } catch (IOException ex) {
            Debug.debug(5, "WorldFile constructor: IOException reading world file " + filename + ", e=" + ex);
            ex.printStackTrace();
            Debug.debug(5, "WorldFile constructor: Rethrowing exception.");
            throw ex;
        }
        init(a, d, b, e, c, f);
    }

    public void toCoords(double[] pixel, double[] coords) {
        coords[0] = A * pixel[0] + B * pixel[1] + C;
        coords[1] = D * pixel[0] + E * pixel[1] + F;
    }

    public void toPixel(double[] coords, double[] pixel) {
        pixel[0] = Ap * coords[0] + Bp * coords[1] + Cp;
        pixel[1] = Dp * coords[0] + Ep * coords[1] + Fp;
    }
}
