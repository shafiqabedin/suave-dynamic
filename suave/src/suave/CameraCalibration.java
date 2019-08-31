/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package suave;

import java.util.Calendar;
import org.apache.commons.math.geometry.Vector3D;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;

// So, basically the process is, based on;
//
// http://www.vision.caltech.edu/bouguetj/calib_doc/htmls/parameters.html
//
// Using as inputs our "world to camera" stuff, i.e. eye position, dir
// vector (or 'target' position, we can compute dir from target - eye),
// and 'up' vector, as well as;
//
// fc : 2x1 camera focal length (vertical and horizontal)
// cc : 2x1 'principal point location'?
// kc : 5x1 distortiion coefficients, both radial and tangential
// alpha: skew coefficient between x and y pixel (i.e. square vs non-square pixels)
//
// and matrix KK where KK is;
//
// fc(1)        alpha*fc(2)        cc(1)
//   0             fc(2)           cc(2)
//   0               0                1
//
// 0) pre-Compute a bunch of 'constants' based on our lens - more on this later
//
// 1) Project from 'world' X to 'camera' Xc - the typical OpenGL matrix
// should do the job just fine, i.e. set up a gluLookat and grab that
// matrix, use it to project from world to camera reference frame
//
// 2) Project pinhole style, i.e. Xpin = Xc(1)/Xc(3), Xc(2)/Xc(3)
//
// 3) compute Xd using the formulas and the constants;
//
// r^2 = Xpin(1)^2 + Xpin(2)^2
//
// dx(1) =  2 * kc(3) * Xpin(1) * Xpin(2) + kc(4) * (r^2 + 2*Xpin(1)^2)
// dx(2) =  kc(3) * (r^2 + 2 * Xpin(2)^2)   + 2 * kc(4) * Xpin(1) * Xpin(2)
//
// other_constant = ( 1 + kc(1) * r^2 + kc(2) * r^4 + kc(5) * r^6)
//
// Xd(1) = other_constant * Xpin(1) + dx(1)
// Xd(2) = other_constant * Xpin(2) + dx(2)
//
// 4) compute Xp = KK * xd to get pixel coords - specifically since KK is
// a 3x3 matrix;
//
//  xp          xd(1)
//  yp  = KK *  xd(2)
//  1             1
/**
 *
 * @author owens
 */
public class CameraCalibration {

