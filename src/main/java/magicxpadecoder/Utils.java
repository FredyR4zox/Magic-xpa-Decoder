package magicxpadecoder;

import burp.api.montoya.core.ByteArray;

import java.io.*;

public class Utils {
    // private final MontoyaApi api;

    public static final String TAG_OPEN = "<xml id=\"MGDATA\">";
    public static final String TAG_CLOSE = "</xml>";

    public static final String TAG_ERROR_OPEN = "<RIA_ERROR_RESPONSE>";

    public static final int XML_MIN_RANDOM = -48;
    public static final int XML_MAX_RANDOM = 47;
    public static final int XML_ILLEGAL_RANDOM = 41;

    public static DecodeResult encode(ByteArray content) {
        int curr = 0;
        byte currChr;
        int length = content.length();
        double random = randomScramble(content.length());
        double key = Math.floor(Math.sqrt(length)) + random;

        ByteArray output = ByteArray.byteArrayOfLength(length + 2);

        output.setByte(0, (byte) (random + 81));
        int index = 1;
        for (int i = 0; i < key; i++) {
            curr = i;
            while (curr < length) {
                currChr = content.getByte(curr);
                output.setByte(index, currChr);
                index++;
                curr += key;
            }
        }
        output.setByte(index, '_');

        DecodeResult result = new DecodeResult(output, false);

        return result;
    }

    private static double randomScramble(int len) {
        double sqrt = Math.sqrt(len);
        double low = XML_MIN_RANDOM;
        double high = XML_MAX_RANDOM;

        if (low < (-1 * sqrt / 2)) {
            low = Math.floor(-1 * sqrt / 2);
        }
        if (high > sqrt / 2) {
            high = Math.floor(sqrt / 2);
        }

        double delta = Math.random() * (high - low) + low;
        delta = Math.floor(delta);

        if (delta == XML_ILLEGAL_RANDOM) {
            delta++;
        }

        return delta;
    }

    public static DecodeResult decode(ByteArray content) {
        String originalString = content.toString();
        String filteredContent;
        int start = 0;
        int finish = originalString.length();
        // String openTag;
        // String closeTag;

        int openTagLocation = originalString.indexOf(TAG_OPEN);
        if (openTagLocation != -1) {
            start = openTagLocation + TAG_OPEN.length();
            // openTag = originalString.substring(0, start);
            finish = originalString.indexOf(TAG_CLOSE);
            // closeTag = originalString.substring(finish);

            filteredContent = originalString.substring(start, finish);
        } else {
            openTagLocation = originalString.indexOf(TAG_ERROR_OPEN);
            if (openTagLocation != -1) {
                start = openTagLocation + TAG_ERROR_OPEN.length();
                // finish = originalString.length();
                // openTag = originalString.substring(0, start);

                filteredContent = originalString.substring(start, originalString.length());
            } else {
                filteredContent = originalString;
            }
        }

        int whitespacesOffset = locateScramble(filteredContent);
        if (whitespacesOffset != 0) {
            filteredContent = filteredContent.substring(whitespacesOffset, filteredContent.length());
        }

        // System.out.println(filteredContent);

        int length = filteredContent.length() - 2;
        int key = (int) ((int) filteredContent.charAt(0) - 81 + Math.floor(Math.sqrt(length)));

        // System.out.println(filteredContent);
        // System.out.println(filteredContent.charAt(0));
        // System.out.println((int) filteredContent.charAt(0));
        // System.out.println(start);
        // System.out.println(length);
        // System.out.println(key);
        // System.out.println();

        ByteArray output = ByteArray.byteArrayOfLength(length);
        int blockSize = (int) Math.floor(length / key);
        int remainder = length % key;

        // System.out.println(blockSize);
        // System.out.println(remainder);

        int currOut = 0;
        int currIn;
        int currBlk;

        for (int i = 0; i < length; i++) {
            currIn = i;
            currBlk = 1;

            while (currIn < length && currOut < length) {
                output.setByte(currOut, filteredContent.charAt(currIn + 1));
                currIn = currIn + blockSize;

                if (currBlk <= remainder) {
                    currIn++;
                }

                currOut++;
                currBlk++;
            }
        }

        // System.out.println(output);

        String result = originalString.substring(0, start) + output.toString()
                + originalString.substring(finish, originalString.length());

        return new DecodeResult(ByteArray.byteArray(result), false);
    }

    private static int locateScramble(String content) {
        int i = 0;
        while (i < content.length() && isWhitespace(content.charAt(i))) {
            i++;
        }
        return i;
    }

    private static boolean isWhitespace(char c) {
        return c == ' ' || c == '\n' || c == '\r' || c == '\t';
    }

    // private String getStackTrace(Throwable t) {
    // StringWriter stringWriter = new StringWriter();
    // PrintWriter printWriter = new PrintWriter(stringWriter, true);
    // t.printStackTrace(printWriter);
    // printWriter.flush();
    // stringWriter.flush();

    // return stringWriter.toString();
    // }

    static class DecodeResult {
        private final ByteArray result;
        private final boolean error;

        DecodeResult(ByteArray result, boolean error) {
            this.result = result;
            this.error = error;
        }

        public ByteArray getResult() {
            return result;
        }

        public boolean getError() {
            return error;
        }
    }
}
