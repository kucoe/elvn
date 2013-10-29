var cli = require('cline')();
var fs = require('fs');
var commands = require('./commands');
commands.init(cli);

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

var prompt = function (line) {
    if (line === '\\s') {
        timer.toggleSilent();
    } else if (line) {
        var res = cli.parse(line);
        if (res instanceof  commands.Command) {
            var current = res.run(display, config);
            var next = res.next;
            if(next){
                current = next.run(display, config);
            }
            if (current != null) {
                display.currentList = current;
            }
        }
    }
    cli.prompt('elvn>', prompt);
};

var sync = config.getSync(cli);
if (sync != null) {
    sync.pull(prompt);
}

prompt();

cli.on('close', function () {
    timer.cancel();
    if (sync != null) {
        sync.push();
    }
    fs.writeFileSync(historyPath, cli.history().join('\n'), 'utf-8');
    process.exit();
});