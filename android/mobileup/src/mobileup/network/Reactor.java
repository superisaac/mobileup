/** 
 * MobileUp: A javascript development framework in android/html5 browsers
 * Copyright (C) 2009-2010 Zeng Ke
 * 
 * Licensed under to term of GNU General Public License Version 2 or Later (the "GPL")
 *   http://www.gnu.org/licenses/gpl.html
 *
 **/
package mobileup.network;
import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;
import java.util.concurrent.*;
import java.nio.*;
import mobileup.util.Log;

class ChangeRequest {
    public static final int REGISTER = 1;
    public static final int CHANGEOPS = 2;

    public SocketChannel channel;
    public int type;
    public int ops;
    public Object attachment;

    public ChangeRequest(SocketChannel channel, int type, int ops, Object attach) {
	this.channel = channel;
	this.type = type;
	this.ops = ops;
	this.attachment = attach;
    }
}

public class Reactor extends Thread
{
    public boolean running = false;
    private Selector selector;
    private boolean cont = true;
    private ByteBuffer readBuffer = ByteBuffer.allocate(1024);
    private List<ChangeRequest> pendingChanges = new LinkedList<ChangeRequest>();
    private Map<SocketChannel, List<ByteBuffer>> pendingData = new HashMap<SocketChannel, List<ByteBuffer>>();
    ExecutorService threadPool;

    private Reactor() {
	setDaemon(true);
	try {
	    selector = initSelector();
	}catch(IOException e) {
	    e.printStackTrace();
	    throw new RuntimeException(e.toString());
	}
    }

    private static Reactor _instance;
    public static Reactor getInstance() {
	if(_instance == null) {
	    _instance = new Reactor();
	}
	return _instance;
    }
    
    private Selector initSelector() throws IOException{
	return SelectorProvider.provider().openSelector();
    }

    public void listen(InetAddress host, int port, IFactory factory) throws IOException {
	ServerSocketChannel serverChannel = ServerSocketChannel.open();
	serverChannel.configureBlocking(false);
	InetSocketAddress addr = new InetSocketAddress(host, port);
        ServerSocket socket = serverChannel.socket();
        socket.setReuseAddress(true);
        socket.setReceiveBufferSize(4096);
	socket.bind(addr, 50);
	Log.d("reactor", " " + selector);
	serverChannel.register(selector, SelectionKey.OP_ACCEPT, factory);
    }

    public void connect(InetAddress host, int port, Protocol clientProtocol) throws IOException{

	SocketChannel channel = SocketChannel.open();
	InetSocketAddress addr = new InetSocketAddress(host, port);
	channel.configureBlocking(false);
	clientProtocol.channel = channel;
	channel.connect(addr);
	synchronized(this.pendingChanges) {
	    this.pendingChanges.add(new ChangeRequest(channel, ChangeRequest.REGISTER, SelectionKey.OP_CONNECT, clientProtocol));
	}
    }

    private void handleAccept(SelectionKey key) throws IOException {
	ServerSocketChannel serverChannel = (ServerSocketChannel)key.channel();
	IFactory factory = (IFactory)key.attachment();
	SocketChannel channel = serverChannel.accept();
	Log.d("reactor", "accept");
	Protocol protocol = factory.buildProtocol();
	protocol.channel = channel;
	protocol.factory = factory;
	channel.configureBlocking(false);
        //channel.socket().setReceiveBufferSize(4096);
	channel.register(this.selector, SelectionKey.OP_READ, protocol);
	protocol.onConnectionMade();
    }

    private void closeKey(SelectionKey key, boolean call) throws IOException {
	Object attach = key.attachment();
	if(attach != null && (attach instanceof Protocol) && call) {
	    ((Protocol)attach).onConnectionLost();
	}
	key.channel().close();
	key.cancel();
    }

    private void closeKey(SelectionKey key) throws IOException {
	closeKey(key, true);
    }

    public void closeChannel(SocketChannel channel) throws IOException{
	SelectionKey key = channel.keyFor(this.selector);
        closeKey(key);
    }

    private void handleRead(SelectionKey key) throws IOException {
	SocketChannel channel = (SocketChannel) key.channel();
	this.readBuffer.clear();

	final int numRead;
	final Protocol protocol = (Protocol)key.attachment();
	try {
	    numRead = channel.read(readBuffer); //protocol.buffer); //this.readBuffer);
	} catch(IOException e) {
	    closeChannel(channel);
	    return;
	}
	if(numRead == -1) {
            closeChannel(channel);
	    return;
	}
        //byte[] fBytes = new byte[readBuffer.position];
        final byte[] fBytes = readBuffer.array();
        
	if(protocol != null) {
	    /*if(protocol.runnable) {
		if(threadPool == null) {
		    threadPool = Executors.newFixedThreadPool(2);
		}
		Runnable run = new Runnable() {
			public void run() {
			    protocol.onData(fBytes, numRead);
			}
		    };
		threadPool.execute(run);
	    } else {
		protocol.onData(fBytes, numRead);
		}*/
	    protocol.onData(fBytes, numRead);
	}
    }

