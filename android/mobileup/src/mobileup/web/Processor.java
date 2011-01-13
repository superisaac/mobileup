/** 
 * MobileUp: A javascript development framework in android/html5 browsers
 * Copyright (C) 2009-2010 Zeng Ke
 * 
 * Licensed under to term of GNU General Public License Version 2 or Later (the "GPL")
 *   http://www.gnu.org/licenses/gpl.html
 *
 **/
package mobileup.web;

public abstract class Processor
{
    public abstract void process(HttpProtocol request);
    public void processData(HttpProtocol request, byte[] data, int offset, int numRead){}
    public void processConnectionLost() {}
}