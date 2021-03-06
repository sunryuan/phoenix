/**
 * Project: phoenix-router
 * 
 * File Created at 2013-4-15
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.maven.plugin.tools.misc.file;

import java.io.File;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

/**
 * 
 * @author Leo Liang
 * 
 */
public class ProjectMetaGeneratorTest extends H2DBBasedTestCase {
    private File file = new File(System.getProperty("java.io.tmpdir", "."), "project-meta.xml");

    @Test
    public void test() throws Exception {
        String expected = "<projects>\n"
                        + "    <project name=\"shoppic-service\">\n"
                        + "        <port>2000</port>\n" 
                        + "    </project>\n" 
                        + "    <project name=\"group-service\">\n" 
                        + "        <port>2001</port>\n" 
                        + "    </project>\n" 
                        + "    <project name=\"account-service\">\n" 
                        + "        <port>2002</port>\n" 
                        + "    </project>\n" 
                        + "    <project name=\"user-service\">\n" 
                        + "        <port>2003</port>\n" 
                        + "    </project>\n"
                        + "</projects>";
        ProjectMetaContext context = new ProjectMetaContext("org.h2.Driver",
                "jdbc:h2:mem:hawk;DB_CLOSE_DELAY=-1", "", "");
        ProjectMetaGenerator smg = new ProjectMetaGenerator();
        smg.generate(file, context);
        Assert.assertEquals(expected, FileUtils.readFileToString(file));
    }

    protected String getDBBaseUrl() {
        return "jdbc:h2:mem:";
    }

    protected String getCreateScriptConfigFile() {
        return "project-meta-generator-test-create.xml";
    }

    protected String getDataFile() {
        return "project-meta-generator-test-data.xml";
    }

    public static void main(String[] args) throws Exception {
        ProjectMetaContext context = new ProjectMetaContext("com.mysql.jdbc.Driver",
                "jdbc:mysql://192.168.7.105:3306/hawk", "dpcom_hawk", "123456");
        ProjectMetaGenerator smg = new ProjectMetaGenerator();
        smg.generate(new File("/Users/leoleung/project-port.xml"), context);
    }
}