    // output of camera calibration, for VSB2 camera;
    //  Calibration results after optimization (with uncertainties): 
    //   
    //  Focal Length:          fc = [ 451.93246   453.89293 ] +/- [ 9.01896   6.63878 ] 
    //  Principal point:       cc = [ 322.64415   247.73978 ] +/- [ 6.44784  14.23368 ] 
    //  Skew:             alpha_c = [ 0.00000 ] +/- [ 0.00000  ]   =    //  angle of pixel axes = 90.00000 +/- 0.00000 degrees 
    //  Distortion:            kc = [ -0.00529   0.15478   0.00005   0.00252  0.00000 ] +/- [ 0.03040   0.17018   0.00347   0.00546  0.00000 ] 
    //  Pixel error:          err = [ 0.24842   0.09187 ] 
    //   
    //  Note: The numerical errors are approximately three times the standard 
    //  deviations (for reference). 
    //   
    //   
    //  Recommendation: Some distortion coefficients are found equal to zero (within 
    //  their uncertainties). 
    //                  To reject them from the optimization set 
    //  est_dist=[0;1;0;0;0] and run Calibration 
    //   
    //      //     //  KK 
    //   
    //  KK = 
    //   
    //    451.9325         0  322.6441 
    //           0  453.8929  247.7398 
    //           0         0    1.0000 
//    double[] fc = {451.93246, 453.89293};
//    double[] cc = {322.64415, 247.73978};
//    double alpha_c = 0.0;
//    double[] kc = {-0.00529, 0.15478, 0.00005, 0.00252, 0.00000};
//    double[][] kkparams = {
//        {451.9325, 0, 322.6441},
//        {0, 453.8929, 247.7398},
//        {0, 0, 1.0000}
//    };
    // > From: Nathan Brooks <nbbrooks@gmail.com>
    // > To: owens@cs.cmu.edu
    // > Subject: Re: VBS2 camera calibration
    // > Date: Tue, 25 Jan 2011 01:49:31 -0500
    // >
    // > VBS2 has the default camera FOV set to 0.7, so fc = [640*0.7; 480*0.7] = [448; 336]. So KK is
    // >
    // >
    // > [448, 0, 320
    // >
    // > 0, 336, 240,
    // >
    // > 0, 0, 1]
    // >
    // > kc will be all zeros.
    //
    // This is the "IDEAL" calibration matrix;  from the Matlat camera calibration parameters  page;
    //
    // http://www.vision.caltech.edu/bouguetj/calib_doc/htmls/parameters.html
    //
    //       |  fc(1)    alpha_c*fc(1)  cc(1)  |
    //       |                                 |
    // KK =  |   0          fc(2)       cc(2)  |
    //       |                                 |
    //       |   0            0          1     |
    //
    // so
    //
    // fc = { 448, 336 }
    // cc = { 320, 240 }
    // alpha_c = 0
    // kc = all zeros!
    //
    //
    // > We're going to set the VBS2 camera FOV set to 0.5, so fc = [640*0.5; 480*0.5] = [320; 240]. So KK is
    // >
    // >
    // > [320, 0, 320
    // >
    // > 0, 240, 240,
    // >
    // > 0, 0, 1]
    // >
    // > kc will be all zeros.
    //
    //
//    double[] fc = {448, 336};
//    double[] cc = {320, 240};
//    double alpha_c = 0.0;
//    double[] kc = {0.0, 0.0, 0.0, 0.0, 0.0};
//    double[][] kkparams = {
//        {448, 0, 320},
//        {0, 336, 240},
//        {0, 0, 1.0000}
//    };
    // 'ideal' matrix from Pras
    // Camera FOV = 0.7 (default VBS2 camera)
    double[] fc = {457.8, 457.8};
    double[] cc = {320, 240};
    double alpha_c = 0.0;
    double[] kc = {0.0, 0.0, 0.0, 0.0, 0.0};
    double[][] kkparams = {
        {457.8, 0, 320},
        {0, 457.8, 240},
        {0, 0, 1.0000}
    };

    // Camera FOV = 0.5 (zoomed in)
//        double[] fc = {320, 320};
//        double[] cc = {320, 240};
//        double alpha_c = 0.0;
//        double[] kc = {0.0, 0.0, 0.0, 0.0, 0.0};
//        double[][] kkparams = {
//            {320, 0, 320},
//            {0, 320, 240},
//            {0, 0, 1.0000}
//        };
//
//    // Based on using  link.evaluate("cameraOrthography [true, 240]; "); in vbs2Gui.Main to change VBS camera calibration
//    double[] fc = {240, 180};
//    double[] cc = {120, 90};
//    double alpha_c = 0.0;
//    double[] kc = {0.0, 0.0, 0.0, 0.0, 0.0};
//    double[][] kkparams = {
//        {240, 0, 120},
//        {0, 180, 90},
//        {0, 0, 1.0000}
//    };
    RealMatrix worldToCamera;
    RealMatrix KK;

    public CameraCalibration() {
        RealVector r0 = new ArrayRealVector(kkparams[0]);
        RealVector r1 = new ArrayRealVector(kkparams[1]);
        RealVector r2 = new ArrayRealVector(kkparams[2]);

        KK = new Array2DRowRealMatrix(3, 3);
        KK.setRowVector(0, r0);
        KK.setRowVector(1, r1);
        KK.setRowVector(2, r1);
    }

