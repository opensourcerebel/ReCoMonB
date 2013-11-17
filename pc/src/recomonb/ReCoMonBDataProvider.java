package recomonb;

import org.hyperic.sigar.*;

public class ReCoMonBDataProvider
{
  private ReCoMonBLoadDataProvider loadProvider;
  private ConfigDataProvider  configDataProvider;

  public void setConfigDataProvider(ConfigDataProvider configDataProvider)
  {
    this.configDataProvider = configDataProvider;
  }

  public void setCPULoadDataProvider(ReCoMonBLoadDataProvider cpuLoadProvider)
  {
    this.loadProvider = cpuLoadProvider;
  }

  byte[]           lastData;
  private double[] lastAllCoresLoads;

  public double[] getLastAllCoresLoads()
  {
    return lastAllCoresLoads;
  }
//@formatter:off
  Long[]                lastNetworkData;
  MeasurementDataHolder downloadDataHolder;
  MeasurementDataHolder uploadDataHolder;
  MeasurementDataHolder hddReadDataHolder;
  MeasurementDataHolder hddWriteDataHolder;
  Long[]                lastHDDData = new Long[]
                                    { 0l, 0l, 0l, 0l };
  private double        cpuLoadCombined;
  private double        cpuLoadDistribution;
  // @formatter:on  
  public byte[] getCpuData() throws SigarException
  {
    lastAllCoresLoads = loadProvider.getAllCoresLoad();

    NetworkData networkData = loadProvider.getNetworkData();
    lastNetworkData = networkData.getMetric();

    double downloadPercent = getDownloadPercent();
    double uploadPercent = getUploadPercent();

    Long[] rawHDDData = loadProvider.getHddData();

    double readKBytesPercent = getHddReadPercent(rawHDDData);
    double writeKBytesPercent = getHddWritePercent(rawHDDData);
    double hddSpacePercent = (double) rawHDDData[2] / (double) rawHDDData[3];
    // System.out.println(hddSpacePercent + "/" + rawHDDData[2] + "/" +
    // rawHDDData[3]);
    lastHDDData[2] = rawHDDData[2];
    lastHDDData[3] = rawHDDData[3];

    double memoryPercent = loadProvider.getUsedMemoryPercent();

    CpuPerc mainCPU = loadProvider.getCPU();
    cpuLoadCombined = mainCPU.getCombined();
    double cpuLoadUser = mainCPU.getUser();
    cpuLoadDistribution = cpuLoadUser / cpuLoadCombined;

    // @formatter:off
    byte[] cpuBar = getLEDBar(cpuLoadCombined, CColor.WHITE, CColor.BLACK);
    byte[] cpuDistribution = new byte[4]; 
    //System.out.println(cpuLoadCombined);
    if(cpuLoadCombined >= 0.125)
    {
      cpuDistribution = getLEDBar(cpuLoadDistribution, CColor.WHITE, CColor.RED);
    }    
    byte[] memoryBay      = getLEDBar(memoryPercent,      CColor.RED,     CColor.GREEN);
    byte[] hddSpaceBar    = getLEDBar(hddSpacePercent,    CColor.YELLOW,  CColor.GREEN);
    byte[] hddReadBar     = getLEDBar(readKBytesPercent,  CColor.BLUE,    CColor.BLACK);
    byte[] hddWriteBar    = getLEDBar(writeKBytesPercent, CColor.YELLOW,  CColor.BLACK);
    byte[] netDownloadBar = getLEDBar(downloadPercent,    CColor.MAGENTA, CColor.BLACK);
    byte[] netUploadBar   = getLEDBar(uploadPercent,      CColor.CYAN,    CColor.BLACK);

    byte[] data = new byte[]
        { 0, 0, 0, 0, 0,
                cpuBar[0],         cpuBar[1],         cpuBar[2],         cpuBar[3],//1
       cpuDistribution[0],cpuDistribution[1],cpuDistribution[2],cpuDistribution[3],//2
             memoryBay[0],      memoryBay[1],      memoryBay[2],      memoryBay[3],//3
            hddReadBar[0],     hddReadBar[1],     hddReadBar[2],     hddReadBar[3],//5
           hddWriteBar[0],    hddWriteBar[1],    hddWriteBar[2],    hddWriteBar[3],//6
           hddSpaceBar[0],    hddSpaceBar[1],    hddSpaceBar[2],    hddSpaceBar[3],//4
          netUploadBar[0],   netUploadBar[1],   netUploadBar[2],   netUploadBar[3],//7
        netDownloadBar[0], netDownloadBar[1], netDownloadBar[2], netDownloadBar[3],//8
        };
    
 // @formatter:on    
    // dumpOutput(data);
    lastData = data;
    return data;
  }

//  private void dumpOutput(byte[] data)
//  {
//    int brk = 1;
//    byte[] dataVal = new byte[32];
//    System.arraycopy(data, 5, dataVal, 0, 32);
//    for (int i = 0; i < dataVal.length; i++)
//    {
//      // System.out.print("[" + brk + ":" + Integer.toHexString(dataVal[i]) +
//      // "|");
//      System.out.print(fixStr(dataVal, i) + "|");
//      if (brk % 4 == 0)
//      {
//        System.out.println();
//      }
//      brk++;
//    }
//    System.out.println();
//  }

//  private String fixStr(byte[] dataVal, int i)
//  {
//    String hexString = Integer.toHexString(dataVal[i]);
//    if (hexString.length() == 1)
//    {
//      return "0" + hexString;
//    }
//    return hexString;
//  }

