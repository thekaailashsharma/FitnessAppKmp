"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.getLocalHour = getLocalHour;
exports.getLocalDayOfWeek = getLocalDayOfWeek;
exports.jsToAppDay = jsToAppDay;
/**
 * Get the current local hour for a given IANA timezone.
 * Returns -1 if the timezone is invalid.
 */
function getLocalHour(tz) {
    try {
        const now = new Date();
        const formatter = new Intl.DateTimeFormat("en-US", {
            timeZone: tz,
            hour: "numeric",
            hour12: false,
        });
        return parseInt(formatter.format(now), 10);
    }
    catch (_a) {
        return -1;
    }
}
/**
 * Get the local day of week (0 = Sunday, 1 = Monday, ... 6 = Saturday).
 */
function getLocalDayOfWeek(tz) {
    var _a;
    try {
        const now = new Date();
        const formatter = new Intl.DateTimeFormat("en-US", {
            timeZone: tz,
            weekday: "short",
        });
        const day = formatter.format(now);
        const map = {
            Sun: 0, Mon: 1, Tue: 2, Wed: 3, Thu: 4, Fri: 5, Sat: 6,
        };
        return (_a = map[day]) !== null && _a !== void 0 ? _a : -1;
    }
    catch (_b) {
        return -1;
    }
}
/**
 * Convert JS day (0=Sun) to the app's dayOfWeek (1=Mon...7=Sun).
 */
function jsToAppDay(jsDay) {
    return jsDay === 0 ? 7 : jsDay;
}
//# sourceMappingURL=timezone.js.map