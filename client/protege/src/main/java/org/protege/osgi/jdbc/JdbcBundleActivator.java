package org.protege.osgi.jdbc;


import com.google.common.collect.ImmutableList;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.protege.osgi.jdbc.impl.OSGiJdbcDriverBundleActivator;
import org.protege.osgi.jdbc.preferences.JdbcPreferencesPanelBundleActivator;

/**
 * @author xiao
 */
public class JdbcBundleActivator implements BundleActivator {

    private final ImmutableList<BundleActivator> activators = ImmutableList.of(
            new OSGiJdbcDriverBundleActivator(),
            new JdbcPreferencesPanelBundleActivator());

    @Override
    public void start(BundleContext context) throws Exception {
        for (BundleActivator activator : activators)
            activator.start(context);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        for (BundleActivator activator : activators)
            activator.stop(context);
    }
}
