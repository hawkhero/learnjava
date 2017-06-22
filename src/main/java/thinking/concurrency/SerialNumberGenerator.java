package thinking.concurrency;


//: concurrency/SerialNumberGenerator.java

public class SerialNumberGenerator {
  private static volatile int serialNumber = 0;
  public synchronized static int nextSerialNumber() {
    return serialNumber++; // Not thread-safe
  }
} ///:~
