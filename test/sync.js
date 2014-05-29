var fs = require('fs');

var i = require('../lib/items');
var s = require('../lib/sync');


var items, sync, syncNoKey, cli;


describe('sync', function () {
    beforeEach(function () {
        cli = {
            password: function (text, cb) {
                cb && cb('aaa');
            },
            stream: {
              emit:function(){}
            }
        };
        items = new i.Items();
        items.getBasePath = function () {
            return this.getUserDir() + "/.11-test/";
        };
        sync = new s.Sync('becevka@mail.ru', false, null, items.getBasePath(), items, cli);
        syncNoKey = new s.Sync('becevka@ya.ru', true, null, items.getBasePath(), items, cli);
    });
    afterEach(function () {
        items.finish();
        var basePath = items.getBasePath();
        var files = fs.readdirSync(basePath);
        files.forEach(function (file) {
            fs.unlinkSync(basePath + "/" + file);
        });
        fs.rmdirSync(basePath);
    });
    it('should encrypt and decrypt', function(){
        var hash = sync.hash('lalala');
        var time = new Date().getTime();
        var crypt = sync.encrypt(hash, time);
        var dec = sync.decrypt(crypt, time);
        dec.should.eql(hash, 'decrypted');
    });
    it('should auth', function (done) {
        this.timeout(5000);
        sync.pull(function () {
            fs.existsSync(items.getBasePath() + "sync.key").should.eql(true, 'key exists');
            sync.authorized.should.eql(true, 'authorized');
            done();
        });
    });
    it('should auth fail on wrong pass', function (done) {
        cli.password = function (text, cb) {
            cb && cb('bbb');
        };
        this.timeout(5000);
        sync.pull(function () {
            fs.existsSync(items.getBasePath() + "sync.key").should.eql(false, 'key not exists');
            sync.authorized.should.eql(false, 'not authorized');
            done();
        });
    });
    it('should support nokey', function (done) {
        this.timeout(5000);
        syncNoKey.pull(function () {
            fs.existsSync(items.getBasePath() + "sync.key").should.eql(false, 'key not exists');
            syncNoKey.authorized.should.eql(true, 'authorized');
            done();
        });
    });
});
