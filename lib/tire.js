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

    var _slice = Array.prototype.slice;

    var tire = function (activity, thisArg) {
        var rr = [];
        if(!thisArg) {
            thisArg = module;
        }
        var r = function (fn) {
            rr.push(fn);
            return r;
        };
        var next = tire.next = function () {
            var fn = rr.shift();
            if (fn) {
                fn.apply(thisArg, _slice.call(arguments));
            }
        };
        activity(r, next);
        nextTick(next);
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



