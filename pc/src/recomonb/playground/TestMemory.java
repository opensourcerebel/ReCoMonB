package recomonb.playground;

import org.hyperic.sigar.Mem;
import org.hyperic.sigar.SigarException;

import recomonb.Monitoring;

public class TestMemory
{
  public static void main(String[] args) throws SigarException
  {
    Monitoring cpuMon = new Monitoring();
    Mem mem = cpuMon.memory();
    System.out.println("fp:" + (100-mem.getFreePercent()));
    System.out.println("fp:" + (mem.getActualUsed()/1024d/1024d/1024d));
    System.out.println("fp:" + (mem.getTotal()/1024d/1024d/1024d));
    
//    double total = 1024.0 * 1024.0 *1024.0;
//    System.out.println("t:" + Math.round(mem.getTotal() / total) + "/" + mem.getActualUsed()/ total);
//    System.out.println("u:" + mem.getUsed()/ total);
//    System.out.println("f:" + mem.getFree()/ total);
//    System.out.println("fp:" + mem.getFreePercent());
//    System.out.println("au:" + mem.getActualUsed()/ total);
//    System.out.println("af:" + mem.getActualFree());
  }
}
