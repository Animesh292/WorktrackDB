package com.myapp.worktrack_backend.isprimenumbertest;

import com.myapp.worktrack_backend.isprimenumber.PrimeNumber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

public class IsPrimeNumberTest {


    //@Test
    //public void validateIsPrimeNumber() {
//        int number = 2;
//        //arrange
//        PrimeNumber pn = new PrimeNumber();
//        boolean result = pn.isPrimeNumber(number);
//
//        System.out.println(
//                "Input: " + number + ", Result: " + result
//        );
//        //Assertion
//        assertTrue(result);
   // }
    //

//    @Test
//    public void validateIsNotPrimeNumber() {
//        int number = 1;
//        //arrange
//        PrimeNumber pn = new PrimeNumber();
//        boolean result = pn.isPrimeNumber(number);
//
//        System.out.println(
//                "Input: " + number + ", Result: " + result
//        );
//        //Assertion
//        assertFalse(result);
//    }

    //single Test method runs multiple times for different inputs
    @ParameterizedTest
    @CsvSource({ "1, false",
            "2, true",
            "3, true",
            "9, false",
            "10, false"})
    public void isPrimeNumberTest(int number, boolean expected) {
        PrimeNumber pn = new PrimeNumber();
        assertEquals(expected, pn.isPrimeNumber(number));
    }



}
