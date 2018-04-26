# JCProfiler
Performance profiler for JavaCard code 

The performance profiling of JavaCard applet code is a notoriously difficult task. As the card environment is build to protect the stored and processed secrets against an attacker with direct physical access, it is difficult to obtain a precise timing trace of the executed code on the granularity of separate methods or even lines of code. To the best of our knowledge, there is no open-source performance profiler available for the JavaCard platform. So we decided to build one.

The profiler is based on the following idea: The source code of an applet is extended with  numerous additional lines of code called "performance traps" capable to prematurely interrupt the applet's execution if the condition match the controlling _trapID_ variable. The trap can be inserted after every single line of an applet's original code to achieved the finest profiling granularity if required. The client-side testing application is then repeatedly executed with the different value of controlling _trapID_ variable. As a result increasingly larger chunk of applet's code is executed before interrupted on the corresponding trap. The client-side time measurements are collected and processed to compute the time difference between the two consecutive traps - resulting in the time required to execute a block of an original code between these two traps.

The usage is simple:
1. Developer signalizes interseting parts of code to profile by insertion of fixed strings
2. JCProfiler tool automatically generates all necessary testing code 
3. Developer sets proper applet AID, applet CLA and APDU command which will trigger inspected operation
4. Performance measurement client is executed to collect all timing measurements 
5. Applet source code is annotted with the extracted timings

Please read [wiki](https://github.com/petrs/JCProfiler/wiki) for all details.

## Simple example
The code below was automatically transformed and profiled after insertion of perfromance traps 'PM.check(PM.TRAP...'. Time in milliseconds provides time necessary to reach particular trap from the previous one. A name of card and unique profiling session ID in braces (gd60,1500968219581).

```` java
private short multiplication_x_KA(Bignat scalar, byte[] outBuffer, short outBufferOffset) {
  PM.check(PM.TRAP_ECPOINT_MULT_X_1); // 40 ms (gd60,1500968219581) 
  priv.setS(scalar.as_byte_array(), (short) 0, scalar.length());
  PM.check(PM.TRAP_ECPOINT_MULT_X_2); // 12 ms (gd60,1500968219581) 

  keyAgreement.init(priv);
  PM.check(PM.TRAP_ECPOINT_MULT_X_3); // 120 ms (gd60,1500968219581) 

  short len = this.getW(point_arr1, (short) 0); 
  PM.check(PM.TRAP_ECPOINT_MULT_X_4); // 9 ms (gd60,1500968219581) 
  len = keyAgreement.generateSecret(point_arr1, (short) 0, len, outBuffer, outBufferOffset);
  PM.check(PM.TRAP_ECPOINT_MULT_X_5); // 186 ms (gd60,1500968219581) 

  return COORD_SIZE;
}
````


### Satisfied users so far
* [JCMathLib](https://github.com/OpenCryptoProject/JCMathLib) library for Bignat and ECPoint operations
* [AEonJC](https://github.com/palkrajesh/AEonJC) implementation of ACORN, AEGIS, ASCON, CLOC, and MORUS authenticated encryption functions 
* You? Give it a try - love to hear the feedback :)

### Future work
* Proper ant compilation, travis...
* Averaging from multiple results instead of single run
* Better analysis of applet and detection of developer-provided info
* Automatic insertion of all performance traps (no requirement for manual templates)
* Code improvements (a lot of hardcoded strings etc. )

