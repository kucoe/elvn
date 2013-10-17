var fs = require('fs');
var tire = require('../lib/tire');

var self = {a: 'Hello!'};

tire(function (r, next) {
    r(function () {
        fs.exists('a', next);
    });
    r(function (exists) {
        if (exists) {
            fs.readFile('a', 'utf-8', next);
        } else {
            var data = "uuu";
            fs.writeFile('a', data, 'utf-8');
            next(null, data);
        }
    });
    r(function (err, data) {
        console.log(data);
        console.log(this.a);
    });
}, self);

tire(function () {
    fs.exists('a', tire.next);
}, self)(function (exists) {
    if (exists) {
        fs.readFile('a', 'utf-8', tire.next);
    } else {
        var data = "uuu";
        fs.writeFile('a', data, 'utf-8');
        tire.next(null, data);
    }
})(function (err, data) {
    console.log(data);
    console.log(this.a);
});

exports['waterfall'] = function (test) {
    test.expect(6);
    var call_order = [];
    async.waterfall([
        function (callback) {
            call_order.push('fn1');
            setTimeout(function () {
                callback(null, 'one', 'two');
            }, 0);
        },
        function (arg1, arg2, callback) {
            call_order.push('fn2');
            test.equals(arg1, 'one');
            test.equals(arg2, 'two');
            setTimeout(function () {
                callback(null, arg1, arg2, 'three');
            }, 25);
        },
        function (arg1, arg2, arg3, callback) {
            call_order.push('fn3');
            test.equals(arg1, 'one');
            test.equals(arg2, 'two');
            test.equals(arg3, 'three');
            callback(null, 'four');
        },
        function (arg4, callback) {
            call_order.push('fn4');
            test.same(call_order, ['fn1', 'fn2', 'fn3', 'fn4']);
            callback(null, 'test');
        }
    ], function (err) {
        test.done();
    });
};

exports['waterfall empty array'] = function (test) {
    async.waterfall([], function (err) {
        test.done();
    });
};

exports['waterfall non-array'] = function (test) {
    async.waterfall({}, function (err) {
        test.equals(err.message, 'First argument to waterfall must be an array of functions');
        test.done();
    });
};

exports['waterfall no callback'] = function (test) {
    async.waterfall([
        function (callback) {
            callback();
        },
        function (callback) {
            callback();
            test.done();
        }
    ]);
};

exports['waterfall async'] = function (test) {
    var call_order = [];
    async.waterfall([
        function (callback) {
            call_order.push(1);
            callback();
            call_order.push(2);
        },
        function (callback) {
            call_order.push(3);
            callback();
        },
        function () {
            test.same(call_order, [1, 2, 3]);
            test.done();
        }
    ]);
};

exports['waterfall error'] = function (test) {
    test.expect(1);
    async.waterfall([
        function (callback) {
            callback('error');
        },
        function (callback) {
            test.ok(false, 'next function should not be called');
            callback();
        }
    ], function (err) {
        test.equals(err, 'error');
    });
    setTimeout(test.done, 50);
};

exports['waterfall multiple callback calls'] = function (test) {
    var call_order = [];
    var arr = [
        function (callback) {
            call_order.push(1);
            // call the callback twice. this should call function 2 twice
            callback(null, 'one', 'two');
            callback(null, 'one', 'two');
        },
        function (arg1, arg2, callback) {
            call_order.push(2);
            callback(null, arg1, arg2, 'three');
        },
        function (arg1, arg2, arg3, callback) {
            call_order.push(3);
            callback(null, 'four');
        },
        function (arg4) {
            call_order.push(4);
            arr[3] = function () {
                call_order.push(4);
                test.same(call_order, [1, 2, 2, 3, 3, 4, 4]);
                test.done();
            };
        }
    ];
    async.waterfall(arr);
};
