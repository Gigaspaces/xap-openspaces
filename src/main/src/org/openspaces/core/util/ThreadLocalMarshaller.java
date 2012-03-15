/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
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
        OptimizedByteArrayOutputStream outStream = cachedByteArrayOutputStream.get();
        outStream.reset();
        ObjectOutputStream out = new ObjectOutputStream(outStream);
        out.writeObject(obj);
        out.flush();
        byte[] result = outStream.toByteArray();
        out.close();

        return result;
    }

}
