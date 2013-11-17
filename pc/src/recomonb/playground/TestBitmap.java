package recomonb.playground;

public class TestBitmap
{
  public static void main(String[] args)
  {
    char data[]  = 
       {0x12, 0x13, 0x14, 0x11,
        0x22, 0x22, 0x22, 0x22,
        0x33, 0x33, 0x33, 0x33,
        0x44, 0x44, 0x44, 0x44,
        0x55, 0x55, 0x55, 0x55,
        0x66, 0x66, 0x66, 0x66,
        0x55, 0x50, 0x00, 0x00,
        0x77, 0x77, 0x77, 0x77,};
    
    //while (true)
    {
      int x, y,c;
      c = 0;
      for (x = 1; x <= 8; x++)
      {
        for (y = 1; y <= 8; y=y+2)
        {
          byte twoPixels = (byte) data[c];
          byte first  = 0;
          byte second = 0;
          first   |= twoPixels   >> 4; //remove the last four bits and move in the same time 
          second  |= twoPixels & 0x0F; //remove the first 4 bits
              
              
          System.out.println("X:[" + x + "] Y:[" + y     + "] [" + Integer.toBinaryString(first)  +  "] cnt:" + c);
          System.out.println("X:[" + x + "] Y:[" + (y+1) + "] [" + Integer.toBinaryString(second) + "] cnt:" + c);
          c++;
        }
      }
    }

//    byte color = addFirstColor(CColor.RED);
//    color = addSecondColor(CColor.RED, color);    
//    System.out.println(Integer.toBinaryString(color));
    
    //System.out.println((int)(20 / 12.5));
  }
}
