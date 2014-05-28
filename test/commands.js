var cli = require('cline')();
var should = require('should');
var commands = require('../lib/commands');
var items = require('../lib/items');

commands.init(cli);

describe('commands', function () {
    it('should parse list switch', function () {
        var parse = cli.parse('/all');
        parse.should.be.instanceof(commands.SwitchList, 'list switch command');
        parse.plan.should.eql('all', 'list plan ');
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
        parse.plan.should.eql(['g'], 'plan');
        parse.text.should.eql('text', 'text');
    });
    it('should parse wrong create task', function () {
        var parse = cli.parse('text');
        should.not.exist(parse);
    });
    it('should parse create multi word task', function () {
        var parse = cli.parse('g:text to test');
        parse.should.be.instanceof(commands.EditTask, 'task command');
        parse.plan.should.eql(['g'], 'plan');
        parse.text.should.eql('text to test', 'text');
    });
    it('should parse create multi plan task', function () {
        var parse = cli.parse('g,h:text to test');
        parse.should.be.instanceof(commands.EditTask, 'task command');
        parse.plan.should.eql(['g','h'], 'plan');
        parse.text.should.eql('text to test', 'text');
    });
    it('should parse create simple task', function () {
        var parse = cli.parse(':text');
        parse.should.be.instanceof(commands.EditTask, 'task command');
        parse.plan.should.eql([], 'plan');
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
    it('should parse locate task with change plan command', function () {
        var parse = cli.parse('#2:idea,home');
        parse.should.be.instanceof(commands.LocateTask, 'task locate command');
        parse.text.should .eql('', 'text');
        parse.command.should.eql(':', 'command');
        parse.positions.should.eql([2], 'positions');
        parse.plan.should.eql(['idea', 'home'], 'command');
    });
    it('should parse locate task with append plan command', function () {
        var parse = cli.parse('#2+idea');
        parse.should.be.instanceof(commands.LocateTask, 'task locate command');
        parse.text.should .eql('', 'text');
        parse.command.should.eql('+', 'command');
        parse.positions.should.eql([2], 'positions');
        parse.plan.should.eql(['idea'], 'command');
    });
    it('should parse locate task with remove plan command', function () {
        var parse = cli.parse('#2-idea');
        parse.should.be.instanceof(commands.LocateTask, 'task locate command');
        parse.text.should .eql('', 'text');
        parse.command.should.eql('-', 'command');
        parse.positions.should.eql([2], 'positions');
        parse.plan.should.eql(['idea'], 'command');
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
    it('should parse locate range task with change plan command', function () {
        var parse = cli.parse('#2-4:idea');
        parse.should.be.instanceof(commands.LocateTask, 'task locate command');
        parse.text.should .eql('', 'text');
        parse.positions.should.eql([2,3,4], 'positions');
        parse.command.should.eql(':', 'command');
        parse.plan.should.eql(['idea'], 'command');
    });
    it('should parse locate range task with append plan command', function () {
        var parse = cli.parse('#2-4+idea,home');
        parse.should.be.instanceof(commands.LocateTask, 'task locate command');
        parse.text.should .eql('', 'text');
        parse.positions.should.eql([2,3,4], 'positions');
        parse.command.should.eql('+', 'command');
        parse.plan.should.eql(['idea', 'home'], 'command');
    });
    it('should parse locate range task with remove plan command', function () {
        var parse = cli.parse('#2-4-idea');
        parse.should.be.instanceof(commands.LocateTask, 'task locate command');
        parse.text.should .eql('', 'text');
        parse.positions.should.eql([2,3,4], 'positions');
        parse.command.should.eql('-', 'command');
        parse.plan.should.eql(['idea'], 'command');
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
    it('should parse locate group task with change plan command', function () {
        var parse = cli.parse('#2,4:idea');
        parse.should.be.instanceof(commands.LocateTask, 'task locate command');
        parse.text.should .eql('', 'text');
        parse.command.should.eql(':', 'command');
        parse.positions.should.eql([2,4], 'positions');
        parse.plan.should.eql(['idea'], 'command');
    });
    it('should parse locate group task with append plan command', function () {
        var parse = cli.parse('#2,4+idea');
        parse.should.be.instanceof(commands.LocateTask, 'task locate command');
        parse.text.should .eql('', 'text');
        parse.command.should.eql('+', 'command');
        parse.positions.should.eql([2,4], 'positions');
        parse.plan.should.eql(['idea'], 'command');
    });
    it('should parse locate group task with remove plan command', function () {
        var parse = cli.parse('#2,4-idea, home');
        parse.should.be.instanceof(commands.LocateTask, 'task locate command');
        parse.text.should .eql('', 'text');
        parse.command.should.eql('-', 'command');
        parse.positions.should.eql([2,4], 'positions');
        parse.plan.should.eql(['idea', 'home'], 'command');
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
    it('should have plan', function () {
        items.is('a', 'a').should.eql(true, 'single-single');
        items.is(['a'], 'a').should.eql(true, 'array-single');
        items.is(['a', 'b'], 'a').should.eql(true, 'array-single more');
        items.is('a', ['a']).should.eql(true, 'single-array');
        items.is('a', ['a', 'b']).should.eql(true, 'single-array more');
        items.is(items.all, ['a', 'b']).should.eql(true, 'array-array');
        items.is(items.all, ['ab', 'b']).should.eql(false, 'not array-array');
    });
});