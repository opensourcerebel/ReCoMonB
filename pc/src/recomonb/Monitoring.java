package recomonb;

import org.hyperic.sigar.*;
import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.NetInfo;
import org.hyperic.sigar.cmd.*;
/**
 * Display cpu information for each cpu found on the system.
 */
public class Monitoring extends SigarCommandBase {

    public boolean displayTimes = true;
    
    public Monitoring(Shell shell) {
        super(shell);
    }

    public Monitoring() {
        super();
    }

    public String getUsageShort() {
        return "Display cpu information";
    }

    private void output(CpuPerc cpu) {
        println("User Time....." + CpuPerc.format(cpu.getUser()));
        println("Sys Time......" + CpuPerc.format(cpu.getSys()));
        println("Idle Time....." + CpuPerc.format(cpu.getIdle()));
        println("Wait Time....." + CpuPerc.format(cpu.getWait()));
        println("Nice Time....." + CpuPerc.format(cpu.getNice()));
        println("Combined......" + CpuPerc.format(cpu.getCombined()));
        println("Irq Time......" + CpuPerc.format(cpu.getIrq()));
        if (SigarLoader.IS_LINUX) {
            println("SoftIrq Time.." + CpuPerc.format(cpu.getSoftIrq()));
            println("Stolen Time...." + CpuPerc.format(cpu.getStolen()));
        }
        println("");
    }

    public void output(String[] args) throws SigarException {
        CpuInfo[] infos =
            this.sigar.getCpuInfoList();

        CpuPerc[] cpus =
            this.sigar.getCpuPercList();

        CpuInfo info = infos[0];
        long cacheSize = info.getCacheSize();
        println("Vendor........." + info.getVendor());
        println("Model.........." + info.getModel());
        println("Mhz............" + info.getMhz());
        println("Total CPUs....." + info.getTotalCores());
        if ((info.getTotalCores() != info.getTotalSockets()) ||
            (info.getCoresPerSocket() > info.getTotalCores()))
        {
            println("Physical CPUs.." + info.getTotalSockets());
            println("Cores per CPU.." + info.getCoresPerSocket());
        }

        if (cacheSize != Sigar.FIELD_NOTIMPL) {
            println("Cache size...." + cacheSize);
        }
        println("");

        if (!this.displayTimes) {
            return;
        }

        for (int i=0; i<cpus.length; i++) {
            println("CPU " + i + ".........");
            output(cpus[i]);
        }

        println("Totals........");
        output(this.sigar.getCpuPerc());
    }

    public static void main(String[] args) throws Exception {
        new Monitoring().processCommand(args);
    }

    public CpuInfo[] getProcessorsInfo() throws SigarException
    {
       return this.sigar.getCpuInfoList();
    }

    public CpuPerc[] getCPUs() throws SigarException
    {
      return this.sigar.getCpuPercList();
    }
    
    public CpuPerc getCPU() throws SigarException
    {
      return this.sigar.getCpuPerc();
    }

    public Mem memory() throws SigarException 
    {
      return this.sigar.getMem();     
    }
    
    public NetInfo network() throws SigarException 
    {
      return this.sigar.getNetInfo();     
    }
    
    public Sigar getSigar()
    {
      return this.sigar;
    }
}
