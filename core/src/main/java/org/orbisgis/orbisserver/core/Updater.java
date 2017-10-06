package org.orbisgis.orbisserver.core;

import org.apache.felix.framework.BundleWiringImpl;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleReference;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.scheduler.Every;
import org.wisdom.api.concurrent.ManagedExecutorService;
import org.wisdom.api.scheduler.Scheduled;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 */
@Component
@Provides
@Instantiate
public class Updater extends DefaultController implements Scheduled {

    public Updater(){
    }

    @Every(period = 1, unit = TimeUnit.MINUTES)
    public void updateBundles() {
    }
}
