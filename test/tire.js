var tire = require('../lib/tire');
var fs = require('fs');

var self = {a: 'Hello!'};
var path = 'a';
var call_order = [];

describe('tire', function () {
    afterEach(function () {
        call_order = [];
    });
    it('should wire functions', function (done) {
        tire(function (next) {
            call_order.push('fn1');
            setTimeout(function () {
                next('one', 'two');
            }, 0);
        })(function (arg1, arg2, next) {
            call_order.push('fn2');
            arg1.should.eql('one', 'param');
            arg2.should.eql('two', 'param');
            setTimeout(function () {
                next(arg1, arg2, 'three');
            }, 25);
        })(function (arg1, arg2, arg3, next) {
            call_order.push('fn3');
            arg1.should.eql('one', 'param');
            arg2.should.eql('two', 'param');
            arg3.should.eql('three', 'param');
            next('four');
        })(function (arg1, next) {
            arg1.should.eql('four', 'param');
            call_order.push('fn4');
            call_order.should.eql(['fn1', 'fn2', 'fn3', 'fn4'], 'calls');
            next('test');
        })(function () {
            done();
        });
    });
    it('should react on errors', function (done) {
        tire(function (next) {
            call_order.push('fn1');
            next('one', 'two');
        }, function (err) {
            err.message.should.eql('lala', 'error');
            call_order.should.eql(['fn1', 'fn2']);
            done();
        })(function (arg1, arg2, next) {
            call_order.push('fn2');
            next(new Error('lala'));
        })(function () {
            call_order.push('fn3');
        });
    });
    it('should not stop on error if no handler', function (done) {
        tire(function (next) {
            call_order.push('fn1');
            next('one', 'two');
        })(function (arg1, arg2, next) {
            call_order.push('fn2');
            next(new Error('lala'));
        })(function (err) {
            call_order.push('fn3');
            err.message.should.eql('lala', 'error');
            call_order.should.eql(['fn1', 'fn2', 'fn3']);
            done();
        });
    });
    it('should run empty', function (done) {
        tire(function () {
            done();
        });
    });
    it('should allow multi function calls', function (done) {
        tire(function (next) {
            call_order.push(1);
            next();
            next();
        })(function (next) {
            call_order.push(2);
            next();
        })(function () {
            call_order.push(4);
        });
        setTimeout(function () {
            call_order.should.eql([1, 2, 2, 4, 4], 'calls');
            done();
        }, 200);
    });
    it('should allow attaching functions', function (done) {
        var t = tire(function (next) {
            call_order.push(1);
            next();
            next();
        });
        t(function (next) {
            call_order.push(2);
            next();
        })(function (next) {
            call_order.push(4);
            t(function () {
                call_order.push(5);
                call_order.should.eql([1, 2, 2, 4, 4, 5], 'calls');
                done();
            });
            next();
        });
    });
    it('should work with fs', function (done) {
        tire(function (next) {
            fs.exists(path, next);
        }, self)
        (function (exists, next) {
            exists.should.eql(false, 'file');
            var data = "uuu";
            fs.writeFile(path, data, 'utf-8', next);
        })
        (function (err, next) {
            fs.readFile(path, 'utf-8', next);
        })
        (function (err, data, next) {
            data.should.eql('uuu', 'data');
            this.a.should.eql('Hello!', 'context');
            fs.unlink(path, next);
        })
        (function () {
            done();
        });
    });
});