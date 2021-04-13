package com.example.snarkportingtest;

/*******************************************************************************
 * Author: Seongho Park <shparkk95@kookmin.ac.kr>
 *******************************************************************************/

import java.math.BigInteger;

public class BigIntegerAffinePoint {
    public BigInteger x;
    public BigInteger y;

    public BigIntegerAffinePoint(BigInteger x) {
        this.x = x;
    }

    public BigIntegerAffinePoint(BigInteger x, BigInteger y) {
        this.x = x;
        this.y = y;
    }

    public BigIntegerAffinePoint(BigIntegerAffinePoint p) {
        this.x = p.x;
        this.y = p.y;
    }
}
