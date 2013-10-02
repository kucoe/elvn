var fs = require('fs');

var colors = require('./colors');

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
        var o = obj;
        if (fs.existsSync(path)) {
            var data = fs.readFileSync(path, 'utf-8');
            o = JSON.parse(data);
            for (var prop in o) {
                if (o.hasOwnProperty(prop)) {
                    o[prop] = obj[prop];
                }
            }
        }
        fs.writeFileSync(path, JSON.stringify(o), 'utf-8');
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
            this.config.lists.forEach(function(item) {
                if(item.color === color) {
                    list = item;
                    return false;
                }
                return true;
            });
            if (list) {
                res = {label: list.label, color: list.color};
                res.tasks = this.config.tasks.filter(function (item) {
                    return item.color === color;
                });
            }
        }
        return res;
    },

    saveTask: function (task) {
        this.checkInit();
        console.log(this.config);
        var list = this.getList(task.color);
        var tasks = list.tasks;
        var idx = tasks.indexOf(task);
        if (idx != -1) {
            tasks[idx] = task;
        } else {
            tasks.push(task);
        }
        this.commit();
        if (this.listener != null) {
            this.listener.call(this, "");//TODO fix this
        }
    },

    commit: function () {
        var path = this.getConfigPath();
        if (!fs.existsSync(path)) {
            file.write(path, this.config);
        }
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

    getConfig: function () {
        var path = this.getConfigPath();
        if (!fs.existsSync(path)) {
            file.write(path, defaultConfig);
        }
        return file.read(path, {});
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
        return this.getUserDir() + "/.elvn/";
    },

    getUserDir: function () {
        return process.env.HOME || process.env.HOMEPATH || process.env.USERPROFILE
    }
};
