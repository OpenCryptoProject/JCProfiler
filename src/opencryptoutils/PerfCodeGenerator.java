package opencryptoutils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import static java.nio.file.StandardCopyOption.*;
import java.util.ArrayList;

/**
 *
 * @author Petr Svenda
 */
public class PerfCodeGenerator {
    public final static int MAX_TRAPS_PER_METHOD = 16;

    void generatePerfStopStrings() {
        ArrayList<PerfCodeConfig> testList = new ArrayList<>();
/*
        testList.add(new PerfCodeConfig("EC_MUL", "EC scalar-point multiplication", 6, 0x7780));

        testList.add(new PerfCodeConfig("BN_STR", "BigNatural Storage", 3, 0x7740));
        testList.add(new PerfCodeConfig("BN_ADD", "BigNatural Addition", 7, 0x7730));
        testList.add(new PerfCodeConfig("BN_SUB", "BigNatural Subtraction", 7, 0x7720));
        testList.add(new PerfCodeConfig("BN_MUL", "BigNatural Multiplication", 6, 0x7710));
        testList.add(new PerfCodeConfig("BN_EXP", "BigNatural Exponentiation", 6, 0x7700));
        testList.add(new PerfCodeConfig("BN_MOD", "BigNatural Modulo", 5, 0x76f0));
        testList.add(new PerfCodeConfig("BN_ADD_MOD", "BigNatural Addition (Modulo)", 7, 0x76e0));
        testList.add(new PerfCodeConfig("BN_SUB_MOD", "BigNatural Subtraction (Modulo)", 6, 0x76d0));
        testList.add(new PerfCodeConfig("BN_MUL_MOD", "BigNatural Multiplication (Modulo)", 6, 0x76c0));
        testList.add(new PerfCodeConfig("BN_EXP_MOD", "BigNatural Exponentiation (Modulo)", 6, 0x76b0));
        testList.add(new PerfCodeConfig("BN_INV_MOD", "BigNatural Inversion (Modulo)", 5, 0x76a0));

        testList.add(new PerfCodeConfig("INT_STR", "Integer Storage", 2, 0x7690));
        testList.add(new PerfCodeConfig("INT_ADD", "Integer Addition", 4, 0x7680));
        testList.add(new PerfCodeConfig("INT_SUB", "Integer Subtraction", 4, 0x7670));
        testList.add(new PerfCodeConfig("INT_MUL", "Integer Multiplication", 4, 0x7660));
        testList.add(new PerfCodeConfig("INT_DIV", "Integer Division", 4, 0x7650));
        testList.add(new PerfCodeConfig("INT_EXP", "Integer Exponentiation", 4, 0x7640));
        testList.add(new PerfCodeConfig("INT_MOD", "Integer Modulo", 4, 0x7630));

        testList.add(new PerfCodeConfig("ECCURVE_NEWKEYPAIR", "ECCurve_newKeyPair", 7, 0x75f0));
        testList.add(new PerfCodeConfig("ECPOINT_ADD", "ECPoint_add", 7, 0x75e0));
        testList.add(new PerfCodeConfig("ECPOINT_MULT", "ECPoint_multiplication", 12, 0x75d0));
*/
        testList.add(new PerfCodeConfig("short multiplication_x(", "ECPOINT_MULT_X", "ECPoint_multiplication_x", 5, 0x75c0));
        testList.add(new PerfCodeConfig("void negate(", "ECPOINT_NEGATE", "ECPoint_negate", 5, 0x75b0));
        
        generatePerfStopStrings(testList);
    }

    
    
    void generatePerfStopStrings(ArrayList<PerfCodeConfig> testList) {
                
        for (PerfCodeConfig item : testList) {
            generatePerfTrapsStrings_TrapIDs(item.baseName, item.numStops, item.baseStopCode);
            System.out.println();
        }
        System.out.println("\n ------------------ \n");
        for (PerfCodeConfig item : testList) {
            generatePerfTrapsStrings_Mappings(item.baseName, item.numStops);
            System.out.println();
        }
        System.out.println("\n ------------------ \n");

        for (PerfCodeConfig item : testList) {
            generatePerfTrapsStrings_InitList(item.baseName, item.testName, item.numStops);
        }
        System.out.println("\n ------------------ \n");

    }

    static String generatePerfTrapsStrings_TrapIDs(String baseName, int numStops, int baseOffset) {
        String result = String.format("    public static final short %s = (short) 0x%s;\n", baseName, Integer.toHexString(baseOffset));
        for (int i = 1; i <= numStops; i++) {
            result += String.format("    public static final short %s_%d = (short) (%s + %d);\n", baseName, i, baseName, i);
        }
        result += String.format("    public static final short %s_COMPLETE = %s;\n", baseName, baseName);
        
        return result;
    }

