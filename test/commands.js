var cli = require('cline')();
var should = require('should');
var commands = require('../lib/commands');

commands.init(cli);

describe('commands', function () {
    it('should parse list switch', function () {
        var parse = cli.parse('/all');
        parse.should.be.instanceof(commands.SwitchList, 'list switch command');
        parse.color.should.eql('all', 'list color ');
    });
    it('should parse list editor switch', function () {
        var parse = cli.parse('&');
        parse.should.be.instanceof(commands.SwitchListEdit, 'list edit switch command');
    });
    it('should parse ideas switch', function () {
        var parse = cli.parse('@');
        parse.should.be.instanceof(commands.SwitchIdeas, 'ideas switch command');
    });
    it('should parse status switch', function () {
        var parse = cli.parse('!');
        parse.should.be.instanceof(commands.SwitchStatus, 'status switch command');
    });
    it('should parse sync switch', function () {
        var parse = cli.parse('%');
        parse.should.be.instanceof(commands.SwitchSync, 'sync switch command');
        parse.command.should.eql('', 'sync command ');
    });
    it('should parse sync command', function () {
        var parse = cli.parse('%>');
        parse.should.be.instanceof(commands.SwitchSync, 'sync switch command');
        parse.command.should.eql('>', 'sync command ');
    });
    it('should parse timer switch', function () {
        var parse = cli.parse('$');
        parse.should.be.instanceof(commands.SwitchTimer, 'timer switch command');
        parse.command.should.eql('', 'timer command ');
    });
    it('should parse timer command', function () {
        var parse = cli.parse('$:');
        parse.should.be.instanceof(commands.SwitchTimer, 'timer switch command');
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
    it('should parse create task', function () {
        var parse = cli.parse('g:text');
        parse.should.be.instanceof(commands.EditTask, 'task command');
        parse.color.should.eql('g', 'color');
        parse.text.should.eql('text', 'text');
    });
    it('should parse create multi word task', function () {
        var parse = cli.parse('g:text to test');
        parse.should.be.instanceof(commands.EditTask, 'task command');
        parse.color.should.eql('g', 'color');
        parse.text.should.eql('text to test', 'text');
    });
    it('should parse create simple task', function () {
        var parse = cli.parse(':text');
        parse.should.be.instanceof(commands.EditTask, 'task command');
        parse.color.should.eql('', 'color');
        parse.text.should.eql('text', 'text');
    });
    it('should parse search task', function () {
        var parse = cli.parse('?alla');
        parse.should.be.instanceof(commands.SearchTask, 'task search command');
        parse.query.should.eql('alla', 'query');
    });
    it('should parse locate task', function () {
        var parse = cli.parse('#2');
        parse.should.be.instanceof(commands.LocateTask, 'task locate command');
        parse.text.should .eql('', 'text');
        parse.command.should.eql('', 'command');
        parse.positions.should.eql([2], 'positions');
    });
    it('should parse locate task with command', function () {
        var parse = cli.parse('#2-');
        parse.should.be.instanceof(commands.LocateTask, 'task locate command');
        parse.text.should .eql('', 'text');
        parse.command.should.eql('-', 'command');
        parse.positions.should.eql([2], 'positions');
    });
    it('should parse locate range task', function () {
        var parse = cli.parse('#2-4');
        parse.should.be.instanceof(commands.LocateTask, 'task locate command');
        parse.text.should .eql('', 'text');
        parse.command.should.eql('', 'command');
        parse.positions.should.eql([2,3,4], 'positions');
    });
    it('should parse locate reverse range task', function () {
        var parse = cli.parse('#4-2');
        parse.should.be.instanceof(commands.LocateTask, 'task locate command');
        parse.text.should .eql('', 'text');
        parse.command.should.eql('', 'command');
        parse.positions.should.eql([2,3,4], 'positions');
    });
    it('should parse locate big range task', function () {
        var parse = cli.parse('#2-225');
        parse.should.be.instanceof(commands.LocateTask, 'task locate command');
        parse.text.should .eql('', 'text');
        parse.command.should.eql('', 'command');
        var arr = [];
        for(var i = 2; i <= 225; i++) {
            arr.push(i);
        }
        parse.positions.should.eql(arr, 'positions');
    });
    it('should parse locate range task with command', function () {
        var parse = cli.parse('#2-4>');
        parse.should.be.instanceof(commands.LocateTask, 'task locate command');
        parse.text.should .eql('', 'text');
        parse.positions.should.eql([2,3,4], 'positions');
        parse.command.should.eql('>', 'command');
    });
    it('should parse locate group task', function () {
        var parse = cli.parse('#2,4');
        parse.should.be.instanceof(commands.LocateTask, 'task locate command');
        parse.text.should .eql('', 'text');
        parse.command.should.eql('', 'command');
        parse.positions.should.eql([2,4], 'positions');
    });
    it('should parse locate group task with command', function () {
        var parse = cli.parse('#2,4-');
        parse.should.be.instanceof(commands.LocateTask, 'task locate command');
        parse.text.should .eql('', 'text');
        parse.command.should.eql('-', 'command');
        parse.positions.should.eql([2,4], 'positions');
    });
    it('should parse locate range task with replace', function () {
        var parse = cli.parse('#3-5=correct%fixed');
        parse.should.be.instanceof(commands.LocateTask, 'task locate command');
        parse.text.should.eql('correct%fixed', 'text');
        parse.positions.should.eql([3,4,5], 'positions');
    });
    it('should parse locate task with replace', function () {
        var parse = cli.parse('#1=correct%fixed');
        parse.should.be.instanceof(commands.LocateTask, 'task locate command');
        parse.text.should.eql('correct%fixed', 'text');
        parse.positions.should.eql([1], 'positions');
    });
    it('should parse locate range task with edit', function () {
        var parse = cli.parse('#2-3=lalala');
        parse.should.be.instanceof(commands.LocateTask, 'task locate command');
        parse.text.should.eql('lalala', 'text');
        parse.positions.should.eql([2,3], 'positions');
    });
    it('should parse locate group task with edit', function () {
        var parse = cli.parse('#1,3=lalala');
        parse.should.be.instanceof(commands.LocateTask, 'task locate command');
        parse.text.should.eql('lalala', 'text');
        parse.positions.should.eql([1,3], 'positions');
    });
    it('should parse locate all tasks ', function () {
        var parse = cli.parse('#*x');
        parse.should.be.instanceof(commands.LocateTask, 'task locate command');
        parse.text.should .eql('', 'text');
        parse.command.should.eql('x', 'command');
        parse.positions.should.eql([], 'positions');
    });
    it('should parse wrong locate', function () {
        var parse = cli.parse('#s');
        should.not.exist(parse);
    });
});