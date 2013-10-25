var should = require('should');
var commands = require('../lib/commands');
var fs = require('fs');

var d = require('../lib/display');
var c = require('../lib/config');
var colors = require('../lib/colors');

var lastMessage,display, config;

var create = function () {
    config.checkInit();
    var editTask = new commands.EditTask('b', 'aaa');
    editTask.getId = function() {
        return Math.floor(Math.random() * (1000000 - 1 + 1)) + 3;
    };
    for (var i = 0; i < 50; i++) {
        editTask.run(display, config);
    }
};

describe('config', function () {
    beforeEach(function () {
        display = new d.Display();
        config = new c.Config();
        display.currentList = 'all';
        display.show = function (text) {
            lastMessage = text;
        };
        config.getBasePath = function () {
            return this.getUserDir() + "/.elvn-test/";
        };
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
    it('should read list by color', function () {
        var size = config.getList('b').tasks.length;
        size.should.eql(1, 'list size');
    });
    it('should read list by label', function () {
        var size = config.getList('Work').tasks.length;
        size.should.eql(1, 'list size');
    });
    it('should assign list', function () {
        should.not.exist(config.getList('p'));
        should.not.exist(config.getList('Test'));
        display.currentList = '&';
        new commands.EditTask("p", "Test").run(display, config);
        should.exist(config.getList('p'));
        should.exist(config.getList('Test'));
    });
    it('should remove list', function () {
        should.exist(config.getList('g'));
        should.exist(config.getList('Personal'));
        display.currentList = '&';
        new commands.EditTask("g", "").run(display, config);
        should.not.exist(config.getList('g'));
        should.not.exist(config.getList('Personal'));
    });
    it('should not remove default list', function () {
        display.currentList = '&';
        new commands.EditTask("b", "").run(display, config);
        should.exist(config.getList('b'));
        should.exist(config.getList('Work'));
    });
    it('should skip dumb list', function () {
        display.currentList = '&';
        new commands.EditTask("u", "AAA").run(display, config);
        should.not.exist(config.getList('u'));
        should.not.exist(config.getList('AAA'));
    });
    it('should create task', function () {
        var size = config.getList('all').tasks.length;
        var command = new commands.EditTask("b", "a");
        colors.all.should.include(command.run(display, config));
        config.getList('all').tasks.length.should.eql(size + 1, 'list size');
    });
    it('should create tasks', function () {
        config.checkInit();
        var editTask = new commands.EditTask('b', 'aaa');
        editTask.getId = function() {
            return null;
        };
        var s = new Date().getTime();
        for (var i = 0; i < 2000; i++) {
            editTask.run(display, config);
        }
        var e = new Date().getTime();
        console.log(e-s + 'ms');
        config.getList('b').tasks.length.should.eql(2001, 'list size');
    });
    it('should subtract from task', function () {
        var command = new commands.LocateTask("-task", [1]);
        colors.all.should.include(command.run(display, config));
        config.getList('all').tasks[0].text.should.eql('Test', 'text');
    });
    it('should append to task', function () {
        var command = new commands.LocateTask("+popopo", [1]);
        colors.all.should.include(command.run(display, config));
        config.getList('all').tasks[0].text.should.eql('Test taskpopopo', 'text');
    });
    it('should create idea', function () {
        var size = config.getIdeas().length;
        display.currentList = '@';
        var command = new commands.EditTask("", "aaaa");
        command.run(display, config).should.eql('@', 'list');
        config.getIdeas().length.should.eql(size + 1, 'list size');
        config.getIdeas()[1].text.should.eql('aaaa', 'text');
    });
    it('should create idea with colon', function () {
        var size = config.getIdeas().length;
        display.currentList = '@';
        var command = new commands.EditTask("aaaa", "bbbb");
        command.run(display, config).should.eql('@', 'list');
        config.getIdeas().length.should.eql(size + 1, 'list size');
        config.getIdeas()[1].text.should.eql('aaaa:bbbb', 'text');
    });
    it('should replace in idea', function () {
        display.currentList = '@';
        var command = new commands.LocateTask("idea%task", [1]);
        command.run(display, config).should.eql('@', 'list');
        config.getIdeas()[0].text.should.eql('Test task', 'text');
    });
    it('should append to idea', function () {
        display.currentList = '@';
        var command = new commands.LocateTask("+popopo", [1]);
        command.run(display, config).should.eql('@', 'list');
        config.getIdeas()[0].text.should.eql('Test ideapopopo', 'text');
    });
    it('should subtract from idea', function () {
        display.currentList = '@';
        var command = new commands.LocateTask("-idea", [1]);
        command.run(display, config).should.eql('@', 'list');
        config.getIdeas()[0].text.should.eql('Test', 'text');
    });
    it('should convert task to idea', function () {
        var size = config.getIdeas().length;
        var command = new commands.LocateTask('', [1], "@");
        command.run(display, config);
        command.next.run(display, config).should.eql('@', 'next list');
        config.getIdeas().length.should.eql(size + 1, 'ideas size');
    });
    it('should convert idea to task', function () {
        var size = config.getList('g').tasks.length;
        var isize = config.getIdeas().length;
        display.currentList = '@';
        var command = new commands.LocateTask('', [1], "g");
        command.run(display, config);
        colors.all.should.include(command.next.run(display, config));
        config.getIdeas().length.should.eql(isize - 1, 'ideas size');
        config.getList('g').tasks.length.should.eql(size + 1, 'list size');
    });
    it('should run group task', function () {
        var size = config.getList('t').tasks.length;
        var command = new commands.LocateTask('', [1, 2], ">");
        colors.all.should.include(command.run(display, config));
        config.getList('t').tasks.length.should.eql(size + 2, 'list size');
        config.getTimer().running.should.eql(true, 'timer');
    });
    it('should delete group task', function () {
        var size = config.getList('a').tasks.length;
        var command = new commands.LocateTask('', [1, 2], "x");
        colors.all.should.include(command.run(display, config));
        config.getList('a').tasks.length.should.eql(size - 2, 'list size');
        config.getTimer().running.should.eql(false, 'timer');
    });
    it('should complete big group', function () {
        create();
        var size = config.getList('a').tasks.length;
        var csize = config.getList('d').tasks.length;
        size.should.eql(52, 'initial size');
        var pos = [];
        for (var i = 0; i < 50; i++) {
            pos.push(i + 3);
        }
        var command = new commands.LocateTask('', pos, "v");
        colors.all.should.include(command.run(display, config));
        config.getList('a').tasks.length.should.eql(size - 50, 'list size');
        config.getList('d').tasks.length.should.eql(csize + 50, 'done list size');
    });
    it('should delete big group', function () {
        create();
        var size = config.getList('a').tasks.length;
        size.should.eql(52, 'initial size');
        var pos = [];
        for (var i = 0; i < 50; i++) {
            pos.push(i + 3);
        }
        var command = new commands.LocateTask('', pos, "x");
        colors.all.should.include(command.run(display, config));
        config.getList('a').tasks.length.should.eql(size - 50, 'list size');
    });
    it('should delete all', function () {
        create();
        var size = config.getList('a').tasks.length;
        size.should.eql(52, 'initial size');
        var pos = [];
        var command = new commands.LocateTask('', pos, "x");
        colors.all.should.include(command.run(display, config));
        config.getList('a').tasks.length.should.eql(0, 'list size');
    });
    it('should delete completed big group', function () {
        create();
        var size = config.getList('a').tasks.length;
        var csize = config.getList('d').tasks.length;
        size.should.eql(52, 'initial size');
        var pos = [];
        for (var i = 0; i < 50; i++) {
            pos.push(i + 3);
        }
        var command = new commands.LocateTask('', pos, "v");
        colors.all.should.include(command.run(display, config));
        config.getList('a').tasks.length.should.eql(size - 50, 'list size');
        config.getList('d').tasks.length.should.eql(csize + 50, 'done list size');

        display.currentList = 'd';
        pos = [];
        for (i = 0; i < 50; i++) {
            pos.push(i + 2);
        }
        command = new commands.LocateTask('', pos, "x");
        colors.done.should.include(command.run(display, config));
        config.getList('d').tasks.length.should.eql(1, 'done list size');
    });
    it('should delete idea group', function () {
        display.currentList = '@';
        var command = new commands.LocateTask('', [1], "x");
        command.run(display, config).should.eql('@', 'idea list');
        config.getIdeas().length.should.eql(0, 'ideas size');
    });
    it('should ignore wrong list', function () {
        display.currentList = 'g';
        var size = config.getList('g').tasks.length;
        var command = new commands.EditTask("greena", "text");
        command.run(display, config).should.eql('g', 'list type');
        config.getList('g').tasks.length.should.eql(size + 1, 'list size');
        config.getList('g').tasks[1].text.should.eql('greena:text', 'task text');
    });
    it('should ignore wrong list with empty text', function () {
        display.currentList = 'g';
        var size = config.getList('g').tasks.length;
        var command = new commands.EditTask("greena");
        command.run(display, config).should.eql('g', 'list type');
        config.getList('g').tasks.length.should.eql(size + 1, 'list size');
        config.getList('g').tasks[1].text.should.eql('greena:', 'task text');
    });
    it('should trim text', function () {
        display.currentList = '@';
        var size = config.getIdeas().length;
        var command = new commands.EditTask('', " aaaa ");
        command.run(display, config).should.eql('@', 'list type');
        config.getIdeas().length.should.eql(size + 1, 'list size');
        config.getIdeas()[1].text.should.eql('aaaa', 'task text');
    });
    it('should trim text on edit', function () {
        display.currentList = '@';
        var command = new commands.LocateTask(" aaaa ", [1]);
        command.run(display, config).should.eql('@', 'list type');
        config.getIdeas()[0].text.should.eql('aaaa', 'task text');
    });
    it.skip('should search task', function () {
        create();
        var size = config.getList('a').tasks.length;
        size.should.eql(52, 'initial size');
        var command = new commands.SearchTask('aaa');
        colors.all.should.include(command.run(display, config));
        config.getList('a').tasks.length.should.eql(size - 50, 'list size');
    });
    it('should run timer', function () {
        var command = new commands.LocateTask('', [1], ">");
        command.run(display, config);
        config.getList('t').tasks.length.should.eql(1, 'list size');
        config.getTimer().running.should.eql(true, 'timer running');
        config.getTimer().lastStage.should.eql('elvn', 'timer stage');
    });
    it('should cancel timer', function () {
        new commands.LocateTask('', [1], ">").run(display, config);
        config.getTimer().running.should.eql(true, 'timer running');
        new commands.SwitchTimer("x").run(display, config);
        config.getTimer().running.should.eql(false, 'timer not running');
        config.getList('d').tasks.length.should.eql(1, 'completed size');
    });
    it('should complete timer', function (done) {
        new commands.LocateTask('', [1], ">").run(display, config);
        config.getTimer().running.should.eql(true, 'timer running');
        new commands.SwitchTimer("v").run(display, config);
        setTimeout(function () {
            config.getTimer().running.should.eql(false, 'timer not running');
            config.getList('d').tasks.length.should.eql(2, 'completed size');
            done();
        }, 1000);
    });
    it('should skip timer', function (done) {
        new commands.LocateTask('', [1], ">").run(display, config);
        config.getTimer().running.should.eql(true, 'timer running');
        config.getTimer().lastStage.should.eql('elvn', 'timer stage');
        new commands.SwitchTimer("^").run(display, config);
        setTimeout(function () {
            config.getTimer().running.should.eql(true, 'timer running');
            config.getTimer().lastStage.should.eql('work', 'timer stage');
            done();
        }, 1000);
    });
    it('should show status', function(done){
        new commands.LocateTask('', [1], ">").run(display, config);
        new commands.SwitchTimer("v").run(display, config);
        setTimeout(function () {
            config.getStatus().indexOf('Done: 1').should.not.eql(-1, 'contains');
            done();
        }, 1000);
    });
    it('should show help', function(){
        new commands.SwitchTimer("s").run(display, config);
        lastMessage.indexOf('Available commands:').should.not.eql(-1, 'contains help');
        new commands.LocateTask('', [1,2], "s").run(display, config);
        lastMessage.indexOf('Available commands:').should.not.eql(-1, 'contains help');
        new commands.SwitchSync("s").run(display, config);
        lastMessage.should.eql('Sync is not initialized', 'no sync message');
        config.getSync = function() {
            return {};
        };
        new commands.SwitchSync("s").run(display, config);
        lastMessage.indexOf('Available commands:').should.not.eql(-1, 'contains help');
    });
});
