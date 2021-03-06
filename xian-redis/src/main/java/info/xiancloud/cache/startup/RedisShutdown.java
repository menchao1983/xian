package info.xiancloud.cache.startup;

import info.xiancloud.cache.redis.Redis;
import info.xiancloud.plugin.init.shutdown.ShutdownHook;

/**
 * Redis 资源回收
 *
 * @author John_zero
 */
public class RedisShutdown implements ShutdownHook {

    @Override
    public boolean shutdown() {
        Redis.destroy();
        return true;
    }

    @Override
    public float shutdownOrdinal() {
        return 1000;
    }

}