    // @TODO: NOTE, below code is messing up the KK matrix somehow.  When I use
    // the ideal constants from pras (above) and use the other constructor, calibration
    // is perfect.  When I use this constructor (sending the same constants from Main)
    // everything goes wonky.
//    public CameraCalibration(
//            double[] fc,
//            double[] cc,
//            double alpha_c,
//            double[] kc) {
//        this.fc = new double[fc.length];
//        System.arraycopy(fc, 0, this.fc, 0, fc.length);
//        this.cc = new double[cc.length];
//        System.arraycopy(cc, 0, this.cc, 0, cc.length);
//        this.alpha_c = alpha_c;
//        this.kc = new double[kc.length];
//        System.arraycopy(kc, 0, this.kc, 0, kc.length);
//
//        RealVector r0 = new ArrayRealVector(new double[]{fc[0], alpha_c * fc[0], cc[0]});
//        RealVector r1 = new ArrayRealVector(new double[]{0, fc[1], cc[1]});
//        RealVector r2 = new ArrayRealVector(new double[]{0, 0, 1});
//
//        KK = new Array2DRowRealMatrix(3, 3);
//        KK.setRowVector(0,r0);
//        KK.setRowVector(1,r1);
//        KK.setRowVector(2,r1);
//    }
    public void setCameraOld(Vector3D eye, Vector3D dir, Vector3D up) {
//    public void setCamera(Vector3D eye, Vector3D dir, Vector3D up) {
        Debug.debug(1, "Setting Camera to eye=" + eye + ", dir=" + dir + ", up=" + up);

        dir = dir.normalize();
        up = up.normalize();

        // Pras's code
        Vector3D left = Vector3D.crossProduct(up, dir);
        RealVector mEye = new ArrayRealVector(new double[]{eye.getX(), eye.getY(), eye.getZ()});
        RealVector mDir = new ArrayRealVector(new double[]{dir.getX(), dir.getY(), dir.getZ()});
        RealVector mUp = new ArrayRealVector(new double[]{up.getX(), up.getY(), up.getZ()});
        RealVector mLeft = new ArrayRealVector(new double[]{left.getX(), left.getY(), left.getZ()});

        RealMatrix xForm1 = new Array2DRowRealMatrix(3, 4);
        xForm1.setColumnVector(0, mLeft);
        xForm1.setColumnVector(1, mUp);
        xForm1.setColumnVector(2, mDir);
        xForm1.setColumnVector(3, mEye);

        RealMatrix xForm2 = new Array2DRowRealMatrix(4, 4);
        xForm2.setSubMatrix(xForm1.getData(), 0, 0);
        xForm2.setEntry(3, 3, 1);

        RealMatrix xForm_44 = new LUDecompositionImpl(xForm2).getSolver().getInverse();

        worldToCamera = xForm_44;
    }

    public void transformOld(double[] worldPoint, double[] pixelPoint) {
//    public void transform(double[] worldPoint, double[] pixelPoint) {
        // world frame to camera reference frame
        double Pworld[] = {worldPoint[0], worldPoint[1], worldPoint[2], 1};
        RealMatrix Pcamera = new Array2DRowRealMatrix(Pworld);
        Pcamera = worldToCamera.multiply(Pcamera);

        // pinhole perspective projection - just divide by Z
        double PpinholeX = Pcamera.getEntry(0, 0) / Pcamera.getEntry(2, 0);
        double PpinholeY = Pcamera.getEntry(1, 0) / Pcamera.getEntry(2, 0);

        // Now for Distortion

        // compute some sub expressions
        double r2 = PpinholeX * PpinholeX + PpinholeY * PpinholeY;
        double r4 = r2 * r2;
        double r6 = r4 * r2;
        double PpinholeXtimesY = PpinholeX * PpinholeY;
        double dx1 = 2 * kc[2] * PpinholeXtimesY + kc[3] * (r2 + 2 * PpinholeX * PpinholeX);
        double dx2 = kc[2] * (r2 + 2 * PpinholeY * PpinholeY) + 2 * kc[3] * PpinholeXtimesY;

        double otherDistortionConstant = (1 + kc[0] * r2 + kc[1] * r4 + kc[4] * r6);

        double PdistortionX = otherDistortionConstant * PpinholeX + dx1;
        double PdistortionY = otherDistortionConstant * PpinholeY + dx2;
        double[] Pdistortionparams = {PdistortionX, PdistortionY, 1};

        RealMatrix Pdistortion = new Array2DRowRealMatrix(Pdistortionparams);
        RealMatrix Ppixel = KK.multiply(Pdistortion);

        pixelPoint[0] = Ppixel.getEntry(0, 0);
        pixelPoint[1] = Ppixel.getEntry(1, 0);
        // if we unrolled the matrix we would get;
        //            pixelPoint[0] = fc[0] * (PdistortionX + alpha_c * PdistortionY) + cc[0];
        //            pixelPoint[1] = fc[1] * PdistortionY + cc[1];
    }
    // -------------------------------------------------------------------------------------
    // Experimental hopefully faster code - mostly unrollign the apache commons stuff
    // -------------------------------------------------------------------------------------
    double w00 = 0;
    double w01 = 0;
    double w02 = 0;
    double w03 = 0;
    double w10 = 0;
    double w11 = 0;
    double w12 = 0;
    double w13 = 0;
    double w20 = 0;
    double w21 = 0;
    double w22 = 0;
    double w23 = 0;
    double w30 = 0;
    double w31 = 0;
    double w32 = 0;
    double w33 = 0;
    double kc0 = 0;
    double kc1 = 0;
    double kc2 = 0;
    double kc3 = 0;
    double kc4 = 0;
    double kk00 = 0;
    double kk01 = 0;
    double kk02 = 0;
    double kk03 = 0;
    double kk10 = 0;
    double kk11 = 0;
    double kk12 = 0;
    double kk13 = 0;
    double kk20 = 0;
    double kk21 = 0;
    double kk22 = 0;
    double kk23 = 0;
    double kk30 = 0;
    double kk31 = 0;
    double kk32 = 0;
    double kk33 = 0;

