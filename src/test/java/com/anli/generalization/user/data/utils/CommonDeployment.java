package com.anli.generalization.user.data.utils;

import java.io.File;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

public class CommonDeployment {

    public static WebArchive getDeployment() {
        File[] dependencies = Maven.resolver().loadPomFromFile("pom.xml")
                .importRuntimeAndTestDependencies().resolve().withTransitivity().asFile();
        JavaArchive testArchive = ShrinkWrap.create(JavaArchive.class, "jpa-user-data.jar")
                .addPackages(true, "com/anli/generalization/user/data")
                .addAsResource("META-INF/persistence-template.xml", "META-INF/persistence.xml");
        return ShrinkWrap.create(WebArchive.class, "jpa-user-data-test.war")
                .addAsLibrary(testArchive)
                .addAsLibraries(dependencies);
    }
}
