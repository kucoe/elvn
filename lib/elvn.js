var cli = require('cline')();
var fs = require('fs');
var commands = require('./commands');

var i = require('./items');
var d = require('./display');

var items = new i.Items();

var historyPath = items.getHistoryPath();
if (fs.existsSync(historyPath)) {
    cli.history(fs.readFileSync(historyPath, 'utf-8').split('\n'));
}

var display = new d.Display();
var timer = items.getTimer();

function init() {
    new commands.SwitchList(i.today[1]).exec(display, items);
    cli.stream.emit('line', '\n')
}

commands.init(cli, display, items);
cli.interact('elvn>');

var sync = items.getSync(cli);
if (sync != null) {
    sync.pull(init);
} else {
    init();
}

cli.on('close', function () {
    timer.cancel();
    fs.writeFileSync(historyPath, cli.history().join('\n'), 'utf-8');
    if (sync != null) {
        sync.push(function () {
            process.exit();
        });
    } else {
        process.exit();
    }
});