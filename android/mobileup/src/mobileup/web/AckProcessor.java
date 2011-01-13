/** 
 * MobileUp: A javascript development framework in android/html5 browsers
 * Copyright (C) 2009-2010 Zeng Ke
 * 
 * Licensed under to term of GNU General Public License Version 2 or Later (the "GPL")
 *   http://www.gnu.org/licenses/gpl.html
 *
 **/

package mobileup.web;
import java.io.*;
import java.net.*;
import java.util.*;
import mobileup.util.Log;

public class AckProcessor extends Processor
{
    public void process(HttpProtocol request) {
	Response response = request.response;
	String ack = "Ack";
	response.begin(ack.length());
	response.send(ack);
	response.finish();
    }
}