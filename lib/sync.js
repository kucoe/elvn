var fs = require('fs');
var qs = require('querystring');
var crypto = require('crypto');
var url = require('url');
var tire = require('tire');
var util = require('util');

var DEFAULT_SERVER_PATH = "https://kucoe.net/e2/";

exports.Sync = function (email, noKey, serverPath, basePath, config, cli) {
    this.email = email;
    this.noKey = noKey;
    this.serverPath = serverPath ? serverPath : DEFAULT_SERVER_PATH;
    this.basePath = basePath;
    this.config = config;
    this.cli = cli;
    this.key = null;
    this.authorized = false;
};

var AuthFailedError = function () {
    Error.call(this, 'Not authorized');
};

util.inherits(AuthFailedError, Error);


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
        this.backup();
        tire(this.auth, this, this.errFn(cb))
        (function (token, next) {
            this.req('configs/' + token, next);
        })
        (function (remote) {
            if (remote.error) {
                cb(this.reqError(remote.error));
            } else if (!remote.value) {
                this.push(cb);
            } else {
                console.log(remote.value);
                this.config.commit(JSON.parse(remote.value));
                this.showSynchronizedStatus();
                cb();
            }
        });
    },

    push: function (cb) {
        tire(this.auth, this, this.errFn(cb))
        (function (token, next) {
            this.req("configs/" + token, {config: JSON.stringify(this.config.getConfig())}, next);
        })(function (remote) {
            if (remote.error) {
                cb(this.reqError(remote.error));
            } else {
                this.showSynchronizedStatus();
                cb();
            }
        });
    },

    auth: function (cb) {
        tire(function (next) {
            this.req("users/0/" + this.email, next);
        }, this, cb)(function (remote, next) {
            if (remote.value === 0) {
                this.register(next);
            } else {
                this.getKey(next);
            }
        })(function (key, next) {
            this.req("auth", {email: this.email, key: key}, next);
        })(function (remote, next) {
            if (remote.error) {
                cb(this.reqError(remote.error));
            } else {
                this.authorized = true;
                cb(remote.value);
            }
        });
    },

    register: function (cb) {
        tire(function (next) {
            this.askForPassword(false, next);
        }, this, cb)(function (password, next) {
            var key = this.hash(password);
            if (!this.noKey) {
                var path = this.basePath + "sync.key";
                this.saveKey(path, key);
            }
            this.key = key;
            this.req('users', {email: this.email, key: key}, cb);
        });
    },

    askForPassword: function (exists, cb) {
        if (exists) {
            this.cli.password("Please enter your password:", cb);
        } else {
            this.cli.password("Please choose password to register with:", cb);
        }
    },

    getKey: function (cb) {
        if (!this.key) {
            var path = this.basePath + "sync.key";
            tire(function (next) {
                fs.exists(path, next);
            }, this, cb)(function (exists, next) {
                if (exists) {
                    this.existedKey(path, next);
                } else {
                    this.newKey(path, next);
                }
            })(function (key) {
                this.key = key;
                cb.call(this, key);
            });
        } else {
            cb(this.key);
        }
    },

    newKey: function (path, cb) {
        var key = null;
        tire(function (next) {
            this.askForPassword(true, next);
        }, this, cb)(function (password) {
            key = this.hash(password);
            if (!this.noKey) {
                this.saveKey(path, key);
            }
            cb.call(this, key);
        });
    },

    saveKey: function (path, key) {
        tire(function (next) {
            fs.writeFile(path, key, 'utf-8', next);
        }, this)(function (err, next) {
            this.creationTime(path, next);
        })(function (time) {
            fs.writeFile(path, this.encrypt(key, time), 'utf-8');
        });
    },

    existedKey: function (path, cb) {
        var key = null;
        tire(function (next) {
            fs.readFile(path, 'utf-8', next);
        }, this, cb)(function (err, data, next) {
            key = data;
            this.creationTime(path, next);
        })(function (err, time) {
            cb.call(this, this.decrypt(key, time));
        });
    },

    hash: function (password) {
        return crypto.createHash('sha512').update(password).digest("hex");
    },

    encrypt: function (key, time) {
        var encrypt = crypto.createCipher('aes-256-cbc', '' + time);
        var a = encrypt.update(key, 'hex', 'base64');
        var b = encrypt.final('base64');
        return a + b;
    },

    decrypt: function (crypted, time) {
        var decrypt = crypto.createDecipher('aes-256-cbc', '' + time);
        var a = decrypt.update(crypted, 'base64', 'hex');
        var b = decrypt.final('hex');
        return a + b;
    },

    creationTime: function (path, cb) {
        fs.stat(path, function (err, stat) {
            cb(err, stat.ctime);
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

        params = qs.stringify(params);
        opts.headers['Content-Length'] = params ? params.length : 0;

        var connect = require(path.substr(0, 8) == 'https://' ? 'https' : 'http');
        var req = connect.request(opts);

        if (params) {
            req.write(params);
        }

        req.on('response', function (res) {
            res.setEncoding('utf8');
            res.body = '';
            res.on('data', function (chunk) {
                res.body += chunk;
            });
            res.on('end', function () {
                cb.call(self, JSON.parse(res.body));
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
    },

    reqError: function (error) {
        if (error === 'authentication failed') {
            return new AuthFailedError();
        } else {
            return new Error(error);
        }
    },

    errFn: function (cb) {
        return function (err) {
            if (typeof err === 'string') {
                err = new Error(err);
            }
            if (err instanceof AuthFailedError) {
                this.showAuthFailedStatus();
            } else if (err) {
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
