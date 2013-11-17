package recomonb;

import java.io.IOException;

import org.hyperic.sigar.SigarException;

import com.codeminders.hidapi.HIDDevice;
import com.codeminders.hidapi.HIDManager;

public class ReCoMonBUpdater
{
  private HIDDevice cpuMeter;

  public void connect() throws IOException, SigarException, InterruptedException
  {
    boolean b = com.codeminders.hidapi.ClassPathLibraryLoader.loadNativeHIDLibrary();
    System.out.println("loadNativeHIDLibrary:[" + b + "]");

    HIDManager hm = HIDManager.getInstance();

    
    cpuMeter = hm.openById(2341, 28673, null);
    if (cpuMeter == null)
    {
      throw new RuntimeException("Cannot connect to device");
    }
    System.out.println("Device connected");
  }
  
  public synchronized void pushData(byte[] data) throws IOException
  {
    //long start = System.nanoTime();
    int xx = cpuMeter.write(data);
    //long end = System.nanoTime();
    dumpWriteData(data, xx);

    byte[] read = new byte[12];
    int xx1 = cpuMeter.read(read);
    dumpReadData(read, xx1);
    for (int i = 0; i < 12; i++)
    {
      if (read[i] != data[i + 1])
      {
        throw new RuntimeException("Mismatch while reading back on element:[" + i + "], expexted:[" + data[i + 1] + "], got:[" + read[i] + "]");
      }
    }
  }
  
  boolean debug = false;
  private void dumpReadData(byte[] read, int xx1)
  {
    if(!debug)
    {
      return;
    }
    
    System.out.println("r:" + xx1);
    System.out.print("X|");
    for (byte oneByte : read)
    {
      System.out.print(oneByte + "|");
    }
    System.out.println();
  }

  private void dumpWriteData(byte[] data, int xx)
  {
    if(!debug)
    {
      return;
    }
    
    System.out.println("w:" + xx);
    for (byte oneByte : data)
    {
      System.out.print(oneByte + "|");
    }
    System.out.println();
  }
}
