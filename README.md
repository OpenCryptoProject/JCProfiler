# JCProfiler
Performance profiler for Java Card code 

The performance profiling parts of Java Card applet code is a notoriously difficult task. As the card environment is build to protect stored and processed secrets even against an attacker with direct physical access, it's difficult to obtain precise timing trace for the executed code. We are not aware of any free performance profiler for Java Card platform so we decided to build one.

The usage is simple:
1. Developer signalizes interseting parts of code to profile by insertion of fixed strings
2. JCProfiler tool automatically generates all necessary testing code 
3. Developer sets proper applet AID, applet CLA and APDU command which will trigger inspected operation
4. Performance measurement client is executed to collect all required measurements 
5. Applet source code is annotted with measured timings

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


### Satisfied users
* JCMathLib library for Bignat and ECPoint operations
* You? Give it a try - love to hear the feedback :)

### Future work
* Averaging from multiple results instead of single run
* Better analysis of applet and detection of developer-provided info
* Code improvements (a lot of hardcoded strings etc. )

