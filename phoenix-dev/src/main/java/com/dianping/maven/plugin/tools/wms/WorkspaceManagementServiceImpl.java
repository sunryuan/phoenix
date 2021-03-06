/**
 * Project: phoenix-maven-plugin
 * 
 * File Created at 2013-5-14
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
package com.dianping.maven.plugin.tools.wms;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.dianping.maven.plugin.tools.misc.file.ContainerBizServerGenerator;
import com.dianping.maven.plugin.tools.misc.file.ContainerPomXMLGenerator;
import com.dianping.maven.plugin.tools.misc.file.ContainerWebXMLGenerator;
import com.dianping.maven.plugin.tools.vcs.CodeRetrieveConfig;
import com.dianping.maven.plugin.tools.vcs.CodeRetrieveService;
import com.dianping.maven.plugin.tools.vcs.GitCodeRetrieveConfig;
import com.dianping.maven.plugin.tools.vcs.SVNCodeRetrieveConfig;

/**
 * 
 * @author Leo Liang
 * 
 */
public class WorkspaceManagementServiceImpl implements WorkspaceManagementService {

    private final static String LINE_SEPARATOR = System.getProperty("line.separator");
    private RepositoryManager   repositoryManager;

    public void setRepositoryManager(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    @Override
    public void create(WorkspaceContext context, OutputStream out) throws WorkspaceManagementException {
        if (context.getProjects() != null && context.getBaseDir() != null && out != null) {

            printContent("Generating phoenix workspace...", out);

            if (context.getBaseDir().exists()) {
                try {
                    FileUtils.cleanDirectory(context.getBaseDir());
                } catch (IOException e) {
                    throw new WorkspaceManagementException(e);
                }
            }
            printContent(String.format("Workspace folder(%s) cleared...", context.getBaseDir()), out);

            try {
                FileUtils.forceMkdir(context.getBaseDir());
            } catch (IOException e) {
                throw new WorkspaceManagementException(e);
            }
            printContent(String.format("Workspace folder(%s) created...", context.getBaseDir()), out);

            for (String project : context.getProjects()) {
                Repository repository = repositoryManager.find(project);
                if (repository == null) {
                    printContent(String.format("Project(%s) not found...", project), out);
                }

                CodeRetrieveConfig codeRetrieveConfig = toCodeRetrieveConfig(repository, new File(context.getBaseDir(),
                        project).getAbsolutePath(), out);
                if (codeRetrieveConfig != null) {
                    printContent(
                            String.format("Checking out project %s(repo:%s)...", project, repository.getRepoUrl()), out);
                    CodeRetrieveService.getInstance().retrieveCode(codeRetrieveConfig);
                } else {
                    printContent(String.format("Project repository(%s) unknown...", project), out);
                }
            }

            printContent("Generating phoenix-container...", out);

            generateContainerProject(context);

            printContent("Phoenix workspace generated...", out);

        } else {
            throw new WorkspaceManagementException("projects/basedir can not be null");
        }
    }

    private void generateContainerProject(WorkspaceContext context) throws WorkspaceManagementException {
        File projectBase = new File(context.getBaseDir(), "phoenix-container");
        File sourceFolder = new File(projectBase, "src/main/java");
        File resourceFolder = new File(projectBase, "src/main/resources");
        File webinfFolder = new File(projectBase, "src/main/webapp/WEB-INF");
        try {
            FileUtils.forceMkdir(sourceFolder);
            FileUtils.forceMkdir(resourceFolder);
            FileUtils.forceMkdir(webinfFolder);

            FileUtils.copyFileToDirectory(FileUtils.toFile(this.getClass().getResource("/byteman-2.1.2.jar")),
                    resourceFolder);

            // web.xml
            ContainerWebXMLGenerator containerWebXMLGenerator = new ContainerWebXMLGenerator();
            containerWebXMLGenerator.generate(new File(webinfFolder, "web.xml"), null);

            // pom.xml
            ContainerPomXMLGenerator containerPomXMLGenerator = new ContainerPomXMLGenerator();
            Map<String, String> containerPomXMLGeneratorContext = new HashMap<String, String>();
            containerPomXMLGeneratorContext.put("phoenixRouterVersion", context.getPhoenixRouterVersion());
            containerPomXMLGenerator.generate(new File(projectBase, "pom.xml"),
                    containerPomXMLGeneratorContext);
            
            // BizServer.java
            ContainerBizServerGenerator containerBizServerGenerator = new ContainerBizServerGenerator();
            containerBizServerGenerator.generate(new File(sourceFolder, "com/dianping/phoenix/container/BizServer.java"), null);
            
            context.setBootstrapProjectDir(projectBase);
        } catch (Exception e) {
            throw new WorkspaceManagementException(e);
        }
    }

    private void printContent(String content, OutputStream out) {

        try {
            out.write(("---------------------------------------------" + LINE_SEPARATOR).getBytes());
            out.write((content + LINE_SEPARATOR).getBytes());
            out.write(("---------------------------------------------" + LINE_SEPARATOR).getBytes());
        } catch (IOException e) {
            // ignore
        }
    }

    private CodeRetrieveConfig toCodeRetrieveConfig(Repository repository, String path, OutputStream out) {
        if (repository instanceof SvnRepository) {
            return new SVNCodeRetrieveConfig(repository.getRepoUrl(), path, repository.getUser(), repository.getPwd(),
                    out, ((SvnRepository) repository).getRevision());
        } else if (repository instanceof GitRepository) {
            return new GitCodeRetrieveConfig(repository.getRepoUrl(), path, repository.getUser(), repository.getPwd(),
                    out, ((GitRepository) repository).getBranch());
        } else {
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        WorkspaceManagementServiceImpl wms = new WorkspaceManagementServiceImpl();
        wms.setRepositoryManager(new RepositoryManager() {

            @Override
            public Repository find(String project) {
                if ("shop-web".equals(project)) {
                    return new SvnRepository("http://192.168.8.45:81/svn/dianping/dianping/shop/trunk/shop-web/",
                            "-", "-", -1l);
                } else if ("user-web".equals(project)) {
                    return new SvnRepository("http://192.168.8.45:81/svn/dianping/dianping/user/trunk/user-web/",
                            "-", "-", -1l);
                } else {
                    return null;
                }
            }
        });
        WorkspaceContext context = new WorkspaceContext();
        List<String> projects = new ArrayList<String>();
        projects.add("shop-web");
        // projects.add("user-web");
        context.setProjects(projects);
        context.setBaseDir(new File("/Users/leoleung/test"));
        wms.create(context, System.out);
    }
}
