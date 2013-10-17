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
