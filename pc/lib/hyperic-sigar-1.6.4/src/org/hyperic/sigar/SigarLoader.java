/*
 * Copyright (c) 2006-2008 Hyperic, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hyperic.sigar;

import org.hyperic.jni.ArchLoader;
import org.hyperic.jni.ArchLoaderException;
import org.hyperic.jni.ArchName;
import org.hyperic.jni.ArchNotSupportedException;

public class SigarLoader extends ArchLoader {
    public static final String PROP_SIGAR_JAR_NAME = "sigar.jar.name";

    private static String location = null;
    private static String nativeName = null;

    public SigarLoader(Class loaderClass) {
        super(loaderClass);
    }

    //XXX same as super.getArchLibName()
    //but db2monitor.jar gets loaded first in jboss
    //results in NoSuchMethodError
    public String getArchLibName()
        throws ArchNotSupportedException {

      //System.out.println("getArchLibName():" + getName() + "-" + ArchName.getName());
        return getName() + "-" + ArchName.getName();
    }

    public String getDefaultLibName()
        throws ArchNotSupportedException {

     // System.out.println("getDefaultLibName():" + getArchLibName());
        return getArchLibName(); //drop "java" prefix
    }

    //override these methods to ensure our ClassLoader
    //loads the native library.
    protected void systemLoadLibrary(String name) {
     // System.out.println("systemLoadLibrary():" + name);
        System.loadLibrary(name);
    }

    protected void systemLoad(String name) {
      //System.out.println("systemLoad():" + name);
        System.load(name);
    }

    public String getJarName() {
      //System.out.println("getJarName():" + System.getProperty(PROP_SIGAR_JAR_NAME,          super.getJarName()));
        return System.getProperty(PROP_SIGAR_JAR_NAME,
                                  super.getJarName());
    }

    public static void setSigarJarName(String jarName) {
      //System.out.println("setSigarJarName():" + PROP_SIGAR_JAR_NAME + jarName);
        System.setProperty(PROP_SIGAR_JAR_NAME, jarName);
    }

    public static String getSigarJarName() {
      //System.out.println("getSigarJarName():" + System.getProperty(PROP_SIGAR_JAR_NAME));
        return System.getProperty(PROP_SIGAR_JAR_NAME);
    }

    /**
     * Returns the path where sigar.jar is located.
     */
    public synchronized static String getLocation() {
      //System.out.println("getLocation():");
        if (location == null) {
            SigarLoader loader = new SigarLoader(Sigar.class);
            try {
                location = loader.findJarPath(getSigarJarName());
            } catch (ArchLoaderException e) {
                location = ".";
            }
        }
        System.out.println("getLocation():" + location);
        return location;
    }

    /**
     * Returns the name of the native sigar library.
     */
    public synchronized static String getNativeLibraryName() {
      System.out.println("getNativeLibraryName():");
        if (nativeName == null) {
            SigarLoader loader = new SigarLoader(Sigar.class);

            try {
                nativeName = loader.getLibraryName();
            } catch (ArchNotSupportedException e) {
                nativeName = null;
            }
        }

        System.out.println("getNativeLibraryName():" + nativeName);
        return nativeName;
    }
}
