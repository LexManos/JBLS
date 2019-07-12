package org.jbls.test;

import org.junit.jupiter.api.Test;

import Hashing.BrokenSHA1;
import util.PadString;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HashTest {
    @Test
    public void testBSHA1() {
        String[] tests = {
            "67452301efcdab8998badcfe10325476c3d2e1f0", "",
            "fe4424936dc20078a033955159f823036e513f13", "a",
            "7bf57a7acc1be999f975dce65522fe81ab78c8d6", "abc",
            "b46b845a32a0363b222751511e03cee2f36f83c1", "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq",
            "f5c42efa8db3d52e9eb8e8cc707824d11873f714", "abcdefghbcdefghicdefghijdefghijkefghijklfghijklmghijklmnhijklmnoijklmnopjklmnopqklmnopqrlmnopqrsmnopqrstnopqrstu"
        };

        for (int x = 0; x < tests.length - 1; x += 2) {
            assertEquals(tests[x], toHex(BrokenSHA1.calcHashBuffer(tests[x+1].getBytes()), 5));
        }

    }

    private String toHex(int[] data, int len) {
        StringBuilder buf = new StringBuilder();
        for (int x = 0; x < len && x < data.length; x++)
            buf.append(PadString.padHex(data[x], 8));
        return buf.toString();
    }
}
