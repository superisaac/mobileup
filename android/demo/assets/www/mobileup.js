/** 
 * MobileUp: A javascript development framework in android/html5 browsers
 * Copyright (C) 2009-2010 Zeng Ke
 * 
 * Licensed under to term of GNU General Public License Version 2 or Later (the "GPL")
 *   http://www.gnu.org/licenses/gpl.html
 *
 **/

if(window.WebSocket == undefined) {
    // Add web socket features for android webview, it maybe absoleted
    // in the future
    (function () {
	var global = window;
	var WebSocket = global.WebSocket = function (url) {
	    // listener to overload
	    this.onopen = null;
	    this.onmessage = null;
	    this.onerror = null;
	    this.onclose = null;
	    
	    this._handler = WebSocketFactory.getNew(url);
	    WebSocket.registry[this._handler.getIdentifier()] = this;
	    
	    this.readyState = WebSocket.CONNECTING;
	};
	
	WebSocket.registry = {};
	
	WebSocket.triggerEvent = function (evt) {
	    WebSocket.__open
	    WebSocket.registry[evt.target]['on' + evt.type].call(global, evt);
	}
	
	WebSocket.prototype.send = function (data) 
	{
	    this._handler.send(data);
	}
	
	WebSocket.prototype.close = function () 
	{
	    this._handler.close(data);
	}	
	
    })();
}

function defer(related) {
    this._cb = null;
    this._eb = null;
    this.called = false;
    this.called_arguments = [];
    this.error_arguments = []
    this.related = related;
}

defer.prototype.callback = function(cbfunc) {
    if(this.called) {
	cbfunc.apply(this.related, this.called_arguments);
    } else {
	this._cb = cbfunc;
    }
    return this;
}

defer.prototype.errback = function(cbfunc) {
    if(this.called) {
	cbfunc.apply(this.related, this.error_arguments);
    } else {
	this._eb = cbfunc;
    }
    return this;
}


defer.prototype.err = function() {
    if(!this.called && this._eb) {
	this.error_arguments = arguments;
	this._eb.apply(this.related, arguments);
	this._eb = null;
	this.called = true;
    }
}

defer.prototype.call = function() {
    if(!this.called && this._cb) {
	this.called_arguments = arguments;
	this._cb.apply(this.related, arguments);
	this._cb = null;
	this.called = true;
    }
}

window.mobileUp = (function() {
    var calling_pool = new Object();
    var queue_pool = new Object();
    var calling_id = 1;
    var ws = null;
    var connect_defer = new defer();
    var disconnect_defer = new defer();

    function disconnect(cbfunc) {
	if(cbfunc != undefined) {
	    disconnect_defer.callback(cbfunc); 
	    return;
	}
	if(ws != null) {
	    ws.close();
	}
    }

    function connect(cbfunc) {
	if(cbfunc != undefined) {
	    connect_defer.callback(cbfunc);
	    return;
	}

	if(ws != null) {
	    ws.close();
	}
	ws = new WebSocket("ws://" + window.location.host + "/websockets");

	ws.onopen = function(evt) {
	    connect_defer.call(ws);
	}

	ws.onmessage = function(evt) {
	    var t = /^(\S+)(( +\S+)*)(\n(.*))?$/.exec(evt.data);
	    if(t) {
		var msg = new Object();
		msg.command = t[1];
		msg.args = t[2].trim().split(/\s+/g);
		msg.attachment = t[5]? t[5].trim(): '';
		if(msg.command == "call.return") {
		    var call_id = parseInt(msg.args[0]);
		    var d = calling_pool[call_id];
		    if(d != undefined) {
			d.call(JSON.parse(msg.attachment).v);
			delete(calling_pool[call_id]);
		    }
		} else if(msg.command == "call.error") {
		    var call_id = parseInt(msg.args[0]);
		    var d = calling_pool[call_id];
		    if(d != undefined) {
			d.err(JSON.parse(msg.attachment).message);
			delete(calling_pool[call_id]);
		    }
		} else if(msg.command == 'ntf') {
		    var qname = msg.args[0];
		    var evt = document.createEvent('Event');
		    evt.initEvent('queue.' + qname, true, true);
		    var args = [];
		    for(var i=1; i<msg.args.length; i++) {
			args.push(msg.args[i]);
		    }
		    evt.data = args;
		    document.dispatchEvent(evt);
		}
	    }
	}
	ws.onclose = function(evt) {
	    disconnect_defer.call();
	    ws = null;
	}
    } // end of function connect

    var callRPC = function(obj, method) {
	    calling_id++;
	    var deferred = new defer();
	    calling_pool[calling_id] = deferred;

	    var data = "call " + calling_id + " " + obj + " " + method;
	    if(arguments.length > 2) {
		var args = new Array();
		for(var i=2; i<arguments.length; i++) {
		    args.push(arguments[i]);
		}
		data += '\n' + JSON.stringify(args);
	    }	    
	    ws.send(data);
	    return deferred;
    };

    return {
	connect: connect,
	disconnect: disconnect,
	system: {
	    listDir: function(dire) { return callRPC("system", "listdir", dire);}
	},

	phone: {
	    getInfo: function() { return callRPC("phone", "getinfo");},
	    listContact: function() { return callRPC("phone", "listcontact");},
	    sendSms: function(addr, text) { return callRPC("phone", "sendsms", addr, text);},
	    listSms: function(offset, limit) { return callRPC("phone", "listsms", offset, limit);}
	},
	queue: {
	    sub: function(qname) {
		ws.send("sub " + qname);
	    },

	    unsub: function(qname) {
		ws.send("unsub " + qname);
	    },

	    put: function(qname, data) {
		ws.send("put " + qname + " " + data);
	    }
	},

	browser: {
	    page_params: function() {
		var params = new Object();
		var search = window.location.search;
		var regexp = /(\w+)=([^&#=]*)/;
		var r = regexp.exec(search);
		while(r) {
		    params[r[1]] = r[2];
		    search = search.substring(r.index + r[0].length);
		    r = regexp.exec(search);
		}
		return params;
	    }()
	}
    };
})();
