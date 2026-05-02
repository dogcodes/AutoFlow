const DEFAULT_AD_CONFIG = {
  version: 1,
  timestamp: Date.now(),
  global: {
    enabled: true,
    requireOaid: true,
    hotStartupCooldownMs: 10 * 60 * 1000,
  },
  rewardedPolicy: {
    rewardMinutes: 30,
    dailyLimit: 5,
    cooldownSeconds: 60,
  },
  placements: {
    splash: {
      enabled: true,
      slotId: "100007398",
      cooldownMs: 86400000,
      dailyLimit: 10,
    },
    rewarded: {
      enabled: true,
      slotId: "100007403",
      cooldownMs: 0,
      dailyLimit: 0,
    },
    interstitial: {
      enabled: true,
      slotId: "100007402",
      cooldownMs: 300000,
      dailyLimit: 0,
    },
    floating: {
      enabled: true,
      slotId: "100007397",
      cooldownMs: 0,
      dailyLimit: 0,
    },
    floatingBall: {
      enabled: false,
      slotId: "100007405",
      cooldownMs: 0,
      dailyLimit: 0,
    },
    banner: {
      enabled: true,
      slotId: "100007404",
      cooldownMs: 0,
      dailyLimit: 0,
    },
    feed: {
      enabled: true,
      slotId: "100007396",
      cooldownMs: 0,
      dailyLimit: 0,
    },
  },
  platforms: [
    {
      name: "umeng",
      enabled: true,
      priority: 0,
      enabledTypes: ["splash", "rewarded", "interstitial", "floating", "floatingBall", "banner", "feed"],
    },
  ],
};

async function loadConfig(env) {
  const kv = env.AD_CONFIG;
  if (!kv) {
    return DEFAULT_AD_CONFIG;
  }
  try {
    const stored = await kv.get("config", { type: "json" });
    if (stored) {
      return stored;
    }
    await persistConfig(env, DEFAULT_AD_CONFIG);
    return DEFAULT_AD_CONFIG;
  } catch (error) {
    console.error("failed to read ad config", error);
    return DEFAULT_AD_CONFIG;
  }
}

async function persistConfig(env, config) {
  const kv = env.AD_CONFIG;
  if (!kv) return;
  await kv.put("config", JSON.stringify(config));
}

async function getConfig(env) {
  return loadConfig(env);
}

async function updateConfig(env, overrides) {
  const current = await loadConfig(env);
  const merged = {
    ...DEFAULT_AD_CONFIG,
    ...current,
    ...overrides,
    global: {
      ...DEFAULT_AD_CONFIG.global,
      ...(current.global || {}),
      ...(overrides.global || {}),
    },
    rewardedPolicy: {
      ...DEFAULT_AD_CONFIG.rewardedPolicy,
      ...(current.rewardedPolicy || {}),
      ...(overrides.rewardedPolicy || {}),
    },
    placements: {
      ...DEFAULT_AD_CONFIG.placements,
      ...(current.placements || {}),
      ...(overrides.placements || {}),
    },
  };
  merged.timestamp = Date.now();
  await persistConfig(env, merged);
  return merged;
}

export { getConfig, updateConfig };
