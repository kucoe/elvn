var items = require('./items');

exports.Display = function () {
    this.currentList = 't';
};

exports.Display.prototype = {

    showItems: function (items) {
        items.forEach(function (item, i) {
            var format = this.formatItem('all', item, i + 1);
            this.show(format);
        }, this);
    },

    showList: function (list) {
        this.show('\t' + list.label);
        var items = list.items;
        items.forEach(function (item, i) {
            var format = this.formatItem(list.plan, item, i + 1);
            this.show(format);
        }, this);
    },

    showItem: function (item, position) {
        this.show('#' + position + '='
            + this.formatItem('all', item, 0));
    },

    formatItem: function (currentList, item, pos) {
        var res = '';
        if (pos > 0) {
            res += '\t' + pos + '.';
        }
        var plan = item.plan;
        var sep = ':';
        if (items.is(items.today, currentList)) {
            plan = items.set(plan, items.today[0], false);
            plan = items.set(plan, items.today[1], false);
        } else if (items.is(items.all, currentList)) {
            plan = items.set(plan, items.all[0], false);
            plan = items.set(plan, items.all[1], false);
        } else if (items.is(items.done, currentList)) {
            plan = items.set(plan, items.done[0], false);
            plan = items.set(plan, items.done[1], false);
        } else if (items.is(items.journal, currentList)) {
            plan = items.set(plan, items.journal[0], false);
            plan = items.set(plan, items.journal[1], false);
            sep = '-';
        }
        plan = items.set(plan, currentList, false);
        if(plan.length > 0) {
            res += plan + sep;
        }
        res += item.text;
        return res;
    },

    show: function (text) {
        console.log(text);
    }
};
