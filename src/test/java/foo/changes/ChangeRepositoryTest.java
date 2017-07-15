package foo.changes;

import foo.repository.ChangeRepository;
import foo.utils.PrintUtil;
import foo.workspace.Workspace;
import org.junit.Test;

import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;

public class ChangeRepositoryTest {
    private Workspace workspace = new Workspace();

    @Test
    public void fetch() throws Exception {
        URL url = ChangeRepositoryTest.class.getResource("/changes.yaml");
        List<Change> changes = ChangeRepository.fetch(url);

        workspace.apply(changes);

//        assertEquals(6, changes.size());

        assertEquals(
            "project My Project\n" +
            "  package com.example.package\n" +
            "    def foo [x y]\n" +
            "      plus\n" +
            "        x: x\n" +
            "        y: y\n",
            PrintUtil.toString(workspace.getProjects().get(0)));
    }

}