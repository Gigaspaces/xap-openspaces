package org.openspaces.utest.core.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openspaces.core.util.MemoryUnit;

public class MemoryUnitTest {

    private static final String[] postfixes = { 
        "b",
        "k",
        "m",
        "g",
        "t",
        "p",
        "e",
    };
    
    @Test
    public void bytesTest() {
        assertEquals("b",MemoryUnit.BYTES.getPostfix());
        assertEquals(1,  MemoryUnit.BYTES.convert("1"));
        assertEquals(1,  MemoryUnit.BYTES.convert("1b"),1);
        assertEquals(pow2(10), MemoryUnit.BYTES.convert("1k"));
        assertEquals(pow2(20),MemoryUnit.BYTES.convert("1m"));
        assertEquals(pow2(30),MemoryUnit.BYTES.convert("1g"));
        assertEquals(pow2(40),MemoryUnit.BYTES.convert("1t"));
        assertEquals(pow2(50),MemoryUnit.BYTES.convert("1p"));
        assertEquals(pow2(60),MemoryUnit.BYTES.convert("1e"));      
        assertEquals(MemoryUnit.BYTES.toBytes(1),1);
        assertEquals(MemoryUnit.BYTES.convert(1,MemoryUnit.BYTES),1L);
        assertEquals(MemoryUnit.toBytes("1"),1);
        assertEquals(1024L,MemoryUnit.BYTES.convert(1,MemoryUnit.KILOBYTES));
        assertEquals(MemoryUnit.BYTES.toKiloBytes(1),0);
        assertEquals(MemoryUnit.BYTES.toKiloBytes(1024),1);
        assertEquals(MemoryUnit.BYTES.convert(1,MemoryUnit.MEGABYTES),1024L*1024L);
        assertEquals(MemoryUnit.BYTES.convert(1,MemoryUnit.GIGABYTES),1024L*1024L*1024L);
    }
    
    @Test
    public void kilobytesTest() {
        assertEquals("k",MemoryUnit.KILOBYTES.getPostfix());
        assertEquals(MemoryUnit.toKiloBytes("1k"),1);
        assertEquals(0       ,MemoryUnit.KILOBYTES.convert("1"));
        assertEquals(pow2(0) ,MemoryUnit.KILOBYTES.convert("1k"));
        assertEquals(pow2(10),MemoryUnit.KILOBYTES.convert("1m"));
        assertEquals(pow2(20),MemoryUnit.KILOBYTES.convert("1g"));
        assertEquals(pow2(30),MemoryUnit.KILOBYTES.convert("1t"));
        assertEquals(pow2(40),MemoryUnit.KILOBYTES.convert("1p"));
        assertEquals(pow2(50),MemoryUnit.KILOBYTES.convert("1e"));
    }
    
    @Test
    public void megabytesTest() {
        assertEquals("m",MemoryUnit.MEGABYTES.getPostfix());
        assertEquals(MemoryUnit.toMegaBytes("1m"),1);
        assertEquals(0       ,MemoryUnit.MEGABYTES.convert("1"));
        assertEquals(0       ,MemoryUnit.MEGABYTES.convert("1k"));
        assertEquals(pow2(0) ,MemoryUnit.MEGABYTES.convert("1m"));
        assertEquals(pow2(10),MemoryUnit.MEGABYTES.convert("1g"));
        assertEquals(pow2(20),MemoryUnit.MEGABYTES.convert("1t"));
        assertEquals(pow2(30),MemoryUnit.MEGABYTES.convert("1p"));
        assertEquals(pow2(40),MemoryUnit.MEGABYTES.convert("1e"));
    }
    
    @Test
    public void gigabytesTest() {
        assertEquals("g",MemoryUnit.GIGABYTES.getPostfix());
        assertEquals(MemoryUnit.toGigaBytes("1g"),1);
        assertEquals(0       ,MemoryUnit.GIGABYTES.convert("1"));
        assertEquals(0       ,MemoryUnit.GIGABYTES.convert("1k"));
        assertEquals(0       ,MemoryUnit.GIGABYTES.convert("1m"));
        assertEquals(pow2(0) ,MemoryUnit.GIGABYTES.convert("1g"));
        assertEquals(pow2(10),MemoryUnit.GIGABYTES.convert("1t"));
        assertEquals(pow2(20),MemoryUnit.GIGABYTES.convert("1p"));
        assertEquals(pow2(30),MemoryUnit.GIGABYTES.convert("1e"));
    }
    
    @Test
    public void terabytesTest() {
        assertEquals("t",MemoryUnit.TERABYTES.getPostfix());
        assertEquals(MemoryUnit.toTeraBytes("1t"),1);
        assertEquals(0       ,MemoryUnit.TERABYTES.convert("1"));
        assertEquals(0       ,MemoryUnit.TERABYTES.convert("1k"));
        assertEquals(0       ,MemoryUnit.TERABYTES.convert("1m"));
        assertEquals(0       ,MemoryUnit.TERABYTES.convert("1g"));
        assertEquals(pow2(0) ,MemoryUnit.TERABYTES.convert("1t"));
        assertEquals(pow2(10),MemoryUnit.TERABYTES.convert("1p"));
        assertEquals(pow2(20),MemoryUnit.TERABYTES.convert("1e"));
    }
    
    @Test
    public void petabytesTest() {
        assertEquals("p",MemoryUnit.PETABYTES.getPostfix());
        assertEquals(MemoryUnit.toPetaBytes("1p"),1);
        assertEquals(0       ,MemoryUnit.PETABYTES.convert("1"));
        assertEquals(0       ,MemoryUnit.PETABYTES.convert("1k"));
        assertEquals(0       ,MemoryUnit.PETABYTES.convert("1m"));
        assertEquals(0       ,MemoryUnit.PETABYTES.convert("1g"));
        assertEquals(0       ,MemoryUnit.PETABYTES.convert("1t"));
        assertEquals(pow2(0) ,MemoryUnit.PETABYTES.convert("1p"));
        assertEquals(pow2(10),MemoryUnit.PETABYTES.convert("1e"));
    }
    
    @Test
    public void exabytesTest() {
        assertEquals("e",MemoryUnit.EXABYTES.getPostfix());
        assertEquals(MemoryUnit.toExaBytes("1e"),1);
        assertEquals(0       ,MemoryUnit.EXABYTES.convert("1"));
        assertEquals(0       ,MemoryUnit.EXABYTES.convert("1k"));
        assertEquals(0       ,MemoryUnit.EXABYTES.convert("1m"));
        assertEquals(0       ,MemoryUnit.EXABYTES.convert("1g"));
        assertEquals(0       ,MemoryUnit.EXABYTES.convert("1t"));
        assertEquals(0       ,MemoryUnit.EXABYTES.convert("1p"));
        assertEquals(pow2(0) ,MemoryUnit.EXABYTES.convert("1e"));
    }

    private long pow2(int x) {
        return (long)Math.pow(2, x);
    }

}
