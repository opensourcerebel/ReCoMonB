package recomonb.playground;
import java.io.IOException;

import com.codeminders.hidapi.HIDDevice;
import com.codeminders.hidapi.HIDManager;

public class TestUSB
{
  public static void main(String[] args) throws IOException, InterruptedException
  {
    boolean b = com.codeminders.hidapi.ClassPathLibraryLoader.loadNativeHIDLibrary();
    System.out.println(b);

    HIDManager hm = HIDManager.getInstance();
//    HIDDeviceInfo[] devices = hm.listDevices();
//    HIDDeviceInfo[] allDevices = devices;
//    for (HIDDeviceInfo oneDevice : allDevices)
//    {
//      System.out.println(oneDevice.toString());
//    }

    HIDDevice cpuMeter = hm.openById(2341, 28673, null);
    if (cpuMeter != null)
    {
      for (int i = 1; i < 10; i++)
      {
        byte configGeneral = (byte) Integer.parseInt("00000000", 2);

        byte cpuUtilization = (byte) (i * 10);

        byte configGaugeLight = 60;
        byte configLEDLight = 0;

        byte cpu0 = cpuUtilization;
        byte cpu1 = (byte) (cpuUtilization + 10);

        byte[] data = new byte[]
        //{ 1, configGeneral, configGaugeLight, cpuUtilization, configLEDLight, cpu0, cpu0, cpu0, cpu0, cpu1, cpu1, cpu1, cpu1 };
            { 0, configGeneral, configGaugeLight, cpuUtilization, configLEDLight, (byte)(cpu0+1), (byte)(cpu0+2), (byte)(cpu0+3), (byte)(cpu0+4), (byte)(cpu1+1), (byte)(cpu1+2), (byte)(cpu1+2), (byte)(cpu1+3) };

        int xx = cpuMeter.write(data);
        System.out.println("w:" + xx);
        for (byte oneByte : data)
        {
          System.out.print(oneByte + "|");
        }
        System.out.println();
        
        byte[] read = new byte[12];
        int xx1 = cpuMeter.read(read);
        System.out.println("r:" + xx1);
        System.out.print("X|");
        for (byte oneByte : read)
        {
          System.out.print(oneByte + "|");
        }
        System.out.println();
        
        Thread.sleep(1000);
      }
    }
  }
}
