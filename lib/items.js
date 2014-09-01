var fs = require('fs');

var t = require('./timer');
var sync = require('./sync');

exports.restricted = ['a', 'all', 't', 'today', 'd', 'done', 'completed'];

var all = exports.all = ['a', 'all'];
var today = exports.today = ['t', 'today'];
var done = exports.done = ['d', 'done'];
var idea = exports.idea = ['$', 'idea'];
var journal = exports.journal = ['@', 'journal'];

var is = exports.is = function (testPlan, plan) {
    if (!Array.isArray(testPlan)) {
        testPlan = [testPlan];
    }
    if (!Array.isArray(plan)) {
        plan = [plan];
    }
    for (var i = 0; i < testPlan.length; i++) {
        var el = testPlan[i];
        if (plan.indexOf(el.toLowerCase()) != -1) {
            return true;
        }
    }
    return false;
};

var set = exports.set = function (plan, addPlan, isSet) {
    if (!Array.isArray(plan)) {
        plan = [plan];
    } else {
        plan = plan.slice();
    }
    if (isSet && !is(plan, addPlan)) {
        plan.push(addPlan);
    }
    if (!isSet && is(plan, addPlan)) {
        plan.splice(plan.indexOf(addPlan), 1);
    }
    return plan;
};

var simpleSort = function (a, b) {
    return a.id - b.id;
};

var itemsSort = function (a, b) {
    var aPlanned = is(today, a.plan) || false;
    var bPlanned = is(today, b.plan) || false;
    return (aPlanned == bPlanned) ? simpleSort(a, b) : (aPlanned ? -1 : 1);
};

var todayString = function () {
    var today = new Date();
    var date = today.getDate();
    if (date < 10) {
        date = "0" + date;
    }
    var month = (today.getMonth() + 1);
    if (month < 10) {
        month = "0" + month;
    }
    return [date, month, today.getFullYear()].join('-');
};

var now = function () {
    var today = new Date();
    var hours = today.getHours();
    if (hours < 10) {
        hours = "0" + hours;
    }
    var minutes = today.getMinutes();
    if (minutes < 10) {
        minutes = "0" + minutes;
    }
    return [hours, minutes].join(':');
};

var defaultItems =
    [
        {plan: ["work"], text: "Test task", id: 1362771947351},
        {plan: ["home"], text: "Test task 2", id: 1362771947352},
        {plan: ["home", "done"], text: "Test completed task", id: 1362771947353},
        {plan: ["work", "idea"], text: "Test idea", id: 1362771947355}
    ];

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

exports.Items = function () {
    this.items = [];
    this.search = null;
    this.changes = 0;
};

