function format(fmt) {
    var len = arguments.length;
    for(var i=1; i<len; i++) {
	fmt = fmt.replace("%s", arguments[i]);
    }
    return fmt;
}

window.subscribed = false;
function sensor_control(anchor, sensor_type) {
    subscribed = !subscribed;
    if(subscribed) {
        mobileUp.queue.sub(sensor_type);
        anchor.innerHTML = "Unsub sensor";
    } else {
        mobileUp.queue.unsub(sensor_type);
        anchor.innerHTML = "Sub sensor";
    }
}

/** orientation sensor demo **/
function on_sensor_demo_init() {
    var trig1 = new trig(new Vector(0, 0), new Vector(-5, 50), new Vector(5, 50));
    var trig2 = new trig(new Vector(0, 0), new Vector(-5, 50), new Vector(5, 50));
    var trig3 = new trig(new Vector(0, 0), new Vector(-5, 50), new Vector(5, 50));
    
    var canvas;
    var context;
    
    var angle1 = 0;
    var angle2 = 0;
    var angle3 = 0;


    function draw_angles()
    {
	context.fillRect(0, 0, canvas.width, canvas.height);
	trig1.rotate(angle1).draw(context);
	trig2.rotate(angle2).draw(context);
	trig3.rotate(angle3).draw(context);
    }
    
    document.addEventListener("queue.sen.ori", function(evt) {
	angle1 = parseInt(evt.data[0]);
	if(evt.data.length > 1) {
	    angle2 = parseInt(evt.data[1]);
	    if(evt.data.length > 2) {
		angle3 = parseInt(evt.data[2]);
	    }
	}
	draw_angles();
    });
    mobileUp.connect();

    canvas = document.getElementById("area-canvas");
    context = canvas.getContext("2d");
    context.strokeStyle = "#000";
    context.fillStyle = "#eee";
    context.lineWidth = 1;

    trig1.moveToXY(canvas.width/4, canvas.height/4);
    trig1.draw(context);

    trig2.moveToXY(canvas.width * 3/4, canvas.height/4);
    trig2.draw(context);

    trig3.moveToXY(canvas.width/4, canvas.height * 3/4);
    trig3.draw(context);
}

/** Gravity sensor demo */
function on_acc_demo_init() {
    var trig1 = new trig(new Vector(0, 0), new Vector(-10, 100), new Vector(10, 100));
    trig1 = trig1.rotate(270);
    
    var canvas;
    var context;
    
    function draw_trig(r)
    {
	context.fillRect(0, 0, canvas.width, canvas.height);
	trig1.rotateRadian(r).draw(context);
    }
    
    document.addEventListener("queue.sen.acc", function(evt) {
	if(evt.data.length < 3) {
	    return;
	}
	var gx = parseFloat(evt.data[0]);
	var gy = parseFloat(evt.data[1]);
	var gz = parseFloat(evt.data[2]);

	var radian = Math.atan2(-gy, -gx);
	draw_trig(radian);
    });
    mobileUp.connect();

    canvas = document.getElementById("area-canvas");
    context = canvas.getContext("2d");
    context.strokeStyle = "#000";
    context.fillStyle = "#eee";
    context.lineWidth = 1;

    trig1.moveToXY(canvas.width/2, canvas.height/2);
    trig1.draw(context);
}


/** sdcard explorer demo **/
window.pwd = "";

function opendir(newdir) {
    mobileUp.system.listDir(newdir).callback(function(args) { 
        window.pwd = newdir;
        gotlistdir(args);
    });
}

function openparentdir() {
    var pdir = window.pwd.replace(/\/[^\/]*$/, "");
    opendir(pdir);
}

function gotlistdir(files) {
    var ul = document.getElementById('filelist');
    var html = "";
    for(var i=0; i<files.length; i++) {
        var entry = files[i];
        if(entry.isDir) {
            var subdir = window.pwd + "/" + entry.fileName;
            html += format('<li><a href="javascript:void(0);" onclick="javascript:opendir(\'%s\');">%s</a></li>', subdir, entry.fileName);
        }  else {
            if(entry.fileName.search(/\.(jpg|gif|png)$/) >= 0 && entry.size < 1024 * 1024) {
	        html += format('<li><a href="/sd%s/%s" target="_blank">%s</a> (%s)</li>',
			       window.pwd, entry.fileName, entry.fileName, entry.size);
            } else {
                html += format('<li>%s (%s)</li>', entry.fileName, entry.size);
            }
        }
    }
    ul.innerHTML = html;
}

function on_sdcard_explorer_init() {
    mobileUp.connect(function() {
	mobileUp.system.listDir(window.pwd).callback(gotlistdir);
    })
    mobileUp.connect();
}

/** contact list demo **/
function got_contact_list(contact_list) {
    var ul = document.getElementById('contact-list');
    var html = "";
    for(var i=0; i<contact_list.length; i++) {
        var entry = contact_list[i];
        html += format('<li>%s <a href="sendsms_demo.html?p=%s">%s</a></li>', entry.name, entry.phone, entry.phone);
    }
    ul.innerHTML = html;
}

function listContact() {
    mobileUp.phone.listContact().callback(got_contact_list);
}

function on_contact_explorer_init() {
    mobileUp.connect(function() {
	mobileUp.phone.listContact().callback(got_contact_list);
    })
    mobileUp.connect();
}


/** send sms demo **/

function send_sms() {
    var text = document.getElementById("id_text").value;
    var phone = document.getElementById("id_phone").value;
    if(text && phone) {
	mobileUp.phone.sendSms(phone, text).callback(sms_sent).errback(sms_sent_err);
    }
}

function sms_sent_err(msg) {
    console.info("sms sent error", msg);
}

function sms_sent(result) {
    document.getElementById("id_text").value = '';
    document.getElementById("id_phone").value = '';
}

function set_phoneno(pno) {
    if(pno == undefined) {
        pno = "";
    }
    document.getElementById('id_phone').value = pno;
}

function on_send_sms_init() {
    mobileUp.connect(function() {
	var t = mobileUp.browser.page_params.p;
        if(t) {
            set_phoneno(t);
        } else {
	    mobileUp.phone.getInfo().callback(function(info){
                set_phoneno(info.phoneNo);
            });
        }
    })
    mobileUp.connect();
}
/** sms list demo **/
window.curr_p = 0;
window.page_size = 20;

function next_page() {
    var p = curr_p + 1;
    mobileUp.phone.listSms(p * page_size, page_size + 1).callback(function (sms_list) {
	if(sms_list.length > page_size) {
            curr_p = p;
        }
        got_sms_list(sms_list);
    });
}

function prev_page() {
    var p = curr_p - 1;
    if(p < 0) {
        p = 0;
    }
    mobileUp.phone.listSms(p * page_size, page_size + 1).callback(function (sms_list) {
        curr_p = p;
        got_sms_list(sms_list);
    });
}

function listsms() {
    mobileUp.phone.listSms(0, page_size).callback(got_sms_list);
}

function got_sms_list(sms_list) {
    var ul = document.getElementById('sms-list');
    var html = "";
    for(var i=0; i<sms_list.length && i<page_size; i++) {
        var entry = sms_list[i];
        html += format('<li><a href="sendsms_demo.html?p=%s">%s</a><br/>%s</li>', entry.addr, entry.addr, entry.body);
    }
    ul.innerHTML = html;
}

function on_sms_list_init() {
    mobileUp.connect(function() {
        listsms();
    })
    mobileUp.connect();
}
