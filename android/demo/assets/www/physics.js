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

Vector.prototype.minusEqual = function(other) {
    this.x -= other.x;
    this.y -= other.y;
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


function CollisionDetector(particle1, particle2) {
    this.p1 = particle1;
    this.p2 = particle2;
    this.func = this.funcMap[this.p1.name + '+' + this.p2.name];
}

CollisionDetector.prototype.funcMap = {};
CollisionDetector.prototype.exec = function(ground) {
    this.func.apply(this, [ground, this.p1, this.p2]);
}

CollisionDetector.prototype.addFunc = function(cls1, cls2, callback) {
    var name1 = cls1.prototype.name + '+' + cls2.prototype.name;
    this.funcMap[name1] = callback;
    var name2 = cls2.prototype.name + '+' + cls1.prototype.name;
    if(name1 != name2) {
	this.funcMap[name2] = function(g, p1, p2) { return callback.apply(this, [g, p2, p1]); };
    }
}


var normalVectors = [new Vector(0, 1), 
    new Vector(-1, 0),
    new Vector(0, -1),
    new Vector(1, 0)];

// Bound Checker: check if the particle/ball is collide with other
function BoundChecker(particle) {
    this.objectCounter = objectCounter++;
    this.particle = particle;
    this.func = this.funcMap[particle.name];
}
BoundChecker.prototype.toString = function() {
    return "BoundChecker " + this.objectCounter;
}
BoundChecker.prototype.funcMap = {};
BoundChecker.prototype.exec = function(ground) {
    this.func.apply(this, [ground, this.particle]);
}

BoundChecker.prototype.addFunc = function(cls, callback) {
    this.funcMap[cls.prototype.name] = callback;
}


function BoundSlider(particle, which) {
    this.objectCounter = objectCounter++;
    this.particle = particle;
    this.which = which;
    this.func = this.funcMap[this.particle.name];
}

BoundSlider.prototype.toString = function() {
    return "BoundSlider " + this.objectCounter;
}

BoundSlider.prototype.funcMap = {};
BoundSlider.prototype.exec = function(ground) {
    this.func.apply(this, [ground, this.particle, this.which]);
}

BoundSlider.prototype.addFunc = function(cls, callback) {
    this.funcMap[cls.prototype.name] = callback;
}

function Ground() {
    this.constraint_list = [];
    this.particle_list = [];
    this.gravity = new Vector(0, 6);
    
    this.removing_list = {};
    this.adding_list = [];
    this.width = 600;
    this.height = 600;
}

Ground.prototype.setSize = function(w, h) {
    this.width = w;
    this.height = h;
};

Ground.prototype.removeConstraints = function() {
    var newc = [];
    for(var i =0; i< this.constraint_list.length; i++) {
	var c = this.constraint_list[i];
	if(c in this.removing_list) {
	    continue;
	} else {
	    newc.push(c);
	}
    }
    this.constraint_list = newc;
};


Ground.prototype.removeConstraint = function(c) {
    this.removing_list[c.toString()] = c;
};

Ground.prototype.addConstraint = function(c) {
    this.adding_list.push(c);
}

Ground.prototype.addParticle = function(p) {
    for(var i=0; i<this.particle_list.length; i++) {
	var pp = this.particle_list[i];
	this.constraint_list.push(
	    new CollisionDetector(pp, p));
    }
    this.particle_list.push(p);
    this.constraint_list.push(new BoundChecker(p));
};

Ground.prototype.oneStep = function(context) {
    for(var i=0; i<this.particle_list.length; i++) {
	var ball = this.particle_list[i];
	ball.pos.addEqual(ball.velocity);
    }

    this.removing_list = {};
    this.adding_list = [];
    for(var i = 0; i<this.constraint_list.length; i++) {
	var cons = this.constraint_list[i];
	cons.exec(this);
    }
    
    this.removeConstraints();
    
    for(var i = 0; i< this.adding_list.length;i++) {
	this.constraint_list.push(this.adding_list[i]);
    }
    
    for(var i=0; i<this.particle_list.length; i++) {
	var ball = this.particle_list[i];
	ball.draw(context);
	//var acc = gravity.minus(ball.velocity.normalize(2));
	//ball.velocity.addEqual(gravity);
	ball.velocity.addEqual(this.gravity);
    }
}

// A Ball particle and related functions
function Ball(pos, r) {
    this.pos = pos;
    this.velocity = new Vector(0, 0);
    this.radius = r;
}

Ball.prototype.name = "Ball";
Ball.prototype.draw = function(context) {
    context.beginPath();
    context.arc(this.pos.x, this.pos.y, this.radius, 0, 2 * Math.PI, false);
    context.stroke();
    context.closePath();
}

CollisionDetector.prototype.addFunc(Ball, Ball, function(ground, ball1, ball2) {
    var v = ball1.pos.minus(ball2.pos);
    var r = v.magnitude();
    var dr = ball1.radius + ball2.radius - r;
    if(dr >= 0) {
	var vr = ball1.velocity.magnitude() + ball2.velocity.magnitude();
	var t = v.normalize(dr * 0.5);
	ball1.pos.addEqual(t);
	ball2.pos.minusEqual(t);
	ball1.velocity = v.normalize(vr * ball2.radius/(ball1.radius + ball2.radius) );
	ball2.velocity = ball1.velocity.mul(ball1.radius/ball2.radius).negative();
    }
});

BoundChecker.prototype.addFunc(Ball, function(ground, ball) {
    var newpos = ball.pos;
    var newv = ball.velocity;
    var width = ground.width;
    var height = ground.height;
    if(newpos.x <= ball.radius) {
	if(normalVectors[3].dot(newv) < 0) {
	    var dx = newpos.x - ball.radius;
	    var dy = newv.y * dx / newv.x;
	    ball.pos.x = newpos.x - dx;
	    ball.pos.y = newpos.y - dy;
	    var vx = -DAMPING * newv.x;
	    if(Math.abs(vx) < 5) {
		ground.removeConstraint(this);
		ground.addConstraint(new BoundSlider(ball, 3));
		vx = 0;
	    }
	    ball.velocity = new Vector(vx, newv.y);
	}
    } else if(newpos.x >= width - ball.radius) {
	if(normalVectors[1].dot(newv) < 0) {
	    var dx = newpos.x - width + ball.radius;
	    var dy = newv.y * dx / newv.x;
	    ball.pos.x = newpos.x - dx;
	    ball.pos.y = newpos.y - dy;
	    var vx = -DAMPING * newv.x;
	    if(Math.abs(vx) < 5) {
		ground.removeConstraint(this);
		ground.addConstraint(new BoundSlider(ball, 1));
		vx = 0;
	    }
	    ball.velocity = new Vector(vx, newv.y);
	}
    } else if(newpos.y <= ball.radius) {
	if(normalVectors[0].dot(newv) < 0) {
	    var dy = newpos.y - ball.radius;
	    var dx = newv.x * dy / newv.y;
	    ball.pos.x = newpos.x - dx;
	    ball.pos.y = newpos.y - dy;
	    var vy = -DAMPING * newv.y;
	    if(Math.abs(vy) < 5) {
		ground.removeConstraint(this);
		ground.addConstraint(new BoundSlider(ball, 0));
		vy = 0;
	    }
	    ball.velocity = new Vector(newv.x, vy);
	}
    } else if(newpos.y >= height - ball.radius) {
	if(normalVectors[2].dot(newv) < 0) {
	    var dy = newpos.y - height + ball.radius;
	    var dx = newv.x * dy / newv.y;
	    ball.pos.x = newpos.x - dx;
	    ball.pos.y = newpos.y - dy;
	    var vy = -DAMPING * newv.y;
	    if(Math.abs(vy) < 5) {
		ground.removeConstraint(this);
		ground.addConstraint(new BoundSlider(ball, 2));
		vy = 0;
	    }
	    ball.velocity = new Vector(newv.x, vy);
	}
    } else {
	if(ball.velocity.magnitude() > 0.8) {
	    var acc = ball.velocity.normalize(0.8);
	    ball.velocity.minusEqual(acc);
	}
    }
});

BoundSlider.prototype.addFunc(Ball, function(ground, ball, which) {
    var newpos = ball.pos;
    var width = ground.width;
    var height = ground.height;

    if(normalVectors[which].dot(ball.velocity) > 0) {
	ground.removeConstraint(this);
	ground.addConstraint(new BoundChecker(ball));
	return;
    }

    switch(which) {
    case 0:  // top
	{
	    if(newpos.x <= ball.radius) {
		ball.pos.x = ball.radius;
		ball.velocity.x = -ball.velocity.x;
	    } else if(newpos.x >= width - ball.radius) {
		ball.pos.x = width - ball.radius;
		ball.velocity.x = -ball.velocity.x;
	    }
	    ball.velocity.y = 0;
	    ball.velocity.x = 0.93 * ball.velocity.x;
	    ball.pos.y = ball.radius;
	}
	break;
    case 1:  // right
	{
	    if(newpos.y <= ball.radius) {
		ball.pos.y = ball.radius;
		ball.velocity.y = -ball.velocity.y;
	    } else if(newpos.y >= height - ball.radius) {
		ball.pos.y = height - ball.radius;
		ball.velocity.y = -ball.velocity.y;
	    }
	    ball.velocity.x = 0;
	    ball.velocity.y = 0.93 * ball.velocity.y;
	    ball.pos.x = width - ball.radius;
	}
	break;
    case 2: // bottom
	{
	    if(newpos.x <= ball.radius) {
		ball.pos.x = ball.radius;
		ball.velocity.x = -ball.velocity.x;
	    } else if(newpos.x >= width - ball.radius) {
		ball.pos.x = width - ball.radius;
		ball.velocity.x = -ball.velocity.x;
	    }
	    ball.velocity.y = 0;
	    ball.velocity.x = 0.93 * ball.velocity.x;
	    ball.pos.y = height - ball.radius;
	}
	break;
    case 3: // left
	{
	    if(newpos.y <= ball.radius) {
		ball.pos.y = ball.radius;
		ball.velocity.y = -ball.velocity.y;
	    } else if(newpos.y >= height - ball.radius) {
		ball.pos.y = height - ball.radius;
		ball.velocity.y = -ball.velocity.y;
	    }
	    ball.velocity.x = 0;
	    ball.velocity.y = 0.93 * ball.velocity.y;
	    ball.pos.x = ball.radius;
	}
	break;
    }
});


