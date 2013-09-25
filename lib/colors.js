exports.colors = ['a', 'all', 't', 'today', 'd', 'done', 'completed',
    'r', 'red', 'o', 'orange', 'y', 'yellow', 'g', 'green', 'b', 'blue', 'p', 'pink', 'f', 'violet'];

exports.restricted = ['a', 'all', 't', 'today', 'd', 'done', 'completed'];


exports.all = ['a', 'all'];
exports.today = ['t', 'today'];
exports.done = ['d', 'done'];
exports.bits = ['r', 'o', 'y', 'g', 'b', 'p', 'f'];

exports.is = function(colors, color) {
    return colors.indexOf(color.toLowerCase()) != -1;
};
