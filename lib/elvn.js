var cli = require('cline')();
var fs = require('fs');
var commands = require('./commands');

var c = require('./config');
var d = require('./display');

var config = new c.Config();

var historyPath = config.getHistoryPath();
if (fs.existsSync(historyPath)) {
    cli.history(fs.readFileSync(historyPath, 'utf-8').split('\n'));
}

var display = new d.Display();
var timer = config.getTimer();

var start = new commands.SwitchList('t');
var current = start.run(display, config);
display.currentList = current;

commands.init(cli, display, config);

var sync = config.getSync(cli);
if (sync != null) {
    sync.pull();
}

cli.interact('elvn>');

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