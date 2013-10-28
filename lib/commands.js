var util = require('util');

var colors = require('./colors');

var Command = function () {
};

Command.prototype.run = function (display, config) {
    return null;
};

Command.prototype.forward = function (list, result) {
    if(result) {
        this.next = result;
    }
    if(list) {
        return list;
    }
    return null;
};

var SwitchIdeas = function () {
    Command.call(this);
    this.run = function (display, config) {
        var ideas = config.getIdeas();
        display.showIdeas(ideas);
        return this.forward('@');
    };
};

var SwitchList = function (color) {
    Command.call(this);
    this.color = color;
    this.run = function (display, config) {
        config.search = null;
        var list = config.getList(this.color);
        if (list != null) {
            display.showList(list);
            return this.forward(list.color);
        }
        return this.forward();
    };
};

var SwitchListEdit = function () {
    Command.call(this);
    this.run = function (display, config) {
        display.showLists(config);
        return this.forward('&');
    };
};

var SwitchStatus = function () {
    Command.call(this);
    this.run = function (display, config) {
        display.show(config.getStatus());
        return this.forward();
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
                    display.show(this.help(c));
                }
            } else {
                display.show("Sync is not initialized");
            }
        } else {
            display.showConfig(config.getConfig());
        }
        return this.forward();
    };
    this.help = function (invalid) {
        return "Wrong sync command: " + invalid + "\n" + "Available commands:\n" + "\t> push\n" + "\t< pull\n"
            + "\t- revert";
    };
};

