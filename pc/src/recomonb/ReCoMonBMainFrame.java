package recomonb;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import org.hyperic.sigar.*;

public class ReCoMonBMainFrame implements ChangeListener, ConfigDataProvider, MouseListener, ActionListener, WindowListener
{
  private JFrame frame;
  private Monitoring cpuMon;
  private JSlider sliderAnalogGaugeLight;
  private JSlider sliderLedMatrixLight;
  private JSlider sliderUpdateSpeed;
  private ReCoMonBUpdater cpuGadgedUpdater;
  private ReCoMonBDataProvider cpuGadgetDataProvider;
  private JButton hideBtn;
  private JButton eventBtn;
  private JButton event2Btn;
  private JButton exitBtn;
  private JProgressBar cpuLoadIndicator;
  private JProgressBar cpuLoadDistributionIndicator;
  private JProgressBar memoryIndicator;

  private JProgressBar hddSpaceIndicator;
  private JProgressBar hddReadIndicator;
  private JProgressBar hddWriteIndicator;
  private JProgressBar downloadIndicator;
  private JProgressBar uploadIndicator;

  public ReCoMonBMainFrame()
  {
    frame = new JFrame();
    cpuMon = new Monitoring();
    cpuBars = new HashMap<String, JProgressBar>();
    cpuGadgedUpdater = new ReCoMonBUpdater();

    cpuLoadIndicator = getSmartProgressBar();
    cpuLoadDistributionIndicator = getSmartProgressBar();
    memoryIndicator = getSmartProgressBar();

    hddSpaceIndicator = getSmartProgressBar();
    hddReadIndicator = getSmartProgressBar();
    hddWriteIndicator = getSmartProgressBar();
    downloadIndicator = getSmartProgressBar();
    uploadIndicator = getSmartProgressBar();
    MeasurementDataHolder netDownloadDataHolder = new MeasurementDataHolder();
    netDownloadDataHolder.setLimit(20);

    MeasurementDataHolder netUploadDataHolder = new MeasurementDataHolder();
    netUploadDataHolder.setLimit(20);

    MeasurementDataHolder hddReadDataHodler = new MeasurementDataHolder();
    hddReadDataHodler.setLimit(20);

    MeasurementDataHolder hddWriteDataHolder = new MeasurementDataHolder();
    hddWriteDataHolder.setLimit(20);

    ReCoMonBLoadDataProvider cpuLoadDataProvider = new ReCoMonBLoadDataProvider();
    cpuLoadDataProvider.setCpuMonitoring(cpuMon);
    cpuLoadDataProvider.setNetworkData(new NetworkData(new Sigar()));

    cpuGadgetDataProvider = new ReCoMonBDataProvider();
    cpuGadgetDataProvider.setCPULoadDataProvider(cpuLoadDataProvider);
    cpuGadgetDataProvider.setConfigDataProvider(this);
    cpuGadgetDataProvider.setNetDownloadDataHolder(netDownloadDataHolder);
    cpuGadgetDataProvider.setNetUploadDataHolder(netUploadDataHolder);
    cpuGadgetDataProvider.setHddReadDataHolder(hddReadDataHodler);
    cpuGadgetDataProvider.setHddWriteDataHolder(hddWriteDataHolder);
  }

  public static void main(String[] args) throws SigarException, IOException, InterruptedException
  {
    new ReCoMonBMainFrame().show();
  }

  private Map<String, JProgressBar> cpuBars;
  private int sleepTime = 3000;
  private TrayIcon trayIcon;

  protected static Image createImage(String path, String description)
  {
    URL imageURL = ReCoMonBMainFrame.class.getResource(path);

    if (imageURL == null)
    {
      System.err.println("Resource not found: " + path);
      return null;
    }
    else
    {
      return (new ImageIcon(imageURL, description)).getImage();
    }
  }

