export default {
  async fetch(request, env, ctx) {
    console.info({ message: 'Hello World Worker received a request!' });

    const url = new URL(request.url);

    // 1. /hello 接口
    if (url.pathname.startsWith('/hello')) {
      return new Response("Hello World! 成功访问 Worker ✅", {
        headers: { "Content-Type": "text/html; charset=utf-8" }
      });
    }

    // 2. /api 接口
    if (url.pathname.startsWith('/api')) {
      return new Response('我是Worker动态接口', { status: 200 });
    }

    // 3. 其他请求 → 走静态资源（public 文件夹下的文件）
    return env.ASSETS.fetch(request);
  }
};