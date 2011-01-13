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

public class Base64
{
    public static String encode(String src) throws IOException{
        StringEncoder encoder = new StringEncoder();
        encoder.feed(src.getBytes("utf-8"), 0, src.length());
        encoder.finish();
        return encoder.getValue();
    }

    public static String decode(String src) throws IOException{
        StringDecoder decoder = new StringDecoder();
        decoder.feed(src.getBytes("utf-8"), 0, src.length());
        decoder.finish();
        return decoder.getValue();
    }
}