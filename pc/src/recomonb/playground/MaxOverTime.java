package recomonb.playground;

import static org.junit.Assert.*;

import org.junit.Test;

import recomonb.MeasurementDataHolder;

public class MaxOverTime
{
  @Test
  public void testOneValue()
  {
    MeasurementDataHolder dataHolder = new MeasurementDataHolder();
    dataHolder.newMeasurement(10L);
    
    assertEquals(10L, dataHolder.getMaximum());
  }
  
  @Test
  public void testTwoValue()
  {
    
    MeasurementDataHolder dataHolder = new MeasurementDataHolder();
    dataHolder.newMeasurement(10L);
    dataHolder.newMeasurement(15L);
    
    assertEquals(15L, dataHolder.getMaximum());
  }
  
  @Test
  public void testThreeValue()
  {
    
    MeasurementDataHolder dataHolder = new MeasurementDataHolder();
    dataHolder.setLimit(2);
    
    dataHolder.newMeasurement(150L);
    dataHolder.newMeasurement(15L);
    dataHolder.newMeasurement(10L);
    
    assertEquals(15L, dataHolder.getMaximum());
    
    dataHolder.newMeasurement(8L);
    
    assertEquals(10L, dataHolder.getMaximum());
  }
  
  @Test
  public void testFiveLimit()
  {
    
    MeasurementDataHolder dataHolder = new MeasurementDataHolder();
    dataHolder.setLimit(5);
    
    dataHolder.newMeasurement(150L);
    dataHolder.newMeasurement(15L);
    dataHolder.newMeasurement(10L);
    dataHolder.newMeasurement(10L);
    dataHolder.newMeasurement(10L);
    
    assertEquals(150L, dataHolder.getMaximum());
  }
}
