package recomonb;

import java.io.IOException;

import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.SigarException;

import com.codeminders.hidapi.HIDDevice;
import com.codeminders.hidapi.HIDManager;

public class ComputerMonitoring
{
  public static void main(String[] args) throws IOException, InterruptedException, SigarException
  {
    ComputerMonitoring mon = new ComputerMonitoring();
    mon.start();
  }

//  public static boolean loadNativeCIRARLibrary()
//  {
//    boolean isHIDLibLoaded = false;
//
//    for (String path : HID_LIB_NAMES)
//    {
//      try
//      {
//        // have to use a stream
//        InputStream in = ClassPathLibraryLoader.class.getResourceAsStream(path);
//        if (in != null)
//        {
//          try
//          {
//            // always write to different location
//            String tempName = path.substring(path.lastIndexOf('/') + 1);
//            File fileOut = File.createTempFile(tempName.substring(0, tempName.lastIndexOf('.')), tempName.substring(tempName.lastIndexOf('.'), tempName.length()));
//            fileOut.deleteOnExit();
//
//            OutputStream out = new FileOutputStream(fileOut);
//            byte[] buf = new byte[1024];
//            int len;
//            while ((len = in.read(buf)) > 0)
//            {
//              out.write(buf, 0, len);
//            }
//
//            out.close();
//            Runtime.getRuntime().load(fileOut.toString());
//            isHIDLibLoaded = true;
//          } finally
//          {
//            in.close();
//          }
//        }
//      } catch (Exception e)
//      {
//        // ignore
//      } catch (UnsatisfiedLinkError e)
//      {
//        // ignore
//      }
//
//      if (isHIDLibLoaded)
//      {
//        break;
//      }
//    }
//
//    return isHIDLibLoaded;
//  }

  private void start() throws IOException, SigarException, InterruptedException
  {
    boolean b = com.codeminders.hidapi.ClassPathLibraryLoader.loadNativeHIDLibrary();
    System.out.println("loadNativeHIDLibrary:[" + b + "]");

    HIDManager hm = HIDManager.getInstance();

    Monitoring cpuMon = new Monitoring();
    
    HIDDevice cpuMeter = hm.openById(2341, 28673, null);
    if (cpuMeter != null)
    {
      // for (int i = 1; i < 10; i++)
      for (;;)
      {
        byte[] data = getByteArray(getAllCoresLoad(cpuMon), cpuMon.getCPU().getCombined());

        //long start = System.nanoTime();
        //int xx = cpuMeter.write(data);
        //long end = System.nanoTime();
        // dumpWriteData(data, xx);

        byte[] read = new byte[12];
        //int xx1 = cpuMeter.read(read);
        // dumpReadData(read, xx1);
        for (int i = 0; i < 12; i++)
        {
          if (read[i] != data[i + 1])
          {
            throw new RuntimeException("Mismatch while reading back on element:[" + i + "], expexted:[" + data[i + 1] + "], got:[" + read[i] + "]");
          }
        }

        Thread.sleep(2000);
        // System.out.println("=" + (end - start) / 1000000 + "ms");
      }
    }
  }

//  private void dumpReadData(byte[] read, int xx1)
//  {
//    System.out.println("r:" + xx1);
//    System.out.print("X|");
//    for (byte oneByte : read)
//    {
//      System.out.print(oneByte + "|");
//    }
//    System.out.println();
//  }

  private double[] getAllCoresLoad(Monitoring cpuMon) throws SigarException
  {
    CpuPerc[] allCores = cpuMon.getCPUs();
    double[] toRet = new double[allCores.length];
    for (int i = 0; i < allCores.length; i++)
    {
      toRet[i] = allCores[i].getCombined();
    }

    return toRet;
  }

  public byte[] getByteArray(double[] coresLoad, double averageLoad)
  {
    byte configGeneral = (byte) Integer.parseInt("00000000", 2);

    // byte cpuUtilization = (byte) (i * 10);
    byte cpuUtilization = (byte) (averageLoad * 100);
    // System.out.println("cpuUtilization:[" + cpuUtilization +
    // "]");

    byte configGaugeLight = 60;
    byte configLEDLight = 0;

    byte[] data = new byte[]
    { 0, configGeneral, configGaugeLight, cpuUtilization, configLEDLight, 0, 0, 0, 0, 0, 0, 0, 0, };
    int barLimit = 8 / coresLoad.length;

    for (int coreIdx = 0; coreIdx < coresLoad.length; coreIdx++)
    {
      byte cpuValue = (byte) (coresLoad[coreIdx] * 100);

      int startingPint = coreIdx * barLimit;
      for (int bar = startingPint; bar < startingPint + barLimit; bar++)
      {
        int barIdx = bar + 5;

        data[barIdx] = cpuValue;
      }
      // System.out.println("====");
    }

    return data;
  }
  
  public byte[] getByteArrayNew(double[] coresLoad, double averageLoad)
  {
    byte configGeneral = (byte) Integer.parseInt("00000000", 2);

    // byte cpuUtilization = (byte) (i * 10);
    byte cpuUtilization = (byte) (averageLoad * 100);
    // System.out.println("cpuUtilization:[" + cpuUtilization +
    // "]");

    byte configGaugeLight = 60;
    byte configLEDLight = 0;

    byte[] data = new byte[]
    { 0, configGeneral, configGaugeLight, cpuUtilization, configLEDLight, 0, 0, 0, 0, 0, 0, 0, 0, };
    int barLimit = 4 / coresLoad.length;

    for (int coreIdx = 0; coreIdx < coresLoad.length; coreIdx++)
    {
      byte cpuValue = (byte) (coresLoad[coreIdx] * 100);

      int startingPint = coreIdx * barLimit;
      for (int bar = startingPint; bar < startingPint + barLimit; bar++)
      {
        int barIdx = bar + 5;

        data[barIdx] = cpuValue;
      }
      // System.out.println("====");
    }

    return data;
  }

//  private void dumpWriteData(byte[] data, int xx)
//  {
//    System.out.println("w:" + xx);
//    for (byte oneByte : data)
//    {
//      System.out.print(oneByte + "|");
//    }
//    System.out.println();
//  }
}
