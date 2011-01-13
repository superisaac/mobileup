/** 
 * MobileUp: A javascript development framework in android/html5 browsers
 * Copyright (C) 2009-2010 Zeng Ke
 * 
 * Licensed under to term of GNU General Public License Version 2 or Later (the "GPL")
 *   http://www.gnu.org/licenses/gpl.html
 *
 **/

package mobileup.util;
import java.io.*;

public abstract class Base64Encoder extends Base64Buffer
{
    private static byte[] alphabet = new byte[]{
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '+', '/'
    };

    public Base64Encoder() {
        this(256);
    }
    public Base64Encoder(int bufferSize) {
        super(4 * bufferSize);
    }

    public void feed(byte[] src, int offset, int sz) throws IOException {
        int shiftedLength = (int)(sz / 3) * 3;
        int idx = offset;
        byte p;
        int a;
        for(;idx < shiftedLength;) {
            p = src[idx++];
            a = p >> 2;

            buffer[pointer++] = alphabet[a];

            a = (p & 0x3) << 4;
            p = src[idx++];

            a += (p>>4);
            buffer[pointer++] = alphabet[a];

            a = (p & 0xf) << 2;
            p = src[idx++];
            a += p >> 6;

            buffer[pointer++] = alphabet[a];

            a = p & 0x3f;
            buffer[pointer++] = alphabet[a];

            if(pointer >= buffer.length) {
                flush(buffer, pointer);
                pointer = 0;
            }
        }
        int n = sz - shiftedLength;

        if(n == 1) {
            p = src[idx++];
            a = p >> 2;
            buffer[pointer++] = alphabet[a];
            a = (p & 0x3) << 4;
            buffer[pointer++] = alphabet[a];

            buffer[pointer++] = '=';
            buffer[pointer++] = '=';
            if(pointer >= buffer.length) {
                flush(buffer, pointer);
                pointer = 0;
            }
        } else if(n == 2) {
            p = src[idx++];
            a = p >> 2;
            buffer[pointer++] = alphabet[a];

            a = (p & 0x3) << 4;
            p = src[idx++];
            a += p >> 4;
            buffer[pointer++] = alphabet[a];

            a = (p & 0xf)<<2;
            buffer[pointer++] = alphabet[a];
            buffer[pointer++] = '=';
            if(pointer >= buffer.length) {
                flush(buffer, pointer);
                pointer = 0;
            }
        }
    }
}

class StringEncoder extends Base64Encoder
{
    private StringBuffer sbuffer;
    public StringEncoder() {
        super();
        sbuffer = new StringBuffer();
    }
    protected void flush(byte[] data, int sz) throws IOException{
        sbuffer.append(new String(data, 0, sz));
    }

    public String getValue() {
        return String.valueOf(sbuffer);
    }
}

class FileEncoder extends Base64Encoder
{
    private FileOutputStream file;
    public FileEncoder(File f) throws FileNotFoundException {
        super();
        file = new FileOutputStream(f);
    }

    public void flush(byte[] data, int sz) throws IOException{
        file.write(data, 0, sz);
        file.flush();
    }
}
