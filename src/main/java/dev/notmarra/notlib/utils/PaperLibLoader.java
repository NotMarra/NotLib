package dev.notmarra.notlib.utils;

import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;

public class PaperLibLoader implements PluginLoader {
    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();

        resolver.addDependency(new Dependency(new DefaultArtifact("org.mariadb.jdbc:mariadb-java-client:3.5.7"), null));

        resolver.addDependency(new Dependency(new DefaultArtifact("org.xerial:sqlite-jdbc:3.51.2.0"), null));

        resolver.addRepository(new RemoteRepository.Builder("central", "default", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR).build());

        classpathBuilder.addLibrary(resolver);
    }
}
