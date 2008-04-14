/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.itest.esb.mule.message;

import org.openspaces.esb.mule.message.AbstractMessageHeader;
import org.openspaces.itest.esb.mule.Message;

/**
 * A simple message object that is written to the space. Note, this
 * message uses GigaSpaces support for POJO entries. With GigaSpaces
 * support for POJO entries there is no need even to mark the class
 * using annotations or xml though further customization is allowed
 * when using it.
 *
 * This object extends <code> AbstractMessageHeader </code> in order to hold Mule metadata attributes.
 *
 * @author yitzhaki
 */
public class MessageWithMessageHeader extends AbstractMessageHeader implements Message {

    private String message;

    private boolean read;

    public MessageWithMessageHeader() {
    }

    public MessageWithMessageHeader(String message, String id) {
        this.message = message;
        setUniqueId(id);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }




    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        MessageWithMessageHeader that = (MessageWithMessageHeader) o;

        if (read != that.read) return false;
        if (message != null ? !message.equals(that.message) : that.message != null) return false;

        return true;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (read ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "message = " + message + " read = " +  read + " " + super.toString();
    }
}