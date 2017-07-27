package opencryptoutils;

/**
 *
 * @author Petr Svenda
 */ 
public class PerfCodeConfig {

    public String methodName;
    public String insBase;
    public String baseName;
    public String testName;
    public String apduCode;
    public int numStops;
    public int baseStopCode;

    PerfCodeConfig(String methodName, String insBase, String testName, int numStops, int baseStopCode) {
        this.methodName = methodName;
        this.insBase = insBase;
        this.baseName = String.format("TRAP_%s", insBase);
        this.apduCode = String.format("INS_%s", insBase);
        this.testName = testName;
        this.numStops = numStops;
        this.baseStopCode = baseStopCode;
    }
    PerfCodeConfig(PerfCodeConfig other) {
        this.methodName = other.methodName;
        this.insBase = other.insBase;
        this.baseName = other.baseName;
        this.apduCode = other.apduCode;
        this.testName = other.testName;
        this.numStops = other.numStops;
        this.baseStopCode = other.baseStopCode;
    }
}
