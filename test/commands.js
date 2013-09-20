var cli = require('cline')();
var should = require('should');
var fs = require('fs');
var results = require('../lib/results');

require('../lib/commands').init(cli);

describe('commands', function () {
    it('should parse list switch', function () {
        var parse = cli.parse('/all');
        parse.should.be.instanceof(results.SwitchList, 'list switch result');
        parse.color.should.eql('all', 'list color ');
    });
    it('should parse list editor switch', function () {
        var parse = cli.parse('&');
        parse.should.be.instanceof(results.SwitchListEdit, 'list edit switch result');
    });
    it('should parse ideas switch', function () {
        var parse = cli.parse('@');
        parse.should.be.instanceof(results.SwitchIdeas, 'ideas switch result');
    });
    it('should parse status switch', function () {
        var parse = cli.parse('!');
        parse.should.be.instanceof(results.SwitchStatus, 'status switch result');
    });
    it('should parse sync switch', function () {
        var parse = cli.parse('%');
        parse.should.be.instanceof(results.SwitchSync, 'sync switch result');
        parse.command.should.eql('', 'sync command ');
    });
    it('should parse sync command', function () {
        var parse = cli.parse('%>');
        parse.should.be.instanceof(results.SwitchSync, 'sync switch result');
        parse.command.should.eql('>', 'sync command ');
    });
    it('should parse timer switch', function () {
        var parse = cli.parse('$');
        parse.should.be.instanceof(results.SwitchTimer, 'timer switch result');
        parse.command.should.eql('', 'timer command ');
    });
    it('should parse timer command', function () {
        var parse = cli.parse('$:');
        parse.should.be.instanceof(results.SwitchTimer, 'timer switch result');
        parse.command.should.eql(':', 'timer command ');
    });
    it('should parse wrong list editor switch', function () {
        var parse = cli.parse('&s');
        should.not.exist(parse);
    });
    it('should parse wrong idea switch', function () {
        var parse = cli.parse('@s');
        should.not.exist(parse);
    });
    it('should parse wrong status switch', function () {
        var parse = cli.parse('!s');
        should.not.exist(parse);
    });
    it('should parse wrong sync switch', function () {
        var parse = cli.parse('%s');
        should.not.exist(parse);
    });
    it('should parse wrong timer switch', function () {
        var parse = cli.parse('$s');
        should.not.exist(parse);
    });
});