    public void send(SocketChannel channel, byte[] data, int offset, int length) {
	synchronized (this.pendingChanges) {
	    this.pendingChanges.add(new ChangeRequest(channel, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE, null));
	    synchronized (this.pendingData) {
		List<ByteBuffer> queue =  this.pendingData.get(channel);
		if (queue == null) {
		    queue = new ArrayList<ByteBuffer>();
		    this.pendingData.put(channel, queue);
		}
		queue.add(ByteBuffer.wrap(data, offset, length));
	    }
	}
	this.selector.wakeup();
    }
    
    private void handleConnect(SelectionKey key) throws IOException {
	SocketChannel socketChannel = (SocketChannel) key.channel();
	socketChannel.finishConnect();
	final Protocol protocol = (Protocol)key.attachment();
	if(protocol != null) {
	    Log.d("reactor", "connection made made");
	    protocol.onConnectionMade();
	}
	key.interestOps(SelectionKey.OP_WRITE);
    }

    private void handleWrite(SelectionKey key) throws IOException {
	SocketChannel socketChannel = (SocketChannel) key.channel();
	
	synchronized (this.pendingData) {
	    List queue = (List) this.pendingData.get(socketChannel);
	    
	    // Write until there's not more data ...
	    while (!queue.isEmpty()) {
		ByteBuffer buf = (ByteBuffer) queue.get(0);
		try{
                    socketChannel.write(buf);
                }catch(IOException e) {
                    Log.e("Reactor", "peer reset " + socketChannel);
                    closeKey(key);
                    return;
                }
		if (buf.remaining() > 0) {
		    // ... or the socket's buffer fills up
		    break;
		}
		queue.remove(0);
	    }

	    if(queue.isEmpty()) {
		Protocol protocol = (Protocol)key.attachment();
		if(protocol != null && protocol.getWantClose()) {
		    closeKey(key, false);
		} else {
		    // We wrote away all data, so we're no longer interested
		    // in writing on this socket. Switch back to waiting for
		    // data.
		    key.interestOps(SelectionKey.OP_READ);
		}
	    }
	}
    }

    public void kill() {
	cont = false;
	selector.wakeup();	
	if(threadPool != null) {
	    threadPool.shutdown();
	}
    }
    
    public void onTerminate() {
	Set<SelectionKey> keys = selector.keys();
	for(SelectionKey key: keys) {
	    try {
		/*Protocol protocol = (Protocol)key.attachment();
		protocol.onConnectionLost(); 

		key.channel().close();
		key.cancel(); */
		closeKey(key);
	    }catch(IOException e) {
		e.printStackTrace();
		Log.d("Reactor", e.toString());
	    }
	}
    }

    @Override
    public void start() {
	Log.d("Reactor", "start");
	if(isRunning()) {
	    Log.d("Reactor", "Already running, exiting");
	    return;
	}
	super.start();
    }

    public boolean isRunning() {
	Thread.State state = getState();
	return state != Thread.State.NEW && state != Thread.State.TERMINATED;
    }

    public void run() {
	Log.d("reactor", "run");
	try{
	    running = true;
	    while(cont) {
		synchronized (this.pendingChanges) {
		    Iterator changes = this.pendingChanges.iterator();
		    while (changes.hasNext()) {
			ChangeRequest change = (ChangeRequest) changes.next();
			switch (change.type) {
			case ChangeRequest.CHANGEOPS:
                            {
                                SelectionKey key = change.channel.keyFor(this.selector);
				if(key != null) {
				    try {
					key.interestOps(change.ops);
				    } catch(java.nio.channels.CancelledKeyException e) {
					closeKey(key);
				    }
				}
                            }
			    break;
			case ChangeRequest.REGISTER:
			    change.channel.register(this.selector, change.ops, change.attachment);
			    break;
			}

		    }
		    this.pendingChanges.clear();
		}
		selector.select(100);
		Iterator selectedKeys = this.selector.selectedKeys().iterator();
		while(selectedKeys.hasNext()) {
		    SelectionKey key = (SelectionKey) selectedKeys.next();
		    selectedKeys.remove();
		    if(!key.isValid()) {
                        Log.e("Reactor", "key is invalid");
			continue;
		    }
		    
		    if(key.isAcceptable()) {
			this.handleAccept(key);
		    } else if(key.isConnectable()) {
			this.handleConnect(key);
		    } else if(key.isReadable()) {
			this.handleRead(key);
		    } else if(key.isWritable()) {
			this.handleWrite(key);
		    }
		}
	    }
	    running = false;
	    onTerminate();
	    //serverChannel.close();
	    selector.close();
	} catch(Exception e){
	    e.printStackTrace();
	}
	Log.d("Reactor", "thread die");
	_instance = null;
    }
}