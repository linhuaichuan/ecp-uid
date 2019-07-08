package com.myzmds.ecp.core.uid.worker;

import java.util.Random;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.myzmds.ecp.core.uid.baidu.utils.DockerUtils;
import com.myzmds.ecp.core.uid.util.NetUtils;
import com.myzmds.ecp.core.uid.worker.dao.WorkerNodeDAO;
import com.myzmds.ecp.core.uid.worker.entity.WorkerNode;

/**
 * DB编号分配器(利用数据库来管理)
 * @author yutianbao
 */
public class DisposableWorkerIdAssigner implements WorkerIdAssigner {
    private static final Logger LOGGER = LoggerFactory.getLogger(DisposableWorkerIdAssigner.class);
    
    @Resource
    private WorkerNodeDAO workerNodeDAO;

    /**
     * Assign worker id base on database.<p>
     * If there is host name & port in the environment, we considered that the node runs in Docker container<br>
     * Otherwise, the node runs on an actual machine.
     * 
     * @return assigned worker id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public long assignWorkerId() {
        // build worker node entity
        WorkerNode workerNodeEntity = buildWorkerNode();

        // add worker node for new (ignore the same IP + PORT)
        workerNodeDAO.addWorkerNode(workerNodeEntity);
        LOGGER.info("Add worker node:" + workerNodeEntity);

        return workerNodeEntity.getId();
    }

    /**
     * Build worker node entity by IP and PORT
     */
    private WorkerNode buildWorkerNode() {
        WorkerNode workerNodeEntity = new WorkerNode();
        if (DockerUtils.isDocker()) {
            workerNodeEntity.setType(WorkerNodeType.CONTAINER.value());
            workerNodeEntity.setHostName(DockerUtils.getDockerHost());
            workerNodeEntity.setPort(DockerUtils.getDockerPort());

        } else {
            workerNodeEntity.setType(WorkerNodeType.ACTUAL.value());
            workerNodeEntity.setHostName(NetUtils.getLocalInetAddress().getHostAddress());
            workerNodeEntity.setPort(System.currentTimeMillis() + "-" + RANDOM.nextInt(100000));
        }

        return workerNodeEntity;
    }
    
    private static final Random RANDOM = new Random();
}