    public void setCamera(Vector3D eye, Vector3D dir, Vector3D up) {
//    public void setCameraNew(Vector3D eye, Vector3D dir, Vector3D up) {
        Debug.debug(1, "Setting Camera to eye=" + eye + ", dir=" + dir + ", up=" + up);

        dir = dir.normalize();
        up = up.normalize();

        // Pras's code
        Vector3D left = Vector3D.crossProduct(up, dir);
        RealVector mEye = new ArrayRealVector(new double[]{eye.getX(), eye.getY(), eye.getZ()});
        RealVector mDir = new ArrayRealVector(new double[]{dir.getX(), dir.getY(), dir.getZ()});
        RealVector mUp = new ArrayRealVector(new double[]{up.getX(), up.getY(), up.getZ()});
        RealVector mLeft = new ArrayRealVector(new double[]{left.getX(), left.getY(), left.getZ()});

        RealMatrix xForm1 = new Array2DRowRealMatrix(3, 4);
        xForm1.setColumnVector(0, mLeft);
        xForm1.setColumnVector(1, mUp);
        xForm1.setColumnVector(2, mDir);
        xForm1.setColumnVector(3, mEye);

        RealMatrix xForm2 = new Array2DRowRealMatrix(4, 4);
        xForm2.setSubMatrix(xForm1.getData(), 0, 0);
        xForm2.setEntry(3, 3, 1);

        RealMatrix xForm_44 = new LUDecompositionImpl(xForm2).getSolver().getInverse();

        worldToCamera = xForm_44;
        w00 = worldToCamera.getEntry(0, 0);
        w01 = worldToCamera.getEntry(0, 1);
        w02 = worldToCamera.getEntry(0, 2);
        w03 = worldToCamera.getEntry(0, 3);
        w10 = worldToCamera.getEntry(1, 0);
        w11 = worldToCamera.getEntry(1, 1);
        w12 = worldToCamera.getEntry(1, 2);
        w13 = worldToCamera.getEntry(1, 3);
        w20 = worldToCamera.getEntry(2, 0);
        w21 = worldToCamera.getEntry(2, 1);
        w22 = worldToCamera.getEntry(2, 2);
        w23 = worldToCamera.getEntry(2, 3);
        w30 = worldToCamera.getEntry(3, 0);
        w31 = worldToCamera.getEntry(3, 1);
        w32 = worldToCamera.getEntry(3, 2);
        w33 = worldToCamera.getEntry(3, 3);
        kc0 = kc[0];
        kc1 = kc[1];
        kc2 = kc[2];
        kc3 = kc[3];
        kc4 = kc[4];
        kk00 = KK.getEntry(0, 0);
        kk01 = KK.getEntry(0, 1);
        kk02 = KK.getEntry(0, 2);
        kk10 = KK.getEntry(1, 0);
        kk11 = KK.getEntry(1, 1);
        kk12 = KK.getEntry(1, 2);
        kk20 = KK.getEntry(2, 0);
        kk21 = KK.getEntry(2, 1);
        kk22 = KK.getEntry(2, 2);
    }