  boolean debug = false;

  private byte[] getLEDBar(double dataInPercent, byte firstColor, byte secondColor)
  {
    int utilization = (int) (dataInPercent * 100);
    if (debug)
    {
      System.out.println("utilization:" + utilization);
    }
    int bars = (int) (utilization / 12.5);
    if (debug)
    {
      System.out.println("bars:" + bars);
    }
    byte[] fillData = new byte[8];
    CColor.fillData(firstColor, 0, bars, fillData);
    CColor.fillData(secondColor, bars, 8, fillData);

    if (debug)
    {
      for (int i = 0; i < fillData.length; i++)
      {
        System.out.println(i + "/" + fillData[i]);
      }
    }

    return CColor.compressData(fillData);
  }

  private double getHddWritePercent(Long[] rawHDDData)
  {
    Long writeKbytes = rawHDDData[1] / 1024;
    lastHDDData[1] = writeKbytes;
    hddWriteDataHolder.newMeasurement(writeKbytes);
    // System.out.println("ww:" + rawHDDData[1] + "/" + writeKbytes);

    double writeMax = Math.max(hddReadDataHolder.getMaximum(), hddWriteDataHolder.getMaximum());
    double writeKBytesPercent = 0;
    // System.out.println("wMax:" + writeMax);
    if (writeMax != 0)
    {
      writeKBytesPercent = writeKbytes / writeMax;
    }
    return writeKBytesPercent;
  }

  private double getHddReadPercent(Long[] rawHDDData)
  {
    Long readKbytes = rawHDDData[0] / 1024;
    lastHDDData[0] = readKbytes;
    hddReadDataHolder.newMeasurement(readKbytes);
    // System.out.println("rr:" + rawHDDData[0] + "/" + readKbytes);

    double readMax = Math.max(hddReadDataHolder.getMaximum(), hddWriteDataHolder.getMaximum());
    double readKBytesPercent = 0;
    // System.out.println("rMax:" + readMax);
    if (readMax != 0)
    {
      readKBytesPercent = readKbytes / readMax;
    }
    return readKBytesPercent;
  }

  private double getDownloadPercent()
  {
    Long download = lastNetworkData[0];
    downloadDataHolder.newMeasurement(download);

    double max = Math.max(downloadDataHolder.getMaximum(), uploadDataHolder.getMaximum());
    double downloadPercent = 0;
    if (max != 0)
    {
      downloadPercent = download / max;
    }
    // System.out.println("downloadPercent:" + downloadPercent + "");
    return downloadPercent;
  }

  private double getUploadPercent()
  {
    Long upload = lastNetworkData[1];
    uploadDataHolder.newMeasurement(upload);

    double max = Math.max(downloadDataHolder.getMaximum(), uploadDataHolder.getMaximum());
    double uploadPercent = 0;
    if (max != 0)
    {
      uploadPercent = upload / max;
    }
    // System.out.println("uploadPercent:" + uploadPercent + "");
    return uploadPercent;
  }

