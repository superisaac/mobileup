/** %LICENSE% **/
package mobileup.web;

public abstract class Processor
{
    public abstract void process(HttpProtocol request);
    public void processData(HttpProtocol request, byte[] data, int offset, int numRead){}
    public void processConnectionLost() {}
}