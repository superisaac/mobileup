/** %LICENSE% **/
package mobileup.web;
import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import mobileup.util.Log;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class ChallengeException extends Exception
{
}

public class WebSocketProcessor extends Processor
{
    private String remaining = "";
    private byte[] clgBytes = new byte[8];
    private int  clgBytesIndex = 0;
    private int state = -1;
    protected HttpProtocol protocol;
    public static byte[] dataFrameHead = new byte[]{0};
    public static byte[] dataFrameTail = new byte[]{-1};
    
    public static byte[] getChallenge(String key1, String key2, byte[] clg) throws NoSuchAlgorithmException {
        try {
            byte[] part1 = calculateChallengePart(key1);
            byte[] part2 = calculateChallengePart(key2);
            //generateChallengeResponse(part1, part2, clg);
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(part1);
            digest.update(part2);
            digest.update(clg);
            return digest.digest();
        } catch(ChallengeException e) {
            return null;
        }
    }

    private void processChallenge(HttpProtocol request, byte[] clg) {
	String key1 = request.headers.get("sec-websocket-key1");
        String key2 = request.headers.get("sec-websocket-key2");
        try {
            byte[] resp = getChallenge(key1, key2, clg);
            if(resp == null) {
                Log.e("Websocket", "Challenge failed");
                request.response.finish();
            } else {
                request.response.beginWebSocket(resp);
                send("Hello");
            }
        } catch(NoSuchAlgorithmException e) {
            Log.e("Websocket", "No digest algorithm MD5, aborting");
            request.response.finish();
        }
    }

    private static byte[] calculateChallengePart(String key) throws ChallengeException {
        long number = 0;
        int spaces = 0;
        for(char c: key.toCharArray()){
            if(Character.isDigit(c)) {
                number = 10 * number + Character.digit(c, 10);
            } else if(Character.isWhitespace(c)) {
                spaces++;
            }
        }
        if(spaces == 0) {
            throw new ChallengeException();
        }
        int keynumber = (int)(number / spaces); //(int)Math.floor(number / spaces);
        // Java stores data in big endian
        /*ByteArrayOutputStream baos = new ByteArrayOutputStream(4);
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeInt(keynumber);
        } catch(IOException e) {
            Log.e("Websocket", "writting byte array error " + e.toString());
            }*/
        //return baos.toByteArray();
        byte[] packed = new byte[4];
        packed[3] = (byte)(keynumber & 0xff);
        packed[2] = (byte)((keynumber >> 8) & 0xff);
        packed[1] = (byte)((keynumber >> 16) & 0xff);
        packed[0] = (byte)((keynumber >> 24) & 0xff);
        return packed;
    }

    @Override
    public void processData(HttpProtocol request, byte[] data, int offset, int numRead) {
        Response response = request.response;
        int startPos = 0;
        for(int i=offset; i < offset + numRead; i++) {
            byte c = data[i];
            if(state == -1) {
                clgBytes[clgBytesIndex++] = c;
                if(clgBytesIndex >= 8) {
                    state = 0;
                    processChallenge(request, clgBytes);
                    clgBytesIndex = 0;
                }
            } else if(state == 0) {
                if(c != 0) {
                    response.finish();
                    state = 5; // Error
                    return;
                } else {
                    state = 1;
                    startPos = i + 1;
                }
            } else if(state == 1) {
                if(c == -1||c == 0xff) {
                    String msg = new String(data, startPos, i - startPos);
                    if(remaining.length() > 0) {
                        onMessage(remaining + msg);
                    } else {
                        onMessage(msg);
                    }
                    remaining = "";
                    state = 0;
                }
            }
        }
        if(state == 1) {
            remaining += new String(data, startPos, numRead - startPos);
        }
    }

    protected void onMessage(String message) {
	//
    }

    public void send(final byte[] message) {
        protocol.response.send(dataFrameHead, 0, 1);
        protocol.response.send(message);
        protocol.response.send(dataFrameTail, 0, 1);
    }

    public void send(String message) {
        send(message.getBytes());
    }

    public void process(HttpProtocol request) {
	//Response response = request.response;
        protocol = request;
    }
}
