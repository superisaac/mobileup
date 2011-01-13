/** %LICENSE% **/
package mobileup.network;
import java.nio.channels.*;
import java.nio.*;
import java.io.IOException;
import mobileup.util.Log;

public class Protocol {
    public SocketChannel channel;
    public IFactory factory;
    private boolean wantClose = false;

    public boolean getWantClose() {
        return wantClose;
    }

    public void write(byte[] data, int offset, int length) {
	Reactor.getInstance().send(channel, data, offset, length);
    }

    public void write(byte[] data) {
        write(data, 0, data.length);
    }

    public void write(String str) {
	write(str.getBytes());
    }

    public void dataWrote() {
        
    }
    
    public void close() {
        wantClose = true;
        write("");
	/*Reactor reactor = Reactor.getInstance();
	  reactor.wantClose(channel); */
    }

    public void onConnectionMade() {
	Log.d("conn", "made");
	// Do nothing
    }
    public void onConnectionLost() {
	Log.d("conn", "lost");
	// Do nothing
    }

    public void onData(byte[] data, int numRead) {
	// Do nothing
    }
}
