var fs = require('fs');

var colors = require('./colors');
var t = require('./timer');
var sync = require('./sync');

var today = new Date();
today.setHours(0);
today.setMinutes(0);
today.setSeconds(0);
today.setMilliseconds(0);

var isToday = function(dateString) {
    var date = new Date(dateString);
    date.setHours(0);
    date.setMinutes(0);
    date.setSeconds(0);
    date.setMilliseconds(0);
    return today.getTime() == date.getTime();
};

var defaultConfig =
{
    lists: [
        {label: "Work", color: "b"},
        {label: "Personal", color: "g"}
    ],
    tasks: [
        {color: "b", text: "Test task", id: 1362771947351},
        {color: "g", text: "Test task 2", id: 1362771947352},
        {color: "g", text: "Test completed task", id: 1362771947353, completedOn: "2012-09-12"}
    ],
    ideas: [
        {text: "Test idea", id: 1362771947355}
    ]
};

var file = {

    read: function (path, obj) {
        var data = fs.readFileSync(path, 'utf-8');
        var o = JSON.parse(data);
        for (var prop in o) {
            if (o.hasOwnProperty(prop)) {
                obj[prop] = o[prop];
            }
        }
        return data;
    },

    write: function (path, obj) {
        fs.writeFileSync(path, JSON.stringify(obj), 'utf-8');
    },

    watch: function (path, obj, interval) {
        this.read(path, obj);
        var fWatch = function (curr, prev) {
            if (curr.mtime.getTime() > prev.mtime.getTime()) {
                file.read(path, obj)
            }
        };
        fs.watchFile(path, {interval: (interval || 5007) }, fWatch);
        return fWatch;
    },

    unwatch: function (path, listener) {
        fs.unwatchFile(path, listener);
    }
};

exports.Config = function () {
    this.config = {};
};