  // private byte[] getByteArrayNew(double[] coresLoad, double averageLoad,
  // double hddRead, double hddWrite, double download, double upload)
  // {
  // byte configGeneral = (byte) Integer.parseInt("00000000", 2);
  //
  // // byte cpuUtilization = (byte) (i * 10);
  // byte cpuUtilization = (byte) (averageLoad * 100);
  // byte hddReadAsByte = (byte) (hddRead * 100);
  // byte hddWriteAsByte = (byte) (hddWrite * 100);
  // byte downloadasByte = (byte) (download * 100);
  // byte uploadasByte = (byte) (upload * 100);
  // // System.out.println("hddReadAsByte:[" + hddReadAsByte + "]" + hddRead);
  // // System.out.println("hddWriteAsByte:[" + hddWriteAsByte + "]" +
  // hddWrite);
  //
  // byte configGaugeLight = configDataProvider.getAnalogGaugeLight();
  // byte configLEDLight = configDataProvider.getMatrixGaugeLight();
  //
  // byte[] data = new byte[]
  // // { 0, configGeneral, configGaugeLight, cpuUtilization, configLEDLight,
  // // uploadasByte, downloadasByte, memasByte,0, 0, 0, 0, 0, };
  // { 0, configGeneral, configGaugeLight, cpuUtilization, configLEDLight, 0, 0,
  // 0, 0, hddReadAsByte, hddWriteAsByte, downloadasByte, uploadasByte };
  // int barLimit = 4 / coresLoad.length;
  //
  // for (int coreIdx = 0; coreIdx < coresLoad.length; coreIdx++)
  // {
  // byte cpuValue = (byte) (coresLoad[coreIdx] * 100);
  // // System.out.println("cpuValue:" + cpuValue);
  //
  // int startingPint = coreIdx * barLimit;
  // for (int bar = startingPint; bar < startingPint + barLimit; bar++)
  // {
  // int barIdx = bar + 5;
  //
  // data[barIdx] = cpuValue;
  // }
  // // System.out.println("====");
  // }
  // // System.out.println("++++++++");
  //
  // return data;
  // }

  // private byte[] getByteArray(double[] coresLoad, double averageLoad)
  // {
  // byte configGeneral = (byte) Integer.parseInt("00000000", 2);
  //
  // // byte cpuUtilization = (byte) (i * 10);
  // byte cpuUtilization = (byte) (averageLoad * 100);
  // // System.out.println("cpuUtilization:[" + cpuUtilization +
  // // "]");
  //
  // byte configGaugeLight = configDataProvider.getAnalogGaugeLight();
  // byte configLEDLight = configDataProvider.getMatrixGaugeLight();
  //
  // byte[] data = new byte[]
  // { 0, configGeneral, configGaugeLight, cpuUtilization, configLEDLight, 0, 0,
  // 0, 0, 0, 0, 0, 0, };
  // int barLimit = 8 / coresLoad.length;
  //
  // for (int coreIdx = 0; coreIdx < coresLoad.length; coreIdx++)
  // {
  // byte cpuValue = (byte) (coresLoad[coreIdx] * 100);
  // //System.out.println("cpuValue:" + cpuValue);
  //
  // int startingPint = coreIdx * barLimit;
  // for (int bar = startingPint; bar < startingPint + barLimit; bar++)
  // {
  // int barIdx = bar + 5;
  //
  // data[barIdx] = cpuValue;
  // }
  // // System.out.println("====");
  // }
  // //System.out.println("++++++++");
  //
  // return data;
  // }

  public byte[] getCpuDataConfigOnly() throws SigarException
  {
    if (lastData != null)
    {
      return new byte[]
      { 0, 0, configDataProvider.getAnalogGaugeLight(), lastData[3], configDataProvider.getMatrixGaugeLight(), lastData[5], lastData[6], lastData[7],
          lastData[8], lastData[9], lastData[10], lastData[11], lastData[12], };
    }
    else
    {
      return getCpuData();
    }
  }

  public int getTotalMemory() throws SigarException
  {
    return (int) (loadProvider.getTotalMemory() / 1024 / 1024);
  }

  public int getUsedMemory() throws SigarException
  {
    return (int) (loadProvider.getUsedMemory() / 1024 / 1024);
  }

  public NetworkData getNetworkData()
  {
    return loadProvider.getNetworkData();
  }

  public Long[] getLastNetworkData()
  {
    return lastNetworkData;
  }

  public int getMaximumDownload()
  {
    return (int) downloadDataHolder.getMaximum();
  }

  public int getMaximumUpload()
  {
    return (int) uploadDataHolder.getMaximum();
  }

  public void setNetDownloadDataHolder(MeasurementDataHolder downloadDataHolder)
  {
    this.downloadDataHolder = downloadDataHolder;
  }

  public void setNetUploadDataHolder(MeasurementDataHolder uploadDataHolder)
  {
    this.uploadDataHolder = uploadDataHolder;
  }

  public void setHddReadDataHolder(MeasurementDataHolder hddReadDataHodler)
  {
    this.hddReadDataHolder = hddReadDataHodler;
  }

  public void setHddWriteDataHolder(MeasurementDataHolder hddWriteDataHolder)
  {
    this.hddWriteDataHolder = hddWriteDataHolder;
  }

  public Long[] getLastHddData()
  {
    return lastHDDData;
  }

  public int getMaximumHddRead()
  {
    return (int) hddReadDataHolder.getMaximum();
  }

  public int getMaximumHddWrite()
  {
    return (int) hddWriteDataHolder.getMaximum();
  }

  public int getCPULoadPercent()
  {
    return (int) (cpuLoadCombined * 100d);
  }

  public int getCPULoadDistributionPercent()
  {
    return (int) (cpuLoadDistribution * 100d);
  }
}
