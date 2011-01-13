/** %LICENSE% **/
package mobileup.network.protocol;
import mobileup.network.Protocol;
import mobileup.util.Log;
import java.nio.charset.Charset;
import java.io.*;

class LineBuffer
{
    public int start = 0;
    public int end = 0;
    public byte[] buffer;
    public int capacity = 0;
    public LineBuffer(int capacity) {
        this.capacity = capacity;
        buffer = new byte[capacity];
    }
    public int length() {
        return end - start;
    }

    public void put(byte[] b, int offset, int count) {
        synchronized(this) {
            if(end + count > capacity) {
                int newcap = length() + count + 1024;
                byte[] newbuffer = new byte[newcap];
                System.arraycopy(buffer, start, newbuffer, 0, length());
                buffer = newbuffer;
                end -= start;
                start = 0;
                capacity = newcap;
            }

            System.arraycopy(b, offset, buffer, end, count);
            end += count;
        }
    }

    public void clear() {
        start = 0;
        end = 0;
    }
    
    public int indexOf(int c) {
        int sstart = start;
        for(;sstart < end; sstart++) {
            if(buffer[sstart] == c) {
                return sstart - start;
            }
        }
        return -1;
    }

    public String getLine(int d) {
        if(d > length()) {
            d = length();
        } 
        return new String(buffer, start, d);
    }

    public int advance(int d) {
        if(d + start > end) {
            start = end;
        } else {
            start += d;
        }
        return start;
    }    
}

public class LineReceiver extends Protocol {
    public String delimiter = "\r\n";
    private LineBuffer remaining = new LineBuffer(1024);
    private boolean rawMode = false;

    public void setRawMode(boolean raw) {
	rawMode = raw;
	if(raw && remaining.length() > 0 ) {
            onRawData(remaining.buffer, remaining.start, remaining.length());
	    remaining.clear();
	}
    }

    public String setDelimiter(String delim) {
	String oldDelim = delimiter;
	delimiter = delim;
	if(!oldDelim.equals(delim) &&
	   remaining.length() != 0) {
	    onData(new byte[]{}, 0);
	}
	return oldDelim;
    }

    public void onRawData(byte[] data, int offset, int numRead) {
	// Do nothing;
    }

    public void onLineReceived(String line) {
	// Do nothing;
    }

    public void onData(byte[] data, int numRead) {
	if(rawMode) {
	    onRawData(data, 0, numRead);
	    return;
	}
        remaining.put(data, 0, numRead);
        int idx = remaining.indexOf(0x0a);
        while(idx >= 0){
            String line = remaining.getLine(idx);
            remaining.advance(idx + 1);
            onLineReceived(line);
            if(rawMode) {
                break;
            }
            idx = remaining.indexOf(0x0a);
        }
    }

    public void writeln(String line) {
	write(line+delimiter);
    }    
}