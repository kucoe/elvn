var fs = require('fs');

var c = require('../lib/config');
var s = require('../lib/sync');


var config, sync, syncNoKey, cli;


describe.only('sync', function () {
    beforeEach(function () {
        cli = {
            password: function (text, cb) {
                cb && cb('aaa');
            }
        };
        config = new c.Config();
        config.getBasePath = function () {
            return this.getUserDir() + "/.elvn-test/";
        };
        sync = new s.Sync('becevka@mail.ru', false, null, config.getBasePath(), config, cli);
        syncNoKey = new s.Sync('becevka@ya.ru', true, null, config.getBasePath(), config, cli);
    });
    afterEach(function () {
        config.finish();
        var basePath = config.getBasePath();
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
        sync.pull(function () {
            fs.existsSync(config.getBasePath() + "sync.key").should.eql(true, 'key exists');
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
            fs.existsSync(config.getBasePath() + "sync.key").should.eql(false, 'key not exists');
            sync.authorized.should.eql(false, 'not authorized');
            done();
        });
    });
    it('should support nokey', function (done) {
        syncNoKey.pull(function () {
            fs.existsSync(config.getBasePath() + "sync.key").should.eql(false, 'key not exists');
            syncNoKey.authorized.should.eql(true, 'authorized');
            done();
        });
    });
});
