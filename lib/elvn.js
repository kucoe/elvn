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

function init() {
    new commands.SwitchList('t').exec(display, config);
    cli.stream.emit('line', '\n')
}

commands.init(cli, display, config);
cli.interact('elvn>');

var sync = config.getSync(cli);
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