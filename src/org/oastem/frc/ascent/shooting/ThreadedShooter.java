/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oastem.frc.ascent.shooting;

import edu.wpi.first.wpilibj.PIDOutput;
import org.oastem.frc.pid.PIDGainOutput;

/**
 *
 * @author STEM
 */
public class ThreadedShooter {
    private double[] vars;
    private Runnable[] runners; 
    private int instances;
    private int curInst = 0;
    private Thread[] thrs;
    private final int THREAD_DELAY = 100;
    private boolean stop = false;
    
    public ThreadedShooter(double[] data, int inst) {
        runners = new Runnable[inst];
        thrs = new Thread[inst];
        instances = inst;
        update(data);
    }
    
    public void update(double[] data) {
        vars = data;
    }
    
    public void attach(final int index, final PIDOutput out) {
        runners[curInst++] = new Runnable() {
            public void run() {
                while(true) {
                    if (stop) {
                        break;
                    }
                    out.pidWrite(vars[index]);
                    try {
                        Thread.sleep(THREAD_DELAY);
                    } catch (Exception e) {
                        
                    }
                }
            }
        };
    }
    
    public void run() {
        for (int i = 0; i < instances; i++) {
            thrs[i] = new Thread(runners[i]);
            thrs[i].start();
        }
    }
    
    public void stop() {
        stop = true;
    }
}
