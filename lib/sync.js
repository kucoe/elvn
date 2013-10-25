var fs = require('fs');
var qs = require('querystring');
var crypto = require('crypto');
var url = require('url');

var tire = require('./tire');

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

var AuthFailedError = function () {
    Error.call(this, 'Not authorized');
};

exports.Sync.prototype = {

    restore: function () {
        var path = this.basePath + "config.bak";
        tire(function (next) {
            fs.exists(path, next);
        }, this, this.errFn())(function (exists, next) {
            if (exists) {
                fs.readFile(path, 'utf-8', next);
            }
        })(function (err, data) {
            var o = JSON.parse(data);
            this.config.commit(o);
        });
    },

    backup: function () {
        var path = this.basePath + "config.bak";
        fs.writeFile(path, JSON.stringify(this.config.getConfig()), 'utf-8');
    },

    pull: function (cb) {
        this.tries = 0;
        this.backup();
        tire(this.auth, this, this.errFn(cb))(function (next) {
            this.req('pull', next);
        })(function (remote, next) {
            if (!remote) {
                this.push(next);
            } else if (remote === 'false') {
                next(new AuthFailedError());
            } else {
                this.config.commit(JSON.parse(remote));
                this.showSynchronizedStatus();
                next();
            }
        })(cb);
    },

    push: function (cb) {
        this.tries = 0;
        tire(this.auth, this, this.errFn(cb))(function (next) {
            this.req("push", {config: JSON.stringify(this.config.getConfig())}, next);
        })(function (remote, next) {
            if (remote === 'false') {
                next(new AuthFailedError());
            } else {
                this.showSynchronizedStatus();
                next();
            }
        })(cb);
    },

    auth: function (cb) {
        if (!this.authorized) {
            tire(function (next) {
                this.req("auth", next);
            }, this, cb)(function (remote, next) {
                this.authorized = remote === 'true';
                if (!this.authorized) {
                    this.reauth(next);
                } else {
                    this.tries = 0;
                    next();
                }
            })(cb);
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
        } else {
            var err = new AuthFailedError();
            cb.call(this, err);
        }
    },

    askForPassword: function (cb) {
        tire(function (next) {
            this.req("auth", {email: this.email}, next);
        }, this, cb)(function (data, next) {
            if (data === 'true') {
                this.cli.password("Please enter your password:", next);
            } else {
                this.cli.password("Please choose password to register with:", next);
            }
        })(cb);
    },

    sendKey: function (password, key, cb) {
        tire(function (next) {
            this.req("auth", {email: this.email, password: password, key: key}, next);
        }, this, cb)(function (data) {
            if (data === "false") {
                this.reauth(cb);
            } else {
                cb.call(this, key);
            }
        });
    },

    key: function (cb) {
        var path = this.basePath + "sync.key";
        tire(function (next) {
            fs.exists(path, next);
        }, this)(function (exists) {
            if (exists) {
                this.existedKey(path, cb);
            } else {
                this.newKey(path, cb);
            }
        });
    },

    newKey: function (path, cb) {
        var key = null;
        var pass = null;
        tire(function (next) {
            this.askForPassword(next);
        }, this, cb)(function (password, next) {
            pass = password;
            key = crypto.createHash('sha512').update(password).digest("hex");
            fs.writeFile(path, key, 'utf-8', next);
        })(function (err, data, next) {
            this.creationTime(path, next);
        })(function (time) {
            this.sendKey(pass, this.getKey(key, time), cb);
        });
    },

    existedKey: function (path, cb) {
        var key = null;
        tire(function (next) {
            fs.readFile(path, 'utf-8', next);
        }, this, cb)(function (err, data, next) {
            key = data;
            this.creationTime(path, next);
        })(function (time) {
            cb.call(this, this.getKey(key, time));
        });
    },

    params: function (params, cb) {
        if (!params) {
            this.authParams(cb);
        } else {
            var built = qs.stringify(params);
            if (params['key'] || params['email']) {
                cb.call(this, built);
            } else {
                this.authParams(function (auth) {
                    cb.call(this, auth + '&' + built);
                });
            }
        }
    },

    authParams: function (cb) {
        if (this.noKey) {
            this.askForPassword(function (pass) {
                var built = qs.stringify({email: this.email, password: pass});
                cb.call(this, built);
            });
        } else {
            this.key(function (key) {
                var built = qs.stringify({email: this.email, key: key});
                cb.call(this, built);
            });
        }
    },

    getKey: function (key, time) {
        var str = key.substring(0, 64) + time + key.substring(64);
        return  crypto.createHash('sha512').update(str).digest("hex");
    },

    creationTime: function (path, cb) {
        fs.stat(path, function (stat) {
            cb(stat.ctime);
        });
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
                    cb.call(self, res.body);
                });
            });

            req.on('error', function (e) {
                var s = e.message;
                switch (e.code) {
                    case 'ECONNREFUSED':
                        s = 'Connection refused from address: ' + path;
                        break;
                    case 'ECONNRESET':
                        s = 'Connection reset for address: ' + path;
                        break;
                    case 'ENOTFOUND':
                        s = 'Could not reach server by address: ' + path;
                        break;
                    default:
                        s = 'Connection problem for adsress: ' + path + ' - ' + s;
                }
                cb.call(self, new Error(s));
            });


            req.end();
        });
    },

    errFn: function (cb) {
        return function (err) {
            if (typeof err === 'string') {
                err = new Error(err);
            }
            if (err instanceof AuthFailedError) {
                this.showAuthFailedStatus();
            }
            if (err) {
                this.showSynchronizedFailedStatus(err.message);
            }
            cb && cb(err);
        };
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