exports.Items.prototype = {

    getId: function () {
        return new Date().getTime();
    },

    getByPlan: function (plan) {
        this.checkInit();
        var res = null;
        if (is(all, plan)) {
            res = {label: 'All', plan: all[0]};
            res.items = this.items.filter(function (item) {
                return !is(done, item.plan);
            });
        } else if (is(today, plan)) {
            res = {label: 'Today', plan: today[0]};
            res.items = this.items.filter(function (item) {
                return is(today, item.plan) && !is(done, item.plan);
            });
        } else if (is(idea, plan)) {
            res = {label: 'Ideas', plan: idea[0]};
            res.items = this.items.filter(function (item) {
                return is(idea, item.plan) && !is(done, item.plan);
            });
        } else if (is(done, plan)) {
            res = {label: 'Done', plan: done[0]};
            res.items = this.items.filter(function (item) {
                return is(done, item.plan);
            });
        } else if (is(journal, plan)) {
            var when = arguments.length > 1 ? arguments[1] : null;
            var date = when || todayString();
            res = {label: 'Journal for ' + date, plan: journal[0], date: date};
            res.items = this.items.filter(function (item) {
                return is(journal, item.plan) && is(date, item.plan);
            });
        } else {
            res = {label: plan, plan: plan};
            res.items = this.items.filter(function (item) {
                var p = item.plan;
                return is(plan, p) && !is(done, p) && !is(idea, p);
            });
        }
        if (res) {
            res.items = res.items.sort(itemsSort);
        }
        return res;
    },

    runItem: function (item, onTime) {
        if (!item) {
            return;
        }
        this.checkInit();
        var p = item.plan;
        p = set(p, today[1], true);
        p = set(p, done[1], false);
        this.saveItem({id: item.id, plan: p, text: item.text});
        var self = this;
        this.getTimer().runElvn(item, function (completed) {
            var p = item.plan;
            p = set(p, today[1], true);
            p = set(p, done[1], true);
            self.saveItem({id: item.id, plan: p, text: item.text});
            if (onTime) {
                onTime(completed);
            }
        });
    },

    journal: function (entry, time) {
        var plan = journal[1];
        plan = set(plan, todayString(), true);
        plan = set(plan, time || now(), true);
        var item = {id: this.getId(), plan: plan, text: entry};
        this.saveItem(item);
    },

    saveItem: function (item) {
        this.checkInit();
        var items = this.items;
        var idx = this.byItem(item);
        if (idx != -1) {
            items[idx] = item;
        } else {
            items.push(item);
        }
        this.commit();
    },

    removeItem: function (item) {
        this.checkInit();
        var items = this.items;
        var idx = this.byItem(item);
        if (idx != -1) {
            items.splice(idx, 1);
        }
        this.commit();
    },

    findItems: function (query) {
        this.checkInit();
        var result = [];
        if (!query) {
            return result;
        }
        result = this.items;
        result = result.filter(function (item) {
            var text = item.text;
            return text && text.toLowerCase().indexOf(query.toLowerCase()) >= 0;
        });
        result = result.sort(itemsSort);
        this.search = result;
        return result;
    },

    commit: function (items) {
        if (items) {
            this.config = items;
        }
        file.write(this.getItemsPath(), this.items);
        this.changes++;
        if (this.sync != null && this.changes > 5) {
            this.sync.push();
            this.changes = 0;
        }
    },

    checkInit: function () {
        if (!this.fWatch) {
            this.getItems();
            this.fWatch = file.watch(this.getItemsPath(), this.items);
        }
    },

    getTimer: function () {
        if (!this.timer) {
            this.timer = new t.Timer();
        }
        return this.timer;
    },

    getSync: function (cli) {
        if (!this.sync) {
            var path = this.getSyncPath();
            if (fs.existsSync(path)) {
                var cfg = {};
                file.read(path, cfg);
                var email = cfg.email;
                var server = cfg.server;
                var noKey = cfg.nokey;
                this.sync = new sync.Sync(email, noKey, server, this.getBasePath(), this, cli);
            }
        }
        return this.sync;
    },

    getItems: function () {
        var path = this.getItemsPath();
        if (!fs.existsSync(path)) {
            file.write(path, defaultItems);
        }
        file.read(path, this.items);
        return this.items;
    },

    getHistoryPath: function () {
        this.checkElvnDir();
        return this.getBasePath() + "history";
    },

    getSyncPath: function () {
        this.checkElvnDir();
        return this.getBasePath() + "sync.json";
    },

    getItemsPath: function () {
        this.checkElvnDir();
        return this.getBasePath() + "items.json";
    },

    checkElvnDir: function () {
        var basePath = this.getBasePath();
        if (!fs.existsSync(basePath)) {
            fs.mkdirSync(basePath)
        }
    },

    getBasePath: function () {
        return this.getUserDir() + "/.11/";
    },

    getUserDir: function () {
        return process.env.HOME || process.env.HOMEPATH || process.env.USERPROFILE
    },

    finish: function () {
        this.getTimer().cancel();
        this.commit();
        file.unwatch(this.getItemsPath(), this.fWatch);
    },

    byItem: function (item) {
        var res = -1;
        if (!item || !item.id) {
            return res;
        }
        var items = this.items;
        items.forEach(function (i, idx) {
            if (i.id === item.id) {
                res = idx;
                return false;
            }
            return true;
        });
        return res;
    }

};
