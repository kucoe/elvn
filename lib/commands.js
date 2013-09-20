var results = require('./results');

exports.init = function(cli) {
    cli.command('/{color}', 'show tasks list by color or label', {color: '\\w+'}, function(input, args){
        return new results.SwitchList(args.color);
    });
    cli.command('@', 'show ideas', function() {
        return new results.SwitchIdeas();
    });
    cli.command('&', 'show available lists', function(){
        return new results.SwitchListEdit();
    });
    cli.command('!', 'show status', function(){
        return new results.SwitchStatus();
    });
    cli.command('%{command}', 'sync config', {command : '-|<|>|'}, function(input, args){
        return new results.SwitchSync(args.command);
    });
    cli.command('${command}', 'show timer and do', {command : ':|>|^|v|x|'}, function(input, args){
        return new results.SwitchTimer(args.command);
    });
};
