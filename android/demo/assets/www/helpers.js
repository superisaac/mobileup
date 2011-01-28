function trig(v1, v2, v3) {
    this.center = v1.add(v2, v3).mul(1/3);
    this.v1 = v1.minus(this.center);
    this.v2 = v2.minus(this.center);
    this.v3 = v3.minus(this.center);
}

trig.prototype.moveToXY = function(x, y) {
    this.center = new Vector(x, y);
}

trig.prototype.moveTo = function(other) {
    this.center = other;
}


trig.prototype.rotate = function(angle) {
    return new trig(this.v1.rotate(angle).add(this.center),
		    this.v2.rotate(angle).add(this.center),
		    this.v3.rotate(angle).add(this.center));
}

trig.prototype.rotateRadian = function(r) {
    return new trig(this.v1.rotateRadian(r).add(this.center),
		    this.v2.rotateRadian(r).add(this.center),
		    this.v3.rotateRadian(r).add(this.center));
}

trig.prototype.draw = function(context) {
    var vv1 = this.center.add(this.v1);
    var vv2 = this.center.add(this.v2);
    var vv3 = this.center.add(this.v3);
    context.beginPath();
    context.moveTo(vv1.x, vv1.y);
    context.lineTo(vv2.x, vv2.y);
    context.lineTo(vv3.x, vv3.y);
    context.lineTo(vv1.x, vv1.y);
    context.stroke();
    context.closePath();
}
