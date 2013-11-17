package recomonb;

public class CColor
{
  public static final byte BLACK = 0x0;
  public static final byte RED = 0x1;
  public static final byte GREEN = 0x2;
  public static final byte YELLOW = 0x3;
  public static final byte BLUE = 0x4;
  public static final byte MAGENTA = 0x5;
  public static final byte CYAN = 0x6;
  public static final byte WHITE = 0x7;
  
  public static final byte addSecondColor(byte color, byte holder)
  {
    holder =  (byte) (holder << 4);    
    holder |= color;
    return holder;
  }

  public static final byte addFirstColor(byte color)
  {
    byte c = 0;
    c |= color;
    return c;
  }
  
  public static final  byte[] compressData(byte[] data)
  {
    byte compressedData[] = new byte[4];
    int cnt = 0;
    for(int i = 0; i < data.length;i=i+2)
    {
      compressedData[cnt] = CColor.addFirstColor(data[i]);
      compressedData[cnt] = CColor.addSecondColor(data[i+1], compressedData[cnt]);
      cnt++;
    }
    return compressedData;
  }

  public static void fillData(byte color, int start, int lenght, byte []data)
  {
    for(int i = start; i < lenght; i++)
    {
      data[i] = color;
    }
  }
}
