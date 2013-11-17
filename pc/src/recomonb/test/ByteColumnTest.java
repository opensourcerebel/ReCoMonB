package recomonb.test;

import static org.junit.Assert.*;

import org.junit.*;

import recomonb.*;

public class ByteColumnTest
{
  @Test
  public void testCompress() throws Exception
  {
    byte data[] = {CColor.RED, CColor.GREEN, CColor.RED, CColor.BLACK, CColor.BLACK, CColor.BLACK, CColor.BLACK, CColor.BLACK};
    byte[] compressedData = CColor.compressData(data);
    
    assertEquals(compressedData[0], 0x12);
    assertEquals(compressedData[1], 0x10);
    assertEquals(compressedData[2], 0x00);
    assertEquals(compressedData[3], 0x00);
  }

  @Test
  public void testFill() throws Exception
  {
    byte[] fillData = new byte[8];
    CColor.fillData(CColor.RED, 0, 3, fillData);
    
    assertEquals(0x1, fillData[0]);
    assertEquals(0x1, fillData[1]);
    assertEquals(0x1, fillData[2]);
    assertEquals(0x0, fillData[3]);
    assertEquals(0x0, fillData[4]);
    assertEquals(0x0, fillData[5]);
    assertEquals(0x0, fillData[6]);
    assertEquals(0x0, fillData[7]);
  }
  
  @Test
  public void testFillFull() throws Exception
  {
    byte[] fillData = new byte[8];
    CColor.fillData(CColor.RED, 0, 8, fillData);
    
    assertEquals(0x1, fillData[0]);
    assertEquals(0x1, fillData[1]);
    assertEquals(0x1, fillData[2]);
    assertEquals(0x1, fillData[3]);
    assertEquals(0x1, fillData[4]);
    assertEquals(0x1, fillData[5]);
    assertEquals(0x1, fillData[6]);
    assertEquals(0x1, fillData[7]);
  }
  
  @Test
  public void testFillMixed100() throws Exception
  {
    int utilization = (int) (1.0 * 100);
    int bars = (int) (utilization / 12.5);
    byte[] fillData = new byte[8];
    
    CColor.fillData(CColor.MAGENTA, 0, bars, fillData);
    CColor.fillData(CColor.BLACK, bars, 8, fillData);
    
    assertEquals(CColor.MAGENTA, fillData[0]);
    assertEquals(CColor.MAGENTA, fillData[1]);
    assertEquals(CColor.MAGENTA, fillData[2]);
    assertEquals(CColor.MAGENTA, fillData[3]);
    assertEquals(CColor.MAGENTA, fillData[4]);
    assertEquals(CColor.MAGENTA, fillData[5]);
    assertEquals(CColor.MAGENTA, fillData[6]);
    assertEquals(CColor.MAGENTA, fillData[7]);
  }
  
  @Test
  public void testFillMixed40() throws Exception
  {
    int utilization = (int) (0.7 * 100);
    int bars = (int) (utilization / 12.5);
    byte[] fillData = new byte[8];
    System.out.println(bars);    
    
    CColor.fillData(CColor.MAGENTA, 0, bars, fillData);
    CColor.fillData(CColor.BLACK, bars, 8, fillData);
    
    assertEquals(CColor.MAGENTA, fillData[0]);
    assertEquals(CColor.MAGENTA, fillData[1]);
    assertEquals(CColor.MAGENTA, fillData[2]);
    assertEquals(CColor.MAGENTA, fillData[3]);
    assertEquals(CColor.MAGENTA, fillData[4]);
    assertEquals(CColor.BLACK, fillData[5]);
    assertEquals(CColor.BLACK, fillData[6]);
    assertEquals(CColor.BLACK, fillData[7]);
  }
}
