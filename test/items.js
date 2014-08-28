var commands = require('../lib/commands');
var fs = require('fs');

var d = require('../lib/display');
var i = require('../lib/items');

var lastMessage, display, items;

var create = function () {
    items.checkInit();
    var editTask = new commands.EditTask('test', 'aaa');
    var orig = items.getId;
    items.getId = function() {
        return Math.floor(Math.random() * (1000000 - 1 + 1)) + 3;
    };
    for (var i = 0; i < 50; i++) {
        editTask.run(display, items);
    }
    items.getId = orig;
};

describe('items', function () {
    beforeEach(function () {
        display = new d.Display();
        items = new i.Items();
        display.currentList = 'all';
        display.show = function (text) {
            lastMessage = text;
        };
        items.getBasePath = function () {
            return this.getUserDir() + "/.11-test/";
        };
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
    it('should read list by plan', function () {
        var size = items.getByPlan('work').items.length;
        size.should.eql(1, 'list size');
    });
    it('should create list', function () {
        var size = items.getByPlan('p').items.length;
        size.should.eql(0, 'list size');
        new commands.EditTask(["p"], "Test").run(display, items);
        size = items.getByPlan('p').items.length;
        size.should.eql(1, 'list size');
    });
    it('should remove list', function () {
        var size = items.getByPlan('home').items.length;
        size.should.eql(1, 'list size');
        display.currentList = 'home';
        new commands.LocateTask('', [1], 'x').run(display, items);
        size = items.getByPlan('home').items.length;
        size.should.eql(0, 'list size');
    });
    it('should create task', function () {
        var size = items.getByPlan('all').items.length;
        var command = new commands.EditTask("work", "a");
        i.all.should.include(command.run(display, items));
        items.getByPlan('all').items.length.should.eql(size + 1, 'list size');
    });
    it('should create default plan task', function () {
        var size = items.getByPlan('work').items.length;
        var command = new commands.EditTask([], "a");
        i.all.should.include(command.run(display, items));
        items.getByPlan('work').items.length.should.eql(size + 1, 'list size');
        items.getByPlan('work').items[size].text.should.eql('a', 'text');
    });
    it('should create tasks', function () {
        items.checkInit();
        var editTask = new commands.EditTask('work', 'aaa');
        var orig = items.getId;
        items.getId = function() {
            return null;
        };
        var s = new Date().getTime();
        for (var i = 0; i < 2000; i++) {
            editTask.run(display, items);
        }
        items.getId = orig;
        var e = new Date().getTime();
        console.log(e-s + 'ms');
        items.getByPlan('work').items.length.should.eql(2001, 'list size');
    });
    it('should subtract from task', function () {
        var command = new commands.LocateTask("-task", [1]);
        i.all.should.include(command.run(display, items));
        items.getByPlan('all').items[0].text.should.eql('Test', 'text');
    });
    it('should append to task', function () {
        var command = new commands.LocateTask("+popopo", [1]);
        i.all.should.include(command.run(display, items));
        items.getByPlan('all').items[0].text.should.eql('Test taskpopopo', 'text');
    });
    it('should create idea', function () {
        var size = items.getByPlan('$').items.length;
        display.currentList = '$';
        var command = new commands.EditTask("", "aaaa");
        command.run(display, items).should.eql('$', 'list');
        items.getByPlan('$').items.length.should.eql(size + 1, 'list size');
        items.getByPlan('$').items[1].text.should.eql('aaaa', 'text');
    });
    it('should create idea with colon', function () {
        var size = items.getByPlan('$').items.length;
        display.currentList = '$';
        var command = new commands.EditTask("", "aaaa:bbbb");
        command.run(display, items).should.eql('$', 'list');
        items.getByPlan('$').items.length.should.eql(size + 1, 'list size');
        items.getByPlan('$').items[1].text.should.eql('aaaa:bbbb', 'text');
    });
    it('should replace in idea', function () {
        display.currentList = '$';
        var command = new commands.LocateTask("idea%task", [1]);
        command.run(display, items).should.eql('$', 'list');
        items.getByPlan('$').items[0].text.should.eql('Test task', 'text');
    });
    it('should append to idea', function () {
        display.currentList = '$';
        var command = new commands.LocateTask("+popopo", [1]);
        command.run(display, items).should.eql('$', 'list');
        items.getByPlan('$').items[0].text.should.eql('Test ideapopopo', 'text');
    });
    it('should subtract from idea', function () {
        display.currentList = '$';
        var command = new commands.LocateTask("-idea", [1]);
        command.run(display, items).should.eql('$', 'list');
        items.getByPlan('$').items[0].text.should.eql('Test', 'text');
    });
    it('should convert task to idea', function () {
        var size = items.getByPlan('$').items.length;
        var command = new commands.LocateTask('', [1], "+", ['$']);
        command.run(display, items);
        items.getByPlan('$').items.length.should.eql(size + 1, 'ideas size');
    });
    it('should convert idea to task', function () {
        var size = items.getByPlan('home').items.length;
        var isize = items.getByPlan('$').items.length;
        display.currentList = '$';
        var command = new commands.LocateTask('', [1], ":", 'home');
        command.run(display, items);
        items.getByPlan('$').items.length.should.eql(isize - 1, 'ideas size');
        items.getByPlan('home').items.length.should.eql(size + 1, 'list size');
    });
    it('should run group task', function () {
        var size = items.getByPlan('t').items.length;
        var command = new commands.LocateTask('', [1, 2], ">");
        i.all.should.include(command.run(display, items));
        items.getByPlan('t').items.length.should.eql(size + 2, 'list size');
        items.getTimer().running.should.eql(true, 'timer');
    });
    it('should delete group task', function () {
        var size = items.getByPlan('a').items.length;
        var command = new commands.LocateTask('', [1, 2], "x");
        i.all.should.include(command.run(display, items));
        items.getByPlan('a').items.length.should.eql(size - 2, 'list size');
        items.getTimer().running.should.eql(false, 'timer');
    });
    it('should complete big group', function () {
        create();
        var size = items.getByPlan('test').items.length;
        var csize = items.getByPlan('d').items.length;
        size.should.eql(50, 'initial size');
        var pos = [];
        for (var j = 0; j < 50; j++) {
            pos.push(j + 1);
        }
        var command = new commands.LocateTask('', pos, "v");
        i.all.should.include(command.run(display, items));
        items.getByPlan('test').items.length.should.eql(size - 50, 'list size');
        items.getByPlan('d').items.length.should.eql(csize + 50, 'done list size');
    });
    it('should delete big group', function () {
        create();
        var size = items.getByPlan('test').items.length;
        size.should.eql(50, 'initial size');
        var pos = [];
        for (var j = 0; j < 50; j++) {
            pos.push(j + 1);
        }
        var command = new commands.LocateTask('', pos, "x");
        i.all.should.include(command.run(display, items));
        items.getByPlan('test').items.length.should.eql(size - 50, 'list size');
    });
    it('should delete all', function () {
        create();
        var size = items.getByPlan('test').items.length;
        size.should.eql(50, 'initial size');
        var pos = [];
        var command = new commands.LocateTask('', pos, "x");
        i.all.should.include(command.run(display, items));
        items.getByPlan('test').items.length.should.eql(0, 'list size');
    });
    it('should delete completed big group', function () {
        create();
        var size = items.getByPlan('test').items.length;
        var csize = items.getByPlan('d').items.length;
        size.should.eql(50, 'initial size');
        var pos = [];
        for (var j = 0; j < 50; j++) {
            pos.push(j + 1);
        }
        var command = new commands.LocateTask('', pos, "v");
        i.all.should.include(command.run(display, items));
        items.getByPlan('test').items.length.should.eql(size - 50, 'list size');
        items.getByPlan('d').items.length.should.eql(csize + 50, 'done list size');

        display.currentList = 'd';
        pos = [];
        for (j = 0; j < 50; j++) {
            pos.push(j + 2);
        }
        command = new commands.LocateTask('', pos, "x");
        i.done.should.include(command.run(display, items));
        items.getByPlan('d').items.length.should.eql(1, 'done list size');
    });
    it('should delete idea group', function () {
        display.currentList = '@';
        var command = new commands.LocateTask('', [1], "x");
        command.run(display, items).should.eql('@', 'idea list');
        items.getByPlan('@').items.length.should.eql(0, 'ideas size');
    });
    it('should trim task text', function () {
        display.currentList = 'g';
        var size = items.getByPlan('g').items.length;
        var command = new commands.EditTask('', " aaaa ");
        command.run(display, items).should.eql('g', 'list type');
        items.getByPlan('g').items.length.should.eql(size + 1, 'list size');
        items.getByPlan('g').items[size].text.should.eql('aaaa', 'task text');
    });
    it('should trim text', function () {
        display.currentList = '$';
        var size = items.getByPlan('$').items.length;
        var command = new commands.EditTask('', " aaaa ");
        command.run(display, items).should.eql('$', 'list type');
        items.getByPlan('$').items.length.should.eql(size + 1, 'list size');
        items.getByPlan('$').items[size].text.should.eql('aaaa', 'task text');
    });
    it('should trim text on edit', function () {
        display.currentList = '$';
        var command = new commands.LocateTask(" aaaa ", [1]);
        command.run(display, items).should.eql('$', 'list type');
        items.getByPlan('$').items[0].text.should.eql('aaaa', 'task text');
    });
    it('should search task', function () {
        create();
        var size = items.getByPlan('test').items.length;
        size.should.eql(50, 'initial size');
        var command = new commands.SearchTask('aaa');
        i.all.should.include(command.run(display, items));
        items.search.length.should.eql(50, 'search size');
    });
    it('should run timer', function () {
        var command = new commands.LocateTask('', [1], ">");
        command.run(display, items);
        items.getByPlan('t').items.length.should.eql(1, 'list size');
        items.getTimer().running.should.eql(true, 'timer running');
        items.getTimer().lastStage.should.eql('Elvn', 'timer stage');
    });
    it('should cancel timer', function () {
        new commands.LocateTask('', [1], ">").run(display, items);
        items.getTimer().running.should.eql(true, 'timer running');
        new commands.SwitchTimer("x").run(display, items);
        items.getTimer().running.should.eql(false, 'timer not running');
        items.getByPlan('d').items.length.should.eql(1, 'completed size');
    });
    it('should complete timer', function (done) {
        new commands.LocateTask('', [1], ">").run(display, items);
        items.getTimer().running.should.eql(true, 'timer running');
        new commands.SwitchTimer("v").run(display, items);
        setTimeout(function () {
            items.getTimer().running.should.eql(false, 'timer not running');
            items.getByPlan('d').items.length.should.eql(2, 'completed size');
            done();
        }, 1000);
    });
    it('should skip timer', function (done) {
        new commands.LocateTask('', [1], ">").run(display, items);
        items.getTimer().running.should.eql(true, 'timer running');
        items.getTimer().lastStage.should.eql('Elvn', 'timer stage');
        new commands.SwitchTimer("^").run(display, items);
        setTimeout(function () {
            items.getTimer().running.should.eql(true, 'timer running');
            items.getTimer().lastStage.should.eql('Work', 'timer stage');
            done();
        }, 1000);
    });
    it('should show help', function(){
        new commands.SwitchTimer("s").run(display, items);
        lastMessage.indexOf('Available commands:').should.not.eql(-1, 'contains help');
        new commands.LocateTask('', [1,2], "s").run(display, items);
        lastMessage.indexOf('Available commands:').should.not.eql(-1, 'contains help');
        new commands.SwitchSync("s").run(display, items);
        lastMessage.should.eql('Sync is not initialized', 'no sync message');
        items.getSync = function() {
            return {};
        };
        new commands.SwitchSync("s").run(display, items);
        lastMessage.indexOf('Available commands:').should.not.eql(-1, 'contains help');
    });
    it('should create journal entry', function () {
        var size = items.getByPlan('@').items.length;
        var command = new commands.JournalTask("2pm", "a");
        i.journal.should.include(command.run(display, items));
        items.getByPlan('@').items.length.should.eql(size + 1, 'list size');
    });
    it('should list journal entries', function () {
        var today = new Date();
        var s = [today.getMonth(), today.getDate(), today.getFullYear()].join('-');
        var size = items.getByPlan('@', s).items.length;
        var command = new commands.JournalTask("2pm", "a");
        i.journal.should.include(command.run(display, items));
        items.getByPlan('@', s).items.length.should.eql(size + 1, 'list size');
    });
});
