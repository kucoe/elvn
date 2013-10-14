var should = require('should');
var fs = require('fs');

var c = require('../lib/config');
var s = require('../lib/sync');


var config, sync, syncNoKey;
var cli = {
    password: function () {
        return 'aaa';
    }
};

describe.only('sync', function () {
    beforeEach(function () {
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
    it('should auth', function () {
        sync.pull();
        fs.existsSync(config.getBasePath() + "sync.key").should.eql(true, 'key exists');
        sync.authorized.should.eql(true, 'authorized');
    });
});
