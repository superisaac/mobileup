/** %LICENSE% **/

package mobileup.util;
import java.io.*;

abstract class Base64Buffer
{
    protected byte[] buffer;
    protected int pointer = 0;    
    abstract protected void flush(byte [] data, int sz) throws IOException;
    abstract public void feed(byte[] srcBytes, int offset, int sz) throws IOException;

    public void finish() throws IOException {
        if(pointer > 0) {
            flush(buffer, pointer);
            pointer = 0;
        }
    }

    public Base64Buffer(int bufferSize) {
        buffer = new byte[bufferSize];
        pointer = 0;
    }
}

