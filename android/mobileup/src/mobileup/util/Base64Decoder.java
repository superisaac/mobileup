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

public abstract class Base64Decoder extends Base64Buffer
{
    private byte index(int b) {
	byte r ;
        if(b >= 'A' && b <= 'Z') {
            r = (byte)(b - 'A');
        } else if(b >= 'a' && b <= 'z') {
            r = (byte)(b - 'a' + 26);
        } else if(b >= '0' && b <= '9') {
            r = (byte)(b - '0' + 52);
        } else if(b == '+') {
            r = 62;
        } else { //
            assert(b == '/');
            r = 63;
        }
	return r;
    }

    public void feed(byte[] srcBytes, int offset, int sz) throws IOException {
        assert(sz % 4 == 0);
	int i = offset;
        while(i < offset + sz) {
            byte a = index(srcBytes[i++]);
            byte b = index(srcBytes[i++]);
            
            buffer[pointer++] = (byte)((a << 2) + (b >> 4));

            if(srcBytes[i] == '=') {
                break;
            }
            byte c = index(srcBytes[i++]);
            buffer[pointer++] = (byte)(((b & 0xf) << 4) + (c >> 2));
            if(srcBytes[i] == '=') {
                break;
            }
            a = index(srcBytes[i++]);
            buffer[pointer++] = (byte)(((c & 0x3) << 6) + a);

            if(pointer >= buffer.length) {
                // Buffer full, flush data
                flush(buffer, pointer);
                pointer = 0;
            }
        }
    }    

    public Base64Decoder() {
        this(256);
    }

    public Base64Decoder(int buffSize) {
        super(3 * buffSize);
    }
}

class StringDecoder extends Base64Decoder
{
    private StringBuffer sbuffer;
    public StringDecoder() {
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


class FileDecoder extends Base64Decoder
{
    private FileOutputStream file;
    public FileDecoder(File f) throws FileNotFoundException {
        super();
        file = new FileOutputStream(f);
    }

    public void flush(byte[] data, int sz) throws IOException{
        file.write(data, 0, sz);
        file.flush();
    }
}
