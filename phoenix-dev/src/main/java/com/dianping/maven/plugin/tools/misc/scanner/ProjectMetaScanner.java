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
package com.dianping.maven.plugin.tools.misc.scanner;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * TODO Comment of ProjectMetaScanner
 * 
 * @author Leo Liang
 * 
 */
public class ProjectMetaScanner extends XmlScanner<ProjectPortEntry> {

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.dianping.phoenix.misc.scanner.XmlScanner#doScan(org.w3c.dom.Document)
     */
    @Override
    protected List<ProjectPortEntry> doScan(Document doc) throws Exception {
        List<ProjectPortEntry> resList = new ArrayList<ProjectPortEntry>();
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        XPathExpression expr = xpath.compile("//project");

        Object xmlRes = expr.evaluate(doc, XPathConstants.NODESET);

        NodeList nodes = (NodeList) xmlRes;

        for (int i = 0; i < nodes.getLength(); i++) {
            String projectName = nodes.item(i).getAttributes().getNamedItem("name").getNodeValue();
            XPathExpression portExpr = xpath.compile("//project[@name='" + projectName + "']/port/text()");
            Object portRes = portExpr.evaluate(doc, XPathConstants.NODESET);
            NodeList ports = (NodeList) portRes;
            int projectPort = -1;
            if (ports.getLength() >= 1) {
                projectPort = Integer.parseInt(ports.item(0).getNodeValue());
            }

            if (projectPort > 0 && StringUtils.isNotBlank(projectName)) {
                resList.add(new ProjectPortEntry(projectName, projectPort));
            }
        }

        return resList;
    }

}
