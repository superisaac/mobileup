/** 
 * MobileUp: A javascript development framework in android/html5 browsers
 * Copyright (C) 2009-2010 Zeng Ke
 * 
 * Licensed under to term of GNU General Public License Version 2 or Later (the "GPL")
 *   http://www.gnu.org/licenses/gpl.html
 *
 **/

var RADIUS = 10;
var DAMPING = 0.81
var objectCounter = 0;

// Vector class
function Vector(x, y) {
    this.x = x;
    this.y = y;
}

Vector.prototype.copy = function() {
    return new Vector(this.x, this.y);
}

Vector.prototype.add = function() {
    var x = this.x;
    var y = this.y;
    for(var i =0; i<arguments.length; i++) {
	x += arguments[i].x;
	y += arguments[i].y;
    }
    return new Vector(x, y);
}

Vector.prototype.addEqual = function(other) {
    this.x += other.x;
    this.y += other.y;
}

Vector.prototype.negative = function() {
    return new Vector(-this.x, -this.y);
}

Vector.prototype.minus = function(other) {
    return new Vector(this.x - other.x, this.y - other.y);
}

Vector.prototype.mul = function(r) {
    return new Vector(this.x * r, this.y * r);
}

Vector.prototype.dot = function(other) {
    return this.x * other.x + this.y * other.y;
}

Vector.prototype.magnitude = function() {
    return Math.sqrt(this.x * this.x + this.y * this.y);
}

Vector.prototype.normalize = function(thick) {
    var m = this.magnitude();
    if(m == 0) {
	m = 0.001;
    }
    return this.mul(thick/m);
}

Vector.prototype.rotate = function(dig) {
    var cos_t = Math.cos(dig * Math.PI/180.0);
    var sin_t = Math.sin(dig * Math.PI/180.0);
    return new Vector(this.x * cos_t + this.y * sin_t,
		      this.y * cos_t - this.x * sin_t);
}

Vector.prototype.rotateRadian = function(r) {
    var cos_t = Math.cos(r);
    var sin_t = Math.sin(r);
    return new Vector(this.x * cos_t + this.y * sin_t,
		      this.y * cos_t - this.x * sin_t);
}

// A Ball particle
function Ball(pos) {
    this.pos = pos;
    this.velocity = new Vector(0, 0);
}

Ball.prototype.collideWithOtherBall = function(other) {
    var v = this.pos.minus(other.pos);
    var r = v.magnitude();
    if(r <= RADIUS + RADIUS) {
	var vr = 0.45 * (this.velocity.magnitude() + other.velocity.magnitude());
	this.pos.addEqual(v.normalize(RADIUS - r * 0.5));
	other.pos.addEqual(v.normalize(RADIUS - r * 0.5));
	this.velocity = v.normalize(vr);
	other.velocity = this.velocity.negative();
    }
}

Ball.prototype.draw = function(context) {
    context.beginPath();
    context.arc(this.pos.x, this.pos.y, RADIUS, 0, 2 * Math.PI, false);
    context.stroke();
    context.closePath();
}

var normalVectors = [new Vector(0, 1), 
    new Vector(-1, 0),
    new Vector(0, -1),
    new Vector(1, 0)];

// Bound Checker: check if the particle/ball is collide with other
function BoundChecker(ball) {
    this.objectCounter = objectCounter++;
    this.ball = ball;
}

BoundChecker.prototype.toString = function() {
    return "BoundChecker " + this.objectCounter;
}

BoundChecker.prototype.exec = function() {
    var newpos = this.ball.pos;
    var newv = this.ball.velocity;
    var width = GROUND.width();
    var height = GROUND.height();
    if(newpos.x <= RADIUS) {
	if(normalVectors[3].dot(newv) < 0) {
	    var dx = newpos.x - RADIUS;
	    var dy = newv.y * dx / newv.x;
	    this.ball.pos.x = newpos.x - dx;
	    this.ball.pos.y = newpos.y - dy;
	    var vx = -DAMPING * newv.x;
	    if(Math.abs(vx) < 5) {
		GROUND.removeConstraint(this);
		GROUND.addConstraint(new Slider(this.ball, 3));
		vx = 0;
	    }
	    this.ball.velocity = new Vector(vx, newv.y);
	}
    } else if(newpos.x >= width - RADIUS) {
	if(normalVectors[1].dot(newv) < 0) {
	    var dx = newpos.x - width + RADIUS;
	    var dy = newv.y * dx / newv.x;
	    this.ball.pos.x = newpos.x - dx;
	    this.ball.pos.y = newpos.y - dy;
	    var vx = -DAMPING * newv.x;
	    if(Math.abs(vx) < 5) {
		GROUND.removeConstraint(this);
		GROUND.addConstraint(new Slider(this.ball, 1));
		vx = 0;
	    }
	    this.ball.velocity = new Vector(vx, newv.y);
	}
    } else if(newpos.y <= RADIUS) {
	if(normalVectors[0].dot(newv) < 0) {
	    var dy = newpos.y - RADIUS;
	    var dx = newv.x * dy / newv.y;
	    this.ball.pos.x = newpos.x - dx;
	    this.ball.pos.y = newpos.y - dy;
	    var vy = -DAMPING * newv.y;
	    if(Math.abs(vy) < 5) {
		GROUND.removeConstraint(this);
		GROUND.addConstraint(new Slider(this.ball, 0));
		vy = 0;
	    }
	    this.ball.velocity = new Vector(newv.x, vy);
	}
    } else if(newpos.y >= height - RADIUS) {
	if(normalVectors[2].dot(newv) < 0) {
	    var dy = newpos.y - height + RADIUS;
	    var dx = newv.x * dy / newv.y;
	    this.ball.pos.x = newpos.x - dx;
	    this.ball.pos.y = newpos.y - dy;
	    var vy = -DAMPING * newv.y;
	    if(Math.abs(vy) < 5) {
		GROUND.removeConstraint(this);
		GROUND.addConstraint(new Slider(this.ball, 2));
		vy = 0;
	    }
	    this.ball.velocity = new Vector(newv.x, vy);
	}
    }
}

