var colors = require('./colors');

exports.Display = function () {
    this.currentList = 't';
};

var simpleSort = function (a, b) {
    return a.id - b.id;
};

var taskSort = function (a, b) {
    var aPlanned = a.planned || false;
    var bPlanned = b.planned || false;
    return (aPlanned == bPlanned) ? simpleSort(a, b) : (aPlanned ? -1 : 1);
};

exports.Display.prototype = {

    showConfig: function (config) {
        this.show("\tConfig");
        this.show(config);
    },

    showLists: function (config) {
        this.show("\tLists");
        colors.bits.forEach(function (color) {
            var list = config.getList(color);
            if (list == null) {
                this.show("\t" + color + ":" + colors.NOT_ASSIGNED);
            } else {
                this.show("\t" + color + ":" + list.label);
            }

        }, this);
    },

    showTasks: function (tasks) {
        tasks.sort(taskSort);
        tasks.forEach(function (task, i) {
            var format = this.formatTask('all', task, i + 1);
            this.show(format);
        }, this);
    },

    showList: function (list) {
        this.show('\t' + list.label);
        var tasks = list.tasks;
        tasks.sort(taskSort);
        tasks.forEach(function (task, i) {
            var format = this.formatTask(list.color, task, i + 1);
            this.show(format);
        }, this);
    },

    showIdeas: function (ideas) {
        this.show("\tIdeas");
        ideas.sort(simpleSort);
        ideas.forEach(function (idea, i) {
            var format = this.formatIdea(idea, i + 1);
            this.show(format);
        }, this);
    },

    showIdea: function (idea, position) {
        this.show('#' + position + '=' + this.formatIdea(idea, 0));
    },

    showTask: function (task, position) {
        this.show('#' + position + '='
            + this.formatTask('all', task, 0));
    },

    formatTask: function (currentList, task, pos) {
        var res = '';
        if (pos > 0) {
            res += '\t' + pos + '.';
        }
        if (colors.is(colors.restricted, currentList)) {
            res += task.color + ':';
        }
        res += task.text;
        if (task.completedOn == null && task.planned && colors.is(colors.today, currentList)) {
            res += "-planned";
        }
        return res;
    },

    formatIdea: function (idea, pos) {
        var res = '';
        if (pos > 0) {
            res += '\t' + pos + '.';
        }
        res += idea.text;
        return res;
    },

    show: function (text) {
        console.log(text);
    }
};
