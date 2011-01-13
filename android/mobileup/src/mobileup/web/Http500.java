/** %LICENSE% **/
package mobileup.web;

public class Http500 extends RuntimeException
{
    public Http500(String msg) {
	super(msg);
    }
}
