(function (module) {
    'use strict';

    var nextTick = function (fn) {
        if (typeof module.setImmediate === 'function') {
            module.setImmediate(fn);
        } else if (typeof process !== 'undefined' && process.nextTick) {
            process.nextTick(fn);
        } else {
            module.setTimeout(fn, 0);
        }
    };

    var _forEach = Array.prototype.forEach;

    var tire = function (activity, thisArg, errorHandler) {
        var rr = [];
        if (typeof thisArg == 'function') {
            errorHandler = thisArg;
            thisArg = null;
        }
        if (!thisArg) {
            thisArg = module;
        }
        var r = function (fn) {
            rr.push(fn);
            return r;
        };

        function next() {
            var fn = rr.shift();
            if (fn) {
                var n = next();
                return function () {
                    var arr = [];
                    var err = null;
                    _forEach.call(arguments, function (item) {
                        if (item instanceof Error && errorHandler) {
                            err = item;
                        }
                        arr.push(item);
                        return !err;
                    });
                    if (err) {
                        errorHandler.call(thisArg, err);
                    } else {
                        arr.push(n);
                        nextTick(function () {
                            fn.apply(thisArg, arr);
                        });
                    }
                }
            }
            return function () {
                if (rr.length > 0) {
                    nextTick(function () {
                        next().apply(thisArg, arguments);
                    });
                }
            }
        }

        r(activity);
        nextTick(function () {
            next()();
        });
        return r;
    };


    if (typeof module.define !== 'undefined' && module.define.amd) {
        module.define([], function () {
            return tire;
        }); // RequireJS
    } else if (typeof module.exports === 'object') {
        module.exports = tire; // CommonJS
    } else {
        module.tire = tire; // <script>
    }

})(typeof exports === 'object' ? module : window);