  private void show() throws SigarException, IOException, InterruptedException
  {
//    try
//    {
//      cpuGadgedUpdater.connect();
//    }
//    catch (Exception x)
//    {
//      System.err.println("Cannot connect to device");
//      x.printStackTrace();
//    }

    prepareTray();

    final CpuPerc[] cpus = cpuMon.getCPUs();
    JPanel pnl = new JPanel(new BorderLayout());

    JPanel pnlProgressHolder = new JPanel();
    pnlProgressHolder.setLayout(new GridLayout(1, cpus.length));
    JPanel pnlLabelHolder = new JPanel();
    pnlLabelHolder.setLayout(new GridLayout(1, cpus.length));

    for (int i = 0; i < cpus.length; i++)
    {
      JProgressBar indicator = getSmartProgressBar();
      indicator.setForeground(Color.green);
      indicator.setBackground(Color.black);
      indicator.setMinimum(0);
      indicator.setMaximum(100);
      indicator.setOrientation(JProgressBar.VERTICAL);
      String cpuID = "CPU:" + i;
      indicator.setString(cpuID);
      indicator.setStringPainted(true);

      pnlProgressHolder.add(indicator);
      cpuBars.put(cpuID, indicator);
    }

    // for (int i = 0; i < cpus.length; i++)
    // {
    // String cpuID = "CPU:" + i;
    // pnlLabelHolder.add(new JLabel(cpuID, JLabel.RIGHT));
    // }
    pnl.add(pnlLabelHolder, BorderLayout.SOUTH);
    pnl.add(pnlProgressHolder, BorderLayout.CENTER);

    SwingWorker<CpuPerc[], Void> worker = new SwingWorker<CpuPerc[], Void>()
    {
      @Override
      public CpuPerc[] doInBackground() throws InterruptedException, SigarException, IOException
      {
        boolean work = true;
        boolean connected = false;
        byte[] funData = new byte[]
            { 0, 0, 0, 0, 0,
            0x00, 0x00, 0x00, 0x00,//1
            0x07, 0x77, 0x77, 0x70,//2
            0x00, 0x00, 0x07, 0x00,//3
            0x00, 0x00, 0x70, 0x00,//4
            0x00, 0x00, 0x70, 0x00,//5
            0x00, 0x00, 0x07, 0x00,//6
            0x07, 0x77, 0x77, 0x70,//7
            0x00, 0x00, 0x00, 0x00,//8
            };
        
        int cnt = 0;
        boolean back = false;
        while (work)
        {
          try
          {
            byte[] cpuData = cpuGadgetDataProvider.getCpuData();
            if(cnt >= 9)
            {
//              back = true;
//              cnt = 8; 
              back = false;
              cnt = 0;
            }
            if(cnt < 0)
            {
              back = false;
              cnt = 0;
            }
            
            //System.out.println(cnt);
            //cpuData = getTestData2(cnt);
            
            if(back)
            {
              cnt--;
            }
            else
            {
              cnt++;
            }
            
            if(eventTest)
            {
              cpuData = funData;
            }
           // cpuGadgetDataProvider.getCpuData();
            try
            {
              if (!connected)
              {
                cpuGadgedUpdater.connect();
                cpuGadgedUpdater.pushData(cpuData);
                connected = true;
              }
              else
              {
                cpuGadgedUpdater.pushData(cpuData);
              }
            }
            catch (Exception e)
            {
              connected = false;
              if(e instanceof com.codeminders.hidapi.HIDDeviceNotFoundException)
              {
                System.out.println("Device not connected");  
              }
              else if (e.getMessage().equals("The device is not connected."))
              {
                System.out.println("Device unplugged");  
              }
              else
              {
                e.printStackTrace();
              }
            }

            SwingUtilities.invokeLater(new Runnable()
            {
              @Override
              public void run()
              {
                // CpuPerc[] cpuuuu = cpuMon.getCPUs();
                double[] lastLoad = cpuGadgetDataProvider.getLastAllCoresLoads();
                for (int i = 0; i < lastLoad.length; i++)
                {
                  // final CpuPerc oneCPU = cpuuuu[i];
                  final String key = "CPU:" + i;
                  byte load = (byte) (lastLoad[i] * 100);
                  // final int cpuValue = (int) (load * 100);

                  // System.out.println(key + "->" + load + "..." + load);
                  cpuBars.get(key).setValue(load);
                }
                // System.out.println("//////////////////////");
              }
            });

            try
            {
              cpuLoadIndicator.setValue(cpuGadgetDataProvider.getCPULoadPercent());
              cpuLoadDistributionIndicator.setValue(cpuGadgetDataProvider.getCPULoadDistributionPercent());
              memoryIndicator.setValue(cpuGadgetDataProvider.getUsedMemory());
              setHddData();
              setNetworkData();
              //System.out.println("-----------------------------------------");
            }
            catch (Exception x)
            {
              x.printStackTrace();
            }

            Thread.sleep(sleepTime);
          }
          catch (Throwable t)
          {
            System.err.println("General error!");
            t.printStackTrace();
          }
        }
        
        return null;
      }

      @Override
      public void done()
      {

      }
    };

    worker.execute();

    sliderAnalogGaugeLight = new JSlider(0, 100);
    sliderAnalogGaugeLight.setPaintLabels(true);
    sliderAnalogGaugeLight.setPaintTicks(true);
    sliderAnalogGaugeLight.setValue(60);
    sliderAnalogGaugeLight.setMajorTickSpacing(10);
    sliderAnalogGaugeLight.addChangeListener(this);

    sliderUpdateSpeed = new JSlider(0, 6000);
    sliderUpdateSpeed.setPaintLabels(true);
    sliderUpdateSpeed.setPaintTicks(true);
    sliderUpdateSpeed.setValue(sleepTime);
    sliderUpdateSpeed.setMajorTickSpacing(1000);
    sliderUpdateSpeed.addChangeListener(this);

    sliderLedMatrixLight = new JSlider(0, 100);
    sliderLedMatrixLight.setPaintLabels(true);
    sliderLedMatrixLight.setPaintTicks(true);
    sliderLedMatrixLight.setValue(70);
    sliderLedMatrixLight.setMajorTickSpacing(10);
    sliderLedMatrixLight.addChangeListener(this);

    JPanel slidersPanelControls = new JPanel(new GridLayout(1, 1));
    slidersPanelControls.add(sliderUpdateSpeed);
    //slidersPanelControls.add(sliderAnalogGaugeLight); 
    //slidersPanelControls.add(sliderLedMatrixLight);

    JPanel slidersPanelLabels = new JPanel(new GridLayout(1, 1));
    slidersPanelLabels.add(new JLabel("Update Speed (mili seconds)"));
    //slidersPanelLabels.add(new JLabel("Analog gauge brightness"));
    //slidersPanelLabels.add(new JLabel("LED Matrix brightness"));

    JPanel slidersPanel = new JPanel(new BorderLayout());
    slidersPanel.add(slidersPanelLabels, BorderLayout.WEST);
    slidersPanel.add(slidersPanelControls, BorderLayout.CENTER);
    // slidersPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

    hideBtn = new JButton("Hide");
    hideBtn.addActionListener(this);
    exitBtn = new JButton("Exit");
    exitBtn.addActionListener(this);
    eventBtn = new JButton("EventM");
    eventBtn.addActionListener(this);
    event2Btn = new JButton("EventB");
    event2Btn.addActionListener(this);
    
    JPanel btnsPnl = new JPanel(new GridLayout(1, 2));
    btnsPnl.add(hideBtn);
    btnsPnl.add(eventBtn);
    btnsPnl.add(event2Btn);
    btnsPnl.add(exitBtn);

    cpuLoadIndicator.setOrientation(JProgressBar.VERTICAL);
    cpuLoadDistributionIndicator.setOrientation(JProgressBar.VERTICAL);
    memoryIndicator.setOrientation(JProgressBar.VERTICAL);

    hddSpaceIndicator.setOrientation(JProgressBar.VERTICAL);
    hddReadIndicator.setOrientation(JProgressBar.VERTICAL);
    hddWriteIndicator.setOrientation(JProgressBar.VERTICAL);

    uploadIndicator.setOrientation(JProgressBar.VERTICAL);
    downloadIndicator.setOrientation(JProgressBar.VERTICAL);

    JPanel additionalIndicatorsPnl = new JPanel(new GridLayout(1, 3));
    additionalIndicatorsPnl.add(cpuLoadIndicator);
    additionalIndicatorsPnl.add(cpuLoadDistributionIndicator);
    additionalIndicatorsPnl.add(memoryIndicator);

    additionalIndicatorsPnl.add(hddReadIndicator);
    additionalIndicatorsPnl.add(hddWriteIndicator);
    additionalIndicatorsPnl.add(hddSpaceIndicator);

    additionalIndicatorsPnl.add(uploadIndicator);
    additionalIndicatorsPnl.add(downloadIndicator);

    cpuLoadIndicator.setForeground(Color.white);
    cpuLoadIndicator.setBackground(Color.black);
    cpuLoadIndicator.setMinimum(0);
    cpuLoadIndicator.setMaximum(100);
    cpuLoadDistributionIndicator.setString("CPU");
    cpuLoadDistributionIndicator.setStringPainted(true);

    cpuLoadDistributionIndicator.setForeground(Color.white);
    cpuLoadDistributionIndicator.setBackground(Color.red);
    cpuLoadDistributionIndicator.setMinimum(0);
    cpuLoadDistributionIndicator.setMaximum(100);
    cpuLoadDistributionIndicator.setString("USR");
    cpuLoadDistributionIndicator.setStringPainted(true);

    uploadIndicator.setString("UP");
    uploadIndicator.setStringPainted(true);

    downloadIndicator.setString("DOWN");
    downloadIndicator.setStringPainted(true);

    memoryIndicator.setString("MEM");
    memoryIndicator.setStringPainted(true);

    hddSpaceIndicator.setForeground(Color.orange);
    hddSpaceIndicator.setBackground(Color.green);
    hddSpaceIndicator.setMinimum(0);
    hddSpaceIndicator.setMaximum(100);
    hddSpaceIndicator.setString("HDD");
    hddSpaceIndicator.setStringPainted(true);

    hddReadIndicator.setForeground(Color.blue);
    hddReadIndicator.setBackground(Color.black);
    hddReadIndicator.setString("RD");
    hddReadIndicator.setStringPainted(true);
    
    hddWriteIndicator.setForeground(Color.yellow);
    hddWriteIndicator.setBackground(Color.black);
    hddWriteIndicator.setString("WR");
    hddWriteIndicator.setStringPainted(true);

    memoryIndicator.setForeground(Color.red);
    memoryIndicator.setBackground(Color.green);
    memoryIndicator.setMinimum(0);
    memoryIndicator.setMaximum(cpuGadgetDataProvider.getTotalMemory());

    downloadIndicator.setForeground(Color.magenta);
    downloadIndicator.setBackground(Color.black);

    uploadIndicator.setForeground(Color.CYAN);
    uploadIndicator.setBackground(Color.black);

    JPanel mainIndicatorPanel = new JPanel(new BorderLayout());
    // mainIndicatorPanel.add(pnl, BorderLayout.CENTER);
    mainIndicatorPanel.add(additionalIndicatorsPnl, BorderLayout.CENTER);

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(mainIndicatorPanel, BorderLayout.NORTH);
    mainPanel.add(slidersPanel, BorderLayout.CENTER);
    mainPanel.add(btnsPnl, BorderLayout.SOUTH);
    mainPanel.setPreferredSize(new Dimension(600, 300));

    frame.setTitle("Real Computer Monitoring Block");
    frame.setIconImage(createImage("/images/bulb.gif", "tray icon"));
    frame.getContentPane().add(mainPanel);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.addWindowListener(this);
    // frame.setBounds(200, 200, 600, 300);

    frame.setVisible(true);
    frame.pack();

    // |MAX79| 1-G 2-F 3-d1 4-DP 5-d3 6-E 7-C 8-d0
    // |MAX79| 1-d4 2-d6 3-A 4-B 5-d7 6-D 7-d5 8-d2

    // |MAX79| 3-d1 5-d3 8-d0
    // |MAX79| 1-d4 2-d6 5-d7 7-d5 8-d2

  }

