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

