package jcprofiler;


/**
 * @author Petr Svenda
 */
public class JCProfiler_client {

    public static void main(String[] args) {
        JCProfiler_client app = new JCProfiler_client();
        app.run(args);
    }

    private void run(String[] args) {
        System.out.println("JCProfiler v1.0 by OpenCryptoProject, 2017");
        try {
            PerfTests perfTests = new PerfTests();
            perfTests.RunPerformanceTests(1, true);
        } catch (Exception ex) {
            System.out.println("Exception : " + ex);
        }
    }

}
