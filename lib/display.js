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
        if (items.is(items.restricted, currentList)) {
            res += plan + ':';
        }
        res += item.text;
        if (!items.is(items.done, plan) && items.is(items.today, plan) && !items.is(items.today, currentList)) {
            res += "-planned";
        }
        return res;
    },

    show: function (text) {
        console.log(text);
    }
};