function Slider(ball, which) {
    this.objectCounter = objectCounter++;
    this.ball = ball;
    this.which = which;
}

Slider.prototype.toString = function() {
    return "Slider " + this.objectCounter;
}

Slider.prototype.exec = function() {
    var newpos = this.ball.pos;
    var width = GROUND.width();
    var height = GROUND.height();
    if(normalVectors[this.which].dot(this.ball.velocity) > 0) {
	GROUND.removeConstraint(this);
	GROUND.addConstraint(new BoundChecker(this.ball));
	return;
    }

    switch(this.which) {
    case 0:  // top
	{
	    if(newpos.x <= RADIUS) {
		this.ball.pos.x = RADIUS;
		this.ball.velocity.x = -this.ball.velocity.x;
	    } else if(newpos.x >= width - RADIUS) {
		this.ball.pos.x = width - RADIUS;
		this.ball.velocity.x = -this.ball.velocity.x;
	    }
	    this.ball.velocity.y = 0;
	    this.ball.velocity.x = 0.93 * this.ball.velocity.x;
	    this.ball.pos.y = RADIUS;
	}
	break;
    case 1:  // right
	{
	    if(newpos.y <= RADIUS) {
		this.ball.pos.y = RADIUS;
		this.ball.velocity.y = -this.ball.velocity.y;
	    } else if(newpos.y >= height - RADIUS) {
		this.ball.pos.y = height - RADIUS;
		this.ball.velocity.y = -this.ball.velocity.y;
	    }
	    this.ball.velocity.x = 0;
	    this.ball.velocity.y = 0.93 * this.ball.velocity.y;
	    this.ball.pos.x = width - RADIUS;
	}
	break;
    case 2: // bottom
	{
	    if(newpos.x <= RADIUS) {
		this.ball.pos.x = RADIUS;
		this.ball.velocity.x = -this.ball.velocity.x;
	    } else if(newpos.x >= width - RADIUS) {
		this.ball.pos.x = width - RADIUS;
		this.ball.velocity.x = -this.ball.velocity.x;
	    }
	    this.ball.velocity.y = 0;
	    this.ball.velocity.x = 0.93 * this.ball.velocity.x;
	    this.ball.pos.y = height - RADIUS;
	}
	break;
    case 3: // left
	{
	    if(newpos.y <= RADIUS) {
		this.ball.pos.y = RADIUS;
		this.ball.velocity.y = -this.ball.velocity.y;
	    } else if(newpos.y >= height - RADIUS) {
		this.ball.pos.y = height - RADIUS;
		this.ball.velocity.y = -this.ball.velocity.y;
	    }
	    this.ball.velocity.x = 0;
	    this.ball.velocity.y = 0.93 * this.ball.velocity.y;
	    this.ball.pos.x = RADIUS;
	}
	break;
    }
}

GROUND = function(){
    var constraint_list = [];
    var particle_list = [];
    var gravity = new Vector(0, 3);
    
    var removing_list = {};
    var adding_list = [];
    var width = 600;
    var height = 600;

    function setSize(w, h) {
	width = w;
	height = h;
    }

    function removeConstraints() {
	var newc = [];
	for(var i =0; i< constraint_list.length; i++) {
	    var c = constraint_list[i];
	    if(c in removing_list) {
		continue;
	    } else {
		newc.push(c);
	    }
	}
	constraint_list = newc;
    }

    function addBall(ball) {
	particle_list.push(ball);
	constraint_list.push(new BoundChecker(ball));
    }

    function one_step(context) {
	for(var i=0; i<particle_list.length; i++) {
	    var ball = particle_list[i];
	    ball.pos.addEqual(ball.velocity);
	}

	for(var i = 0; i< particle_list.length - 1; i++) {
	    var ba = particle_list[i];
	    for(var j = i + 1; j < particle_list.length; j++) {
		ba.collideWithOtherBall(particle_list[j]);
	    }
	}

	removing_list = {};
	adding_list = [];
	for(var i = 0; i<constraint_list.length; i++) {
	    var cons = constraint_list[i];
	    cons.exec();
	}

	removeConstraints();

	for(var i = 0; i< adding_list.length;i++) {
	    constraint_list.push(adding_list[i]);
	}

	for(var i=0; i<particle_list.length; i++) {
	    var ball = particle_list[i];
	    ball.draw(context);
	    ball.velocity.addEqual(gravity);
	}
    }

    return {
	'removeConstraint':  function(c) { removing_list[c] = 1;},
	'addConstraint': function(c) {adding_list.push(c);},
	'constraint_list': constraint_list,
	'particle_list': particle_list,
	'addBall': addBall,
	'gravity': function(g) {
	    if(g == undefined) {
		return gravity;
	    } else {
		gravity = g;
		return g;
	    }
	},
	'width': function() {return width;},
	'height': function() { return height;},
	'setSize': setSize,
	'one_step': one_step,
    };
}();