    public void transform(double[] worldPoint, double[] pixelPoint) {
//    public void transformNew(double[] worldPoint, double[] pixelPoint) {
  
        double Pworld0 = worldPoint[0];
        double Pworld1 = worldPoint[1];
        double Pworld2 = worldPoint[2];
        double Pcamera0 = w00 * Pworld0 + w01 * Pworld1 + w02 * Pworld2 + w03;
        double Pcamera1 = w10 * Pworld0 + w11 * Pworld1 + w12 * Pworld2 + w13;
        double Pcamera2 = w20 * Pworld0 + w21 * Pworld1 + w22 * Pworld2 + w23;

        double PpinholeX = Pcamera0 / Pcamera2;
        double PpinholeY = Pcamera1 / Pcamera2;

        // compute some sub expressions
        double PpinholeX2 = PpinholeX * PpinholeX;
        double PpinholeY2 = PpinholeY * PpinholeY;
        double r2 = PpinholeX2 + PpinholeY2;
        double r4 = r2 * r2;
        double r6 = r4 * r2;
        double PpinholeXtimesY = PpinholeX * PpinholeY;
        double dx1 = 2 * kc2 * PpinholeXtimesY + kc3 * (r2 + 2 * PpinholeX2);
        double dx2 = kc2 * (r2 + 2 * PpinholeY2) + 2 * kc3 * PpinholeXtimesY;

        double otherDistortionConstant = (1 + kc0 * r2 + kc1 * r4 + kc4 * r6);

        double PdistortionX0 = otherDistortionConstant * PpinholeX + dx1;
        double PdistortionY1 = otherDistortionConstant * PpinholeY + dx2;

        pixelPoint[0] = kk00 * PdistortionX0 + kk01 * PdistortionY1 + kk02;
        pixelPoint[1] = kk11 * PdistortionY1 + kk12;
    }
//      double w00 = 0;
//    double w01 = 0;
//    double w02 = 0;
//    double w03 = 0;
//    double w10 = 0;
//    double w11 = 0;
//    double w12 = 0;
//    double w13 = 0;
//    double w20 = 0;
//    double w21 = 0;
//    double w22 = 0;
//    double w23 = 0;
//    double w30 = 0;
//    double w31 = 0;
//    double w32 = 0;
//    double w33 = 0;
//    double kc0 = 0;
//    double kc1 = 0;
//    double kc2 = 0;
//    double kc3 = 0;
//    double kc4 = 0;
//    double kk00 = 0;
//    double kk01 = 0;
//    double kk02 = 0;
//    double kk03 = 0;
//    double kk10 = 0;
//    double kk11 = 0;
//    double kk12 = 0;
//    double kk13 = 0;
//    double kk20 = 0;
//    double kk21 = 0;
//    double kk22 = 0;
//    double kk23 = 0;
//    double kk30 = 0;
//    double kk31 = 0;
//    double kk32 = 0;
//    double kk33 = 0;
   public void setCameraTest(Vector3D eye, Vector3D dir, Vector3D up) {
        Debug.debug(1, "Setting Camera to eye=" + eye + ", dir=" + dir + ", up=" + up);

//        eye = eye.normalize();
        dir = dir.normalize();
        up = up.normalize();

        // Pras's code
        Vector3D left = Vector3D.crossProduct(up, dir);
        RealVector mEye = new ArrayRealVector(new double[]{eye.getX(), eye.getY(), eye.getZ()});
        RealVector mDir = new ArrayRealVector(new double[]{dir.getX(), dir.getY(), dir.getZ()});
        RealVector mUp = new ArrayRealVector(new double[]{up.getX(), up.getY(), up.getZ()});
        RealVector mLeft = new ArrayRealVector(new double[]{left.getX(), left.getY(), left.getZ()});

        RealMatrix xForm1 = new Array2DRowRealMatrix(3, 4);
        xForm1.setColumnVector(0, mLeft);
        xForm1.setColumnVector(1, mUp);
        xForm1.setColumnVector(2, mDir);
        xForm1.setColumnVector(3, mEye);

        RealMatrix xForm2 = new Array2DRowRealMatrix(4, 4);
        xForm2.setSubMatrix(xForm1.getData(), 0, 0);
        xForm2.setEntry(3, 3, 1);

        RealMatrix xForm_44 = new LUDecompositionImpl(xForm2).getSolver().getInverse();

        worldToCamera = xForm_44;
        w00 = worldToCamera.getEntry(0, 0);
        w01 = worldToCamera.getEntry(0, 1);
        w02 = worldToCamera.getEntry(0, 2);
        w03 = worldToCamera.getEntry(0, 3);
        w10 = worldToCamera.getEntry(1, 0);
        w11 = worldToCamera.getEntry(1, 1);
        w12 = worldToCamera.getEntry(1, 2);
        w13 = worldToCamera.getEntry(1, 3);
        w20 = worldToCamera.getEntry(2, 0);
        w21 = worldToCamera.getEntry(2, 1);
        w22 = worldToCamera.getEntry(2, 2);
        w23 = worldToCamera.getEntry(2, 3);
        w30 = worldToCamera.getEntry(3, 0);
        w31 = worldToCamera.getEntry(3, 1);
        w32 = worldToCamera.getEntry(3, 2);
        w33 = worldToCamera.getEntry(3, 3);
        kc0 = kc[0];
        kc1 = kc[1];
        kc2 = kc[2];
        kc3 = kc[3];
        kc4 = kc[4];
        kk00 = KK.getEntry(0, 0);
        kk01 = KK.getEntry(0, 1);
        kk02 = KK.getEntry(0, 2);
        kk10 = KK.getEntry(1, 0);
        kk11 = KK.getEntry(1, 1);
        kk12 = KK.getEntry(1, 2);
        kk20 = KK.getEntry(2, 0);
        kk21 = KK.getEntry(2, 1);
        kk22 = KK.getEntry(2, 2);
    }