  protected byte[] getTestData(int cnt)
  {
    if(cnt == 0)
    {
    return new byte[]
        { 0, 0, 0, 0, 0,
        0x00, 0x00, 0x00, 0x00,//1
        0x00, 0x00, 0x00, 0x00,//1
        0x00, 0x00, 0x00, 0x00,//1
        0x00, 0x00, 0x00, 0x00,//1
        0x00, 0x00, 0x00, 0x00,//1
        0x00, 0x00, 0x00, 0x00,//1
        0x00, 0x00, 0x00, 0x00,//1
        0x00, 0x00, 0x00, 0x00,//1
        };
    }
    else if(cnt == 1)
    {
      return new byte[]
          { 0, 0, 0, 0, 0,
          0x10, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          };
    }
    else if(cnt == 2)
    {
      return new byte[]
          { 0, 0, 0, 0, 0,
          0x11, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          };
    }
    else if(cnt == 3)
    {
      return new byte[]
          { 0, 0, 0, 0, 0,
          0x11, 0x10, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          };
    }
    else if(cnt == 4)
    {
      return new byte[]
          { 0, 0, 0, 0, 0,
          0x11, 0x11, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          };
    }
    else if(cnt == 5)
    {
      return new byte[]
          { 0, 0, 0, 0, 0,
          0x11, 0x11, 0x10, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          };
    }
    else if(cnt == 6)
    {
      return new byte[]
          { 0, 0, 0, 0, 0,
          0x11, 0x11, 0x11, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          };
    }
    else if(cnt == 7)
    {
      return new byte[]
          { 0, 0, 0, 0, 0,
          0x11, 0x11, 0x11, 0x10,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x32, 0x22, 0x22, 0x22,//6
          0x00, 0x00, 0x00, 0x00,//1
          0x66, 0x66, 0x00, 0x00,//1
          };
    }
    //else if(cnt == 8)
    {
      return new byte[]
          { 0, 0, 0, 0, 0,
          0x11, 0x11, 0x11, 0x11,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x32, 0x22, 0x22, 0x22,//6
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          };
    }
  }
  protected byte[] getTestData1(int cnt)
  {
    if(cnt == 0)
    {
      return new byte[]
          { 0, 0, 0, 0, 0,
          0x00, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x11, 0x22, 0x22, 0x22,//3
          0x00, 0x00, 0x00, 0x00,//1
          0x55, 0x55, 0x55, 0x00,//5
          0x32, 0x22, 0x22, 0x22,//6
          0x00, 0x00, 0x00, 0x00,//1
          0x66, 0x66, 0x66, 0x66,//1
          };
    }
    else if(cnt == 1)
    {
      return new byte[]
          { 0, 0, 0, 0, 0,
          0x60, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x11, 0x22, 0x22, 0x22,//3
          0x00, 0x00, 0x00, 0x00,//1
          0x55, 0x55, 0x55, 0x00,//5
          0x32, 0x22, 0x22, 0x22,//6
          0x00, 0x00, 0x00, 0x00,//1
          0x60, 0x00, 0x00, 0x00,//1
          };
    }
    else if(cnt == 2)
    {
      return new byte[]
          { 0, 0, 0, 0, 0,
          0x66, 0x00, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x11, 0x22, 0x22, 0x22,//3
          0x00, 0x00, 0x00, 0x00,//1
          0x55, 0x55, 0x55, 0x00,//5
          0x32, 0x22, 0x22, 0x22,//6
          0x00, 0x00, 0x00, 0x00,//1
          0x66, 0x00, 0x00, 0x00,//1
          };
    }
    else if(cnt == 3)
    {
      return new byte[]
          { 0, 0, 0, 0, 0,
          0x66, 0x60, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x11, 0x22, 0x22, 0x22,//3
          0x00, 0x00, 0x00, 0x00,//1
          0x55, 0x55, 0x55, 0x00,//5
          0x32, 0x22, 0x22, 0x22,//6
          0x00, 0x00, 0x00, 0x00,//1
          0x66, 0x60, 0x00, 0x00,//1
          };
    }
    else if(cnt == 4)
    {
      return new byte[]
          { 0, 0, 0, 0, 0,
          0x66, 0x66, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x11, 0x22, 0x22, 0x22,//3
          0x00, 0x00, 0x00, 0x00,//1
          0x55, 0x55, 0x55, 0x00,//5
          0x32, 0x22, 0x22, 0x22,//6
          0x00, 0x00, 0x00, 0x00,//1
          0x66, 0x66, 0x00, 0x00,//1
          };
    }
    else if(cnt == 5)
    {
      return new byte[]
          { 0, 0, 0, 0, 0,
          0x66, 0x66, 0x60, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x11, 0x22, 0x22, 0x22,//3
          0x00, 0x00, 0x00, 0x00,//1
          0x55, 0x55, 0x55, 0x00,//5
          0x32, 0x22, 0x22, 0x22,//6
          0x00, 0x00, 0x00, 0x00,//1
          0x66, 0x66, 0x60, 0x00,//1
          };
    }
    else if(cnt == 6)
    {
      return new byte[]
          { 0, 0, 0, 0, 0,
          0x66, 0x66, 0x66, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x11, 0x22, 0x22, 0x22,//3
          0x00, 0x00, 0x00, 0x00,//1
          0x55, 0x55, 0x55, 0x00,//5
          0x32, 0x22, 0x22, 0x22,//6
          0x00, 0x00, 0x00, 0x00,//1
          0x66, 0x66, 0x66, 0x00,//1
          };
    }
    else if(cnt == 7)
    {
      return new byte[]
          { 0, 0, 0, 0, 0,
          0x66, 0x66, 0x66, 0x60,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x11, 0x22, 0x22, 0x22,//3
          0x00, 0x00, 0x00, 0x00,//1
          0x55, 0x55, 0x55, 0x00,//5
          0x32, 0x22, 0x22, 0x22,//6
          0x00, 0x00, 0x00, 0x00,//1
          0x66, 0x66, 0x66, 0x60,//1
          };
    }
    //else if(cnt == 8)
    {
      return new byte[]
          { 0, 0, 0, 0, 0,
          0x66, 0x66, 0x66, 0x66,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x11, 0x22, 0x22, 0x22,//3
          0x00, 0x00, 0x00, 0x00,//1
          0x55, 0x55, 0x55, 0x00,//5
          0x32, 0x22, 0x22, 0x22,//6
          0x00, 0x00, 0x00, 0x00,//1
          0x66, 0x66, 0x66, 0x66,//1
          };
    }
  }
  
