var fs = require('fs');
var crypto = require('crypto');
var url = require('url');

var DEFAULT_SERVER_PATH = "https://kucoe.net/elvn/";

exports.Sync = function (email, noKey, serverPath, basePath, config, cli) {
    this.email = email;
    this.noKey = noKey;
    this.serverPath = serverPath ? serverPath : DEFAULT_SERVER_PATH;
    this.basePath = basePath;
    this.config = config;
    this.cli = cli;
    this.authorized = false;
};

exports.Sync.prototype = {
    restore: function () {
        var path = this.basePath + "config.bak";
        if (fs.existsSync(path)) {
            var o = JSON.parse(fs.readFileSync(path, 'utf-8'));
            this.config.commit(o);
        }
    },

    backup: function () {
        var path = this.basePath + "config.bak";
        fs.writeFileSync(path, JSON.stringify(this.config.getConfig()), 'utf-8');
    },

    pull: function () {
        this.tries = 0;
        this.backup();
        this.auth();
        try {
            console.log('Pull');
            var remote = this.call("pull");
            if (remote === '') {
                this.push();
            } else if (remote === 'false') {
                this.showAuthFailedStatus();
                this.reauth();
            } else {
                this.config.commit(JSON.parse(remote));
                this.showSynchronizedStatus();
            }
        } catch (e) {
            console.error(e.stack);
            this.showSynchronizedFailedStatus(e.message);
        }
    },

    push: function () {
        this.tries = 0;
        this.auth();
        try {
            console.log('Push');
            var remote = this.call("push", {config: JSON.stringify(this.config.getConfig())});
            if (remote === 'false') {
                this.showAuthFailedStatus();
                this.reauth();
            } else {
                this.showSynchronizedStatus();
            }
        } catch (e) {
            console.error(e.stack);
            this.showSynchronizedFailedStatus(e.message);
        }
    },

    auth: function () {
        if (!this.authorized) {
            console.log('Auth');
            this.authorized = this.call("auth") === 'true';
            if (!this.authorized) {
                this.showAuthFailedStatus();
                this.reauth();
            } else {
                this.tries = 0;
            }
        }
    },

    reauth: function () {
        this.tries++;
        this.authorized = false;
        var path = this.basePath + "sync.key";
        if (fs.existsSync(path)) {
            fs.unlinkSync(path);
        }
        if (this.tries <= 10) {
            this.auth();
        }
    },

    userExists: function () {
        console.log('Check');
        return this.call("auth", {email: this.email}) === 'true';
    },

    askForPassword: function () {
        if (this.userExists()) {
            return this.cli.password("Please enter your password:");
        }
        return this.cli.password("Please choose password to register with:");
    },

    sendKey: function (password, key) {
        console.log('Send key');
        var remote = this.call("auth", {email: this.email, password: password, key: key});
        if (remote === "false") {
            this.showAuthFailedStatus();
            this.reauth();
        }
    },

    key: function () {
        var path = this.basePath + "sync.key";
        if (!fs.existsSync(path)) {
            var password = this.askForPassword();
            var key = this.newKey(password);
            fs.writeFileSync(path, key, 'utf-8');
            key = this.getKey(key, this.getCreationTime(path));
            this.sendKey(password, key);
            return key;
        }
        var content = fs.readFileSync(path, 'utf-8');
        return this.getKey(content, this.getCreationTime(path));
    },

    getKey: function (key, time) {
        var str = key.substring(0, 64) + time + key.substring(64);
        return  crypto.createHash('sha512').update(str).digest("hex");
    },

    newKey: function (password) {
        return  crypto.createHash('sha512').update(password).digest("hex");
    },

    getCreationTime: function (path) {
        return fs.statSync(path).ctime;
    },

    call: function (resource, params) {
        var path = this.serverPath + resource;
        var opts = url.parse(path);
        opts.method = 'POST';
        opts.headers = {};
        if (opts.method == 'POST') {
            opts.headers['Content-Type'] = 'application/x-www-form-urlencoded';
        }
        var connect = require(path.substr(0, 8) == 'https://' ? 'https' : 'http');
        var req = connect.request(opts, function (res) {
            console.log('STATUS: ' + res.statusCode);
            console.log('HEADERS: ' + JSON.stringify(res.headers));
            res.setEncoding('utf8');
            res.on('data', function (chunk) {
                console.log('BODY: ' + chunk);
            });
        });

        req.on('error', function (e) {
            switch (e.code) {
                case 'ECONNREFUSED':
                    console.error('Connection refused from address: ' + path);
                    break;
                case 'ECONNRESET':
                    console.error('Connection reset for address: ' + path);
                    break;
                case 'ENOTFOUND':
                    console.error('Could not reach server by address: ' + path);
                    break;
                default:
                    console.error('Connection problem for adsress: ' + path + ' - ' + e.message);
            }
        });

        console.log('Before ' + JSON.stringify(params));
        params = this.params(params);
        console.log('Path ' +path);
        console.log('After ' + params);
        if (params) {
            req.write(params);
        }
        req.end();
    },

    params: function (params) {
        if (!params) {
            return this.authParams();
        }
        var size = params.length;
        if (size <= 0) {
            return this.authParams();
        }
        var built = this.buildParams(params);
        if (params['key'] || params['email']) {
            return built;
        }
        return this.authParams() + '&' + built;
    },

    authParams: function () {
        if (this.noKey) {
            return this.buildParams({email: this.email, password: this.askForPassword()});
        }
        return this.buildParams({email: this.email, key: this.key()});
    },

    buildParams: function (params) {
        var s = '';
        var i = 0;
        for (var key in params) {
            if (params.hasOwnProperty(key)) {
                if (i > 0) {
                    s += '&';
                }
                s += key;
                var p = params[key];
                if (p) {
                    s += '=';
                    s += p;
                }
                i++;
            }
        }
        return s;
    },

    showAuthFailedStatus: function () {
        console.log("Authorization failed for " + this.email);
    },

    showSynchronizedStatus: function () {
        console.log("Synchronized successfully as " + this.email);
    },

    showSynchronizedFailedStatus: function (message) {
        console.log("Synchronization failed, cause:" + message);
    }
};
