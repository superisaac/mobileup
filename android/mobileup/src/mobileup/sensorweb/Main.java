/** 
 * MobileUp: A javascript development framework in android/html5 browsers
 * Copyright (C) 2009-2010 Zeng Ke
 * 
 * Licensed under to term of GNU General Public License Version 2 or Later (the "GPL")
 *   http://www.gnu.org/licenses/gpl.html
 *
 **/
package mobileup.sensorweb;

import mobileup.network.IFactory;
import mobileup.network.Reactor;
import mobileup.network.Protocol;
import mobileup.web.FileProcessor;
import mobileup.web.Processor;
import mobileup.web.HttpProtocol;
import mobileup.util.Log;
import java.io.IOException;
import java.security.*;
import java.nio.*;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException{
        /*String dest = Base64.encode("xxxy");
        System.out.println(dest);
        String bs = Base64.decode(dest);
	System.out.println(bs); */

        /*File tf = File.createTempFile("kkk_", "_ppp");
        FileDecoder decoder = new FileDecoder(tf);
        decoder.write(dest); */
        /*String key1 = ")12.7d5 9992  1 2";
        String key2 = "106@7  7 _Rc; lTW6]3-8U12";
        byte[] clg = new byte[]{'>', 'W', (byte)0xe4, '9', ';', '%', 
                                (byte)0xc7, (byte)0xcc};
        byte[] resp = mobileup.web.WebSocketProcessor.getChallenge(key1, key2, clg);
        Log.p("Main", resp); */

	Reactor reactor = Reactor.getInstance();
	SensorFactory sf = new SensorFactory();
	reactor.listen(null, 8899, sf);
	Log.d("Server", "Server starts at port 5899");
	reactor.start();
	reactor.join();
    }
}

class AckProtocol extends HttpProtocol
{
    public Processor route() {
	if(object.equals("/websockets")){
	    return new WireProcessor();
	}
        return new FileProcessor("./www");
    }
}

class SensorFactory implements IFactory
{
    public Protocol buildProtocol() {
	return new AckProtocol();
    }
}
