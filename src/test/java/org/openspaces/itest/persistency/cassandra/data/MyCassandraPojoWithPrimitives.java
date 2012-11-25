package org.openspaces.itest.persistency.cassandra.data;

import java.util.Arrays;

public class MyCassandraPojoWithPrimitives
{
    private boolean booleanProperty;
    private byte byteProperty;
    private char charProperty;
    private short shortProperty;
    private int intProperty;
    private long longProperty;
    private float floatProperty;
    private double doubleProperty;
    
    private boolean[] booleanArrayProperty;
    private byte[] byteArrayProperty;
    private char[] charArrayProperty;
    private short[] shortArrayProperty;
    private int[] intArrayProperty;
    private long[] longArrayProperty;
    private float[] floatArrayProperty;
    private double[] doubleArrayProperty;
    
    public boolean isBooleanProperty()
    {
        return booleanProperty;
    }
    public void setBooleanProperty(boolean booleanProperty)
    {
        this.booleanProperty = booleanProperty;
    }
    public byte getByteProperty()
    {
        return byteProperty;
    }
    public void setByteProperty(byte byteProperty)
    {
        this.byteProperty = byteProperty;
    }
    public char getCharProperty()
    {
        return charProperty;
    }
    public void setCharProperty(char charProperty)
    {
        this.charProperty = charProperty;
    }
    public short getShortProperty()
    {
        return shortProperty;
    }
    public void setShortProperty(short shortProperty)
    {
        this.shortProperty = shortProperty;
    }
    public int getIntProperty()
    {
        return intProperty;
    }
    public void setIntProperty(int intProperty)
    {
        this.intProperty = intProperty;
    }
    public long getLongProperty()
    {
        return longProperty;
    }
    public void setLongProperty(long longProperty)
    {
        this.longProperty = longProperty;
    }
    public float getFloatProperty()
    {
        return floatProperty;
    }
    public void setFloatProperty(float floatProperty)
    {
        this.floatProperty = floatProperty;
    }
    public double getDoubleProperty()
    {
        return doubleProperty;
    }
    public void setDoubleProperty(double doubleProperty)
    {
        this.doubleProperty = doubleProperty;
    }
    public boolean[] getBooleanArrayProperty()
    {
        return booleanArrayProperty;
    }
    public void setBooleanArrayProperty(boolean[] booleanArrayProperty)
    {
        this.booleanArrayProperty = booleanArrayProperty;
    }
    public byte[] getByteArrayProperty()
    {
        return byteArrayProperty;
    }
    public void setByteArrayProperty(byte[] byteArrayProperty)
    {
        this.byteArrayProperty = byteArrayProperty;
    }
    public char[] getCharArrayProperty()
    {
        return charArrayProperty;
    }
    public void setCharArrayProperty(char[] charArrayProperty)
    {
        this.charArrayProperty = charArrayProperty;
    }
    public short[] getShortArrayProperty()
    {
        return shortArrayProperty;
    }
    public void setShortArrayProperty(short[] shortArrayProperty)
    {
        this.shortArrayProperty = shortArrayProperty;
    }
    public int[] getIntArrayProperty()
    {
        return intArrayProperty;
    }
    public void setIntArrayProperty(int[] intArrayProperty)
    {
        this.intArrayProperty = intArrayProperty;
    }
    public long[] getLongArrayProperty()
    {
        return longArrayProperty;
    }
    public void setLongArrayProperty(long[] longArrayProperty)
    {
        this.longArrayProperty = longArrayProperty;
    }
    public float[] getFloatArrayProperty()
    {
        return floatArrayProperty;
    }
    public void setFloatArrayProperty(float[] floatArrayProperty)
    {
        this.floatArrayProperty = floatArrayProperty;
    }
    public double[] getDoubleArrayProperty()
    {
        return doubleArrayProperty;
    }
    public void setDoubleArrayProperty(double[] doubleArrayProperty)
    {
        this.doubleArrayProperty = doubleArrayProperty;
    }
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(booleanArrayProperty);
        result = prime * result + (booleanProperty ? 1231 : 1237);
        result = prime * result + Arrays.hashCode(byteArrayProperty);
        result = prime * result + byteProperty;
        result = prime * result + Arrays.hashCode(charArrayProperty);
        result = prime * result + charProperty;
        result = prime * result + Arrays.hashCode(doubleArrayProperty);
        long temp;
        temp = Double.doubleToLongBits(doubleProperty);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + Arrays.hashCode(floatArrayProperty);
        result = prime * result + Float.floatToIntBits(floatProperty);
        result = prime * result + Arrays.hashCode(intArrayProperty);
        result = prime * result + intProperty;
        result = prime * result + Arrays.hashCode(longArrayProperty);
        result = prime * result + (int) (longProperty ^ (longProperty >>> 32));
        result = prime * result + Arrays.hashCode(shortArrayProperty);
        result = prime * result + shortProperty;
        return result;
    }
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MyCassandraPojoWithPrimitives other = (MyCassandraPojoWithPrimitives) obj;
        if (!Arrays.equals(booleanArrayProperty, other.booleanArrayProperty))
            return false;
        if (booleanProperty != other.booleanProperty)
            return false;
        if (!Arrays.equals(byteArrayProperty, other.byteArrayProperty))
            return false;
        if (byteProperty != other.byteProperty)
            return false;
        if (!Arrays.equals(charArrayProperty, other.charArrayProperty))
            return false;
        if (charProperty != other.charProperty)
            return false;
        if (!Arrays.equals(doubleArrayProperty, other.doubleArrayProperty))
            return false;
        if (Double.doubleToLongBits(doubleProperty) != Double.doubleToLongBits(other.doubleProperty))
            return false;
        if (!Arrays.equals(floatArrayProperty, other.floatArrayProperty))
            return false;
        if (Float.floatToIntBits(floatProperty) != Float.floatToIntBits(other.floatProperty))
            return false;
        if (!Arrays.equals(intArrayProperty, other.intArrayProperty))
            return false;
        if (intProperty != other.intProperty)
            return false;
        if (!Arrays.equals(longArrayProperty, other.longArrayProperty))
            return false;
        if (longProperty != other.longProperty)
            return false;
        if (!Arrays.equals(shortArrayProperty, other.shortArrayProperty))
            return false;
        if (shortProperty != other.shortProperty)
            return false;
        return true;
    }
    
