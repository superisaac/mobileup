/** 
 * MobileUp: A javascript development framework in android/html5 browsers
 * Copyright (C) 2009-2010 Zeng Ke
 * 
 * Licensed under to term of GNU General Public License Version 2 or Later (the "GPL")
 *   http://www.gnu.org/licenses/gpl.html
 *
 **/
package mobileup.sensorweb;

public class Message {
    public static String[] VOIDARGS = new String[]{};
    public String command;
    public String[] args = VOIDARGS;
    public String payload = null;

    public Message(String packet) {
        String[] a = packet.split("\n", 2);
        String line = a[0];
        if(a.length > 1) {
            payload = a[1];
        }

        a = line.split(" ", 2);
        command = a[0];
        if(a.length > 1) {
            args = a[1].split(" ");
        }
    }
}
