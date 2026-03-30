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
    return stored ?? DEFAULT_CHECKIN_CONFIG;
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

export default {
  async fetch(request, env, ctx) {
    const url = new URL(request.url);

    if (url.pathname.startsWith("/checkin-config")) {
      if (request.method === "GET") {
        const config = await loadConfig(env);
        return new Response(JSON.stringify(config), {
          headers: { "Content-Type": "application/json; charset=utf-8" },
        });
      }

      if (request.method === "POST") {
        try {
          const body = await request.json();
          const merged = { ...DEFAULT_CHECKIN_CONFIG, ...body };
          await persistConfig(env, merged);
          return new Response(JSON.stringify(merged), {
            headers: { "Content-Type": "application/json; charset=utf-8" },
          });
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
