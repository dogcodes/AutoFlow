const DEFAULT_CACHE_TTL_SECONDS = 3600;

function getCacheKey(request) {
  const url = new URL(request.url);
  url.search = "";
  url.hash = "";
  return new Request(url.toString(), {
    method: "GET",
    headers: request.headers
  });
}

function resolveCacheTTL(env) {
  const raw = env.CACHE_TTL;
  const parsed = Number(raw);
  if (!Number.isNaN(parsed) && parsed > 0) {
    return Math.floor(parsed);
  }
  return DEFAULT_CACHE_TTL_SECONDS;
}

function buildCacheHeaders(env) {
  const ttl = resolveCacheTTL(env);
  return new Headers({
    "Content-Type": "application/json; charset=utf-8",
    "Cache-Control": `public, max-age=${ttl}`
  });
}

function buildCachedJsonResponse(payload, env, status = 200) {
  return new Response(JSON.stringify(payload), {
    status,
    headers: buildCacheHeaders(env)
  });
}

async function cacheJsonResponse(request, env, payload, status = 200) {
  const cache = caches.default;
  const response = buildCachedJsonResponse(payload, env, status);
  await cache.put(getCacheKey(request), response.clone());
  return response;
}

async function respondWithCachedJson(request, env, loader) {
  const cache = caches.default;
  const cacheKey = getCacheKey(request);
  const cached = await cache.match(cacheKey);
  if (cached) {
    return cached.clone();
  }
  const payload = await loader();
  return cacheJsonResponse(request, env, payload);
}

export { cacheJsonResponse, respondWithCachedJson };
