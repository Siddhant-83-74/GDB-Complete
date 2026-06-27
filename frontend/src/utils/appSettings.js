/**
 * App settings persistence + application.
 *
 * Settings are stored in localStorage (per browser) and applied to the document
 * so that visible preferences (theme, compact mode, language) take effect
 * immediately and survive page reloads.
 */

const STORAGE_KEY = 'gdb_app_settings';

export const DEFAULT_SETTINGS = {
  general: {
    language: 'en',
    timezone: 'Asia/Kolkata',
    dateFormat: 'DD/MM/YYYY',
    currency: 'INR',
  },
  notifications: {
    emailNotifications: true,
    transactionAlerts: true,
    loginAlerts: true,
    marketingEmails: false,
    weeklyReports: true,
    smsAlerts: false,
  },
  security: {
    twoFactorEnabled: false,
    sessionTimeout: '30',
    ipRestriction: false,
  },
  appearance: {
    theme: 'light',
    sidebarCollapsed: false,
    compactMode: false,
  },
};

export const loadSettings = () => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return DEFAULT_SETTINGS;
    const parsed = JSON.parse(raw);
    // Merge with defaults so newly added keys are always present
    return {
      general: { ...DEFAULT_SETTINGS.general, ...parsed.general },
      notifications: { ...DEFAULT_SETTINGS.notifications, ...parsed.notifications },
      security: { ...DEFAULT_SETTINGS.security, ...parsed.security },
      appearance: { ...DEFAULT_SETTINGS.appearance, ...parsed.appearance },
    };
  } catch {
    return DEFAULT_SETTINGS;
  }
};

export const saveSettings = (settings) => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(settings));
};

/**
 * Apply visible settings to the document (theme, compact mode, language).
 */
export const applySettings = (settings) => {
  if (!settings) return;
  const root = document.documentElement;

  // Theme
  const theme = settings.appearance?.theme || 'light';
  const prefersDark = window.matchMedia?.('(prefers-color-scheme: dark)').matches;
  const dark = theme === 'dark' || (theme === 'system' && prefersDark);
  root.classList.toggle('dark', dark);

  // Compact mode
  root.classList.toggle('compact', !!settings.appearance?.compactMode);

  // Language attribute
  if (settings.general?.language) {
    root.lang = settings.general.language;
  }
};

/** Load + apply on app startup. */
export const initSettings = () => {
  const settings = loadSettings();
  applySettings(settings);
  return settings;
};