  boolean bbb = false;
  private boolean eventTest;
  protected byte[] getTestData2(int cnt)
  {
    bbb = !bbb;
    if(bbb)
    {
      return new byte[]
          { 0, 0, 0, 0, 0,
          0x66, 0x33, 0x00, 0x66,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x11, 0x22, 0x22, 0x22,//3
          0x00, 0x00, 0x00, 0x00,//1
          0x55, 0x55, 0x55, 0x00,//5
          0x32, 0x22, 0x22, 0x22,//6
          0x00, 0x00, 0x00, 0x00,//1
          0x66, 0x66, 0x66, 0x66,//1
          };
    }
    else
    {
      return new byte[]
          { 0, 0, 0, 0, 0,
          0x66, 0x66, 0x00, 0x00,//1
          0x00, 0x00, 0x00, 0x00,//1
          0x11, 0x22, 0x22, 0x22,//3
          0x00, 0x00, 0x00, 0x00,//1
          0x55, 0x55, 0x55, 0x00,//5
          0x32, 0x22, 0x22, 0x22,//6
          0x00, 0x00, 0x00, 0x00,//1
          0x66, 0x66, 0x00, 0x00,//1
          };
    }
  }
  

  private void setHddData()
  {
    Long[] hddData = cpuGadgetDataProvider.getLastHddData();

    int hddSpaceValue = (int) ((hddData[2] / (double) hddData[3]) * 100);

    hddSpaceIndicator.setValue(hddSpaceValue);

    int maximumHddRead = (int) cpuGadgetDataProvider.getMaximumHddRead();
    int maximumHddWrite = (int) cpuGadgetDataProvider.getMaximumHddWrite();
    
    int max = Math.max(maximumHddRead, maximumHddWrite);
    
    hddReadIndicator.setMaximum(max);
    hddWriteIndicator.setMaximum(max);

    hddReadIndicator.setValue(hddData[0].intValue());
    hddWriteIndicator.setValue(hddData[1].intValue());

    // System.out.println("hddP:" + hddSpaceValue);
    //System.out.println("R:" + hddData[0].intValue() + "/" + max);
    //System.out.println("W:" + hddData[1].intValue() + "/" + max);
    // System.out.println("R:" + hddData[0].intValue() + "@" +
    // hddReadIndicator.getValue() + "/" + hddReadIndicator.getMaximum());
    // System.out.println("W:" + hddData[1].intValue() + "@" +
    // hddWriteIndicator.getValue() + "/" + hddWriteIndicator.getMaximum());
  }

