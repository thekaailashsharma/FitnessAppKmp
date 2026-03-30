/**
 * Get the current local hour for a given IANA timezone.
 * Returns -1 if the timezone is invalid.
 */
export function getLocalHour(tz: string): number {
  try {
    const now = new Date();
    const formatter = new Intl.DateTimeFormat("en-US", {
      timeZone: tz,
      hour: "numeric",
      hour12: false,
    });
    return parseInt(formatter.format(now), 10);
  } catch {
    return -1;
  }
}

/**
 * Get the local day of week (0 = Sunday, 1 = Monday, ... 6 = Saturday).
 */
export function getLocalDayOfWeek(tz: string): number {
  try {
    const now = new Date();
    const formatter = new Intl.DateTimeFormat("en-US", {
      timeZone: tz,
      weekday: "short",
    });
    const day = formatter.format(now);
    const map: Record<string, number> = {
      Sun: 0, Mon: 1, Tue: 2, Wed: 3, Thu: 4, Fri: 5, Sat: 6,
    };
    return map[day] ?? -1;
  } catch {
    return -1;
  }
}

/**
 * Convert JS day (0=Sun) to the app's dayOfWeek (1=Mon...7=Sun).
 */
export function jsToAppDay(jsDay: number): number {
  return jsDay === 0 ? 7 : jsDay;
}