//    @Override
//    public int hashCode()
//    {
//        final int prime = 31;
//        int result = 1;
//        result = prime * result + (booleanProperty ? 1231 : 1237);
//        result = prime * result + byteProperty;
//        result = prime * result + charProperty;
//        long temp;
//        temp = Double.doubleToLongBits(doubleProperty);
//        result = prime * result + (int) (temp ^ (temp >>> 32));
//        result = prime * result + Float.floatToIntBits(floatProperty);
//        result = prime * result + intProperty;
//        result = prime * result + (int) (longProperty ^ (longProperty >>> 32));
//        result = prime * result + shortProperty;
//        return result;
//    }
//    @Override
//    public boolean equals(Object obj)
//    {
//        if (this == obj)
//            return true;
//        if (obj == null)
//            return false;
//        if (getClass() != obj.getClass())
//            return false;
//        MyCassandraPojoWithPrimitives other = (MyCassandraPojoWithPrimitives) obj;
//        if (booleanProperty != other.booleanProperty)
//            return false;
//        if (byteProperty != other.byteProperty)
//            return false;
//        if (charProperty != other.charProperty)
//            return false;
//        if (Double.doubleToLongBits(doubleProperty) != Double.doubleToLongBits(other.doubleProperty))
//            return false;
//        if (Float.floatToIntBits(floatProperty) != Float.floatToIntBits(other.floatProperty))
//            return false;
//        if (intProperty != other.intProperty)
//            return false;
//        if (longProperty != other.longProperty)
//            return false;
//        if (shortProperty != other.shortProperty)
//            return false;
//        return true;
//    }
    
    
    
}
