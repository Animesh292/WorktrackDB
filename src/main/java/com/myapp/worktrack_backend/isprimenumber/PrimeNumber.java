package com.myapp.worktrack_backend.isprimenumber;

public class PrimeNumber {

    public boolean isPrimeNumber(int number) {
        if(number <= 1) {
            return false;
        }
        for(int i = 2; i <= number/2; i++) {
            if(number%i == 0) {
                return false;
            }
        }
        return true;
    }
}
