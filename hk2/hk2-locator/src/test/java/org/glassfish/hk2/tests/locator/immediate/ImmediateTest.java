/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.hk2.tests.locator.immediate;

import java.util.List;

import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.FactoryDescriptors;
import org.glassfish.hk2.api.Immediate;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class ImmediateTest {
    /* package */ static final String EXPECTED = "Exepcted Immediate Exception";
    
    /**
     * Tests that an immediate service is started and stopped when
     * added and removed
     * @throws InterruptedException 
     */
    @Test
    public void testBasicImmediate() throws InterruptedException {
        WaitableImmediateService.clear();
        
        ServiceLocator locator = LocatorHelper.create();
        
        ServiceLocatorUtilities.enableImmediateScope(locator);
        
        List<ActiveDescriptor<?>> ims = ServiceLocatorUtilities.addClasses(locator, WaitableImmediateService.class);
        
        int numCreations = WaitableImmediateService.waitForCreationsGreaterThanZero(5 * 1000);
        Assert.assertEquals(1, numCreations);
        Assert.assertEquals(0, WaitableImmediateService.getNumDeletions());
        
        ServiceLocatorUtilities.removeOneDescriptor(locator, ims.get(0));
        
        int numDeletions = WaitableImmediateService.waitForDeletionsGreaterThanZero(5 * 1000);
        Assert.assertEquals(1, numDeletions);
        Assert.assertEquals(1, WaitableImmediateService.getNumCreations());
    }
    
    /**
     * This test ensures that services added *prior* to the scope being added
     * will get created
     * 
     * @throws InterruptedException
     */
    @Test
    public void testImmediateAfterthought() throws InterruptedException {
        WaitableImmediateService.clear();
        
        ServiceLocator locator = LocatorHelper.create();
        
        List<ActiveDescriptor<?>> ims = ServiceLocatorUtilities.addClasses(locator, WaitableImmediateService.class);
        
        // No scope added, not created yet
        Assert.assertEquals(0, WaitableImmediateService.getNumCreations());
        Assert.assertEquals(0, WaitableImmediateService.getNumDeletions());
        
        // Scope added now
        ServiceLocatorUtilities.enableImmediateScope(locator);
        
        int numCreations = WaitableImmediateService.waitForCreationsGreaterThanZero(5 * 1000);
        Assert.assertEquals(1, numCreations);
        Assert.assertEquals(0, WaitableImmediateService.getNumDeletions());
        
        ServiceLocatorUtilities.removeOneDescriptor(locator, ims.get(0));
        
        int numDeletions = WaitableImmediateService.waitForDeletionsGreaterThanZero(5 * 1000);
        Assert.assertEquals(1, numDeletions);
        Assert.assertEquals(1, WaitableImmediateService.getNumCreations());
    }
    
    /**
     * This test ensures that the error handler is called
     * 
     * @throws InterruptedException
     */
    @Test
    public void testImmediateFailedInConstructor() throws InterruptedException {
        ServiceLocator locator = LocatorHelper.create();
        ServiceLocatorUtilities.enableImmediateScope(locator);
        
        List<ActiveDescriptor<?>> ims = ServiceLocatorUtilities.addClasses(locator,
                ConstructorFailingImmediateService.class,
                ImmediateErrorHandlerImpl.class);
        
        ImmediateErrorHandlerImpl handler = locator.getService(ImmediateErrorHandlerImpl.class);
        
        List<ErrorData> errorDatum = handler.waitForAtLeastOneConstructionError(5 * 1000);
        Assert.assertEquals(1, errorDatum.size());
        
        Assert.assertEquals(ims.get(0), errorDatum.get(0).getDescriptor());
        Assert.assertTrue(errorDatum.get(0).getThrowable().toString().contains(EXPECTED));
    }
    
    /**
     * This test ensures that the error handler is called when post construct fails
     * 
     * @throws InterruptedException
     */
    @Test
    public void testImmediateFailedInPostConstruct() throws InterruptedException {
        ServiceLocator locator = LocatorHelper.create();
        ServiceLocatorUtilities.enableImmediateScope(locator);
        
        List<ActiveDescriptor<?>> ims = ServiceLocatorUtilities.addClasses(locator,
                PostConstructFailingImmediateService.class,
                ImmediateErrorHandlerImpl.class);
        
        ImmediateErrorHandlerImpl handler = locator.getService(ImmediateErrorHandlerImpl.class);
        
        List<ErrorData> errorDatum = handler.waitForAtLeastOneConstructionError(5 * 1000);
        Assert.assertEquals(1, errorDatum.size());
        
        Assert.assertEquals(ims.get(0), errorDatum.get(0).getDescriptor());
        Assert.assertTrue(errorDatum.get(0).getThrowable().toString().contains(EXPECTED));
    }
    
    /**
     * This test ensures that the error handler is called when pre destroy fails
     * 
     * @throws InterruptedException
     */
    @Test
    public void testImmediateFailedInPreDestroy() throws InterruptedException {
        ServiceLocator locator = LocatorHelper.create();
        ServiceLocatorUtilities.enableImmediateScope(locator);
        
        List<ActiveDescriptor<?>> ims = ServiceLocatorUtilities.addClasses(locator,
                PreDestroyFailingImmediateService.class,
                ImmediateErrorHandlerImpl.class);
        
        ImmediateErrorHandlerImpl handler = locator.getService(ImmediateErrorHandlerImpl.class);
        
        // Doing this ensures that the immediate service is registered
        Assert.assertNotNull(locator.getService(PreDestroyFailingImmediateService.class));
        
        ServiceLocatorUtilities.removeOneDescriptor(locator, ims.get(0));
        
        List<ErrorData> errorDatum = handler.waitForAtLeastOneDestructionError(5 * 1000);
        Assert.assertEquals(1, errorDatum.size());
        
        Assert.assertEquals(ims.get(0), errorDatum.get(0).getDescriptor());
        Assert.assertTrue(errorDatum.get(0).getThrowable().toString().contains(EXPECTED));
    }
    
    /**
     * Tests that an immediate service is started and stopped when
     * added and removed and the service is created by a Factory
     * 
     * @throws InterruptedException 
     */
    @Test
    public void testFactoryImmediate() throws InterruptedException {
        ServiceLocator locator = LocatorHelper.create();
        ServiceLocatorUtilities.enableImmediateScope(locator);
        
        DynamicConfiguration cd = locator.getService(DynamicConfigurationService.class).createDynamicConfiguration();
        
        FactoryDescriptors fd = BuilderHelper.link(ImmediateServiceFactory.class.getName()).
          to(GenericImmediateService.class).
          in(Immediate.class.getName()).
          buildFactory(Singleton.class);
        
        FactoryDescriptors added = cd.bind(fd);
        
        cd.commit();
        
        ImmediateServiceFactory factory = locator.getService(ImmediateServiceFactory.class);
        
        Assert.assertTrue(factory.waitToCreate(5 * 1000));
        
        ServiceLocatorUtilities.removeOneDescriptor(locator, added.getFactoryAsAFactory());
        
        Assert.assertTrue(factory.waitToDestroy(5 * 1000));
    }
    
    /**
     * This test is a little non-black-boxy.  In the current implementation the
     * one thread that does the work has a decay time, which means that if it has
     * no work for 20 seconds or so, it will then go away.  This is meant to keep
     * the system from spawning a lot of threads during a flurry of configuration
     * events such as what happens at the boot or shutdown of a system.
     * 
     * This test makes sure that is nominally working by adding an immediate service,
     * waiting a second, and then adding another and making sure they were
     * both created on the same thread
     * @throws InterruptedException 
     */
    @Test
    public void testThreadDecay() throws InterruptedException {
        clearTid();
        
        ServiceLocator locator = LocatorHelper.create();
        
        ServiceLocatorUtilities.enableImmediateScope(locator);
        
        ServiceLocatorUtilities.addClasses(locator,
                ImmediateTidRecorder.class);
        
        long firstTid = waitForTid(5 * 1000);
        Assert.assertTrue(firstTid > 0);
        
        clearTid();
        
        long dummyTid = waitForTid(500);
        
        // This is a good test that perhaps the first
        // service is not created twice
        Assert.assertEquals(-1, dummyTid);
        
        // Add a second, should happen on that old idling thread
        ServiceLocatorUtilities.addClasses(locator,
                ImmediateTidRecorder.class);
        
        long secondTid = waitForTid(5 * 1000);
        Assert.assertTrue(secondTid > 0);
        
        Assert.assertEquals(firstTid, secondTid);
    }
    
    /**
     * Tests that an immediate service is started and stopped when
     * added and removed and the service is created by a Factory
     * 
     * @throws InterruptedException 
     */
    @Test
    public void testDestroyedWhenLocatorShutdown() throws InterruptedException {
        ServiceLocator locator = LocatorHelper.getServiceLocator(
                AnotherGetsDestroyedService.class,
                GetsDestroyedService.class,
                GetsDestroyedPerLookupService.class);
        
        ServiceLocatorUtilities.enableImmediateScope(locator);
        
        Thread.sleep(200);
        
        AnotherGetsDestroyedService agds = locator.getService(AnotherGetsDestroyedService.class);
        GetsDestroyedService gds = locator.getService(GetsDestroyedService.class);
        GetsDestroyedPerLookupService gdpls = gds.getPerLookupService();
        
        Assert.assertFalse(agds.isDestroyed());
        Assert.assertFalse(gds.isDestroyed());
        Assert.assertFalse(gdpls.isDestroyed());
        
        locator.shutdown();
        
        Assert.assertTrue(agds.isDestroyed());
        Assert.assertTrue(gds.isDestroyed());
        Assert.assertTrue(gdpls.isDestroyed());
    }
    
    private final static Object sLock = new Object();
    private static long immediateTid = -1;
    
    /* package */ static void registerTid(long tid) {
        synchronized (sLock) {
            immediateTid = tid;
            sLock.notifyAll();
        }
    }
    
    private static long waitForTid(long waitTime) throws InterruptedException {
        synchronized (sLock) {
            while (immediateTid == -1 && waitTime > 0) {
                long elapsedTime = System.currentTimeMillis();
                sLock.wait(waitTime);
                elapsedTime = System.currentTimeMillis() - elapsedTime;
                waitTime -= elapsedTime;
            }
            
            return immediateTid;
        }
        
        
    }
    
    private static void clearTid() {
        synchronized (sLock) {
            immediateTid = -1;
        }
    }

}