    static String generatePerfTrapsStrings_Mappings(String baseName, int numStops) {
        String indent = "        ";
        String result = "";
        for (int i = 1; i <= numStops; i++) {
            result += String.format("%sPERF_TRAPS_MAPPING.put(PMC.%s_%d, \"%s_%d\");\n", indent, baseName, i, baseName, i);
        }
        result += String.format("%sPERF_TRAPS_MAPPING.put(PMC.%s_COMPLETE, \"%s_COMPLETE\");\n", indent, baseName, baseName);
        
        return result;
    }

    static String generatePerfTrapsStrings_InitList(String baseName, String testName, int numStops) {
        String indent = "            ";
        String sanitizedTestName = testName.replace(" ", "_").replace("(", "_").replace(")", "_");
        String result = String.format("%sshort[] PERFTRAPS_%s = {", indent, sanitizedTestName);
        for (int i = 1; i <= numStops; i++) {
            result += String.format("PMC.%s_%d, ", baseName, i);
        }
        result += String.format("PMC.%s_COMPLETE};\n", baseName);
        result += String.format("%scfg.perfStops = PERFTRAPS_%s;\n", indent, sanitizedTestName);
        result += String.format("%scfg.perfStopComplete = PMC.%s_COMPLETE;", indent, baseName);
        
        return result;
    }    

    void insertPerfTraps(PerfCodeConfig cfg, String filePath) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String filePathTransform = filePath + ".perf";
            
            FileOutputStream fileOut = new FileOutputStream(filePathTransform);
            String strLine;
            String perfTrapLine = String.format("PerfMeasure.check(PerfMeasure.PERF_TEST_%s_0);\n", cfg.insBase);
            boolean bInsideTargetFunction = false;
            String methodNameEnd = String.format("end %s", cfg.methodName);
            
            // For every line of program insert generic performance trap
            while ((strLine = br.readLine()) != null) {
                if (bInsideTargetFunction) {
                    if (strLine.contains(methodNameEnd)) {
                        bInsideTargetFunction = false;
                    }
                    String strLineTrim = strLine.trim();
                    if (strLineTrim.length() > 0) {
                        int indentCount = strLine.indexOf(strLineTrim);
                        String indent = strLine.substring(0, indentCount);
                        // non-zero line detected, insert perf trap with proper indentation
                        fileOut.write(indent.getBytes());
                        fileOut.write(perfTrapLine.getBytes());
                    }    
                }
                else {
                    if (strLine.contains(cfg.methodName)) {
                        bInsideTargetFunction = true;
                    }
                }
                // Insert original line (always)
                strLine += "\n";
                fileOut.write(strLine.getBytes());
            }

