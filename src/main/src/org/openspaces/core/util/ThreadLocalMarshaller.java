package org.openspaces.core.util;

import net.jini.io.OptimizedByteArrayInputStream;
import net.jini.io.OptimizedByteArrayOutputStream;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author kimchy
 */
public class ThreadLocalMarshaller {

    private static ThreadLocal<OptimizedByteArrayOutputStream> cachedByteArrayOutputStream = new ThreadLocal<OptimizedByteArrayOutputStream>() {
        @Override
        protected OptimizedByteArrayOutputStream initialValue() {
            return new OptimizedByteArrayOutputStream(1024);
        }
    };

    /**
     * Creates an object from a byte buffer.
     */
    public static Object objectFromByteBuffer(byte[] buffer) throws IOException, ClassNotFoundException {
        if (buffer == null)
            return null;

        OptimizedByteArrayInputStream inStream = new OptimizedByteArrayInputStream(buffer);
        ObjectInputStream in = new ObjectInputStream(inStream);
        Object retval = in.readObject();
        in.close();

        return retval;
    }

    /**
     * Serializes an object into a byte buffer.
     * The object has to implement interface Serializable or Externalizable.
     */
    public static byte[] objectToByteBuffer(Object obj) throws IOException {
        byte[] result = null;
        OptimizedByteArrayOutputStream outStream = cachedByteArrayOutputStream.get();
        outStream.reset();
        ObjectOutputStream out = new ObjectOutputStream(outStream);
        out.writeObject(obj);
        out.flush();
        result = outStream.toByteArray();
        out.close();

        return result;
    }

}
