package recomonb.playground;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import org.hyperic.sigar.*;

import recomonb.Monitoring;

public class CPUFrame
{
  JFrame frame;
  Monitoring cpuMon;
  
  public CPUFrame()
  {
    frame = new JFrame();
    cpuMon = new Monitoring();
    cpuBars = new HashMap<String, JProgressBar>();
  }
  
  public static void main(String[] args) throws SigarException
  {
    new CPUFrame().show();    
  }

  Map<String, JProgressBar> cpuBars;
  private void show() throws SigarException
  {
    final CpuPerc[] cpus = cpuMon.getCPUs();
    JPanel pnl = new JPanel();
    pnl.setLayout(new GridLayout(cpus.length, 2));
    
    
    for(int i = 0; i < cpus.length; i++)
    {
      JProgressBar indicator = new JProgressBar(0, 100);
      String cpuID = "CPU:" + i;
      pnl.add(new JLabel(cpuID, JLabel.RIGHT));   
      pnl.add(indicator);   
      cpuBars.put(cpuID, indicator);
    }
    
    
    SwingWorker<CpuPerc[], Void> worker = new SwingWorker<CpuPerc[], Void>() {
      
      
//      @Override
//      protected void process(List<String> chunks){
//        for(String message : chunks){
//          System.out.println(message);
//        }
//      }
      
      @Override
      public CpuPerc[] doInBackground() throws InterruptedException, SigarException {
        boolean work = true;
        CpuPerc[] cpuuuu = null;
        while(work)
        {
          cpuuuu = cpuMon.getCPUs();
          for(int i = 0; i < cpuuuu.length; i++)
          {
            final CpuPerc oneCPU = cpuuuu[i];
            final String key = "CPU:" + i;
            final int cpuValue = (int)(oneCPU.getCombined()*100);
            System.out.println(cpuValue);
            SwingUtilities.invokeLater(new Runnable()
            {
              
              @Override
              public void run()
              {
                cpuBars.get(key).setValue(cpuValue);    
              }
            });
                        
            //System.out.println("bip:[" + (int)oneCPU.getCombined()*100 + "]");
          }
          System.out.println("=====");
          Thread.sleep(1000);
        }
        return cpuuuu;        
      }

      @Override
      public void done() {
         
      }
  };
    
    worker.execute();
    
    frame.setTitle("CPU Monitoring");
    frame.getContentPane().add(pnl);    
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setBounds(200, 200, 600, 200);
    frame.setVisible(true);
  }
}
