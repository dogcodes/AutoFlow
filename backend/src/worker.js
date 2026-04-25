import { cacheJsonResponse, respondWithCachedJson } from "./cache.js";
import { getConfig, updateConfig } from "./checkinConfig.js";
import { getVersionConfig } from "./versionConfig.js";

export default {
  async fetch(request, env, ctx) {
    const url = new URL(request.url);

    if (url.pathname === "/time") {
      return new Response(JSON.stringify({ t: Date.now() }), {
        headers: { "Content-Type": "application/json; charset=utf-8" },
      });
    }

    if (url.pathname === "/version") {
      if (request.method === "GET") {
        return respondWithCachedJson(request, env, () => getVersionConfig(env));
      }

      return new Response("Method not allowed", { status: 405 });
    }

    if (url.pathname.startsWith("/checkin-config")) {
      if (request.method === "GET") {
        return respondWithCachedJson(request, env, () => getConfig(env));
      }

      if (request.method === "POST") {
        try {
          const body = await request.json();
          const merged = await updateConfig(env, body);
          return cacheJsonResponse(request, env, merged);
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
