var util = require('util');
var it = require('./items');

var Command = function () {
};

Command.prototype.run = function (display, items) {
    return null;
};

Command.prototype.forward = function (list, result) {
    if (result) {
        this.next = result;
    }
    if (list) {
        return list;
    }
    return null;
};

Command.prototype.exec = function (display, items) {
    if (!display || !items) {
        return;
    }
    var current = this.run(display, items);
    var next = this.next;
    if (next) {
        current = next.run(display, items);
    }
    if (current != null) {
        display.currentList = current;
    }
};

var SwitchList = function (plan) {
    Command.call(this);
    this.plan = plan;
    this.run = function (display, items) {
        items.search = null;
        var list = items.getByPlan(this.plan);
        if (list != null) {
            display.showList(list);
            return this.forward(list.plan);
        }
        return this.forward();
    };
};

var SwitchSync = function (command) {
    Command.call(this);
    this.command = command;
    this.run = function (display, items) {
        var sync = items.getSync();
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
            display.showItems(items.getItems());
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
    this.run = function (display, items) {
        var reshow = true;
        var timer = items.getTimer();
        var c = this.command;
        if (c) {
            if (c === ':') {
                timer.pause();
            } else if (c === '>') {
                if (timer.running) {
                    timer.resume();
                } else {
                    var tasks = items.getByPlan(it.today[0]).items;
                    if (tasks.length > 0) {
                        var onTime = function () {
                            var task = tasks.shift();
                            if (task) {
                                tasks.runItem(task, onTime);
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
        } else {
            timer.show();
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
    this.run = function (display, items) {
        var tasks = items.findItems(this.query);
        display.showItems(tasks);
        return this.forward('all');
    };
};

var EditTask = function (plan, text) {
    Command.call(this);
    this.plan = plan;
    this.text = text;
    this.run = function (display, items) {
        var currentList = display.currentList;
        this.updateTask({plan: 'work', text: text}, items, currentList);
        return this.forward(currentList, new SwitchList(currentList));
    };
    this.getId = function () {
        return new Date().getTime();
    };
    this.updateTask = function (item, items, currentList) {
        var id = item.id;
        if (id == null) {
            id = this.getId();
        }
        var c = this.plan;
        var t = this.text;
        if (!c || c.length == 0 || it.is(it.restricted, c)) {
            if (!it.is(it.restricted, currentList)) {
                c = currentList;
            } else {
                c = item.plan;
            }
        }
        if (c == null && t.charAt(0) == ':') {
            t = t.substring(1);
        }
        if (t != null) {
            t = this.processText(item.text, t);
        } else {
            t = item.text;
        }
        var planned = it.is(it.today, item.plan) || it.is(it.today, currentList);
        var completedOn = it.is(it.done, item.plan);
        it.set(c, it.today[1], planned);
        it.set(c, it.done[1], completedOn);
        items.saveItem({id: id, plan: c, text: t});
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

var LocateTask = function (text, positions, command, plan) {
    EditTask.call(this, null, text);
    this.positions = positions;
    this.command = command;
    this.plan = plan;
    this.run = function (display, items) {
        var currentList = display.currentList;
        var next = new SwitchList(currentList);
        var its = this.getTasks(currentList, items);
        var first = null;
        its.forEach(function (item, i) {
            var c = this.command;
            if (c) {
                var p = item.plan;
                var save = true;
                if (c == '>') {
                    first = item;
                    p = it.set(p, it.today[1], true);
                    p = it.set(p, it.done[1], false);
                    next = null;
                } else if (c == 'x') {
                    items.removeItem(item);
                    save = false;
                } else if (c == '+') {
                    if (this.plan && this.plan.length > 0) {
                        this.plan.forEach(function (plan) {
                            p = it.set(p, plan, true);
                        });
                    } else {
                        p = it.set(p, it.today[1], true);
                        p = it.set(p, it.done[1], false);
                    }
                } else if (c == '-') {
                    if (this.plan && this.plan.length > 0) {
                        this.plan.forEach(function (plan) {
                            p = it.set(p, plan, false);
                        });
                    } else {
                        p = it.set(p, it.today[1], false);
                        p = it.set(p, it.done[1], false);
                    }
                } else if (c == 'v') {
                    p = it.set(p, it.done[1], true);
                } else if (c == '^') {
                    p = it.set(p, it.done[1], false);
                } else if (c == ':') {
                    p = this.plan;
                } else {
                    c = null;
                }
                if (save) {
                    items.saveItem({id: item.id, plan: p, text: item.text});
                }
                if (c == null) {
                    next = null;
                    display.show(this.help(this.command));
                }
            } else if (!this.plan && !this.text) {
                next = null;
                display.showItem(item, this.positions[i]);
            } else {
                this.updateTask(item, items, currentList);
            }
        }, this);
        if (first) {
            items.runItem(first, null);
        }
        return this.forward(currentList, next);
    };
    this.getTasks = function (currentList, items) {
        var res = [];
        var list = items.getByPlan(currentList);
        if (list) {
            var its = items.search ? items.search : list.items;
            if (this.positions.length == 0) {
                return its;
            }
            this.positions.forEach(function (pos) {
                if (its.length >= pos) {
                    res.push(its[pos - 1]);
                }
            });
        }
        return res;
    };
    this.help = function (invalid) {
        return "Wrong item command: " + invalid + "\n" + "Available commands:\n" + "\t> run item\n"
            + "\t+ make item planned for today\n" + "\t- make item not planned for today\n"
            + "\tv make item completed\n" + "\t^ make item not completed\n" + "\tx delete item";
    };
};

util.inherits(SwitchList, Command);
util.inherits(SwitchSync, Command);
util.inherits(SwitchTimer, Command);
util.inherits(SearchTask, Command);
util.inherits(EditTask, Command);
util.inherits(LocateTask, EditTask);


exports.Command = Command;
exports.SwitchList = SwitchList;
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
    } else if (args.positions == '*') {
        positions = [];
    } else {
        var split = args.positions.split(',');
        split.forEach(function (pos) {
            positions.push(parseInt(pos));
        });
    }
    return positions;
}

function calculatePlan(args) {
    var plan = args.plan ? args.plan.split(',') : [];
    var res = [];
    plan.forEach(function(item) {
        res.push(item.trim());
    });
    return res;
}

exports.init = function (cli, display, items) {
    cli.command('/{plan}', 'show items by plan', {plan: '\\w+'}, function (input, args) {
        var cmd = new SwitchList(args.plan);
        cmd.exec(display, items);
        return cmd;
    });
    cli.command('%{command}', 'sync items and executes command if specified.' +
        '\n\tSync commands:\n\t> push\n\t< pull\n\t- revert', {command: '[-<>]?'}, function (input, args) {
        var cmd = new SwitchSync(args.command);
        cmd.exec(display, items);
        return cmd;
    });
    cli.command('${command}', 'show timer and executes command if specified. ' +
        '\n\tTimer commands:\n\t> runs/resume task\n\t: pause task\n\t^ skip current stage\n\tx cancel task\n\tv complete task',
        {command: '[:>^vx]?'}, function (input, args) {
            var cmd = new SwitchTimer(args.command);
            cmd.exec(display, items);
            return cmd;
        });
    cli.command('?{query}', 'search item', {query: '.+'}, function (input, args) {
        var cmd = new SearchTask(args.query);
        cmd.exec(display, items);
        return cmd;
    });
    cli.command('#{start}-{end}={search}%{replacement}', 'locate item by range (ex. #1-4) and replace search with replacement',
        {start: '\\d+', end: '\\d+', search: '.+', replacement: '.+'}, function (input, args) {
            var cmd = new LocateTask(args.search + '%' + args.replacement, calculateRange(args));
            cmd.exec(display, items);
            return cmd;
        });
    cli.command('#{start}-{end}={text}', 'locate item by range (ex. #1-4) and change its text',
        {start: '\\d+', end: '\\d+', text: '.+'}, function (input, args) {
            var cmd = new LocateTask(args.text, calculateRange(args));
            cmd.exec(display, items);
            return cmd;
        });
    cli.command('#{start}-{end}{command}', 'show item by range (ex. #1-4) and executes command if specified. ' +
        '\n\tItem commands:\n\t> run item\n\t+ plan item for today\n\t- not plan item for today\n' +
        '\tv make item completed\n\t^ make item not completed\n\tx delete item',
        {start: '\\d+', end: '\\d+', command: '[>+-v^x]?'}, function (input, args) {
            var cmd = new LocateTask('', calculateRange(args), args.command);
            cmd.exec(display, items);
            return cmd;
        });
    cli.command('#{start}-{end}:{plan}', 'locate item by range (ex. #1-4) and change its plan',
        {start: '\\d+', end: '\\d+', plan: '[^:]*'}, function (input, args) {
            var cmd = new LocateTask('', calculateRange(args), ':', calculatePlan(args));
            cmd.exec(display, items);
            return cmd;
        });
    cli.command('#{start}-{end}+{plan}', 'locate item by range (ex. #1-4) and appends to its plan',
        {start: '\\d+', end: '\\d+', plan: '[^:]*'}, function (input, args) {
            var cmd = new LocateTask('', calculateRange(args), '+', calculatePlan(args));
            cmd.exec(display, items);
            return cmd;
        });
    cli.command('#{start}-{end}-{plan}', 'locate item by range (ex. #1-4) and removes from its plan',
        {start: '\\d+', end: '\\d+', plan: '[^:]*'}, function (input, args) {
            var cmd = new LocateTask('', calculateRange(args), '-', calculatePlan(args));
            cmd.exec(display, items);
            return cmd;
        });
    cli.command('#{positions}={search}%{replacement}', 'locate item by positions (ex. #2 or #2,4) and replace search with replacement',
        {positions: '(\\d+,?|\\*)+', search: '.+', replacement: '.+'}, function (input, args) {
            var cmd = new LocateTask(args.search + '%' + args.replacement, calculatePositions(args));
            cmd.exec(display, items);
            return cmd;
        });
    cli.command('#{positions}={text}', 'locate item by positions (ex. #2 or #2,4) and change its text',
        {positions: '(\\d+,?|\\*)+', text: '.+'}, function (input, args) {
            var cmd = new LocateTask(args.text, calculatePositions(args));
            cmd.exec(display, items);
            return cmd;
        });
    cli.command('#{positions}{command}', 'show item by positions (ex. #2 or #2,4) and executes command if specified.' +
        '\n\tItem commands:\n\t> run item\n\t+ plan item for today\n\t- not plan item for today\n' +
        '\tv make item completed\n\t^ make item not completed\n\tx delete item',
        {positions: '(\\d+,?|\\*)+', command: '[>+-v^x]?'}, function (input, args) {
            var cmd = new LocateTask('', calculatePositions(args), args.command);
            cmd.exec(display, items);
            return cmd;
        });
    cli.command('#{positions}:{plan}', 'locate item by positions (ex. #2 or #2,4) and changes its plan',
        {positions: '(\\d+,?|\\*)+', plan: '[^:]*'}, function (input, args) {
            var cmd = new LocateTask('', calculatePositions(args), ':', calculatePlan(args));
            cmd.exec(display, items);
            return cmd;
        });
    cli.command('#{positions}+{plan}', 'locate item by positions (ex. #2 or #2,4) and appends tp its plan',
        {positions: '(\\d+,?|\\*)+', plan: '[^:]*'}, function (input, args) {
            var cmd = new LocateTask('', calculatePositions(args), '+', calculatePlan(args));
            cmd.exec(display, items);
            return cmd;
        });
    cli.command('#{positions}-{plan}', 'locate item by positions (ex. #2 or #2,4) and removes from its plan',
        {positions: '(\\d+,?|\\*)+', plan: '[^:]*'}, function (input, args) {
            var cmd = new LocateTask('', calculatePositions(args), '-', calculatePlan(args));
            cmd.exec(display, items);
            return cmd;
        });
    cli.command('{plan}:{text}', 'add item', {plan: '[^:]*', text: '.+'}, function (input, args) {
        var cmd = new EditTask(calculatePlan(args), args.text);
        cmd.exec(display, items);
        return cmd;
    });
    cli.command('\\\\s', 'toggle silent state for timer', function () {
        items && items.getTimer().toggleSilent();
    });
};
