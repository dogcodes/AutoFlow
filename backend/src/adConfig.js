const DEFAULT_AD_CONFIG = {
  version: 1,
  timestamp: Date.now(),
  globalEnabled: true,
  requireOaid: false,
  hotStartupCooldownMs: 10 * 60 * 1000,
  rewardedPolicy: {
    rewardMinutes: 30,
    dailyLimit: 5,
    cooldownSeconds: 60,
  },
  slots: {
    splash: {
      enabled: true,
      cooldownMs: 10 * 1000,
      dailyLimit: 10,
    },
    rewarded: {
      enabled: true,
    },
  },
  platforms: [
    {
      name: "umeng",
      priority: 0,
      enabledTypes: ["splash", "rewarded", "interstitial", "banner", "floating", "floatingball", "feed"],
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
    rewardedPolicy: {
      ...DEFAULT_AD_CONFIG.rewardedPolicy,
      ...(current.rewardedPolicy || {}),
      ...(overrides.rewardedPolicy || {}),
    },
    slots: {
      ...DEFAULT_AD_CONFIG.slots,
      ...(current.slots || {}),
      ...(overrides.slots || {}),
    },
  };
  merged.timestamp = Date.now();
  await persistConfig(env, merged);
  return merged;
}

export { getConfig, updateConfig };