exports.Config.prototype = {

    getList: function (color) {
        this.checkInit();
        var res = null;
        if (colors.is(colors.all, color)) {
            res = {label: 'All', color: 'a'};
            res.tasks = this.config.tasks.filter(function (item) {
                return !item.completedOn;
            });
        } else if (colors.is(colors.today, color)) {
            res = {label: 'Today', color: 't'};
            res.tasks = this.config.tasks.filter(function (item) {
                return item.planned;
            });
        } else if (colors.is(colors.done, color)) {
            res = {label: 'Completed', color: 'd'};
            res.tasks = this.config.tasks.filter(function (item) {
                return item.completedOn;
            });
        } else {
            var list = null;
            this.config.lists.forEach(function (item) {
                if (item.color === color || item.label === color) {
                    list = item;
                    return false;
                }
                return true;
            });
            if (list) {
                res = {label: list.label, color: list.color};
                res.tasks = this.config.tasks.filter(function (item) {
                    return item.color === list.color && !item.completedOn;
                });
            }
        }
        return res;
    },

    getIdeas: function () {
        this.checkInit();
        return this.config.ideas;
    },

    runTask: function (task, onTime) {
        if (!task) {
            return;
        }
        this.checkInit();
        if (!task.planned) {
            this.saveTask({id: task.id, color: task.color, text: task.text, planned: true, completedOn: null});
        }
        var self = this;
        this.getTimer().runElvn(task, function (completed) {
            self.saveTask({id: task.id, color: task.color, text: task.text, planned: true, completedOn: new Date()});
            if (onTime) {
                onTime(completed);
            }
        });
    },

    saveTask: function (task) {
        this.checkInit();
        var tasks = this.config.tasks;
        var idx = this.byItem(task);
        if (idx != -1) {
            tasks[idx] = task;
        } else {
            tasks.push(task);
        }
        this.commit();
    },

    saveIdea: function (idea) {
        this.checkInit();
        var ideas = this.config.ideas;
        var idx = this.byItem(idea);
        if (idx != -1) {
            ideas[idx] = idea;
        } else {
            ideas.push(idea);
        }
        this.commit();
    },

    saveList: function(color, label) {
        if(!colors.is(colors.colors, color) || colors.is(colors.restricted, color)) {
            return;
        }
        this.checkInit();
        var list = this.getList(color);
        var idx = this.config.lists.indexOf(list);
        if (list != null) {
            if ((!label || colors.NOT_ASSIGNED === label)
                && list.color != 'b') {
                this.config.lists.splice(idx, 1);
            } else {
                this.config.lists[idx] = {color:list.color, label:label}
            }
        } else {
            this.config.lists.push({color:color[0], label:label});
        }
        this.commit();
    },

    removeTask: function (task) {
        this.checkInit();
        var tasks = this.config.tasks;
        var idx = this.byItem(task);
        if (idx != -1) {
            tasks.splice(idx, 1);
        }
        this.commit();
    },

    removeIdea: function (idea) {
        this.checkInit();
        var ideas = this.config.ideas;
        var idx = this.byItem(idea);
        if (idx != -1) {
            ideas.splice(idx, 1);
        }
        this.commit();
    },

    findTasks: function (query) {
        this.checkInit();
        var result = [];
        if (!query) {
            return result;
        }
        result = this.config.tasks;
        result = result.filter(function (task) {
            var text = task.text;
            return text && text.toLowerCase().indexOf(query.toLowerCase());
        });
        return result;
    },

    findIdeas: function (query) {
        this.checkInit();
        var result = [];
        if (!query) {
            return result;
        }
        result = this.config.ideas;
        result = result.filter(function (idea) {
            var text = idea.text;
            return text && text.toLowerCase().indexOf(query.toLowerCase());
        });
        return result;
    },

    commit: function (config) {
        if(config) {
            this.config = config;
        }
        file.write(this.getConfigPath(), this.config);
        if (this.sync != null) {
            this.sync.push();
        }
    },

    checkInit: function () {
        if (!this.fWatch) {
            this.getConfig();
            this.fWatch = file.watch(this.getConfigPath(), this.config);
        }
    },

    getTimer: function () {
        if (!this.timer) {
            this.timer = new t.Timer();
        }
        return this.timer;
    },

    getSync : function(cli) {
        if (!this.sync) {
            var path = this.getSyncPath();
            if (fs.existsSync(path)) {
                var cfg = {};
                file.read(path, cfg);
                var email = cfg.email;
                var server = cfg.server;
                var noKey = cfg.nokey;
                this.sync = new sync.Sync(email, noKey, server, getBasePath(), this, cli);
            }
        }
        return this.sync;
    },

    getStatus: function() {
        this.checkInit();
        var today = new Date().toLocaleDateString();
        var planned = this.getList('t').tasks.length;
        var done = 0;
        var tasks = this.getList('d').tasks;
        tasks.forEach(function (item) {
            if (isToday(item.completedOn)) {
                done++;
                if (item.planned) {
                    planned++;
                }
            }
        }, this);
        return today + " Planned: " + planned + "; Done: " + done;
    },

    getConfig: function () {
        var path = this.getConfigPath();
        if (!fs.existsSync(path)) {
            file.write(path, defaultConfig);
        }
        file.read(path, this.config);
        return this.config;
    },

    getHistoryPath: function () {
        this.checkElvnDir();
        return this.getBasePath() + "history";
    },

    getSyncPath: function () {
        this.checkElvnDir();
        return this.getBasePath() + "sync.json";
    },

    getConfigPath: function () {
        this.checkElvnDir();
        return this.getBasePath() + "config.json";
    },

    checkElvnDir: function () {
        var basePath = this.getBasePath();
        if (!fs.existsSync(basePath)) {
            fs.mkdirSync(basePath)
        }
    },

    getBasePath: function () {
        return this.getUserDir() + "/.e2/";
    },

    getUserDir: function () {
        return process.env.HOME || process.env.HOMEPATH || process.env.USERPROFILE
    },

    finish: function() {
        this.getTimer().cancel();
        this.commit();
        file.unwatch(this.getConfigPath(), this.fWatch);
    },

    byId: function (id) {
        var res = null;
        if(!id) {
            return res;
        }
        var ideas = this.config.ideas;
        ideas.forEach(function (idea) {
            if (id === idea.id) {
                res = idea;
                return false;
            }
            return true;
        });
        if (!res) {
            var tasks = this.config.tasks;
            tasks.forEach(function (task) {
                if (id === task.id) {
                    res = task;
                    return false;
                }
                return true;
            });
        }
        return res;
    },

    byItem: function (item) {
        var res = -1;
        if(!item || !item.id) {
            return res;
        }
        var ideas = this.config.ideas;
        ideas.forEach(function (idea, idx) {
            if (item.id === idea.id) {
                res = idx;
                return false;
            }
            return true;
        });
        if (res == -1) {
            var tasks = this.config.tasks;
            tasks.forEach(function (task, idx) {
                if (item.id === task.id) {
                    res = idx;
                    return false;
                }
                return true;
            });
        }
        return res;
    }

};
