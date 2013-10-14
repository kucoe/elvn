var fs = require('fs');
var crypto = require('crypto');
var https = require('https');

var DEFAULT_HOST = "kucoe.net";

exports.Sync = function (email, noKey, host, basePath, config, cli) {
    this.email = email;
    this.noKey = noKey;
    this.serverPath = host ? DEFAULT_HOST : host;
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
            var remote = this.call("pull");
            if (!remote) {
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
            var remote = this.call("push", {config: JSON.stringify(this.config.getConfig())});
            if (!remote) {
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
            this.authorized = this.call("auth");
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
        return this.call("auth", {email: this.email}) === 'true';
    },

    askForPassword: function () {
        if (this.userExists()) {
            return this.cli.password("Please enter your password:");
        }
        return this.cli.password("Please choose password to register with:");
    },

    sendKey: function (password, key) {
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
        var opts = {
            hostname: this.serverPath,
            path: '/elvn/' + resource,
            method: 'POST',
            headers: {}
        };
        if (opts.method == 'POST') {
            opts.headers['Content-Type'] = 'application/x-www-form-urlencoded';
        }
        var req = https.request(opts, function (res) {
            console.log('STATUS: ' + res.statusCode);
            console.log('HEADERS: ' + JSON.stringify(res.headers));
            res.setEncoding('utf8');
            res.on('data', function (chunk) {
                console.log('BODY: ' + chunk);
            });
        });

        req.on('error', function (e) {
            console.log('problem with request: ' + e.message);
        });

        params = this.params(params);
        if (params) {
            req.write(params);
        }
        req.end();
    },

    params: function (params) {
        if (!params) {
            return null;
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
        console.log("Synchronized sucessfully as " + this.email);
    },

    showSynchronizedFailedStatus: function (message) {
        console.log("Synchronization failed, cause:" + message);
    }
};
