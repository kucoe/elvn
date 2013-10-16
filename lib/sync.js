var fs = require('fs');
var qs = require('querystring');
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

    pull: function (cb) {
        this.tries = 0;
        this.backup();
        this.auth(function () {
            try {
                this.req("pull", function (remote) {
                    if (!remote) {
                        this.push(cb);
                    } else if (remote === 'false') {
                        this.showAuthFailedStatus();
                    } else {
                        this.config.commit(JSON.parse(remote));
                        this.showSynchronizedStatus();
                        cb && cb.call(this);
                    }
                });
            } catch (e) {
                this.showSynchronizedFailedStatus(e.message);
            }
        });
    },

    push: function (cb) {
        this.tries = 0;
        this.auth();
        try {
            this.req("push", {config: JSON.stringify(this.config.getConfig())}, function(remote){
                if (remote === 'false') {
                    this.showAuthFailedStatus();
                } else {
                    this.showSynchronizedStatus();
                    cb && cb.call(this);
                }
            });
        } catch (e) {
            this.showSynchronizedFailedStatus(e.message);
        }
    },

    auth: function (cb) {
        if (!this.authorized) {
            this.req("auth", function (remote) {
                this.authorized = remote === 'true';
                if (!this.authorized) {
                    this.showAuthFailedStatus();
                    this.reauth(cb);
                } else {
                    this.tries = 0;
                    cb && cb.call(this)
                }
            });
        }
    },

    reauth: function (cb) {
        this.tries++;
        this.authorized = false;
        var path = this.basePath + "sync.key";
        if (fs.existsSync(path)) {
            fs.unlinkSync(path);
        }
        if (this.tries <= 10) {
            this.auth(cb);
        }
    },

    userExists: function (cb) {
        this.req("auth", {email: this.email}, function (data) {
            cb.call(this, data === 'true');
        });
    },

    askForPassword: function (cb) {
        var self = this;
        var passCb = function (pass) {
            cb && cb.call(self, pass);
        };
        this.userExists(function (exists) {
            if (exists) {
                this.cli.password("Please enter your password:", passCb);
            } else {
                this.cli.password("Please choose password to register with:", passCb);
            }
        });
    },

    sendKey: function (password, key, cb) {
        this.req("auth", {email: this.email, password: password, key: key}, function (remote) {
            if (remote === "false") {
                this.showAuthFailedStatus();
                this.reauth(cb);
            } else {
                cb && cb.call(this, key);
            }
        });
    },

    key: function (cb) {
        var path = this.basePath + "sync.key";
        if (!fs.existsSync(path)) {
            this.askForPassword(function (password) {
                var key = this.newKey(password);
                fs.writeFileSync(path, key, 'utf-8');
                key = this.getKey(key, this.getCreationTime(path));
                this.sendKey(password, key, cb);
            });
        } else {
            var content = fs.readFileSync(path, 'utf-8');
            cb && cb.call(this, this.getKey(content, this.getCreationTime(path)));
        }
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

    req: function (resource, params, cb, method) {
        var self = this;
        if (typeof params === 'function') {
            cb = params;
            params = null;
        }
        var path = this.serverPath + resource;
        var opts = url.parse(path);
        opts.method = method || 'POST';
        opts.headers = {"User-Agent": "Mozilla/4.0 (compatible; MSIE 5.0; Windows 98; DigExt)"};
        if (opts.method == 'POST') {
            opts.headers['Content-Type'] = 'application/x-www-form-urlencoded';
        }

        this.params(params, function (built) {
            opts.headers['Content-Length'] = built ? built.length : 0;

            var connect = require(path.substr(0, 8) == 'https://' ? 'https' : 'http');
            var req = connect.request(opts);

            if (built) {
                req.write(built);
            }

            req.on('response', function (res) {
                res.setEncoding('utf8');
                res.body = '';
                res.on('data', function (chunk) {
                    res.body += chunk;
                });
                res.on('end', function () {
                    cb && cb.call(self, res.body);
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


            req.end();
        });
    },

    params: function (params, cb) {
        if (!params) {
            this.authParams(cb);
        } else {
            var built = qs.stringify(params);
            if (params['key'] || params['email']) {
                cb && cb.call(this, built);
            } else {
                this.authParams(function (auth) {
                    cb && cb.call(this, auth + '&' + built);
                });
            }
        }
    },

    authParams: function (cb) {
        if (this.noKey) {
            this.askForPassword(function (pass) {
                var built = qs.stringify({email: this.email, password: pass});
                cb && cb.call(this, built);
            });
        } else {
            this.key(function (key) {
                var built = qs.stringify({email: this.email, key: key});
                cb && cb.call(this, built);
            });
        }
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