            br.close();
            fileOut.close();
        } catch (Exception e) {
            System.out.println(String.format("Failed to transform file %s ", filePath) + e);
        }
    }
    
    void generatePersonalizedProfiler(PerfCodeConfig baseCfg, String baseDirectory) throws IOException {
        ArrayList<PerfCodeConfig> filesWithTraps = new ArrayList<>();

        //
        // Process all input files, try to find performance trap template and transform files with relevant traps 
        //
        // make subdir for results
        String outputDirApplet = String.format("%s/target/profiler_applet/", baseDirectory);
        new File(outputDirApplet).mkdirs();
        String outputDirClient = String.format("%s/target/profiler_client/", baseDirectory);
        new File(outputDirClient).mkdirs();
        String baseAppletFilesDir = String.format("%s/templates/input_applet_files/", baseDirectory);
        File dir = new File(baseAppletFilesDir);
        String[] filesArray = dir.list();
        if ((filesArray != null) && (dir.isDirectory() == true)) {

            for (String fileName : filesArray) {
                String filePath = baseAppletFilesDir + fileName;
                File inputFile = new File(filePath);
                if (!inputFile.isDirectory()) {
                    // Copy file from templates to target
                    String targetFilePathOrig = outputDirApplet + fileName + ".orig";
                    String targetFilePath = outputDirApplet + fileName;
                    Files.copy(inputFile.toPath(), (new File(targetFilePathOrig)).toPath(), REPLACE_EXISTING);

                    PerfCodeConfig cfg = new PerfCodeConfig(baseCfg);
                    if (enumeratePerfTrapsFile(cfg, targetFilePathOrig, targetFilePath)) {
                        filesWithTraps.add(cfg);
                    }
                }
            }
        }
        
        //
        // Generate helper files for card-side profiler application 
        //
        String templateAppletDir = String.format("%s/templates/template_profiler_applet/", baseDirectory);
        copy(new File(templateAppletDir), new File(outputDirApplet));
        personalizeTemplatesApplet(filesWithTraps, outputDirApplet); //=> PM, PMC 

        //
        // Generate helper files for client-side profiler application 
        //
        String templateClientDir = String.format("%s/templates/template_profiler_client/", baseDirectory);
        copy(new File(templateClientDir), new File(outputDirClient)); 
        ArrayList<String> filesToCopy = new ArrayList<>();
        filesToCopy.add(String.format("%s/PMC.java", outputDirApplet));
        personalizeTemplatesClient(filesWithTraps, outputDirClient, filesToCopy); // JCProfiler_client
        
        
        System.out.println("\n\n#########################################");
        System.out.println(String.format("INFO: The personalized profiler generation is now finished."));
        System.out.println(String.format("Directory '%s' contains your applet's transformed files with numbered performance traps.\nNow you need to:", outputDirApplet));
        System.out.println("1. Copy applet files (together with PMC.java and PM.java) back to your applet structure.");        
        System.out.println("2. Open PM.java and PMC.java and update package to your applet's package name.");
        System.out.println("3. Open PM.java and *move* specified part of code (INS_PERF_SETSTOP) at the end of file to process() method of your applet.");
        System.out.println("4. Convert your applet and upload to target card as usual.");      
        System.out.println();
        System.out.println(String.format("Directory '%s' contains client-side code of the profiler.\nNow you need to:", outputDirClient));
        System.out.println("1. Open PerfTests.java and correct APPLET_CLA, APPLET_AID according to your applet.");
        System.out.println("2. Open PerfTests.java and set proper apdu APDU_TRIGGER which will trigger (let execute) the method you like to profile (method which now have 'PM.check(PMC.TRAP_' inserted).");
        System.out.println("3. (Optional) Set CARD_NAME to sensible string. If APDU_CLEANUP is set, this apdu is send to card after every measurement command (for 'cleaning').");
        System.out.println("4. Compile and run JCProfiler_client. Measurement apdu commands are send to card and resulting measurements are inserted as comment directly behind the correspoding performance trap.");
        System.out.println(String.format("5. Inspect console results and modified files which are copied into directory '%s/perf/unique_experiment_id'.", outputDirApplet));
        System.out.println();
        
        
    }
    
    void personalizeTemplatesApplet(ArrayList<PerfCodeConfig> filesWithTraps, String outputDirApplet) throws IOException {
        //
        // Personalize PMC.java
        //
        String PLACEHOLDER_PMC_CONSTANTS = "//### PLACEHOLDER PMC CONSTANTS";
        String result = "";
        for (PerfCodeConfig item : filesWithTraps) {
            result += generatePerfTrapsStrings_TrapIDs(item.baseName, item.numStops, item.baseStopCode);
        }
        String inputFilePath = String.format("%sPMC.java", outputDirApplet);
        System.out.println(String.format("INFO: Transforming file '%s' for trapID constants.", inputFilePath));
        replaceStringInCopiedFile(inputFilePath, PLACEHOLDER_PMC_CONSTANTS, result, true);
    }
    
    void deleteFileNoExcept(String filePath) {
        try { 
            Files.delete(new File(filePath).toPath());
        }
        catch (NoSuchFileException ignored) {} 
        catch (IOException ignored) {}
    }

    void personalizeTemplatesClient(ArrayList<PerfCodeConfig> filesWithTraps, String outputDirClient, ArrayList<String> filesToCopy) throws IOException {
        //
        // Personalize PerfTests.java
        //
        String resultPerfTrapInit = "";
        String resultPerfTrapMappings = "";
        for (PerfCodeConfig item : filesWithTraps) {
            resultPerfTrapInit += generatePerfTrapsStrings_InitList(item.baseName, item.testName, item.numStops);
            resultPerfTrapMappings += generatePerfTrapsStrings_Mappings(item.baseName, item.numStops);
        }
        String inputFilePath = String.format("%ssrc/jcprofiler/PerfTests.java", outputDirClient);
        
        System.out.println(String.format("INFO: Transforming file '%s' for code with trapID to send.", inputFilePath));
        String PLACEHOLDER_PERFTRAPS_INIT = "//### PLACEHOLDER PERFTRAPS INIT";
        replaceStringInCopiedFile(inputFilePath, PLACEHOLDER_PERFTRAPS_INIT, resultPerfTrapInit, true);

        System.out.println(String.format("INFO: Transforming file '%s' for code with mapping between name and trapID.", inputFilePath));
        String PLACEHOLDER_PMC_MAPPINGS = "//### PLACEHOLDER PMC MAPPINGS";
        replaceStringInCopiedFile(inputFilePath, PLACEHOLDER_PMC_MAPPINGS, resultPerfTrapMappings, true);

        //
        // Copy required personalized files from applet into client (constants etc.)
        //
        for (String fileToCopy : filesToCopy) {
            File file = new File(fileToCopy);
            String localPath = String.format("%s/src/jcprofiler/%s", outputDirClient, file.getName());
            copy(new File(fileToCopy), new File(localPath));
        }
    }

    boolean enumeratePerfTrapsFile(PerfCodeConfig cfg, String inputFilePath, String outputFilePath) {
        boolean bSomeTrapFound = false;
        System.out.println(String.format("INFO: Processing file '%s'", inputFilePath));
        
        try {
            BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
            String filePathTransform = outputFilePath;
            FileOutputStream fileOut = new FileOutputStream(filePathTransform);
            String strLine;
            String perfTrapLineTemplate = String.format("PM.check(PMC.TRAP_%s_0);", cfg.insBase);
            String perfTrapCounterTemplate = String.format("%s_0", cfg.insBase);
            int perfTrapCount = 1;

            // For every line of program insert generic performance trap
            while ((strLine = br.readLine()) != null) {
                if (strLine.contains(perfTrapLineTemplate)) { 
                    bSomeTrapFound = true; // We found at least one trap!

                    // Replace by trap with counter 
                    String perfTrapCounter = String.format("%s_%d", cfg.insBase, perfTrapCount);
                    perfTrapCount++;
                    if (perfTrapCount > MAX_TRAPS_PER_METHOD) {
                        System.out.println("  Too much traps templates found - try to decrease below " + MAX_TRAPS_PER_METHOD);
                    }
                    
                    strLine = strLine.replaceFirst(perfTrapCounterTemplate, perfTrapCounter);
                    
                }
                // Insert original or modified final line 
                strLine += "\n";
                fileOut.write(strLine.getBytes());
            }
            
            br.close();
            fileOut.close();
            
            if (!bSomeTrapFound) {
                System.out.println(String.format("  No template performance traps found in file '%s'", inputFilePath));
            }
            else {
                System.out.println(String.format("  OK: Total '%d' traps found in file '%s'", perfTrapCount, inputFilePath));
            }
            
            cfg.numStops = perfTrapCount - 1;
        } catch (Exception e) {
            System.out.println(String.format("Failed to transform file %s ", inputFilePath) + e);
        }
        
        return bSomeTrapFound;
    }
    
    boolean replaceStringInCopiedFile(String targetFilePath, String stringToFind, String stringReplace, boolean bLeaveFind) {
        boolean bReplacePerformed = false;
        try {
            String inputFilePathTmp = String.format("%s.tmp", targetFilePath);
            // make local copy
            deleteFileNoExcept(inputFilePathTmp);
            new File(targetFilePath).renameTo(new File(inputFilePathTmp));
            
            // Transform file
            BufferedReader br = new BufferedReader(new FileReader(inputFilePathTmp));
            FileOutputStream fileOut = new FileOutputStream(targetFilePath);
            String strLine;

            while ((strLine = br.readLine()) != null) {
                if (strLine.contains(stringToFind)) {
                    if (bLeaveFind) {
                        strLine = strLine.replaceFirst(stringToFind, String.format("%s\n\n%s", stringToFind, stringReplace));
                    }
                    else {
                        strLine = strLine.replaceFirst(stringToFind, stringReplace);
                    }
                    bReplacePerformed = true;
                }
                // Insert original or modified final line 
                strLine += "\n";
                fileOut.write(strLine.getBytes());
            }
            br.close();
            fileOut.close();
            
            // Delete temp file
            deleteFileNoExcept(inputFilePathTmp);
            
        } catch (Exception e) {
            System.out.println(String.format("  Failed to transform file '%s' ", targetFilePath) + e);
        }
        
        if (!bReplacePerformed) {
            System.out.println(String.format("  Problem: no occurence of '%s' in file '%s'", stringToFind, targetFilePath));
        }
        else {
            System.out.println(String.format("  OK: '%s' found and replaced", stringToFind));
        }
        
        return bReplacePerformed;
    }
    
    
    
    
    
    
    
    
    public void copy(File sourceLocation, File targetLocation) throws IOException {
        if (sourceLocation.isDirectory()) {
            copyDirectory(sourceLocation, targetLocation);
        } else {
            copyFile(sourceLocation, targetLocation);
        }
    }

    private void copyDirectory(File source, File target) throws IOException {
        if (!target.exists()) {
            target.mkdir();
        }

        for (String f : source.list()) {
            copy(new File(source, f), new File(target, f));
        }
    }

    private void copyFile(File source, File target) throws IOException {
        try (
            InputStream in = new FileInputStream(source);
            OutputStream out = new FileOutputStream(target)) {
            byte[] buf = new byte[1024];
            int length;
            while ((length = in.read(buf)) > 0) {
                out.write(buf, 0, length);
            }
        }
    }    
}