  private void setNetworkData()
  {
    Long[] nData = cpuGadgetDataProvider.getLastNetworkData();

    int maximumDownload = (int) cpuGadgetDataProvider.getMaximumDownload();
    int maximumUpload = (int) cpuGadgetDataProvider.getMaximumUpload();
    
    int max = Math.max(maximumDownload, maximumUpload);
    
    downloadIndicator.setMaximum(max);
    uploadIndicator.setMaximum(max);

    downloadIndicator.setValue(nData[0].intValue());
    uploadIndicator.setValue(nData[1].intValue());

    //System.out.println("D:" + nData[0].intValue() + "/" + max);
    //System.out.println("U:" + nData[1].intValue() + "/" + max);
    // System.out.println("D:" + downloadIndicator.getValue() + "/" +
    // downloadIndicator.getMaximum());
    // System.out.println("U:" + uploadIndicator.getValue() + "/" +
    // uploadIndicator.getMaximum());
  }

  private JProgressBar getSmartProgressBar()
  {
    return new JProgressBar()
    {
      /**
       * 
       */
      private static final long serialVersionUID = 1L;

      @Override
      public int getOrientation()
      {
        for (StackTraceElement elem : new Throwable().getStackTrace())
        {
          if (elem.getMethodName().equals("paintText") || (elem.getMethodName().equals("paintString")))
          {
            return JProgressBar.HORIZONTAL;
          }
        }
        return JProgressBar.VERTICAL;
      }
    };
  }

