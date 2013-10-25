var cli = require('cline')();
var fs = require('fs');
require('./commands').init(cli);

var home = process.env.HOME || process.env.HOMEPATH || process.env.USERPROFILE;
cli.history(fs.readFileSync(home + '/.elvn/history', 'utf-8').split('\n'));
cli.interact('>');

cli.on('close', function () {
    fs.writeFileSync(home +'/.elvn/history', cli.history().join('\n'), 'utf-8');
    process.exit();
});