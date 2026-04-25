const DEFAULT_VERSION_CONFIG = {
  versionCode: 1,
  versionName: "0.1.0",
  downloadUrl: "",
  releaseNotes: "修复若干问题，提升稳定性。",
  forceUpdate: false
};

function parseInteger(value, fallback) {
  const parsed = Number.parseInt(value, 10);
  return Number.isNaN(parsed) ? fallback : parsed;
}

function parseBoolean(value, fallback) {
  if (typeof value !== "string") return fallback;
  if (value === "true") return true;
  if (value === "false") return false;
  return fallback;
}

function getVersionConfig(env) {
  return {
    versionCode: parseInteger(env.VERSION_CODE, DEFAULT_VERSION_CONFIG.versionCode),
    versionName: env.VERSION_NAME || DEFAULT_VERSION_CONFIG.versionName,
    downloadUrl: env.VERSION_DOWNLOAD_URL || DEFAULT_VERSION_CONFIG.downloadUrl,
    releaseNotes: env.VERSION_RELEASE_NOTES || DEFAULT_VERSION_CONFIG.releaseNotes,
    forceUpdate: parseBoolean(env.VERSION_FORCE_UPDATE, DEFAULT_VERSION_CONFIG.forceUpdate)
  };
}

export { getVersionConfig };
