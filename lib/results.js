var util = require('util');

var Command = function () {
    this.forwardEnabled = true;
};

Command.prototype.run = function (display, config) {
    return null;
};

Command.prototype.forward = function (result, display, config) {
    if (!this.forwardEnabled) {
        return null;
    }
    return result.run(display, config);
};

var SwitchIdeas = function () {
    Command.call(this);
    this.run = function (display, config) {
        var ideas = config.getIdeas();
        display.showIdeas(ideas);
        return ELCommand.Ideas.el();
    };
};

var SwitchList = function (color) {
    Command.call(this);
    this.color = color;
    this.run = function (display, config) {
        var list = config.getList(this.color);
        if (list != null) {
            display.showList(list);
            return list.getColor();
        }
        return null;
    };
};

var SwitchListEdit = function () {
    Command.call(this);
    this.run = function (display, config) {
        display.showLists(config);
        return ELCommand.ListEdit.el();
    };
};

var SwitchStatus = function () {
    Command.call(this);
    this.run = function (display, config) {
        display.showStatus(config.getStatus());
        return null;
    };
};

var SwitchSync = function (command) {
    Command.call(this);
    this.command = command;
    this.run = function (display, config) {
        var sync = config.getSync();
        var c = this.command;
        if (c) {
            if (sync != null) {
                if (c === '<') {
                    sync.pull();
                } else if (c === '>') {
                    sync.push();
                } else if (c === '-') {
                    sync.restore();
                } else {
                    display.showHelp(this.help(c));
                }
            } else {
                display.showHelp("Sync is not initialized");
            }
        }
        config.reload();
        display.showConfig(config.getConfig());
        return ELCommand.Sync.el();
    };
    this.help = function (invalid) {
        return "\tWrong sync command: " + invalid + "\n" + "\tAvailable commands:\n" + "\t> push\n" + "\t< pull\n"
            + "\t- revert";
    };
};

var SwitchTimer = function (command) {
    Command.call(this);
    this.command = command;
    this.run = function (display, config) {
        var reshow = true;
        var c = this.command;
        if (c == null) {
            Timer.show();
            return null;
        } else {
            if (c === ':') {
                Timer.pause();
            } else if (c === '>') {
                if (Timer.isRunning()) {
                    Timer.resume();
                } else {
                    var tasks = config.getList(ListColor.Today).getTasks();
                    if (tasks.length > 0) {
                        var onTime = function () {
                            var task = tasks.shift();
                            if (task) {
                                config.runTask(task, onTime);
                            }
                        };
                        onTime();
                    }
                    reshow = false;
                }
            } else if (c === 'x') {
                Timer.cancel();
            } else if (c === '^') {
                Timer.fire(false);
            } else if (c === 'v') {
                Timer.fire(true);
            } else {
                display.showHelp(this.help(c));
            }
            if (reshow) {
                Timer.show();
            }
        }
        return null;
    };
    this.help = function (invalid) {
        return "\tWrong timer command: " + invalid + "\n" + "\tAvailable commands:\n" + "\t> run/resume task\n"
            + "\t: pause task\n" + "\t^ skip current stage\n" + "\tx cancel task\n" + "\tv complete task";
    };
};

util.inherits(SwitchIdeas, Command);
util.inherits(SwitchList, Command);
util.inherits(SwitchListEdit, Command);
util.inherits(SwitchStatus, Command);
util.inherits(SwitchSync, Command);
util.inherits(SwitchTimer, Command);


exports.Command = Command;
exports.SwitchIdeas = SwitchIdeas;
exports.SwitchList = SwitchList;
exports.SwitchListEdit = SwitchListEdit;
exports.SwitchStatus = SwitchStatus;
exports.SwitchSync = SwitchSync;
exports.SwitchTimer = SwitchTimer;