    public void transformTest(double[] worldPoint, double[] pixelPoint) {

        // ORIG ----------------------------------------------------
        // world frame to camera reference frame
        double Pworld[] = {worldPoint[0], worldPoint[1], worldPoint[2], 1};
        RealMatrix Pcamera = new Array2DRowRealMatrix(Pworld);
        Pcamera = worldToCamera.multiply(Pcamera);

        // NEW ----------------------------------------------------
        double Pworld0 = worldPoint[0];
        double Pworld1 = worldPoint[1];
        double Pworld2 = worldPoint[2];
        double Pworld3 = 1;
        double Pcamera0 = w00 * Pworld0 + w01 * Pworld1 + w02 * Pworld2 + w03 * Pworld3;
        double Pcamera1 = w10 * Pworld0 + w11 * Pworld1 + w12 * Pworld2 + w13 * Pworld3;
        double Pcamera2 = w20 * Pworld0 + w21 * Pworld1 + w22 * Pworld2 + w23 * Pworld3;

        // TEST
        if (Pcamera0 != Pcamera.getEntry(0, 0)
                || Pcamera1 != Pcamera.getEntry(1, 0)
                || Pcamera2 != Pcamera.getEntry(2, 0)) {
            System.err.println("1: NEW CODE FAILS!");
        }

        // ORIG ----------------------------------------------------
        // pinhole perspective projection - just divide by Z
        double PpinholeX = Pcamera.getEntry(0, 0) / Pcamera.getEntry(2, 0);
        double PpinholeY = Pcamera.getEntry(1, 0) / Pcamera.getEntry(2, 0);

        // NEW ----------------------------------------------------
        double PpinholeXnew = Pcamera0 / Pcamera2;
        double PpinholeYnew = Pcamera1 / Pcamera2;

        // Test
        if (PpinholeXnew != PpinholeX || PpinholeYnew != PpinholeY) {
            System.err.println("2: NEW CODE FAILS!");
        }

        // Now for Distortion

        // ORIG ----------------------------------------------------
        // compute some sub expressions
        double r2 = PpinholeX * PpinholeX + PpinholeY * PpinholeY;
        double r4 = r2 * r2;
        double r6 = r4 * r2;
        double PpinholeXtimesY = PpinholeX * PpinholeY;
        double dx1 = 2 * kc[2] * PpinholeXtimesY + kc[3] * (r2 + 2 * PpinholeX * PpinholeX);
        double dx2 = kc[2] * (r2 + 2 * PpinholeY * PpinholeY) + 2 * kc[3] * PpinholeXtimesY;

        double otherDistortionConstant = (1 + kc[0] * r2 + kc[1] * r4 + kc[4] * r6);

        double PdistortionX = otherDistortionConstant * PpinholeX + dx1;
        double PdistortionY = otherDistortionConstant * PpinholeY + dx2;
        double[] Pdistortionparams = {PdistortionX, PdistortionY, 1};

        // NEW ----------------------------------------------------

        // compute some sub expressions
        double PpinholeXnew2 = PpinholeXnew * PpinholeXnew;
        double PpinholeYnew2 = PpinholeYnew * PpinholeYnew;
        double r2new = PpinholeXnew2 + PpinholeYnew2;
        double r4new = r2new * r2new;
        double r6new = r4new * r2new;
        double PpinholeXtimesYnew = PpinholeXnew * PpinholeYnew;
        double dx1new = 2 * kc2 * PpinholeXtimesYnew + kc3 * (r2new + 2 * PpinholeXnew2);
        double dx2new = kc2 * (r2new + 2 * PpinholeYnew2) + 2 * kc3 * PpinholeXtimesYnew;

        double otherDistortionConstantnew = (1 + kc0 * r2new + kc1 * r4new + kc4 * r6new);

        double PdistortionXnew0 = otherDistortionConstantnew * PpinholeXnew + dx1new;
        double PdistortionYnew1 = otherDistortionConstantnew * PpinholeYnew + dx2new;
        double Pdistortionnew2 = 1;

        // Test
        if (PdistortionXnew0 != PdistortionX || PdistortionYnew1 != PdistortionY) {
            System.err.println("3: NEW CODE FAILS!");
        }

        // ORIG ----------------------------------------------------
        RealMatrix Pdistortion = new Array2DRowRealMatrix(Pdistortionparams);
        RealMatrix Ppixel = KK.multiply(Pdistortion);
        pixelPoint[0] = Ppixel.getEntry(0, 0);
        pixelPoint[1] = Ppixel.getEntry(1, 0);

        // NEW ----------------------------------------------------

        double PpixelXnew = kk00 * PdistortionXnew0 + kk01 * PdistortionYnew1 + kk02;
        double PpixelYnew = kk11 * PdistortionYnew1 + kk12;

        // Test
        if (PpixelXnew != pixelPoint[0] || PpixelYnew != pixelPoint[1]) {
            System.err.println("4: NEW CODE FAILS!");
        }
    }

    public static void main(String[] args) {
        CameraCalibration cc = new CameraCalibration();
        Vector3D eye = new Vector3D(0, 0, 0);
        Vector3D up = new Vector3D(0, 1, 0);
        Vector3D dir = new Vector3D(0, 0, 1);
        double[][] points = {
            {-10, 0, 100},
            {0, 0, 100},
            {10, 0, 100},
            {-10, 10, 100},
            {-0, 10, 100},
            {10, 10, 100},
            {-10, -10, 100},
            {0, -10, 100},
            {10, -10, 100}
        };

        cc.setCamera(eye, dir, up);
        double[] pixel = new double[3];
        for (int loopi = 0; loopi < points.length; loopi++) {
            cc.transform(points[loopi], pixel);
        }

    }
}
