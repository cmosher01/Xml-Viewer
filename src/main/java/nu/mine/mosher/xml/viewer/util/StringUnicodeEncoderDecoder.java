package nu.mine.mosher.xml.viewer.util;

import java.util.Objects;

/**
 * This class provides Unicode conversion utility methods that allow to convert a string into Unicode sequence and vice-versa. (See methods
 * descriptions for details)
 *
 * @author Michael Gantman
 * <p>
 * The MIT License (MIT) Copyright © 2015 Michael Gantman
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF
 * OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
public class StringUnicodeEncoderDecoder {
    private final static String UNICODE_PREFIX = "\\u";
    private final static String UPPER_CASE_UNICODE_PREFIX = "\\U";
    private final static String UPPER_CASE_UNICODE_PREFIX_REGEX = "\\\\U";
    private final static String DELIMITER = "\\\\u";

    // strip trailing newline
    public static String filter(String s) { // short name
        s = StringUnicodeEncoderDecoder.encodeStringToUnicodeSequence(s);
        if (s.endsWith("\n")) {
            s = s.substring(0, s.length()-1);
        }
        return s;
    }

    /**
     * This method converts a {@link String} of characters in any language into a String That contains a sequence of Unicode codes corresponding to
     * characters in the original String For Example String "Hello" will be converted into a String "\u005c\u00750048\u005c\u00750065\u005c\u0075006c\u005c\u0075006c\u005c\u0075006f" Null or empty
     * String conversion will return an empty String
     *
     * @param txt {@link String} that contains a sequence of characters to convert
     * @return {@link String} non-ASCII characters as backslash-u-xxxx escapes, spaces as ".", and linefeeds after U+000a escapes, "\" as "\\"
     * or "[NULL]" if null
     *
     * Modifications, 2019, Christopher Alan Mosher. Same license.
     */
    public static String encodeStringToUnicodeSequence(final String txt) throws IndexOutOfBoundsException {
        if (Objects.isNull(txt)) {
            return "[NULL]";
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < txt.length(); i++) {
            final int cp = Character.codePointAt(txt, i);
            final String uni;
            if (cp == 0x20) {
                uni = ".";
            } else if (cp == 0x5c) {
                uni = "\\\\";
            } else if (cp < 0x20 || 0x7f <= cp) {
                uni = convertCodePointToUnicodeString(cp);
            } else {
                uni = Character.toString((char)cp);
            }
            result.append(uni);
            if (cp == 0x0a) {
                result.append('\n');
            }
            if (Character.isHighSurrogate(txt.charAt(i))) {
                i++;
            }
        }
        return result.toString();
    }

    /**
     * This method converts {@link String} that contains a sequence of Unicode codes onto a String of corresponding characters. For example a String
     * "\u005c\u00750048\u005c\u00750065\u005c\u0075006c\u005c\u0075006c\u005c\u0075006f" will be converted into String "Hello" by this method. This method performs reverse conversion of the one
     * performed by method {@link #encodeStringToUnicodeSequence(String)} I.e. Any textual String converted into sequence of Unicode codes by method
     * {@link #encodeStringToUnicodeSequence(String)} may be retrieved back by invoking this method on that Unicode sequence String.
     *
     * @param unicodeSequence {@link String} That contains sequence of Unicode codes. Each code must be in hexadecimal format and must be preceded by
     *                        "'backslash' + 'u'" prefix. (note that prefix '\U' is now valid as opposed to earlier versions). This method allows
     *                        leading and trailing whitespaces for the whole String as well as spaces between codes. Those white spaces will be ignored.
     * @return {@link String} That contains sequence of characters that correspond to the respective Unicode codes in the original String
     * @throws IllegalArgumentException if input String is in invalid format. For example if any code is not in hexadecimal format or the code is not a valid Unicode code
     *                                  (not valid code point).
     */
    public static String decodeUnicodeSequenceToString(String unicodeSequence) throws IllegalArgumentException {
        StringBuilder result = new StringBuilder();
        try {
            unicodeSequence = replaceUpperCase_U_WithLoverCase(unicodeSequence);
            unicodeSequence = unicodeSequence.trim().substring(UNICODE_PREFIX.length());
            for (String codePointStr : unicodeSequence.split(DELIMITER)) {
                result.append(Character.toChars(Integer.parseInt(codePointStr.trim(), 16)));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error occurred while converting unicode sequence String to String", e);
        }
        return result.toString();
    }

    private static String replaceUpperCase_U_WithLoverCase(String unicodeSequence) {
        String result = unicodeSequence;
        if(unicodeSequence != null && unicodeSequence.contains(UPPER_CASE_UNICODE_PREFIX)) {
            result = unicodeSequence.replaceAll(UPPER_CASE_UNICODE_PREFIX_REGEX, DELIMITER);
        }
        return result;
    }

    /**
     * This method converts an integer that holds a unicode code value into a String
     *
     * @param codePoint a unicode code value
     * @return {@link String} that starts with prefix "'backslash' + 'u'" that follows with hexadecimal value of an integer. If the hexadecimal value
     *         of an integer is less then four digits the value is padded with preceding zeros. For example if the integer has value 32 (decimal) it
     *         will be converted into String "\u0020"
     */
    private static String convertCodePointToUnicodeString(int codePoint) {
        StringBuilder result = new StringBuilder(UNICODE_PREFIX);
        String codePointHexStr = Integer.toHexString(codePoint);
        codePointHexStr = codePointHexStr.startsWith("0") ? codePointHexStr.substring(1) : codePointHexStr;
        if (codePointHexStr.length() <= 4) {
            result.append(getPrecedingZerosStr(codePointHexStr.length()));
        }
        result.append(codePointHexStr);
        return result.toString();
    }

    /**
     * This method receives a length of a String and if it is less then 4 it generates a padding String of zeros that can be appended to the String to
     * make it of length 4 I.e. if parameter passed is 1 the returned String will be "000". If the parameter passed is 4 or greater empty String is
     * returned.
     *
     * @param codePointStrLength Length of a String to be padded by preceding zeros to the length of 4
     * @return padding String
     */
    private static String getPrecedingZerosStr(int codePointStrLength) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 4 - codePointStrLength; i++) {
            result.append("0");
        }
        return result.toString();
    }
}