var SwitchTimer = function (command) {
    Command.call(this);
    this.command = command;
    this.run = function (display, config) {
        var reshow = true;
        var timer = config.getTimer();
        var c = this.command;
        if (c == null) {
            timer.show();
            return null;
        } else {
            if (c === ':') {
                timer.pause();
            } else if (c === '>') {
                if (timer.running) {
                    timer.resume();
                } else {
                    var tasks = config.getList('t').tasks;
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
                timer.cancel();
            } else if (c === '^') {
                timer.fire(false);
            } else if (c === 'v') {
                timer.fire(true);
            } else {
                display.show(this.help(c));
                reshow = false;
            }
            if (reshow) {
                timer.show();
            }
        }
        return this.forward();
    };
    this.help = function (invalid) {
        return "Wrong timer command: " + invalid + "\n" + "Available commands:\n" + "\t> run/resume task\n"
            + "\t: pause task\n" + "\t^ skip current stage\n" + "\tx cancel task\n" + "\tv complete task";
    };
};

var SearchTask = function (query) {
    Command.call(this);
    this.query = query;
    this.run = function (display, config) {
        var currentList = display.currentList;
        if (currentList == '@') {
            var ideas = config.findIdeas(this.query);
            display.showIdeas(ideas);
            return currentList;
        }
        var tasks = config.findTasks(this.query);
        display.showTasks(tasks);
        return this.forward('all');
    };
};

var EditTask = function (color, text) {
    Command.call(this);
    this.color = color;
    this.text = text;
    this.run = function (display, config) {
        var currentList = display.currentList;
        if (currentList == '@') {
            var t = this.text;
            if (t == null) {
                t = "";
            }
            if (this.color) {
                t = this.color + ":" + t;
            }
            config.saveIdea({id: this.getId(), text: t.trim()});
            return this.forward(currentList, new SwitchIdeas());
        }
        if (currentList == '&') {
            config.saveList(color, text);
            return this.forward(currentList, new SwitchListEdit());
        }
        this.updateTask({color: 'b', text: text}, config, currentList);
        return this.forward(currentList, new SwitchList(currentList));
    };
    this.getId = function () {
        return new Date().getTime();
    };
    this.updateTask = function (task, config, currentList) {
        var id = task.id;
        if (id == null) {
            id = this.getId();
        }
        var c = this.color;
        var t = this.text;
        if (!c || !colors.is(colors.colors, c) || colors.is(colors.restricted, c)) {
            if (c && !colors.is(colors.colors, c)) {
                t = t == null ? c + ":" : c + ":" + t;
            }
            if (!colors.is(colors.restricted, currentList)) {
                c = currentList;
            } else {
                c = task.color;
            }
        }
        if(c == null && t.charAt(0) == ':') {
            t = t.substring(1);
        }
        if (t != null) {
            t = this.processText(task.text, t);
        } else {
            t = task.text;
        }
        var planned = task.planned || colors.is(colors.today, currentList);
        var completedOn = task.completedOn || null;
        config.saveTask({id: id, color: c, text: t, planned: planned, completedOn: completedOn});
    };
    this.processText = function (oldText, newText) {
        var t = newText;
        if (t.indexOf("%") != -1) {
            var split = t.split("%");
            var replace = "";
            if (split.length > 1) {
                replace = split[1];
            }
            t = oldText.replace(split[0], replace);
        } else if (t.charAt(0) == "+") {
            t = oldText.concat(t.substring(1));
        } else if (t.charAt(0) == "-") {
            t = oldText.replace(t.substring(1), "");
        }
        return t.trim();
    };
};

var LocateTask = function (text, positions, command) {
    EditTask.call(this, null, text);
    this.positions = positions;
    this.command = command;
    this.run = function (display, config) {
        var currentList = display.currentList;
        var c = this.command;
        var next = new SwitchIdeas();
        if (currentList == '@') {
            var ideas = this.getIdeas(config);
            ideas.forEach(function (idea, i) {
                if (c) {
                    if (c == 'x') {
                        config.removeIdea(idea);
                    } else if (colors.is(colors.colors, c)) {
                        config.removeIdea(idea);
                        config.saveTask({id: idea.id, color: c, text: idea.text});
                        next = new SwitchList('all');
                    }
                } else if (this.text == null && this.color == null) {
                    next = null;
                    display.showIdea(idea, this.positions[i]);
                } else if (this.text) {
                    var t = this.text;
                    if (t == null) {
                        t = "";
                    }
                    t = this.color == null ? t : this.color + ":" + t;
                    config.saveIdea({id: idea.id, text: this.processText(idea.text, t)});
                }
            }, this);
            return this.forward(currentList, next);
        }
        var tasks = this.getTasks(currentList, config);
        next = new SwitchList(currentList);
        var first = null;
        tasks.forEach(function (task, i) {
            if (c) {
                if (c == '>') {
                    first = task;
                    config.saveTask({id: task.id, color: task.color, text: task.text, planned: true, completedOn: null});
                    next = null;
                } else if (c == 'x') {
                    config.removeTask(task);
                } else if (c == '+') {
                    config.saveTask({id: task.id, color: task.color, text: task.text, planned: true, completedOn: null});
                } else if (c == '-') {
                    config.saveTask({id: task.id, color: task.color, text: task.text, planned: false, completedOn: null});
                } else if (c == 'v') {
                    config.saveTask({id: task.id, color: task.color, text: task.text, planned: task.planned, completedOn: new Date()});
                } else if (c == '^') {
                    config.removeTask(task);
                    config.saveTask({id: task.id, color: task.color, text: task.text, planned: task.planned, completedOn: null});
                } else if (c == '@') {
                    config.removeTask(task);
                    config.saveIdea({id: task.id, text: task.text});
                    next = new SwitchIdeas();
                } else {
                    display.show(this.help(c));
                }
            } else if (this.color == null && this.text == null) {
                next = null;
                display.showTask(task, this.positions[i]);
            } else {
                this.updateTask(task, config, currentList);
            }
        }, this);
        if (first) {
            config.runTask(first, null);
        }
        return this.forward(currentList, next);
    };
    this.getIdeas = function (config) {
        var ideas = config.search ? config.search : config.getIdeas();
        if (this.positions.length == 0) {
            return ideas;
        }
        var res = [];
        this.positions.forEach(function (pos) {
            if (ideas.length >= pos) {
                res.push(ideas[pos - 1]);
            }
        });
        return res;
    };
    this.getTasks = function (currentList, config) {
        var res = [];
        if (currentList == '@') {
            return res;
        }
        var list = config.getList(currentList);
        if (list) {
            var tasks = config.search ? config.search : list.tasks;
            if (this.positions.length == 0) {
                return tasks;
            }
            this.positions.forEach(function (pos) {
                if (tasks.length >= pos) {
                    res.push(tasks[pos - 1]);
                }
            });
        }
        return res;
    };
    this.help = function (invalid) {
        return "Wrong task command: " + invalid + "\n" + "Available commands:\n" + "\t> run task\n"
            + "\t@ convert task to idea\n" + "\t+ make task planned\n" + "\t- make task not planned\n"
            + "\tv make task completed\n" + "\t^ make task not completed\n" + "\tx delete task";
    };
};

util.inherits(SwitchIdeas, Command);
util.inherits(SwitchList, Command);
util.inherits(SwitchListEdit, Command);
util.inherits(SwitchStatus, Command);
util.inherits(SwitchSync, Command);
util.inherits(SwitchTimer, Command);
util.inherits(SearchTask, Command);
util.inherits(EditTask, Command);
util.inherits(LocateTask, EditTask);


exports.Command = Command;
exports.SwitchIdeas = SwitchIdeas;
exports.SwitchList = SwitchList;
exports.SwitchListEdit = SwitchListEdit;
exports.SwitchStatus = SwitchStatus;
exports.SwitchSync = SwitchSync;
exports.SwitchTimer = SwitchTimer;
exports.SearchTask = SearchTask;
exports.EditTask = EditTask;
exports.LocateTask = LocateTask;


function calculateRange(args) {
    var start = args.start;
    var end = args.end;
    if (start > end) {
        var tmp = start;
        start = end;
        end = tmp;
    }
    var positions = [];
    var i = 0;
    while (start <= end) {
        positions[i++] = start++;
    }
    return positions;
}

function calculatePositions(args) {
    var positions = [];
    if (typeof args.positions === 'number') {
        positions = [args.positions];
    } else if(args.positions == '*') {
        positions = [];
    } else {
        var split = args.positions.split(',');
        split.forEach(function (pos) {
            positions.push(parseInt(pos));
        });
    }
    return positions;
}

exports.init = function (cli) {
    cli.command('/{color}', 'show tasks list by color or label', {color: '\\w+'}, function (input, args) {
        return new SwitchList(args.color);
    });
    cli.command('@', 'show ideas', function () {
        return new SwitchIdeas();
    });
    cli.command('&', 'show available lists', function () {
        return new SwitchListEdit();
    });
    cli.command('!', 'show status', function () {
        return new SwitchStatus();
    });
    cli.command('%{command}', 'sync config and executes command if specified.' +
        '\n\tSync commands:\n\t> push\n\t< pull\n\t- revert', {command: '[-<>]?'}, function (input, args) {
        return new SwitchSync(args.command);
    });
    cli.command('${command}', 'show timer and executes command if specified. ' +
        '\n\tTimer commands:\n\t> run/resume task\n\t: pause task\n\t^ skip current stage\n\tx cancel task\n\tv complete task',
        {command: '[:>^vx]?'}, function (input, args) {
            return new SwitchTimer(args.command);
        });
    cli.command('?{query}', 'search task or idea', {query: '.+'}, function (input, args) {
        return new SearchTask(args.query);
    });
    cli.command('#{start}-{end}={search}%{replacement}', 'locate task or idea by range (ex. #1-4) and replace search with replacement',
        {start: '\\d+', end: '\\d+', search: '.+', replacement: '.+'}, function (input, args) {
            return new LocateTask(args.search + '%' + args.replacement, calculateRange(args));
        });
    cli.command('#{start}-{end}={text}', 'locate task or idea by range (ex. #1-4) and change its text',
        {start: '\\d+', end: '\\d+', text: '.+'}, function (input, args) {
            return new LocateTask(args.text, calculateRange(args));
        });
    cli.command('#{start}-{end}{command}', 'show task or idea by range (ex. #1-4) and executes command if specified. ' +
        '\n\tTask commands:\n\t> run task\n\t@ convert task to idea\n\t+ make task planned\n\t- make task not planned\n' +
        '\tv make task completed\n\t^ make task not completed\n\tx delete task',
        {start: '\\d+', end: '\\d+', command: '[>@+-v^x]?'}, function (input, args) {
            return new LocateTask('', calculateRange(args), args.command);
        });
    cli.command('#{positions}={search}%{replacement}', 'locate task or idea by positions (ex. #2 or #2,4) and replace search with replacement',
        {positions: '(\\d+,?|\\*)+', search: '.+', replacement: '.+'}, function (input, args) {
            return new LocateTask(args.search + '%' + args.replacement, calculatePositions(args));
        });
    cli.command('#{positions}={text}', 'locate task or idea by positions (ex. #2 or #2,4) and change its text',
        {positions: '(\\d+,?|\\*)+', text: '.+'}, function (input, args) {
            return new LocateTask(args.text, calculatePositions(args));
        });
    cli.command('#{positions}{command}', 'show task or idea by positions (ex. #2 or #2,4) and executes command if specified.' +
        '\n\tTask commands:\n\t> run task\n\t@ convert task to idea\n\t+ make task planned\n\t- make task not planned\n' +
        '\tv make task completed\n\t^ make task not completed\n\tx delete task',
        {positions: '(\\d+,?|\\*)+', command: '[>@+-v^x]?'}, function (input, args) {
            return new LocateTask('', calculatePositions(args), args.command);
        });
    cli.command('{color}:{text}', 'add task or idea', {color: '[roygbfp]?', text: '.+'}, function (input, args) {
        return new EditTask(args.color, args.text);
    });
};