  private void prepareTray()
  {
    if (SystemTray.isSupported())
    {
      trayIcon = new TrayIcon(createImage("/images/bulb.png", "tray icon"));
      SystemTray tray = SystemTray.getSystemTray();

      try
      {
        tray.add(trayIcon);
        trayIcon.addMouseListener(this);
      }
      catch (AWTException e)
      {
        System.out.println("TrayIcon could not be added.");
      }
    }
  }

  // private CpuPerc[] updateUI() throws SigarException
  // {
  // SwingUtilities.invokeLater(new Runnable()
  // {
  // @Override
  // public void run()
  // {
  // try
  // {
  // CpuPerc[] cpuuuu = cpuMon.getCPUs();
  // for (int i = 0; i < cpuuuu.length; i++)
  // {
  // final CpuPerc oneCPU = cpuuuu[i];
  // final String key = "CPU:" + i;
  // double load = oneCPU.getCombined();
  // final int cpuValue = (int) (load * 100);
  //
  // System.out.println(key + "->" + cpuValue + "..." + load);
  // cpuBars.get(key).setValue(cpuValue);
  // }
  // System.out.println("//////////////////////");
  // }
  // catch (SigarException e)
  // {
  // // TODO Auto-generated catch block
  // e.printStackTrace();
  // }
  //
  // }
  // });
  // return null;
  // }

