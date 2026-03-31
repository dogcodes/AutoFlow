const DEFAULT_CHECKIN_CONFIG = {
  enabled: true,
  showBanner: true,
  title: "每日签到",
  description: "签到即可获得额外会员时长",
  buttonText: "立即签到",
  rewardMinutes: 10,
  cooldownMinutes: 1440
};

async function loadConfig(env) {
  const kv = env.CHECKIN_CONFIG;
  if (!kv) {
    return DEFAULT_CHECKIN_CONFIG;
  }
  try {
    const stored = await kv.get("config", { type: "json" });
    if (stored) {
      return stored;
    }
    await persistConfig(env, DEFAULT_CHECKIN_CONFIG);
    return DEFAULT_CHECKIN_CONFIG;
  } catch (error) {
    console.error("failed to read check-in config", error);
    return DEFAULT_CHECKIN_CONFIG;
  }
}

async function persistConfig(env, config) {
  const kv = env.CHECKIN_CONFIG;
  if (!kv) return;
  await kv.put("config", JSON.stringify(config));
}

async function getConfig(env) {
  return loadConfig(env);
}

async function updateConfig(env, overrides) {
  const merged = { ...DEFAULT_CHECKIN_CONFIG, ...overrides };
  await persistConfig(env, merged);
  return merged;
}

export { getConfig, updateConfig };
