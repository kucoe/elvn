var should = require('should');
var commands = require('../lib/commands');

var d = require('../lib/display');
var c = require('../lib/config');

var display = new d.Display();
var config = new c.Config();

display.currentList = 'all';

config.getBasePath = function() {
    return this.getUserDir() + "/.elvn-test/";
};

describe('config', function () {
    it('should create task', function () {
        var size = config.getList('a').tasks.length;
        var command = new commands.EditTask("b", "a");
        command.run(display, config).should.eql('all', 'current list');
        config.getList('a').tasks.length.should.eql(size + 1, 'list size');
    });
});
