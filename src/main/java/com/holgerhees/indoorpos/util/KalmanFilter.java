package com.holgerhees.indoorpos.util;

/**
 * Created by hhees on 14.05.17.
 */
public class KalmanFilter
{
    /**
     * Create 1-dimensional kalman filter
     * @param  {Number} options.R Process noise
     * @param  {Number} options.Q Measurement noise
     * @param  {Number} options.A State vector
     * @param  {Number} options.B Control vector
     * @param  {Number} options.C Measurement vector
     * @return {KalmanFilter}
     */

    private double R;
    private double Q;
    private double A;
    private double B;
    private double C;

    private double cov;
    private double x;

    private boolean firstRun = true;

    public KalmanFilter() {

        this.R = 1; // noise power desirable
        this.Q = 1; // noise power estimated

        this.A = 1;
        this.B = 0;
        this.C = 1;

        this.cov = 0;
        this.x = 0; // estimated signal without noise
    }

    public void setR( double r )
    {
        R = r;
    }

    public void setQ( double q )
    {
        Q = q;
    }

    public void setA( double a )
    {
        A = a;
    }

    /**
     * Filter a new value
     * @param  {Number} z Measurement
     * @param  {Number} u Control
     * @return {Number}
     */
    public double filter( double z) {

        double u = 0;

        if( firstRun )
        {
            this.x = (1 / this.C) * z;
            this.cov = (1 / this.C) * this.Q * (1 / this.C);
            this.firstRun = false;
        }
        else
        {
            // Compute prediction
            double predX = (this.A * this.x) + (this.B * u);
            double predCov = ((this.A * this.cov) * this.A) + this.R;

            // Kalman gain
            double K = predCov * this.C * (1 / ((this.C * predCov * this.C) + this.Q));

            // Correction
            this.x = predX + K * (z - (this.C * predX));
            this.cov = predCov - (K * this.C * predCov);
        }

        return this.x;
    }

    /**
     * Return the last filtered measurement
     * @return {Number}
     */
    public double lastMeasurement()
    {
        return this.x;
    }

    /**
     * Set measurement noise Q
     * @param {Number} noise
     */
    public void setMeasurementNoise(double noise)
    {
        this.Q = noise;
    }

    /**
     * Set the process noise R
     * @param {Number} noise
     */
    public void setProcessNoise( double noise)
    {
        this.R = noise;
    }
}
