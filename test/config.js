var should = require('should');
var commands = require('../lib/commands');
var fs = require('fs');

var d = require('../lib/display');
var c = require('../lib/config');

var display = new d.Display();
var config = new c.Config();

display.currentList = 'all';

config.getBasePath = function () {
    return this.getUserDir() + "/.elvn-test/";
};

describe('config', function () {
    afterEach(function () {
        var basePath = config.getBasePath();
        var files = fs.readdirSync(basePath);
        files.forEach(function (file, index) {
            fs.unlinkSync(basePath + "/" + file);
        });
        fs.rmdirSync(basePath);
    });
    it('should create task', function () {
        var size = config.getList('all').tasks.length;
        var command = new commands.EditTask("b", "a");
        command.run(display, config).should.eql('all', 'current list');
        config.getList('a').tasks.length.should.eql(size + 1, 'list size');
    });
});
