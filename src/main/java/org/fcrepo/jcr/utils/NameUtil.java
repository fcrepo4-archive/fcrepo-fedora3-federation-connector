/**
 * Copyright 2013 DuraSpace, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fcrepo.jcr.utils;

import java.io.UnsupportedEncodingException;

/**
 * A utility class with methods to convert arbitrary strings into valid JCR
 * local names which can then be faithfully restored to their original String.
 */
public class NameUtil {

    private static final char KEY_CHAR = '%';

    /**
     * A method that will guarantee a result that can be a valid JRC local name
     * and that can be transformed back into the original value by invoking
     * {@link decodeLocalName()}.
     * @param value any String
     * @return an escaped version of the provided value which can be a valid
     * JCR localname
     */
    public static String encodeForLocalName(String value) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < value.length(); i ++) {
            char c = value.charAt(i);
            if (isValidLocalNameCharacter(c) && c != KEY_CHAR) {
                result.append(c);
            } else {
                result.append(encodeChar(c));
            }
        }
        if (result.toString().equals("..")) {
            return encodeChar('.') + encodeChar('.');
        } else if (result.toString().equals(".")) {
            return String.valueOf(encodeChar('.'));
        } else {
            return result.toString();
        }
    }

    /**
     * A method to reverse the transformation of a String that was passed to
     * {@link encodeForLocalName()}.
     * @param encodedName the result of a previous call to
     * {@link encodeForLocalName()}.
     * @return the decoded String
     */
    public static String decodeLocalName(String encodedName) {
        // determine the number of encoded bytes in the name
        // so that we can a) return early if possible, b) efficiently
        // use an appropriately sized byte buffer
        int encodedByteCount = 0;
        for (int i = 0; i < encodedName.length(); i ++) {
            if (encodedName.charAt(i) == KEY_CHAR) {
                encodedByteCount ++;
            }
        }
        if (encodedByteCount == 0) {
            return encodedName;
        }

        StringBuffer result = new StringBuffer();
        byte[] bytes = new byte[encodedByteCount];
        int byteOffset = 0;
        for (int i = 0; i < encodedName.length(); i ++) {
            char c = encodedName.charAt(i);
            if (c == KEY_CHAR) {
                // parse a byte
                bytes[byteOffset ++]
                    = (byte) Integer.parseInt(
                                encodedName.substring(i + 1, i + 3), 16);
                i += 2;

                // if no more bytes exist, decode the bytes (flush the buffer)
                if (encodedName.length() <= i + 3
                        || encodedName.charAt(i + 3) != KEY_CHAR) {
                    byte[] tmpBytes = new byte[byteOffset];
                    System.arraycopy(bytes, 0, tmpBytes, 0, byteOffset);
                    try {
                        result.append(new String(tmpBytes, "UTF-8"));
                    } catch (UnsupportedEncodingException ex) {
                        // UTF-8 is always supported in java
                        throw new RuntimeException(ex);
                    }
                    byteOffset = 0;
                }
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * Encodes a single character into a string containing the KEY_CHAR
     * followed by numeric characters representing the hexidecimal digits
     * for a given byets that make of the character in UTF-8.  This encoding is
     * identical to the process used for URL encoding.
     */
    protected static String encodeChar(char c) {
        try {
            StringBuffer result = new StringBuffer();
            for (byte b : String.valueOf(c).getBytes("UTF-8")) {
                result.append(KEY_CHAR + Integer.toHexString(b).toUpperCase());
            }
            return result.toString();
        } catch (UnsupportedEncodingException ex) {
            // can't happen, java always supports UTF-8
            throw new RuntimeException(ex);
        }
    }

    /**
     * Determines if a character can be included in a local name or must
     * be encoded.
     */
    protected static boolean isValidLocalNameCharacter(char c) {
        return isValidXMLChar(c)
                && c != '/'
                && c != ':'
                && c != '['
                && c != ']'
                && c != '|'
                && c != '*';
    }

    /**
     * Determines if a character is a valid XML character as specified
     * here http://www.w3.org/TR/xml/#NT-Char.
     */
    protected static boolean isValidXMLChar(char c) {
        return c == 0x9
                || c == 0xA
                || c == 0xD
                || ( c >= 0x20 && c <= 0xD7FF)
                || (c >= 0xE000 && c <= 0xFFFD)
                || (c >= 0x10000 && c <= 0x10FFFF);
    }
}
