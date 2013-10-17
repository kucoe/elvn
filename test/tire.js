var tire = require('../lib/tire');


var call_order = [];

describe.only('tire', function () {
    afterEach(function(){
        call_order = [];
    });
    it('should wire functions', function (done) {
        tire(function (r, next) {
            r(function () {
                call_order.push('fn1');
                setTimeout(function () {
                    next('one', 'two');
                }, 0);
            });
            r(function (arg1, arg2) {
                call_order.push('fn2');
                arg1.should.eql('one', 'param');
                arg2.should.eql('two', 'param');
                setTimeout(function () {
                    next(arg1, arg2, 'three');
                }, 25);
            });
            r(function (arg1, arg2, arg3) {
                call_order.push('fn3');
                arg1.should.eql('one', 'param');
                arg2.should.eql('two', 'param');
                arg3.should.eql('three', 'param');
                next('four');
            });
            r(function (arg1) {
                arg1.should.eql('four', 'param');
                call_order.push('fn4');
                call_order.should.eql(['fn1', 'fn2', 'fn3', 'fn4'], 'calls');
                next('test');
            });
            r(function () {
                done();
            });
        });
    });
    it('should run empty', function (done) {
        tire(function () {
            done();
        });
    });
    it('should allow multi function calls', function (done) {
        tire(function (r, next) {
            r(function (cb) {
                call_order.push(1);
                // call the callback twice. this should call function 2 twice
                next('one', 'two');
            });
            r(function (arg1, arg2) {
                call_order.push(2);
                next(arg1, arg2, 'three');
            });
            r(function () {
                call_order.push(3);
                next('four');
            });
            r(function () {
                call_order.push(4);
                r(function () {
                    call_order.push(4);
                    call_order.should.eql([1, 2, 3, 4, 4], 'calls');
                    done();
                });
            });
        });
    });
});