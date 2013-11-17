package recomonb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hyperic.sigar.*;

public class ReCoMonBLoadDataProvider
{
  NetworkData networkData;

  public void setNetworkData(NetworkData data)
  {
    networkData = data;
  }

  Monitoring monitoring;

  public void setCpuMonitoring(Monitoring cpuMon)
  {
    this.monitoring = cpuMon;
  }

  public double[] getAllCoresLoad() throws SigarException
  {
    CpuPerc[] allCores = monitoring.getCPUs();
    double[] toRet = new double[allCores.length];
    for (int i = 0; i < allCores.length; i++)
    {
      toRet[i] = allCores[i].getCombined();
      // System.out.println("L:" + toRet[i]);
    }
    // System.out.println("*****************");

    return toRet;
  }

  public double getCPULoadCombined() throws SigarException
  {
    return monitoring.getCPU().getCombined();
  }

  public double getUsedMemoryPercent() throws SigarException
  {
    Mem mem = monitoring.memory();
    double used = (mem.getActualUsed() / 1024d / 1024d / 1024d);
    double total = (mem.getTotal() / 1024d / 1024d / 1024d);

    return used / total;
  }

  public long getUsedMemory() throws SigarException
  {
    Mem mem = monitoring.memory();

    return mem.getActualUsed();
  }

  public long getTotalMemory() throws SigarException
  {
    Mem mem = monitoring.memory();

    return mem.getTotal();
  }

  public NetworkData getNetworkData()
  {
    return networkData;
  }

  boolean first = false;
  Map<String, List<Long>> fsData = new HashMap<String, List<Long>>();

  public Long[] getHddData() throws SigarException
  {
    FileSystem[] fslist = monitoring.getSigar().getFileSystemList();
    long totalRead = 0L;
    long totalWrite = 0L;
    long totalUsed = 0L;
    long totalTotal = 0L;
    for (int i = 0; i < fslist.length; i++)
    {
      if (fslist[i].getType() == FileSystem.TYPE_LOCAL_DISK)
      {
        String dirName = fslist[i].getDirName();
        
        FileSystemUsage usage = monitoring.getSigar().getFileSystemUsage(dirName);
        long used = usage.getUsed()/1024/1024;
        long total = usage.getTotal()/1024/1024;
        totalUsed += totalUsed + used;
        totalTotal += totalTotal + total;
        //System.out.println(usage.getUsePercent());
        //System.out.println("us:" + used + "/" + total);
        
        long read = usage.getDiskReadBytes();
        long write = usage.getDiskWriteBytes();

        List<Long> rwList = fsData.get(dirName);
        if (rwList == null)
        {
          rwList = new ArrayList<Long>();
          rwList.add(read);
          rwList.add(write);
          fsData.put(dirName, rwList);
        }
        else
        {
          long readOld = rwList.get(0);
          long writeOld = rwList.get(1);

          totalRead += read - readOld;
          totalWrite += write - writeOld;
          
          rwList.set(0, read);
          rwList.set(1, write);
        }
      }
    }

    //System.out.println("tr:" + totalRead);
    //System.out.println("tw:" + totalWrite);
    //System.out.println("tu:" + totalUsed);
    //System.out.println("tt:" + totalTotal);
    return new Long[]{totalRead, totalWrite, totalUsed, totalTotal};
  }

  public double getCPULoadUser() throws SigarException
  {
    return monitoring.getCPU().getUser();
  }

  public double getCPULoadSys() throws SigarException
  {
    return monitoring.getCPU().getSys();
  }

  public CpuPerc getCPU() throws SigarException
  {
    return monitoring.getCPU();
  }
}
