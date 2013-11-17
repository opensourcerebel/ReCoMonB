package recomonb.test;

import static org.junit.Assert.*;

import org.junit.Test;

import recomonb.ComputerMonitoring;

public class CPUMonTest
{
  @Test
  public void test1CPU() throws Exception
  {
    ComputerMonitoring cpuMon = new ComputerMonitoring();
    byte[] result = cpuMon.getByteArray(new double[]{0.7}, 0.2);
    assertArrayEquals(new byte[]{0, 0, 60, 20, 0, 70,70, 70, 70, 70, 70, 70, 70,}, result);
  }
  
  @Test
  public void test2CPU() throws Exception
  {
    ComputerMonitoring cpuMon = new ComputerMonitoring();
    byte[] result = cpuMon.getByteArray(new double[]{0.1, 0.2}, 0.9);
    assertArrayEquals(new byte[]{0, 0, 60, 90, 0, 10,10, 10, 10, 20, 20, 20, 20,}, result);
  }
  
  @Test
  public void test4CPU() throws Exception
  {
    ComputerMonitoring cpuMon = new ComputerMonitoring();
    byte[] result = cpuMon.getByteArray(new double[]{0.1, 0.2, 0.3, 0.4}, 0.9);
    assertArrayEquals(new byte[]{0, 0, 60, 90, 0, 10,10, 20, 20, 30, 30, 40, 40,}, result);
  }
  
  @Test
  public void test8CPU() throws Exception
  {
    ComputerMonitoring cpuMon = new ComputerMonitoring();
    byte[] result = cpuMon.getByteArray(new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7}, 0.2);
    assertArrayEquals(new byte[]{0, 0, 60, 20, 0, 0, 10, 20, 30, 40, 50, 60, 70,}, result);
  }

  @Test
  public void test1CPUNew() throws Exception
  {
    ComputerMonitoring cpuMon = new ComputerMonitoring();
    byte[] result = cpuMon.getByteArrayNew(new double[]{0.7}, 0.2);
    assertArrayEquals(new byte[]{0, 0, 60, 20, 0, 70,70, 70, 70, 0, 0, 0, 0,}, result);
  }
  
  @Test
  public void test2CPUNew() throws Exception
  {
    ComputerMonitoring cpuMon = new ComputerMonitoring();
    byte[] result = cpuMon.getByteArrayNew(new double[]{0.1, 0.2}, 0.9);
    assertArrayEquals(new byte[]{0, 0, 60, 90, 0, 10,10, 20, 20, 0, 0, 0, 0,}, result);
  }
  
  @Test
  public void test4CPUNew() throws Exception
  {
    ComputerMonitoring cpuMon = new ComputerMonitoring();
    byte[] result = cpuMon.getByteArrayNew(new double[]{0.1, 0.2, 0.3, 0.4}, 0.9);
    assertArrayEquals(new byte[]{0, 0, 60, 90, 0, 10,20, 30, 40, 0, 0, 0, 0,}, result);
  }  
}
