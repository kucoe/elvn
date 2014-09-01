var commands = require('./commands');

var i = require('./items');
var d = require('./display');

var args = process.argv;
var nonInteractive = args.length > 2 && args[2] == '-ni';
var initial = args.length > 3 ? args[3] : i.today[1];

var items = new i.Items();
var display = new d.Display();

if (nonInteractive) {
    new commands.SwitchList(initial).exec(display, items);
    process.exit(0);
} else {
    var fs = require('fs');
    var cli = require('cline')();
    var historyPath = items.getHistoryPath();
    if (fs.existsSync(historyPath)) {
        cli.history(fs.readFileSync(historyPath, 'utf-8').split('\n'));
    }

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
}