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
    this.lists = [];
    this.ideas = [];
    this.tasks = [];

};

exports.Config.prototype = {

    getList: function (color) {
        this.checkInit();
        if (colors.is(colors.all, color)) {
            res = {label: 'All', color: 'a'};
            res.tasks = this.tasks.filter(function (item) {
                return !item.completedOn;
            });
        } else if (colors.is(colors.today, color)) {
            res = {label: 'Today', color: 't'};
            res.tasks = this.tasks.filter(function (item) {
                return item.planned;
            });
        } else if (colors.is(colors.done, color)) {
            res = {label: 'Completed', color: 'd'};
            res.tasks = this.tasks.filter(function (item) {
                return item.completedOn;
            });
        } else {
            var list = this.lists[color];
            var res = null;
            if (list) {
                res = {label: list.label, color: list.color};
                res.tasks = this.tasks.filter(function (item) {
                    return item.color === color;
                });
            }
        }
        return res;
    },

    checkInit: function () {
        if (!this.fWatch) {
            this.getConfig();
            this.fWatch = file.watch(this.getConfigPath(), this);
        }
    },

    getConfig: function () {
        var path = this.getConfigPath();
        if (!fs.existsSync(path)) {
            file.write(path, defaultConfig)
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