  @Override
  public void stateChanged(ChangeEvent event)
  {
    JSlider src = (JSlider) event.getSource();
    if (src == sliderAnalogGaugeLight)
    {
      SwingUtilities.invokeLater(new Runnable()
      {
        @Override
        public void run()
        {
          try
          {
            cpuGadgedUpdater.pushData(cpuGadgetDataProvider.getCpuDataConfigOnly());
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
        }
      });
    }
    else if (src == sliderLedMatrixLight)
    {
      SwingUtilities.invokeLater(new Runnable()
      {
        @Override
        public void run()
        {
          try
          {
            cpuGadgedUpdater.pushData(cpuGadgetDataProvider.getCpuDataConfigOnly());
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
        }
      });

    }
    else if (src == sliderUpdateSpeed)
    {
      sleepTime = sliderUpdateSpeed.getValue();
    }
  }

  @Override
  public byte getAnalogGaugeLight()
  {
    return (byte) sliderAnalogGaugeLight.getValue();
  }

  @Override
  public byte getMatrixGaugeLight()
  {
    return (byte) sliderLedMatrixLight.getValue();
  }

  @Override
  public void mouseClicked(MouseEvent event)
  {
    Object src = event.getSource();
    if (src == trayIcon)
    {
      if (frame.isVisible())
      {
        frame.setVisible(false);
      }
      else
      // if(frame.isVisible())
      {
        frame.setVisible(true);
        frame.setState(JFrame.NORMAL);
      }
    }
  }

  @Override
  public void mouseEntered(MouseEvent arg0)
  {
  }

  @Override
  public void mouseExited(MouseEvent arg0)
  {
  }

  @Override
  public void mousePressed(MouseEvent arg0)
  {
  }

  @Override
  public void mouseReleased(MouseEvent arg0)
  {
  }

  @Override
  public void actionPerformed(ActionEvent event)
  {
    Object src = event.getSource();
    if (src == hideBtn)
    {
      frame.setVisible(false);
    }
    else if (src == exitBtn)
    {
      System.exit(0);
    }
    else if (src == eventBtn)
    {
      pushM();
    }
    else if (src == event2Btn)
    {
      pushB();
    }
  }

  private void pushM()
  {
    //eventTest = !eventTest;
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          byte[] funData = new byte[]
              { 0, 0, 0, 0, 0,
              0x00, 0x00, 0x00, 0x00,//1
              0x07, 0x77, 0x77, 0x70,//2
              0x00, 0x00, 0x07, 0x00,//3
              0x00, 0x00, 0x70, 0x00,//4
              0x00, 0x00, 0x70, 0x00,//5
              0x00, 0x00, 0x07, 0x00,//6
              0x07, 0x77, 0x77, 0x70,//7
              0x00, 0x00, 0x00, 0x00,//8
              };
          cpuGadgedUpdater.pushData(funData);
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    });
  }
  
  private void pushB()
  {
    //eventTest = !eventTest;
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          byte[] funData = new byte[]
              { 0, 0, 0, 0, 0,
              0x00, 0x00, 0x00, 0x00,//1
              0x01, 0x11, 0x11, 0x10,//2
              0x01, 0x01, 0x10, 0x10,//3
              0x01, 0x01, 0x10, 0x10,//4
              0x00, 0x10, 0x01, 0x00,//5
              0x00, 0x00, 0x00, 0x00,//6
              0x01, 0x01, 0x11, 0x10,//7
              0x00, 0x00, 0x00, 0x00,//8
              };
          cpuGadgedUpdater.pushData(funData);
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    });
  }

  @Override
  public void windowActivated(WindowEvent arg0)
  {
  }

  @Override
  public void windowClosed(WindowEvent arg0)
  {
  }

  @Override
  public void windowClosing(WindowEvent arg0)
  {
  }

  @Override
  public void windowDeactivated(WindowEvent arg0)
  {
  }

  @Override
  public void windowDeiconified(WindowEvent arg0)
  {
  }

  @Override
  public void windowIconified(WindowEvent arg0)
  {
    frame.setVisible(false);
  }

  @Override
  public void windowOpened(WindowEvent arg0)
  {
  }
}
