package com.mdc.mim;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheMode;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZkTest {
    CuratorFramework client;

    @BeforeEach
    public void initalClient() {
        var retryPolicy = new ExponentialBackoffRetry(1000, 3); // 定义重试策略
        var client = CuratorFrameworkFactory.newClient("127.0.0.1:2182", retryPolicy);
        System.out.println(client);
        this.client = client;
    }

    @Test
    public void testSimpleClientBuild() {
        // 测试简单的client构造方法
        var retryPolicy = new ExponentialBackoffRetry(1000, 3); // 定义重试策略
        var client = CuratorFrameworkFactory.newClient("127.0.0.1:2182", retryPolicy);
        System.out.println(client);
    }

    @Test
    public void testComplexClientBuild() {
        var client = CuratorFrameworkFactory.builder().connectString("127.0.0.1:2182")
                .retryPolicy(new ExponentialBackoffRetry(1000, 3)).connectionTimeoutMs(1000).sessionTimeoutMs(200000)
                .build();
        System.out.println(client);
    }

    @Test
    public void testCreatePersistentNode() throws Exception {
        String nodePath = "/test/shuangshu";
        // 节点写入数据
        byte[] payload = "shuangshu".getBytes();
        client.start();
        client.create()
                .creatingParentsIfNeeded() // 如果需要创建父节点
                .withMode(CreateMode.PERSISTENT) // 创建持久化节点
                .forPath(nodePath, payload);
    }

    @Test
    public void testCreatePersistentSequantialNode() throws Exception {
        String nodePath = "/test/shuangshu/go_";
        var payload = "test".getBytes();
        client.start();
        for (int i = 0; i < 5; i++) {
            client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                    .forPath(nodePath, payload);
        }
    }

    @Test
    public void testReadZNode() throws Exception {
        String nodePath = "/test/shuangshu";
        client.start();
        var stat = client.checkExists().forPath(nodePath); // 检查是否存在，并得到Stat对象
        if (stat != null) {
            var payload = client.getData().forPath(nodePath);
            var data = new String(payload);
            System.out.println("read data: " + data);
            String parentPath = "/test/shuangshu";
            var children = client.getChildren().forPath(parentPath);
            for (var child : children) {
                System.out.println(child);
            }
        }
    }

    @Test
    public void testSynchronizedUpdate() throws Exception {
        client.start();
        var data = "shuangshu_new".getBytes();
        var nodePath = "/test/shuangshu";
        client.setData().forPath(nodePath, data);
        // 测试读取新数据
        var newData = new String(client.getData().forPath(nodePath));
        System.out.println("new data: " + newData);
    }

    @Test
    public void testAsynchronizedUpdate() throws Exception {
        client.start();
        // 定义回调对象
        var callback = new AsyncCallback.StringCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, String name) {
                System.out.println(
                        "rc=" + rc +
                                "path=" + path +
                                "ctx=" + ctx +
                                "name=" + name);
            }
        };
        var nodePath = "/test/shuangshu";
        var data = "shuangshu_new_new".getBytes();
        client.setData().inBackground(callback).forPath(nodePath, data);
        Thread.sleep(10000);
    }

    @Test
    public void testDelete() throws Exception {
        client.start();
        var nodePath = "/test/shuangshu";
        client.delete().deletingChildrenIfNeeded().forPath(nodePath);
    }

    @Test
    public void testWatcher() throws Exception {
        // 1 定义监听器
        var w = new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("监听到的事件：" + event);
            }
        };
        client.start();
        var nodePath = "/test/shuangshu";
        client.getData().usingWatcher(w).forPath(nodePath);
        var newData = "test_new".getBytes();
        // 第一次修改
        client.setData().forPath(nodePath, newData);
        // 第二次修改
        client.setData().forPath(nodePath, newData);
        Thread.sleep(1000);
    }

    static class SnowFlakeId {
        private SnowFlakeId() {
        }

        private static volatile SnowFlakeId INSTANCE;

        private static int workerId;
        private static int seqNo;
        private static long prevTimestamp;
        private static boolean initialized;
        private static final int TIMESTAMP_OFFSET = 22;
        private static final int WORKER_ID_OFFSET = 12;
        private static final int MAX_SEQ_NO = (1 << 12) - 1;
        private static final long START_TIME = System.currentTimeMillis();;

        public void init(int wid) {
            workerId = wid;
            initialized = true;
        }

        public synchronized int getId() {
            if (!initialized) {
                throw new IllegalStateException("SnowFlakeId not initialized");
            }
            long timestamp = System.currentTimeMillis() - START_TIME;
            if (timestamp == prevTimestamp) {
                seqNo++;
                if (seqNo > MAX_SEQ_NO) {
                    this.waitUtilNextMs();
                    return getId();
                } else {
                    return ((int) timestamp << TIMESTAMP_OFFSET) | (workerId << WORKER_ID_OFFSET) | seqNo;
                }
            } else {
                seqNo = 0;
            }
            prevTimestamp = timestamp;
            return ((int) timestamp << TIMESTAMP_OFFSET) | (workerId << WORKER_ID_OFFSET) | seqNo;
        }

        private void waitUtilNextMs() {
            long nextTime = System.currentTimeMillis() + 1;
            while (System.currentTimeMillis() < nextTime) {
            }
        }

        public static SnowFlakeId getInstance() {
            if (INSTANCE == null) {
                synchronized (SnowFlakeId.class) {
                    if (INSTANCE == null) {
                        INSTANCE = new SnowFlakeId();
                    }
                    return INSTANCE;
                }
            }
            return INSTANCE;
        }

        public static int getId(String nodePath, String fullPath) {
            int index = fullPath.indexOf(nodePath) + nodePath.length();
            return Integer.valueOf(fullPath.substring(index, fullPath.length()));
        }

    }

    @Test
    public void testSnowFlake() throws Exception {
        // 测试基于ZK的雪花算法
        client.start();
        var workerIdPath = "/test/worker-";
        var newId = client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(
                workerIdPath,
                new byte[] {});
        var workerId = SnowFlakeId.getId(workerIdPath, newId);
        var snowFlakeIdGenerator = SnowFlakeId.getInstance();
        snowFlakeIdGenerator.init(workerId);
        for (int i = 0; i < 10; i++) {
            System.out.println(snowFlakeIdGenerator.getId());
        }
    }

    @Test
    @SuppressWarnings("all")
    public void testNodeCache() throws Exception {
        client.start();
        var nodePath = "/test/shuangshu";
        // 如果不存在节点，则创建
        if (client.checkExists().forPath(nodePath) == null) {
            client.create().forPath(nodePath, "".getBytes());
        }
        // 构造nodeCache
        final var nodeCache = new NodeCache(client, nodePath, false);
        // 创建listener
        var listener = new NodeCacheListener() {
            @Override
            public synchronized void nodeChanged() throws Exception {
                var childData = nodeCache.getCurrentData();
                if (childData != null) {
                    log.info("节点状态改变：{}", childData.getPath());
                    log.info("节点数据: {}", new String(childData.getData()));
                } else {
                    log.info("节点为空");
                }
            }
        };
        // 注册listener
        nodeCache.getListenable().addListener(listener);
        nodeCache.start();
        // 变更节点数据
        // 第一次修改
        client.setData().forPath(nodePath, "test_new__1".getBytes());
        Thread.sleep(1000);
        // 第二次修改
        client.setData().forPath(nodePath, "test_new__2".getBytes());
        Thread.sleep(1000);
        // 第三次修改
        client.setData().forPath(nodePath, "test_new__3".getBytes());
        // 第四次修改
        client.delete().forPath(nodePath);
        Thread.sleep(1000);
    }

    private void modifyZk(CuratorFramework client) throws Exception {
        var subNodePath = "/test/shuangshu/node";
        // 创建节点
        client.create().creatingParentsIfNeeded().forPath(subNodePath, "".getBytes());
        Thread.sleep(1000);
        // 更新节点
        client.setData().forPath(subNodePath, "good".getBytes());
        Thread.sleep(1000);
        // 删除节点
        client.delete().forPath(subNodePath);
        Thread.sleep(1000);
    }

    @Test
    public void testPathChildrenCache() throws Exception {
        client.start();
        var nodePath = "/test/shuangshu";
        // 创建PathChildrenCache
        var pathChildrenCache = new PathChildrenCache(client, nodePath, PathChildrenCacheMode.CACHE_DATA_AND_STAT);
        var listener = new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                var data = event.getData();
                if (data != null) {
                    log.info("子节点变化：{}", data);
                    switch (event.getType()) {
                        case CHILD_ADDED:
                            log.info("增加节点：path={}, data={}", data.getPath(), new String(data.getData()));
                            break;
                        case CHILD_UPDATED:
                            log.info("更新节点：path={}, data={}", data.getPath(), new String(data.getData()));
                            break;
                        case CHILD_REMOVED:
                            log.info("删除节点：path={}, data={}", data.getPath(), new String(data.getData()));
                            break;
                    }
                }
            }
        };
        pathChildrenCache.getListenable().addListener(listener);
        pathChildrenCache.start();
        modifyZk(client);
    }

    @Test
    public void testTreeCache() throws Exception {
        client.start();
        var nodePath = "/test/shuangshu";
        // 创建treeCache
        var treeCache = new TreeCache(client, nodePath);
        var listener = new TreeCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
                log.info("节点发生变动");
                var eventData = event.getData();
                if (eventData != null) {
                    switch (event.getType()) {
                        case NODE_ADDED:
                            log.info("增加节点：path={}, data={}", eventData.getPath(), new String(eventData.getData()));
                            break;
                        case NODE_UPDATED:
                            log.info("修改节点：path={}, data={}", eventData.getPath(), new String(eventData.getData()));
                            break;
                        case NODE_REMOVED:
                            log.info("删除节点：path={}, data={}", eventData.getPath(), new String(eventData.getData()));
                            break;
                    }
                }
            }
        };
        treeCache.getListenable().addListener(listener);
        treeCache.start();
        modifyZk(client);
    }

    @Test
    public void testCuratorCache() throws Exception {
        client.start();
        var nodePath = "/test/shuangshu";
        // 创建curatorCache
        var curatorCache = CuratorCache.build(client, nodePath);
        var listener = new CuratorCacheListener() {
            @Override
            public void event(Type type, ChildData oldData, ChildData data) {
                if (data != null) {
                    switch (type) {
                        case NODE_CREATED:
                            log.info("增加节点：path={}, data={}", data.getPath(), new String(data.getData()));
                            break;
                        case NODE_CHANGED:
                            log.info("修改节点：path={}, data={}", data.getPath(), new String(data.getData()));
                            break;

                    }
                } else if (oldData != null) {
                    if (type.equals(Type.NODE_DELETED)) {
                        log.info("删除节点：path={}, oldData={}", oldData.getPath(), new String(oldData.getData()));
                    }
                }
            }
        };
        curatorCache.listenable().addListener(listener);
        curatorCache.start();
        modifyZk(client);
    }
}
