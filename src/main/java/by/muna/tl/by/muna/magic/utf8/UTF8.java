package by.muna.tl.by.muna.magic.utf8;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class UTF8 {
    public static final Charset CHARSET = Charset.forName("UTF-8");
    private static final CharsetEncoder ENCODER = UTF8.CHARSET.newEncoder();

    // from http://stackoverflow.com/questions/8511490/calculating-length-in-utf-8-of-java-string-without-actually-encoding-it
    public static int calcSize(CharSequence sequence) {
        int count = 0;

        for (int i = 0, len = sequence.length(); i < len; i++) {
            char ch = sequence.charAt(i);
            if (ch <= 0x7F) {
                count++;
            } else if (ch <= 0x7FF) {
                count += 2;
            } else if (Character.isHighSurrogate(ch)) {
                count += 4;
                ++i;
            } else {
                count += 3;
            }
        }

        return count;
    }

    public static synchronized void encode(CharSequence sequence, ByteBuffer buffer) {
        //CharsetEncoder encoder = UTF8.CHARSET.newEncoder();
        UTF8.ENCODER.reset();

        CharBuffer charBuffer = CharBuffer.wrap(sequence);

        UTF8.ENCODER.encode(charBuffer, buffer, true);

        //encoder.flush(buffer);
    }
}
