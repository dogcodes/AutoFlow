const DEFAULT_CHECKIN_CONFIG = {
  enabled: true,
  showBanner: true,
  title: "每日签到",
  description: "签到即可获得额外会员时长",
  buttonText: "立即签到",
  rewardMinutes: 10,
  cooldownMinutes: 1440
};

function resolveCacheTTL(env) {
  const DEFAULT_CACHE_TTL = 3600;
  const raw = env.CACHE_TTL;
  const parsed = Number(raw);
  if (!Number.isNaN(parsed) && parsed > 0) {
    return Math.floor(parsed);
  }
  return DEFAULT_CACHE_TTL;
}

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

function getConfigCacheKey(request) {
  const url = new URL(request.url);
  url.search = "";
  url.hash = "";
  return new Request(url.toString(), {
    method: "GET",
    headers: request.headers
  });
}

function buildConfigResponse(config, env) {
  const ttl = resolveCacheTTL(env);
  const headers = new Headers({
    "Content-Type": "application/json; charset=utf-8",
    "Cache-Control": `public, max-age=${ttl}`
  });
  return new Response(JSON.stringify(config), { headers });
}

async function cacheConfigResponse(request, env, config) {
  const cache = caches.default;
  const cacheKey = getConfigCacheKey(request);
  const response = buildConfigResponse(config, env);
  await cache.put(cacheKey, response.clone());
  return response;
}

async function respondWithCachedConfig(request, env) {
  const cache = caches.default;
  const cacheKey = getConfigCacheKey(request);
  const cached = await cache.match(cacheKey);
  if (cached) {
    return cached.clone();
  }
  const config = await loadConfig(env);
  return cacheConfigResponse(request, env, config);
}

export default {
  async fetch(request, env, ctx) {
    const url = new URL(request.url);

    if (url.pathname.startsWith("/checkin-config")) {
      if (request.method === "GET") {
        return respondWithCachedConfig(request, env);
      }

      if (request.method === "POST") {
        try {
          const body = await request.json();
          const merged = { ...DEFAULT_CHECKIN_CONFIG, ...body };
          await persistConfig(env, merged);
          return cacheConfigResponse(request, env, merged);
        } catch (error) {
          return new Response(JSON.stringify({ error: "Invalid payload" }), {
            status: 400,
            headers: { "Content-Type": "application/json; charset=utf-8" },
          });
        }
      }

      return new Response("Method not allowed", { status: 405 });
    }

    if (url.pathname.startsWith("/hello")) {
      return new Response("Hello World! 成功访问 Worker ✅", {
        headers: { "Content-Type": "text/html; charset=utf-8" },
      });
    }

    if (url.pathname.startsWith("/api")) {
      return new Response("我是Worker动态接口", { status: 200 });
    }

    return env.ASSETS.fetch(request);
  },
};
