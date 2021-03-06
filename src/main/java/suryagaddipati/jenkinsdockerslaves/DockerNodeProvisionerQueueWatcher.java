package suryagaddipati.jenkinsdockerslaves;

import hudson.Extension;
import hudson.model.PeriodicWork;
import hudson.model.Queue;
import jenkins.model.Jenkins;

import java.util.List;
import java.util.logging.Logger;

@Extension
public class DockerNodeProvisionerQueueWatcher extends PeriodicWork {
    private static final Logger LOGGER = Logger.getLogger(DockerNodeProvisionerQueueWatcher.class.getName());
    @Override
    public long getRecurrencePeriod() {
        return 10*1000;
    }

    @Override
    protected void doRun() throws Exception {
        List<Queue.Item> items = Jenkins.getInstance().getQueue().getApproximateItemsQuickly();
        DockerSlaveConfiguration slaveConfig = DockerSlaveConfiguration.get();
        for(Queue.Item item : items){
            DockerSlaveInfo slaveInfo = item.getAction(DockerSlaveInfo.class);
            if( slaveInfo != null && item instanceof Queue.BuildableItem && !slaveInfo.isProvisioningInProgress()){
                if (! (slaveInfo.getProvisioningAttempts() >  slaveConfig.getMaxProvisioningAttempts())){
                    LOGGER.info("Scheduling build: "+ item.task);
                    OneshotBuildScheduler.scheduleBuild(((Queue.BuildableItem)item),true);
                }else{
                    LOGGER.info("Ignoring "+ item.task + " since it exceeded max provisioning attempts. Attempts :" + slaveInfo.getProvisioningAttempts());
                }
            }
        }
    }
}
