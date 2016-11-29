// AidlFitnessService.aidl
package com.tin.proj_fit;

// Declare any non-default types here with import statements

interface AidlFitnessService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

    void putData(double distance, long duration, int calories);
    String debugPrint(double distance, long duration, int calories);
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
}
