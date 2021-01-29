package org.protege.osgi.jdbc;


import com.google.common.collect.ImmutableList;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.protege.osgi.jdbc.prefs.Activator;

/**
 * @author xiao
 */
public class JdbcBundleActivator implements BundleActivator {

    private ImmutableList<BundleActivator> activators;

    @Override
    public void start(BundleContext context) throws Exception {
        activators = ImmutableList.of(
                new org.protege.osgi.jdbc.impl.Activator(),
                new Activator());

        for (BundleActivator activator : activators)
            activator.start(context);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        for (BundleActivator activator : activators)
            activator.stop(context);

        activators = null;
    }
}
