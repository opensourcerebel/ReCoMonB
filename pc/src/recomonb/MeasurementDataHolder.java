package recomonb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MeasurementDataHolder
{
  List<Long> measurements;
  private int limit = 1;
  private long maxValue = -1;

  public MeasurementDataHolder()
  {
    measurements = new ArrayList<Long>();
  }

  public void newMeasurement(long data)
  {
    if (measurements.size() >= limit)
    {
      measurements.remove(0);
    }

    measurements.add(data);

    List<Long> temp = new ArrayList<Long>(measurements);
    Collections.sort(temp);

    maxValue = temp.get(temp.size() - 1);
  }

  public long getMaximum()
  {
    return maxValue;
  }

  public void setLimit(int limit)
  {
    this.limit = limit;
  }

}
