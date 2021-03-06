package de.tum.androidcontroller.utils;


/**
 * Created by konstantin on 08/04/17.
 */

public class MathUtils {

    /**
     * The function is used to round floats to the custom amount of digits. It returns with 0s on the end of the float
     * @param in the very long float
     * @param decimalDigits the amount of decimal digits to have after calling this method
     * @return the in float with the amount of digits specified
     */
    public static float toDigits(float in, int decimalDigits){
        return (float) ((float) (Math.round(in*(Math.pow(10,decimalDigits)+0.0))/(Math.pow(10,decimalDigits)+0.0)) % Math.pow(10,sizeInt(decimalDigits)));
    }

    /**
     * The function is used to round floats to the custom amount of digits. It returns no 0s on the end of the float
     * @param in the very long float
     * @param decimalDigits the amount of decimal digits to have after calling this method
     * @return the in float with the amount of digits specified
     */
    public static float toDigitsClean(float in, int decimalDigits){
        return zeroCutter(toDigits(in,decimalDigits));
    }

    public static float zeroCutter(float in){
        String noZeros = Float.toString(in);
        noZeros = !noZeros.contains(".") ? noZeros : noZeros.replaceAll("0*$", "").replaceAll("\\.$", "");	//http://stackoverflow.com/questions/14984664/remove-trailing-zero-in-java
        return Float.parseFloat(noZeros);
    }
    /**
     * For obtaining the length of a integer
     * @param some the requested int
     * @return the length of the integer
     */
    public static int sizeInt(int some){
        return (int) Math.log10(Math.abs(some)+1);
    }
}
