/**
 * Working time interval
 */
exports.WORK_TIME = 15;

/**
 * Break time interval
 */
exports.BREAK_TIME = 2;

/**
 * Long break time interval
 */
exports.LONG_BREAK_TIME = 11;

var ELVN = 'elvn';
var BREAK = 'break';
var WORK = 'work';
var WORK2 = 'work2';
var WORK3 = 'work3';

var growl = require('growl');

var formatSecs = function (seconds) {
    var mins = (seconds / 60);
    var secs = seconds % 60;
    var divider = secs < 10 ? ":0" : ":";
    return "" + mins + divider + secs;
};

exports.Timer = function () {
    this.running = false;
    this.silent = false;
    this.paused = false;
    this.timeSeconds = 0;
};

exports.Timer.prototype = {

    init: function (timeout, onTime) {
        this.timeSeconds = timeout;
        var self = this;
        var interval = function() {
            if(self.timeSeconds > 0) {
                if (!self.paused) {
                    self.timeSeconds--;
                    self.update(self.timeSeconds);
                }
            }
            if (self.timeSeconds == 0 || self.timeSeconds == -1) {
                var completed = self.timeSeconds < 0;
                self.playOnTime();
                self.timeSeconds = 0;
                onTime(completed);
                clearInterval(self.intervalId);
            }
        };
        this.intervalId  = setInterval(interval, 1000);
    },

    runElvn: function (task, onTime) {
        var timer = this;
        this.run(exports.LONG_BREAK_TIME, task, ELVN, function (completed) {
            if (completed) {
                onTime(completed);
            } else {
                iterateTillDone(timer, task, onTime, 1);
            }
        });
    },

    run: function (mins, task, stage, onTime) {
        this.paused = false;
        if (this.running) {
            this.show();
            return;
        }
        var timeSeconds = mins * 60;
        var self = this;
        this.show(task, stage, timeSeconds);
        this.init(timeSeconds, function (completed) {
            self.running = false;
            onTime(completed);
        });
        this.running = true;
    },

    resume: function () {
        if (!this.running) {
            return;
        }
        this.paused = false;
    },

    pause: function () {
        if (!this.running) {
            return;
        }
        this.paused = true;
    },

    cancel: function () {
        if (!this.running) {
            return;
        }
        this.clear();
        this.timeSeconds = -2;
        this.running = false;
    },

    fire: function (complete) {
        if (!this.running) {
            return;
        }
        if (complete) {
            this.timeSeconds = -1;
        } else {
            this.timeSeconds = 0;
        }
    },

    toggleSilent: function () {
        this.silent = !this.silent;
        console.write("Silent mode " + (this.silent ? "activated" : "deactivated"));
    },

    show: function (task, stage, seconds) {
        if (task) {
            this.lastTask = task;
        }
        if (stage) {
            this.lastStage = stage;
        }
        if (seconds && seconds > 0) {
            this.lastSeconds = seconds;
        }
        if (!this.lastTask) {
            console.log("No running tasks");
        } else {
            console.log(this.lastStage + ":" + this.lastTask.text + " - " + formatSecs(this.lastSeconds));
        }
    },

    update: function (seconds) {
        this.lastSeconds = seconds;
        if (!this.silent && this.lastSeconds % 60 == 0) {
            this.show();
        }
    },

    playOnTime: function () {
        if (!this.lastTask) {
            return;
        }
        growl('The stage ' + this.lastStage + ' finished', {title: 'Elvn', name: 'elvn'});
        this.clear();
    },

    clear: function() {
        this.lastSeconds = 0;
        this.lastStage = null;
        this.lastTask = null;
        this.paused = false;
    }

};

var iterateTillDone = function (timer, task, onTime, cycle) {
    if (cycle == 3) {
        timer.run(exports.WORK_TIME, task, WORK3, onTime);
    } else {
        timer.run(exports.WORK_TIME, task, cycle == 2 ? WORK2 : WORK, function (completed) {
            if (completed) {
                onTime(completed);
            } else {
                timer.run(exports.BREAK_TIME, task, BREAK, function (completed) {
                    if (completed) {
                        onTime(completed);
                    } else {
                        iterateTillDone(timer, task, onTime, cycle + 1);
                    }
                });
            }
        });
    }
};