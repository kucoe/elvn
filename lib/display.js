var colors = require('./colors');

exports.Display = function () {
    this.currentList = 't';
};

exports.Display.prototype = {

    showHelp: function (helpMessage) {
        this.showBodyText(helpMessage);
    },

    showStatus: function (status) {
        this.showBodyText(status);
    },

    showConfig: function (config) {
        this.showHeader("Config");
        this.showBodyText(config);
    },

    showLists: function (config) {
        this.showHeader("Lists");
        colors.bits.forEach(function (color) {
            var list = config.getList(color);
            if (list == null) {
                this.showBodyText("\t" + color + ":not assigned");
            } else {
                this.showBodyText("\t" + color + ":" + list.label);
            }

        }, this);
    },

    showTasks: function (tasks) {
        Collections.sort(tasks);
        tasks.forEach(function (task, i) {
            var format = this.formatTask('all', task, i);
            console.write(format);
        }, this);
    },

    showList: function (list) {
        this.showHeader(list.label);
        var tasks = list.tasks;
        Collections.sort(tasks);
        tasks.forEach(function (task, i) {
            var format = this.formatTask(list.color, task, i);
            console.write(format);
        }, this);
    },

    showIdeas: function (ideas) {
        this.showHeader("Ideas");
        Collections.sort(ideas);
        ideas.forEach(function (idea, i) {
            var format = this.formatIdea(idea, i);
            console.write(format);
        }, this);
    },

    showIdea: function (idea, position) {
        this.showBodyText('#' + position + '=' + this.formatIdea(idea, 0));
    },

    showTask: function (task, position) {
        this.showBodyText('#' + position + '='
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

    showHeader: function (header) {
        console.write("\t" + header);
    },

    showBodyText: function (text) {
        console.write(text);
    }
};
